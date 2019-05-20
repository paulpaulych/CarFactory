package carfactory;

import carfactory.exception.CarFactoryConfigException;
import carfactory.preferences.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

public class CarFactory extends Observable {

    private static final Logger log = LogManager.getLogger();

    private static final int DEFAULT_TASK_TIME = 3000;
    
    private boolean running = false;

    private Preferences prefs = Preferences.userNodeForPackage(this.getClass());

    private Storage<Car> carStorage = new Storage<>(prefs.getInt(Config.CAR_STORAGE_SIZE, 10));
    private Storage<CarBody> bodyStorage = new Storage<>(prefs.getInt(Config.BODY_STORAGE_SIZE, 10));
    private Storage<CarEngine> engineStorage = new Storage<>(prefs.getInt(Config.ENGINE_STORAGE_SIZE, 10));
    private Storage<CarAccessories> accessoriesStorage = new Storage<>(prefs.getInt(Config.ACCESSORIES_STORAGE_SIZE, 10));

    private ExecutorService dealerPool;
    private ExecutorService bodySupplier;
    private ExecutorService engineSupplier;
    private ExecutorService accessoriesSupplierPool;
    private CarStorageController carStorageController;

    private int dealerTime = DEFAULT_TASK_TIME;
    private int bodySupplierTime = DEFAULT_TASK_TIME;
    private int engineSupplierTime = DEFAULT_TASK_TIME;
    private int accessoriesSupplierTime = DEFAULT_TASK_TIME;

    private AtomicInteger bodyTotalMade = new AtomicInteger(0);
    private AtomicInteger engineTotalMade = new AtomicInteger(0);
    private AtomicInteger accessoriesTotalMade = new AtomicInteger(0);
    private AtomicInteger carTotalMade = new AtomicInteger(0);

    private int dealersNum;
    private int accessoriesSuppliersNum;

    public CarFactory() throws CarFactoryConfigException{
        try(InputStream is = new FileInputStream(Config.CONF_FILE_NAME)){
            Preferences.importPreferences(is);
        } catch (IOException | InvalidPreferencesFormatException e){
            log.error(e);
            throw new CarFactoryConfigException(e);
        }
    }

    public void run() throws NoSuchMethodException{

        running = true;

        bodySupplier = Executors.newSingleThreadExecutor();
        bodySupplier.submit(new SupplierTask<>(CarBody.class,
                bodyStorage,
                bodySupplierTime,
                bodyTotalMade));

        engineSupplier = Executors.newSingleThreadExecutor();
        engineSupplier.submit(new SupplierTask<>(CarEngine.class,
                engineStorage,
                engineSupplierTime,
                engineTotalMade));

        accessoriesSuppliersNum = prefs.getInt(Config.ACCESSORIES_SUPPLIERS_NUM, 1);
        accessoriesSupplierPool = Executors.newFixedThreadPool(accessoriesSuppliersNum);
        for(int i = 0; i < accessoriesSuppliersNum; ++i){
            accessoriesSupplierPool.submit(new SupplierTask<>(CarAccessories.class,
                    accessoriesStorage,
                    accessoriesSupplierTime,
                    accessoriesTotalMade));
        }

        dealersNum = prefs.getInt(Config.DEALERS_NUM, 1);
        dealerPool = Executors.newFixedThreadPool(dealersNum);
        for(int i = 0; i < dealersNum; ++i){
            dealerPool.submit(dealerTask);
        }

        carStorageController = new CarStorageController();
        //костыль. Надо как-то оповестить carStorageController о начале работы
        carStorageController.update(carStorage, new Object());
    }

    public void stop(){

        if(!running){
            return;
        }

        running = false;

        bodySupplier.shutdownNow();
        engineSupplier.shutdownNow();

        accessoriesSupplierPool.shutdownNow();
        dealerPool.shutdownNow();

        carStorageController.stop();

        bodyStorage.clear();
        engineStorage.clear();
        accessoriesStorage.clear();
        carStorage.clear();

        log.debug("carFactory.stop()");
    }

    class CarStorageController implements Observer {

        ExecutorService workerPool;

        private final Runnable workerTask = () -> {
            log.debug("I'm worker");
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    CarBody carBody = bodyStorage.get();
                    CarEngine carEngine = engineStorage.get();
                    CarAccessories carAccessories = accessoriesStorage.get();
                    TimeUnit.MILLISECONDS.sleep(prefs.getInt(Config.WORKER_TIME, DEFAULT_TASK_TIME));
                    Car car = new Car(carBody, carEngine, carAccessories);
                    carStorage.add(car);
                    carTotalMade.incrementAndGet();
                    log.debug("Car (ID: ", car.getID(), ") added to the CarStorage");
                    setChanged();
                    notifyObservers();
                } catch (InterruptedException e) {
                    log.debug("Interrupted. Finished.");
                    break;
                }
            }
        };

        CarStorageController() {
            carStorage.addObserver(this);
            workerPool = Executors.newFixedThreadPool(prefs.getInt(Config.WORKERS_NUM, 1));
        }

        void stop() {
            workerPool.shutdownNow();
        }

        @Override
        public void update(Observable o, Object arg) {
            Storage<Car> storage = (Storage<Car>)o;
            if (storage.size() / (double) storage.getMaxSize() < 0.2) {
                int newTasksNum = 3;//magic??
                for (int i = 0; i < newTasksNum; ++i) {
                    workerPool.submit(workerTask);
                }
            }
        }
    }

    private class SupplierTask<T extends Numerable> implements Runnable {

        private Constructor<T> carPartConstructor;
        private Storage<T> storage;
        private int sleepTime;
        private String carPartName;
        AtomicInteger counter;

        SupplierTask(Class<T> carPartType, Storage<T> storage, int sleepTime, AtomicInteger counter) throws NoSuchMethodException {
            this.carPartConstructor = carPartType.getConstructor();
            this.storage = storage;
            this.sleepTime = sleepTime;
            this.carPartName = carPartType.getName();
        }

        @Override
        public void run() {
            log.debug("I'm ", carPartName, "Supplier.");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(sleepTime);
                    storage.add(carPartConstructor.newInstance());
                    counter.incrementAndGet();
                    log.debug(carPartName, " added to the bodyStorage.");
                    setChanged();
                    notifyObservers();
                } catch (InterruptedException e) {
                    log.debug("Interrupted. Finished.");
                    break;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    log.error(e);
                    break;
                }
            }
        }
    }

    private final Runnable dealerTask = () -> { 
        log.debug("I'm dealer.");
        while(!Thread.currentThread().isInterrupted()) {
            try{
                TimeUnit.MILLISECONDS.sleep(dealerTime);
                Car car = carStorage.get();
                log.debug("Car taken from the storage.");
                //log.info("");
                setChanged();
                notifyObservers();
            }catch(InterruptedException e) {
                log.debug("Interrupted. Finished.");
                break;
            }
        }
    };

    public int getBodyStorageSize(){
        return bodyStorage.size();
    }
    public int getEngineStorageSize(){
        return engineStorage.size();
    }
    public int getAccessoriesStorageSize(){
        return accessoriesStorage.size();
    }
    public int getCarStorageSize(){
        return carStorage.size();
    }
    public int getBodyStorageMaxSize(){
        return prefs.getInt(Config.BODY_STORAGE_SIZE, 10);
    }
    public int getEngineStorageMaxSize(){
        return prefs.getInt(Config.ENGINE_STORAGE_SIZE, 10);
    }
    public int getAccessoriesStorageMaxSize(){
        return prefs.getInt(Config.ACCESSORIES_STORAGE_SIZE, 10);
    }
    public int getCarStorageMaxSize(){
        return prefs.getInt(Config.CAR_STORAGE_SIZE, 10);
    }
    public int getDealersNum(){
        return dealersNum;
    }

    public int getBodyTotalMade(){
        return bodyTotalMade.get();
    }
    public int getEngineTotalMade(){
        return engineTotalMade.get();
    }
    public int getAccessoriesTotalMade(){
        return accessoriesTotalMade.get();
    }
    public int getCarTotalMade(){
        return carTotalMade.get();
    }

    public void setDealerTime(int dealerTime) {
        this.dealerTime = dealerTime;
    }
    public void setBodySupplierTime(int bodySupplierTime) {
        this.bodySupplierTime = bodySupplierTime;
    }
    public void setEngineSupplierTime(int engineSupplierTime) {
        this.engineSupplierTime = engineSupplierTime;
    }
    public void setAccessoriesSupplierTime(int accessoriesSupplierTime) {
        this.accessoriesSupplierTime = accessoriesSupplierTime;
    }

    public boolean isRunning(){
        return running;
    }

}