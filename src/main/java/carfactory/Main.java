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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Scanner;

public class Main extends Application {

    private static final Logger log = LogManager.getLogger();

    @Override
    public void start(Stage primaryStage) throws Exception{
        try {

            InputStream fxml = getClass().getClassLoader()
                    .getResourceAsStream("sample.fxml");
            assert fxml != null;
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader()
                    .getResource("sample.fxml"));
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