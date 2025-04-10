package ca.humber.controller;

import java.net.URL;
import java.util.ResourceBundle;

import ca.humber.dao.RoleDAO;
import ca.humber.model.RolePermission;
import ca.humber.model.UserRole;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PermissionFormController implements Initializable {

	@FXML
	private ComboBox<String> tableNameComboBox;

	@FXML
	private CheckBox isReadOnlyCheckBox;

	@FXML
	private Button saveButton;

	@FXML
	private Button cancelButton;

	private int roleId;
	private String mode = "add";
	private RolePermission existingRolePermission;

	String[] tableNames = { "APPOINTMENT", "CUSTOMER", "INVENTORY", "MECHANIC", "SERVICE", "USER_ROLE", "USERS",
			"VEHICLE", "REPORT" };

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		ObservableList<String> tableList = FXCollections.observableArrayList(tableNames);
		tableNameComboBox.setItems(tableList);
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setRolePermission(RolePermission permission) {
		this.existingRolePermission = permission;
		tableNameComboBox.setValue(permission.getTableName());
		isReadOnlyCheckBox.setSelected(permission.getIsReadOnly() == 1);
	}

	@FXML
	private void handleSave() {
		if (!validateInput()) {
			return;
		}

		try {
			String tableName = tableNameComboBox.getValue().trim();
			int isReadOnly = isReadOnlyCheckBox.isSelected() ? 1 : 0;

			if ("add".equals(mode)) {
				RoleDAO.insertPermission(roleId, tableName.toUpperCase(), isReadOnly);
				AlertDialog.showSuccess("Success", "Permission added successfully");
			} else {
				existingRolePermission.setTableName(tableName.toUpperCase());

				RoleDAO.updatePermission(existingRolePermission.getPermissionId(), tableName.toUpperCase(), isReadOnly);
				AlertDialog.showSuccess("Success", "Permission updated successfully");
			}

			closeForm();
		} catch (Exception e) {
			AlertDialog.showWarning("Error", "An error occurred while saving the permission: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private boolean validateInput() {
		if (tableNameComboBox.getValue() == null) {
			AlertDialog.showWarning("Validation Error", "Please select a table name");
			tableNameComboBox.requestFocus();
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