package carfactory;

/**
 * Runnable, который по завершении дергает колбэк
 */
public class CompletableRunnable implements Runnable {

    private final Runnable task;
    private Runnable onComplete;

    public CompletableRunnable(Runnable task, Runnable onComplete) {
        this.task = task;
        this.onComplete = onComplete;
    }
    public CompletableRunnable(Runnable task) {
        this.task = task;
    }

    public void onComplete(Runnable onComplete){
        this.onComplete = onComplete;
    }

    @Override
    public void run() {
        task.run();
        if (onComplete != null) {
            onComplete.run();
        }
    }
}
