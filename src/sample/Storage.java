package sample;

import java.util.LinkedList;
import java.util.List;

public class Storage<T>{

	int maxSize;

	private final Object addLock = new String();
	private final Object getLock = new String();

	List<T> storage = new LinkedList<T>();
	
	public Storage(int maxSize){
		this.maxSize = maxSize;
	}

	public double howFull() {
		return storage.size()/(double)maxSize * 100;
	}

	public void add(T t) throws InterruptedException{
		synchronized (addLock){
			while(true) {
				if (storage.size() < maxSize) {
					storage.add(t);
					System.out.println("added");
					//без этого не работает
					synchronized (getLock){
						getLock.notifyAll();
					}
					return;
				} else {
					addLock.wait();
				}
			}
		}
	}

	public synchronized T get() throws InterruptedException{
		synchronized (getLock) {
			while (true) {
				if (storage.size() > 0) {
					T t = storage.remove(storage.size() - 1);
					//без этого не работает
					synchronized (addLock) {
						addLock.notify();
					}
					return t;
				} else {
					System.out.println("getLock.wait()");
					getLock.wait();
				}
			}
		}
	}
}