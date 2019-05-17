package carfactory;

import carfactory.preferences.Config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CarStorageController extends Thread{

    private int maxStorageSize;
    private Runnable task;
    private ExecutorService workerPool;

    public CarStorageController(Runnable task, int maxStorageSize, int workersNum){
        this.maxStorageSize = maxStorageSize;
        this.task = task;
        workerPool = Executors.newFixedThreadPool(workersNum);
    }

    @Override
    public void run(){
        while(!Thread.currentThread().isInterrupted()){
            if(curStorageSize / (double)maxStorageSize < 0.2){
                for(int i = 0; i < curRequestsNumber; ++i){
                    workerPool.submit(task);
                }
            }
        }

    }

}
