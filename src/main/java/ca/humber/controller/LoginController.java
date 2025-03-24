package ca.humber.controller;

import ca.humber.App;
import ca.humber.service.AuthService;
import ca.humber.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            AlertDialog.showWarning("Login", "Please enter username and password.");
            return;
        }

        try {
            //TODO: Ari
//            authService.login(username, password);
            openDashboard();
        } catch (Exception e) {
            AlertDialog.showWarning("Login", e.getMessage());
        }
    }

    private void openDashboard() {
//        System.out.println("Login successful. Welcome: " + SessionManager.getCurrentUser().getUsername());

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
