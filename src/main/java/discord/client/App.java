package discord.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class App extends Application {


    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("loginMenu.fxml"));
        Controller controller = new Controller(null, new MySocket(new Socket("127.0.0.1", 6000)));
        fxmlLoader.setController(controller);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Discord");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void loadNewScene(FXMLLoader loader, Stage stage, Controller controller) {
        try {
            loader.setController(controller);
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}