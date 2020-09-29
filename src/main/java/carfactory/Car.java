package carfactory;


import java.util.concurrent.atomic.AtomicInteger;

public class Car implements Numerable{

	private static final AtomicInteger serialNumberGenerator = new AtomicInteger(0);

	private final int ID = serialNumberGenerator.incrementAndGet();

	private final CarEngine engine;
	private final CarBody body;
	private final CarAccessories accessories;
	
	public Car(CarBody body, CarEngine engine, CarAccessories accessories) {
		this.body = body;
		this.engine = engine;
		this.accessories = accessories;
	}

	@Override
	public int getID() { return ID; }

	public int getBodyID(){
		return body.getID();
	}

	public int getEngineID(){
		return engine.getID();
	}

	public int getAccessoriesID(){
		return accessories.getID();
	}
}
