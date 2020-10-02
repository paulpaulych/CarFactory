package carfactory.model.supplier;

import carfactory.Storage;
import carfactory.model.details.CarAccessories;
import org.slf4j.LoggerFactory;
import java.util.function.Supplier;

public class AccessoriesSupplier extends DetailsSupplier<CarAccessories> {

    public AccessoriesSupplier(Storage<CarAccessories> storage, Supplier<Long> fixedDelay) {
        super(storage, fixedDelay, LoggerFactory.getLogger(AccessoriesSupplier.class));
    }

    @Override
    protected CarAccessories newInstance() {
        var accessory = new CarAccessories();
        logger.info("produced: {}", accessory);
        return accessory;
    }
}
