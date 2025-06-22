package com.example.demo5;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private VBox chatBox;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;

    @FXML
    private Label statusLabel;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Thread receiverThread;
    private boolean connected = false;
    private final String SERVER_ADDRESS = "localhost";
    private final int SERVER_PORT = 5000;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load navbar
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("navbar.fxml"));
            Parent navbar = loader.load();
            mainBorderPane.setTop(navbar);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize chat features
        connectToServer();
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                // Try to connect to the server
                socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                writer = new PrintWriter(socket.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Update UI to show connected status
                Platform.runLater(() -> {
                    statusLabel.setText("Connected to support");
                    statusLabel.setTextFill(Color.web("#27ae60"));

                    // Add system message that we're connected
                    addSystemMessage("Connected to support. You can start chatting now.");
                });

                connected = true;

                // Send username to identify user
                writer.println("USER:" + getData.username);

                // Start listening for incoming messages
                startMessageReceiver();

            } catch (IOException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Connection failed");
                    addSystemMessage("Could not connect to support. Please try again later.");
                });
                System.err.println("Failed to connect to chat server: " + e.getMessage());
            }
        }).start();
    }

    private void startMessageReceiver() {
        receiverThread = new Thread(() -> {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    final String finalMessage = message;
                    Platform.runLater(() -> {
                        // Process the message
                        if (finalMessage.startsWith("ADMIN:")) {
                            String adminMsg = finalMessage.substring(6); // Remove "ADMIN:" prefix
                            addAdminMessage(adminMsg);
                        } else {
                            // Handle other message types if needed
                            System.out.println("Unknown message format: " + finalMessage);
                        }
                    });
                }
            } catch (IOException e) {
                if (connected) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Disconnected");
                        statusLabel.setTextFill(Color.web("#e74c3c"));
                        addSystemMessage("Disconnected from support.");
                    });
                    connected = false;
                }
            }
        });
        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    @FXML
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty() || !connected) {
            return;
        }

        // Send message to server
        writer.println("MSG:" + message);

        // Add message to chat UI
        addUserMessage(message);

        // Clear input field
        messageField.clear();
    }

    private void addUserMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5, 0, 5, 0));

        VBox textBox = new VBox();
        textBox.setStyle("-fx-background-color: #633364; -fx-background-radius: 10 0 10 10;");
        textBox.setPadding(new Insets(10));
        textBox.setMaxWidth(500);

        Text messageText = new Text(message);
        messageText.setFill(Color.WHITE);
        messageText.setWrappingWidth(480);

        Text timeText = new Text(LocalDateTime.now().format(timeFormatter));
        timeText.setFill(Color.LIGHTGRAY);
        timeText.setFont(Font.font("System", FontWeight.NORMAL, 10));

        textBox.getChildren().addAll(messageText, timeText);
        messageBox.getChildren().add(textBox);

        chatBox.getChildren().add(messageBox);
    }

    private void addAdminMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 0, 5, 0));

        VBox textBox = new VBox();
        textBox.setStyle("-fx-background-color: #449b6d; -fx-background-radius: 0 10 10 10;");
        textBox.setPadding(new Insets(10));
        textBox.setMaxWidth(500);

        Text messageText = new Text(message);
        messageText.setFill(Color.WHITE);
        messageText.setWrappingWidth(480);

        Text timeText = new Text(LocalDateTime.now().format(timeFormatter));
        timeText.setFill(Color.LIGHTGRAY);
        timeText.setFont(Font.font("System", FontWeight.NORMAL, 10));

        textBox.getChildren().addAll(messageText, timeText);
        messageBox.getChildren().add(textBox);

        chatBox.getChildren().add(messageBox);
    }

    private void addSystemMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(5, 0, 5, 0));

        Label systemMessage = new Label(message);
        systemMessage.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");
        systemMessage.setPadding(new Insets(5, 10, 5, 10));
        systemMessage.setTextFill(Color.GRAY);

        messageBox.getChildren().add(systemMessage);
        chatBox.getChildren().add(messageBox);
    }

    public void closeConnection() {
        connected = false;
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (receiverThread != null) {
            receiverThread.interrupt();
        }
    }

    // Method to be called when the window is closed
    public void shutdown() {
        closeConnection();
    }
}
