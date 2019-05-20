package carfactory;

import java.util.concurrent.atomic.AtomicInteger;

class Numerable {

    private static AtomicInteger serialNumberGenerator = new AtomicInteger(0);

    private final int ID = serialNumberGenerator.incrementAndGet();

    public int getID() {
        return ID;
    }
}
