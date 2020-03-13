import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AuthoriseTest {
    User hrEmployee;
    User hrManager;
    User miles;
    User itEmployee;
    User itManager;

    ArrayList<String[]> MainReviewCreatePayload = new ArrayList<String[]>();
    ArrayList<String[]> MainReviewRestPayLoad = new ArrayList<String[]>();
    ArrayList<String[]> updatedPastPerformanceEntries = new ArrayList<String[]>();
    ArrayList<ArrayList<String[]>> updatedPastPerformances = new ArrayList<>();

    String[] joinArrays(String[] first, String[] second)
    {
        String[] returned = new String[first.length + second.length];
        for (int i = 0; i < returned.length; i++)
        {
            if (i <= first.length-1)
                returned[i] = first[i];
            else
                returned[i] = second[i];
        }
        return returned;
    }
    String getMainReviewDocId()
    {
        return MainReviewCreatePayload.get(0)[2];
    }

    void setupReviewMainMandatoryPayloads(String empId)
    {
        // 0: Expected
        MainReviewCreatePayload.add(new String[] { empId, "2020-12-31", User.generateSalt(), "hrm123", "dir123" });
        // 1: Missing Reviewee
        MainReviewCreatePayload.add(new String[] { null, "2020-12-31", User.generateSalt(), "hrm123", "dir123" });
        // 2: Missing Due Date
        MainReviewCreatePayload.add(new String[] { empId, null, User.generateSalt(), "hrm123", "dir123" });
        // 3: Missing docId
        MainReviewCreatePayload.add(new String[] { "hre123", "2020-12-31", null, "hrm123", "dir123" });
        // 4, 5, 6: Missing Reviewers
        MainReviewCreatePayload.add(new String[] { empId, "2020-12-31", User.generateSalt(), null, "dir123" });
        MainReviewCreatePayload.add(new String[] { empId, "2020-12-31", User.generateSalt(), "hrm123", null });
        MainReviewCreatePayload.add(new String[] { empId, "2020-12-31", User.generateSalt(), null, null });
    }

    void setupReviewMainOptionalPayloads()
    {
        // 0: Filled, not signed
        MainReviewRestPayLoad.add(new String[] { "false", "false", "false", "2020-03-13", "Some text that makes up the summary and is irrelevant for testing", "reviewercomments that are irrelevent for testing", "recommendation that is irrelevant for testing"});
        // 1: Filled, signed
        MainReviewRestPayLoad.add(new String[] { "true", "true", "true", "2020-03-13", "Some text that makes up the summary and is irrelevant for testing", "reviewercomments that are irrelevent for testing", "recommendation that is irrelevant for testing"});
        // 2: No date, not signed
        MainReviewRestPayLoad.add(new String[] { "false", "false", "false", null, "Some text that makes up the summary and is irrelevant for testing", "reviewercomments that are irrelevent for testing", "recommendation that is irrelevant for testing"});
        // 3: no comments
        MainReviewRestPayLoad.add(new String[] { "false", "false", "false", "2020-03-13", null, null, null});
        // 4: No dates or comments
        MainReviewRestPayLoad.add(new String[] { "false", "false", "false", null, null, null, null});
        // 5: Empty
        MainReviewRestPayLoad.add(new String[] { null, null, null, null, null, null, null});
    }

    void setUpdatedPastPerformances()
    {
        updatedPastPerformanceEntries.add(new String[] { getMainReviewDocId(), "0", "Some objective that is irrelevant to testing"});
        updatedPastPerformanceEntries.add(new String[] { getMainReviewDocId(), "1", "Some other objective that is irrelevant to testing"});
        updatedPastPerformanceEntries.add(new String[] { getMainReviewDocId(), "2", "Some other objective that is irrelevant to testing"});
        updatedPastPerformances.add(updatedPastPerformanceEntries.get(0));
        updatedPastPerformanceEntries.removeAll();
    }

    @BeforeEach
    void setup() {
        dbSetup();
        setupReviewMainMandatoryPayloads("hre123");
        setupReviewMainOptionalPayloads();

        if(!Authenticate.addNewUser("dir123", "password", null, Position.Department.HR, Position.Role.Director))
            System.out.println("Failed to add user miles | dir123");
        if (!Authenticate.addNewUser("hrm123", "password", "dir123", Position.Department.HR, Position.Role.Manager))
            System.out.println("Failed to add user hrm123");
        if(!Authenticate.addNewUser("hre123", "password", "hrm123", Position.Department.HR, Position.Role.Employee))
            System.out.println("Failed to add user hre123");
        if (!Authenticate.addNewUser("itm123", "password", "dir123", Position.Department.IT, Position.Role.Manager))
            System.out.println("Failed to add user itm123");
        if (!Authenticate.addNewUser("ite123", "password", "itm123", Position.Department.IT, Position.Role.Employee))
            System.out.println("Failed to add user ite123");

        miles = Authenticate.login("dir123", "password");
        hrManager = Authenticate.login("hrm123", "password");
        hrEmployee = Authenticate.login("hre123", "password");
        itManager = Authenticate.login("itm123", "password");
        itEmployee = Authenticate.login("ite123", "password");
    }

    @Test
    void createPerformanceReviewNonHR()
    {
        assertFalse(Authorise.createPerformanceReview(hrEmployee, MainReviewCreatePayload.get((1))));
    }

    @Test
    void createPerformanceReview() {

    }

    @Test
    void readPerformanceReview() {
    }

    @Test
    void updatePerformanceReview() {
    }


    static boolean checkIsFirstBoot()
    {
        File dbFile = new File("./databases/yuconz.db");
        return dbFile.exists();
    }
    static void dbSetup()
    {
        File database = new File("./databases/yuconz.db");
        if (!database.delete())
            System.out.println("Failed to delete the old database file");

        // Main method copied over
        if(!checkIsFirstBoot())
        {
            File dir = new File("./databases");
            dir.mkdir();
            DatabaseParser dp = new DatabaseParser();
            dp.setupDatabase();
        }
    }
}