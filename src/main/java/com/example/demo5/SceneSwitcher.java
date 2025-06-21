package com.example.demo5;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class SceneSwitcher {

    /**
     * Switch to a different scene
     *
     * @param currentStage The current stage
     * @param fxmlPath The path to the FXML file for the new scene
     * @param title The title for the new stage
     * @param setResizable Whether the new stage should be resizable
     * @param userData Optional data to pass to the new scene's controller
     */
    public static void switchScene(Stage currentStage, String fxmlPath, String title,
                                  boolean setResizable, Object userData) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Get the controller and set user data if provided
            if (userData != null) {
                Object controller = loader.getController();
                if (controller instanceof DataReceiver) {
                    ((DataReceiver) controller).receiveData(userData);
                }
            }

            Scene scene = new Scene(root);

            // Use the existing stage
            currentStage.setTitle(title);
            currentStage.setResizable(setResizable);
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to load the requested page: " + e.getMessage());
        }
    }

    /**
     * Create a new stage and show the specified scene
     */
    public static void openNewStage(String fxmlPath, String title, boolean setResizable,
                                   StageStyle stageStyle, Object userData) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Get the controller and set user data if provided
            if (userData != null) {
                Object controller = loader.getController();
                if (controller instanceof DataReceiver) {
                    ((DataReceiver) controller).receiveData(userData);
                }
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setResizable(setResizable);

            if (stageStyle != null) {
                stage.initStyle(stageStyle);
            }

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to open new window: " + e.getMessage());
        }
    }

    /**
     * Show an error alert
     */
    private static void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
