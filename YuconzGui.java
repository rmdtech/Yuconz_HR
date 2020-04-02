import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
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
    public Button managerPortalButton;
    public Button hrPortalButton;
    public ComboBox<String> reviewsDropdown;
    public Label reviewHeader;
    public Label nameLabel;
    public Label reviewer1Label;
    public Label reviewer2Label;
    public Label recommendationLabel;
    public TextField revieweeTextField;
    public TextField firstReviewerTextField;
    public TextField secondReviewerTextField;
    public TextField dueByTextField;
    public Label departmentLabel;
    public Label roleLabel;
    public TableView futurePerformanceTable;
    public static TableColumn fpNumberCol;
    public static TableColumn fpObjectiveColumn;
    public TableView pastPerformanceTable;
    public static TableColumn ppNumberCol;
    public static TableColumn ppObjectivesCol;
    public static TableColumn ppAchievementsCol;
    public TextField newEmployeeIdTextField;
    public PasswordField newPasswordTextField;
    public TextField newNameTextField;
    public TextField newSurnameTextField;
    public TextField newSupervisorTextField;
    public TextField newDateOfBirthTextField;
    public TextField newAddressTextField;
    public TextField newCityTextField;
    public TextField newCountyTextField;
    public TextField newPostcodeTextField;
    public TextField newPhoneTextField;
    public TextField newMobileTextField;
    public TextField newEmergencyContactTextField;
    public TextField newEmergencyNumberTextField;
    public TextArea performanceSummaryTextArea;
    public TextArea reviewerCommentsTextArea;
    public ComboBox<String> newRoleComboBox;
    public ComboBox<String> newDepartmentComboBox;
    public ComboBox<String> otherUserDetailsComboBox;
    public ComboBox<String> recommendationComboBox;
    public Button viewOtherUsersPersonalDetailsButton;
    public Button viewReviewsButton;
    public Button signButton;
    public Button saveButton;
    public Button addFPRowButton;
    public Button deleteFPRowButton;
    public Label youAreADirectorLabel;
    public Label signatureLabel;
    public ComboBox<String> manageReviewsDropdown;
    public Label personalDetailsHeader;
    public ComboBox<String> viewCompletedReviewsDropdown;

    //Initialising other  elements
    public static User user;
    public static String[] mainReview;


    FXMLLoader loader = new FXMLLoader();
    public static Scene scene;
    private ActionEvent actionEvent;

    public YuconzGui(){} // Empty constructor required for JFX

    public static void main(String[] args) {
        Application.launch(args);
    }

    public String getAbsPath(String filename)
    {
        return "file:///" + new File(filename).getAbsolutePath();
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
     * @throws Exception Required for JFX
     */
    public void changeScene(String fxml) throws Exception
    {
        Stage stage = (Stage) Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
        loader.setLocation(new URL(getAbsPath(fxml)));
        AnchorPane anchorPane = loader.load();
        scene = new Scene(anchorPane);
        Objects.requireNonNull(stage).setScene(scene);
        stage.show();
        stage.setResizable(false);
    }

    /**
     * Displays an error message box to the user
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

    /**
     * Displays an info message box to the user
     * @param infoHeader String content of the header
     * @param infoContent String content of the content box
     */
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
     * @return A Boolean whether or not validation was successful
     */
    public boolean validatePersonalDetailsPayload(String[] payload)
    {
        if(payload[0].length() != 6)
        {
            showError("EmployeeID Error!", "The EmployeeID must be 6 Characters.");
            return false;
        }
        if(payload[1].length() < 1)
        {
            showError("Surname Error!", "You must enter your Surname.");
            return false;
        }
        if(payload[2].length() < 1)
        {
            showError("First Name Error!", "You must enter your First Name.");
            return false;
        }
        Pattern datePattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
        if(!datePattern.matcher(payload[3]).matches())
        {
            showError("Birth Date Error!", "The Date should fit the format YYYY-MM-DD");
            return false;
        }
        Pattern postcodePattern = Pattern.compile("^[A-Z]{1,2}[0-9R][0-9A-Z]? [0-9][ABD-HJLNP-UW-Z]{2}$");
        if(!postcodePattern.matcher(payload[7]).matches())
        {
            showError("Postcode Error!", "Please use a valid UK Postcode. Example: CT2 7SG");
            return false;
        }
        Pattern telephonePattern = Pattern.compile("^(?:(?:\\(?(?:0(?:0|11)\\)?[\\s-]?\\(?|\\+)44\\)?[\\s-]?(?:\\(?0\\)?[\\s-]?)?)|(?:\\(?0))(?:(?:\\d{5}\\)?[\\s-]?\\d{4,5})|(?:\\d{4}\\)?[\\s-]?(?:\\d{5}|\\d{3}[\\s-]?\\d{3}))|(?:\\d{3}\\)?[\\s-]?\\d{3}[\\s-]?\\d{3,4})|(?:\\d{2}\\)?[\\s-]?\\d{4}[\\s-]?\\d{4}))(?:[\\s-]?(?:x|ext\\.?|\\#)\\d{3,4})?$");
        if(!telephonePattern.matcher(payload[8]).matches())
        {
            showError("Telephone Number Error!", "The telephone number must be 11 numbers long, including no spaces.");
            return false;
        }
        Pattern mobilePattern = Pattern.compile("((\\+44(\\s\\(0\\)\\s|\\s0\\s|\\s)?)|0)7\\d{3}(\\s)?\\d{6}");
        if(!mobilePattern.matcher(payload[9]).matches())
        {
            showError("Mobile Number Error!", "The mobile number must be 11 numbers long, including no spaces.");
            return false;
        }
        if((!mobilePattern.matcher(payload[11]).matches()) && (!telephonePattern.matcher(payload[11]).matches()))
        {
            showError("Emergency Contact Number Error!", "The emergency contact number must be a valid UK phone number.");
            return false;
        }
        return true;
    }

    /**
     * Validates if the user details are valid prior to adding them to the Database
     * @param employeeId The Employee ID which shall be given to the new user
     * @param password The Password which shall be given to the new user
     * @param supervisor The Employee ID of the user's supervisor
     * @param department The Department the Employee will belong to
     * @param role The Role of the Employee
     * @return A Boolean stating whether or not the user details are valid
     */
    public boolean validateUserAttributes(String employeeId, String password, String supervisor, String department, String role)
    {
        if(employeeId.length() != 6)
        {
            showError("Employee ID Error!", "The Employee ID must be 6 Characters.");
            return false;
        }

        if(password.length() < 1)
        {
            showError("Password Error", "Please enter a password");
            return false;
        }

        if(!(supervisor == null || supervisor.length() == 6))
        {
            showError("Supervisor ID Error!", "The supervisor must be a valid employee ID or be left blank for directors");
            return false;
        }

        if(department == null)
        {
            showError("Department Error!", "Please select a department from the dropdown menu");
            return false;
        }

        if(role == null)
        {
            showError("Role Error!", "Please select a role from the dropdown menu");
            return false;
        }
        return true;
    }

    /**
     * Takes the user to their profile page if they entered correct details, displays an error if they did not.
     * @param actionEvent required for JFX
     * @throws Exception required for JFX
     */
    public void login(ActionEvent actionEvent) throws Exception {
        this.actionEvent = actionEvent;

        String employeeId = employeeIdField.getText();
        String password = passwordField.getText();
        user = Authenticate.login(employeeId, password);

        if(user != null)
        {
            changeScene("ProfilePage.fxml");
            initialiseProfilePage();
            Stage stage = (Stage) Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
            Objects.requireNonNull(stage).setTitle("Yuconz - Logged in as: " + employeeId);
        }
        else
        {
            showError("Login Form Error!", "Your username or password is incorrect!");
        }
    }

    public void logout(ActionEvent actionEvent) throws Exception {
        this.actionEvent = actionEvent;
        Stage stage = (Stage) Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
        Objects.requireNonNull(stage).setTitle("Yuconz");
        Authenticate.logout(user);
        changeScene("Login.fxml");
    }

    public void initialiseProfilePage()
    {
        if(user.isLoggedIn())
        {
            ObservableList<String> reviewDatesForDisplay = FXCollections.observableArrayList();

            for(String[] rev: View.getReviewsAsReviewee(user))
            {
                reviewDatesForDisplay.add(rev[1]);
            }
            reviewsDropdown = (ComboBox<String>) scene.lookup("#reviewsDropdown");
            reviewsDropdown.setItems(reviewDatesForDisplay);

            noPortalsLabel = (Label) scene.lookup("#noPortalsLabel");
            portalsPane = (Pane) scene.lookup("#portalsPane");
            if(user.getDepartment() == Position.Department.HR)
            {
                noPortalsLabel.setVisible(false);
                Button hrPortalButton = (Button) scene.lookup("#hrPortalButton");
                hrPortalButton.setTranslateY(30);
                hrPortalButton.setTranslateX(5);
                hrPortalButton.setVisible(true);
            }
            if(user.getRole().level > 0)
            {
                noPortalsLabel.setVisible(false);
                Button managerPortalButton = (Button) scene.lookup("#managerPortalButton");
                managerPortalButton.setTranslateY(30);
                managerPortalButton.setTranslateX(5);

                if(user.getDepartment() == Position.Department.HR)
                {
                    managerPortalButton.setTranslateX(80);
                }
                managerPortalButton.setVisible(true);
            }
            if(user.getRole().level == 2)
            {
                viewReviewsButton = (Button) scene.lookup("#viewReviewsButton");
                youAreADirectorLabel = (Label) scene.lookup("#youAreADirectorLabel");
                reviewsDropdown.setVisible(false);
                viewReviewsButton.setVisible(false);
                youAreADirectorLabel.setVisible(true);
            }

        }
    }

    public void autofillSupervisor()
    {
        firstReviewerTextField.setText(View.getDirectSupervisor(revieweeTextField.getText()));
    }

    public void doCreateReview() throws Exception {
        Authorise.createPerformanceReview(user, new String[]{
                revieweeTextField.getText(),
                dueByTextField.getText(),
                secondReviewerTextField.getText()
                });
        changeScene("HrPortal.fxml");
    }

    /**
     * Initialises the updated document and signs the document based on who is pressing the button
     */
    public void signPerformanceReview()
    {
        String[] updatedDocument = mainReview;

        if(user.getEmployeeId().equals(updatedDocument[0]))
        {
            updatedDocument[5] = "true";
        }
        if(user.getEmployeeId().equals(updatedDocument[3]))
        {
            updatedDocument[6] = "true";
        }
        if(user.getEmployeeId().equals(updatedDocument[4]))
        {
            updatedDocument[7] = "true";
        }
        savePerformanceReview(updatedDocument);
    }

    /**
     *Initialises the updated document
     */
    public void savePerformanceReviewWithoutSigning()
    {
        String[] updatedDocument = mainReview;
        savePerformanceReview(updatedDocument);
    }

    /**
     * Saves the performance review to the Database. Only to be used by members of a review.
     * @param updatedDocument The review document including changes made by the user
     */
    public void savePerformanceReview(String[] updatedDocument)
    {

        ArrayList<String[]> updatedPastPerformance = new ArrayList<>();
        ArrayList<String> updatedFuturePerformance = new ArrayList<>();


        updatedDocument[1] = reviewHeader.getText().substring(20, 30); //Get dueBy date
        updatedDocument[2] = View.getReviewDocId(updatedDocument[0], updatedDocument[1]); //Get DocID
        updatedDocument[3] = reviewer1Label.getText().substring(reviewer1Label.getText().length() - 6);
        updatedDocument[4] = reviewer2Label.getText().substring(reviewer2Label.getText().length() - 6);
        updatedDocument[9] = performanceSummaryTextArea.getText();
        updatedDocument[10] = reviewerCommentsTextArea.getText();
        if(recommendationComboBox.getValue() != null)
        {
            updatedDocument[11] = recommendationComboBox.getValue();
        }

        //Begin preparing pastPerformance data
        for (Object o : pastPerformanceTable.getItems()) {
            String data[] = {ppObjectivesCol.getCellData(o).toString(), ppAchievementsCol.getCellData(o).toString()};
            updatedPastPerformance.add(data);
        }
        //Finish preparing pastPerformance data


        //Begin preparing futurePerformance data
        if(futurePerformanceTable.getItems().size() > 0)
        {
            for (Object o : futurePerformanceTable.getItems()) {
                updatedFuturePerformance.add(fpObjectiveColumn.getCellData(o).toString());
            }
        }

        //Finish preparing futurePerformance data

        Authorise.updatePerformanceReview(user, updatedDocument, updatedPastPerformance, updatedFuturePerformance);
        showInfo("Review Changed", "You have updated the review with new data");
    }


    /**
     * Loads review data from the Database and into the GUI
     * @param revieweeId 6 Character ID of the employee who is being reviewed
     * @param dueBy YYYY-MM-DD Date identifying the review
     */
    public void initialisePerformanceReviewView(String revieweeId, String dueBy)
    {

        String documentId = View.getReviewDocId(revieweeId, dueBy);

        if(Authorise.readPerformanceReview(user, revieweeId, dueBy))
        {

            mainReview = Authorise.readReviewMain(documentId); //import existing review data from database
            if(mainReview[5] == null)
            {
                mainReview[5] = "false";
            }
            if(mainReview[6] == null)
            {
                mainReview[6] = "false";
            }
            if(mainReview[7] == null)
            {
                mainReview[7] = "false";
            }
            ArrayList<String[]> pastPerformance = Authorise.readPastPerformance(documentId);
            ArrayList<String> futurePerformance = Authorise.readFuturePerformance(documentId);
            reviewHeader = (Label) scene.lookup("#reviewHeader");
            reviewHeader.setText("Performance Review (" + dueBy +")");

            employeeIdLabel = (Label) scene.lookup("#employeeIdLabel");
            employeeIdLabel.setText("Employee ID: " + revieweeId);

            nameLabel = (Label) scene.lookup("#nameLabel");
            nameLabel.setText("Name: " + View.getUserName(revieweeId)[0] + " " + View.getUserName(revieweeId)[1]);

            reviewer1Label = (Label) scene.lookup("#reviewer1Label");
            reviewer1Label.setText("Reviewer 1: " + mainReview[3]);

            reviewer2Label = (Label) scene.lookup("#reviewer2Label");
            reviewer2Label.setText("Reviewer 2: " + mainReview[4]);

            departmentLabel = (Label) scene.lookup("#departmentLabel");
            departmentLabel.setText("Department: " + View.getDepartment(revieweeId));

            roleLabel = (Label) scene.lookup("#roleLabel");
            roleLabel.setText("Role: " + View.getRole(revieweeId));

            recommendationLabel = (Label) scene.lookup("#recommendationLabel");
            if(mainReview[11] == null)
            {
                mainReview[11] = "";
            }
            recommendationLabel.setText("Recommendation: " + mainReview[11]);

            recommendationComboBox = (ComboBox<String>) scene.lookup("#recommendationComboBox");
            recommendationComboBox.getItems().add("Stay in post");
            recommendationComboBox.getItems().add("Salary increase");
            recommendationComboBox.getItems().add("Promotion");
            recommendationComboBox.getItems().add("Probation");
            recommendationComboBox.getItems().add("Termination");


            //Start setting up pastPerformanceTable
            pastPerformanceTable = (TableView) scene.lookup("#pastPerformanceTable");
            ppNumberCol = new TableColumn("No.");
            ppObjectivesCol = new TableColumn("Objective");
            ppAchievementsCol = new TableColumn("Achievement");
            pastPerformanceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            ppNumberCol.setMaxWidth(300.00);
            ppNumberCol.setCellValueFactory(new PropertyValueFactory<>("no"));
            ppObjectivesCol.setCellValueFactory(new PropertyValueFactory<>("objective"));
            ppAchievementsCol.setCellValueFactory(new PropertyValueFactory<>("achievement"));
            pastPerformanceTable.getColumns().addAll(ppNumberCol, ppObjectivesCol, ppAchievementsCol);

            if(pastPerformance.size() > 0) {
                for (int i = 0; i < pastPerformance.size(); i++) {
                    pastPerformanceTable.getItems().add(new ReviewGuiTableWrapper("" + (i + 1), pastPerformance.get(i)[0], pastPerformance.get(i)[1]));
                }
            }
            else // get info from futurePerformance of last previous review
            {
                String prevReviewDoc = View.getPreviousReview(user, revieweeId, dueBy);
                ArrayList<String> previousFuturePerformance = Authorise.readFuturePerformance(prevReviewDoc);
                for(int i = 0; i < previousFuturePerformance.size(); i++)
                {
                    pastPerformanceTable.getItems().add(new ReviewGuiTableWrapper("" + (i + 1), previousFuturePerformance.get(i), ""));
                }

            }
            //Finish setting up pastPerformanceTable

            performanceSummaryTextArea = (TextArea) scene.lookup("#performanceSummaryTextArea");
            performanceSummaryTextArea.setWrapText(true);
            performanceSummaryTextArea.setText(mainReview[9]);

            //Begin setting up futurePerformanceTable
            futurePerformanceTable = (TableView) scene.lookup("#futurePerformanceTable");

            fpNumberCol = new TableColumn("No.");
            fpObjectiveColumn = new TableColumn("Objective");
            futurePerformanceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            fpNumberCol.setMaxWidth(310.00);
            fpNumberCol.setCellValueFactory(new PropertyValueFactory<>("no"));
            fpObjectiveColumn.setCellValueFactory(new PropertyValueFactory<>("objective"));
            futurePerformanceTable.getColumns().addAll(fpNumberCol, fpObjectiveColumn);

            for(int i = 0; i < futurePerformance.size(); i++)
            {
                futurePerformanceTable.getItems().add(new ReviewGuiTableWrapper("" + (i+1), futurePerformance.get(i)));
            }
            //Finish setting up futurePerformanceTable

            reviewerCommentsTextArea = (TextArea) scene.lookup("#reviewerCommentsTextArea");
            reviewerCommentsTextArea.setWrapText(true);
            reviewerCommentsTextArea.setText(mainReview[10]);

            //Begin preparing signature label
            signatureLabel = (Label) scene.lookup("#signatureLabel");
            String signatureLabelText = "";
            if(mainReview[5] != "false")
            {
                signatureLabelText = signatureLabelText + "(" + revieweeId + ": " + mainReview[5] + ") ";
            }
            if(mainReview[6] != "false")
            {
                signatureLabelText = signatureLabelText + "(" + mainReview[3] + ": " + mainReview[6] + ") ";
            }
            if(mainReview[7] != "false")
            {
                signatureLabelText = signatureLabelText + "(" + mainReview[4] + ": " + mainReview[7] + ")";
            }
            signatureLabel.setText(signatureLabelText);
            //Finish preparing signature label

            //Disable inputs if all users have signed.
            if(mainReview[5] != "false" && mainReview[6] != "false" && mainReview[7] != "false")
            {
                signButton = (Button) scene.lookup("#signButton");
                signButton.setDisable(true);
                saveButton = (Button) scene.lookup("#saveButton");
                saveButton.setDisable(true);
                addFPRowButton = (Button) scene.lookup("#addFPRowButton");
                addFPRowButton.setDisable(true);
                deleteFPRowButton = (Button) scene.lookup("#deleteFPRowButton");
                deleteFPRowButton.setDisable(true);
                recommendationComboBox.setDisable(true);
                reviewerCommentsTextArea.setDisable(true);
                performanceSummaryTextArea.setDisable(true);
            }

        }
        else
        {
            showError("Permissions Error", "You do not have permission to view this document.");
        }
    }

    public void addFPRow()
    {
        TextInputDialog td = new TextInputDialog();
        td.setHeaderText("Input Objective");
        td.setContentText(("Input the objective here"));
        td.showAndWait();
        if(!futurePerformanceTable.getItems().contains("1"))
        {
            futurePerformanceTable.getItems().add(new ReviewGuiTableWrapper(""+(futurePerformanceTable.getItems().size()+1), td.getEditor().getText()));
        }

    }

    public void deleteSelectedFPRow()
    {
        futurePerformanceTable.getItems().removeAll(
                futurePerformanceTable.getSelectionModel().getSelectedItems()
        );
    }

    public void initialiseViewPersonalDetails(String employeeId)
    {
        String[] payload = Authorise.readPersonalDetails(user, employeeId);

        personalDetailsHeader = (Label) scene.lookup("#personalDetailsHeader");
        if(!user.getEmployeeId().equals(employeeId))
        {
            personalDetailsHeader.setText(employeeId + "'s Details");
        }

        employeeIdLabel = (Label) scene.lookup("#employeeIdLabel");
        employeeIdLabel.setText("Employee ID: " + Objects.requireNonNull(payload)[0]);

        surnameField = (TextField) scene.lookup("#surnameField");
        surnameField.setText(payload[1]);

        firstNameField = (TextField) scene.lookup("#firstNameField");
        firstNameField.setText(payload[2]);

        dobField = (TextField) scene.lookup("#dobField");
        dobField.setText(payload[3]);

        addressField = (TextField) scene.lookup("#addressField");
        addressField.setText(payload[4]);

        cityField = (TextField) scene.lookup("#cityField");
        cityField.setText(payload[5]);

        countyField = (TextField) scene.lookup("#countyField");
        countyField.setText(payload[6]);

        postcodeField = (TextField) scene.lookup("#postcodeField");
        postcodeField.setText(payload[7]);

        telephoneField = (TextField) scene.lookup("#telephoneField");
        telephoneField.setText(payload[8]);

        mobileField = (TextField) scene.lookup("#mobileField");
        mobileField.setText(payload[9]);

        emergencyContactNameField = (TextField) scene.lookup("#emergencyContactNameField");
        emergencyContactNameField.setText(payload[10]);

        emergencyContactNumberField = (TextField) scene.lookup("#emergencyContactNumberField");
        emergencyContactNumberField.setText(payload[11]);
    }

    /**
     * Initialises the HR Director user which can then create additional users who can log into the system.
     * @param actionEvent required for JFX
     * @throws Exception required for JFX
     */
    public void initialiseUser(ActionEvent actionEvent) throws Exception {
        this.actionEvent = actionEvent;
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
            if(dir.mkdir())
            {
                DatabaseParser dp = new DatabaseParser();
                dp.setupDatabase();
            }

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
        this.actionEvent = actionEvent;
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
        this.actionEvent = actionEvent;
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

    public void initialiseCreateNewUser()
    {
        newDepartmentComboBox = (ComboBox<String>) scene.lookup("#newDepartmentComboBox");
        newDepartmentComboBox.setItems(FXCollections.observableArrayList(
                "HR",
                "IT",
                "Admin",
                "BI",
                "MC",
                "SalesAndMarketing"
        ));

        newRoleComboBox = (ComboBox<String>) scene.lookup("#newRoleComboBox");
        newRoleComboBox.setItems(FXCollections.observableArrayList(
                "Employee",
                "Manager",
                "Director"
        ));
    }

    public void doCreateUser(ActionEvent actionEvent) throws Exception
    {
        this.actionEvent = actionEvent;
        String supervisor = newSupervisorTextField.getText();
        if(supervisor.equals(""))
        {
            supervisor = null;
        }

        String[] personalDetailsPayload = new String[] {
                newEmployeeIdTextField.getText(),
                newSurnameTextField.getText(),
                newNameTextField.getText(),
                newDateOfBirthTextField.getText(),
                newAddressTextField.getText(),
                newCityTextField.getText(),
                newCountyTextField.getText(),
                newPostcodeTextField.getText(),
                newPhoneTextField.getText(),
                newMobileTextField.getText(),
                newEmergencyContactTextField.getText(),
                newEmergencyNumberTextField.getText()
        };

        boolean areDetailsValid = validateUserAttributes(
                newEmployeeIdTextField.getText(),
                newPasswordTextField.getText(),
                supervisor,
                newDepartmentComboBox.getValue(),
                newRoleComboBox.getValue()
        );

        if(areDetailsValid && validatePersonalDetailsPayload(personalDetailsPayload))
        {
            if(!Authenticate.addNewUser(
                    newEmployeeIdTextField.getText(),
                    newPasswordTextField.getText(),
                    supervisor,
                    Position.Department.valueOf(newDepartmentComboBox.getValue()),
                    Position.Role.valueOf(newRoleComboBox.getValue())
            ))
            {
                showError("General Error", "A general error occurred, please make sure all data has been entered correctly and the user does not already exist");
                return;
            }

            if(!Authorise.createPersonalDetailsRecord(user, personalDetailsPayload))
            {
                showError("General Error", "A general error occurred, please make sure all data has been entered correctly and the user does not already exist");
                return;
            }
            showInfo("Database Updated", "New User Added Successfully!");
            changeScene("HrPortal.fxml");
            initialiseHrPortal();
        }
    }

    public void initialiseHrPortal()
    {
        otherUserDetailsComboBox = (ComboBox<String>) scene.lookup("#otherUserDetailsComboBox");
        viewCompletedReviewsDropdown = (ComboBox<String>) scene.lookup("#viewCompletedReviewsDropdown");

        ObservableList<String> users = FXCollections.observableArrayList();
        users.addAll(Objects.requireNonNull(View.getAllUsers(user)));

        ObservableList<String> completedReviews = FXCollections.observableArrayList();
        for(String[] pair : Objects.requireNonNull(View.getAllCompletedReviewKeys(user)))
        {
            completedReviews.add(pair[0] + " (" + pair[1] + ")");
        }

        otherUserDetailsComboBox.setItems(users);
        viewCompletedReviewsDropdown.setItems(completedReviews);

    }

    public void initialiseManagerPortal()
    {
        ArrayList<String[]> myReviews = View.getReviewsAsReviewer(user);
        ObservableList<String> myReviewsAsStrings = FXCollections.observableArrayList();
        for (String[] pair : myReviews)
        {
            myReviewsAsStrings.add(pair[0] + " (" + pair[1]+ ")");
        }
        manageReviewsDropdown = (ComboBox<String>) scene.lookup("#manageReviewsDropdown");
        manageReviewsDropdown.setItems(myReviewsAsStrings);
    }

    public void viewOtherUsersPersonalDetailsForm() throws Exception
    {
        if(otherUserDetailsComboBox.getValue() == null)
        {
            showError("Details Selection Error", "Please select a personal details document from the dropdown menu");
        }
        else
        {
            String selectedEmpId = otherUserDetailsComboBox.getValue();
            changeScene("ViewPersonalDetails.fxml");
            initialiseViewPersonalDetails(selectedEmpId);
        }
    }

    public void viewCreateNewUser(ActionEvent actionEvent) throws Exception
    {
        this.actionEvent = actionEvent;
        changeScene("CreateNewUser.fxml");
        initialiseCreateNewUser();
    }

    public void viewPersonalDetails(ActionEvent actionEvent) throws Exception
    {
        this.actionEvent = actionEvent;
        changeScene("ViewPersonalDetails.fxml");
        initialiseViewPersonalDetails(user.getEmployeeId());
    }

    public void viewHrPortal(ActionEvent actionEvent) throws Exception
    {
        this.actionEvent = actionEvent;
        changeScene("HrPortal.fxml");
        initialiseHrPortal();
    }

    public void viewManagerPortal(ActionEvent actionEvent) throws Exception
    {
        this.actionEvent = actionEvent;
        changeScene("ManagerPortal.fxml");
        initialiseManagerPortal();
    }

    public void viewInitialiseReview(ActionEvent actionEvent) throws Exception
    {
        this.actionEvent = actionEvent;
        changeScene("InitialiseReview.fxml");
    }

    public void viewViewPerformanceReview() throws Exception
    {
        if(reviewsDropdown.getValue() == null)
        {
            showError("Review Selection Error", "Please select a review from the dropdown menu");
        }
        else
        {
            changeScene("viewPerformanceReview.fxml");
            initialisePerformanceReviewView(user.getEmployeeId(), reviewsDropdown.getValue());
        }
    }

    public void viewViewPerformanceReviewAsReviewer() throws Exception
    {
        if(manageReviewsDropdown.getValue() == null)
        {
            showError("Review Selection Error", "Please select a review from the dropdown menu");
        }
        else
        {
            String employeeIdOfReview = manageReviewsDropdown.getValue().split(" ")[0];
            String dateOfReview = manageReviewsDropdown.getValue().split(" ")[1].replace("(", "").replace(")", "");
            changeScene("viewPerformanceReview.fxml");
            initialisePerformanceReviewView(employeeIdOfReview, dateOfReview);
        }
    }

    public void viewViewPerformanceReviewAsHr() throws Exception {
        if(viewCompletedReviewsDropdown.getValue() == null)
        {
            showError("Review Selection Error", "Please select a review from the dropdown menu");
        }
        else
        {
            String employeeIdOfReview = viewCompletedReviewsDropdown.getValue().split(" ")[0];
            String dateOfReview = viewCompletedReviewsDropdown.getValue().split(" ")[1].replace("(", "").replace(")", "");
            changeScene("viewPerformanceReview.fxml");
            initialisePerformanceReviewView(employeeIdOfReview, dateOfReview);
        }
    }

    public void goHome(ActionEvent actionEvent) throws Exception {
        this.actionEvent = actionEvent;
        changeScene("ProfilePage.fxml");
        initialiseProfilePage();
    }
}
