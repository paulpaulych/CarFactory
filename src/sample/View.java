package sample;

import java.util.Observable;
import java.util.Observer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class View implements Observer {

    @FXML private Label bodyStorageOccupacity;
    @FXML private Label engineStorageOccupacity;
    @FXML private Label accessoriesStorageOccupacity;

//    boolean key = false;
//
//    private CarFactory carFactory = new CarFactory();
//
//    public View(){
//        carFactory.addObserver(this);
//    }
//
    @Override
    public void update(Observable o, Object arg) {
        Platform.runLater(() -> {
//            bodyStorageOccupacity.setText(String.valueOf(carFactory.getBodyStorageSize()));
//            engineStorageOccupacity.setText(String.valueOf(carFactory.getEngineStorageSize()));
//            accessoriesStorageOccupacity.setText(String.valueOf(carFactory.getAccessoriesStorageSize()));
        });
    }
}
