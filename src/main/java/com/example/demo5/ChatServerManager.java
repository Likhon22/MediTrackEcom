package com.example.demo5;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages the chat server that can handle multiple client connections
 * and opens a new window for each client.
 */
public class ChatServerManager {

    private static ChatServerManager instance;
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private final int SERVER_PORT = 5000;
    private ExecutorService executorService;
    private Map<String, Stage> activeChats = new HashMap<>();

    // Private constructor for singleton
    private ChatServerManager() {
        executorService = Executors.newCachedThreadPool();
    }

    // Singleton pattern
    public static synchronized ChatServerManager getInstance() {
        if (instance == null) {
            instance = new ChatServerManager();
        }
        return instance;
    }

    /**
     * Start the chat server to listen for client connections
     */
    public void startServer() {
        if (isRunning) {
            System.out.println("Server is already running");
            return;
        }

        executorService.submit(() -> {
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                isRunning = true;
                System.out.println("Chat server started on port " + SERVER_PORT);

                while (isRunning) {
                    try {
                        // Wait for a client to connect
                        Socket clientSocket = serverSocket.accept();
                        handleNewClient(clientSocket);
                    } catch (IOException e) {
                        if (isRunning) {
                            System.err.println("Error accepting client connection: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to start chat server: " + e.getMessage());
            }
        });
    }

    /**
     * Handle a new client connection by opening a new chat window
     */
    private void handleNewClient(Socket clientSocket) {
        Platform.runLater(() -> {
            try {
                // Create a new FXMLLoader for the admin chat window
                FXMLLoader loader = new FXMLLoader(getClass().getResource("admin_chat.fxml"));
                Parent root = loader.load();

                // Get the controller and initialize it with the client socket
                AdminChatController controller = loader.getController();
                controller.initializeWithClient(clientSocket);

                // Create a new stage for this chat
                Stage chatStage = new Stage();
                String title = "MediTrack - Admin Chat";
                chatStage.setTitle(title);
                chatStage.setScene(new Scene(root, 1000, 700));
                chatStage.setResizable(false);

                // Register the chat window with the username (will be updated when user identifies)
                String initialId = "Client-" + clientSocket.getInetAddress().getHostAddress();
                activeChats.put(initialId, chatStage);

                // Update the map when the username is identified
                controller.setOnUserIdentified(username -> {
                    // Remove the temporary entry and add with real username
                    activeChats.remove(initialId);
                    activeChats.put(username, chatStage);
                    chatStage.setTitle(title + " - " + username);
                });

                // Handle chat window close
                chatStage.setOnCloseRequest(e -> {
                    controller.shutdown();
                    // Remove from active chats
                    activeChats.entrySet().removeIf(entry -> entry.getValue() == chatStage);
                });

                // Show the chat window
                chatStage.show();

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to create chat window: " + e.getMessage());
                try {
                    clientSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * Stop the chat server and close all active chats
     */
    public void stopServer() {
        isRunning = false;

        // Close the server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Close all active chat windows
        Platform.runLater(() -> {
            for (Stage chatStage : activeChats.values()) {
                chatStage.close();
            }
            activeChats.clear();
        });

        // Shutdown the executor service
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    /**
     * Returns true if the server is currently running
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Get the number of active chat sessions
     */
    public int getActiveChatsCount() {
        return activeChats.size();
    }
}
