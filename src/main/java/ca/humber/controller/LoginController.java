package ca.humber.controller;

import ca.humber.App;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if ("admin".equals(username) && "password".equals(password)) {
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
        } else {
            AlertDialog.showWarning("Login Failed", "Please check your username and password.");
        }
    }
}
