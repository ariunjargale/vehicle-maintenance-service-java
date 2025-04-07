package ca.humber.controller;

import java.net.URL;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import ca.humber.dao.CustomerDAO;
import ca.humber.model.Customer;
import ca.humber.model.ServiceTypeWrapper;
import ca.humber.util.HibernateUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import oracle.jdbc.OracleTypes;

public class ReportsTabController implements Initializable {

    @FXML
    private ComboBox<String> reportTypeComboBox;

    @FXML
    private ComboBox<String> reportFormatComboBox;

    @FXML
    private VBox revenueOptionsPane;

    @FXML
    private VBox invoiceOptionsPane;

    @FXML
    private TextField yearField;

    @FXML
    private ComboBox<String> monthComboBox;

    // 修改這裡，改用 ServiceTypeWrapper
    @FXML
    private ComboBox<ServiceTypeWrapper> serviceTypeComboBox;

    @FXML
    private TextField invoiceIdField;

    @FXML
    private ComboBox<Customer> customerComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Button generateReportButton;

    @FXML
    private Button saveReportButton;

    @FXML
    private Button clearButton;

    @FXML
    private TableView<ObservableList<Object>> reportTableView;

    private Map<String, Integer> monthNameToNumber = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 設置月份映射
        String[] months = {"January", "February", "March", "April", "May", "June", 
                          "July", "August", "September", "October", "November", "December"};
        for (int i = 0; i < months.length; i++) {
            monthNameToNumber.put(months[i], i + 1);
        }
        
        // 設定報表類型變更事件
        reportTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if ("Revenue Report".equals(newValue)) {
                    revenueOptionsPane.setVisible(true);
                    revenueOptionsPane.setManaged(true);
                    invoiceOptionsPane.setVisible(false);
                    invoiceOptionsPane.setManaged(false);
                } else if ("Invoice Report".equals(newValue)) {
                    invoiceOptionsPane.setVisible(true);
                    invoiceOptionsPane.setManaged(true);
                    revenueOptionsPane.setVisible(false);
                    revenueOptionsPane.setManaged(false);
                }
            }
        });
        
        // 初始化年份為當前年份
        yearField.setText(String.valueOf(LocalDate.now().getYear()));
        
        // 初始化月份為當前月份
        monthComboBox.setValue(months[LocalDate.now().getMonthValue() - 1]);
        
        // 初始化日期選擇器
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        
        // 載入服務類型
        loadServiceTypes();
        
        // 載入客戶
        loadCustomers();
        
        // 初始化儲存報表按鈕
        saveReportButton.setDisable(true);
    }

    // 在 ReportsTabController 類中添加此方法
    private void loadServiceTypes() {
        try {
            ObservableList<ServiceTypeWrapper> serviceTypes = FXCollections.observableArrayList();
            
            // 直接創建預設的服務類型選項，與 ServiceFormController 保持一致
            serviceTypes.addAll(
                new ServiceTypeWrapper(1, "Regular Maintenance"),
                new ServiceTypeWrapper(2, "Engine Repair"),
                new ServiceTypeWrapper(3, "Transmission Repair"),
                new ServiceTypeWrapper(4, "Brake Service"),
                new ServiceTypeWrapper(5, "Electrical Repair"),
                new ServiceTypeWrapper(6, "Air Conditioning"),
                new ServiceTypeWrapper(7, "Suspension Work"),
                new ServiceTypeWrapper(8, "Wheel and Tire Service"),
                new ServiceTypeWrapper(9, "Diagnostic Service"),
                new ServiceTypeWrapper(10, "Exhaust System Repair")
            );
            
            serviceTypeComboBox.setItems(serviceTypes);
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "Failed to load service types: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCustomers() {
        try {
            ObservableList<Customer> customers = FXCollections.observableArrayList();
            customers.addAll(CustomerDAO.getActiveCustomers());
            customerComboBox.setItems(customers);
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "Failed to load customers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGenerateReport() {
        String reportType = reportTypeComboBox.getValue();
        if (reportType == null) {
            AlertDialog.showWarning("Warning", "Please select a report type");
            return;
        }

        clearTableView();

        try {
            if ("Revenue Report".equals(reportType)) {
                generateRevenueReport();
            } else if ("Invoice Report".equals(reportType)) {
                generateInvoiceReport();
            }

            saveReportButton.setDisable(false);
        } catch (Exception e) {
            AlertDialog.showError("Error", "Failed to generate report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateRevenueReport() {
        try {
            int year = Integer.parseInt(yearField.getText());
            int month = getMonthNumber(monthComboBox.getValue());
            
            // 獲取選擇的服務類型
            ServiceTypeWrapper selectedServiceType = serviceTypeComboBox.getValue();
            
            // 根據是否選擇了服務類型，選擇調用不同的函數
            if (selectedServiceType != null) {
                // 使用服務類型和日期範圍函數
                LocalDate startDate = LocalDate.of(year, month, 1);
                LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
                
                HibernateUtil.callFunction(conn -> {
                    try (CallableStatement stmt = conn.prepareCall("{ ? = call report_pkg.fn_revenue_report_by_service_type(?, ?, ?) }")) {
                        stmt.registerOutParameter(1, OracleTypes.CURSOR);
                        stmt.setString(2, selectedServiceType.getName()); // 使用服務類型名稱
                        stmt.setDate(3, java.sql.Date.valueOf(startDate));
                        stmt.setDate(4, java.sql.Date.valueOf(endDate));
                        stmt.execute();
                        
                        try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                            populateTableFromResultSet(rs);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException("Failed to execute revenue report by service type: " + e.getMessage(), e);
                    }
                    return null;
                });
            } else {
                // 使用年月報表函數
                HibernateUtil.callFunction(conn -> {
                    try (CallableStatement stmt = conn.prepareCall("{ ? = call report_pkg.fn_revenue_report(?, ?) }")) {
                        stmt.registerOutParameter(1, OracleTypes.CURSOR);
                        stmt.setInt(2, year);
                        stmt.setInt(3, month);
                        stmt.execute();
                        
                        try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                            populateTableFromResultSet(rs);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException("Failed to execute revenue report: " + e.getMessage(), e);
                    }
                    return null;
                });
            }
            
            AlertDialog.showSuccess("Report Generated", "Revenue report generated successfully");
        } catch (NumberFormatException e) {
            AlertDialog.showWarning("Input Error", "Please enter valid year");
        } catch (Exception e) {
            AlertDialog.showError("Error", "Failed to generate revenue report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateInvoiceReport() {
        try {
            String invoiceId = invoiceIdField.getText().trim();
            
            // 根據是否指定了發票ID選擇不同的查詢方式
            if (!invoiceId.isEmpty()) {
                // 如果指定了發票ID，則直接查詢特定發票
                int id = Integer.parseInt(invoiceId);
                
                HibernateUtil.callFunction(conn -> {
                    try (CallableStatement stmt = conn.prepareCall("{ ? = call report_pkg.fn_invoice_by_id(?) }")) {
                        stmt.registerOutParameter(1, OracleTypes.CURSOR);
                        stmt.setInt(2, id);
                        stmt.execute();
                        
                        try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                            populateTableFromResultSet(rs);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException("Failed to execute invoice report: " + e.getMessage(), e);
                    }
                    return null;
                });
            } else {
                // 根據條件查詢發票
                LocalDate startDate = startDatePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();
                String statusValue = statusComboBox.getValue();
                
                // 轉換狀態碼
                String statusCode = null;
                if (statusValue != null && !statusValue.equals("All")) {
                    if ("Scheduled".equals(statusValue)) statusCode = "S";
                    else if ("In Progress".equals(statusValue)) statusCode = "I";
                    else if ("Completed".equals(statusValue)) statusCode = "C";
                    else if ("Canceled".equals(statusValue)) statusCode = "X";
                }
                
                // 檢查是否選擇了客戶
                Customer selectedCustomer = customerComboBox.getValue();
                
                // 如果選擇了特定客戶
                if (selectedCustomer != null) {
                    final int customerId = selectedCustomer.getCustomerId();
                    
                    HibernateUtil.callFunction(conn -> {
                        try (CallableStatement stmt = conn.prepareCall("{ ? = call report_pkg.fn_invoices_by_customer(?, ?, ?) }")) {
                            stmt.registerOutParameter(1, OracleTypes.CURSOR);
                            stmt.setInt(2, customerId);
                            stmt.setDate(3, java.sql.Date.valueOf(startDate));
                            stmt.setDate(4, java.sql.Date.valueOf(endDate));
                            stmt.execute();
                            
                            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                                populateTableFromResultSet(rs);
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException("Failed to execute invoice report by customer: " + e.getMessage(), e);
                        }
                        return null;
                    });
                }
                // 如果選擇了特定狀態
                else if (statusCode != null) {
                    final String finalStatusCode = statusCode;
                    
                    HibernateUtil.callFunction(conn -> {
                        try (CallableStatement stmt = conn.prepareCall("{ ? = call report_pkg.fn_invoices_by_status(?, ?, ?) }")) {
                            stmt.registerOutParameter(1, OracleTypes.CURSOR);
                            stmt.setString(2, finalStatusCode);
                            stmt.setDate(3, java.sql.Date.valueOf(startDate));
                            stmt.setDate(4, java.sql.Date.valueOf(endDate));
                            stmt.execute();
                            
                            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                                populateTableFromResultSet(rs);
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException("Failed to execute invoice report by status: " + e.getMessage(), e);
                        }
                        return null;
                    });
                }
                // 否則查詢日期範圍內的所有發票
                else {
                    HibernateUtil.callFunction(conn -> {
                        try (CallableStatement stmt = conn.prepareCall("{ ? = call report_pkg.fn_revenue_report_by_date_range(?, ?) }")) {
                            stmt.registerOutParameter(1, OracleTypes.CURSOR);
                            stmt.setDate(2, java.sql.Date.valueOf(startDate));
                            stmt.setDate(3, java.sql.Date.valueOf(endDate));
                            stmt.execute();
                            
                            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                                populateTableFromResultSet(rs);
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException("Failed to execute invoice report by date range: " + e.getMessage(), e);
                        }
                        return null;
                    });
                }
            }
            
            AlertDialog.showSuccess("Report Generated", "Invoice report generated successfully");
        } catch (NumberFormatException e) {
            AlertDialog.showWarning("Input Error", "Please enter valid invoice ID");
        } catch (Exception e) {
            AlertDialog.showError("Error", "Failed to generate invoice report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveReport() {
        if (reportTableView.getItems().isEmpty()) {
            AlertDialog.showWarning("Warning", "No report data to save");
            return;
        }

        AlertDialog.showSuccess("Save Report Test",
                "This is a test of the save report functionality. In production, this would save the report to a file.");

    }

    @FXML
    private void handleClear() {
        reportTypeComboBox.setValue(null);
        revenueOptionsPane.setVisible(false);
        revenueOptionsPane.setManaged(false);
        invoiceOptionsPane.setVisible(false);
        invoiceOptionsPane.setManaged(false);

        yearField.setText(String.valueOf(LocalDate.now().getYear()));
        monthComboBox.setValue("January");
        serviceTypeComboBox.setValue(null);

        invoiceIdField.clear();
        customerComboBox.setValue(null);
        statusComboBox.setValue("All");

        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());

        clearTableView();

        saveReportButton.setDisable(true);
    }

    private void clearTableView() {
        reportTableView.getColumns().clear();
        reportTableView.getItems().clear();
    }

    // 修改 populateTableFromResultSet 方法以增強錯誤處理和數據類型轉換安全性
    private void populateTableFromResultSet(ResultSet rs) throws SQLException {
        // 清空表格
        clearTableView();

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // 创建表格列
        for (int i = 1; i <= columnCount; i++) {
            final int columnIndex = i - 1;
            String columnName = metaData.getColumnName(i);

            TableColumn<ObservableList<Object>, Object> column = new TableColumn<>(columnName);
            column.setCellValueFactory(cellData -> {
                ObservableList<Object> row = cellData.getValue();
                if (columnIndex >= row.size()) {
                    return new SimpleObjectProperty<>("");
                }
                Object value = row.get(columnIndex);
                if (value == null) {
                    return new SimpleObjectProperty<>("");
                }
                return new SimpleObjectProperty<>(value);
            });

            reportTableView.getColumns().add(column);
        }

        // 添加數據行，增強錯誤處理
        ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();
        while (rs.next()) {
            ObservableList<Object> row = FXCollections.observableArrayList();
            for (int i = 1; i <= columnCount; i++) {
                try {
                    Object value = rs.getObject(i);
                    row.add(value);
                } catch (SQLException e) {
                    // 如果獲取對象失敗，嘗試作為字符串獲取
                    try {
                        String stringValue = rs.getString(i);
                        row.add(stringValue);
                    } catch (SQLException ex) {
                        // 如果還是失敗，添加空字符串
                        row.add("");
                    }
                }
            }
            data.add(row);
        }

        reportTableView.setItems(data);
    }

    private int getMonthNumber(String monthName) {
        return monthNameToNumber.getOrDefault(monthName, 1);
    }
}