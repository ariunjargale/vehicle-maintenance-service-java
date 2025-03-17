package ca.humber.main;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

/**
 * JavaFX UI Test - Login Screen
 */
public class loginUI_test extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 載入 login.fxml 檔案
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        
        // 創建場景
        Scene scene = new Scene(root, 400, 300);
        
        // 設定舞台
        primaryStage.setTitle("Login Test");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        System.out.println("Login FXML loaded successfully");
    }

    public static void main(String[] args) {
        launch(args);
    }
}