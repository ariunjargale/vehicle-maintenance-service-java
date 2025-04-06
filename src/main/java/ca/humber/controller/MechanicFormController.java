package ca.humber.controller;

import ca.humber.dao.MechanicDAO;
import ca.humber.model.Mechanic;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class MechanicFormController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField phoneField;

    @FXML
    private ComboBox<String> specializationComboBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private Mechanic mechanic;
    private String mode;
    private MechanicsTabController parentController;

    @FXML
    public void initialize() {
        // Initialize specialization options
        specializationComboBox.getItems().addAll(
                "General Mechanic",
                "Engine Specialist",
                "Transmission Specialist",
                "Electrical Systems",
                "Brake Specialist",
                "Suspension Specialist",
                "Air Conditioning"
        );

        // Default to first option
        specializationComboBox.getSelectionModel().selectFirst();

        // Set up validation for phone number
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                phoneField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (phoneField.getText().length() > 10) {
                phoneField.setText(phoneField.getText().substring(0, 10));
            }
        });
    }

    public void setMode(String mode) {
        this.mode = mode;
        if ("add".equals(mode)) {
            mechanic = new Mechanic();
        }
    }

    public void setMechanic(Mechanic mechanic) {
        this.mechanic = mechanic;

        // Populate fields with mechanic data
        nameField.setText(mechanic.getName());
        phoneField.setText(mechanic.getPhone());
        specializationComboBox.setValue(mechanic.getSpecialization());
    }

    public void setParentController(MechanicsTabController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        // Update mechanic object with form data
        mechanic.setName(nameField.getText().trim());
        mechanic.setPhone(phoneField.getText().trim());
        mechanic.setSpecialization(specializationComboBox.getValue());

        try {
            MechanicDAO.saveMechanic(mechanic);
            AlertDialog.showSuccess("Success", "Mechanic saved successfully");
            closeForm();

            // Refresh parent table
            if (parentController != null) {
                parentController.refreshMechanics();
            }
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "Failed to save mechanic: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeForm();
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText().trim().isEmpty()) {
            errors.append("- Name is required\n");
        }

        if (phoneField.getText().trim().isEmpty()) {
            errors.append("- Phone number is required\n");
        } else if (phoneField.getText().trim().length() < 10) {
            errors.append("- Please enter a 10-digit phone number\n");
        }

        if (specializationComboBox.getValue() == null) {
            errors.append("- Specialization is required\n");
        }

        if (errors.length() > 0) {
            AlertDialog.showWarning("Validation Error", "Please correct the following errors:\n" + errors.toString());
            return false;
        }

        return true;
    }

    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}