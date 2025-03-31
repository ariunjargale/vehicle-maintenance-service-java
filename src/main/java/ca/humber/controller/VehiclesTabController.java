package ca.humber.controller;

import ca.humber.dao.VehicleDAO;
import ca.humber.exceptions.ConstraintException;
import ca.humber.model.Vehicle;
import ca.humber.model.Customer;
import ca.humber.dao.CustomerDAO;

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

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class VehiclesTabController implements Initializable {
    @FXML
    private TableView<Vehicle> vehicleTable;
    @FXML
    private TableColumn<Vehicle, Integer> vehicleIdColumn;
    @FXML
    private TableColumn<Vehicle, String> makeColumn;
    @FXML
    private TableColumn<Vehicle, String> modelColumn;
    @FXML
    private TableColumn<Vehicle, Integer> yearColumn;
    @FXML
    private TableColumn<Vehicle, String> vinColumn;
    @FXML
    private TableColumn<Vehicle, Customer> ownerColumn;
    @FXML
    private TableColumn<Vehicle, String> lastServiceColumn;
    @FXML
    private TextField vehicleSearchField;

    private ObservableList<Vehicle> vehicleList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set table columns to bind with Vehicle object properties
        vehicleIdColumn.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));
        makeColumn.setCellValueFactory(new PropertyValueFactory<>("make")); // 添加make屬性綁定
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        vinColumn.setCellValueFactory(new PropertyValueFactory<>("vin"));

        // Owner column requires special handling as it is an associated object
        ownerColumn.setCellValueFactory(cellData -> {
            Customer customer = cellData.getValue().getCustomer();
            return new javafx.beans.property.SimpleObjectProperty<>(customer);
        });
        ownerColumn.setCellFactory(column -> new TableCell<Vehicle, Customer>() {
            @Override
            protected void updateItem(Customer customer, boolean empty) {
                super.updateItem(customer, empty);
                if (empty || customer == null) {
                    setText(null);
                } else {
                    setText(customer.getName());
                }
            }
        });

        // Load all vehicle data
        loadVehicles();
    }

    // Load all vehicles
    private void loadVehicles() {
        try {
            List<Vehicle> vehicles = VehicleDAO.getVehiclesList();
            vehicleList.clear();
            vehicleList.addAll(vehicles);
            vehicleTable.setItems(vehicleList);
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while loading vehicle data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Search vehicles
    @FXML
    private void handleVehicleSearch() {
        String searchTerm = vehicleSearchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadVehicles();
            return;
        }

        try {
            List<Vehicle> searchResults = VehicleDAO.searchVehicles(searchTerm);
            vehicleList.clear();
            vehicleList.addAll(searchResults);
            vehicleTable.setItems(vehicleList);

            if (searchResults.isEmpty()) {
                AlertDialog.showSuccess("Search Results", "No vehicles found matching the criteria");
            } else {
                AlertDialog.showSuccess("Search Results", "Found " + searchResults.size() + " vehicles matching the criteria");
            }
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while searching for vehicles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Add new vehicle
    @FXML
    private void handleAddVehicle() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/vehicle_form.fxml"));
            Parent root = loader.load();
            VehicleFormController controller = loader.getController();
            controller.setMode("add");

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Vehicle");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Reload data
            loadVehicles();
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while opening the add vehicle form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Edit vehicle
    @FXML
    private void handleEditVehicle() {
        Vehicle selectedVehicle = vehicleTable.getSelectionModel().getSelectedItem();
        if (selectedVehicle == null) {
            AlertDialog.showWarning("Warning", "Please select a vehicle to edit first");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/vehicle_form.fxml"));
            Parent root = loader.load();
            VehicleFormController controller = loader.getController();
            controller.setMode("edit");
            controller.setVehicle(selectedVehicle);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Vehicle");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Reload data
            loadVehicles();
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while opening the edit vehicle form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Delete vehicle
    @FXML
    private void handleDeleteVehicle() {
        Vehicle selectedVehicle = vehicleTable.getSelectionModel().getSelectedItem();
        if (selectedVehicle == null) {
            AlertDialog.showWarning("Warning", "Please select a vehicle to delete first");
            return;
        }

        boolean confirm = AlertDialog.showConfirm("Confirm Deletion",
                "Are you sure you want to delete the vehicle " + selectedVehicle.getYear() + " " + 
                selectedVehicle.getMake() + " " + selectedVehicle.getModel() + " with VIN: " + 
                selectedVehicle.getVin() + "?");

        if (confirm) {
            try {
                boolean success = VehicleDAO.deleteVehicle(selectedVehicle.getVehicleId());
                if (success) {
                    AlertDialog.showSuccess("Success", "Vehicle successfully deleted (marked as inactive)");
                    loadVehicles();  // Reload data
                } else {
                    AlertDialog.showWarning("Error", "Failed to delete vehicle");
                }
            } catch (ConstraintException e) {
                AlertDialog.showWarning("Constraint Error", e.getMessage());
            } catch (Exception e) {
                AlertDialog.showWarning("Error", "An error occurred while deleting the vehicle: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Refresh vehicle list
    @FXML
    private void handleRefreshVehicle() {
        loadVehicles();
        AlertDialog.showSuccess("Refresh", "Vehicle list has been refreshed");
    }
}
