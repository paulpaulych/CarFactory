package carfactory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadException;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        try {
            Parent root = FXMLLoader.load(getClass().getResource("../resources/sample.fxml"));
            primaryStage.setTitle("Car Factory");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch(LoadException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}