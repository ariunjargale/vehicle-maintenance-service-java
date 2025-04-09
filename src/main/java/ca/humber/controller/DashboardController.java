
package ca.humber.controller;

import java.io.IOException;

import ca.humber.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.TabPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DashboardController {
	@FXML
	private TabPane mainTabPane;

	@FXML
	private Menu adminMenu;

	@FXML
	private void handleUserManagement() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/users.fxml"));
			Parent root = loader.load();

			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("User Management");
			stage.setScene(new Scene(root));
			stage.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void handleRoleManagement() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/roles.fxml"));
			Parent root = loader.load();

			Stage stage = new Stage();
			stage.setTitle("Roles");
			stage.setScene(new Scene(root));
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();
		}
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
