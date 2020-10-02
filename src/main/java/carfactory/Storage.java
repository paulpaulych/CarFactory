package carfactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Storage<T> implements Observable {

	private static final Logger log = LoggerFactory.getLogger(Storage.class);

	private final int maxSize;
	private final Queue<T> storage = new ArrayDeque<>();
	private final List<Observer> observers = new ArrayList<>();

	public Storage(int maxSize){
		this.maxSize = maxSize;
	}

	public int size(){
		return storage.size();
	}

	public synchronized void add(T t) throws InterruptedException {
		while(true) {
			if (storage.size() < maxSize) {
				storage.add(t);
				notifyAll();
				notifyObservers();
				return;
			}
			wait();
		}
	}

	public synchronized T get() throws InterruptedException {
		while (true) {
			if (storage.size() > 0) {
				T t = storage.remove();
				notifyAll();
				notifyObservers();
				return t;
			}
			log.debug("getLock.wait()" + Thread.currentThread().getName());
			wait();
		}
	}

	public void clear(){
		storage.clear();
	}

	private void notifyObservers() {
		observers.forEach(Observer::onUpdate);
	}

	@Override
	public void subscribe(Observer observer) {
		observers.add(observer);
	}
}