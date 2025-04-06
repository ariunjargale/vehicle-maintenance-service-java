package ca.humber.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import ca.humber.dao.UsersDao;
import ca.humber.model.User;
import ca.humber.model.UserRole;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class UsersController implements Initializable {

	@FXML
	private TableView<User> userTable;

	@FXML
	private TableColumn<User, Integer> userIdColumn;

	@FXML
	private TableColumn<User, String> usernameColumn;

	@FXML
	private TableColumn<User, UserRole> roleColumn;

	@FXML
	private TextField searchField;

	private ObservableList<User> userList = FXCollections.observableArrayList();
	private ObservableList<UserRole> userRoles = FXCollections.observableArrayList();

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
		usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

		roleColumn.setCellValueFactory(cellData -> {
			int roleId = cellData.getValue().getRoleId();
			UserRole role = userRoles.stream().filter(r -> r.getRoleId() == roleId).findFirst().orElse(null);
			return new SimpleObjectProperty<>(role);
		});
		roleColumn.setCellFactory(_ -> new TableCell<User, UserRole>() {
			@Override
			protected void updateItem(UserRole role, boolean empty) {
				super.updateItem(role, empty);
				if (empty || role == null) {
					setText(null);
				} else {
					setText(role.getRoleName());
				}
			}
		});

		loadUsers();
		loadUserRoles();
	}

	private void loadUsers() {
		try {
			List<User> list = UsersDao.getUsers();
			userList.clear();
			userList.addAll(list);
			userTable.setItems(userList);
		} catch (Exception e) {
			AlertDialog.showWarning("Error", "An error occurred while loading user data: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void loadUserRoles() {
		try {
			List<UserRole> list = UsersDao.getUserRoles();
			userRoles.clear();
			userRoles.addAll(list);
		} catch (Exception e) {
			AlertDialog.showWarning("Error", "An error occurred while loading user data: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Search users
	@FXML
	private void handleSearch() {
		String searchTerm = searchField.getText().trim();
		if (searchTerm.isEmpty()) {
			loadUsers();
			return;
		}

		try {
			List<User> searchResults = UsersDao.getUserByUsername(searchTerm);
			userList.clear();
			userList.addAll(searchResults);
			userTable.setItems(userList);

			if (searchResults.isEmpty()) {
				AlertDialog.showSuccess("Search Results", "No users found matching the criteria");
			} else {
				AlertDialog.showSuccess("Search Results",
						"Found " + searchResults.size() + " users matching the criteria");
			}
		} catch (Exception e) {
			AlertDialog.showWarning("Error", "An error occurred while searching for users: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	private void handleAdd() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/user_form.fxml"));
			Parent root = loader.load();
			UserFormController controller = loader.getController();
			controller.setMode("add");

			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Add User");
			stage.setScene(new Scene(root));
			stage.showAndWait();

			// Reload data
			loadUsers();
		} catch (Exception e) {
			AlertDialog.showWarning("Error", "An error occurred while opening the add user form: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Edit
	@FXML
	private void handleEdit() {
		User selected = userTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			AlertDialog.showWarning("Warning", "Please select a user to edit first");
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/user_form.fxml"));
			Parent root = loader.load();
			UserFormController controller = loader.getController();
			controller.setMode("edit");
			controller.setUser(selected);

			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Edit User");
			stage.setScene(new Scene(root));
			stage.showAndWait();

			// Reload data
			loadUsers();
		} catch (Exception e) {
			AlertDialog.showWarning("Error", "An error occurred while opening the edit user form: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	private void handleChangePassword() {
		User selected = userTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			AlertDialog.showWarning("Warning", "Please select a user to edit first");
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/user_change_password_form.fxml"));
			Parent root = loader.load();
			UserChangePasswordController controller = loader.getController();
			controller.setUser(selected);

			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Change Password");
			stage.setScene(new Scene(root));
			stage.showAndWait();

			// Reload data
			loadUsers();
		} catch (Exception e) {
			AlertDialog.showWarning("Error",
					"An error occurred while opening the change password form: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Delete
	@FXML
	private void handleDelete() {
		User selected = userTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			AlertDialog.showWarning("Warning", "Please select a user to delete first");
			return;
		}

		boolean confirm = AlertDialog.showConfirm("Confirm Deletion",
				"Are you sure you want to delete the user " + selected.getUsername() + "?");

		if (confirm) {
			try {
				UsersDao.deactivateUser(selected.getUserId());
				AlertDialog.showSuccess("Success", "User successfully deleted (marked as inactive)");
				loadUsers();
			} catch (Exception e) {
				AlertDialog.showWarning("Error", "An error occurred while deleting the user: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	// Refresh user list
	@FXML
	private void handleRefresh() {
		loadUsers();
		AlertDialog.showSuccess("Refresh", "User list has been refreshed");
	}
}
