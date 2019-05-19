package carfactory;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import carfactory.preferences.Config;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Controller
        extends View
        implements Initializable {

    @FXML private Button buttonStart;
    @FXML private Button buttonStop;

    @FXML private Slider dealerTimeSlider;
    @FXML private Slider bodySupplierTimeSlider;
    @FXML private Slider engineSupplierTimeSlider;
    @FXML private Slider accessoriesSupplierTimeSlider;
    @FXML private Slider workersNumberSlider;
    @FXML private Slider accessoriesSuppliersNumberSlider;
    @FXML private Slider dealersNumberSlider;

    @FXML public void onClickButtonStart(){
        if(!carFactory.isRunning()){
            carFactory.run();
            System.out.println("carFactory.run()");
        } else {
            System.out.println("CarFactory has already started");
        }
    }

    @FXML public void onClickButtonStop() {
        carFactory.stop();
        System.out.println("carFactory.stop()");
    }

    @FXML public void applyConfig(){
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        prefs.putInt(Config.CAR_STORAGE_SIZE, 10);
        prefs.putInt(Config.BODY_STORAGE_SIZE, 10);
        prefs.putInt(Config.ENGINE_STORAGE_SIZE, 10);
        prefs.putInt(Config.WORKER_TIME, 3000);

        try(OutputStream outputStream =
                new BufferedOutputStream(new FileOutputStream(Config.CONF_FILE_NAME))){
            prefs.exportNode(outputStream);
            System.out.println("Preferences loaded");
        }catch(IOException | BackingStoreException exc) {
            System.out.println("exportPreferences() failed\n" + exc);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dealerTimeSlider.valueProperty().addListener((observable, oldValue, newValue) ->{
                System.err.println("dealer time changed");
                carFactory.setDealerTime(newValue.intValue() * 1000);
            });
        bodySupplierTimeSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                carFactory.setBodySupplierTime(newValue.intValue() * 1000));
        engineSupplierTimeSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                carFactory.setEngineSupplierTime(newValue.intValue() * 1000));
        accessoriesSupplierTimeSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                carFactory.setAccessoriesSupplierTime(newValue.intValue() * 1000));

    }
}