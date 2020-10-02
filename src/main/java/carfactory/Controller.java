package carfactory;

import carfactory.model.CarFactory;
import carfactory.model.Preferences;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller
        extends View
        implements Initializable {

    @FXML private Slider dealerFixedDelaySlider;
    @FXML private Slider bodySupplierFixedDelaySlider;
    @FXML private Slider engineSupplierFixedDelaySlider;
    @FXML private Slider accessoriesSupplierFixedDelaySlider;
    @FXML private Slider workerFixedDelaySlider;

    @FXML public void onClickButtonStart() {
        if(carFactory != null){
            log.debug("CarFactory has already started");
            return;
        }
        this.carFactory = new CarFactory(
                config,
                new Preferences(
                    speedToDelay(workerFixedDelaySlider.getValue()),
                    speedToDelay(bodySupplierFixedDelaySlider.getValue()),
                    speedToDelay(engineSupplierFixedDelaySlider.getValue()),
                    speedToDelay(accessoriesSupplierFixedDelaySlider.getValue()),
                    speedToDelay(dealerFixedDelaySlider.getValue())
            ));
        this.carFactory.subscribe(this::onNext);
    }

    @FXML public void onClickButtonStop() {
        if(carFactory == null){
            return;
        }
        carFactory.close();
        carFactory = null;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dealerFixedDelaySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                if(carFactory != null) carFactory.getPreferences().setDealerFixedDelayMs(speedToDelay(newValue));
        });
        bodySupplierFixedDelaySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(carFactory != null) carFactory.getPreferences().setBodySupplierDelay(speedToDelay(newValue));
        });
        engineSupplierFixedDelaySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(carFactory != null) carFactory.getPreferences().setEngineSupplierDelay(speedToDelay(newValue));
        });
        accessoriesSupplierFixedDelaySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(carFactory != null) carFactory.getPreferences().setAccessoriesSupplierDelay(speedToDelay(newValue));
        });
        workerFixedDelaySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(carFactory != null) carFactory.getPreferences().setWorkerDelay(speedToDelay(newValue));
        });
    }

    private Long speedToDelay(Number speed){
        return (long)(1000 / speed.doubleValue());
    }
}