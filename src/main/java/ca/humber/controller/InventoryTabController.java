
package ca.humber.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class InventoryTabController {
    @FXML
    private TableView<?> inventoryTable;
    @FXML
    private TableColumn<?, ?> itemIdColumn;
    @FXML
    private TableColumn<?, ?> itemNameColumn;
    @FXML
    private TableColumn<?, ?> categoryColumn;
    @FXML
    private TableColumn<?, ?> quantityColumn;
    @FXML
    private TableColumn<?, ?> unitPriceColumn;
    @FXML
    private TableColumn<?, ?> supplierColumn;
    @FXML
    private TextField searchField;

    @FXML
    private void handleSearch() {
        AlertDialog.showSuccess("Vehicles", "Inventory Search Clicked");
    }

    @FXML
    private void handleAddItem() {
        AlertDialog.showSuccess("Vehicles", "Add Item Clicked");
    }

    @FXML
    private void handleEditItem() {
        AlertDialog.showSuccess("Vehicles", "Edit Item Clicked");
    }

    @FXML
    private void handleDeleteItem() {
        AlertDialog.showSuccess("Vehicles", "Delete Item Clicked");
    }

    @FXML
    private void handleRefresh() {
        AlertDialog.showSuccess("Vehicles", "Refresh Inventory Clicked");
    }
}
