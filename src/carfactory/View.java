package carfactory;

import java.util.Observable;
import java.util.Observer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class View implements Observer {

    @FXML private Label bodyStorageOccupacity;
    @FXML private Label engineStorageOccupacity;
    @FXML private Label accessoriesStorageOccupacity;
    @FXML private Label carStorageOccupacity;

    protected CarFactory carFactory;

    void setCarFactory(CarFactory carFactory){
        this.carFactory = carFactory;
        carFactory.addObserver(this);
        System.out.println("carFactory attahed");
    }

    public View(){
        System.out.println("hi im view");
    }

    @Override
    public void update(Observable o, Object arg) {
        Platform.runLater(() -> {
            bodyStorageOccupacity.setText(String.valueOf(carFactory.getBodyStorageSize()) + "/" +
                    carFactory.getBodyStorageMaxSize());
            engineStorageOccupacity.setText(String.valueOf(carFactory.getEngineStorageSize()) + "/" +
                    carFactory.getEngineStorageMaxSize());
            accessoriesStorageOccupacity.setText(String.valueOf(carFactory.getAccessoriesStorageSize()) + "/" +
                    carFactory.getAccessoriesStorageMaxSize());
            carStorageOccupacity.setText(String.valueOf(carFactory.getCarStorageSize()) + "/" +
                    carFactory.getCarStorageMaxSize());
        });
    }
}
