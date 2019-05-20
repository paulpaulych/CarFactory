package carfactory;


public class Car extends Numerable {

	private CarEngine engine;
	private CarBody body;
	private CarAccessories accessories;
	
	public Car(CarBody body, CarEngine engine, CarAccessories accessories) {
		this.body = body;
		this.engine = engine;
		this.accessories = accessories;
	}

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
