
package ca.humber.controller;

import ca.humber.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController {
    @FXML
    private TabPane mainTabPane;

    @FXML
    private Menu adminMenu;


    @FXML
    private void handleUserManagement() {
        System.out.println("Navigating to user management...");
        // loadFXML("user_management.fxml");
    }

    @FXML
    private void handleSystemSettings() {
        System.out.println("System settings clicked");
    }

    @FXML
    private void handleProfileInfo() {
        AlertDialog.showSuccess("Profile Info", "Logged in as: " + SessionManager.getCurrentUser().getUsername());
    }

    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) mainTabPane.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.setTitle("Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
