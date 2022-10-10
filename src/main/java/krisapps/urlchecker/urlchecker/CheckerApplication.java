package krisapps.urlchecker.urlchecker;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CheckerApplication extends javafx.application.Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CheckerApplication.class.getResource("application_ui.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 700);
        stage.setTitle("URL Checker");
        stage.setScene(scene);
        stage.show();
        scene.getWindow().setOnCloseRequest(CheckerController.onWindowClosed());
        scene.getWindow().setOnShown(CheckerController.onWindowShown());
    }
}