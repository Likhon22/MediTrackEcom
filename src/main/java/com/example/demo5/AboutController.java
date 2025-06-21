package com.example.demo5;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AboutController implements Initializable, DataReceiver {

    @FXML
    private BorderPane mainBorderPane;

    private NavbarController navbarController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialize the navbar controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("navbar.fxml"));
            AnchorPane navbarPane = loader.load();
            navbarController = loader.getController();

            // Set the navbar at the top with proper spacing
            if (mainBorderPane != null) {
                mainBorderPane.setTop(navbarPane);
            }

            // Check if user is logged in and update navbar
            if (getData.username != null && !getData.username.isEmpty()) {
                navbarController.updateNavigation();
            }

            // Highlight the current page in the navbar
            navbarController.highlightActivePage("about");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error initializing navbar: " + e.getMessage());
        }
    }

    @FXML
    private void navigateToHome(ActionEvent event) {
        Stage stage = (Stage) mainBorderPane.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "home.fxml", "MediTrack - Home", false, null);
    }

    @FXML
    private void navigateToProducts(ActionEvent event) {
        Stage stage = (Stage) mainBorderPane.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "products.fxml", "MediTrack - Products", false, null);
    }

    @Override
    public void receiveData(Object data) {
        // Handle any data passed to this controller
    }
}
