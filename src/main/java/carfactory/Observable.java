package carfactory;

@FunctionalInterface
public interface Observable {

    void subscribe(Observer observer);

}

