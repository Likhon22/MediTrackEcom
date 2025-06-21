package com.example.demo5;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;

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
            // Get the resource URL first to verify it exists
            URL fxmlUrl = SceneSwitcher.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("Cannot find FXML resource: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Get the controller and set user data if provided
            if (userData != null) {
                Object controller = loader.getController();
                if (controller instanceof DataReceiver) {
                    ((DataReceiver) controller).receiveData(userData);
                }
            }

            // Set scene dimensions based on the page type
            Scene scene;

            // Login/signup page should be smaller (700x1000)
            if (fxmlPath.equals("hello-view.fxml")) {
                scene = new Scene(root, 1000, 700);
            } else {
                // All other pages use the regular size (1400x900)
                scene = new Scene(root, 1200, 800);
            }

            // Use the existing stage
            currentStage.setTitle(title);
            currentStage.setResizable(setResizable);
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to load the requested page: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Unexpected Error", "An unexpected error occurred: " + e.getMessage() +
                          "\nCheck if all resource paths in FXML files are correct.");
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

            // Set scene dimensions based on the page type
            Scene scene;

            // Login/signup page should be smaller (700x1000)
            if (fxmlPath.equals("hello-view.fxml")) {
                scene = new Scene(root, 1000, 700);
            } else {
                // All other pages use the regular size (1400x900)
                scene = new Scene(root, 1400, 900);
            }

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
