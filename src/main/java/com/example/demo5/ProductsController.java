package com.example.demo5;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class ProductsController implements Initializable, DataReceiver {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private GridPane productsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> sortComboBox;

    private NavbarController navbarController;
    private Connection connect;
    private PreparedStatement prepare;
    private Statement statement;
    private ResultSet result;

    private List<ProductItem> currentProducts = new ArrayList<>();

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
            navbarController.highlightActivePage("products");

            // Initialize the sort combo box
            initSortComboBox();

            // Load all products
            loadAllProducts();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error initializing products page: " + e.getMessage());
        }
    }

    private void initSortComboBox() {
        sortComboBox.getItems().addAll(
            "Default",
            "Price: Low to High",
            "Price: High to Low"
        );
        sortComboBox.setValue("Default");
        sortComboBox.setOnAction(event -> applySorting());
    }

    private void applySorting() {
        String sortOption = sortComboBox.getValue();

        if ("Price: Low to High".equals(sortOption)) {
            currentProducts.sort(Comparator.comparing(ProductItem::getPrice));
        } else if ("Price: High to Low".equals(sortOption)) {
            currentProducts.sort(Comparator.comparing(ProductItem::getPrice).reversed());
        } else {
            // Default sorting - by ID
            currentProducts.sort(Comparator.comparing(ProductItem::getId));
        }

        displayProducts(currentProducts);
    }

    private void loadAllProducts() {
        connect = database.connectDb();

        try {
            productsContainer.getChildren().clear();
            currentProducts.clear();

            // Get all available products ordered by ID
            String sql = "SELECT * FROM medicine ORDER BY medicine_id ASC";
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();

            // Store products in a list first
            while (result.next()) {
                ProductItem product = new ProductItem(
                    result.getInt("medicine_id"),
                    result.getString("brand"),
                    result.getString("productName"),
                    result.getDouble("price"),
                    result.getString("image"),
                    result.getString("status")
                );
                currentProducts.add(product);
            }

            // Display products with current sorting
            applySorting();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading products: " + e.getMessage());
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

    @FXML
    private void searchProducts() {
        String searchText = searchField.getText().toLowerCase();

        if (searchText.isEmpty()) {
            loadAllProducts();
            return;
        }

        connect = database.connectDb();

        try {
            currentProducts.clear();

            // Search products by name or brand only (not type)
            String sql = "SELECT * FROM medicine WHERE LOWER(productName) LIKE ? OR LOWER(brand) LIKE ?";
            prepare = connect.prepareStatement(sql);
            String searchParam = "%" + searchText + "%";
            prepare.setString(1, searchParam);
            prepare.setString(2, searchParam);

            result = prepare.executeQuery();

            // Store products in a list first
            while (result.next()) {
                ProductItem product = new ProductItem(
                    result.getInt("medicine_id"),
                    result.getString("brand"),
                    result.getString("productName"),
                    result.getDouble("price"),
                    result.getString("image"),
                    result.getString("status")
                );
                currentProducts.add(product);
            }

            // Apply current sorting to search results
            applySorting();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error searching products: " + e.getMessage());
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

    private void displayProducts(List<ProductItem> products) {
        productsContainer.getChildren().clear();

        // Add products to grid (3 columns)
        int column = 0;
        int row = 0;
        for (ProductItem product : products) {
            VBox productBox = createProductBox(
                product.getId(),
                product.getBrand(),
                product.getProductName(),
                product.getPrice(),
                product.getImagePath(),
                product.getStatus()
            );
            productsContainer.add(productBox, column, row);

            column++;
            if (column == 3) { // After 3 columns, move to next row
                column = 0;
                row++;
            }
        }
    }

    private VBox createProductBox(int id, String brand, String productName, double price, String imagePath, String status) {
        VBox productBox = new VBox();
        productBox.setPrefWidth(250);
        productBox.setPrefHeight(250);
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

    // Inner class to represent a product item for sorting purposes
    private static class ProductItem {
        private int id;
        private String brand;
        private String productName;
        private double price;
        private String imagePath;
        private String status;

        public ProductItem(int id, String brand, String productName, double price, String imagePath, String status) {
            this.id = id;
            this.brand = brand;
            this.productName = productName;
            this.price = price;
            this.imagePath = imagePath;
            this.status = status;
        }

        public int getId() {
            return id;
        }

        public String getBrand() {
            return brand;
        }

        public String getProductName() {
            return productName;
        }

        public double getPrice() {
            return price;
        }

        public String getImagePath() {
            return imagePath;
        }

        public String getStatus() {
            return status;
        }
    }
}
