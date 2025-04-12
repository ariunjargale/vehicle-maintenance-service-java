package ca.humber.controller;

import java.net.URL;
import java.util.ResourceBundle;

import ca.humber.dao.RoleDAO;
import ca.humber.model.UserRole;
import ca.humber.util.HibernateUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RoleFormController implements Initializable {

	@FXML
	private TextField roleNameField;

	@FXML
	private Button saveButton;

	@FXML
	private Button cancelButton;

	private String mode = "add";
	private UserRole existingRole;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setRole(UserRole role) {
		this.existingRole = role;
		roleNameField.setText(role.getRoleName());
	}

	@FXML
	private void handleSave() {
		if (!validateInput()) {
			return;
		}

		try {
			String roleName = roleNameField.getText().trim();

			if ("add".equals(mode)) {
				RoleDAO.insertRole(roleName.toUpperCase());
				AlertDialog.showSuccess("Success", "Role added successfully");
			} else {
				existingRole.setRoleName(roleName.toUpperCase());

				RoleDAO.updateRole(existingRole.getRoleId(), existingRole.getRoleName());
				AlertDialog.showSuccess("Success", "Role updated successfully");
			}

			closeForm();
		} catch (Exception e) {
			String error = HibernateUtil.message(e);
			AlertDialog.showWarning("Error", error);
			e.printStackTrace();
		}
	}

	private boolean validateInput() {
		if (roleNameField.getText().trim().isEmpty()) {
			AlertDialog.showWarning("Validation Error", "Please enter the role name");
			roleNameField.requestFocus();
			return false;
		}

		return true;
	}

	@FXML
	private void handleCancel() {
		closeForm();
	}

	private void closeForm() {
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
	}
}