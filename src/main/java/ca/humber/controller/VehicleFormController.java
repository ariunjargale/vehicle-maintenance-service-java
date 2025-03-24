package ca.humber.controller;

import ca.humber.dao.CustomerDAO;
import ca.humber.dao.VehicleDAO;
import ca.humber.model.Customer;
import ca.humber.model.Vehicle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class VehicleFormController implements Initializable {
    
    @FXML
    private TextField makeField;
    @FXML
    private TextField modelField;
    @FXML
    private TextField yearField;
    @FXML
    private TextField vinField;
    @FXML
    private TextField licensePlateField;
    @FXML
    private ComboBox<Customer> customerComboBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    
    private String mode = "add"; // Default mode is "add"
    private Vehicle existingVehicle; // Holds the vehicle being edited
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load all customers into the dropdown
        loadCustomers();
        
        // Add numeric validation for the year field
        yearField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                yearField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }
    
    // Load all customers into the dropdown
    private void loadCustomers() {
        try {
            List<Customer> customers = CustomerDAO.getActiveCustomers();
            customerComboBox.setItems(FXCollections.observableArrayList(customers));
            
            // Set the display to show customer names instead of object references
            customerComboBox.setCellFactory(param -> new ListCell<Customer>() {
                @Override
                protected void updateItem(Customer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName());
                    }
                }
            });
            
            customerComboBox.setButtonCell(new ListCell<Customer>() {
                @Override
                protected void updateItem(Customer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName());
                    }
                }
            });
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while loading customer data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Set the form mode (add or edit)
    public void setMode(String mode) {
        this.mode = mode;
    }
    
    // Set the existing vehicle to be edited
    public void setVehicle(Vehicle vehicle) {
        this.existingVehicle = vehicle;
        
        // Populate the form fields
        makeField.setText(vehicle.getMake());
        modelField.setText(vehicle.getModel());
        yearField.setText(String.valueOf(vehicle.getYear()));
        vinField.setText(vehicle.getVin());
        licensePlateField.setText(vehicle.getLicensePlate());
        
        // Set the selected customer
        if (vehicle.getCustomer() != null) {
            for (Customer customer : customerComboBox.getItems()) {
                if (customer.getCustomerId() == vehicle.getCustomer().getCustomerId()) {
                    customerComboBox.getSelectionModel().select(customer);
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
            String make = makeField.getText().trim();
            String model = modelField.getText().trim();
            int year = Integer.parseInt(yearField.getText().trim());
            String vin = vinField.getText().trim();
            String licensePlate = licensePlateField.getText().trim();
            Customer selectedCustomer = customerComboBox.getValue();
            
            if ("add".equals(mode)) {
                // Create a new vehicle
                Vehicle newVehicle = new Vehicle();
                newVehicle.setMake(make);
                newVehicle.setModel(model);
                newVehicle.setYear(year);
                newVehicle.setVin(vin);
                newVehicle.setLicensePlate(licensePlate);
                newVehicle.setCustomer(selectedCustomer);
                newVehicle.setIsActive(true);
                
                // Save to the database
                VehicleDAO.insertVehicle(newVehicle);
                AlertDialog.showSuccess("Success", "Vehicle added successfully");
            } else {
                // Update the existing vehicle
                existingVehicle.setMake(make);
                existingVehicle.setModel(model);
                existingVehicle.setYear(year);
                existingVehicle.setVin(vin);
                existingVehicle.setLicensePlate(licensePlate);
                existingVehicle.setCustomer(selectedCustomer);
                
                // Save to the database
                boolean success = VehicleDAO.updateVehicle(existingVehicle);
                if (success) {
                    AlertDialog.showSuccess("Success", "Vehicle updated successfully");
                } else {
                    AlertDialog.showWarning("Error", "An error occurred while updating the vehicle");
                    return;
                }
            }
            
            // Close the form
            closeForm();
        } catch (NumberFormatException e) {
            AlertDialog.showWarning("Input Error", "Year must be a valid number");
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while saving the vehicle: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Validate form input
    private boolean validateInput() {
        // Check required fields
        if (makeField.getText().trim().isEmpty()) {
            AlertDialog.showWarning("Validation Error", "Please enter the make");
            makeField.requestFocus();
            return false;
        }
        
        if (modelField.getText().trim().isEmpty()) {
            AlertDialog.showWarning("Validation Error", "Please enter the model");
            modelField.requestFocus();
            return false;
        }
        
        if (yearField.getText().trim().isEmpty()) {
            AlertDialog.showWarning("Validation Error", "Please enter the year");
            yearField.requestFocus();
            return false;
        }
        
        try {
            int year = Integer.parseInt(yearField.getText().trim());
            int currentYear = java.time.Year.now().getValue();
            if (year < 1900 || year > currentYear + 1) { // Allow next year's models
                AlertDialog.showWarning("Validation Error", "Please enter a valid year (1900-" + (currentYear + 1) + ")");
                yearField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            AlertDialog.showWarning("Validation Error", "Year must be a valid number");
            yearField.requestFocus();
            return false;
        }
        
        if (vinField.getText().trim().isEmpty()) {
            AlertDialog.showWarning("Validation Error", "Please enter the VIN");
            vinField.requestFocus();
            return false;
        }
        
        if (customerComboBox.getValue() == null) {
            AlertDialog.showWarning("Validation Error", "Please select a customer");
            customerComboBox.requestFocus();
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