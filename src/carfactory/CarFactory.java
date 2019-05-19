package carfactory;

import carfactory.preferences.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

public class CarFactory extends Observable {

    private static final Logger log = LogManager.getLogger();

    private Preferences prefs;

    private boolean running = false;

    private ExecutorService dealerPool;
    private ExecutorService bodySupplier;
    private ExecutorService engineSupplier;
    private ExecutorService accessoriesSupplierPool;

    private CarStorageController carStorageController;

    private Storage<Car> carStorage;
    private Storage<CarBody> bodyStorage;
    private Storage<CarEngine> engineStorage;
    private Storage<CarAccessories> accessoriesStorage;

    private int dealerTime = 3000;
    private int bodySupplierTime = 3000;
    private int engineSupplierTime = 3000;
    private int accessoriesSupplierTime = 3000;

    public CarFactory(){
        prefs = Preferences.userNodeForPackage(this.getClass());
    }

    public void run() {

        running = true;

        try(InputStream is = new FileInputStream(Config.CONF_FILE_NAME)){
            Preferences.importPreferences(is);
        } catch (IOException | InvalidPreferencesFormatException e){
            e.printStackTrace();
        }

        carStorage = new Storage<>(prefs.getInt(Config.CAR_STORAGE_SIZE, 10));
        bodyStorage = new Storage<>(prefs.getInt(Config.BODY_STORAGE_SIZE, 10));
        engineStorage = new Storage<>(prefs.getInt(Config.ENGINE_STORAGE_SIZE, 10));
        accessoriesStorage = new Storage<>(prefs.getInt(Config.ACCESSORIES_STORAGE_SIZE, 10));

        bodySupplier = Executors.newSingleThreadExecutor();
        engineSupplier = Executors.newSingleThreadExecutor();

        int accessoriesSuppliersNum = prefs.getInt(Config.ACCESSORIES_SUPPLIERS_NUM, 1);
        accessoriesSupplierPool = Executors.newFixedThreadPool(accessoriesSuppliersNum);
        for(int i = 0; i < accessoriesSuppliersNum; ++i){
            accessoriesSupplierPool.submit(accessoriesSupplierTask);
        }

        int dealersNum = prefs.getInt(Config.DEALERS_NUM, 1);
        dealerPool = Executors.newFixedThreadPool(dealersNum);
        for(int i = 0; i < dealersNum; ++i){
            dealerPool.submit(dealerTask);
        }

        carStorageController = new CarStorageController();
        //костыль. Надо как-то оповестить carStorageController о начале работы
        carStorageController.update(carStorage, new Object());

        bodySupplier.submit(bodySupplierTask);
        engineSupplier.submit(engineSupplierTask);
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
    }

    class CarStorageController implements Observer {

        private final Runnable workerTask = () -> {
            System.err.println(Thread.currentThread().getName() + "I'm worker!!!");
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    CarBody carBody = bodyStorage.get();
                    CarEngine carEngine = engineStorage.get();
                    CarAccessories carAccessories = accessoriesStorage.get();
                    TimeUnit.MILLISECONDS.sleep(prefs.getInt(Config.WORKER_TIME, 5000));
                    carStorage.add(new Car(carBody, carEngine, carAccessories));
                    System.out.println("car created");
                    setChanged();
                    notifyObservers();
                } catch (InterruptedException e) {
                    System.err.println(Thread.currentThread().getName() + "I'm done (c)worker");
                    break;
                }
            }
        };

        ExecutorService workerPool;

        CarStorageController() {
            carStorage.addObserver(this);

            int num = prefs.getInt(Config.WORKERS_NUM, 1);
            workerPool = Executors.newFixedThreadPool(num);
        }

        void stop() {
            workerPool.shutdownNow();
        }

        @Override
        public void update(Observable o, Object arg) {
            System.out.println("listened");

            Storage<Car> storage = (Storage<Car>)o;
            if (storage.size() / (double) storage.getMaxSize() < 0.2) {
                int newTasksNum = 3;//magic??
                for (int i = 0; i < newTasksNum; ++i) {
                    workerPool.submit(workerTask);
                }
            }
        }
    }

    private final Runnable bodySupplierTask = () -> {
        System.err.println(Thread.currentThread().getName() + "I'm working bodySupplier!!!");
        while(!Thread.currentThread().isInterrupted()) {
            try {
                TimeUnit.MILLISECONDS.sleep(bodySupplierTime);
                bodyStorage.add(new CarBody());
                System.out.println("body added");
                setChanged();
                notifyObservers();
            } catch (InterruptedException e) {
                System.err.println(Thread.currentThread().getName() + "I'm done (c)bodySupplier");
                break;
            }
        }
    };
    private final Runnable engineSupplierTask = () -> {
        System.err.println(Thread.currentThread().getName() + "I'm working engineSupplier!!!");
        while(!Thread.currentThread().isInterrupted()) {
            try {
                TimeUnit.MILLISECONDS.sleep(engineSupplierTime);
                engineStorage.add(new CarEngine());
                System.out.println("engine added");
                setChanged();
                notifyObservers();
            } catch (InterruptedException e) {
                System.err.println(Thread.currentThread().getName() + "I'm done (c)engineSupplier");
                break;
            }
        }
    };
    private final Runnable accessoriesSupplierTask = () -> {
        System.err.println(Thread.currentThread().getName() + "I'm working accessoriesSupplier!!!");
        while(!Thread.currentThread().isInterrupted()) {
            try {
                TimeUnit.MILLISECONDS.sleep(accessoriesSupplierTime);
                accessoriesStorage.add(new CarAccessories());
                System.out.println("accessories added");
                setChanged();
                notifyObservers();
            } catch (InterruptedException e) {
                System.err.println(Thread.currentThread().getName() + "I'm done (c)accessoriesSupplier");
                break;
            }
        }
    };
    private final Runnable dealerTask = () -> {
        System.err.println(Thread.currentThread().getName() + "I'm working dealer!!!");
        while(!Thread.currentThread().isInterrupted()) {
            try{
                TimeUnit.MILLISECONDS.sleep(dealerTime);
                Car car = carStorage.get();
                System.err.println("carTaken, time is " + dealerTime);
                setChanged();
                notifyObservers();
            }catch(InterruptedException e) {
                System.err.println(Thread.currentThread().getName() + "was interrupted");
                break;
            }
        }
        System.err.println(Thread.currentThread().getName() + "I'm done (c)dealer");
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