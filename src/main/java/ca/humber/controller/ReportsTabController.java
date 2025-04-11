package ca.humber.controller;

import java.io.File;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import ca.humber.dao.CustomerDAO;
import ca.humber.model.Customer;
import ca.humber.model.ServiceTypeWrapper;
import ca.humber.util.HibernateUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import oracle.jdbc.OracleTypes;
import ca.humber.service.ReportExportService;

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

    @FXML
    private VBox totalRevenueBox;

    @FXML
    private TextField totalRevenueField;

    private Map<String, Integer> monthNameToNumber = new HashMap<>();

    private VBox customerSummaryBox;

    @FXML
    private VBox owedAmountBox;

    @FXML
    private TextField owedAmountField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up month mapping
        String[] months = { "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December" };
        for (int i = 0; i < months.length; i++) {
            monthNameToNumber.put(months[i], i + 1);
        }

        // Add "All Year" option to the month dropdown
        ObservableList<String> monthOptions = FXCollections.observableArrayList();
        monthOptions.add("All Year"); // Add all year option
        monthOptions.addAll(months);
        monthComboBox.setItems(monthOptions);

        // Load service types
        loadServiceTypes();

        // Set report type change event
        reportTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Use Platform.runLater to ensure UI updates are completed
                Platform.runLater(() -> {
                    // First clear the previous form data but keep the report type selection
                    clearFormData();

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

                        loadCustomers();
                    }
                });
            }
        });

        yearField.setText(String.valueOf(LocalDate.now().getYear()));

        monthComboBox.setValue("All Year");
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());

        saveReportButton.setDisable(true);

        totalRevenueBox.setVisible(false);
        totalRevenueBox.setManaged(false);

        owedAmountBox.setVisible(false);
        owedAmountBox.setManaged(false);
    }

    // Add a method to clear form data but keep report type selection
    private void clearFormData() {
        yearField.setText(String.valueOf(LocalDate.now().getYear()));
        monthComboBox.setValue("All Year");
        serviceTypeComboBox.getSelectionModel().selectFirst(); // Select "All Service Types"

        invoiceIdField.clear();
        customerComboBox.setValue(null);
        statusComboBox.setValue("All");
        totalRevenueField.clear();

        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());

        clearTableView();

        // Hide total revenue box
        totalRevenueBox.setVisible(false);
        totalRevenueBox.setManaged(false);

        owedAmountBox.setVisible(false);
        owedAmountBox.setManaged(false);

        // Disable save report button
        saveReportButton.setDisable(true);
    }

    // Add this method in ReportsTabController class
    private void loadServiceTypes() {
        try {
            ObservableList<ServiceTypeWrapper> serviceTypes = FXCollections.observableArrayList();

            // Add "All Service Types" option
            serviceTypes.add(new ServiceTypeWrapper(0, "All Service Types"));

            // Directly create default service type options, consistent with
            // ServiceFormController
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
                    new ServiceTypeWrapper(10, "Exhaust System Repair"));

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

            // Add a null option, allowing the user to explicitly select it
            customers.add(null);

            // Add all active customers
            customers.addAll(CustomerDAO.getActiveCustomers());

            customerComboBox.setItems(customers);

            customerComboBox.setCellFactory(param -> new javafx.scene.control.ListCell<Customer>() {
                @Override
                protected void updateItem(Customer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("-- Select a Customer --");
                        return;
                    } else {
                        setText(item.getName());
                    }
                }
            });

            // Set the button cell to properly handle null values
            customerComboBox.setButtonCell(new ListCell<Customer>() {
                @Override
                protected void updateItem(Customer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("-- Select a Customer --");
                    } else {
                        setText(item.getName());
                    }
                }
            });

            // Set to null option
            customerComboBox.getSelectionModel().selectFirst();
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

        if ("Revenue Report".equals(reportType)) {
            totalRevenueBox.setVisible(true);
            totalRevenueBox.setManaged(true);
            owedAmountBox.setVisible(false);
            owedAmountBox.setManaged(false);
        } else if ("Invoice Report".equals(reportType)) {
            totalRevenueBox.setVisible(false);
            totalRevenueBox.setManaged(false);
            owedAmountBox.setVisible(true);
            owedAmountBox.setManaged(true);
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
            String monthSelection = monthComboBox.getValue();
            boolean isFullYear = "All Year".equals(monthSelection);
            int month = isFullYear ? 0 : getMonthNumber(monthSelection);

            ServiceTypeWrapper selectedServiceType = serviceTypeComboBox.getValue();

            if (selectedServiceType != null && selectedServiceType.getId() != 0) {
                // Process report for specific service type
                LocalDate startDate;
                LocalDate endDate;

                if (isFullYear) {
                    // Full year range: January 1 to December 31
                    startDate = LocalDate.of(year, 1, 1);
                    endDate = LocalDate.of(year, 12, 31);
                } else {
                    // Specific month range
                    startDate = LocalDate.of(year, month, 1);
                    endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
                }

                String serviceTypeId = String.valueOf(selectedServiceType.getId());

                HibernateUtil.callFunction(conn -> {
                    // Execute query for specific service type with date range
                    try (CallableStatement stmt = conn
                            .prepareCall("{ ? = call report_pkg.fn_revenue_report_by_service_type(?, ?, ?) }")) {
                        stmt.registerOutParameter(1, OracleTypes.CURSOR);
                        stmt.setString(2, serviceTypeId);
                        stmt.setDate(3, java.sql.Date.valueOf(startDate));
                        stmt.setDate(4, java.sql.Date.valueOf(endDate));
                        stmt.execute();

                        try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                            // Process results and calculate total revenue
                            processRevenueResultSet(rs);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(
                                "Failed to execute revenue report by service type: " + e.getMessage(), e);
                    }
                    return null;
                });
            } else {
                // Process report for all service types
                if (isFullYear) {
                    // Handle full year report for all service types
                    LocalDate startDate = LocalDate.of(year, 1, 1);
                    LocalDate endDate = LocalDate.of(year, 12, 31);

                    HibernateUtil.callFunction(conn -> {
                        try (CallableStatement stmt = conn
                                .prepareCall("{ ? = call report_pkg.fn_revenue_report_by_date_range(?, ?) }")) {
                            stmt.registerOutParameter(1, OracleTypes.CURSOR);
                            stmt.setDate(2, java.sql.Date.valueOf(startDate));
                            stmt.setDate(3, java.sql.Date.valueOf(endDate));
                            stmt.execute();

                            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                                // Process results and calculate total revenue
                                processRevenueResultSet(rs);
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException("Failed to execute annual revenue report: " + e.getMessage(), e);
                        }
                        return null;
                    });
                } else {
                    // Handle specific month report for all service types (existing code)
                    HibernateUtil.callFunction(conn -> {
                        try (CallableStatement stmt = conn
                                .prepareCall("{ ? = call report_pkg.fn_revenue_report(?, ?) }")) {
                            stmt.registerOutParameter(1, OracleTypes.CURSOR);
                            stmt.setInt(2, year);
                            stmt.setInt(3, month);
                            stmt.execute();

                            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                                // Process results and calculate total revenue
                                processRevenueResultSet(rs);
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException("Failed to execute revenue report: " + e.getMessage(), e);
                        }
                        return null;
                    });
                }
            }

            // Show total revenue
            totalRevenueBox.setVisible(true);
            totalRevenueBox.setManaged(true);

            AlertDialog.showSuccess("Report Generated",
                    isFullYear ? "Annual revenue report generated successfully"
                            : "Revenue report generated successfully");
        } catch (NumberFormatException e) {
            AlertDialog.showWarning("Input Error", "Please enter valid year");
        } catch (Exception e) {
            AlertDialog.showError("Error", "Failed to generate revenue report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Add a helper method to process revenue report result sets
    private void processRevenueResultSet(ResultSet rs) throws SQLException {
        // First, save all data from the result set to a local collection
        ObservableList<ObservableList<Object>> allRows = FXCollections.observableArrayList();
        double totalRev = 0.0;
        double owedAmount = 0.0; 

        // Read all rows and calculate total revenue
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Find the index of the total revenue column
        int revenueColumnIndex = -1;
        int statusColumnIndex = -1;
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            if ("TOTAL_REVENUE".equalsIgnoreCase(columnName)) {
                revenueColumnIndex = i;
            } else if ("STATUS_ID".equalsIgnoreCase(columnName)) {
                statusColumnIndex = i;
            }
        }

        while (rs.next()) {
            // Add the current row to the collection
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
            allRows.add(row);

            // Calculate total revenue
            if (revenueColumnIndex > 0) {
                try {
                    double revenue = rs.getDouble(revenueColumnIndex);
                    totalRev += revenue;


                    if (statusColumnIndex > 0) {
                        String status = rs.getString(statusColumnIndex);
                        if ("C".equals(status) && !"P".equals(status)) {
                            owedAmount += revenue; 
                        }
                    } else {
   
                        owedAmount += revenue;
                    }
                } catch (SQLException ex) {
                    // Handle the case where the field is not found or the value conversion fails
                }
            }
        }

        // Set total revenue
        totalRevenueField.setText(String.format("$%.2f", totalRev));
        
        // Set owed amount (only displayed in invoice reports)
        owedAmountField.setText(String.format("$%.2f", owedAmount));
        
        populateTableWithData(rs.getMetaData(), allRows);
    }

    private void populateTableWithData(ResultSetMetaData metaData, ObservableList<ObservableList<Object>> data)
            throws SQLException {
        // Clear the table
        clearTableView();

        int columnCount = metaData.getColumnCount();

        // Create table columns
        for (int i = 1; i <= columnCount; i++) {
            final int columnIndex = i - 1;
            String columnName = metaData.getColumnName(i);

            TableColumn<ObservableList<Object>, Object> column = new TableColumn<>(formatColumnName(columnName));
            column.setCellValueFactory(cellData -> {
                ObservableList<Object> row = cellData.getValue();
                if (columnIndex >= row.size()) {
                    return new SimpleObjectProperty<>("");
                }
                Object value = row.get(columnIndex);
                if (value == null) {
                    return new SimpleObjectProperty<>("");
                }

                // Special handling for service type column - support multiple column names
                if (columnName.equalsIgnoreCase("SERVICE_TYPE_ID") ||
                        columnName.equalsIgnoreCase("SERVICE_CATEGORY") ||
                        columnName.equalsIgnoreCase("SERVICE_TYPE")) {
                    return new SimpleObjectProperty<>(getServiceTypeName(value.toString()));
                }

                return new SimpleObjectProperty<>(value);
            });

            reportTableView.getColumns().add(column);
        }

        reportTableView.setItems(data);
        
        boolean isInvoiceReport = "Invoice Report".equals(reportTypeComboBox.getValue());
        if (isInvoiceReport) {
            owedAmountBox.setVisible(true);
            owedAmountBox.setManaged(true);
            totalRevenueBox.setVisible(false);
            totalRevenueBox.setManaged(false);
        } else {
            totalRevenueBox.setVisible(true);
            totalRevenueBox.setManaged(true);
            owedAmountBox.setVisible(false);
            owedAmountBox.setManaged(false);
        }
    }

    // Update getMonthNumber method to handle "All Year" option
    private int getMonthNumber(String monthName) {
        if ("All Year".equals(monthName)) {
            return 0; // Use 0 to indicate full year
        }
        return monthNameToNumber.getOrDefault(monthName, 1);
    }

    private void generateInvoiceReport() {
        try {
            String invoiceId = invoiceIdField.getText().trim();

            // If an invoice ID is specified, query the specific invoice directly
            if (!invoiceId.isEmpty()) {
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

                // Check if a customer is selected
                Customer selectedCustomer = customerComboBox.getValue();
                if (selectedCustomer == null) {
                    AlertDialog.showWarning("Input Error", "Please select a customer for invoice report");
                    return;
                }

                final int customerId = selectedCustomer.getCustomerId();
                String statusValue = statusComboBox.getValue();

                // Convert status code
                String statusCode = null;
                if (statusValue != null && !statusValue.equals("All")) {
                    if ("Scheduled".equals(statusValue))
                        statusCode = "S";
                    else if ("In Progress".equals(statusValue))
                        statusCode = "I";
                    else if ("Completed".equals(statusValue))
                        statusCode = "C";
                    else if ("Canceled".equals(statusValue))
                        statusCode = "X";
                }

                // If a specific status is selected
                if (statusCode != null) {
                    final String finalStatusCode = statusCode;

                    HibernateUtil.callFunction(conn -> {
                        try (CallableStatement stmt = conn
                                .prepareCall(
                                        "{ ? = call report_pkg.fn_invoices_by_customer_and_status(?, ?, ?, ?) }")) {
                            stmt.registerOutParameter(1, OracleTypes.CURSOR);
                            stmt.setInt(2, customerId);
                            stmt.setString(3, finalStatusCode);
                            stmt.setDate(4, java.sql.Date.valueOf(startDate));
                            stmt.setDate(5, java.sql.Date.valueOf(endDate));
                            stmt.execute();

                            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                                populateTableFromResultSet(rs);
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(
                                    "Failed to execute invoice report by customer and status: " + e.getMessage(),
                                    e);
                        }
                        return null;
                    });
                }
                // Otherwise, query invoices for all statuses
                else {
                    HibernateUtil.callFunction(conn -> {
                        try (CallableStatement stmt = conn
                                .prepareCall("{ ? = call report_pkg.fn_invoices_by_customer(?, ?, ?) }")) {
                            stmt.registerOutParameter(1, OracleTypes.CURSOR);
                            stmt.setInt(2, customerId);
                            stmt.setDate(3, java.sql.Date.valueOf(startDate));
                            stmt.setDate(4, java.sql.Date.valueOf(endDate));
                            stmt.execute();

                            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                                populateTableFromResultSet(rs);
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(
                                    "Failed to execute invoice report by customer: " + e.getMessage(), e);
                        }
                        return null;
                    });
                }

                AlertDialog.showSuccess("Report Generated", "Invoice report generated successfully for customer: "
                        + selectedCustomer.getName());
            }
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
            AlertDialog.showWarning("警告", "沒有可匯出的報表數據");
            return;
        }

        try {
            String reportType = reportTypeComboBox.getValue();
            
            String defaultFileName = ReportExportService.getDefaultFileName(reportType);
            
            File file = ReportExportService.showSaveDialog(reportTableView, defaultFileName);
            
            if (file != null) {
                ReportExportService.exportReportToPdf(
                    file,
                    reportType,
                    reportTableView,
                    totalRevenueField.getText(),
                    owedAmountField.getText(),
                    totalRevenueBox.isVisible(),
                    owedAmountBox.isVisible(),
                    customerSummaryBox
                );
            }
        } catch (Exception e) {
            AlertDialog.showError("Export Error", "Failed to export PDF report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear() {
        reportTypeComboBox.setValue(null);
        revenueOptionsPane.setVisible(false);
        revenueOptionsPane.setManaged(false);
        invoiceOptionsPane.setVisible(false);
        invoiceOptionsPane.setManaged(false);
        totalRevenueBox.setVisible(false);
        totalRevenueBox.setManaged(false);

        yearField.setText(String.valueOf(LocalDate.now().getYear()));
        monthComboBox.setValue("January");
        serviceTypeComboBox.setValue(null);

        invoiceIdField.clear();
        customerComboBox.setValue(null);
        statusComboBox.setValue("All");
        totalRevenueField.clear();

        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());

        clearTableView();

        saveReportButton.setDisable(true);
    }

    private void clearTableView() {
        reportTableView.getColumns().clear();
        reportTableView.getItems().clear();

        if (customerSummaryBox != null) {
            customerSummaryBox.setVisible(false);
            customerSummaryBox.setManaged(false);
        }
        
    }

    // Modify populateTableFromResultSet method to enhance error handling and data
    // type conversion security
    private void populateTableFromResultSet(ResultSet rs) throws SQLException {
        // Clear the table
        clearTableView();

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Store customer information to display in the summary
        String customerName = "";
        String customerPhone = "";
        String customerEmail = "";
        boolean isInvoiceReport = "Invoice Report".equals(reportTypeComboBox.getValue());


        if (isInvoiceReport) {
            owedAmountBox.setVisible(true);
            owedAmountBox.setManaged(true);
            totalRevenueBox.setVisible(false);
            totalRevenueBox.setManaged(false);
        } else {
            totalRevenueBox.setVisible(true);
            totalRevenueBox.setManaged(true);
            owedAmountBox.setVisible(false);
            owedAmountBox.setManaged(false);
        }

        // Column names to hide (for invoice reports)
        Set<String> hiddenColumns = isInvoiceReport ? new HashSet<>(Arrays.asList(
                "CUSTOMER_NAME",
                "CUSTOMER_PHONE",
                "CUSTOMER_EMAIL",
                "SERVICE_CATEGORY",
                "STATUS_ID")) : new HashSet<>();

        // Create table columns
        for (int i = 1; i <= columnCount; i++) {
            final int columnIndex = i - 1;
            String columnName = metaData.getColumnName(i);

            // Skip columns that need to be hidden
            if (hiddenColumns.contains(columnName.toUpperCase())) {
                continue;
            }

            TableColumn<ObservableList<Object>, Object> column = new TableColumn<>(formatColumnName(columnName));
            column.setCellValueFactory(cellData -> {
                ObservableList<Object> row = cellData.getValue();
                if (columnIndex >= row.size()) {
                    return new SimpleObjectProperty<>("");
                }
                Object value = row.get(columnIndex);
                if (value == null) {
                    return new SimpleObjectProperty<>("");
                }

                // Special handling for service type columns
                if (columnName.equalsIgnoreCase("SERVICE_TYPE_ID") ||
                        columnName.equalsIgnoreCase("SERVICE_CATEGORY") ||
                        columnName.equalsIgnoreCase("SERVICE_TYPE")) {
                    return new SimpleObjectProperty<>(getServiceTypeName(value.toString()));
                }

                return new SimpleObjectProperty<>(value);
            });

            reportTableView.getColumns().add(column);
        }

        // Add data rows
        ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();
        double owedAmount = 0.0;
        boolean hasStatusColumn = false;
        int priceColumnIndex = -1;
        int statusColumnIndex = -1;
        
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnName = metaData.getColumnName(i);
            if ("SERVICE_PRICE".equalsIgnoreCase(columnName)) {
                priceColumnIndex = i;
            } else if ("STATUS_ID".equalsIgnoreCase(columnName)) {
                statusColumnIndex = i;
                hasStatusColumn = true;
            }
        }
        while (rs.next()) {
            ObservableList<Object> row = FXCollections.observableArrayList();
            for (int i = 1; i <= columnCount; i++) {
                try {
                    Object value = rs.getObject(i);
                    row.add(value);

                    // 如果是發票報表，計算欠款總額
                    if (isInvoiceReport && hasStatusColumn && i == priceColumnIndex) {
                        try {
                            double price = rs.getDouble(priceColumnIndex);
                            String status = rs.getString(statusColumnIndex);
                            // 只累計已完成但未付款的服務金額
                            if ("C".equals(status) && !"P".equals(status)) {
                                owedAmount += price;
                            }
                        } catch (Exception e) {
                        }
                    }
                    
                    if (isInvoiceReport) {
                        String columnName = metaData.getColumnName(i);
                        if (columnName.equalsIgnoreCase("CUSTOMER_NAME") && value != null) {
                            customerName = value.toString();
                        } else if (columnName.equalsIgnoreCase("CUSTOMER_PHONE") && value != null) {
                            customerPhone = value.toString();
                        } else if (columnName.equalsIgnoreCase("CUSTOMER_EMAIL") && value != null) {
                            customerEmail = value.toString();
                        }
                    }
                } catch (SQLException e) {
                    // If getting the object fails, try getting the string
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
        
        if (isInvoiceReport) {
            owedAmountField.setText(String.format("$%.2f", owedAmount));
            owedAmountBox.setVisible(true);
            owedAmountBox.setManaged(true);
            
            totalRevenueBox.setVisible(false);
            totalRevenueBox.setManaged(false);
            
            // For invoice reports, display customer information summary
            if (isInvoiceReport && !data.isEmpty()) {
                // Create an information panel to display customer information
                if (customerSummaryBox == null) {
                    customerSummaryBox = new VBox();
                    customerSummaryBox.setSpacing(5);
                    customerSummaryBox.setPadding(new Insets(10, 0, 10, 0));
                    customerSummaryBox.setStyle(
                            "-fx-border-color: #cccccc; -fx-border-width: 1px; -fx-background-color: #f8f8f8; -fx-padding: 10px;");
    
                    // Add to the interface - located above the table
                    BorderPane parent = (BorderPane) reportTableView.getParent();
                    parent.setTop(customerSummaryBox);
                } else {
                    customerSummaryBox.getChildren().clear();
                    customerSummaryBox.setVisible(true);
                    customerSummaryBox.setManaged(true);
                }
    
                // Add customer information
                Label nameLabel = new Label("Customer: " + customerName);
                nameLabel.setStyle("-fx-font-weight: bold;");
                customerSummaryBox.getChildren().add(nameLabel);
    
                if (!customerPhone.isEmpty()) {
                    Label phoneLabel = new Label("Phone: " + formatPhoneNumber(customerPhone));
                    customerSummaryBox.getChildren().add(phoneLabel);
                }
    
                if (!customerEmail.isEmpty()) {
                    Label emailLabel = new Label("Email: " + customerEmail);
                    customerSummaryBox.getChildren().add(emailLabel);
                }
            } else if (customerSummaryBox != null) {
                customerSummaryBox.setVisible(false);
                customerSummaryBox.setManaged(false);
            }
        } else {
            // 收入報表不顯示欠款框
            owedAmountBox.setVisible(false);
            owedAmountBox.setManaged(false);
        }
    }

    // Format column names to make them more readable
    private String formatColumnName(String columnName) {
        if (columnName == null || columnName.isEmpty()) {
            return columnName;
        }

        // Replace underscores with spaces and apply title case
        String[] words = columnName.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    // Format phone number
    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }

        // Keep only numbers
        String digitsOnly = phone.replaceAll("[^0-9]", "");

        // Format to (123) 456-7890 or keep original format
        if (digitsOnly.length() == 10) {
            return String.format("(%s) %s-%s",
                    digitsOnly.substring(0, 3),
                    digitsOnly.substring(3, 6),
                    digitsOnly.substring(6));
        } else {
            return phone;
        }
    }

    private String getServiceTypeName(String serviceTypeId) {
        // Check if it is null
        if (serviceTypeId == null || serviceTypeId.trim().isEmpty()) {
            return "";
        }

        // Remove possible non-numeric characters and get the numeric part
        String cleanId = serviceTypeId.trim().replaceAll("[^0-9]", "");

        // If there are no numbers after cleaning, return the original value
        if (cleanId.isEmpty()) {
            return serviceTypeId;
        }

        // Use predefined mapping
        switch (cleanId) {
            case "1":
                return "Regular Maintenance";
            case "2":
                return "Engine Repair";
            case "3":
                return "Transmission Repair";
            case "4":
                return "Brake Service";
            case "5":
                return "Electrical Repair";
            case "6":
                return "Air Conditioning";
            case "7":
                return "Suspension Work";
            case "8":
                return "Wheel and Tire Service";
            case "9":
                return "Diagnostic Service";
            case "10":
                return "Exhaust System Repair";
            default:
                return "Unknown Type (" + serviceTypeId + ")";
        }
    }
}