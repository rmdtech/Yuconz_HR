import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


public class YuconzGui extends Application {

    public TextField employeeIdField;
    public PasswordField passwordField;
    User user = null;
    FXMLLoader loader = new FXMLLoader();

    public YuconzGui() {

    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    public String getAbsPath(String filename)
    {
        String path =  "file:///" + new File(filename).getAbsolutePath();
        System.out.println(path);
        return path;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Yuconz");
        loader.setLocation(new URL(getAbsPath("Login.fxml")));
        AnchorPane anchorPane = loader.<AnchorPane>load();
        Scene loginScene = new Scene(anchorPane);
        primaryStage.setScene(loginScene);
        primaryStage.show();
        primaryStage.setResizable(false);
    }

    public void changeScene(String fxml) throws Exception
    {
        Stage stage = (Stage) Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
        loader.setLocation(new URL(getAbsPath(fxml)));
        AnchorPane anchorPane = loader.<AnchorPane>load();
        Scene scene = new Scene(anchorPane);
        stage.setScene(scene);
        stage.show();
        stage.setResizable(false);
    }

    public void login(ActionEvent actionEvent) throws Exception {

        String employeeId = employeeIdField.getText();
        String password = passwordField.getText();
        user = Authenticate.login(employeeId, password);

        if(user != null)
        {

        }
        else
        {
            changeScene("ProfilePage.fxml");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Login Form Error!");
            alert.setContentText("Your username or password is incorrect!");
            alert.showAndWait();
        }


    }
}
