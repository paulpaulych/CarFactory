package carfactory;

import carfactory.exception.CarFactoryConfigException;
import carfactory.exception.CarFactoryReflectiveException;
import carfactory.preferences.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;
import java.util.Scanner;

public class CarFactory extends Observable {

    private static final Logger log = LogManager.getLogger();

    private static final int DEFAULT_TASK_TIME = 3000;
    private static final int DEFAULT_WORKER_NUMBER = 1;
    private static final int DEFAULT_STORAGE_SIZE = 1;
    
    private boolean running = false;

    private Preferences prefs = Preferences.userNodeForPackage(this.getClass());

    private Storage<Car> carStorage;
    private Storage<CarBody> bodyStorage;
    private Storage<CarEngine> engineStorage;
    private Storage<CarAccessories> accessoriesStorage;

    private Supplier<CarBody> bodySupplier;
    private Supplier<CarEngine> engineSupplier;
    private Supplier<CarAccessories>[] accessoriesSuppliers;
    private Thread[] dealers;
    private CarStorageController carStorageController;

    private int dealerTime = DEFAULT_TASK_TIME;
    private int bodySupplierTime = DEFAULT_TASK_TIME;
    private int engineSupplierTime = DEFAULT_TASK_TIME;
    private int accessoriesSupplierTime = DEFAULT_TASK_TIME;

    private int dealersNum;
    private int accessoriesSuppliersNum;

    public CarFactory() throws CarFactoryConfigException{
        try(InputStream is = new FileInputStream(Config.CONF_FILE_NAME)){
            Preferences.importPreferences(is);
        } catch (IOException | InvalidPreferencesFormatException e){
            log.error(e);
            throw new CarFactoryConfigException(e);
        }
        dealersNum = prefs.getInt(Config.DEALERS_NUM, DEFAULT_WORKER_NUMBER);
        accessoriesSuppliersNum = prefs.getInt(Config.ACCESSORIES_SUPPLIERS_NUM, DEFAULT_WORKER_NUMBER);
        carStorage = new Storage<>(prefs.getInt(Config.CAR_STORAGE_SIZE, DEFAULT_STORAGE_SIZE));
        bodyStorage = new Storage<>(prefs.getInt(Config.BODY_STORAGE_SIZE, DEFAULT_STORAGE_SIZE));
        engineStorage = new Storage<>(prefs.getInt(Config.ENGINE_STORAGE_SIZE, DEFAULT_STORAGE_SIZE));
        accessoriesStorage = new Storage<>(prefs.getInt(Config.ACCESSORIES_STORAGE_SIZE, DEFAULT_STORAGE_SIZE));
    }

    public void run() throws CarFactoryReflectiveException {

        running = true;

        bodySupplier = new Supplier<>(CarBody.class,
                bodyStorage,
                bodySupplierTime);
        engineSupplier = new Supplier<>(CarEngine.class,
                engineStorage,
                engineSupplierTime);
        accessoriesSuppliers = new Supplier[accessoriesSuppliersNum];
        for(int i = 0; i < dealersNum; ++i){
            accessoriesSuppliers[i] = new Supplier<>(CarAccessories.class,
                    accessoriesStorage,
                    accessoriesSupplierTime);
            accessoriesSuppliers[i].start();
        }

        dealersNum = prefs.getInt(Config.DEALERS_NUM, 1);
        dealers = new Thread[dealersNum];
        for(int i = 0; i < dealersNum; ++i){
            dealers[i] = new Thread(dealerTask);
            dealers[i].start();
        }

        bodySupplier.start();
        engineSupplier.start();

        carStorageController = new CarStorageController();
        carStorageController.update(carStorage, new Object());
    }

    public void stop(){
        
        if(!running){
            return;
        }
        running = false;

        carStorageController.stop();

        bodySupplier.interrupt();
        engineSupplier.interrupt();
        for(Thread dealer : dealers){
            dealer.interrupt();
        }
        for(Thread supplier : accessoriesSuppliers){
            supplier.interrupt();
        }

        bodyStorage.clear();
        engineStorage.clear();
        accessoriesStorage.clear();
        carStorage.clear();

        log.debug("carFactory.stop()");
    }

    class CarStorageController implements Observer {

        private static final int DEFAULT_REQUEST_VOLUME = 3;
        private static final double MIN_STORAGE_POINT = 0.2;
        private AtomicInteger totalMadeCounter = new AtomicInteger(0);

        ExecutorService workerPool;

        private final Runnable workerTask = () -> {
            log.debug("I'm worker");
            try {
                CarBody carBody = bodyStorage.get();
                CarEngine carEngine = engineStorage.get();
                CarAccessories carAccessories = accessoriesStorage.get();
                TimeUnit.MILLISECONDS.sleep(prefs.getInt(Config.WORKER_TIME, DEFAULT_TASK_TIME));
                Car car = new Car(carBody, carEngine, carAccessories);
                carStorage.add(car);
                totalMadeCounter.incrementAndGet();
                log.debug("Car (ID: {}) added to the CarStorage", car.getID());
                setChanged();
                notifyObservers();
            } catch (InterruptedException e) {
                log.debug("Interrupted. Finished.");
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
            if (storage.size() / (double) storage.getMaxSize() < MIN_STORAGE_POINT) {
                int newTasksNum = DEFAULT_REQUEST_VOLUME;
                for (int i = 0; i < newTasksNum; ++i) {
                    workerPool.submit(workerTask);
                }
            }
        }

        int getTotalMade(){
            return totalMadeCounter.get();
        }
    }

    private class Supplier<T extends Numerable> extends Thread{

        AtomicInteger totalMadeCounter = new AtomicInteger(0);
        private Constructor<T> carPartConstructor;
        private Storage<T> storage;
        private int sleepTime;
        private String carPartName;

        int getTotalMade(){
            return totalMadeCounter.get();
        }

        Supplier(Class<T> carPartType, Storage<T> storage, int sleepTime) throws CarFactoryReflectiveException {
            try {
                this.carPartConstructor = carPartType.getConstructor();
            }catch (NoSuchMethodException e){
                log.debug(e);
                throw new CarFactoryReflectiveException(e);
            }
            this.storage = storage;
            this.sleepTime = sleepTime;
            this.carPartName = carPartType.getName();
        }

        @Override
        public void run(){
            log.debug("I'm {} Supplier.", carPartName);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(sleepTime);
                    storage.add(carPartConstructor.newInstance());
                    totalMadeCounter.incrementAndGet();
                    setChanged();
                    notifyObservers();
                } catch (InterruptedException e) {
                    log.debug("Interrupted. Finished.");
                    break;
                } catch (ReflectiveOperationException e) {
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
                Scanner scanner = new Scanner(Thread.currentThread().getName()).useDelimiter("Thread-");
                log.info("Dealer {}, Auto {}(Body: {}, Engine: {}, Accessories {})",
                        scanner.hasNextInt() ? scanner.nextInt() % dealersNum + 1 : "0",
                        car.getID(),
                        car.getBodyID(),
                        car.getEngineID(),
                        car.getAccessoriesID());

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
        return prefs.getInt(Config.BODY_STORAGE_SIZE, DEFAULT_STORAGE_SIZE);
    }
    public int getEngineStorageMaxSize(){
        return prefs.getInt(Config.ENGINE_STORAGE_SIZE, DEFAULT_STORAGE_SIZE);
    }
    public int getAccessoriesStorageMaxSize(){
        return prefs.getInt(Config.ACCESSORIES_STORAGE_SIZE, DEFAULT_STORAGE_SIZE);
    }
    public int getCarStorageMaxSize(){
        return prefs.getInt(Config.CAR_STORAGE_SIZE, DEFAULT_STORAGE_SIZE);
    }

    public int getBodyTotalMade(){
        return bodySupplier.getTotalMade();
    }
    public int getEngineTotalMade(){
        return engineSupplier.getTotalMade();
    }
    public int getAccessoriesTotalMade(){
        int res = 0;
        for(Supplier<CarAccessories> supplier : accessoriesSuppliers){
            res += supplier.getTotalMade();
        }
        return res;
    }
    public int getCarTotalMade(){
        return carStorageController.getTotalMade();
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