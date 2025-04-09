package ca.humber.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import ca.humber.dao.RoleDAO;
import ca.humber.model.RolePermission;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PermissionsController implements Initializable {

	@FXML
	private TableView<RolePermission> permissionTable;

	@FXML
	private TableColumn<RolePermission, Integer> permissionIdColumn;

	@FXML
	private TableColumn<RolePermission, String> tableNameColumn;

	@FXML
	private TableColumn<RolePermission, Boolean> isReadOnlyColumn;

	@FXML
	private TextField searchField;

	private ObservableList<RolePermission> permissionList = FXCollections.observableArrayList();

	private int roleId;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		permissionIdColumn.setCellValueFactory(new PropertyValueFactory<>("permissionId"));
		tableNameColumn.setCellValueFactory(new PropertyValueFactory<>("tableName"));
		isReadOnlyColumn.setCellValueFactory(cellData -> {
			RolePermission rp = cellData.getValue();
			boolean isReadOnly = rp.getIsReadOnly() == 1;
			return new SimpleBooleanProperty(isReadOnly);
		});
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
		loadPermissions();
	}

	private void loadPermissions() {
		try {
			List<RolePermission> list = RoleDAO.getPermissionsByRole(roleId);
			permissionList.clear();
			permissionList.addAll(list);
			permissionTable.setItems(permissionList);
		} catch (Exception e) {
			AlertDialog.showWarning("Error", "An error occurred while loading permissions: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	private void handleSearch() {
		String searchTerm = searchField.getText().trim();
		if (searchTerm.isEmpty()) {
			loadPermissions();
			return;
		}

		try {
			List<RolePermission> searchResults = RoleDAO.getPermissionsByName(roleId, searchTerm);
			permissionList.clear();
			permissionList.addAll(searchResults);
			permissionTable.setItems(permissionList);

			if (searchResults.isEmpty()) {
				AlertDialog.showSuccess("Search Results", "No permissions found matching the criteria");
			} else {
				AlertDialog.showSuccess("Search Results",
						"Found " + searchResults.size() + " permissions matching the criteria");
			}
		} catch (Exception e) {
			AlertDialog.showWarning("Error", "An error occurred while searching for permissions: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	private void handleAdd() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/permission_form.fxml"));
			Parent root = loader.load();
			PermissionFormController controller = loader.getController();
			controller.setRoleId(roleId);
			controller.setMode("add");

			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Add Permission");
			stage.setScene(new Scene(root));
			stage.showAndWait();

			loadPermissions();
		} catch (Exception e) {
			AlertDialog.showWarning("Error", "An error occurred while opening the add role form: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	private void handleEdit() {
		RolePermission selected = permissionTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			AlertDialog.showWarning("Warning", "Please select a role to edit first");
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/permission_form.fxml"));
			Parent root = loader.load();
			PermissionFormController controller = loader.getController();
			controller.setMode("edit");
			controller.setRolePermission(selected);

			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Edit Permission - " + selected.getPermissionId());
			stage.setScene(new Scene(root));
			stage.showAndWait();

			loadPermissions();
		} catch (Exception e) {
			AlertDialog.showWarning("Error", "An error occurred while opening the edit role form: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	private void handleDelete() {
		RolePermission selected = permissionTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			AlertDialog.showWarning("Warning", "Please select a permission to delete first");
			return;
		}

		boolean confirm = AlertDialog.showConfirm("Confirm Deletion",
				"Are you sure you want to delete the permission " + selected.getTableName() + "?");

		if (confirm) {
			try {
				RoleDAO.deletePermission(selected.getPermissionId());
				AlertDialog.showSuccess("Success", "Permission successfully deleted");
				loadPermissions();
			} catch (Exception e) {
				AlertDialog.showWarning("Error", "An error occurred while deleting the permission: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@FXML
	private void handleRefresh() {
		loadPermissions();
		AlertDialog.showSuccess("Refresh", "Permission list has been refreshed");
	}
}
