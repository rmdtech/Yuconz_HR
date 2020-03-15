import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;


public class YuconzGui extends Application {

    String absPath;

    public YuconzGui() {
        absPath = new File("Login.fxml").getAbsolutePath();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Yuconz");

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(new URL("file:///" + absPath));

        AnchorPane anchorPane = loader.<AnchorPane>load();
        Scene scene = new Scene(anchorPane);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    public void doThing(ActionEvent actionEvent) {
        System.out.println("Thing Done!");
    }
}
