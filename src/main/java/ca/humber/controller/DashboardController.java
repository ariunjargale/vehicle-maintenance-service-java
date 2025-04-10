
package ca.humber.controller;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ca.humber.App;
import ca.humber.model.RolePermission;
import ca.humber.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DashboardController {

	@FXML
	private TabPane mainTabPane;

	@FXML
	private MenuItem USERS;

	@FXML
	private MenuItem USER_ROLE;

	@FXML
	private Tab APPOINTMENT;
	@FXML
	private Tab CUSTOMER;
	@FXML
	private Tab VEHICLE;
	@FXML
	private Tab INVENTORY;
	@FXML
	private Tab MECHANIC;
	@FXML
	private Tab SERVICE;
	@FXML
	private Tab SERVICE_INVENTORY;
	@FXML
	private Tab REPORT;

	@FXML
	public void initialize() {
		List<RolePermission> permissions = SessionManager.getRolePermissions();

		Set<String> allowedTables = permissions.stream().map(p -> p.getTableName().toUpperCase())
				.collect(Collectors.toSet());

		if (!allowedTables.contains("USERS")) {
			USERS.setVisible(false);
		}

		if (!allowedTables.contains("USER_ROLE")) {
			USER_ROLE.setVisible(false);
		}

		hideIfNotAllowed(APPOINTMENT, allowedTables);
		hideIfNotAllowed(CUSTOMER, allowedTables);
		hideIfNotAllowed(VEHICLE, allowedTables);
		hideIfNotAllowed(INVENTORY, allowedTables);
		hideIfNotAllowed(MECHANIC, allowedTables);
		hideIfNotAllowed(SERVICE, allowedTables);
		hideIfNotAllowed(SERVICE_INVENTORY, allowedTables);
		hideIfNotAllowed(REPORT, allowedTables);
	}

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
			SessionManager.logout();

			Stage stage = (Stage) mainTabPane.getScene().getWindow();
			App.setRoot("/view/login");
			stage.setResizable(false);
			stage.centerOnScreen();
			stage.setTitle("Login");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void hideIfNotAllowed(Tab tab, Set<String> allowedTables) {
		String tableName = tab.getId();
		if (tableName.equals("SERVICE_INVENTORY")) {
			tableName = "SERVICE";
		}
		if (tableName != null && !allowedTables.contains(tableName.toUpperCase())) {
			mainTabPane.getTabs().remove(tab);
		}
	}
}
