import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.sql.SQLException;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseParserTest {

    static DatabaseParser dp = null;

    static void initDatabase() {
        String[] arguments = new String[] {"123"};
        Main.main(arguments);
        dp = new DatabaseParser();
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
                "0",
                "0",
                "0",
                null, null, null, null};
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
                "1", "1", "1",
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

        dp.updateReview("69d76bd5a1ae48a284587698cf980fa6", updatedPayload, updatedPastPerformance, updatedFuturePerformance);

        assertArrayEquals(new String[0], dp.fetchFuturePerformance("c6d2ee23175f434781f79eb0b6f471b6").toArray());
        //DATABASE FILE MUST BE MANUALLY DELETED AFTER TEST RUNS
    }
}