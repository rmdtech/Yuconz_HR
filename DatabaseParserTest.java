import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseParserTest {

    static DatabaseParser dp = null;

    static void initDatabase() {
        String[] arguments = new String[] {"123"};
        Main.main(arguments);
        dp = new DatabaseParser();
    }

    static void deleteDatabase() {
        File dbFile = new File("./databases/yuconz.db");
        if(dbFile.exists()){
            System.out.println("db File Exists");
            if(dbFile.delete())
            {
                System.out.println("success");
            }
            else {
                System.out.println("not successful");
            }
        }
        else {
            System.out.println("db File does not exist");
        }

    }

    static void generateTestUsers() {
        Authenticate.addNewUser("dir123", "password", null, Position.Department.HR, Position.Role.Director);
        Authenticate.addNewUser("hrm123", "password", "dir123", Position.Department.HR, Position.Role.Manager);
        Authenticate.addNewUser("hre123", "password", "hrm123", Position.Department.HR, Position.Role.Employee);
        Authenticate.addNewUser("itm123", "password", "dir123", Position.Department.IT, Position.Role.Manager);
        Authenticate.addNewUser("ite123", "password", "itm123", Position.Department.IT, Position.Role.Employee);
    }

    @Test
    void createReview() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        assertTrue(dp.createReview(payload));
        deleteDatabase();
    }

    @Test
    void createDuplicateReview() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertFalse(dp.createReview(payload));
        deleteDatabase();
    }

    @Test
    void fetchReviewDocumentId() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertEquals("69d76bd5a1ae48a284587698cf980fa6", dp.fetchReviewDocumentId("hre123", "2020-03-23"));
        deleteDatabase();
    }

    @Test
    void fetchNonExistentReviewDocumentId() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6",  "hrm123", "itm123"};
        dp.createReview(payload);
        assertNull(dp.fetchReviewDocumentId("hre123", "2021-01-01"));
        deleteDatabase();
    }

    @Test
    void isReviewer() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertTrue(dp.isReviewer("69d76bd5a1ae48a284587698cf980fa6", "hrm123"));
        deleteDatabase();
    }

    @Test
    void isNotReviewer() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertFalse(dp.isReviewer("69d76bd5a1ae48a284587698cf980fa6", "ite123"));
        deleteDatabase();
    }

    @Test
    void isNonexistentReviewer() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertFalse(dp.isReviewer("69d76bd5a1ae48a284587698cf980fa6", "abc456"));
        deleteDatabase();
    }

    @Test
    void isReviewerOnNonexistentDocument() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertFalse(dp.isReviewer("c6d2ee23175f434781f79eb0b6f471b6", "hrm123"));
        deleteDatabase();
    }

    @Test
    void isReviewee() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertTrue(dp.isReviewee("69d76bd5a1ae48a284587698cf980fa6", "hre123"));
        deleteDatabase();
    }

    @Test
    void isNotReviewee() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertFalse(dp.isReviewee("69d76bd5a1ae48a284587698cf980fa6", "ite123"));
        deleteDatabase();
    }

    @Test
    void isNonexistentReviewee() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertFalse(dp.isReviewee("69d76bd5a1ae48a284587698cf980fa6", "abc456"));
        deleteDatabase();
    }

    @Test
    void isRevieweeOnNonexistentDocument() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertFalse(dp.isReviewer("c6d2ee23175f434781f79eb0b6f471b6", "hre123"));
        deleteDatabase();
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
                "0",
                "0",
                "0",
                null, null, null, null};
        assertArrayEquals(completePayload, dp.fetchReview("69d76bd5a1ae48a284587698cf980fa6"));
        deleteDatabase();
    }

    @Test
    void fetchNonexistentReview() {
        initDatabase();
        generateTestUsers();
        String[] payload = {"hre123", "2020-03-23", "69d76bd5a1ae48a284587698cf980fa6", "hrm123", "itm123"};
        dp.createReview(payload);
        assertNull(dp.fetchReview("c6d2ee23175f434781f79eb0b6f471b6"));
        deleteDatabase();
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
                "1", "1", "1",
                "2020-03-22",
                "They did good",
                "They can improve",
                "Stay In Post"};
        assertTrue(dp.updateReview("69d76bd5a1ae48a284587698cf980fa6", updatedPayload, new ArrayList<String[]>(), new ArrayList<String>()));
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
                "1", "1", "1",
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
    }

    @Test
    void fetchPastPerformance() {
    }

    @Test
    void fetchFuturePerformance() {
    }
}