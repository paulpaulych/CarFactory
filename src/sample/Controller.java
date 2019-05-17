package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import sample.preferences.Config;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Controller extends View {

    @FXML private Button buttonStart;
    @FXML private Button buttonStop;
    @FXML private Slider workerSlider;
    @FXML private Slider bodySupplierSlider;
    @FXML private Slider engineSupplierSlider;
    @FXML private Slider accessoriesSupplierSlider;

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
        prefs.putInt(Config.WORKER_TIME, (int)workerSlider.getValue() * 1000);
        prefs.putInt(Config.BODY_SUPPLIER_TIME, (int)bodySupplierSlider.getValue() * 1000);
        prefs.putInt(Config.ENGINE_SUPPLIER_TIME, (int)engineSupplierSlider.getValue() * 1000);
        prefs.putInt(Config.ACCESSORIES_SUPPLIER_TIME, (int)accessoriesSupplierSlider.getValue() * 1000);
        prefs.putInt(Config.WORKERS_NUM, 2);
        prefs.putInt(Config.CAR_STORAGE_SIZE, 10);
        prefs.putInt(Config.BODY_STORAGE_SIZE, 10);
        prefs.putInt(Config.ENGINE_STORAGE_SIZE, 10);
        prefs.putInt(Config.ACCESSORIES_STORAGE_SIZE, 10);
        try(OutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(Config.CONF_FILE_NAME))){
            prefs.exportNode(outputStream);
            System.out.println("prefs exported");
        }catch(IOException | BackingStoreException exc) {
            System.out.println("exportPreferences() failed\n" + exc);
        }
    }

}