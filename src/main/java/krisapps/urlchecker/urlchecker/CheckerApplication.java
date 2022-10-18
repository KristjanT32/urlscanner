package krisapps.urlchecker.urlchecker;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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

        // Configure window properties.
        stage.setResizable(false);
        stage.setTitle("URL Checker");
        stage.getIcons().add(new Image("/images/icon.jpeg"));
        stage.setScene(scene);
        stage.show();

        scene.getWindow().setOnCloseRequest(CheckerController.onWindowClosed());
        CheckerController.sceneInstance = scene;
        CheckerController.stageInstance = stage;
    }


}