import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseParserTest {
    DatabaseParser dp = new DatabaseParser();

    String[] createDemoEmployee()
    {
        dp.newEmployee("abc123",
                "5583f71e39554054b1aa1ce16fd520f2",
                "8c78ab1466456a50c569849e39ec9909eaf3fce9a7ee91660da9d4da4a18fe10f93842473b05d38a828c2e0e130b7fed1dc4cef83459a8bda95162c87abf19ce",
                "HR",
                "0");
        String[] payload = {"abc123","Smith","John","1992-03-28", "14 York Road", "Canterbury", "Kent", "CT1 3TA",
                "01634289431", "07499274509", "Jane Smith", "07286308174"};
        dp.createPersonalDetailsRecord(payload, "33d8bd019d4b4662bdbc8efd6048d1d9");
        return payload;
    }

    static void generateTestUsers() {
        Authenticate.addNewUser("dir123", "password", null, Position.Department.HR, Position.Role.Director);
        Authenticate.addNewUser("hrm123", "password", "dir123", Position.Department.HR, Position.Role.Manager);
        Authenticate.addNewUser("hre123", "password", "hrm123", Position.Department.HR, Position.Role.Employee);
        Authenticate.addNewUser("itm123", "password", "dir123", Position.Department.IT, Position.Role.Manager);
        Authenticate.addNewUser("ite123", "password", "itm123", Position.Department.IT, Position.Role.Employee);
    }


    @org.junit.jupiter.api.Test
    void createValidPersonalDetailsRecord() {
        dp.newEmployee("abc123",
                "5583f71e39554054b1aa1ce16fd520f2",
                "8c78ab1466456a50c569849e39ec9909eaf3fce9a7ee91660da9d4da4a18fe10f93842473b05d38a828c2e0e130b7fed1dc4cef83459a8bda95162c87abf19ce",
                "HR",
                "0");
        String[] payload = {"abc123","Smith","John","1992-03-28", "14 York Road", "Canterbury", "Kent", "CT1 3TA",
                "01634289431", "07499274509", "Jane Smith", "07286308174"};
        assertTrue(
                dp.createPersonalDetailsRecord(payload, "33d8bd019d4b4662bdbc8efd6048d1d9")
        );

        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");
    }

    @org.junit.jupiter.api.Test
    void createDuplicatePersonalDetailsRecord()
    {
        String[] payload = createDemoEmployee();

        assertFalse(
                dp.createPersonalDetailsRecord(payload, "33d8bd019d4b4662bdbc8efd6048d1d9")
        );

        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");
    }

    @org.junit.jupiter.api.Test
    void updatePersonalDetails()
    {
        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");

        dp.newEmployee("abc123",
                "5583f71e39554054b1aa1ce16fd520f2",
                "8c78ab1466456a50c569849e39ec9909eaf3fce9a7ee91660da9d4da4a18fe10f93842473b05d38a828c2e0e130b7fed1dc4cef83459a8bda95162c87abf19ce",
                "HR",
                "0");
        String[] payload = {"abc123","Smith","John","1992-03-28", "14 York Road", "Canterbury", "Kent", "CT1 3TA",
                "01634289431", "07499274509", "Jane Smith", "07286308174"};
        dp.createPersonalDetailsRecord(payload, "33d8bd019d4b4662bdbc8efd6048d1d9");
        String[] newPayload = {"abc123","Smith","John","1992-03-28", "14 York Road", "Canterbury", "Kent", "CT1 3TA",
                "01622761848", "07499274509", "Jane Smith", "07286308174"};

        dp.updatePersonalDetails(newPayload);

        dp.sqlRead("SELECT telephoneNumber FROM PersonalDetails WHERE employeeId = 'abc123'");

        String res = null;
        try {
            dp.result.next();
            res = dp.result.getString("telephoneNumber");

        }
        catch (SQLException e) {}


        assertEquals(res, "01622761848");

        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");
    }

    @org.junit.jupiter.api.Test
    void updateTwoPersonalDetailsRecords()
    {
        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");

        dp.newEmployee("abc123",
                "5583f71e39554054b1aa1ce16fd520f2",
                "8c78ab1466456a50c569849e39ec9909eaf3fce9a7ee91660da9d4da4a18fe10f93842473b05d38a828c2e0e130b7fed1dc4cef83459a8bda95162c87abf19ce",
                "HR",
                "0");
        String[] payload = {"abc123","Smith","John","1992-03-28", "14 York Road", "Canterbury", "Kent", "CT1 3TA",
                "01634289431", "07499274509", "Jane Smith", "07286308174"};
        dp.createPersonalDetailsRecord(payload, "33d8bd019d4b4662bdbc8efd6048d1d9");
        String[] newPayload = {"abc123","Smith","John","1992-03-28", "14 York Road", "Canterbury", "Kent", "CT1 3TA",
                "01622761848", "07499274509", "Roger Bosch", "07117698761"};

        dp.updatePersonalDetails(newPayload);

        dp.sqlRead("SELECT emergencyContact, emergencyContactNumber FROM PersonalDetails WHERE employeeId = 'abc123'");

        String res1 = null;
        String res2 = null;
        try {
            dp.result.next();
            res1 = dp.result.getString("emergencyContact");
            res2 = dp.result.getString("emergencyContactNumber");

        }
        catch (SQLException e) {}


        assertEquals(res1, "Roger Bosch");
        assertEquals(res2, "07117698761");

        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");
    }

    @org.junit.jupiter.api.Test
    void resetPersonalDetailsRecords()
    {
        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");

        dp.newEmployee("abc123",
                "5583f71e39554054b1aa1ce16fd520f2",
                "8c78ab1466456a50c569849e39ec9909eaf3fce9a7ee91660da9d4da4a18fe10f93842473b05d38a828c2e0e130b7fed1dc4cef83459a8bda95162c87abf19ce",
                "HR",
                "0");
        String[] payload = {"abc123","Smith","John","1992-03-28", "14 York Road", "Canterbury", "Kent", "CT1 3TA",
                "01634289431", "07499274509", "Jane Smith", "07286308174"};
        dp.createPersonalDetailsRecord(payload, "33d8bd019d4b4662bdbc8efd6048d1d9");
        String[] newPayload = {"abc123","Smith","John","1992-03-28", "14 York Road", "Canterbury", "Kent", "CT1 3TA",
                "01622761848", "07499274509", "Roger Bosch", "07117698761"};

        dp.updatePersonalDetails(newPayload);

        dp.sqlRead("SELECT emergencyContact, emergencyContactNumber FROM PersonalDetails WHERE employeeId = 'abc123'");

        String res1 = null;
        String res2 = null;
        try {
            dp.result.next();
            res1 = dp.result.getString("emergencyContact");
            res2 = dp.result.getString("emergencyContactNumber");

        }
        catch (SQLException e) {}


        assertEquals(res1, "Roger Bosch");
        assertEquals(res2, "07117698761");


        String[] newPayload1 = {"abc123","Smith","John","1992-03-28", "14 York Road", "Canterbury", "Kent", "CT1 3TA",
                "01622761848", "07499274509", "Jane Smith", "07286308174"};

        dp.updatePersonalDetails(newPayload1);

        dp.sqlRead("SELECT emergencyContact, emergencyContactNumber FROM PersonalDetails WHERE employeeId = 'abc123'");

        String res3 = null;
        String res4 = null;
        try {
            dp.result.next();
            res3 = dp.result.getString("emergencyContact");
            res4 = dp.result.getString("emergencyContactNumber");

        }
        catch (SQLException e) {}


        assertEquals(res3, "Jane Smith");
        assertEquals(res4, "07286308174");

        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");
    }

    @org.junit.jupiter.api.Test
    void fetchPersonalDetailsPermissions()
    {
        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");

        createDemoEmployee();

        assertArrayEquals(dp.fetchPersonalDetailsPermissions("abc123"), new String[] {"0", null, null, null, null, null});

        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");
    }

    @org.junit.jupiter.api.Test
    void fetchPersonalDetails()
    {
        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");

        createDemoEmployee();

        assertArrayEquals(dp.fetchPersonalDetails("abc123"), new String[] {"abc123","Smith","John","1992-03-28", "14 York Road", "Canterbury", "Kent", "CT1 3TA",
                "01634289431", "07499274509", "Jane Smith", "07286308174"});

        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");


    }

    @org.junit.jupiter.api.Test
    void fetchNonExistentPersonalDetails()
    {
        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");

        createDemoEmployee();

        assertArrayEquals(dp.fetchPersonalDetails("def754"), null);

        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");
    }

    @org.junit.jupiter.api.Test
    void isLoggedInTrue()
    {
        dp.sqlUpdate("DELETE FROM Session");
        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");

        createDemoEmployee();
        dp.createSession("abc123", "9d33cb975ff14580a3fb405efbc2cf22");

        assertTrue(dp.isLoggedIn("abc123", "9d33cb975ff14580a3fb405efbc2cf22"));

        dp.sqlUpdate("DELETE FROM Session");
        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");
    }

    @org.junit.jupiter.api.Test
    void isLoggedInFalse()
    {
        dp.sqlUpdate("DELETE FROM Session");
        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");

        createDemoEmployee();
        dp.createSession("abc123", "9d33cb975ff14580a3fb405efbc2cf22");

        assertFalse(dp.isLoggedIn("def456", "9d33cb975ff14580a3fb405efbc2cf22"));

        dp.sqlUpdate("DELETE FROM Session");
        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");
    }

    @org.junit.jupiter.api.Test
    void isLoggedInAfterLogout()
    {
        dp.sqlUpdate("DELETE FROM Session");
        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");

        createDemoEmployee();
        dp.createSession("abc123", "9d33cb975ff14580a3fb405efbc2cf22");

        dp.deleteSession("9d33cb975ff14580a3fb405efbc2cf22");

        assertFalse(dp.isLoggedIn("abc123", "9d33cb975ff14580a3fb405efbc2cf22"));

        dp.sqlUpdate("DELETE FROM Session");
        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");
    }

    @org.junit.jupiter.api.Test
    void setupDatabase()
    {
        dp.sqlUpdate("DROP TABLE AuthorisationLog");
        dp.sqlUpdate("DROP TABLE AuthenticationLog");
        dp.sqlUpdate("DROP TABLE Session");
        dp.sqlUpdate("DROP TABLE PersonalDetails");
        dp.sqlUpdate("DROP TABLE Permissions");
        dp.sqlUpdate("DROP TABLE Documents");
        dp.sqlUpdate("DROP TABLE User");

        dp.setupDatabase();
        createDemoEmployee();

        assertTrue(dp.checkEmployeeId("abc123"));

        dp.sqlUpdate("DELETE FROM Session");
        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");

    @Test
    void createReview() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        assertTrue(dp.createReview(payload));
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }

    @Test
    void createDuplicateReview() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertFalse(dp.createReview(payload));
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }

    @Test
    void fetchReviewDocumentId() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertEquals("69d76bd5a1ae48a284587698cf980fa6", dp.fetchReviewDocumentId("hre123", "2020-03-23"));
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }

    @Test
    void fetchNonExistentReviewDocumentId() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6",  "hrm123", "itm123"};
        dp.createReview(payload);
        assertNull(dp.fetchReviewDocumentId("hre123", "2021-01-01"));
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }

    @Test
    void isReviewer() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertTrue(dp.isReviewer("69d76bd5a1ae48a284587698cf980fa6", "hrm123"));
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }

    @Test
    void isNotReviewer() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertFalse(dp.isReviewer("69d76bd5a1ae48a284587698cf980fa6", "ite123"));
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }

    @Test
    void isNonexistentReviewer() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertFalse(dp.isReviewer("69d76bd5a1ae48a284587698cf980fa6", "abc456"));
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }

    @Test
    void isReviewerOnNonexistentDocument() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertFalse(dp.isReviewer("c6d2ee23175f434781f79eb0b6f471b6", "hrm123"));
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }

    @Test
    void isReviewee() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertTrue(dp.isReviewee("69d76bd5a1ae48a284587698cf980fa6", "hre123"));
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }

    @Test
    void isNotReviewee() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertFalse(dp.isReviewee("69d76bd5a1ae48a284587698cf980fa6", "ite123"));
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }

    @Test
    void isNonexistentReviewee() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertFalse(dp.isReviewee("69d76bd5a1ae48a284587698cf980fa6", "abc456"));
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }

    @Test
    void isRevieweeOnNonexistentDocument() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertFalse(dp.isReviewer("c6d2ee23175f434781f79eb0b6f471b6", "hre123"));
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }

    @Test
    void fetchReview() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        String[] completePayload = new String[] {"hre123",
                "2020-03-23",
                "69d76bd5a1ae48a284587698cf980fa6",
                "hrm123",
                "itm123",
                null, null, null, null, null, null, null};
        assertArrayEquals(completePayload, dp.fetchReview("69d76bd5a1ae48a284587698cf980fa6"));
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }

    @Test
    void fetchNonexistentReview() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertNull(dp.fetchReview("c6d2ee23175f434781f79eb0b6f471b6"));
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }

    @Test
    void updateReviewPayloadOnly() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        String[] updatedPayload = {"hre123",
                "2020-03-23",
                "69d76bd5a1ae48a284587698cf980fa6",
                "hrm123",
                "itm123",
                "true", "true", "true",
                "2020-03-22",
                "They did good",
                "They can improve",
                "Stay In Post"};
        assertTrue(dp.updateReview("69d76bd5a1ae48a284587698cf980fa6", updatedPayload, new ArrayList<String[]>(), new ArrayList<String>()));
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }

    @Test
    void updateReviewFullDataSet() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        String[] updatedPayload = {"hre123",
                "2020-03-23",
                "69d76bd5a1ae48a284587698cf980fa6",
                "hrm123",
                "itm123",
                "true", "true", "true",
                "2020-03-22",
                "They did good",
                "They can improve",
                "Stay In Post"};
        ArrayList<String[]> updatedPastPerformance = new ArrayList<>();
        updatedPastPerformance.add(new String[]{"do better", "did better"});
        updatedPastPerformance.add(new String[]{"do more", "did more"});
        updatedPastPerformance.add(new String[]{"do things", "did things"});

        ArrayList<String> updatedFuturePerformance = new ArrayList<>();
        updatedFuturePerformance.add("work better");
        updatedFuturePerformance.add("work more");

        assertTrue(dp.updateReview("69d76bd5a1ae48a284587698cf980fa6", updatedPayload, updatedPastPerformance, updatedFuturePerformance));
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }

    @Test
    void fetchPastPerformance() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        String[] updatedPayload = {"hre123",
                "2020-03-23",
                "69d76bd5a1ae48a284587698cf980fa6",
                "hrm123",
                "itm123",
                "true", "true", "true",
                "2020-03-22",
                "They did good",
                "They can improve",
                "Stay In Post"};
        ArrayList<String[]> updatedPastPerformance = new ArrayList<>();
        updatedPastPerformance.add(new String[]{"do better", "did better"});
        updatedPastPerformance.add(new String[]{"do more", "did more"});
        updatedPastPerformance.add(new String[]{"do things", "did things"});

        ArrayList<String> updatedFuturePerformance = new ArrayList<>();
        updatedFuturePerformance.add("work better");
        updatedFuturePerformance.add("work more");

        dp.updateReview("69d76bd5a1ae48a284587698cf980fa6", updatedPayload, updatedPastPerformance, updatedFuturePerformance);

        assertArrayEquals(updatedPastPerformance.toArray(), dp.fetchPastPerformance("69d76bd5a1ae48a284587698cf980fa6").toArray());
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }

    @Test
    void fetchNonexistentPastPerformance() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        String[] updatedPayload = {"hre123",
                "2020-03-23",
                "69d76bd5a1ae48a284587698cf980fa6",
                "hrm123",
                "itm123",
                "true", "true", "true",
                "2020-03-22",
                "They did good",
                "They can improve",
                "Stay In Post"};
        ArrayList<String[]> updatedPastPerformance = new ArrayList<>();
        updatedPastPerformance.add(new String[]{"do better", "did better"});
        updatedPastPerformance.add(new String[]{"do more", "did more"});
        updatedPastPerformance.add(new String[]{"do things", "did things"});

        ArrayList<String> updatedFuturePerformance = new ArrayList<>();
        updatedFuturePerformance.add("work better");
        updatedFuturePerformance.add("work more");

        dp.updateReview("69d76bd5a1ae48a284587698cf980fa6", updatedPayload, updatedPastPerformance, updatedFuturePerformance);

        assertArrayEquals(new String[0], dp.fetchPastPerformance("c6d2ee23175f434781f79eb0b6f471b6").toArray());
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }

    @Test
    void fetchFuturePerformance() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        String[] updatedPayload = {"hre123",
                "2020-03-23",
                "69d76bd5a1ae48a284587698cf980fa6",
                "hrm123",
                "itm123",
                "true", "true", "true",
                "2020-03-22",
                "They did good",
                "They can improve",
                "Stay In Post"};
        ArrayList<String[]> updatedPastPerformance = new ArrayList<>();
        updatedPastPerformance.add(new String[]{"do better", "did better"});
        updatedPastPerformance.add(new String[]{"do more", "did more"});
        updatedPastPerformance.add(new String[]{"do things", "did things"});

        ArrayList<String> updatedFuturePerformance = new ArrayList<>();
        updatedFuturePerformance.add("work better");
        updatedFuturePerformance.add("work more");

        dp.updateReview("69d76bd5a1ae48a284587698cf980fa6", updatedPayload, updatedPastPerformance, updatedFuturePerformance);

        assertArrayEquals(updatedFuturePerformance.toArray(), dp.fetchFuturePerformance("69d76bd5a1ae48a284587698cf980fa6").toArray());
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }

    @Test
    void fetchNonexistentFuturePerformance() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        String[] updatedPayload = {"hre123",
                "2020-03-23",
                "69d76bd5a1ae48a284587698cf980fa6",
                "hrm123",
                "itm123",
                "true", "true", "true",
                "2020-03-22",
                "They did good",
                "They can improve",
                "Stay In Post"};
        ArrayList<String[]> updatedPastPerformance = new ArrayList<>();
        updatedPastPerformance.add(new String[]{"do better", "did better"});
        updatedPastPerformance.add(new String[]{"do more", "did more"});
        updatedPastPerformance.add(new String[]{"do things", "did things"});

        ArrayList<String> updatedFuturePerformance = new ArrayList<>();
        updatedFuturePerformance.add("work better");
        updatedFuturePerformance.add("work more");

        dp.updateReview("69d76bd5a1ae48a284587698cf980fa6", updatedPayload, updatedPastPerformance, updatedFuturePerformance);

        assertArrayEquals(new String[0], dp.fetchFuturePerformance("c6d2ee23175f434781f79eb0b6f471b6").toArray());
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }