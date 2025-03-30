package ca.humber.controller;

import ca.humber.App;
import ca.humber.service.AuthService;
import ca.humber.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private final AuthService authService = new AuthService();
    private static final int LOGIN_TIMEOUT_SECONDS = 10;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            AlertDialog.showWarning("Login", "Please enter username and password.");
            return;
        }

        CompletableFuture<Void> loginTask = CompletableFuture.runAsync(() -> {
            try {
                authService.login(username, password);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Handle timeout
        try {
            loginTask.get(LOGIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            openDashboard();
        } catch (java.util.concurrent.TimeoutException e) {
            // Login timeout
            javafx.application.Platform.runLater(() -> {
                AlertDialog.showWarning("Login Timeout", "Server response took too long. Please try again later.");
            });
        } catch (Exception e) {
            // Other login errors
            javafx.application.Platform.runLater(() -> {
                AlertDialog.showWarning("Login", e.getMessage());
            });
        }
    }

    private void openDashboard() {
        System.out.println("Login successful. Welcome: " + SessionManager.getCurrentUser().getUsername());

        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            App.setRoot("/view/dashboard");
            stage.setWidth(700);
            stage.setHeight(600);
            stage.centerOnScreen();
            stage.setTitle("Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
