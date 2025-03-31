package ca.humber.controller;

import ca.humber.dao.CustomerDAO;
import ca.humber.exceptions.ConstraintException;
import ca.humber.model.Customer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CustomersTabController implements Initializable {
    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private TableColumn<Customer, Integer> customerIdColumn;
    @FXML
    private TableColumn<Customer, String> nameColumn;
    @FXML
    private TableColumn<Customer, String> phoneColumn;
    @FXML
    private TableColumn<Customer, String> emailColumn;
    @FXML
    private TextField customerSearchField;

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        loadCustomers();
    }

    private void setupTableColumns() {
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        phoneColumn.setCellFactory(column -> new TableCell<Customer, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if (item.length() == 10) {
                        setText(item.substring(0, 3) + "-" + item.substring(3, 6) + "-" + item.substring(6));
                    } else {
                        setText(item);
                    }
                }
            }
        });
    }

    private void loadCustomers() {
        try {
            List<Customer> customers = CustomerDAO.getActiveCustomers();
            customerList.clear();
            customerList.addAll(customers);
            customerTable.setItems(customerList);
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while loading customer data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCustomerSearch() {
        String searchTerm = customerSearchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadCustomers();
            return;
        }

        try {
            List<Customer> searchResults = CustomerDAO.searchCustomers(searchTerm);
            customerList.clear();
            customerList.addAll(searchResults);
            customerTable.setItems(customerList);

            if (searchResults.isEmpty()) {
                AlertDialog.showSuccess("Search Results", "No customers matching the criteria were found");
            } else {
                AlertDialog.showSuccess("Search Results",
                        "Found " + searchResults.size() + " customers matching the criteria");
            }
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while searching for customers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddCustomer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/customer_form.fxml"));
            Parent root = loader.load();

            CustomerFormController controller = loader.getController();
            controller.setMode("add");
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Customer");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            AlertDialog.showWarning("Error",
                    "An error occurred while opening the add customer form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditCustomer() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            AlertDialog.showWarning("Edit Customer", "Please select a customer to edit first");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/customer_form.fxml"));
            Parent root = loader.load();

            CustomerFormController controller = loader.getController();
            controller.setMode("edit");
            controller.setCustomer(selectedCustomer);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Customer");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            AlertDialog.showWarning("Error",
                    "An error occurred while opening the edit customer form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteCustomer() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            AlertDialog.showWarning("Delete Customer", "Please select a customer to delete first");
            return;
        }

        boolean confirm = AlertDialog.showConfirmation("Confirm Deletion",
                "Are you sure you want to delete customer " + selectedCustomer.getName() + "?" +
                        "\n\nThis action will not permanently delete the data, it will only mark the customer as inactive.");

        if (confirm) {
            try {
                boolean deleted = CustomerDAO.deleteCustomer(selectedCustomer.getCustomerId());
                if (deleted) {
                    AlertDialog.showSuccess("Delete Customer", "Customer successfully deleted (marked as inactive)");
                    loadCustomers();
                } else {
                    AlertDialog.showWarning("Error", "Failed to delete customer");
                }
            } catch (ConstraintException e) {
                AlertDialog.showWarning("Constraint Error", e.getMessage());
            } catch (Exception e) {
                AlertDialog.showWarning("Error", "An error occurred while deleting the customer: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRefreshCustomer() {
        loadCustomers();
        AlertDialog.showSuccess("Refresh", "Customer list has been updated");
    }

    public void refreshCustomers() {
        loadCustomers();
    }
}
