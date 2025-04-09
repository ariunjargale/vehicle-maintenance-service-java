package ca.humber.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import ca.humber.dao.RoleDAO;
import ca.humber.model.UserRole;
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

public class RolesController implements Initializable {

	@FXML
	private TableView<UserRole> roleTable;

	@FXML
	private TableColumn<UserRole, Integer> roleIdColumn;

	@FXML
	private TableColumn<UserRole, String> roleNameColumn;

	@FXML
	private TextField searchField;

	private ObservableList<UserRole> roleList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		roleIdColumn.setCellValueFactory(new PropertyValueFactory<>("roleId"));
		roleNameColumn.setCellValueFactory(new PropertyValueFactory<>("roleName"));

		loadRoles();
	}

	private void loadRoles() {
		try {
			List<UserRole> list = RoleDAO.getRoles();
			roleList.clear();
			roleList.addAll(list);
			roleTable.setItems(roleList);
		} catch (Exception e) {
			AlertDialog.showWarning("Error", "An error occurred while loading roles: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	private void handleSearch() {
		String searchTerm = searchField.getText().trim();
		if (searchTerm.isEmpty()) {
			loadRoles();
			return;
		}

		try {
			List<UserRole> searchResults = RoleDAO.getRolesByName(searchTerm);
			roleList.clear();
			roleList.addAll(searchResults);
			roleTable.setItems(roleList);

			if (searchResults.isEmpty()) {
				AlertDialog.showSuccess("Search Results", "No roles found matching the criteria");
			} else {
				AlertDialog.showSuccess("Search Results",
						"Found " + searchResults.size() + " roles matching the criteria");
			}
		} catch (Exception e) {
			AlertDialog.showWarning("Error", "An error occurred while searching for roles: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	private void handleAdd() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/role_form.fxml"));
			Parent root = loader.load();
			RoleFormController controller = loader.getController();
			controller.setMode("add");

			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Add Role");
			stage.setScene(new Scene(root));
			stage.showAndWait();

			loadRoles();
		} catch (Exception e) {
			AlertDialog.showWarning("Error", "An error occurred while opening the add role form: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	private void handleEdit() {
		UserRole selected = roleTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			AlertDialog.showWarning("Warning", "Please select a role to edit first");
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/role_form.fxml"));
			Parent root = loader.load();
			RoleFormController controller = loader.getController();
			controller.setMode("edit");
			controller.setRole(selected);

			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Edit Role");
			stage.setScene(new Scene(root));
			stage.showAndWait();

			loadRoles();
		} catch (Exception e) {
			AlertDialog.showWarning("Error", "An error occurred while opening the edit role form: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	private void handleDetail() {
		UserRole selected = roleTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			AlertDialog.showWarning("Warning", "Please select a role to edit first");
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/permissions.fxml"));
			Parent root = loader.load();
			PermissionsController controller = loader.getController();
			controller.setRoleId(selected.getRoleId());

			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Permissions for " + selected.getRoleName());
			stage.setScene(new Scene(root));
			stage.showAndWait();

			loadRoles();
		} catch (Exception e) {
			AlertDialog.showWarning("Error", "An error occurred while opening the permissions: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	private void handleDelete() {
		UserRole selected = roleTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			AlertDialog.showWarning("Warning", "Please select a role to delete first");
			return;
		}

		boolean confirm = AlertDialog.showConfirm("Confirm Deletion",
				"Are you sure you want to delete the role " + selected.getRoleName() + "?");

		if (confirm) {
			try {
				RoleDAO.deleteRole(selected.getRoleId());
				AlertDialog.showSuccess("Success", "Role successfully deleted");
				loadRoles();
			} catch (Exception e) {
				AlertDialog.showWarning("Error", "An error occurred while deleting the role: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@FXML
	private void handleRefresh() {
		loadRoles();
		AlertDialog.showSuccess("Refresh", "Role list has been refreshed");
	}
}
