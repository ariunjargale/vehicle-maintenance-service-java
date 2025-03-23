package ca.humber.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AppointmentsTabController {
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
    private TextField appointmentSearchField;

    @FXML
    private void handleAppointmentSearch() {
        AlertDialog.showSuccess("Title", "Appointment Search Clicked");
    }

    @FXML
    private void handleAddAppointment() {
        AlertDialog.showSuccess("Title", "Appointment Add Clicked");
    }

    @FXML
    private void handleEditAppointment() {
        AlertDialog.showSuccess("Title", "Appointment Edit Clicked");
    }

    @FXML
    private void handleDeleteAppointment() {
        AlertDialog.showSuccess("Title", "Appointment Delete Clicked");
    }

    @FXML
    private void handleRefreshAppointment() {
        AlertDialog.showSuccess("Title", "Appointment Refresh Clicked");
    }
}
