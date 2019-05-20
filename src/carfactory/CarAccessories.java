package carfactory;

import java.util.concurrent.atomic.AtomicInteger;

public class CarAccessories implements Numerable{

    private static AtomicInteger serialNumberGenerator = new AtomicInteger(0);

    private final int ID = serialNumberGenerator.incrementAndGet();

    @Override
    public int getID() { return ID; }

}
