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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class AdminChatController implements Initializable {

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

    @FXML
    private Label userLabel;

    private Socket clientSocket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Thread receiverThread;
    private boolean connected = false;
    private String currentUser = "";

    // Callback for when username is identified
    private Consumer<String> onUserIdentified;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // This is now a passive controller that waits for a client connection
        // The connection will be provided by ChatServerManager
        statusLabel.setText("Waiting for connection...");
        statusLabel.setTextFill(Color.web("#f39c12"));
    }

    /**
     * Initialize this controller with a specific client socket
     */
    public void initializeWithClient(Socket socket) {
        this.clientSocket = socket;
        this.connected = true;

        try {
            // Set up communication streams
            writer = new PrintWriter(clientSocket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            Platform.runLater(() -> {
                statusLabel.setText("User connected");
                statusLabel.setTextFill(Color.web("#27ae60"));
                addSystemMessage("User connected. Waiting for identification...");
            });

            // Start receiving messages
            startMessageReceiver();
        } catch (IOException e) {
            Platform.runLater(() -> {
                statusLabel.setText("Connection error");
                statusLabel.setTextFill(Color.web("#e74c3c"));
                addSystemMessage("Error setting up connection: " + e.getMessage());
            });
            e.printStackTrace();
        }
    }

    /**
     * Set a callback for when the username is identified
     */
    public void setOnUserIdentified(Consumer<String> callback) {
        this.onUserIdentified = callback;
    }

    private void startMessageReceiver() {
        receiverThread = new Thread(() -> {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    final String finalMessage = message;
                    Platform.runLater(() -> processMessage(finalMessage));
                }
            } catch (IOException e) {
                if (connected) {
                    Platform.runLater(() -> {
                        statusLabel.setText("User disconnected");
                        statusLabel.setTextFill(Color.web("#e74c3c"));
                        userLabel.setText("No active user");
                        addSystemMessage("User " + currentUser + " disconnected.");
                        currentUser = "";
                    });
                    connected = false;
                }
            }
        });

        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    private void processMessage(String message) {
        if (message.startsWith("USER:")) {
            // Handle user identification
            currentUser = message.substring(5); // Remove "USER:" prefix
            userLabel.setText(currentUser);
            addSystemMessage("User " + currentUser + " connected.");

            // Notify using the callback if available
            if (onUserIdentified != null) {
                onUserIdentified.accept(currentUser);
            }
        } else if (message.startsWith("MSG:")) {
            // Handle regular message
            String userMsg = message.substring(4); // Remove "MSG:" prefix
            addUserMessage(userMsg);
        } else {
            // Unknown message format
            System.out.println("Unknown message format: " + message);
        }
    }

    @FXML
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty() || !connected) {
            return;
        }

        // Send message to client
        writer.println("ADMIN:" + message);

        // Add message to chat UI
        addAdminMessage(message);

        // Clear input field
        messageField.clear();
    }

    private void addUserMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 0, 5, 0));

        VBox textBox = new VBox();
        textBox.setStyle("-fx-background-color: #3498db; -fx-background-radius: 0 10 10 10;");
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
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5, 0, 5, 0));

        VBox textBox = new VBox();
        textBox.setStyle("-fx-background-color: #449b6d; -fx-background-radius: 10 0 10 10;");
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

    // Method to be called when the window is closed
    public void shutdown() {
        connected = false;

        if (clientSocket != null && !clientSocket.isClosed()) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (receiverThread != null) {
            receiverThread.interrupt();
        }
    }
}
