package ca.humber.controller;

import ca.humber.model.Inventory;
import ca.humber.service.InventoryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

public class InventoryTabController {

    @FXML
    private TableView<Inventory> inventoryTable;
    @FXML
    private TableColumn<Inventory, Long> itemIdColumn;
    @FXML
    private TableColumn<Inventory, String> itemNameColumn;
    @FXML
    private TableColumn<Inventory, Integer> quantityColumn;
    @FXML
    private TableColumn<Inventory, BigDecimal> unitPriceColumn;
    @FXML
    private TextField searchField;

    private final ObservableList<Inventory> inventoryList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        itemIdColumn.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        inventoryTable.setItems(inventoryList);
        refreshInventoryTable();
    }

    private void runInventoryTask(Supplier<List<Inventory>> supplier, String errorTitle) {
        Task<List<Inventory>> task = new Task<>() {
            @Override
            protected List<Inventory> call() {
                return supplier.get();
            }
        };
        task.setOnSucceeded(event -> inventoryList.setAll(task.getValue()));
        task.setOnFailed(event -> AlertDialog.showError(errorTitle, task.getException().getMessage()));
        new Thread(task).start();
    }

    private void refreshInventoryTable() {
        runInventoryTask(() -> InventoryService.getAllInventory(), "Error Loading Inventory");
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            refreshInventoryTable();
            return;
        }
        runInventoryTask(() -> InventoryService.searchInventory(keyword), "Search Error");
    }

    @FXML
    private void handleAddItem() {
        openInventoryForm(true, null);
    }

    @FXML
    private void handleEditItem() {
        Inventory selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertDialog.showWarning("Select Item", "Please select an item to edit.");
            return;
        }
        openInventoryForm(false, selected);
    }

    private void openInventoryForm(boolean isNew, Inventory inventoryToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/inventory_form.fxml"));
            Parent root = loader.load();

            InventoryFormController controller = loader.getController();
            controller.setIsNew(isNew);
            if (!isNew && inventoryToEdit != null) {
                controller.setInventoryToEdit(inventoryToEdit);
            }

            Stage stage = new Stage();
            stage.setTitle(isNew ? "Add Inventory" : "Edit Inventory");
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> refreshInventoryTable());
            stage.show();

        } catch (IOException e) {
            AlertDialog.showError("Error", "Failed to open inventory form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteItem() {
        Inventory selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertDialog.showWarning("Select Item", "Please select an item to delete.");
            return;
        }
        boolean confirmed = AlertDialog.showConfirmation("Confirm Delete", "Are you sure you want to delete this item?");
        if (!confirmed) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                InventoryService.deleteInventory(selected.getItemId());
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            AlertDialog.showSuccess("Deleted", "Item deleted successfully.");
            refreshInventoryTable();
        });
        task.setOnFailed(event -> AlertDialog.showError("Delete Error", "Failed to delete item: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    @FXML
    private void handleRefresh() {
        refreshInventoryTable();
    }
}
