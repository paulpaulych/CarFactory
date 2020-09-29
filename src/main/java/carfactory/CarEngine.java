package carfactory;

import java.util.concurrent.atomic.AtomicInteger;

public class CarEngine implements Numerable {

    private static final AtomicInteger serialNumberGenerator = new AtomicInteger(0);

    private final int ID = serialNumberGenerator.incrementAndGet();

    @Override
    public int getID() { return ID; }

}
