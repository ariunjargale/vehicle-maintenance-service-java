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

    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private ComboBox<Service> serviceComboBox;
    @FXML private ComboBox<Mechanic> mechanicComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private DatePicker appointmentDatePicker;
    @FXML private ComboBox<String> appointmentTimeComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private String mode = "add";
    private Appointment existingAppointment;
    private AppointmentsTabController parentController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        appointmentDatePicker.setValue(LocalDate.now());

        String[] times = { "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00" };
        appointmentTimeComboBox.setItems(FXCollections.observableArrayList(times));
        appointmentTimeComboBox.setValue("09:00");

        String[] statuses = { "Scheduled", "In Progress", "Completed", "Cancelled" };
        statusComboBox.setItems(FXCollections.observableArrayList(statuses));
        statusComboBox.setValue("Scheduled");

        mechanicComboBox.setPromptText("Select a mechanic (optional)");

        setupComboBoxes();

        // Load all dropdown data
        loadCustomers(); // ✅ FIXED: This was missing before
        loadServices();
        loadMechanics();

        // Event listener: load vehicles when customer changes
        customerComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadVehicles(newSelection.getCustomerId());
            } else {
                vehicleComboBox.getItems().clear();
            }
        });

        // Event listener: update available times when date changes
        appointmentDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                loadAvailableTimes(newDate);
            }
        });
    }

    private void setupComboBoxes() {
        customerComboBox.setCellFactory(param -> new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });
        customerComboBox.setButtonCell(new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });

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

        serviceComboBox.setCellFactory(param -> new ListCell<Service>() {
            @Override
            protected void updateItem(Service item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getServiceName() + " ($" + item.getPrice() + ")");
            }
        });
        serviceComboBox.setButtonCell(new ListCell<Service>() {
            @Override
            protected void updateItem(Service item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getServiceName() + " ($" + item.getPrice() + ")");
            }
        });

        mechanicComboBox.setCellFactory(param -> new ListCell<Mechanic>() {
            @Override
            protected void updateItem(Mechanic item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + (item.getSpecialization() != null ? " (" + item.getSpecialization() + ")" : ""));
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
                    setText(item.getName() + (item.getSpecialization() != null ? " (" + item.getSpecialization() + ")" : ""));
                }
            }
        });
    }

    private void loadCustomers() {
        try {
            List<Customer> customers = CustomerDAO.getActiveCustomers();
            customerComboBox.setItems(FXCollections.observableArrayList(customers));
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while loading customers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadVehicles(int customerId) {
        try {
            List<Vehicle> vehicles = VehicleDAO.getVehiclesByCustomerId(customerId);
            vehicleComboBox.setItems(FXCollections.observableArrayList(vehicles));

            if (vehicles.size() == 1) {
                vehicleComboBox.setValue(vehicles.get(0));
            }
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while loading vehicles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadServices() {
        try {
            List<Service> services = ServiceDAO.getActiveServices();
            serviceComboBox.setItems(FXCollections.observableArrayList(services));
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while loading services: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMechanics() {
        try {
            List<Mechanic> mechanics = MechanicDAO.getActiveMechanics();
            mechanicComboBox.setItems(FXCollections.observableArrayList(mechanics));
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while loading mechanics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAvailableTimes(LocalDate date) {
        try {
            Date selectedDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Map<String, String> availableSlots = AppointmentDAO.getAvailableSlots(selectedDate);

            List<String> availableTimes = new java.util.ArrayList<>();
            for (Map.Entry<String, String> entry : availableSlots.entrySet()) {
                if ("AVAILABLE".equals(entry.getValue())) {
                    availableTimes.add(entry.getKey());
                }
            }

            appointmentTimeComboBox.setItems(FXCollections.observableArrayList(availableTimes));
            if (!availableTimes.isEmpty()) {
                appointmentTimeComboBox.setValue(availableTimes.get(0));
            } else {
                appointmentTimeComboBox.setValue(null);
                AlertDialog.showWarning("No Available Slots", "There are no available appointment slots for the selected date.");
            }
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while loading available times: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setMode(String mode) {
        this.mode = mode;
        if ("edit".equals(mode)) {
            saveButton.setText("Update");
        }
    }

    public void setAppointment(Appointment appointment) {
        this.existingAppointment = appointment;

        Customer customer = appointment.getCustomer();
        if (customer != null) {
            for (Customer c : customerComboBox.getItems()) {
                if (c.getCustomerId() == customer.getCustomerId()) {
                    customerComboBox.setValue(c);
                    break;
                }
            }

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

        if (appointment.getAppointmentDate() != null) {
            LocalDate localDate = appointment.getAppointmentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            appointmentDatePicker.setValue(localDate);

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
            String time = sdf.format(appointment.getAppointmentDate());
            appointmentTimeComboBox.setValue(time);
        }

        String statusId = appointment.getStatusId();
        switch (statusId) {
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
                statusComboBox.setValue("Scheduled");
        }
    }

    public void setParentController(AppointmentsTabController controller) {
        this.parentController = controller;
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;

        try {
            Customer selectedCustomer = customerComboBox.getValue();
            Vehicle selectedVehicle = vehicleComboBox.getValue();
            Service selectedService = serviceComboBox.getValue();
            Mechanic selectedMechanic = mechanicComboBox.getValue();
            LocalDate selectedDate = appointmentDatePicker.getValue();
            String selectedTime = appointmentTimeComboBox.getValue();
            String statusId = switch (statusComboBox.getValue()) {
                case "In Progress" -> "I";
                case "Completed" -> "C";
                case "Cancelled" -> "X";
                default -> "S";
            };

            String[] timeParts = selectedTime.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth(), hour, minute, 0);
            Date appointmentDateTime = calendar.getTime();

            boolean success;
            if ("add".equals(mode)) {
                Appointment newAppointment = new Appointment(selectedCustomer, selectedVehicle, selectedService, selectedMechanic, appointmentDateTime, statusId);
                success = AppointmentDAO.createAppointment(newAppointment);
                AlertDialog.showSuccess("Success", "Appointment created successfully.");
            } else {
                existingAppointment.setCustomer(selectedCustomer);
                existingAppointment.setVehicle(selectedVehicle);
                existingAppointment.setService(selectedService);
                existingAppointment.setMechanic(selectedMechanic);
                existingAppointment.setAppointmentDate(appointmentDateTime);
                existingAppointment.setStatusId(statusId);
                success = AppointmentDAO.updateAppointment(existingAppointment);
                AlertDialog.showSuccess("Success", "Appointment updated successfully.");
            }

            if (!success) {
                AlertDialog.showError("Error", "Failed to save appointment.");
                return;
            }

            if (parentController != null) {
                parentController.refreshAppointments();
            }

            closeForm();
        } catch (Exception e) {
            AlertDialog.showError("Error", "An error occurred while saving: " + e.getMessage());
            e.printStackTrace();
        }
    }

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

    @FXML
    private void handleCancel() {
        closeForm();
    }

    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
