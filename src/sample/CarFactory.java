package sample;

import sample.preferences.Config;

import java.io.*;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

public class CarFactory extends Observable {

    private Preferences prefs;

    private boolean running = false;

    private ExecutorService bodySupplier;
    private ExecutorService engineSupplier;
    private ExecutorService accessoriesSupplier;

    private ExecutorService workerExecutor;

    private Storage<Car> carStorage;
    private Storage<CarBody> bodyStorage;
    private Storage<CarEngine> engineStorage;
    private Storage<CarAccessories> accessoriesStorage;

    public CarFactory(){
        prefs = Preferences.userNodeForPackage(this.getClass());
        try(InputStream inputStream = new BufferedInputStream(
                new FileInputStream(Config.CONF_FILE_NAME))){
            Preferences.importPreferences(inputStream);
        }catch(IOException | InvalidPreferencesFormatException exc) {
            System.out.println("importPreferences() failed\n" + exc);
        }
        carStorage = new Storage<>(prefs.getInt(Config.CAR_STORAGE_SIZE, 10));
        bodyStorage = new Storage<>(prefs.getInt(Config.BODY_STORAGE_SIZE, 10));
        engineStorage = new Storage<>(prefs.getInt(Config.ENGINE_STORAGE_SIZE, 10));
        accessoriesStorage = new Storage<>(prefs.getInt(Config.ACCESSORIES_STORAGE_SIZE, 10));

        bodySupplier = Executors.newSingleThreadExecutor();
        engineSupplier = Executors.newSingleThreadExecutor();
        accessoriesSupplier = Executors.newSingleThreadExecutor();

        workerExecutor = Executors.newFixedThreadPool(prefs.getInt(Config.WORKERS_NUM, 1));
    }

    public void run() {
        running = true;
        bodySupplier.submit(bodySupplierTask);
        engineSupplier.submit(engineSupplierTask);
        accessoriesSupplier.submit(accessoriesSupplierTask);
        workerExecutor.submit(workerTask);
    }

    private final Runnable workerTask = () -> {
        System.err.println(Thread.currentThread().getName() + "I'm worker!!!");
        while(!Thread.currentThread().isInterrupted()) {
            try {
                CarBody carBody = bodyStorage.get();
                CarEngine carEngine = engineStorage.get();
                CarAccessories carAccessories = accessoriesStorage.get();
                TimeUnit.MILLISECONDS.sleep(prefs.getInt(Config.WORKER_TIME, 2000));
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

    private final Runnable bodySupplierTask = () -> {
        System.err.println(Thread.currentThread().getName() + "I'm working bodySupplier!!!");
        while(!Thread.currentThread().isInterrupted()) {
            try {
                TimeUnit.MILLISECONDS.sleep(prefs.getInt(Config.BODY_SUPPLIER_TIME, 2000));
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
                TimeUnit.MILLISECONDS.sleep(prefs.getInt(Config.ENGINE_SUPPLIER_TIME, 3000));
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
                TimeUnit.MILLISECONDS.sleep(prefs.getInt(Config.ACCESSORIES_SUPPLIER_TIME, 5000));
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
                TimeUnit.MILLISECONDS.sleep(prefs.getInt(Config.DEALER_TIME, 3000));
                Car car = carStorage.get();
                System.out.println("carTaken");
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



    public void stop(){

        running = false;

        bodySupplier.shutdownNow();
        engineSupplier.shutdownNow();
        accessoriesSupplier.shutdownNow();
        workerExecutor.shutdownNow();

        bodyStorage.clear();
        engineStorage.clear();
        accessoriesStorage.clear();
        carStorage.clear();
    }

    public boolean isRunning(){
        return running;
    }

    @Override
    public synchronized boolean hasChanged() {
        return super.hasChanged();
    }
}