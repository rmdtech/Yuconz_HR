import org.junit.jupiter.api.Test;

import java.io.File;

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
        dbFile.delete();
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
        String[] payload = {"hre123", "2020-03-23", "hrm123", "itm123", "69d76bd5a1ae48a284587698cf980fa6"};
        assertTrue(dp.createReview(payload));
        deleteDatabase();
    }

    @Test
    void fetchReviewDocumentId() {
    }

    @Test
    void isReviewer() {
    }

    @Test
    void isReviewee() {
    }

    @Test
    void fetchReview() {
    }

    @Test
    void fetchPastPerformance() {
    }

    @Test
    void fetchFuturePerformance() {
    }

    @Test
    void updateReview() {
    }
}