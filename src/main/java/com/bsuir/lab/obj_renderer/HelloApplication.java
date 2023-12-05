package com.bsuir.lab.obj_renderer;

import com.bsuir.lab.obj_renderer.model.WindowConstants;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), WindowConstants.WINDOW_WIDTH, WindowConstants.WINDOW_HEIGHT);
        scene.setFill(Color.GRAY);

        stage.setTitle(".OBJ renderer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}