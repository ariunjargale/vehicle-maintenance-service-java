package ca.humber.controller;

import ca.humber.dao.ServiceDAO;
import ca.humber.model.Service;
import ca.humber.model.ServiceTypeWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class ServiceFormController {

    @FXML
    private TextField serviceNameField;

    @FXML
    private ComboBox<ServiceTypeWrapper> serviceTypeComboBox;

    @FXML
    private TextField priceField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private Service service;
    private String mode;
    private ServiceTabController parentController;

    // Default prices for each service type
    private Map<Integer, Double> defaultPrices = new HashMap<>();

    @FXML
    public void initialize() {
        // Initialize service type options
        serviceTypeComboBox.getItems().addAll(
                new ServiceTypeWrapper(1, "Regular Maintenance"),
                new ServiceTypeWrapper(2, "Engine Repair"),
                new ServiceTypeWrapper(3, "Transmission Repair"),
                new ServiceTypeWrapper(4, "Brake Service"),
                new ServiceTypeWrapper(5, "Electrical Repair"),
                new ServiceTypeWrapper(6, "Air Conditioning"),
                new ServiceTypeWrapper(7, "Suspension Work"),
                new ServiceTypeWrapper(8, "Wheel and Tire Service"),
                new ServiceTypeWrapper(9, "Diagnostic Service"),
                new ServiceTypeWrapper(10, "Exhaust System Repair")
        );

        // Default to first option
        serviceTypeComboBox.getSelectionModel().selectFirst();

        // Setup default prices
        defaultPrices.put(1, 59.99);
        defaultPrices.put(2, 199.99);
        defaultPrices.put(3, 149.99);
        defaultPrices.put(4, 129.99);
        defaultPrices.put(5, 89.99);
        defaultPrices.put(6, 99.99);
        defaultPrices.put(7, 119.99);
        defaultPrices.put(8, 79.99);
        defaultPrices.put(9, 69.99);
        defaultPrices.put(10, 159.99);

        // Add listener to update price when service type changes
        serviceTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Double defaultPrice = defaultPrices.get(newValue.getId());
                if (defaultPrice != null) {
                    priceField.setText(String.format("%.2f", defaultPrice));
                }
            }
        });

        // Set initial price based on default selection
        ServiceTypeWrapper initialType = serviceTypeComboBox.getSelectionModel().getSelectedItem();
        if (initialType != null) {
            Double initialPrice = defaultPrices.get(initialType.getId());
            if (initialPrice != null) {
                priceField.setText(String.format("%.2f", initialPrice));
            }
        }

        // Set up validation for price
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                priceField.setText(oldValue);
            }
        });
    }

    public void setMode(String mode) {
        this.mode = mode;
        if ("add".equals(mode)) {
            service = new Service();
        }
    }

    public void setService(Service service) {
        this.service = service;

        // Populate fields with service data
        serviceNameField.setText(service.getServiceName());
        priceField.setText(service.getPriceAsDouble() != null ?
                String.format("%.2f", service.getPriceAsDouble()) : "");

        // Set service type based on serviceTypeId if available
        if (service.getServiceTypeId() != null) {
            for (ServiceTypeWrapper item : serviceTypeComboBox.getItems()) {
                if (item.getId() == service.getServiceTypeId()) {
                    serviceTypeComboBox.getSelectionModel().select(item);
                    break;
                }
            }
        }
    }

    public void setParentController(ServiceTabController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            service.setServiceName(serviceNameField.getText().trim());

            ServiceTypeWrapper selectedType = serviceTypeComboBox.getValue();
            if (selectedType != null) {
                service.setServiceTypeId(selectedType.getId());
            }

            double priceValue = Double.parseDouble(priceField.getText().trim());
            service.setPriceFromDouble(priceValue);

            boolean success;
            if ("add".equals(mode)) {
                success = ServiceDAO.createService(service);
            } else {
                success = ServiceDAO.updateService(service);
            }

            if (success) {
                AlertDialog.showSuccess("Success", "Service saved successfully");
                closeForm();

                if (parentController != null) {
                    parentController.refreshServices();
                }
            } else {
                AlertDialog.showWarning("Error", "Failed to save service");
            }
        } catch (NumberFormatException e) {
            AlertDialog.showWarning("Error", "Please enter a valid price");
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "Failed to save service: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeForm();
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (serviceNameField.getText().trim().isEmpty()) {
            errors.append("- Service name is required\n");
        }

        if (serviceTypeComboBox.getValue() == null) {
            errors.append("- Service type is required\n");
        }

        if (priceField.getText().trim().isEmpty()) {
            errors.append("- Price is required\n");
        } else {
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                if (price < 0) {
                    errors.append("- Price cannot be negative\n");
                }
            } catch (NumberFormatException e) {
                errors.append("- Invalid price format. Please enter a number\n");
            }
        }

        if (errors.length() > 0) {
            AlertDialog.showWarning("Validation Error", "Please correct the following errors:\n" + errors.toString());
            return false;
        }

        return true;
    }

    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}