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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

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

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Thread serverThread;
    private Thread receiverThread;
    private boolean serverRunning = false;
    private boolean connected = false;
    private String currentUser = "";
    private final int SERVER_PORT = 5000;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // No need to load navbar for admin chat
        // Admin chat will be launched directly from dashboard

        // Initialize chat server
        startServer();
    }

    private void startServer() {
        serverThread = new Thread(() -> {
            try {
                // Start the server socket
                serverSocket = new ServerSocket(SERVER_PORT);
                serverRunning = true;

                Platform.runLater(() -> {
                    statusLabel.setText("Server running, waiting for user");
                    statusLabel.setTextFill(Color.web("#f39c12"));
                    addSystemMessage("Chat support server started. Waiting for users...");
                });

                // Wait for a client to connect
                while (serverRunning) {
                    try {
                        clientSocket = serverSocket.accept();

                        // Set up communication streams
                        writer = new PrintWriter(clientSocket.getOutputStream(), true);
                        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                        connected = true;

                        Platform.runLater(() -> {
                            statusLabel.setText("User connected");
                            statusLabel.setTextFill(Color.web("#27ae60"));
                        });

                        // Start receiving messages
                        startMessageReceiver();

                        // Only handle one client at a time for simplicity
                        break;

                    } catch (IOException e) {
                        if (serverRunning) {
                            System.err.println("Error accepting client connection: " + e.getMessage());
                        }
                    }
                }

            } catch (IOException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Server failed to start");
                    statusLabel.setTextFill(Color.web("#e74c3c"));
                    addSystemMessage("Failed to start chat server: " + e.getMessage());
                });
                System.err.println("Failed to start chat server: " + e.getMessage());
            }
        });

        serverThread.setDaemon(true);
        serverThread.start();
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

                    // Try to restart the server to accept new connections
                    startServer();
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

    public void stopServer() {
        serverRunning = false;
        connected = false;

        if (clientSocket != null && !clientSocket.isClosed()) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (serverThread != null) {
            serverThread.interrupt();
        }

        if (receiverThread != null) {
            receiverThread.interrupt();
        }
    }

    // Method to be called when the window is closed
    public void shutdown() {
        stopServer();
    }
}
