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

    // Modify here to use ServiceTypeWrapper
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
        // Set up month mapping
        String[] months = {"January", "February", "March", "April", "May", "June", 
                          "July", "August", "September", "October", "November", "December"};
        for (int i = 0; i < months.length; i++) {
            monthNameToNumber.put(months[i], i + 1);
        }
        
        // Set report type change event
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
        
        // Initialize year to current year
        yearField.setText(String.valueOf(LocalDate.now().getYear()));
        
        // Initialize month to current month
        monthComboBox.setValue(months[LocalDate.now().getMonthValue() - 1]);
        
        // Initialize date pickers
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        
        // Load service types
        loadServiceTypes();
        
        // Load customers
        loadCustomers();
        
        // Initialize save report button
        saveReportButton.setDisable(true);
    }

    // Add this method in ReportsTabController class
    private void loadServiceTypes() {
        try {
            ObservableList<ServiceTypeWrapper> serviceTypes = FXCollections.observableArrayList();
            
            // Add "All Service Types" option
            serviceTypes.add(new ServiceTypeWrapper(0, "All Service Types"));
            
            // Directly create default service type options, consistent with ServiceFormController
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
            
            // Set default selection to "All Service Types"
            serviceTypeComboBox.getSelectionModel().selectFirst();
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
            
            // Get the selected service type
            ServiceTypeWrapper selectedServiceType = serviceTypeComboBox.getValue();
            
            // Depending on whether a service type is selected (not "All Service Types"), choose to call different functions
            if (selectedServiceType != null && selectedServiceType.getId() != 0) {
                // Use service type and date range function
                LocalDate startDate = LocalDate.of(year, month, 1);
                LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
                
                HibernateUtil.callFunction(conn -> {
                    try (CallableStatement stmt = conn.prepareCall("{ ? = call report_pkg.fn_revenue_report_by_service_type(?, ?, ?) }")) {
                        stmt.registerOutParameter(1, OracleTypes.CURSOR);
                        stmt.setString(2, selectedServiceType.getName()); // Use service type name
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
                // Use year and month report function (when "All Service Types" is selected or null)
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
            
            // Select different query methods depending on whether an invoice ID is specified
            if (!invoiceId.isEmpty()) {
                // If an invoice ID is specified, query the specific invoice directly
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
                // Query invoices based on conditions
                LocalDate startDate = startDatePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();
                String statusValue = statusComboBox.getValue();
                
                // Convert status code
                String statusCode = null;
                if (statusValue != null && !statusValue.equals("All")) {
                    if ("Scheduled".equals(statusValue)) statusCode = "S";
                    else if ("In Progress".equals(statusValue)) statusCode = "I";
                    else if ("Completed".equals(statusValue)) statusCode = "C";
                    else if ("Canceled".equals(statusValue)) statusCode = "X";
                }
                
                // Check if a customer is selected
                Customer selectedCustomer = customerComboBox.getValue();
                
                // If a specific customer is selected
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
                // If a specific status is selected
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
                // Otherwise, query all invoices within the date range
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

    // Modify populateTableFromResultSet method to enhance error handling and data type conversion security
    private void populateTableFromResultSet(ResultSet rs) throws SQLException {
        // Clear the table
        clearTableView();

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Create table columns
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

        // Add data rows, enhance error handling
        ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();
        while (rs.next()) {
            ObservableList<Object> row = FXCollections.observableArrayList();
            for (int i = 1; i <= columnCount; i++) {
                try {
                    Object value = rs.getObject(i);
                    row.add(value);
                } catch (SQLException e) {
                    // If getting the object fails, try getting it as a string
                    try {
                        String stringValue = rs.getString(i);
                        row.add(stringValue);
                    } catch (SQLException ex) {
                        // If it still fails, add an empty string
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