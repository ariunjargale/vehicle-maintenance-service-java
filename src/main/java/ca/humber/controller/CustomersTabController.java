
package ca.humber.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class CustomersTabController {
    @FXML
    private TableView<?> customerTable;
    @FXML
    private TableColumn<?, ?> customerIdColumn;
    @FXML
    private TableColumn<?, ?> firstNameColumn;
    @FXML
    private TableColumn<?, ?> lastNameColumn;
    @FXML
    private TableColumn<?, ?> phoneColumn;
    @FXML
    private TableColumn<?, ?> emailColumn;
    @FXML
    private TableColumn<?, ?> addressColumn;
    @FXML
    private TextField customerSearchField;

    @FXML
    private void handleCustomerSearch() {
        AlertDialog.showSuccess("Customers", "Customer Search Clicked");
    }

    @FXML
    private void handleAddCustomer() {
        AlertDialog.showSuccess("Customers", "Customer Add Clicked");

    }

    @FXML
    private void handleEditCustomer() {
        AlertDialog.showSuccess("Customers", "Customer Edit Clicked");

    }

    @FXML
    private void handleDeleteCustomer() {
        AlertDialog.showSuccess("Customers", "Customer Delete Clicked");

    }

    @FXML
    private void handleRefreshCustomer() {
        AlertDialog.showSuccess("Customers", "Customer Refresh Clicked");

    }
}
