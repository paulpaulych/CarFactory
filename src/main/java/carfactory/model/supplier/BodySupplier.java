package carfactory.model.supplier;

import carfactory.Storage;
import carfactory.model.details.CarBody;
import org.slf4j.LoggerFactory;
import java.util.function.Supplier;

public class BodySupplier extends DetailsSupplier<CarBody> {

    public BodySupplier(Storage<CarBody> storage, Supplier<Long> fixedDelay) {
        super(storage, fixedDelay, LoggerFactory.getLogger(BodySupplier.class));
    }

    @Override
    protected CarBody newInstance() {
        var car = new CarBody();
        logger.info("produced: {}", car);
        return car;
    }

}
