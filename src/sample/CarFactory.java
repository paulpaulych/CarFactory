package sample;

import sample.preferences.Config;

import java.io.*;
import java.util.Observable;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

public class CarFactory extends Observable {

    Preferences prefs;

    private boolean running;

    private Thread bodySupplier;
    private Thread engineSupplier;
    private Thread accessoriesSupplier;

    private Thread bodyCustomer;

    private Storage<Car> carStorage;
    private Storage<CarBody> bodyStorage;
    private Storage<CarEngine> engineStorage;
    private Storage<CarAccessories> accessoriesStorage;

    public CarFactory(){

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

        bodySupplier = new Thread(bodySupplierTask);
        engineSupplier = new Thread(engineSupplierTask);
        accessoriesSupplier = new Thread(accessoriesSupplierTask);

//        bodyCustomer = new Thread(bodyCustomerTask);
    }

    public void run() {
        bodySupplier.start();
        engineSupplier.start();
        accessoriesSupplier.start();
    }

//    private final class CarPartSupplier implements Runnable{
//
//        private Storage<CarPart> storage;
//        private String carPartName;
//
//        CarPartSupplier(Storage<CarPart> storage, String carPartName){
//            this.storage = storage;
//            this.carPartName = carPartName;
//        }
//
//        @Override
//        public void run() {
//            System.err.println(Thread.currentThread().getName() + "I'm working " + carPartName + "Supplier!!!");
//            while(!Thread.currentThread().isInterrupted()) {
//                try {
//                    TimeUnit.MILLISECONDS.sleep(prefs.getInt(BODY_SUPPLIER_TIME, 2000));
//                    bodyStorage.add(Class.forName(carPartName).getDeclaredConstructor().newInstance());
//                    setChanged();
//                } catch (InterruptedException e) {
//                    System.err.println(Thread.currentThread().getName() + "I'm done (c)" + carPartName.getName() + "Supplier");
//                    break;
//                } catch (ReflectiveOperationException e){
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    private final Runnable bodySupplierTask = () -> {
        System.err.println(Thread.currentThread().getName() + "I'm working bodySupplier!!!");
        while(!Thread.currentThread().isInterrupted()) {
            try {
                TimeUnit.MILLISECONDS.sleep(prefs.getInt(Config.BODY_SUPPLIER_TIME, 2000));
                bodyStorage.add(new CarBody());
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
                TimeUnit.MILLISECONDS.sleep(prefs.getInt(Config.BODY_SUPPLIER_TIME, 3000));
                engineStorage.add(new CarEngine());
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
                TimeUnit.MILLISECONDS.sleep(prefs.getInt(Config.BODY_SUPPLIER_TIME, 5000));
                accessoriesStorage.add(new CarAccessories());
                setChanged();
                notifyObservers();
            } catch (InterruptedException e) {
                System.err.println(Thread.currentThread().getName() + "I'm done (c)accessoriesSupplier");
                break;
            }
        }
    };

    private final Runnable bodyCustomerTask = () -> {
        System.err.println(Thread.currentThread().getName() + "I'm working bodyCumstomer!!!");
        while(!Thread.currentThread().isInterrupted()) {
            try{
                TimeUnit.MILLISECONDS.sleep(prefs.getInt(Config.ENGINE_SUPPLIER_TIME, 3000));
                bodyStorage.get();
                System.out.println("taken");
                setChanged();
                notifyObservers();
            }catch(InterruptedException e) {
                System.err.println(Thread.currentThread().getName() + "was interrupted");
                break;
            }
        }
        System.err.println(Thread.currentThread().getName() + "I'm done (c)bodyCustomer");
    };

    private final Runnable worker = () -> {
        try{
            System.err.println(Thread.currentThread().getName() + "I'm working!!!");
            Thread.sleep(prefs.getInt(Config.WORKER_TIME, 10000));
            carStorage.add(new Car(
                    engineStorage.get(),
                    bodyStorage.get(),
                    accessoriesStorage.get()));
        }catch(InterruptedException e) {
            System.err.println(Thread.currentThread().getName() + "was interrupted");
        }
    };

    public double getBodyStorageSize(){
        return bodyStorage.howFull();
    }
    public double getEngineStorageSize(){
        return engineStorage.howFull();
    }
    public double getAccessoriesStorageSize(){
        return accessoriesStorage.howFull();
    }


    public void stop(){
        bodySupplier.interrupt();
        engineSupplier.interrupt();
        accessoriesSupplier.interrupt();
//      bodyCustomer.interrupt();
    }

    boolean isRunning(){
        return running;
    }

    @Override
    public synchronized boolean hasChanged() {
        return super.hasChanged();
    }
}