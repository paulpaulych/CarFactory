package carfactory.model;

import carfactory.*;
import carfactory.Observable;
import carfactory.Observer;
import carfactory.model.details.CarAccessories;
import carfactory.model.details.CarBody;
import carfactory.model.details.CarEngine;
import carfactory.model.supplier.AccessoriesSupplier;
import carfactory.model.supplier.BodySupplier;
import carfactory.model.supplier.EngineSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CarFactory implements Observable, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(CarFactory.class);
    private final Config config;
    private final Preferences preferences;
    private final Storage<Car> carStorage;
    private final Storage<CarBody> bodyStorage;
    private final Storage<CarEngine> engineStorage;
    private final Storage<CarAccessories> accessoriesStorage;
    private final BodySupplier bodySupplier;
    private final EngineSupplier engineSupplier;
    private final List<AccessoriesSupplier> accessoriesSuppliers;
    private final List<Dealer> dealers;
    private final FactoryWork factoryWork;
    private final List<Observer> observers = new ArrayList<>();
    private final AtomicInteger totalSold = new AtomicInteger(0);
    private final AtomicInteger salesInProgressCount = new AtomicInteger(0);

    public CarFactory(Config config, Preferences preferences) {
        this.config = config;
        this.preferences = preferences;

        bodyStorage = new Storage<>(config.bodyStorageMaxSize());
        engineStorage = new Storage<>(config.engineStorageMaxSize());
        accessoriesStorage = new Storage<>(config.accessoriesStorageMaxSize());
        carStorage = new Storage<>(config.carStorageMaxSize());

        bodySupplier = new BodySupplier(bodyStorage, preferences::getBodySupplierDelay);
        bodySupplier.subscribe(this::notifyObservers);
        engineSupplier = new EngineSupplier(engineStorage, preferences::getEngineSupplierDelay);
        bodySupplier.subscribe(this::notifyObservers);
        accessoriesSuppliers = Stream.generate(() ->
                new AccessoriesSupplier(accessoriesStorage, preferences::getAccessoriesSupplierDelay))
                .limit(config.accessoriesSuppliersCount())
                .peek(s -> s.subscribe(this::notifyObservers))
                .collect(Collectors.toList());

        dealers = Stream.iterate(1, i -> i + 1)
                .map( i -> new Dealer("DEALER_" + i))
                .limit(config.dealersCount())
                .collect(Collectors.toList());

        factoryWork = new FactoryWork(config.optimalCarStorageSize());
        bodySupplier.start();
        engineSupplier.start();
        accessoriesSuppliers.forEach(AccessoriesSupplier::start);
        dealers.forEach(Thread::start);
    }

    @Override
    public void close(){
        factoryWork.close();
        bodySupplier.interrupt();
        engineSupplier.interrupt();
        accessoriesSuppliers.forEach(AccessoriesSupplier::interrupt);
        dealers.forEach(Dealer::interrupt);
        bodyStorage.clear();
        engineStorage.clear();
        accessoriesStorage.clear();
        carStorage.clear();
        log.debug("car factory stopped");
    }


    /**
     * represents car factory functionality
     * keeps car storage size approximately equal to given optimal storage size
     */
    class FactoryWork implements AutoCloseable {

        private final AtomicInteger totalMadeCounter = new AtomicInteger(0);
        private final AtomicInteger carsInProgress = new AtomicInteger(0);
        private final int requiredStorageSize;
        private final ConcurrentLinkedDeque<Thread> workers = new ConcurrentLinkedDeque<>();
        private final AtomicInteger activeWorkers = new AtomicInteger(0);
        private boolean closed = false;
        FactoryWork(int requiredStorageSize) {
            this.requiredStorageSize = requiredStorageSize;
            runNWorkers(1);
            carStorage.subscribe(() -> runNWorkers(workersNeeded()));
        }

        private void buildCar() {
            try {
                log.debug("started");
                var body = bodyStorage.get();
                var engine = engineStorage.get();
                var accessories = accessoriesStorage.get();
                carsInProgress.incrementAndGet();
                //имитируем бурную деятельность
                TimeUnit.MILLISECONDS.sleep(preferences.getWorkerDelay());
                Car car = new Car(body, engine, accessories);
                carStorage.add(car);
                carsInProgress.decrementAndGet();
                totalMadeCounter.incrementAndGet();
                log.info("{} added to the CarStorage", car);
                notifyObservers();
            } catch (InterruptedException e) {
                log.debug("worker interrupted");
            }
        }

        @SuppressWarnings("SuspiciousMethodCalls")
        private void runNWorkers(int n) {
            log.info("running " + n + " workers");
            for (int i = 0; i < n; i++) {
                CompletableRunnable task = new CompletableRunnable(this::buildCar);
                task.onComplete(() ->{
                    workers.remove(task);
                    activeWorkers.decrementAndGet();
                });
                Thread thread = new Thread(task);
                workers.add(thread);
                activeWorkers.incrementAndGet();
                thread.start();
            }
        }

        private int workersNeeded(){
            var curStorageSize = carStorage.size();
            if(closed || curStorageSize > requiredStorageSize){
                return 0;
            }
            return requiredStorageSize - curStorageSize - activeWorkers.get();
        }

        @Override
        public void close() {
            closed = true;
            workers.forEach(Thread::interrupt);
        }
    }

    class Dealer extends Thread {

        private final String name;
        Dealer(String dealerName){
            this.name = dealerName;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("Dealer-" + name);
            log.debug("started");
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    Car car = carStorage.get();
                    salesInProgressCount.incrementAndGet();
                    TimeUnit.MILLISECONDS.sleep(preferences.getDealerFixedDelayMs());
                    salesInProgressCount.decrementAndGet();
                    totalSold.incrementAndGet();
                    log.debug("Car taken from the storage.");
                    log.info("car sold. dealer: {}, car: {}", name, car);
                    notifyObservers();
                } catch(InterruptedException e) {
                    log.debug("Interrupted. Finished.");
                    break;
                }
            }
        }
    }

    @Override
    public void subscribe(Observer observer) {
        observers.add(observer);
    }

    private void notifyObservers(){
        observers.forEach(Observer::onUpdate);
    }

    public State getState(){
        return new State(
                bodyStorage.size(),
                engineStorage.size(),
                accessoriesStorage.size(),
                getCarStorageSize(),
                bodySupplier.getTotalMade(),
                engineSupplier.getTotalMade(),
                accessoriesSuppliers.stream().mapToInt(AccessoriesSupplier::getTotalMade).sum(),
                factoryWork.totalMadeCounter.get(),
                factoryWork.carsInProgress.get(),
                totalSold.get(),
                salesInProgressCount.get()
        );
    }

    private int getCarStorageSize(){
        return carStorage.size();
    }

    public Config getConfig() {
        return config;
    }

    public Preferences getPreferences() {
        return preferences;
    }
}