package carfactory;


public class Car extends Numerable {

	private int serialNumber;
	private CarEngine engine;
	private CarBody body;
	private CarAccessories accessories;
	
	public Car(CarBody body, CarEngine engine, CarAccessories accessories) {
		this.body = body;
		this.engine = engine;
		this.accessories = accessories;
	}
}
