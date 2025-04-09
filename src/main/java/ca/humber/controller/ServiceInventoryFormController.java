package ca.humber.controller;

import ca.humber.dao.InventoryDAO;
import ca.humber.dao.ServiceDAO;
import ca.humber.dao.ServiceInventoryDAO;
import ca.humber.model.Inventory;
import ca.humber.model.Service;
import ca.humber.model.ServiceInventory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ServiceInventoryFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private ComboBox<Service> serviceIdComboBox;
    @FXML private ComboBox<Inventory> itemIdComboBox;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;

    private ServiceInventoryTabController parentController;
    private String mode = "add";
    private ServiceInventory currentServiceInventory;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));

        loadServices();
        loadInventoryItems();

        serviceIdComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Service s) {
                return (s == null) ? "" : s.getServiceId() + " - " + s.getServiceName();
            }

            @Override public Service fromString(String s) {
                return null;
            }
        });

        itemIdComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Inventory i) {
                return (i == null) ? "" : i.getItemId() + " - " + i.getItemName();
            }

            @Override public Inventory fromString(String s) {
                return null;
            }
        });
    }

    public void setServiceInventoryController(ServiceInventoryTabController controller) {
        this.parentController = controller;
    }

    public void setMode(String mode) {
        this.mode = mode;
        titleLabel.setText(mode.equals("add") ? "Add Service Inventory Item" : "Edit Service Inventory Item");
    }

    public void setServiceInventory(ServiceInventory serviceInventory) {
        this.currentServiceInventory = serviceInventory;

        for (Service service : serviceIdComboBox.getItems()) {
            if (service.getServiceId() == serviceInventory.getServiceId()) {
                serviceIdComboBox.setValue(service);
                break;
            }
        }

        for (Inventory item : itemIdComboBox.getItems()) {
            // Fixed comparison to match types correctly
            if (item.getItemId().intValue() == serviceInventory.getItemId()) {
                itemIdComboBox.setValue(item);
                break;
            }
        }

        quantitySpinner.getValueFactory().setValue(serviceInventory.getQuantityRequired());
        serviceIdComboBox.setDisable(true);
        itemIdComboBox.setDisable(true);
    }

    private void loadServices() {
        try {
            List<Service> services = ServiceDAO.getAllServices();
            serviceIdComboBox.setItems(FXCollections.observableArrayList(services));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load services", e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadInventoryItems() {
        try {
            List<Inventory> items = InventoryDAO.getAllInventory();
            itemIdComboBox.setItems(FXCollections.observableArrayList(items));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load inventory items", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleConfirm() {
        if (!validateInput()) return;

        try {
            Service service = serviceIdComboBox.getValue();
            Inventory item = itemIdComboBox.getValue();
            int quantity = quantitySpinner.getValue();

            ServiceInventory si = new ServiceInventory(
                    service.getServiceId(),
                    item.getItemId().intValue(),
                    quantity
            );

            boolean success;
            if (mode.equals("add")) {
                success = ServiceInventoryDAO.save(si);
            } else {
                success = ServiceInventoryDAO.update(si);
            }

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Item Saved",
                        "Successfully " + (mode.equals("add") ? "added." : "updated."));
                if (parentController != null) {
                    parentController.refreshServiceInventory();
                }
                closeForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Operation Failed",
                        "Item could not be saved.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Operation Failed", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeForm();
    }

    private boolean validateInput() {
        StringBuilder error = new StringBuilder();
        if (serviceIdComboBox.getValue() == null) error.append("Select a service.\n");
        if (itemIdComboBox.getValue() == null) error.append("Select an inventory item.\n");

        if (error.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fix:", error.toString());
            return false;
        }

        return true;
    }

    private void closeForm() {
        ((Stage) confirmButton.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}