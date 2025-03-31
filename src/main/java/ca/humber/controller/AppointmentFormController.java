package ca.humber.controller;

import ca.humber.dao.AppointmentDAO;
import ca.humber.dao.CustomerDAO;
import ca.humber.dao.MechanicDAO;
import ca.humber.dao.ServiceDAO;
import ca.humber.dao.VehicleDAO;
import ca.humber.model.Appointment;
import ca.humber.model.Customer;
import ca.humber.model.Mechanic;
import ca.humber.model.Service;
import ca.humber.model.Vehicle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AppointmentFormController implements Initializable {

    @FXML
    private ComboBox<Customer> customerComboBox;
    @FXML
    private ComboBox<Vehicle> vehicleComboBox;
    @FXML
    private ComboBox<Service> serviceComboBox;
    @FXML
    private ComboBox<Mechanic> mechanicComboBox;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private DatePicker appointmentDatePicker;
    @FXML
    private ComboBox<String> appointmentTimeComboBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private String mode = "add"; // Default mode is "add"
    private Appointment existingAppointment; // Holds the appointment being edited
    private AppointmentsTabController parentController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set the date picker to today
        appointmentDatePicker.setValue(LocalDate.now());

        // Set time options
        String[] times = { "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00" };
        appointmentTimeComboBox.setItems(FXCollections.observableArrayList(times));
        appointmentTimeComboBox.setValue("09:00");

        // Set status options
        String[] statuses = { "Pending", "In Progress", "Completed", "Cancelled" };
        statusComboBox.setItems(FXCollections.observableArrayList(statuses));
        statusComboBox.setValue("Pending");

        // Configure the display format of combo boxes
        setupComboBoxes();

        // Load data
        loadCustomers();

        // Set customer selection event - load vehicles when a customer is selected
        customerComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadVehicles(newSelection.getCustomerId());
            } else {
                vehicleComboBox.getItems().clear();
            }
        });

        // Set date selection event - load available times when a date is selected
        appointmentDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                loadAvailableTimes(newDate);
            }
        });

        // Load services and mechanics
        loadServices();
        loadMechanics();
    }

    private void setupComboBoxes() {
        // Configure customer combo box display format
        customerComboBox.setCellFactory(param -> new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        customerComboBox.setButtonCell(new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        // Configure vehicle combo box display format
        vehicleComboBox.setCellFactory(param -> new ListCell<Vehicle>() {
            @Override
            protected void updateItem(Vehicle item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getYear() + " " + item.getMake() + " " + item.getModel() + " (" + item.getVin() + ")");
                }
            }
        });

        vehicleComboBox.setButtonCell(new ListCell<Vehicle>() {
            @Override
            protected void updateItem(Vehicle item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getYear() + " " + item.getMake() + " " + item.getModel() + " (" + item.getVin() + ")");
                }
            }
        });

        // Configure service combo box display format
        serviceComboBox.setCellFactory(param -> new ListCell<Service>() {
            @Override
            protected void updateItem(Service item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getServiceName() + " ($" + item.getPrice() + ")");
                }
            }
        });

        serviceComboBox.setButtonCell(new ListCell<Service>() {
            @Override
            protected void updateItem(Service item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getServiceName() + " ($" + item.getPrice() + ")");
                }
            }
        });

        // Configure mechanic combo box display format
        mechanicComboBox.setCellFactory(param -> new ListCell<Mechanic>() {
            @Override
            protected void updateItem(Mechanic item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName()
                            + (item.getSpecialization() != null ? " (" + item.getSpecialization() + ")" : ""));
                }
            }
        });

        mechanicComboBox.setButtonCell(new ListCell<Mechanic>() {
            @Override
            protected void updateItem(Mechanic item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName()
                            + (item.getSpecialization() != null ? " (" + item.getSpecialization() + ")" : ""));
                }
            }
        });
    }

    // Load customer list
    private void loadCustomers() {
        try {
            List<Customer> customers = CustomerDAO.getActiveCustomers();
            customerComboBox.setItems(FXCollections.observableArrayList(customers));
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while loading customers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load vehicles for a specific customer
    private void loadVehicles(int customerId) {
        try {
            List<Vehicle> vehicles = VehicleDAO.getVehiclesByCustomerId(customerId);
            vehicleComboBox.setItems(FXCollections.observableArrayList(vehicles));
            // Automatically select if there is only one vehicle
            if (vehicles.size() == 1) {
                vehicleComboBox.setValue(vehicles.get(0));
            }
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while loading vehicles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load service list
    private void loadServices() {
        try {
            List<Service> services = ServiceDAO.getActiveServices();
            serviceComboBox.setItems(FXCollections.observableArrayList(services));
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while loading services: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load mechanic list
    private void loadMechanics() {
        try {
            List<Mechanic> mechanics = MechanicDAO.getActiveMechanics();
            mechanicComboBox.setItems(FXCollections.observableArrayList(mechanics));
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while loading mechanics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load available time slots for the selected date
    private void loadAvailableTimes(LocalDate date) {
        try {
            Date selectedDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Map<String, String> availableSlots = AppointmentDAO.getAvailableSlots(selectedDate);

            // Filter available time slots
            List<String> availableTimes = new java.util.ArrayList<>();
            for (Map.Entry<String, String> entry : availableSlots.entrySet()) {
                if ("AVAILABLE".equals(entry.getValue())) {
                    availableTimes.add(entry.getKey());
                }
            }

            // Update time combo box
            appointmentTimeComboBox.setItems(FXCollections.observableArrayList(availableTimes));
            if (!availableTimes.isEmpty()) {
                appointmentTimeComboBox.setValue(availableTimes.get(0));
            } else {
                appointmentTimeComboBox.setValue(null);
                AlertDialog.showWarning("No Available Slots",
                        "There are no available appointment slots for the selected date.");
            }
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while loading available times: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Set mode (add or edit)
    public void setMode(String mode) {
        this.mode = mode;
        if ("edit".equals(mode)) {
            saveButton.setText("Update");
        }
    }

    // Set the appointment to be edited
    public void setAppointment(Appointment appointment) {
        this.existingAppointment = appointment;

        // Populate the form
        Customer customer = appointment.getCustomer();
        if (customer != null) {
            for (Customer c : customerComboBox.getItems()) {
                if (c.getCustomerId() == customer.getCustomerId()) {
                    customerComboBox.setValue(c);
                    break;
                }
            }

            // Load vehicles
            loadVehicles(customer.getCustomerId());
            Vehicle vehicle = appointment.getVehicle();
            if (vehicle != null) {
                for (Vehicle v : vehicleComboBox.getItems()) {
                    if (v.getVehicleId() == vehicle.getVehicleId()) {
                        vehicleComboBox.setValue(v);
                        break;
                    }
                }
            }
        }

        Service service = appointment.getService();
        if (service != null) {
            for (Service s : serviceComboBox.getItems()) {
                if (s.getServiceId() == service.getServiceId()) {
                    serviceComboBox.setValue(s);
                    break;
                }
            }
        }

        Mechanic mechanic = appointment.getMechanic();
        if (mechanic != null) {
            for (Mechanic m : mechanicComboBox.getItems()) {
                if (m.getMechanicId() == mechanic.getMechanicId()) {
                    mechanicComboBox.setValue(m);
                    break;
                }
            }
        }

        Date appointmentDate = appointment.getAppointmentDate();
        if (appointmentDate != null) {
            LocalDate localDate = appointmentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            appointmentDatePicker.setValue(localDate);

            // Set time
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
            String time = sdf.format(appointmentDate);
            appointmentTimeComboBox.setValue(time);
        }

        // Set status
        String statusId = appointment.getStatusId();
        if (statusId != null) {
            switch (statusId) {
                case "S":  
                    statusComboBox.setValue("Scheduled");
                    break;
                case "I":
                    statusComboBox.setValue("In Progress");
                    break;
                case "C":
                    statusComboBox.setValue("Completed");
                    break;
                case "X":
                    statusComboBox.setValue("Cancelled");
                    break;
                default:
                    statusComboBox.setValue("Pending");
            }
        }
    }

    // Set parent controller
    public void setParentController(AppointmentsTabController controller) {
        this.parentController = controller;
    }

    // Save appointment
    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            // Retrieve form data
            Customer selectedCustomer = customerComboBox.getValue();
            Vehicle selectedVehicle = vehicleComboBox.getValue();
            Service selectedService = serviceComboBox.getValue();
            Mechanic selectedMechanic = mechanicComboBox.getValue();
            LocalDate selectedDate = appointmentDatePicker.getValue();
            String selectedTime = appointmentTimeComboBox.getValue();
            String selectedStatus = statusComboBox.getValue();

            // Convert status to status code
            String statusId;
            switch (selectedStatus) {
                case "In Progress":
                    statusId = "I";
                    break;
                case "Completed":
                    statusId = "C";
                    break;
                case "Cancelled":
                    statusId = "X";
                    break;
                default:
                    statusId = "S";  
            }

            // Combine date and time
            String[] timeParts = selectedTime.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth(), hour, minute, 0);
            Date appointmentDateTime = calendar.getTime();

            boolean success;

            if ("add".equals(mode)) {
                // Create a new appointment
                Appointment newAppointment = new Appointment(
                        selectedCustomer,
                        selectedVehicle,
                        selectedService,
                        selectedMechanic,
                        appointmentDateTime,
                        statusId);

                success = AppointmentDAO.createAppointment(newAppointment);
                if (success) {
                    AlertDialog.showSuccess("Success", "Appointment has been created successfully");
                } else {
                    AlertDialog.showError("Error", "Failed to create appointment. Please try again.");
                    return;
                }
            } else {
                // Update existing appointment
                existingAppointment.setCustomer(selectedCustomer);
                existingAppointment.setVehicle(selectedVehicle);
                existingAppointment.setService(selectedService);
                existingAppointment.setMechanic(selectedMechanic);
                existingAppointment.setAppointmentDate(appointmentDateTime);
                existingAppointment.setStatusId(statusId);

                success = AppointmentDAO.updateAppointment(existingAppointment);
                if (success) {
                    AlertDialog.showSuccess("Success", "Appointment has been updated successfully");
                } else {
                    AlertDialog.showError("Error", "Failed to update appointment. Please try again.");
                    return;
                }
            }

            // Close the form and refresh the list
            if (parentController != null) {
                parentController.refreshAppointments();
            }
            closeForm();
        } catch (Exception e) {
            AlertDialog.showError("Error", "An error occurred while saving the appointment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Validate input
    private boolean validateInput() {
        if (customerComboBox.getValue() == null) {
            AlertDialog.showWarning("Validation Error", "Please select a customer");
            customerComboBox.requestFocus();
            return false;
        }

        if (vehicleComboBox.getValue() == null) {
            AlertDialog.showWarning("Validation Error", "Please select a vehicle");
            vehicleComboBox.requestFocus();
            return false;
        }

        if (serviceComboBox.getValue() == null) {
            AlertDialog.showWarning("Validation Error", "Please select a service");
            serviceComboBox.requestFocus();
            return false;
        }

        if (appointmentDatePicker.getValue() == null) {
            AlertDialog.showWarning("Validation Error", "Please select a date");
            appointmentDatePicker.requestFocus();
            return false;
        }

        if (appointmentTimeComboBox.getValue() == null) {
            AlertDialog.showWarning("Validation Error", "Please select a time");
            appointmentTimeComboBox.requestFocus();
            return false;
        }

        if (statusComboBox.getValue() == null) {
            AlertDialog.showWarning("Validation Error", "Please select a status");
            statusComboBox.requestFocus();
            return false;
        }

        return true;
    }

    // Handle cancel button
    @FXML
    private void handleCancel() {
        closeForm();
    }

    // Close the form
    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
