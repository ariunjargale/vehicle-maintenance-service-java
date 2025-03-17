package ca.humber.main;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;

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

    public static class InventoryController {
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

    private void showAlert(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Button Clicked");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    }
}