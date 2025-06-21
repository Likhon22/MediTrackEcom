package com.example.demo5;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    @FXML
    private Button close;

    @FXML
    private Button loginbtn;

    @FXML
    private Button signupbtn;

    @FXML
    private Button createAccountBtn;

    @FXML
    private Button loginAccountBtn;

    @FXML
    private AnchorPane main_form;

    @FXML
    private AnchorPane login_form;

    @FXML
    private AnchorPane signup_form;

    @FXML
    private PasswordField password;

    @FXML
    private TextField username;

    @FXML
    private TextField signup_name;

    @FXML
    private TextField signup_username;

    @FXML
    private PasswordField signup_password;

    private PreparedStatement prepare;
    private Connection connect;
    private ResultSet result;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the forms
        login_form.setVisible(true);
        signup_form.setVisible(false);
    }

    @FXML
    public void switchForm(ActionEvent event) {
        if (event.getSource() == createAccountBtn) {
            login_form.setVisible(false);
            signup_form.setVisible(true);
        } else if (event.getSource() == loginAccountBtn) {
            login_form.setVisible(true);
            signup_form.setVisible(false);
        }
    }

    @FXML
    private void loginAdmin(ActionEvent event) {
        Alert alert;
        if (username.getText().isEmpty() || password.getText().isEmpty()) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("Please fill all blank fields");
            alert.showAndWait();
            return;
        }
        String sql = "SELECT * FROM admin WHERE username = ? AND password = ?";

        connect = database.connectDb();

        try {
            prepare = connect.prepareStatement(sql);
            prepare.setString(1, username.getText());
            prepare.setString(2, password.getText());

            result = prepare.executeQuery();

            if (result.next()) {
                getData.username = username.getText();
                // Store the is_admin value to determine user role
                getData.isAdmin = result.getBoolean("is_admin");

                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Message");
                alert.setHeaderText(null);
                alert.setContentText("Successfully Login");
                alert.showAndWait();

                loginbtn.getScene().getWindow().hide();

                // Decide where to navigate based on is_admin value
                if (getData.isAdmin) {
                    // Admin user - go to dashboard
                    Parent root = FXMLLoader.load(getClass().getResource("dashboard.fxml"));
                    Stage stage = new Stage();
                    stage.setResizable(false);
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                } else {
                    // Regular user - go to home page
                    Parent root = FXMLLoader.load(getClass().getResource("home.fxml"));
                    Stage stage = new Stage();
                    stage.setResizable(false);
                    Scene scene = new Scene(root);
                    stage.setTitle("MediTrack - Home");
                    stage.setScene(scene);
                    stage.show();
                }

            } else {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Wrong Username or Password!");
                alert.showAndWait();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void signupAdmin(ActionEvent event) {
        Alert alert;

        if (signup_name.getText().isEmpty() || signup_username.getText().isEmpty() || signup_password.getText().isEmpty()) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("Please fill all blank fields");
            alert.showAndWait();
            return;
        }

        connect = database.connectDb();

        try {
            // First check if username already exists
            String checkUsername = "SELECT * FROM admin WHERE username = ?";
            prepare = connect.prepareStatement(checkUsername);
            prepare.setString(1, signup_username.getText());
            result = prepare.executeQuery();

            if (result.next()) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Username " + signup_username.getText() + " already exists!");
                alert.showAndWait();
            } else {
                // Insert new admin using a thread to avoid blocking UI
                Thread signupThread = new Thread(() -> {
                    try {
                        // Insert new admin
                        String insertAdmin = "INSERT INTO admin (name, username, password) VALUES (?, ?, ?)";
                        prepare = connect.prepareStatement(insertAdmin);
                        prepare.setString(1, signup_name.getText());
                        prepare.setString(2, signup_username.getText());
                        prepare.setString(3, signup_password.getText());
                        prepare.executeUpdate();

                        javafx.application.Platform.runLater(() -> {
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Information Message");
                            successAlert.setHeaderText(null);
                            successAlert.setContentText("Successfully registered!");
                            successAlert.showAndWait();

                            // Clear fields
                            signup_name.setText("");
                            signup_username.setText("");
                            signup_password.setText("");

                            // Switch to login form
                            login_form.setVisible(true);
                            signup_form.setVisible(false);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        javafx.application.Platform.runLater(() -> {
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Error Message");
                            errorAlert.setHeaderText(null);
                            errorAlert.setContentText("Error: " + e.getMessage());
                            errorAlert.showAndWait();
                        });
                    }
                });

                signupThread.setDaemon(true);
                signupThread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void close(ActionEvent event) {
        System.exit(0);
    }
}

