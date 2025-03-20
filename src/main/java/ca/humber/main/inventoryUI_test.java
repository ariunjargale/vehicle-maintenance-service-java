package ca.humber.main;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * JavaFX UI Test - Inventory Screen
 */
public class inventoryUI_test extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 載入 inventory.fxml 檔案
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/inventory.fxml"));
        Parent root = loader.load();
        
        // 創建場景
        Scene scene = new Scene(root, 800, 600);
        
        // 設定舞台
        primaryStage.setTitle("Inventory Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        System.out.println("Inventory FXML loaded successfully");
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class InventoryController implements Initializable {
        @FXML
        private TabPane mainTabPane;
        
        // Inventory Tab
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
        
        // Appointment Tab
        @FXML
        private TableView<?> appointmentTable;
        @FXML
        private TableColumn<?, ?> appointmentIdColumn;
        @FXML
        private TableColumn<?, ?> customerNameColumn;
        @FXML
        private TableColumn<?, ?> vehicleInfoColumn;
        @FXML
        private TableColumn<?, ?> dateColumn;
        @FXML
        private TableColumn<?, ?> timeColumn;
        @FXML
        private TableColumn<?, ?> serviceTypeColumn;
        @FXML
        private TableColumn<?, ?> statusColumn;
        @FXML
        private TextField appointmentSearchField;
        
        // Vehicle Tab
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
        private TableColumn<?, ?> lastServiceColumn;
        @FXML
        private TextField vehicleSearchField;
        
        // Customer Tab
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
        
        @Override
        public void initialize(URL location, ResourceBundle resources) {
            // 初始化代碼將在這裡執行
            System.out.println("Controller initialized");
            // 未來可在這裡載入資料
        }
        
        // Inventory Tab Methods
        @FXML
        private void handleSearch() {
            showAlert("Search button clicked");
        }

        @FXML
        private void handleAddItem() {
            showAlert("Add Item button clicked");
        }

        @FXML
        private void handleEditItem() {
            showAlert("Edit Item button clicked");
        }

        @FXML
        private void handleDeleteItem() {
            showAlert("Delete Item button clicked");
        }

        @FXML
        private void handleRefresh() {
            showAlert("Refresh button clicked");
        }
        
        // Appointment Tab Methods
        @FXML
        private void handleAppointmentSearch() {
            showAlert("Appointment Search clicked");
        }
        
        @FXML
        private void handleAddAppointment() {
            showAlert("Add Appointment clicked");
        }
        
        @FXML
        private void handleEditAppointment() {
            showAlert("Edit Appointment clicked");
        }
        
        @FXML
        private void handleDeleteAppointment() {
            showAlert("Delete Appointment clicked");
        }
        
        @FXML
        private void handleRefreshAppointment() {
            showAlert("Refresh Appointments clicked");
        }
        
        // Vehicle Tab Methods
        @FXML
        private void handleVehicleSearch() {
            showAlert("Vehicle Search clicked");
        }
        
        @FXML
        private void handleAddVehicle() {
            showAlert("Add Vehicle clicked");
        }
        
        @FXML
        private void handleEditVehicle() {
            showAlert("Edit Vehicle clicked");
        }
        
        @FXML
        private void handleDeleteVehicle() {
            showAlert("Delete Vehicle clicked");
        }
        
        @FXML
        private void handleRefreshVehicle() {
            showAlert("Refresh Vehicles clicked");
        }
        
        // Customer Tab Methods
        @FXML
        private void handleCustomerSearch() {
            showAlert("Customer Search clicked");
        }
        
        @FXML
        private void handleAddCustomer() {
            showAlert("Add Customer clicked");
        }
        
        @FXML
        private void handleEditCustomer() {
            showAlert("Edit Customer clicked");
        }
        
        @FXML
        private void handleDeleteCustomer() {
            showAlert("Delete Customer clicked");
        }
        
        @FXML
        private void handleRefreshCustomer() {
            showAlert("Refresh Customers clicked");
        }

        private void showAlert(String message) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Button Clicked");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
}
