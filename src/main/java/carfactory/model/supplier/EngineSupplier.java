package carfactory.model.supplier;

import carfactory.Storage;
import carfactory.model.details.CarEngine;
import org.slf4j.LoggerFactory;
import java.util.function.Supplier;

public class EngineSupplier extends DetailsSupplier<CarEngine> {

    public EngineSupplier(Storage<CarEngine> storage, Supplier<Long> fixedDelay) {
        super(storage, fixedDelay, LoggerFactory.getLogger(EngineSupplier.class));
    }

    @Override
    protected CarEngine newInstance() {
        var engine = new CarEngine();
        logger.info("produced: {}", engine);
        return engine;
    }

}
