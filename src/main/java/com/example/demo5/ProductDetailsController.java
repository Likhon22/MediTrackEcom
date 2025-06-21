package com.example.demo5;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ProductDetailsController implements Initializable, DataReceiver {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Label breadcrumbProductName;

    @FXML
    private ImageView productImage;

    @FXML
    private Label productNameLabel;

    @FXML
    private Label brandLabel;

    @FXML
    private Label categoryLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Spinner<Integer> quantitySpinner;

    @FXML
    private Button addToCartButton;

    @FXML
    private Label addToCartMessage;

    @FXML
    private Text descriptionText;

    @FXML
    private Text usageText;

    @FXML
    private HBox relatedProductsContainer;

    @FXML
    private Label noRelatedProductsLabel;

    private NavbarController navbarController;
    private int medicineId;
    private double productPrice;
    private String productName;
    private String productStatus;
    private int maxQuantity = 10; // Default max quantity

    // Database connection variables
    private Connection connect;
    private PreparedStatement prepare;
    private Statement statement;
    private ResultSet result;

    // Product descriptions map (could be loaded from database in a real app)
    private Map<String, String> productDescriptions;
    private Map<String, String> productUsages;

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

            // Setup quantity spinner
            SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxQuantity, 1);
            quantitySpinner.setValueFactory(valueFactory);

            // Initialize product descriptions (in a real app, these would come from a database)
            initializeProductInfo();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error initializing product details page: " + e.getMessage());
        }
    }

    private void initializeProductInfo() {
        // Initialize product descriptions
        productDescriptions = new HashMap<>();
        productDescriptions.put("default", "This is a high-quality pharmaceutical product designed to meet your healthcare needs. "
                + "Made with carefully selected ingredients to ensure effectiveness and safety. "
                + "Our products undergo rigorous quality control to ensure they meet the highest standards of pharmaceutical excellence.");

        // Initialize product usage information
        productUsages = new HashMap<>();
        productUsages.put("default", "Take as directed by your healthcare provider. "
                + "The typical dosage is one tablet twice daily with meals. "
                + "Do not exceed the recommended dosage. Store in a cool, dry place away from direct sunlight. "
                + "Keep out of reach of children. If symptoms persist, consult your doctor.");

        // Add type-specific descriptions
        productDescriptions.put("Hydrocodone", "Hydrocodone is prescribed for the relief of moderate to severe pain. "
                + "It's a narcotic analgesic and works by changing the way the brain and nervous system respond to pain. "
                + "This medication should be used exactly as prescribed by your doctor to avoid potential risks.");

        productDescriptions.put("Antibiotics", "Our antibiotic medications are designed to fight bacterial infections. "
                + "They work by killing bacteria or preventing their growth. "
                + "It's important to complete the full course of antibiotics even if symptoms improve before the medication is finished.");

        productDescriptions.put("Metformin", "Metformin is primarily used to treat type 2 diabetes by decreasing glucose production by the liver. "
                + "It also increases sensitivity to insulin, improving your body's response to the insulin it makes naturally. "
                + "This medication is often prescribed alongside lifestyle changes like healthy eating and regular exercise.");

        productDescriptions.put("Losartan", "Losartan is an angiotensin II receptor blocker (ARB) used to treat high blood pressure and heart failure. "
                + "It relaxes blood vessels so blood can flow more easily, helping to prevent strokes, heart attacks, and kidney problems. "
                + "Regular monitoring by your healthcare provider is recommended when taking this medication.");

        productDescriptions.put("Albuterol", "Albuterol is a bronchodilator that relaxes muscles in the airways and increases air flow to the lungs. "
                + "It's used to prevent and treat wheezing, shortness of breath, and chest tightness caused by lung diseases such as asthma and COPD. "
                + "This medication can be life-saving during acute breathing difficulties.");

        // Add type-specific usage guidelines
        productUsages.put("Hydrocodone", "Take exactly as prescribed, typically every 4-6 hours as needed for pain. "
                + "May be taken with or without food, but taking with food may decrease nausea. "
                + "Do not crush, break, or chew extended-release tablets. "
                + "This medication may cause drowsiness; avoid driving or operating machinery until you know how it affects you.");

        productUsages.put("Antibiotics", "Take at evenly spaced intervals to maintain constant levels in your body. "
                + "Complete the full prescribed course even if symptoms improve. "
                + "Some antibiotics should be taken with food, while others should be taken on an empty stomach. "
                + "Probiotics may be beneficial during and after antibiotic treatment to maintain gut health.");

        productUsages.put("Metformin", "Typically taken with meals to reduce stomach upset. "
                + "Start with a low dose that is gradually increased to minimize side effects. "
                + "Extended-release formulations are usually taken once daily with the evening meal. "
                + "Regular monitoring of blood glucose levels is essential while taking this medication.");

        productUsages.put("Losartan", "Usually taken once daily, with or without food. "
                + "Try to take at the same time each day to maintain consistent blood pressure control. "
                + "May cause dizziness, especially when standing up quickly. "
                + "Continue taking even if you feel well, as high blood pressure often has no symptoms.");

        productUsages.put("Albuterol", "For inhalers: Shake well before use. Exhale fully, then place the mouthpiece in your mouth and inhale deeply while pressing down on the inhaler. "
                + "Wait at least 1 minute between inhalations if multiple doses are prescribed. "
                + "Rinse mouth after use to prevent irritation. "
                + "For nebulizer solutions: Use as directed with appropriate nebulizer equipment.");
    }

    @Override
    public void receiveData(Object data) {
        if (data instanceof Integer) {
            medicineId = (Integer) data;
            loadProductDetails();
        }
    }

    private void loadProductDetails() {
        String sql = "SELECT * FROM medicine WHERE medicine_id = ?";
        connect = database.connectDb();

        try {
            prepare = connect.prepareStatement(sql);
            prepare.setInt(1, medicineId);
            result = prepare.executeQuery();

            if (result.next()) {
                // Get product details
                productName = result.getString("productName");
                String brand = result.getString("brand");
                String type = result.getString("type");
                productPrice = result.getDouble("price");
                String image = result.getString("image");
                productStatus = result.getString("status");

                // Update UI
                breadcrumbProductName.setText(productName);
                productNameLabel.setText(productName);
                brandLabel.setText(brand);
                categoryLabel.setText(type);
                priceLabel.setText(String.format("$%.2f", productPrice));

                // Set status with appropriate color
                statusLabel.setText(productStatus);
                if (productStatus.equals("Available")) {
                    statusLabel.setStyle("-fx-text-fill: #449b6d;");
                    addToCartButton.setDisable(false);
                } else {
                    statusLabel.setStyle("-fx-text-fill: #cc0000;");
                    addToCartButton.setDisable(true);
                }

                // Load image
                loadProductImage(image);

                // Set product description and usage info
                if (productDescriptions.containsKey(type)) {
                    descriptionText.setText(productDescriptions.get(type));
                } else {
                    descriptionText.setText(productDescriptions.get("default"));
                }

                if (productUsages.containsKey(type)) {
                    usageText.setText(productUsages.get(type));
                } else {
                    usageText.setText(productUsages.get("default"));
                }

                // Load similar products based on brand
                loadSimilarProducts(brand);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading product details: " + e.getMessage());
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

    /**
     * Load similar products based on the brand name
     *
     * @param brand The brand name to match
     */
    private void loadSimilarProducts(String brand) {
        // Clear existing products
        if (relatedProductsContainer != null) {
            relatedProductsContainer.getChildren().clear();
        }

        String sql = "SELECT * FROM medicine WHERE brand = ? AND medicine_id != ? AND status = 'Available' LIMIT 3";
        connect = database.connectDb();

        try {
            prepare = connect.prepareStatement(sql);
            prepare.setString(1, brand);
            prepare.setInt(2, medicineId); // Exclude the current product
            result = prepare.executeQuery();

            boolean foundSimilarProducts = false;

            // Add up to 3 similar products
            while (result.next()) {
                foundSimilarProducts = true;
                VBox productBox = createSimilarProductBox(
                    result.getInt("medicine_id"),
                    result.getString("brand"),
                    result.getString("productName"),
                    result.getDouble("price"),
                    result.getString("image")
                );
                relatedProductsContainer.getChildren().add(productBox);
            }

            // Show "No similar products" message if none found
            if (noRelatedProductsLabel != null) {
                noRelatedProductsLabel.setVisible(!foundSimilarProducts);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading similar products: " + e.getMessage());
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

    /**
     * Create a product box for similar products
     */
    private VBox createSimilarProductBox(int id, String brand, String productName, double price, String imagePath) {
        VBox productBox = new VBox();
        productBox.setPrefWidth(200);
        productBox.setPrefHeight(250);
        productBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 10); -fx-cursor: hand;");
        productBox.setPadding(new javafx.geometry.Insets(10));
        productBox.setSpacing(5);

        ImageView imageView = new ImageView();
        try {
            Image image;
            if (imagePath != null && !imagePath.isEmpty()) {
                File file = new File(imagePath);
                if (file.exists()) {
                    image = new Image(file.toURI().toString(), 180, 120, true, true);
                } else {
                    // Default image if file doesn't exist
                    InputStream is = getClass().getResourceAsStream("/images/m.png");
                    if (is != null) {
                        image = new Image(is, 180, 120, true, true);
                    } else {
                        image = new Image("https://via.placeholder.com/180x120", 180, 120, true, true);
                    }
                }
            } else {
                // Default image if path is null or empty
                InputStream is = getClass().getResourceAsStream("/images/m.png");
                if (is != null) {
                    image = new Image(is, 180, 120, true, true);
                } else {
                    image = new Image("https://via.placeholder.com/180x120", 180, 120, true, true);
                }
            }
            imageView.setImage(image);
            imageView.setFitWidth(180);
            imageView.setFitHeight(120);
            imageView.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
        }

        Label nameLabel = new Label(productName);
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label brandLabel = new Label(brand);
        brandLabel.setStyle("-fx-text-fill: #666666;");

        Label priceLabel = new Label(String.format("$%.2f", price));
        priceLabel.setStyle("-fx-text-fill: #449b6d; -fx-font-weight: bold;");

        productBox.getChildren().addAll(imageView, nameLabel, brandLabel, priceLabel);

        // Add click event to navigate to product details
        final int productId = id;
        productBox.setOnMouseClicked(event -> {
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            SceneSwitcher.switchScene(stage, "product_details.fxml", "MediTrack - Product Details", false, productId);
        });

        return productBox;
    }

    private void loadProductImage(String imagePath) {
        try {
            Image image;
            if (imagePath != null && !imagePath.isEmpty()) {
                File file = new File(imagePath);
                if (file.exists()) {
                    image = new Image(file.toURI().toString(), 350, 350, true, true);
                } else {
                    // Default image if file doesn't exist
                    image = new Image(getClass().getResourceAsStream("/images/m.png"), 350, 350, true, true);
                }
            } else {
                // Default image if path is null or empty
                image = new Image(getClass().getResourceAsStream("/images/m.png"), 350, 350, true, true);
            }
            productImage.setImage(image);
        } catch (Exception e) {
            System.out.println("Error loading product image: " + e.getMessage());
            try {
                // Final fallback - use a placeholder image
                productImage.setImage(new Image(getClass().getResourceAsStream("/images/m.png"), 350, 350, true, true));
            } catch (Exception ex) {
                System.out.println("Even default image failed to load: " + ex.getMessage());
            }
        }
    }

    @FXML
    private void addToCart(ActionEvent event) {
        if (getData.username == null || getData.username.isEmpty()) {
            // If not logged in, redirect to login page
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            SceneSwitcher.switchScene(stage, "hello-view.fxml", "MediTrack - Login", false, null);
            return;
        }

        int quantity = quantitySpinner.getValue();

        // Add to cart database
        try {
            connect = database.connectDb();

            // Check if this medicine is already in the user's cart
            String checkSql = "SELECT * FROM cart WHERE user_id = ? AND medicine_id = ?";
            prepare = connect.prepareStatement(checkSql);
            prepare.setString(1, getData.username);
            prepare.setInt(2, medicineId);
            result = prepare.executeQuery();

            if (result.next()) {
                // Item already in cart, update quantity
                int existingQuantity = result.getInt("quantity");
                int newQuantity = existingQuantity + quantity;

                String updateSql = "UPDATE cart SET quantity = ? WHERE user_id = ? AND medicine_id = ?";
                prepare = connect.prepareStatement(updateSql);
                prepare.setInt(1, newQuantity);
                prepare.setString(2, getData.username);
                prepare.setInt(3, medicineId);
                prepare.executeUpdate();
            } else {
                // Add new item to cart
                String insertSql = "INSERT INTO cart (user_id, medicine_id, quantity) VALUES (?, ?, ?)";
                prepare = connect.prepareStatement(insertSql);
                prepare.setString(1, getData.username);
                prepare.setInt(2, medicineId);
                prepare.setInt(3, quantity);
                prepare.executeUpdate();
            }

            // Show success message and fade it out
            addToCartMessage.setVisible(true);

            // Create fade out animation for message
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), addToCartMessage);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.play();
            });
            pause.play();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error adding to cart: " + e.getMessage());
        } finally {
            try {
                if (result != null) result.close();
                if (prepare != null) prepare.close();
                if (connect != null) connect.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Log the action for debugging
        System.out.println("Added to cart: Product ID: " + medicineId +
                ", Name: " + productName +
                ", Price: " + productPrice +
                ", Quantity: " + quantity);
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

    @FXML
    private void navigateToAbout(ActionEvent event) {
        Stage stage = (Stage) mainBorderPane.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "about.fxml", "MediTrack - About", false, null);
    }
}
