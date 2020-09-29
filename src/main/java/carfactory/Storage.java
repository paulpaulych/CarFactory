package carfactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class Storage<T> extends Observable{

	private static final Logger log = LogManager.getLogger();

	private final int maxSize;
	private final Queue<T> storage = new LinkedList<>();

	public Storage(int maxSize){
		this.maxSize = maxSize;
	}

	public int size(){
		return storage.size();
	}
	public int getMaxSize() {
		return maxSize;
	}

	public synchronized void add(T t) throws InterruptedException{
		while(true) {
			if (storage.size() < maxSize) {
				storage.add(t);

				notifyAll();

				hasChanged();
				notifyObservers();

				return;
			} else {
				wait();
			}
		}
	}

	public synchronized T get() throws InterruptedException{
		while (true) {
			if (storage.size() > 0) {
				T t = storage.remove();

				notifyAll();

				hasChanged();
				notifyObservers();

				return t;
			} else {
				log.debug("getLock.wait()" + Thread.currentThread().getName());
				wait();
			}
		}
	}

	public void clear(){
		storage.clear();
	}
}