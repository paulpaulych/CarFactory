package carfactory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadException;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class Main extends Application {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) throws Exception{
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader()
                    .getResource("scene.fxml"));
            Parent root = fxmlLoader.load();
            Controller controller = fxmlLoader.getController();
            var properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("carfactory.properties"));
            controller.setCarFactoryConfig(Config.from(properties));
            primaryStage.setScene(new Scene(root));
            primaryStage.setOnCloseRequest(event -> System.exit(0));
            primaryStage.setTitle("Car Factory");
            primaryStage.show();
        } catch(LoadException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}