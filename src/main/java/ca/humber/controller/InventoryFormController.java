package ca.humber.controller;

import ca.humber.model.Inventory;
import ca.humber.service.InventoryService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class InventoryFormController {

    @FXML
    private TextField itemIdField;
    @FXML
    private TextField itemNameField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField priceField;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label labelItemId;

    private final InventoryService inventoryService = new InventoryService();
    private boolean isNew = true;
    private Inventory inventoryToEdit;

    @FXML
    private void initialize() {
        quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                quantityField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        priceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d{0,2})?")) {
                priceField.setText(oldVal); // revert
            }
        });
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
        itemIdField.setVisible(!isNew);
        itemIdField.setDisable(true);
        labelItemId.setVisible(!isNew);
    }

    public void setInventoryToEdit(Inventory inventory) {
        this.inventoryToEdit = inventory;
        if (inventory != null) {
            itemIdField.setText(String.valueOf(inventory.getItemId()));
            itemNameField.setText(inventory.getItemName());
            quantityField.setText(String.valueOf(inventory.getQuantity()));
            priceField.setText(String.valueOf(inventory.getPrice()));
            setIsNew(false);
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                String name = itemNameField.getText().trim();
                int quantity = Integer.parseInt(quantityField.getText().trim());
                BigDecimal price = new BigDecimal(priceField.getText().trim());

                if (isNew) {
                    Inventory newInventory = new Inventory();
                    newInventory.setItemName(name);
                    newInventory.setQuantity(quantity);
                    newInventory.setPrice(price);
                    inventoryService.createInventory(newInventory);
                } else {
                    inventoryToEdit.setItemName(name);
                    inventoryToEdit.setQuantity(quantity);
                    inventoryToEdit.setPrice(price);
                    inventoryService.updateInventory(inventoryToEdit);
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            AlertDialog.showSuccess("Success", isNew ? "Item added successfully" : "Item updated successfully");
            closeForm();
        });

        task.setOnFailed(e -> AlertDialog.showError("Error", task.getException().getMessage()));

        new Thread(task).start();
    }

    private boolean validateInput() {
        if (itemNameField.getText().trim().isEmpty()) {
            AlertDialog.showWarning("Validation", "Item name is required.");
            itemNameField.requestFocus();
            return false;
        }
        if (quantityField.getText().trim().isEmpty()) {
            AlertDialog.showWarning("Validation", "Quantity is required.");
            quantityField.requestFocus();
            return false;
        }
        if (priceField.getText().trim().isEmpty()) {
            AlertDialog.showWarning("Validation", "Price is required.");
            priceField.requestFocus();
            return false;
        }
        try {
            new BigDecimal(priceField.getText().trim());
        } catch (NumberFormatException e) {
            AlertDialog.showWarning("Validation", "Price must be a valid decimal.");
            priceField.requestFocus();
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
