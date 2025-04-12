package ca.humber.controller;

import java.net.URL;
import java.util.ResourceBundle;

import ca.humber.App;
import ca.humber.service.AuthService;
import ca.humber.util.HibernateUtil;
import ca.humber.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginController implements Initializable {

	@FXML
	private TextField usernameField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private VBox loadingPane;

	private final AuthService authService = new AuthService();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Trigger login on Enter key
		passwordField.setOnKeyPressed(this::handleEnterKey);
		usernameField.setOnKeyPressed(this::handleEnterKey);
	}

	private void handleEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			handleLogin();
		}
	}

	@FXML
	private void handleLogin() {
		String username = usernameField.getText().trim();
		String password = passwordField.getText().trim();

		if (username.isEmpty() || password.isEmpty()) {
			AlertDialog.showWarning("Login", "Please enter both username and password.");
			return;
		}

		loadingPane.setVisible(true);

		new Thread(() -> {
			try {
				authService.login(username, password);

				Platform.runLater(this::openDashboard);

			} catch (Exception e) {
				String error = HibernateUtil.message(e);

				Platform.runLater(() -> {
					AlertDialog.showWarning("Login Failed", error);
				});
			} finally {
				Platform.runLater(() -> loadingPane.setVisible(false));
			}
		}).start();
	}

	private void openDashboard() {
		System.out.println("Login successful. Welcome, " + SessionManager.getCurrentUser().getUsername());

		try {
			Stage stage = (Stage) usernameField.getScene().getWindow();
			App.setRoot("/view/dashboard");
			stage.setWidth(700);
			stage.setHeight(600);
			stage.centerOnScreen();
			stage.setTitle("Dashboard");
		} catch (Exception e) {
			System.err.println("Failed to open dashboard: " + e.getMessage());
		}
	}
}
