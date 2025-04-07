package ca.humber.controller;

import ca.humber.dao.MechanicDAO;
import ca.humber.exceptions.ConstraintException;
import ca.humber.model.Mechanic;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MechanicsTabController implements Initializable {
    @FXML
    private TableView<Mechanic> mechanicsTable;
    @FXML
    private TableColumn<Mechanic, Integer> mechanicIdColumn;
    @FXML
    private TableColumn<Mechanic, String> nameColumn;
    @FXML
    private TableColumn<Mechanic, String> phoneColumn;
    @FXML
    private TableColumn<Mechanic, String> specializationColumn;
    @FXML
    private TextField mechanicSearchField;

    private ObservableList<Mechanic> mechanicList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        loadMechanics();
    }

    private void setupTableColumns() {
        mechanicIdColumn.setCellValueFactory(new PropertyValueFactory<>("mechanicId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        specializationColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));

        // Format phone numbers with dashes if they are 10 digits long
        phoneColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Mechanic, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if (item.length() == 10) {
                        setText(item.substring(0, 3) + "-" + item.substring(3, 6) + "-" + item.substring(6));
                    } else {
                        setText(item);
                    }
                }
            }
        });
    }

    private void loadMechanics() {
        try {
            List<Mechanic> mechanics = MechanicDAO.getActiveMechanics();
            mechanicList.clear();
            mechanicList.addAll(mechanics);
            mechanicsTable.setItems(mechanicList);
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while loading mechanic data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSearchMechanics() {
        String searchTerm = mechanicSearchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadMechanics();
            return;
        }

        try {
            List<Mechanic> searchResults = MechanicDAO.searchMechanics(searchTerm);
            mechanicList.clear();
            mechanicList.addAll(searchResults);
            mechanicsTable.setItems(mechanicList);

            if (searchResults.isEmpty()) {
                AlertDialog.showSuccess("Search Results", "No mechanics matching the criteria were found");
            } else {
                AlertDialog.showSuccess("Search Results",
                        "Found " + searchResults.size() + " mechanics matching the criteria");
            }
        } catch (Exception e) {
            AlertDialog.showWarning("Error", "An error occurred while searching for mechanics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddMechanic() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mechanic_form.fxml"));
            Parent root = loader.load();

            MechanicFormController controller = loader.getController();
            controller.setMode("add");
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Mechanic");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            AlertDialog.showWarning("Error",
                    "An error occurred while opening the add mechanic form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleEditMechanic() {
        Mechanic selectedMechanic = mechanicsTable.getSelectionModel().getSelectedItem();
        if (selectedMechanic == null) {
            AlertDialog.showWarning("Edit Mechanic", "Please select a mechanic to edit first");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mechanic_form.fxml"));
            Parent root = loader.load();

            MechanicFormController controller = loader.getController();
            controller.setMode("edit");
            controller.setMechanic(selectedMechanic);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Mechanic");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            AlertDialog.showWarning("Error",
                    "An error occurred while opening the edit mechanic form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleDeleteMechanic() {
        Mechanic selectedMechanic = mechanicsTable.getSelectionModel().getSelectedItem();
        if (selectedMechanic == null) {
            AlertDialog.showWarning("Delete Mechanic", "Please select a mechanic to delete first");
            return;
        }

        boolean confirm = AlertDialog.showConfirmation("Confirm Deletion",
                "Are you sure you want to delete mechanic " + selectedMechanic.getName() + "?" +
                        "\n\nThis action will not permanently delete the data, it will only mark the mechanic as inactive.");

        if (confirm) {
            try {
                boolean deleted = MechanicDAO.deleteMechanic(selectedMechanic.getMechanicId());
                if (deleted) {
                    AlertDialog.showSuccess("Delete Mechanic", "Mechanic successfully deleted (marked as inactive)");
                    loadMechanics();
                } else {
                    AlertDialog.showWarning("Error", "Failed to delete mechanic");
                }
            } catch (ConstraintException e) {
                AlertDialog.showWarning("Constraint Error", e.getMessage());
            } catch (Exception e) {
                AlertDialog.showWarning("Error", "An error occurred while deleting the mechanic: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleRefreshMechanics() {
        loadMechanics();
        AlertDialog.showSuccess("Refresh", "Mechanic list has been updated");
    }

    public void refreshMechanics() {
        loadMechanics();
    }
}