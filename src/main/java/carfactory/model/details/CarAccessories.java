package carfactory.model.details;

import java.util.concurrent.atomic.AtomicInteger;

public class CarAccessories extends Detail {

    private static final AtomicInteger serialNumberGenerator = new AtomicInteger(0);
    private final int id = serialNumberGenerator.incrementAndGet();

    @Override
    public int getId() { return id; }

    @Override
    public String getName() {
        return "Accessory";
    }
}
