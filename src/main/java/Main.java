import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

// Luồng hoạt động 3 - tier: view <-> controller<-> dao <-> DB
public class Main extends Application {
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/fxml/Menu.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        String css = Objects.requireNonNull(getClass().getResource("/view/css/style.css")).toExternalForm();
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
        stage.setTitle("PAND");
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }
    public static void main(String[] args) {
        Application.launch();
    }
}
