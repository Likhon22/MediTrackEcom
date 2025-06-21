package com.example.demo5;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
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
    private Button logoutBtn;

    private boolean isLoggedIn = false;
    private String currentPage = "home";

    // Keep track of all navigation buttons for styling
    private Map<String, Button> navButtons = new HashMap<>();

    // Button styles
    private final String DEFAULT_BUTTON_STYLE = "-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand;";
    private final String ACTIVE_BUTTON_STYLE = "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;";
    private final String DASHBOARD_BUTTON_STYLE = "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-cursor: hand; -fx-border-radius: 5; -fx-border-color: white; -fx-border-width: 1; -fx-background-radius: 5;";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("NavbarController initializing...");

        // Initialize the button map
        navButtons.put("home", homeBtn);
        navButtons.put("products", productsBtn);
        navButtons.put("about", aboutBtn);
        navButtons.put("login", loginBtn);
        navButtons.put("signup", signupBtn);

        // Check if user is logged in
        if (getData.username != null && !getData.username.isEmpty()) {
            System.out.println("User is logged in: " + getData.username);
            isLoggedIn = true;
            updateNavigation();
        } else {
            System.out.println("No user logged in");
        }

        // Set initial active page
        highlightActivePage("home");
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
    private void navigateToProducts(ActionEvent event) {
        highlightActivePage("products");
        // Since products.fxml was deleted, show an info alert instead
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Products");
        alert.setHeaderText("Coming Soon");
        alert.setContentText("The Products page is currently under construction. Please check back later!");
        alert.showAndWait();
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

