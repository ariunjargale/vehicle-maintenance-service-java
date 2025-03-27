package ca.humber;

import ca.humber.service.AuthService;
import ca.humber.util.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private final AuthService authService = new AuthService();

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("/view/login"));
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

    // Closing Hibernate session when app close
    @Override
    public void stop() throws Exception {
        authService.logout();
        super.stop();
    }

}