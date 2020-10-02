package carfactory.model.details;

import java.util.concurrent.atomic.AtomicInteger;

public class CarEngine extends Detail {

    private static final AtomicInteger serialNumberGenerator = new AtomicInteger(0);
    private final int ID = serialNumberGenerator.incrementAndGet();

    @Override
    public int getId() { return ID; }

    @Override
    public String getName() {
        return "Engine";
    }
}
