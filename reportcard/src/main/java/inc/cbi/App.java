package inc.cbi;


import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static Scene scene;
    private static Stage currentStage;

    @Override
    public void start(Stage stage) throws IOException {
        currentStage = stage;
        stage.setTitle("Planned Vs Actual Report Generator - RLT: 18092 V1.0");
        scene = new Scene(loadFXML("primary"), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        String path = fxml + ".fxml";
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(path));
        //FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("primary.fxml"));
        return fxmlLoader.load();
    }

    public static void run(String[] args) {
        launch();
    }

    @Override
    public void stop(){
        System.out.println("PROGRAM IS SHUTTING DOWN");
    }

    public static Stage getCurrentStage(){
        return currentStage;
    }

    public static Scene getScene() {
        return scene;
    }

    public static void setScene(Scene scene) {
        App.scene = scene;
    }

    public static void setCurrentStage(Stage currentStage) {
        App.currentStage = currentStage;
    }
}