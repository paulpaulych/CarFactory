package carfactory.model;

/**
 * Represents execution params which can be changed by user in runtime
 */
public class Preferences {
    private volatile Long workerDelay;
    private volatile Long bodySupplierDelay;
    private volatile Long engineSupplierDelay;
    private volatile Long accessoriesSupplierDelay;
    private volatile Long dealerFixedDelayMs;

    public Preferences(Long workerDelay, Long bodySupplierDelay, Long engineSupplierDelay, Long accessoriesSupplierDelay, Long dealerFixedDelayMs){
        this.workerDelay = workerDelay;
        this.bodySupplierDelay = bodySupplierDelay;
        this.engineSupplierDelay = engineSupplierDelay;
        this.accessoriesSupplierDelay = accessoriesSupplierDelay;
        this.dealerFixedDelayMs = dealerFixedDelayMs;
    }

    public void setWorkerDelay(Long workerDelay) {
        this.workerDelay = workerDelay;
    }

    public void setBodySupplierDelay(Long bodySupplierDelay) {
        this.bodySupplierDelay = bodySupplierDelay;
    }

    public void setEngineSupplierDelay(Long engineSupplierDelay) {
        this.engineSupplierDelay = engineSupplierDelay;
    }

    public void setAccessoriesSupplierDelay(Long accessoriesSupplierDelay) {
        this.accessoriesSupplierDelay = accessoriesSupplierDelay;
    }

    public void setDealerFixedDelayMs(Long dealerFixedDelayMs) {
        this.dealerFixedDelayMs = dealerFixedDelayMs;
    }

    public Long getDealerFixedDelayMs() {
        return dealerFixedDelayMs;
    }

    public Long getWorkerDelay() {
        return workerDelay;
    }

    public Long getBodySupplierDelay() {
        return bodySupplierDelay;
    }

    public Long getEngineSupplierDelay() {
        return engineSupplierDelay;
    }

    public Long getAccessoriesSupplierDelay() {
        return accessoriesSupplierDelay;
    }

}