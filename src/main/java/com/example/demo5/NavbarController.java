package com.example.demo5;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class NavbarController implements Initializable {

    @FXML
    private AnchorPane navbar;

    @FXML
    private Button homeBtn;

    @FXML
    private Button productsBtn;

    @FXML
    private Button aboutBtn;

    @FXML
    private Button loginBtn;

    @FXML
    private Button signupBtn;

    @FXML
    private Button dashboardBtn;

    @FXML
    private Button cartBtn;

    @FXML
    private Button chatBtn;

    @FXML
    private Button logoutBtn;

    // Cart counter components
    private StackPane cartCounterPane;
    private Label cartCounterLabel;

    private boolean isLoggedIn = false;
    private String currentPage = "home";

    // Keep track of all navigation buttons for styling
    private Map<String, Button> navButtons = new HashMap<>();

    // Button styles
    private final String DEFAULT_BUTTON_STYLE = "-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand;";
    private final String ACTIVE_BUTTON_STYLE = "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;";
    private final String DASHBOARD_BUTTON_STYLE = "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-cursor: hand; -fx-border-radius: 5; -fx-border-color: white; -fx-border-width: 1; -fx-background-radius: 5;";

    // Database connection variables
    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("NavbarController initializing...");

        // Initialize the button map
        navButtons.put("home", homeBtn);
        navButtons.put("products", productsBtn);
        navButtons.put("about", aboutBtn);
        navButtons.put("login", loginBtn);
        navButtons.put("signup", signupBtn);
        navButtons.put("cart", cartBtn);

        // Initialize cart counter
        initializeCartCounter();

        // Check if user is logged in
        if (getData.username != null && !getData.username.isEmpty()) {
            System.out.println("User is logged in: " + getData.username);
            isLoggedIn = true;
            updateNavigation();
            // Update cart counter immediately to show correct count on any page
            updateCartCounter();
        } else {
            System.out.println("No user logged in");
        }

        // Set initial active page
        highlightActivePage("home");
    }

    /**
     * Initialize the cart counter components
     */
    private void initializeCartCounter() {
        cartCounterPane = new StackPane();
        Circle circle = new Circle(10, Color.RED);
        cartCounterLabel = new Label("0");
        cartCounterLabel.setFont(new Font("Arial", 12));
        cartCounterLabel.setTextFill(Color.WHITE);
        cartCounterPane.getChildren().addAll(circle, cartCounterLabel);

        // The positioning needs to happen after the component is rendered
        // Add the counter to the cart button
        cartBtn.setGraphic(new javafx.scene.layout.HBox(5, cartBtn.getGraphic(), cartCounterPane));

        cartCounterPane.setVisible(false);
    }

    /**
     * Update navigation based on login status and user role
     */
    public void updateNavigation() {
        System.out.println("Updating navigation - isLoggedIn: " + isLoggedIn + ", isAdmin: " + getData.isAdmin);

        if (isLoggedIn) {
            // When logged in, show logout instead of login/signup
            loginBtn.setVisible(false);
            signupBtn.setVisible(false);
            logoutBtn.setVisible(true);
            cartBtn.setVisible(true); // Show cart button for logged-in users
            chatBtn.setVisible(true); // Show chat button for logged-in users
            cartCounterPane.setVisible(true); // Show cart counter for logged-in users

            // If admin, show dashboard button
            if (getData.isAdmin) {
                dashboardBtn.setVisible(true);
                dashboardBtn.setStyle(DASHBOARD_BUTTON_STYLE);
            } else {
                dashboardBtn.setVisible(false);
            }
        } else {
            // When not logged in, show login/signup
            loginBtn.setVisible(true);
            signupBtn.setVisible(true);
            logoutBtn.setVisible(false);
            dashboardBtn.setVisible(false);
            cartBtn.setVisible(false); // Hide cart button for non-logged in users
            chatBtn.setVisible(false); // Hide chat button for non-logged in users
            cartCounterPane.setVisible(false); // Hide cart counter for non-logged in users
        }
    }

    /**
     * Highlight the active page button
     */
    public void highlightActivePage(String page) {
        System.out.println("Setting active page: " + page);
        currentPage = page;

        // Reset all buttons to default style
        for (Button button : navButtons.values()) {
            button.setStyle(DEFAULT_BUTTON_STYLE);
        }

        // Highlight the active button if it exists in our map
        if (navButtons.containsKey(page)) {
            navButtons.get(page).setStyle(ACTIVE_BUTTON_STYLE);
        }

        // Always maintain dashboard button style if visible
        if (dashboardBtn != null && dashboardBtn.isVisible()) {
            dashboardBtn.setStyle(DASHBOARD_BUTTON_STYLE);
        }
    }

    /**
     * Set the user role
     */
    public void setAdminStatus(boolean isAdmin) {
        System.out.println("Setting admin status: " + isAdmin);
        getData.isAdmin = isAdmin;
        updateNavigation();
    }

    /**
     * Update the cart counter to reflect the current number of items in the cart
     */
    public void updateCartCounter() {
        if (!isLoggedIn || getData.username == null || getData.username.isEmpty()) {
            cartCounterPane.setVisible(false);
            return;
        }

        try {
            connect = database.connectDb();
            String sql = "SELECT SUM(quantity) as totalItems FROM cart WHERE user_id = ?";
            prepare = connect.prepareStatement(sql);
            prepare.setString(1, getData.username);
            result = prepare.executeQuery();

            int totalItems = 0;
            if (result.next()) {
                totalItems = result.getInt("totalItems");
            }

            if (totalItems > 0) {
                cartCounterLabel.setText(String.valueOf(totalItems));
                cartCounterPane.setVisible(true);
            } else {
                cartCounterPane.setVisible(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error updating cart counter: " + e.getMessage());
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
    private void navigateToHome(ActionEvent event) {
        highlightActivePage("home");
        Stage stage = (Stage) navbar.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "home.fxml", "MediTrack - Home", false, null);
    }

    @FXML
    private void navigateToAbout(ActionEvent event) {
        highlightActivePage("about");
        Stage stage = (Stage) navbar.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "about.fxml", "MediTrack - About", false, null);
    }

    @FXML
    private void navigateToLogin(ActionEvent event) {
        highlightActivePage("login");
        Stage stage = (Stage) navbar.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "hello-view.fxml", "MediTrack - Login", false, null);
    }

    @FXML
    private void navigateToSignup(ActionEvent event) {
        highlightActivePage("signup");
        Stage stage = (Stage) navbar.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "hello-view.fxml", "MediTrack - Sign Up", false, "signup");
    }

    @FXML
    private void navigateToDashboard(ActionEvent event) {
        Stage stage = (Stage) navbar.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "dashboard.fxml", "MediTrack - Dashboard", false, null);
    }

    @FXML
    private void navigateToCart(ActionEvent event) {
        highlightActivePage("cart");
        Stage stage = (Stage) navbar.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "cart.fxml", "MediTrack - Cart", false, null);
    }

    @FXML
    private void navigateToProducts(ActionEvent event) {
        highlightActivePage("products");
        Stage stage = (Stage) navbar.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "products.fxml", "MediTrack - Products", false, null);
    }

    @FXML
    private void navigateToChat(ActionEvent event) {
        highlightActivePage("chat");
        Stage stage = (Stage) navbar.getScene().getWindow();

        // Navigate to the appropriate chat page based on user role
        if (getData.isAdmin) {
            SceneSwitcher.switchScene(stage, "admin_chat.fxml", "MediTrack - Admin Support Chat", false, null);
        } else {
            SceneSwitcher.switchScene(stage, "chat.fxml", "MediTrack - Chat with Support", false, null);
        }
    }

    @FXML
    private void logout(ActionEvent event) {
        // Clear user data
        getData.username = null;
        isLoggedIn = false;
        getData.isAdmin = false;

        // Update navigation
        updateNavigation();

        // Navigate to home
        highlightActivePage("home");
        Stage stage = (Stage) navbar.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "home.fxml", "MediTrack - Home", false, null);
    }
}

