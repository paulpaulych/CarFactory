package carfactory.model.supplier;

import carfactory.Observable;
import carfactory.Storage;
import carfactory.Observer;
import carfactory.model.details.Detail;
import org.slf4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

abstract class DetailsSupplier<T extends Detail> extends Thread implements Observable {

    protected final Logger logger;
    private final AtomicInteger totalMadeCounter = new AtomicInteger(0);
    private final Storage<T> storage;
    private final Supplier<Long> delaySupplier;
    private final List<Observer> observers = new ArrayList<>();

    DetailsSupplier(Storage<T> storage, Supplier<Long> delaySupplier, Logger logger){
        this.storage = storage;
        this.delaySupplier = delaySupplier;
        this.logger = logger;
    }

    @Override
    public final void run(){
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TimeUnit.MILLISECONDS.sleep(delaySupplier.get());
                storage.add(newInstance());
                totalMadeCounter.incrementAndGet();
                notifySubscribers();
            } catch (InterruptedException e) {
                logger.debug("Interrupted. Finished.");
                break;
            }
        }
    }

    protected abstract T newInstance();

    public int getTotalMade(){
        return totalMadeCounter.get();
    }

    private void notifySubscribers(){
        observers.forEach(Observer::onUpdate);
    }

    @Override
    public void subscribe(Observer observer){
        observers.add(observer);
    }

}
