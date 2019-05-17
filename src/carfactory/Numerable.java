package carfactory;

import java.util.concurrent.atomic.AtomicInteger;

class Numerable {

    private static AtomicInteger serialNumberGenerator = new AtomicInteger(0);

    private int serialNumber = serialNumberGenerator.incrementAndGet();

}
