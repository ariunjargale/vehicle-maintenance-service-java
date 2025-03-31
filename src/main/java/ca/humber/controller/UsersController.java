package ca.humber.controller;

import ca.humber.dao.UsersDao;
import ca.humber.exceptions.ConstraintException;
import ca.humber.model.Customer;
import ca.humber.model.UserRole;
import ca.humber.model.Users;
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

import javax.management.relation.Role;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UsersController implements Initializable {
    @FXML
    private TableView<Users> userTable;
    @FXML
    private TableColumn<Users, Integer> userIdColumn;
    @FXML
    private TableColumn<Users, String> usernameColumn;
    @FXML
    private TableColumn<Users, UserRole> roleColumn;
    @FXML
    private TextField searchField;

    private ObservableList<Users> userList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username")); // 添加make屬性綁定

        // Owner column requires special handling as it is an associated object
        roleColumn.setCellValueFactory(cellData -> {
            UserRole role = cellData.getValue().getUserRole();
            return new SimpleObjectProperty<>(role);
        });
        roleColumn.setCellFactory(column -> new TableCell<Users, UserRole>() {
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
    }

    private void loadUsers() {
        try {
            List<Users> list = UsersDao.getUserList();
            userList.clear();
            userList.addAll(list);
            userTable.setItems(userList);
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
            List<Users> searchResults = UsersDao.search(searchTerm);
            userList.clear();
            userList.addAll(searchResults);
            userTable.setItems(userList);

            if (searchResults.isEmpty()) {
                AlertDialog.showSuccess("Search Results", "No users found matching the criteria");
            } else {
                AlertDialog.showSuccess("Search Results", "Found " + searchResults.size() + " users matching the criteria");
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
        Users selected = userTable.getSelectionModel().getSelectedItem();
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

    // Delete
    @FXML
    private void handleDelete() {
        Users selected = userTable.getSelectionModel().getSelectedItem();
        if (selected== null) {
            AlertDialog.showWarning("Warning", "Please select a user to delete first");
            return;
        }

        boolean confirm = AlertDialog.showConfirm("Confirm Deletion",
                "Are you sure you want to delete the user " + selected.getUsername() + "?");

        if (confirm) {
            try {
                boolean success = UsersDao.deleteUser(selected.getUserId());
                if (success) {
                    AlertDialog.showSuccess("Success", "User successfully deleted (marked as inactive)");
                    loadUsers();  // Reload data
                } else {
                    AlertDialog.showWarning("Error", "Failed to delete user");
                }
            } catch (ConstraintException e) {
                AlertDialog.showWarning("Constraint Error", e.getMessage());
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
