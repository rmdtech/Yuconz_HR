import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AuthoriseTest {
    User hrEmployee;
    User hrManager;
    User miles;
    ArrayList<String[]> MainReviewCreatePayload = new ArrayList<String[]>();

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
    String getMainReviewDocId(String[] mainDoc)
    {
        return mainDoc[2];
    }
    void setupReviewMainPayloads(String empId)
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
        MainReviewCreatePayload.add(new String[] { empId, "2020-12-31", User.generateSalt(), "null", "dir123" });
        MainReviewCreatePayload.add(new String[] { empId, "2020-12-31", User.generateSalt(), "hrm123", null });
        MainReviewCreatePayload.add(new String[] { empId, "2020-12-31", User.generateSalt(), null, null });
    }


    @BeforeEach
    void setup() {
        dbSetup();
        setupReviewMainPayloads(hrEmployee.getEmployeeId());

        if(!Authenticate.addNewUser("dir123", "password", null, Position.Department.HR, Position.Role.Director))
            System.out.println("Failed to add user miles | dir123");
        if (!Authenticate.addNewUser("hrm123", "password", "dir123", Position.Department.HR, Position.Role.Manager))
            System.out.println("Failed to add user hrm123");
        if(!Authenticate.addNewUser("hre123", "password", "hrm123", Position.Department.HR, Position.Role.Employee))
            System.out.println("Failed to add user hre123");

        miles = Authenticate.login("dir123", "password");
        hrManager = Authenticate.login("hrm123", "password");
        hrEmployee = Authenticate.login("hre123", "password");
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