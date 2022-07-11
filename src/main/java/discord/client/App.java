package discord.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class App extends Application {

    private static Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("LoginMenu.fxml"));
        try {
        Controller controller = new Controller(new MySocket(new Socket("127.0.0.1", 6000)));
        fxmlLoader.setController(controller);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Discord");
        stage.setScene(scene);
        stage.show();
        } catch (IOException e) {
            System.err.println("The Main Server is down!");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        launch();
    }

    public static Stage getStage() {
        return stage;
    }
}