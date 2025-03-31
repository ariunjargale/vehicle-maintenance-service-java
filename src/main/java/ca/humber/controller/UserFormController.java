package ca.humber.controller;

import ca.humber.dao.UsersDao;
import ca.humber.model.UserRole;
import ca.humber.model.Users;
import ca.humber.util.PasswordUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UserFormController implements Initializable {
    
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
    
    private String mode = "add"; // Default mode is "add"
    private Users existingUser; // Holds the user being edited
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load all role into the dropdown
        loadRoles();
    }
    
    // Load all role into the dropdown
    private void loadRoles() {
        try {
            List<UserRole> roles = UsersDao.getRoleList();
            roleComboBox.setItems(FXCollections.observableArrayList(roles));
            
            // Set the display to show role names instead of object references
            roleComboBox.setCellFactory(param -> new ListCell<UserRole>() {
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
        if (mode == "edit") {
            usernameField.setDisable(true);
        }
    }
    
    // Set the existing user to be edited
    public void setUser(Users user) {
        this.existingUser = user;
        
        // Populate the form fields
        usernameField.setText(user.getUsername());

        
        // Set the selected customer
        if (user.getUserRole() != null) {
            for (UserRole role : roleComboBox.getItems()) {
                if (role.getRoleId() == user.getUserRole().getRoleId()) {
                    roleComboBox.getSelectionModel().select(role);
                    break;
                }
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
                Users newUser = new Users();
                newUser.setUsername(username);
                newUser.setPassword(PasswordUtil.hashPassword(password));
                newUser.setUserRole(selectedRole);
                newUser.setIsActive(1);
                
                // Save to the database
                UsersDao.insertUser(newUser);
                AlertDialog.showSuccess("Success", "User added successfully");
            } else {
                // Update the existing user
                existingUser.setUsername(username);
                existingUser.setPassword(PasswordUtil.hashPassword(password));
                existingUser.setUserRole(selectedRole);
                
                // Save to the database
                boolean success = UsersDao.updateUser(existingUser);
                if (success) {
                    AlertDialog.showSuccess("Success", "User updated successfully");
                } else {
                    AlertDialog.showWarning("Error", "An error occurred while updating the user");
                    return;
                }
            }
            
            // Close the form
            closeForm();
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while saving the user: " + e.getMessage());
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

        if (passwordField.getText().trim().isEmpty()) {
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