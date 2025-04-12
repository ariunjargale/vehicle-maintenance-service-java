package ca.humber.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import ca.humber.dao.UsersDao;
import ca.humber.model.User;
import ca.humber.model.UserRole;
import ca.humber.util.HibernateUtil;
import ca.humber.util.PasswordUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UserFormController implements Initializable {

	@FXML
	private TextField usernameField;

	@FXML
	private Label passwordLabel;

	@FXML
	private PasswordField passwordField;

	@FXML
	private ComboBox<UserRole> roleComboBox;

	@FXML
	private Button saveButton;

	@FXML
	private Button cancelButton;

	private String mode = "add"; // Default mode is "add"
	private User existingUser; // Holds the user being edited

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		// Load all role into the dropdown
		loadRoles();
	}

	// Load all role into the dropdown
	private void loadRoles() {
		try {
			List<UserRole> roles = UsersDao.getUserRoles();
			roleComboBox.setItems(FXCollections.observableArrayList(roles));

			// Set the display to show role names instead of object references
			roleComboBox.setCellFactory(_ -> new ListCell<UserRole>() {
				@Override
				protected void updateItem(UserRole item, boolean empty) {
					super.updateItem(item, empty);
					if (empty || item == null) {
						setText(null);
					} else {
						setText(item.getRoleName());
					}
				}
			});

			roleComboBox.setButtonCell(new ListCell<UserRole>() {
				@Override
				protected void updateItem(UserRole item, boolean empty) {
					super.updateItem(item, empty);
					if (empty || item == null) {
						setText(null);
					} else {
						setText(item.getRoleName());
					}
				}
			});
		} catch (Exception e) {
			AlertDialog.showWarning("Error", "An error occurred while loading role data: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Set the form mode (add or edit)
	public void setMode(String mode) {
		this.mode = mode;
		if (mode.equals("edit")) {
			passwordLabel.setVisible(false);
			passwordField.setVisible(false);
			
			passwordLabel.setManaged(false);
			passwordField.setManaged(false);
			
		}
	}

	// Set the existing user to be edited
	public void setUser(User user) {
		this.existingUser = user;

		// Populate the form fields
		usernameField.setText(user.getUsername());

		// Set the selected role
		for (UserRole role : roleComboBox.getItems()) {
			if (role.getRoleId() == user.getRoleId()) {
				roleComboBox.getSelectionModel().select(role);
				break;
			}
		}

	}

	// Handle save button click
	@FXML
	private void handleSave() {
		if (!validateInput()) {
			return;
		}

		try {
			// Get form data
			String username = usernameField.getText().trim();
			String password = passwordField.getText().trim();

			UserRole selectedRole = roleComboBox.getValue();

			if ("add".equals(mode)) {
				// Create a new user
				User newUser = new User();
				newUser.setUsername(username);
				newUser.setPassword(PasswordUtil.hashPassword(password));
				newUser.setRoleId(selectedRole.getRoleId());
				newUser.setIsActive(1);

				// Save to the database
				UsersDao.insertUser(newUser);
				AlertDialog.showSuccess("Success", "User added successfully");
			} else {
				// Update the existing user
				existingUser.setUsername(username);
				existingUser.setRoleId(selectedRole.getRoleId());

				// Save to the database
				UsersDao.updateUser(existingUser);
				AlertDialog.showSuccess("Success", "User updated successfully");
			}

			// Close the form
			closeForm();
		} catch (Exception e) {
			String error = HibernateUtil.message(e);
			AlertDialog.showWarning("Error", error);
			e.printStackTrace();
		}
	}

	// Validate form input
	private boolean validateInput() {
		// Check required fields
		if (usernameField.getText().trim().isEmpty()) {
			AlertDialog.showWarning("Validation Error", "Please enter the username");
			usernameField.requestFocus();
			return false;
		}

		if (mode.equals("add") && passwordField.getText().trim().isEmpty()) {
			AlertDialog.showWarning("Validation Error", "Please enter the password");
			passwordField.requestFocus();
			return false;
		}

		if (roleComboBox.getValue() == null) {
			AlertDialog.showWarning("Validation Error", "Please select a role");
			roleComboBox.requestFocus();
			return false;
		}

		return true;
	}

	// Handle cancel button click
	@FXML
	private void handleCancel() {
		closeForm();
	}

	private void closeForm() {
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
	}
}