package ca.humber.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.hibernate.Session;

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
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UserChangePasswordController implements Initializable {

	@FXML
	private TextField usernameField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private ComboBox<UserRole> roleComboBox;
	@FXML
	private Button saveButton;
	@FXML
	private Button cancelButton;

	private User user;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		usernameField.setDisable(true);
	}

	public void setUser(User user) {
		this.user = user;
		usernameField.setText(user.getUsername());
	}

	private boolean validateInput() {
		if (passwordField.getText().trim().isEmpty()) {
			AlertDialog.showWarning("Validation Error", "Please enter the password");
			passwordField.requestFocus();
			return false;
		}

		return true;
	}

	private void closeForm() {
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
	}

	@FXML
	private void handleSave() {
		if (!validateInput()) {
			return;
		}

		try {
			String password = passwordField.getText().trim();

			// Save to the database
			UsersDao.resetPassword(user.getUserId(), PasswordUtil.hashPassword(password));
			AlertDialog.showSuccess("Success", "Password changed successfully");

			// Close the form
			closeForm();
		} catch (Exception e) {
			AlertDialog.showWarning("Error", "An error occurred while changing password: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	private void handleCancel() {
		closeForm();
	}

}