package com.example.demo5;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.ResourceBundle;

public class CartController implements Initializable, DataReceiver {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private VBox emptyCartMessage;

    @FXML
    private HBox cartContentArea;

    @FXML
    private TableView<CartItem> cartTableView;

    @FXML
    private TableColumn<CartItem, String> productImageCol;

    @FXML
    private TableColumn<CartItem, String> productNameCol;

    @FXML
    private TableColumn<CartItem, Double> productPriceCol;

    @FXML
    private TableColumn<CartItem, Integer> productQuantityCol;

    @FXML
    private TableColumn<CartItem, Double> productTotalCol;

    @FXML
    private TableColumn<CartItem, Void> actionCol;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label taxLabel;

    @FXML
    private Label shippingLabel;

    @FXML
    private Label totalLabel;

    @FXML
    private Button checkoutButton;

    @FXML
    private Button continueShoppingButton;

    private NavbarController navbarController;
    private ObservableList<CartItem> cartItems;

    // Connection objects for database
    private Connection connect;
    private PreparedStatement prepare;
    private Statement statement;
    private ResultSet result;

    // Formatting for currency display
    private final DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");

    // Shipping cost constant
    private static final double SHIPPING_COST = 5.00;
    // Tax rate constant (5%)
    private static final double TAX_RATE = 0.05;

    /**
     * Update the cart counter in the navbar
     */
    private void updateNavbarCartCounter() {
        if (navbarController != null) {
            navbarController.updateCartCounter();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Load navbar
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
                // Highlight the cart button in the navbar
                navbarController.highlightActivePage("cart");
            } else {
                // If not logged in, redirect to login page
                Stage stage = (Stage) mainBorderPane.getScene().getWindow();
                SceneSwitcher.switchScene(stage, "hello-view.fxml", "MediTrack - Login", false, null);
                return;
            }

            // Setup table columns
            setupTableColumns();

            // Load cart items
            loadCartItems();

            // Update order summary
            calculateOrderSummary();

            // Update cart counter in navbar
            updateNavbarCartCounter();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error initializing cart page: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        // Product Image Column
        productImageCol.setCellValueFactory(data -> data.getValue().imagePathProperty());
        productImageCol.setCellFactory(param -> {
            return new TableCell<CartItem, String>() {
                private final ImageView imageView = new ImageView();

                @Override
                protected void updateItem(String imagePath, boolean empty) {
                    super.updateItem(imagePath, empty);

                    if (empty || imagePath == null) {
                        setGraphic(null);
                    } else {
                        try {
                            Image image;
                            if (imagePath != null && !imagePath.isEmpty()) {
                                File file = new File(imagePath);
                                if (file.exists()) {
                                    image = new Image(file.toURI().toString(), 50, 50, true, true);
                                } else {
                                    // Default image if file doesn't exist
                                    image = new Image(getClass().getResourceAsStream("/images/m.png"), 50, 50, true, true);
                                }
                            } else {
                                // Default image if path is null or empty
                                image = new Image(getClass().getResourceAsStream("/images/m.png"), 50, 50, true, true);
                            }
                            imageView.setImage(image);
                            imageView.setFitWidth(50);
                            imageView.setFitHeight(50);
                            imageView.setPreserveRatio(true);
                            setGraphic(imageView);
                        } catch (Exception e) {
                            setGraphic(null);
                            System.out.println("Error loading image: " + e.getMessage());
                        }
                    }
                }
            };
        });

        // Product Name Column
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

        // Product Price Column
        productPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        productPriceCol.setCellFactory(tc -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(price));
                }
            }
        });

        // Product Quantity Column - Make it editable with spinner
        productQuantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        productQuantityCol.setCellFactory(tc -> new TableCell<CartItem, Integer>() {
            private Spinner<Integer> spinner;

            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);

                if (empty || quantity == null) {
                    setGraphic(null);
                } else {
                    spinner = new Spinner<>(1, 10, quantity);
                    spinner.setEditable(true);
                    spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                        CartItem item = getTableView().getItems().get(getIndex());
                        updateCartItemQuantity(item.getCartId(), newValue);
                        item.setQuantity(newValue);
                        calculateOrderSummary();
                    });
                    spinner.setPrefWidth(80);
                    setGraphic(spinner);
                }
            }
        });

        // Total Price Column
        productTotalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        productTotalCol.setCellFactory(tc -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(total));
                }
            }
        });

        // Action Column (Remove button)
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button removeButton = new Button("✕");

            {
                removeButton.setStyle("-fx-background-color: #ff5252; -fx-text-fill: white; -fx-background-radius: 5;");
                removeButton.setPadding(new Insets(2, 5, 2, 5));
                removeButton.setOnAction(event -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    removeFromCart(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
    }

    /**
     * Load cart items from the database for the current user
     */
    private void loadCartItems() {
        cartItems = FXCollections.observableArrayList();

        try {
            connect = database.connectDb();

            // SQL to get cart items with product details
            String sql = "SELECT c.cart_id, c.user_id, c.medicine_id, c.quantity, " +
                        "m.brand, m.productName, m.price, m.image " +
                        "FROM cart c " +
                        "JOIN medicine m ON c.medicine_id = m.medicine_id " +
                        "WHERE c.user_id = ?";

            prepare = connect.prepareStatement(sql);
            prepare.setString(1, getData.username);
            result = prepare.executeQuery();

            while (result.next()) {
                cartItems.add(new CartItem(
                    result.getInt("cart_id"),
                    result.getString("user_id"),
                    result.getInt("medicine_id"),
                    result.getString("productName"),
                    result.getString("brand"),
                    result.getDouble("price"),
                    result.getInt("quantity"),
                    result.getString("image")
                ));
            }

            cartTableView.setItems(cartItems);

            // Show/hide appropriate sections based on cart contents
            boolean isEmpty = cartItems.isEmpty();
            emptyCartMessage.setVisible(isEmpty);
            cartContentArea.setVisible(!isEmpty);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading cart items: " + e.getMessage());
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
     * Update the quantity of a cart item in the database
     */
    private void updateCartItemQuantity(int cartId, int newQuantity) {
        try {
            connect = database.connectDb();

            String sql = "UPDATE cart SET quantity = ? WHERE cart_id = ?";
            prepare = connect.prepareStatement(sql);
            prepare.setInt(1, newQuantity);
            prepare.setInt(2, cartId);
            prepare.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error updating cart item quantity: " + e.getMessage());
        } finally {
            try {
                if (prepare != null) prepare.close();
                if (connect != null) connect.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Remove an item from the cart
     */
    private void removeFromCart(CartItem item) {
        try {
            connect = database.connectDb();

            // Confirm deletion
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Remove Item");
            alert.setHeaderText(null);
            alert.setContentText("Remove " + item.getProductName() + " from your cart?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Delete from database
                String sql = "DELETE FROM cart WHERE cart_id = ?";
                prepare = connect.prepareStatement(sql);
                prepare.setInt(1, item.getCartId());
                prepare.executeUpdate();

                // Remove from list
                cartItems.remove(item);

                // Update UI
                boolean isEmpty = cartItems.isEmpty();
                emptyCartMessage.setVisible(isEmpty);
                cartContentArea.setVisible(!isEmpty);

                // Recalculate totals
                calculateOrderSummary();

                // Update the cart counter in navbar
                updateNavbarCartCounter();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error removing item from cart: " + e.getMessage());
        } finally {
            try {
                if (prepare != null) prepare.close();
                if (connect != null) connect.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Calculate and update the order summary
     */
    private void calculateOrderSummary() {
        double subtotal = 0.0;

        for (CartItem item : cartItems) {
            subtotal += item.getTotal();
        }

        double tax = subtotal * TAX_RATE;
        double shipping = cartItems.isEmpty() ? 0.0 : SHIPPING_COST;
        double total = subtotal + tax + shipping;

        // Update labels
        subtotalLabel.setText(currencyFormat.format(subtotal));
        taxLabel.setText(currencyFormat.format(tax));
        shippingLabel.setText(currencyFormat.format(shipping));
        totalLabel.setText(currencyFormat.format(total));

        // Disable checkout button if cart is empty
        checkoutButton.setDisable(cartItems.isEmpty());
    }

    /**
     * Process checkout
     */
    @FXML
    private void checkout() {
        try {
            connect = database.connectDb();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Order Placed");
            alert.setHeaderText(null);
            alert.setContentText("Your order has been placed successfully!\n\nThank you for shopping with MediTrack.");
            alert.showAndWait();

            // Clear the cart after checkout
            String sql = "DELETE FROM cart WHERE user_id = ?";
            prepare = connect.prepareStatement(sql);
            prepare.setString(1, getData.username);
            prepare.executeUpdate();

            // Navigate back to home
            navigateToHome();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error during checkout: " + e.getMessage());

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Checkout Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred during checkout. Please try again.");
            alert.showAndWait();
        } finally {
            try {
                if (prepare != null) prepare.close();
                if (connect != null) connect.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void navigateToHome() {
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

    @Override
    public void receiveData(Object data) {
        // Handle any data passed to this controller
    }
}
