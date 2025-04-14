package ca.humber.controller;

import ca.humber.dao.AppointmentDAO;
import ca.humber.exceptions.ConstraintException;
import ca.humber.model.Appointment;
import ca.humber.model.Customer;
import ca.humber.model.Mechanic;
import ca.humber.model.Service;
import ca.humber.model.Vehicle;

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
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ArrayList;

public class AppointmentsTabController implements Initializable {

    @FXML
    private TableView<Appointment> appointmentTable;
    @FXML
    private TableColumn<Appointment, Integer> appointmentIdColumn;
    @FXML
    private TableColumn<Appointment, Customer> customerNameColumn;
    @FXML
    private TableColumn<Appointment, Vehicle> vehicleInfoColumn;
    @FXML
    private TableColumn<Appointment, Date> dateColumn;
    @FXML
    private TableColumn<Appointment, Date> timeColumn;
    @FXML
    private TableColumn<Appointment, Service> serviceTypeColumn;
    @FXML
    private TableColumn<Appointment, Mechanic> mechanicColumn; 
    @FXML
    private TableColumn<Appointment, String> statusColumn;
    @FXML
    private TextField appointmentSearchField;

    // 新增行事曆視圖相關的FXML元素
    @FXML private TabPane appointmentViewTabPane;
    @FXML private GridPane calendarGridPane;
    @FXML private Label monthYearLabel;
    @FXML private Button prevMonthButton;
    @FXML private Button nextMonthButton;
    
    // 新增ScrollPane來包裝GridPane
    private ScrollPane calendarScrollPane;

    // 行事曆狀態變數
    private LocalDate currentCalendarDate = LocalDate.now();
    private Map<LocalDate, List<Appointment>> appointmentsByDate = new HashMap<>();

    private ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        loadAppointments();
        
        // 初始化行事曆視圖
        setupCalendarView();
    }

    private void setupTableColumns() {
        appointmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));

        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customer"));
        customerNameColumn.setCellFactory(column -> new TableCell<Appointment, Customer>() {
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

        vehicleInfoColumn.setCellValueFactory(new PropertyValueFactory<>("vehicle"));
        vehicleInfoColumn.setCellFactory(column -> new TableCell<Appointment, Vehicle>() {
            @Override
            protected void updateItem(Vehicle vehicle, boolean empty) {
                super.updateItem(vehicle, empty);
                if (empty || vehicle == null) {
                    setText(null);
                } else {
                    setText(vehicle.getYear() + " " + vehicle.getMake() + " " + vehicle.getModel());
                }
            }
        });

        dateColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
        dateColumn.setCellFactory(column -> new TableCell<Appointment, Date>() {
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(dateFormat.format(date));
                }
            }
        });

        timeColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
        timeColumn.setCellFactory(column -> new TableCell<Appointment, Date>() {
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(timeFormat.format(date));
                }
            }
        });

        serviceTypeColumn.setCellValueFactory(new PropertyValueFactory<>("service"));
        serviceTypeColumn.setCellFactory(column -> new TableCell<Appointment, Service>() {
            @Override
            protected void updateItem(Service service, boolean empty) {
                super.updateItem(service, empty);
                if (empty || service == null) {
                    setText(null);
                } else {
                    setText(service.getServiceName());
                }
            }
        });

        mechanicColumn.setCellValueFactory(new PropertyValueFactory<>("mechanic"));
        mechanicColumn.setCellFactory(column -> new TableCell<Appointment, Mechanic>() {
            @Override
            protected void updateItem(Mechanic mechanic, boolean empty) {
                super.updateItem(mechanic, empty);
                if (empty || mechanic == null) {
                    setText(""); 
                } else {
                    setText(mechanic.getName() + 
                           (mechanic.getSpecialization() != null ? " (" + mechanic.getSpecialization() + ")" : ""));
                }
            }
        });

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusId"));
        statusColumn.setCellFactory(column -> new TableCell<Appointment, String>() {
            @Override
            protected void updateItem(String statusId, boolean empty) {
                super.updateItem(statusId, empty);
                if (empty || statusId == null) {
                    setText(null);
                } else {
                    switch (statusId) {
                        case "S":
                            setText("Scheduled");
                            break;
                        case "I":
                            setText("In Progress");
                            break;
                        case "C":
                            setText("Completed");
                            break;
                        case "X":
                            setText("Cancelled");
                            break;
                        case "P":
                            setText("Paid");
                            break;
                        default:
                            setText(statusId);
                    }
                }
            }
        });
    }

    private void loadAppointments() {
        appointmentList.clear();
        List<Appointment> appointments = AppointmentDAO.getAllAppointments();
        appointmentList.addAll(appointments);
        appointmentTable.setItems(appointmentList);
        
        // 如果行事曆視圖已初始化，則更新
        if (calendarGridPane != null) {
            updateCalendarView();
        }
    }

    @FXML
    private void handleAppointmentSearch() {
        String searchTerm = appointmentSearchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadAppointments();
            return;
        }

        appointmentList.clear();
        List<Appointment> searchResults = AppointmentDAO.searchAppointments(searchTerm);
        appointmentList.addAll(searchResults);
        appointmentTable.setItems(appointmentList);
    }

    @FXML
    private void handleAddAppointment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/appointment_form.fxml"));
            Parent root = loader.load();

            AppointmentFormController controller = loader.getController();
            controller.setMode("add");
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Appointment");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            AlertDialog.showError("Error", "Unable to open Add Appointment window: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditAppointment() {
        Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();
        if (selectedAppointment == null) {
            AlertDialog.showWarning("Edit Appointment", "Please select an appointment first");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/appointment_form.fxml"));
            Parent root = loader.load();

            AppointmentFormController controller = loader.getController();
            controller.setMode("edit");
            controller.setAppointment(selectedAppointment);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Appointment");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            AlertDialog.showError("Error", "Unable to open Edit Appointment window: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteAppointment() {
        Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();
        if (selectedAppointment == null) {
            AlertDialog.showWarning("Delete Appointment", "Please select an appointment first");
            return;
        }

        boolean confirm = AlertDialog.showConfirmation("Delete Appointment",
                "Are you sure you want to delete appointment #" + selectedAppointment.getAppointmentId()
                        + "? This action cannot be undone.");

        if (confirm) {
            try {
                boolean deleted = AppointmentDAO.deleteAppointment(selectedAppointment.getAppointmentId());
                if (deleted) {
                    AlertDialog.showSuccess("Delete Appointment", "Appointment successfully deleted");
                    loadAppointments();
                } else {
                    AlertDialog.showError("Delete Appointment", "Unable to delete appointment, please try again later");
                }
            } catch (ConstraintException e) {
                AlertDialog.showError("Delete Appointment", e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefreshAppointment() {
        loadAppointments();
        AlertDialog.showSuccess("Refresh", "Appointment list updated");
    }

    public void refreshAppointments() {
        loadAppointments();
    }

    /**
     * Set up the calendar view
     */
    private void setupCalendarView() {
        // 創建ScrollPane來包裝GridPane
        if (calendarScrollPane == null) {
            calendarScrollPane = new ScrollPane();
            calendarScrollPane.setFitToWidth(true);
            calendarScrollPane.setContent(calendarGridPane);
            
            // 找到GridPane的父容器並用ScrollPane替換
            if (calendarGridPane.getParent() instanceof VBox) {
                VBox parent = (VBox) calendarGridPane.getParent();
                int index = parent.getChildren().indexOf(calendarGridPane);
                if (index >= 0) {
                    parent.getChildren().remove(calendarGridPane);
                    parent.getChildren().add(index, calendarScrollPane);
                }
            }
        }
        
        // Set up month navigation buttons
        prevMonthButton.setOnAction(e -> {
            currentCalendarDate = currentCalendarDate.minusMonths(1);
            updateCalendarView();
        });
        
        nextMonthButton.setOnAction(e -> {
            currentCalendarDate = currentCalendarDate.plusMonths(1);
            updateCalendarView();
        });
        
        // Initialize the calendar view
        updateCalendarView();
    }

    /**
     * Update the calendar view based on the currently selected month
     */
    private void updateCalendarView() {
        // Clear existing content
        calendarGridPane.getChildren().clear();
        calendarGridPane.getRowConstraints().clear();
        calendarGridPane.getColumnConstraints().clear();
        
        // 為了確保ScrollPane能正常工作，設置GridPane的最小寬度
        calendarGridPane.setMinWidth(700);  // 根據需要調整此值
        
        // Set the month title
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM");
        monthYearLabel.setText(currentCalendarDate.format(formatter));
        
        // Set up grid columns
        for (int i = 0; i < 7; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(100.0 / 7);
            colConstraints.setHgrow(Priority.SOMETIMES);
            calendarGridPane.getColumnConstraints().add(colConstraints);
        }
        
        // Add weekday headers
        String[] weekdays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(weekdays[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-alignment: center;");
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setAlignment(Pos.CENTER);
            calendarGridPane.add(dayLabel, i, 0);
        }
        
        // Calculate the first and last day of the month
        LocalDate firstDayOfMonth = currentCalendarDate.withDayOfMonth(1);
        int monthLength = currentCalendarDate.lengthOfMonth();
        
        // Calculate the starting date for the calendar (may be a day from the previous month)
        LocalDate calendarStartDate = firstDayOfMonth.minusDays(firstDayOfMonth.getDayOfWeek().getValue() % 7);
        
        // Group appointments by date
        organizeAppointmentsByDate();
        
        // Fill the calendar grid
        int row = 1; // Start from the second row (first row is weekday headers)
        for (int i = 0; i < 6; i++) { // Up to 6 rows are needed to display a month
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setVgrow(Priority.SOMETIMES);
            rowConstraints.setMinHeight(80);
            calendarGridPane.getRowConstraints().add(rowConstraints);
            
            for (int j = 0; j < 7; j++) {
                LocalDate date = calendarStartDate.plusDays(i * 7 + j);
                
                // Create a date cell
                VBox dayCell = createDayCell(date);
                
                // Add to the grid
                calendarGridPane.add(dayCell, j, row);
            }
            row++;
        }
    }

    /**
     * Create a single date cell
     */
    private VBox createDayCell(LocalDate date) {
        VBox dayCell = new VBox(5);
        dayCell.setPadding(new Insets(5));
        dayCell.setMinHeight(80);
        
        // Date label
        Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));
        
        // Style settings
        dayCell.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0.5px;");
        
        // Dim the display if it's not a date in the current month
        if (date.getMonthValue() != currentCalendarDate.getMonthValue()) {
            dayCell.setStyle(dayCell.getStyle() + "-fx-background-color: #f0f0f0;");
            dateLabel.setStyle("-fx-text-fill: #999999;");
        }
        
        // Highlight if it's today
        if (date.equals(LocalDate.now())) {
            dayCell.setStyle(dayCell.getStyle() + "-fx-background-color: #e6f7ff;");
            dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a8cff;");
        }
        
        dayCell.getChildren().add(dateLabel);
        
        // Add appointments for the day
        List<Appointment> dayAppointments = appointmentsByDate.get(date);
        if (dayAppointments != null && !dayAppointments.isEmpty()) {
            // Display a maximum of 3 appointments, and "+ more" if there are more
            int displayCount = Math.min(dayAppointments.size(), 3);
            
            for (int i = 0; i < displayCount; i++) {
                Appointment appointment = dayAppointments.get(i);
                
                // Create an appointment display card
                HBox appointmentBox = createAppointmentBox(appointment);
                dayCell.getChildren().add(appointmentBox);
            }
            
            // If there are more appointments, display a "+ more" prompt
            if (dayAppointments.size() > 3) {
                Hyperlink moreLink = new Hyperlink("+" + (dayAppointments.size() - 3) + " more");
                moreLink.setOnAction(e -> showDayAppointments(date, dayAppointments));
                dayCell.getChildren().add(moreLink);
            }
        }
        
        // Add a click event handler to display all appointments for the day
        dayCell.setOnMouseClicked(e -> {
            if (dayAppointments != null && !dayAppointments.isEmpty()) {
                showDayAppointments(date, dayAppointments);
            } else if (date.getMonthValue() == currentCalendarDate.getMonthValue()) {
                // If it's a date in the current month but there are no appointments, provide an option to add an appointment
                boolean addNew = AlertDialog.showConfirmation("Add Appointment", 
                        "No appointments found for " + date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "\n" +
                        "Do you want to add an appointment?");
                if (addNew) {
                    openAddAppointmentForm(date);
                }
            }
        });
        
        return dayCell;
    }

    /**
     * Create a display card for a single appointment
     */
    private HBox createAppointmentBox(Appointment appointment) {
        HBox appointmentBox = new HBox(5);
        appointmentBox.setPadding(new Insets(2, 4, 2, 4));
        
        // Set different background colors based on the appointment status
        String statusColor;
        switch (appointment.getStatusId()) {
            case "S": // Scheduled
                statusColor = "#e6f7ff"; // Light blue
                break;
            case "I": // In Progress
                statusColor = "#fff7e6"; // Light orange
                break;
            case "C": // Completed
                statusColor = "#f6ffed"; // Light green
                break;
            case "P": // Paid
                statusColor = "#e6f5ff"; // Lavender
                break;
            case "X": // Cancelled
                statusColor = "#fff1f0"; // Light red
                break;
            default:
                statusColor = "#f0f0f0"; // Light gray
        }
        
        appointmentBox.setStyle(
                "-fx-background-color: " + statusColor + ";" +
                "-fx-border-color: #dddddd;" +
                "-fx-border-radius: 3px;" +
                "-fx-background-radius: 3px;"
        );
        
        // Time label
        String timeStr = timeFormat.format(appointment.getAppointmentDate());
        Label timeLabel = new Label(timeStr);
        timeLabel.setStyle("-fx-font-weight: bold;");
        
        // Abbreviated customer name (display a maximum of 8 characters)
        String customerName = appointment.getCustomer().getName();
        if (customerName.length() > 8) {
            customerName = customerName.substring(0, 7) + "..";
        }
        Label customerLabel = new Label(customerName);
        
        appointmentBox.getChildren().addAll(timeLabel, customerLabel);
        
        // Add a click event to display appointment details
        appointmentBox.setOnMouseClicked(e -> {
            e.consume(); // Prevent the click event from propagating to the date cell
            showAppointmentDetails(appointment);
        });
        
        return appointmentBox;
    }

    /**
     * Display detailed information for a single appointment
     */
    private void showAppointmentDetails(Appointment appointment) {
        // Select the appointment in the table
        appointmentTable.getSelectionModel().select(appointment);
        
        // Switch to the table view tab
        appointmentViewTabPane.getSelectionModel().select(0);
        
        // Highlight the selected row
        appointmentTable.scrollTo(appointment);
    }

    /**
     * Display all appointments for a specific day
     */
    private void showDayAppointments(LocalDate date, List<Appointment> appointments) {
        // Create a pop-up dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + " Appointments");
        
        // Create an appointment list
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        
        // Add a title
        Label titleLabel = new Label(date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + " Appointments");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        content.getChildren().add(titleLabel);
        
        // Add each appointment item
        for (Appointment appointment : appointments) {
            HBox appointmentRow = new HBox(15);
            appointmentRow.setPadding(new Insets(10));
            appointmentRow.setStyle("-fx-border-color: #eeeeee; -fx-border-width: 0 0 1 0;");
            
            // Time
            Label timeLabel = new Label(timeFormat.format(appointment.getAppointmentDate()));
            timeLabel.setMinWidth(60);
            
            // Customer and vehicle information
            VBox infoBox = new VBox(5);
            Label customerLabel = new Label(appointment.getCustomer().getName());
            customerLabel.setStyle("-fx-font-weight: bold;");
            Label vehicleLabel = new Label(
                appointment.getVehicle().getYear() + " " + 
                appointment.getVehicle().getMake() + " " + 
                appointment.getVehicle().getModel()
            );
            infoBox.getChildren().addAll(customerLabel, vehicleLabel);
            
            // Service type
            Label serviceLabel = new Label(appointment.getService().getServiceName());
            serviceLabel.setMinWidth(120);
            
            // Status
            String status;
            switch (appointment.getStatusId()) {
                case "S": status = "Scheduled"; break;
                case "I": status = "In Progress"; break;
                case "C": status = "Completed"; break;
                case "P": status = "Paid"; break;
                case "X": status = "Cancelled"; break;
                default: status = "Unknown";
            }
            Label statusLabel = new Label(status);
            
            // Add a view button
            Button viewButton = new Button("View");
            viewButton.setOnAction(e -> {
                dialog.close();
                showAppointmentDetails(appointment);
            });
            
            appointmentRow.getChildren().addAll(timeLabel, infoBox, serviceLabel, statusLabel, viewButton);
            HBox.setHgrow(infoBox, Priority.ALWAYS);
            
            content.getChildren().add(appointmentRow);
        }
        
        // Add a close button
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> dialog.close());
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(closeButton);
        content.getChildren().add(buttonBox);
        
        // 創建一個 ScrollPane 並將 content 放入其中
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);  // 設置一個固定的高度
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        // 顯示對話框
        Scene scene = new Scene(scrollPane, 600, 500);  // 使用固定大小，並使用 ScrollPane 處理溢出
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * Open the add appointment form with the selected date
     */
    private void openAddAppointmentForm(LocalDate date) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/appointment_form.fxml"));
            Parent root = loader.load();

            AppointmentFormController controller = loader.getController();
            controller.setMode("add");
            controller.setParentController(this);
            
            // Set the preselected date
            controller.setPreselectedDate(date);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Appointment");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            AlertDialog.showError("Error", "Unable to open Add Appointment window: " + e.getMessage());
        }
    }

    /**
     * Group appointments by date
     */
    private void organizeAppointmentsByDate() {
        appointmentsByDate.clear();
        
        for (Appointment appointment : appointmentList) {
            Date appointmentDate = appointment.getAppointmentDate();
            if (appointmentDate != null) {
                LocalDate localDate = appointmentDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
                
                appointmentsByDate.computeIfAbsent(localDate, k -> new ArrayList<>())
                    .add(appointment);
            }
        }
    }
}
