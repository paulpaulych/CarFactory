package carfactory;

import java.util.Observable;
import java.util.Observer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class View implements Observer {

    private static final String TOTAL_MADE_TEMPL = "Total made: ";

    protected static final Logger log = LogManager.getLogger();

    @FXML private Label bodyStorageOccupacity;
    @FXML private Label engineStorageOccupacity;
    @FXML private Label accessoriesStorageOccupacity;
    @FXML private Label carStorageOccupacity;

    @FXML private Label bodyTotalMade;
    @FXML private Label engineTotalMade;
    @FXML private Label accessoriesTotalMade;
    @FXML private Label carTotalMade;


    protected CarFactory carFactory;

    void setCarFactory(CarFactory carFactory){
        this.carFactory = carFactory;
        carFactory.addObserver(this);
        log.debug("carFactory attached");
    }

    @Override
    public void update(Observable o, Object arg) {
        Platform.runLater(() -> {
            bodyStorageOccupacity.setText(carFactory.getBodyStorageSize() + "/" +
                    carFactory.getBodyStorageMaxSize());
            engineStorageOccupacity.setText(carFactory.getEngineStorageSize() + "/" +
                    carFactory.getEngineStorageMaxSize());
            accessoriesStorageOccupacity.setText(carFactory.getAccessoriesStorageSize() + "/" +
                    carFactory.getAccessoriesStorageMaxSize());
            carStorageOccupacity.setText(carFactory.getCarStorageSize() + "/" +
                    carFactory.getCarStorageMaxSize());
            bodyTotalMade.setText(TOTAL_MADE_TEMPL + carFactory.getBodyTotalMade());
            engineTotalMade.setText(TOTAL_MADE_TEMPL + carFactory.getEngineTotalMade());
            accessoriesTotalMade.setText(TOTAL_MADE_TEMPL + carFactory.getAccessoriesTotalMade());
            carTotalMade.setText(TOTAL_MADE_TEMPL + carFactory.getCarTotalMade());
        });
    }
}