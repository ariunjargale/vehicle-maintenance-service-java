package ca.humber.controller;

import ca.humber.dao.ServiceDAO;
import ca.humber.exceptions.ConstraintException;
import ca.humber.model.Service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ServiceTabController implements Initializable {
    @FXML
    private TableView<Service> serviceTable;
    @FXML
    private TableColumn<Service, Integer> serviceIdColumn;
    @FXML
    private TableColumn<Service, String> serviceNameColumn;
    @FXML
    private TableColumn<Service, String> serviceTypeIdColumn; // 已改為 String 類型
    @FXML
    private TableColumn<Service, BigDecimal> priceColumn;
    @FXML
    private TextField serviceSearchField;

    private ObservableList<Service> serviceList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        loadServices();
    }

    private void setupTableColumns() {
        serviceIdColumn.setCellValueFactory(new PropertyValueFactory<>("serviceId"));
        serviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        serviceTypeIdColumn.setCellValueFactory(new PropertyValueFactory<>("serviceTypeId"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        serviceTypeIdColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Service, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                
                int index = getIndex();
                if (index >= 0 && index < getTableView().getItems().size()) {
                    Service service = getTableView().getItems().get(index);
                    if (service != null) {
                        setText(service.getServiceTypeName());
                        return;
                    }
                }
                setText(item);
            }
        });

        priceColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Service, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item.doubleValue()));
                }
            }
        });
    }

    private void loadServices() {
        try {
            List<Service> services = ServiceDAO.getActiveServices();
            serviceList.clear();
            serviceList.addAll(services);
            serviceTable.setItems(serviceList);
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while loading service data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSearchService() {
        String searchTerm = serviceSearchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadServices();
            return;
        }

        try {
            List<Service> searchResults = ServiceDAO.searchServices(searchTerm);
            serviceList.clear();
            serviceList.addAll(searchResults);
            serviceTable.setItems(serviceList);

            if (searchResults.isEmpty()) {
                AlertDialog.showSuccess("Search Results", "No services matching the criteria were found");
            } else {
                AlertDialog.showSuccess("Search Results",
                        "Found " + searchResults.size() + " services matching the criteria");
            }
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while searching for services: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddService() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/service_form.fxml"));
            Parent root = loader.load();

            ServiceFormController controller = loader.getController();
            controller.setMode("add");
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Service");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            AlertDialog.showWarning("Error",
                    "An error occurred while opening the add service form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleEditService() {
        Service selectedService = serviceTable.getSelectionModel().getSelectedItem();
        if (selectedService == null) {
            AlertDialog.showWarning("Edit Service", "Please select a service to edit first");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/service_form.fxml"));
            Parent root = loader.load();

            ServiceFormController controller = loader.getController();
            controller.setMode("edit");
            controller.setService(selectedService);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Service");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            AlertDialog.showWarning("Error",
                    "An error occurred while opening the edit service form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleDeleteService() {
        Service selectedService = serviceTable.getSelectionModel().getSelectedItem();
        if (selectedService == null) {
            AlertDialog.showWarning("Delete Service", "Please select a service to delete first");
            return;
        }

        boolean confirm = AlertDialog.showConfirmation("Confirm Deletion",
                "Are you sure you want to delete service " + selectedService.getServiceName() + "?" +
                        "\n\nThis action will not permanently delete the data, it will only mark the service as inactive.");

        if (confirm) {
            try {
                boolean deleted = ServiceDAO.deleteService(selectedService.getServiceId());
                if (deleted) {
                    AlertDialog.showSuccess("Delete Service", "Service successfully deleted (marked as inactive)");
                    loadServices();
                } else {
                    AlertDialog.showWarning("Error", "Failed to delete service");
                }
            } catch (ConstraintException e) {
                AlertDialog.showWarning("Constraint Error", e.getMessage());
            } catch (Exception e) {
                AlertDialog.showWarning("Error", "An error occurred while deleting the service: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleRefreshService() {
        loadServices();
        AlertDialog.showSuccess("Refresh", "Service list has been updated");
    }

    public void refreshServices() {
        loadServices();
    }
}