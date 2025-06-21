package com.example.demo5;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable, DataReceiver {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private FlowPane featuredMedicinesContainer;

    @FXML
    private FlowPane medicinesContainer;

    @FXML
    private Button viewAllBtn;

    private NavbarController navbarController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialize the navbar controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("navbar.fxml"));
            AnchorPane navbarPane = loader.load();
            navbarController = loader.getController();

            // Set the navbar at the top
            if (mainBorderPane != null) {
                mainBorderPane.setTop(navbarPane);
            }

            // Check if user is logged in and update navbar
            if (getData.username != null && !getData.username.isEmpty()) {
                navbarController.updateNavigation();
            }

            // Highlight the current page in the navbar
            navbarController.highlightActivePage("home");

            // Load featured medicines here
            loadFeaturedMedicines();

            // Load all medicines here
            loadAllMedicines();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error initializing navbar: " + e.getMessage());
        }
    }

    private void loadFeaturedMedicines() {
        // This method would load featured medicines from the database
        // For now it's a placeholder
    }

    private void loadAllMedicines() {
        // This method would load all medicines from the database
        // For now it's a placeholder
    }

    @FXML
    private void navigateToProducts(ActionEvent event) {
        // Since products.fxml was deleted, we'll show an info alert instead
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Products");
        alert.setHeaderText("Coming Soon");
        alert.setContentText("The Products page is currently under construction. Please check back later!");
        alert.showAndWait();
    }

    @Override
    public void receiveData(Object data) {
        // Handle any data passed to this controller
    }
}
