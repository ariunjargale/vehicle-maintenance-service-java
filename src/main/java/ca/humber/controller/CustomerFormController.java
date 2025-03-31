package ca.humber.controller;

import ca.humber.dao.CustomerDAO;
import ca.humber.model.Customer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class CustomerFormController implements Initializable {

    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private String mode = "add"; // Default mode is "add"
    private Customer existingCustomer; // Holds the customer being edited
    private CustomersTabController parentController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Allow only numbers in the phone field
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                phoneField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    // Set the form mode (add or edit)
    public void setMode(String mode) {
        this.mode = mode;
        if ("edit".equals(mode)) {
            saveButton.setText("Update");
        }
    }
    
    // Set the customer to be edited
    public void setCustomer(Customer customer) {
        this.existingCustomer = customer;
        
        // Populate form fields
        nameField.setText(customer.getName());
        phoneField.setText(customer.getPhone());
        emailField.setText(customer.getEmail());
    }
    
    // Set the parent controller
    public void setParentController(CustomersTabController controller) {
        this.parentController = controller;
    }
    
    // Handle save button click
    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }
        
        try {
            // Get form data
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            
            if ("add".equals(mode)) {
                // Create a new customer
                Customer newCustomer = new Customer(name, phone, email);
                
                // Save to database
                CustomerDAO.insertCustomer(newCustomer);
                AlertDialog.showSuccess("Success", "Customer added successfully");
            } else {
                // Update existing customer
                existingCustomer.setName(name);
                existingCustomer.setPhone(phone);
                existingCustomer.setEmail(email);
                
                // Save to database
                boolean success = CustomerDAO.updateCustomer(existingCustomer);
                if (success) {
                    AlertDialog.showSuccess("Success", "Customer updated successfully");
                } else {
                    AlertDialog.showWarning("Error", "Error occurred while updating customer");
                    return;
                }
            }
            
            // Close form and refresh list
            if (parentController != null) {
                parentController.refreshCustomers();
            }
            closeForm();
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "Error occurred while saving customer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Validate input
    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            AlertDialog.showWarning("Validation Error", "Please enter the customer's name");
            nameField.requestFocus();
            return false;
        }
        
        if (phoneField.getText().trim().isEmpty()) {
            AlertDialog.showWarning("Validation Error", "Please enter the phone number");
            phoneField.requestFocus();
            return false;
        }
        
        // Validate email format
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            AlertDialog.showWarning("Validation Error", "Please enter a valid email address");
            emailField.requestFocus();
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
