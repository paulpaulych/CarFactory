package sample;

public class Car {

	private CarEngine engine;
	private CarBody body;
	private CarAccessories accessories;
	
	public Car(CarEngine engine, CarBody body, CarAccessories accessories) {
		this.engine = engine;
		this.body = body;
		this.accessories = accessories;
	}
}
