package ca.humber.controller;

import javafx.scene.control.SelectionMode;
import ca.humber.dao.ServiceInventoryDAO;
import ca.humber.model.ServiceInventory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ServiceInventoryTabController implements Initializable {

    @FXML private TableView<ServiceInventory> serviceInventoryTable;
    @FXML private TableColumn<ServiceInventory, Integer> serviceIdColumn;
    @FXML private TableColumn<ServiceInventory, Integer> itemIdColumn;
    @FXML private TableColumn<ServiceInventory, Integer> quantityRequiredColumn;
    @FXML private TableColumn<ServiceInventory, String> serviceNameColumn;
    @FXML private TableColumn<ServiceInventory, String> itemNameColumn;
    @FXML private TextField searchField;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;

    private final ObservableList<ServiceInventory> serviceInventoryList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        loadServiceInventory();

        // Always enable Edit and Delete buttons
        editButton.setDisable(false);
        deleteButton.setDisable(false);
    }

    private void setupTableColumns() {
        serviceIdColumn.setCellValueFactory(new PropertyValueFactory<>("serviceId"));
        itemIdColumn.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        quantityRequiredColumn.setCellValueFactory(new PropertyValueFactory<>("quantityRequired"));
        serviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
    }

    private void loadServiceInventory() {
        serviceInventoryList.clear();
        try {
            serviceInventoryList.addAll(ServiceInventoryDAO.getAllDetailed());
            serviceInventoryTable.setItems(serviceInventoryList);
            searchField.clear();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Load Failed", e.getMessage());
        }
    }

    @FXML
    public void handleSearch() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            serviceInventoryTable.setItems(serviceInventoryList);
        } else {
            try {
                ObservableList<ServiceInventory> filteredList = FXCollections.observableArrayList(
                        ServiceInventoryDAO.search(searchText)
                );
                serviceInventoryTable.setItems(filteredList);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Search Failed", e.getMessage());
            }
        }
    }

    @FXML
    public void handleAdd() {
        openForm("add", null);
    }

    @FXML
    public void handleEdit() {
        ServiceInventory selected = serviceInventoryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openForm("edit", selected);
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "No item selected", "Please select an item to edit.");
        }
    }

    @FXML
    public void handleDelete() {
        ServiceInventory selected = serviceInventoryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Delete Service Inventory Item");
            alert.setContentText("Are you sure you want to delete this item?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    boolean success = ServiceInventoryDAO.delete(selected.getServiceId(), selected.getItemId());
                    if (success) {
                        loadServiceInventory();
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Item Deleted", "The item was deleted.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Delete Failed", "The item could not be deleted.");
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Delete Failed", e.getMessage());
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "No item selected", "Please select an item to delete.");
        }
    }

    @FXML
    public void handleRefresh() {
        loadServiceInventory();
    }

    public void refreshServiceInventory() {
        loadServiceInventory();
    }

    private void openForm(String mode, ServiceInventory selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/service_inventory_form.fxml"));
            Parent root = loader.load();

            ServiceInventoryFormController controller = loader.getController();
            controller.setServiceInventoryController(this);
            controller.setMode(mode);
            if (selected != null) {
                controller.setServiceInventory(selected);
            }

            Stage dialogStage = new Stage();
            dialogStage.setTitle(mode.equals("add") ? "Add Item" : "Edit Item");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(serviceInventoryTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load form", e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
