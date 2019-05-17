package sample;

import java.util.*;
import java.util.concurrent.SynchronousQueue;

public class Storage<T>{

	private int maxSize;
	private Queue<T> storage = new LinkedList<>();

	public Storage(int maxSize){
		this.maxSize = maxSize;
	}

	public double howFull() {
		return storage.size()/(double)maxSize * 100;
	}

	public int size(){
		return storage.size();
	}

	public synchronized void add(T t) throws InterruptedException{
			while(true) {
				if (storage.size() < maxSize) {
					storage.add(t);

					notifyAll();
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
					notify();
					return t;
				} else {
					System.out.println("getLock.wait()" + Thread.currentThread().getName());
					wait();
				}
			}
	}

	public void clear(){
		storage.clear();
	}
}