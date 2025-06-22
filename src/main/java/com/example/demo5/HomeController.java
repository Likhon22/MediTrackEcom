package com.example.demo5;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HomeController implements Initializable, DataReceiver {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private GridPane medicinesContainer;

    @FXML
    private Button viewAllBtn;

    private NavbarController navbarController;
    private Connection connect;
    private PreparedStatement prepare;
    private Statement statement;
    private ResultSet result;

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

            // Load last 5 products
            loadLatestProducts();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error initializing navbar: " + e.getMessage());
        }
    }

    private void loadLatestProducts() {
        connect = database.connectDb();

        try {
            medicinesContainer.getChildren().clear();

            // Query to get the last 5 added products ordered by ID descending
            String sql = "SELECT * FROM medicine ORDER BY id DESC LIMIT 5";
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();

            // Store products in a list
            List<VBox> productBoxes = new ArrayList<>();
            while (result.next()) {
                VBox productBox = createProductBox(
                    result.getInt("medicine_id"),
                    result.getString("brand"),
                    result.getString("productName"),
                    result.getDouble("price"),
                    result.getString("image"),
                    result.getString("status")
                );

                productBoxes.add(productBox);
            }

            // Add products to grid (3 columns)
            int column = 0;
            int row = 0;
            for (VBox productBox : productBoxes) {
                medicinesContainer.add(productBox, column, row);

                column++;
                if (column == 3) { // After 3 columns, move to next row
                    column = 0;
                    row++;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading latest products: " + e.getMessage());
        } finally {
            try {
                if (result != null) result.close();
                if (prepare != null) prepare.close();
                if (connect != null) connect.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private VBox createProductBox(int id, String brand, String productName, double price, String imagePath, String status) {
        VBox productBox = new VBox();
        productBox.setPrefWidth(250);
        productBox.setPrefHeight(280);
        productBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 10); -fx-cursor: hand;");
        productBox.setPadding(new Insets(15));
        productBox.setSpacing(8);

        ImageView imageView = new ImageView();
        try {
            Image image;
            if (imagePath != null && !imagePath.isEmpty()) {
                File file = new File(imagePath);
                if (file.exists()) {
                    image = new Image(file.toURI().toString(), 180, 140, true, true);
                } else {
                    // Default image if file doesn't exist
                    image = new Image(getClass().getResourceAsStream("/images/m.png"), 180, 140, true, true);
                }
            } else {
                // Default image if path is null or empty
                image = new Image(getClass().getResourceAsStream("/images/m.png"), 180, 140, true, true);
            }
            imageView.setImage(image);
            imageView.setFitWidth(180);
            imageView.setFitHeight(140);
            imageView.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
        }

        Label nameLabel = new Label(productName);
        nameLabel.setFont(Font.font("System", 16));
        nameLabel.setWrapText(true);

        Label brandLabel = new Label(brand);
        brandLabel.setStyle("-fx-text-fill: #666666;");
        brandLabel.setFont(Font.font("System", 14));

        Label priceLabel = new Label(String.format("$%.2f", price));
        priceLabel.setStyle("-fx-text-fill: #449b6d; -fx-font-weight: bold;");
        priceLabel.setFont(Font.font("System", 14));

        Label statusLabel = new Label("Status: " + status);
        statusLabel.setStyle("-fx-text-fill: " + (status.equals("Available") ? "#449b6d" : "#cc0000") + ";");

        productBox.getChildren().addAll(imageView, nameLabel, brandLabel, priceLabel, statusLabel);

        // Add click event to navigate to product details
        final int productId = id;
        productBox.setOnMouseClicked(event -> {
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            SceneSwitcher.switchScene(stage, "product_details.fxml", "MediTrack - Product Details", false, productId);
        });

        return productBox;
    }

    @FXML
    private void navigateToProducts(ActionEvent event) {
        Stage stage = (Stage) mainBorderPane.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "products.fxml", "MediTrack - Products", false, null);
    }

    @FXML
    private void navigateToHome(ActionEvent event) {
        Stage stage = (Stage) mainBorderPane.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "home.fxml", "MediTrack - Home", false, null);
    }

    @FXML
    private void navigateToAbout(ActionEvent event) {
        Stage stage = (Stage) mainBorderPane.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "about.fxml", "MediTrack - About", false, null);
    }

    @Override
    public void receiveData(Object data) {
        // Handle any data passed to this controller
    }
}

