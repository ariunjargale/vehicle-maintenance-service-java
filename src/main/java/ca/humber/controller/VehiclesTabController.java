
package ca.humber.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class VehiclesTabController {
    @FXML
    private TableView<?> vehicleTable;
    @FXML
    private TableColumn<?, ?> vehicleIdColumn;
    @FXML
    private TableColumn<?, ?> makeColumn;
    @FXML
    private TableColumn<?, ?> modelColumn;
    @FXML
    private TableColumn<?, ?> yearColumn;
    @FXML
    private TableColumn<?, ?> vinColumn;
    @FXML
    private TableColumn<?, ?> ownerColumn;
    @FXML
    private TextField vehicleSearchField;

    @FXML
    private void handleVehicleSearch() {
        AlertDialog.showSuccess("Customers", "Vehicle Search Clicked");
    }

    @FXML
    private void handleAddVehicle() {
        AlertDialog.showSuccess("Customers", "Add Vehicle Clicked");
    }

    @FXML
    private void handleEditVehicle() {
        AlertDialog.showSuccess("Customers", "Edit Vehicle Clicked");
    }

    @FXML
    private void handleDeleteVehicle() {
        AlertDialog.showSuccess("Customers", "Delete Vehicle Clicked");
    }

    @FXML
    private void handleRefreshVehicle() {
        AlertDialog.showSuccess("Customers", "Refresh Vehicles Clicked");
    }
}
