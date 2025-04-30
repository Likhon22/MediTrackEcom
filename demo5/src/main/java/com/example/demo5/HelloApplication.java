package com.example.demo5;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class HelloApplication extends Application {
    private double x=0;
    private double y=0;
    @Override
    public void start(Stage stage) throws IOException {
        Parent root =  FXMLLoader.load (getClass().getResource("hello-view.fxml"));
        Scene scene = new Scene( root,700, 600);

        stage.setTitle("MediTrack");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}