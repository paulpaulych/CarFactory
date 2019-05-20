package carfactory;

import carfactory.exception.CarFactoryConfigException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadException;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main extends Application {

    private static final Logger log = LogManager.getLogger();

    @Override
    public void start(Stage primaryStage) throws Exception{
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../resources/sample.fxml"));
            Parent root = fxmlLoader.load();
            Controller controller = fxmlLoader.getController();
            CarFactory carFactory = new CarFactory();
            controller.setCarFactory(carFactory);
            primaryStage.setScene(new Scene(root));
            primaryStage.setOnCloseRequest(event ->
                carFactory.stop());
            primaryStage.setTitle("Car Factory");
            primaryStage.show();
        } catch(LoadException | CarFactoryConfigException e) {
            log.error(e);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}