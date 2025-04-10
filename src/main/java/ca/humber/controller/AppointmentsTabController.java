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

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

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

    private ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        loadAppointments();
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
}
