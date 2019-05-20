package carfactory;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller
        extends View
        implements Initializable {
    
    @FXML private Button buttonStart;
    @FXML private Button buttonStop;

    @FXML private Slider dealerTimeSlider;
    @FXML private Slider bodySupplierTimeSlider;
    @FXML private Slider engineSupplierTimeSlider;
    @FXML private Slider accessoriesSupplierTimeSlider;

    @FXML public void onClickButtonStart() throws NoSuchMethodException{
        if(!carFactory.isRunning()){
            carFactory.run();
        } else {
            log.debug("CarFactory has already started");
        }
    }

    @FXML public void onClickButtonStop() {
        carFactory.stop();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dealerTimeSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                carFactory.setDealerTime(newValue.intValue() * 1000));
        bodySupplierTimeSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                carFactory.setBodySupplierTime(newValue.intValue() * 1000));
        engineSupplierTimeSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                carFactory.setEngineSupplierTime(newValue.intValue() * 1000));
        accessoriesSupplierTimeSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                carFactory.setAccessoriesSupplierTime(newValue.intValue() * 1000));
    }
}