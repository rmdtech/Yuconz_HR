import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.util.List;
import java.util.regex.Pattern;


public class YuconzGui extends Application {

    //Initialising fields
    public TextField employeeIdField;
    public PasswordField passwordField;
    public TextField firstNameField;
    public TextField surnameField;
    public TextField dobField;
    public TextField addressField;
    public TextField countyField;
    public TextField emergencyContactNameField;
    public TextField cityField;
    public TextField postcodeField;
    public TextField emergencyContactNumberField;
    public TextField telephoneField;
    public TextField mobileField;
    public TextField initialisePasswordField;
    public Label noPortalsLabel;
    public Pane portalsPane;
    public Label employeeIdLabel;
    public Button editPersonalDetailsButton;
    public Button savePersonalDetailsButton;

    //Initialising other  elements
    public static User user;
    FXMLLoader loader = new FXMLLoader();
    public static Scene scene;

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

    /**
     * Checks if the database exists yet
     * @return boolean
     */
    static boolean checkIsFirstBoot()
    {
        File dbFile = new File("./databases/yuconz.db");
        return !dbFile.exists();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Yuconz");
        loader.setLocation(new URL(getAbsPath("Boot.fxml")));
        primaryStage.show();
        primaryStage.setResizable(false);

        if(checkIsFirstBoot())
        {
            changeScene("InitialiseUser.fxml");
        }
        else
        {
            changeScene("Login.fxml");
        }
    }

    public Scene getScene()
    {
        return scene;
    }

    /**
     * Changes the scene the user can see to whatever is passed in via fxml
     * @param fxml a string in the format (document.fxml)
     * @throws Exception
     */
    public void changeScene(String fxml) throws Exception
    {
        Stage stage = (Stage) Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
        loader.setLocation(new URL(getAbsPath(fxml)));
        AnchorPane anchorPane = loader.<AnchorPane>load();
        scene = new Scene(anchorPane);
        stage.setScene(scene);
        stage.show();
        stage.setResizable(false);
    }

    /**
     * Displays an error message to the user
     * @param errorHeader String content of the header
     * @param errorContent String content of the content box
     */
    public void showError(String errorHeader, String errorContent)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error!");
        alert.setHeaderText(errorHeader);
        alert.setContentText(errorContent);
        alert.showAndWait();
    }

    public void showInfo(String infoHeader, String infoContent)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(infoHeader);
        alert.setContentText(infoContent);
        alert.showAndWait();
    }

    /**
     * Checks that the data being entered in the Personal Details payload is valid data.
     * @param payload the data being added
     * @return
     */
    public boolean validatePersonalDetailsPayload(String[] payload)
    {
        if(payload[0].toString().length() != 6)
        {
            showError("EmployeeID Error!", "The EmployeeID must be 6 Characters.");
            return false;
        }
        if(payload[1].toString().length() < 1)
        {
            showError("Surname Error!", "You must enter your Surname.");
            return false;
        }
        if(payload[2].toString().length() < 1)
        {
            showError("First Name Error!", "You must enter your First Name.");
            return false;
        }
        Pattern datePattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
        if(!datePattern.matcher(payload[3].toString()).matches())
        {
            showError("Birth Date Error!", "The Date should fit the format YYYY-MM-DD");
            return false;
        }
        Pattern postcodePattern = Pattern.compile("^[A-Z]{1,2}[0-9R][0-9A-Z]? [0-9][ABD-HJLNP-UW-Z]{2}$");
        if(!postcodePattern.matcher(payload[7].toString()).matches())
        {
            showError("Postcode Error!", "Please use a valid UK Postcode. Example: CT2 7SG");
            return false;
        }
        Pattern telephonePattern = Pattern.compile("^(?:(?:\\(?(?:0(?:0|11)\\)?[\\s-]?\\(?|\\+)44\\)?[\\s-]?(?:\\(?0\\)?[\\s-]?)?)|(?:\\(?0))(?:(?:\\d{5}\\)?[\\s-]?\\d{4,5})|(?:\\d{4}\\)?[\\s-]?(?:\\d{5}|\\d{3}[\\s-]?\\d{3}))|(?:\\d{3}\\)?[\\s-]?\\d{3}[\\s-]?\\d{3,4})|(?:\\d{2}\\)?[\\s-]?\\d{4}[\\s-]?\\d{4}))(?:[\\s-]?(?:x|ext\\.?|\\#)\\d{3,4})?$");
        if(!telephonePattern.matcher(payload[8].toString()).matches())
        {
            showError("Telephone Number Error!", "The telephone number must be 11 numbers long, including no spaces.");
            return false;
        }
        Pattern mobilePattern = Pattern.compile("((\\+44(\\s\\(0\\)\\s|\\s0\\s|\\s)?)|0)7\\d{3}(\\s)?\\d{6}");
        if(!mobilePattern.matcher(payload[9].toString()).matches())
        {
            showError("Mobile Number Error!", "The mobile number must be 11 numbers long, including no spaces.");
            return false;
        }
        if((!mobilePattern.matcher(payload[11].toString()).matches()) && (!telephonePattern.matcher(payload[11].toString()).matches()))
        {
            showError("Emergency Contact Number Error!", "The emergency contact number must be a valid UK phone number.");
            return false;
        }
        return true;
    }

    /**
     * Takes the user to their profile page if they entered correct details, displays an error if they did not.
     * @param actionEvent
     * @throws Exception
     */
    public void login(ActionEvent actionEvent) throws Exception {

        String employeeId = employeeIdField.getText();
        String password = passwordField.getText();
        user = Authenticate.login(employeeId, password);

        if(user != null)
        {
            changeScene("ProfilePage.fxml");
            initialiseProfilePage();
        }
        else
        {
            showError("Login Form Error!", "Your username or password is incorrect!");
        }
    }

    public void initialiseProfilePage()
    {
        if(user.isLoggedIn())
        {
            noPortalsLabel = (Label) scene.lookup("#noPortalsLabel");
            portalsPane = (Pane) scene.lookup("#portalsPane");
            if(user.getDepartment() == Position.Department.HR)
            {
                noPortalsLabel.setVisible(false);
                Button hrPortalButton = new Button();
                hrPortalButton.setText("HR Portal");
                hrPortalButton.setTranslateY(30);
                hrPortalButton.setTranslateX(5);
                portalsPane.getChildren().add(hrPortalButton);
            }
            if(user.getRole().level > 0)
            {
                noPortalsLabel.setVisible(false);
                Button managerPortalButton = new Button();
                managerPortalButton.setText("Manager/Director Portal");
                managerPortalButton.setTranslateY(30);
                managerPortalButton.setTranslateX(5);
                if(user.getDepartment() == Position.Department.HR)
                {
                    managerPortalButton.setTranslateX(80);
                }
                portalsPane.getChildren().add(managerPortalButton);
            }

        }
    }

    public void updatePersonalDetailsForm() {
        String[] payload = Authorise.readPersonalDetails(user, user.getEmployeeId());

        employeeIdLabel = (Label) scene.lookup("#employeeIdLabel");
        employeeIdLabel.setText("Employee ID: " + payload[0].toString());

        surnameField = (TextField) scene.lookup("#surnameField");
        surnameField.setText(payload[1].toString());

        firstNameField = (TextField) scene.lookup("#firstNameField");
        firstNameField.setText(payload[2].toString());

        dobField = (TextField) scene.lookup("#dobField");
        dobField.setText(payload[3].toString());

        addressField = (TextField) scene.lookup("#addressField");
        addressField.setText(payload[4].toString());

        cityField = (TextField) scene.lookup("#cityField");
        cityField.setText(payload[5].toString());

        countyField = (TextField) scene.lookup("#countyField");
        countyField.setText(payload[6].toString());

        postcodeField = (TextField) scene.lookup("#postcodeField");
        postcodeField.setText(payload[7].toString());

        telephoneField = (TextField) scene.lookup("#telephoneField");
        telephoneField.setText(payload[8].toString());

        mobileField = (TextField) scene.lookup("#mobileField");
        mobileField.setText(payload[9].toString());

        emergencyContactNameField = (TextField) scene.lookup("#emergencyContactNameField");
        emergencyContactNameField.setText(payload[10].toString());

        emergencyContactNumberField = (TextField) scene.lookup("#emergencyContactNumberField");
        emergencyContactNumberField.setText(payload[11].toString());
    }

    /**
     * Initialises the HR Director user which can then create additional users who can log into the sytsem.
     * @param actionEvent
     * @throws Exception
     */
    public void initialiseUser(ActionEvent actionEvent) throws Exception {
        String employeeId = employeeIdField.getText();
        String password = initialisePasswordField.getText();
        String firstName = firstNameField.getText();
        String surname = surnameField.getText();
        String dob = dobField.getText();
        String address = addressField.getText();
        String city = cityField.getText();
        String county = countyField.getText();
        String postcode = postcodeField.getText();
        String telephone = telephoneField.getText();
        String mobile = mobileField.getText();
        String emergencyContact = emergencyContactNameField.getText();
        String emergencyContactNumber = emergencyContactNumberField.getText();

        String[] personalDetails = new String[]{employeeId, surname, firstName, dob, address, city, county, postcode, telephone, mobile, emergencyContact, emergencyContactNumber};

        if(validatePersonalDetailsPayload(personalDetails) && password != null)
        {
            //Initialise the database.
            File dir = new File("./databases");
            dir.mkdir();
            DatabaseParser dp = new DatabaseParser();
            dp.setupDatabase();

            if (Authenticate.addNewUser(employeeId, password, null, Position.Department.HR, Position.Role.Director))
            {
                user = Authenticate.login(employeeId, password);
                if(Authorise.createPersonalDetailsRecord(user, personalDetails))
                {
                    Authenticate.logout(user);
                    changeScene("Login.fxml");
                }
            }
            else
            {
                showError("User Initialisation Error!", "Error initialising User, please check employeeID and password.");
            }
        }

    }

    public void unlockPersonalDetails(ActionEvent actionEvent)
    {
        surnameField.setDisable(false);
        firstNameField.setDisable(false);
        dobField.setDisable(false);
        addressField.setDisable(false);
        cityField.setDisable(false);
        countyField.setDisable(false);
        postcodeField.setDisable(false);
        telephoneField.setDisable(false);
        mobileField.setDisable(false);
        emergencyContactNumberField.setDisable(false);
        emergencyContactNameField.setDisable(false);

        editPersonalDetailsButton = (Button) getScene().lookup("#editPersonalDetailsButton");
        editPersonalDetailsButton.setVisible(false);
        savePersonalDetailsButton = (Button) getScene().lookup("#savePersonalDetailsButton");
        savePersonalDetailsButton.setVisible(true);
    }

    public void lockPersonalDetails(ActionEvent actionEvent) {
        String id = employeeIdLabel.getText().substring(employeeIdLabel.getText().length() - 6);
        String[] payload = new String[]{id, surnameField.getText(), firstNameField.getText(), dobField.getText(), addressField.getText(), cityField.getText(), countyField.getText(), postcodeField.getText(), telephoneField.getText(), mobileField.getText(), emergencyContactNameField.getText(), emergencyContactNumberField.getText()};

        if(validatePersonalDetailsPayload(payload))
        {
            surnameField.setDisable(true);
            firstNameField.setDisable(true);
            dobField.setDisable(true);
            addressField.setDisable(true);
            cityField.setDisable(true);
            countyField.setDisable(true);
            postcodeField.setDisable(true);
            telephoneField.setDisable(true);
            mobileField.setDisable(true);
            emergencyContactNumberField.setDisable(true);
            emergencyContactNameField.setDisable(true);
            editPersonalDetailsButton = (Button) getScene().lookup("#editPersonalDetailsButton");
            editPersonalDetailsButton.setVisible(true);
            savePersonalDetailsButton = (Button) getScene().lookup("#savePersonalDetailsButton");
            savePersonalDetailsButton.setVisible(false);
            System.out.println(user);
            Authorise.updatePersonalDetails(user, payload);
            showInfo("Information", "User Details Updated Successfully!");
        }
        else
        {
            showError("Saving Error!", "Check the details you have entered!");
        }
    }
}
