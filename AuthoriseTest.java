import org.junit.jupiter.api.AfterEach;
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
    DatabaseParser dp;

    ArrayList<String[]> MainReviewCreatePayload = new ArrayList<String[]>();
    ArrayList<String[]> MainReviewRestPayLoad = new ArrayList<String[]>();
    ArrayList<String[]> PastPerformanceEntries = new ArrayList<String[]>();
    ArrayList<ArrayList<String[]>> PastPerformances = new ArrayList<>();
    ArrayList<String> FuturePerformanceEntries = new ArrayList<>();
    ArrayList<ArrayList<String>> FuturePerformances = new ArrayList<>();

    @BeforeEach
    void setup() {
        dbSetup();

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

        System.out.println("\n---- END OF SETUP OUTPUT ----\n");
    }

    @Test
    void createPerformanceReview() {
        // Expected use case where HR Employees can
        setupReviewMainMandatoryPayloads("ite123");
        assertTrue(Authorise.createPerformanceReview(hrEmployee, MainReviewCreatePayload.get(0)));
    }

    @Test
    void createPerformanceReviewNonHR()
    {
        setupReviewMainMandatoryPayloads("hre123");
        assertFalse(Authorise.createPerformanceReview(itEmployee, MainReviewCreatePayload.get((0))));
        assertFalse(Authorise.createPerformanceReview(itManager, MainReviewCreatePayload.get(0)));
    }

    @Test
    void createPerformanceReviewReviewerIsReviewee()
    {
        // Expected use case where HR Employees can
        setupReviewMainMandatoryPayloads("hrm123");
        assertFalse(Authorise.createPerformanceReview(hrManager, MainReviewCreatePayload.get(6)));
    }

    @Test
    void createPerformanceReviewHRMCreateReview()
    {
        setupReviewMainMandatoryPayloads("itm123");
        assertTrue(Authorise.createPerformanceReview(hrManager, MainReviewCreatePayload.get(0)));
    }

    @Test
    void createPerformanceReviewHRDCreateReview()
    {
        setupReviewMainMandatoryPayloads("hrm123");
        assertTrue(Authorise.createPerformanceReview(miles, MainReviewCreatePayload.get(0)));
    }

    @Test
    void createInvalidPerformanceReview()
    {
        setupReviewMainMandatoryPayloads("hre123");
        assertFalse(Authorise.createPerformanceReview(hrEmployee, MainReviewCreatePayload.get(1)));
        assertFalse(Authorise.createPerformanceReview(hrEmployee, MainReviewCreatePayload.get(2)));
        assertFalse(Authorise.createPerformanceReview(hrEmployee, MainReviewCreatePayload.get(3)));
        assertFalse(Authorise.createPerformanceReview(hrEmployee, MainReviewCreatePayload.get(4)));
        assertFalse(Authorise.createPerformanceReview(hrEmployee, MainReviewCreatePayload.get(5)));
    }

    @Test
    void readPerformanceReview() {
        dp = new DatabaseParser();
        setupReviewMainMandatoryPayloads("ite123");
        setupReviewMainOptionalPayloads();
        setupFuturePerformances();
        setupPastPerformances();

        String[] MainDocUpdated = new String[12];
        MainDocUpdated[0] = MainReviewCreatePayload.get(0)[0];
        MainDocUpdated[1] = MainReviewCreatePayload.get(0)[1];
        MainDocUpdated[2] = MainReviewCreatePayload.get(0)[2];
        MainDocUpdated[3] = dp.fetchDirectSupervisor(MainReviewCreatePayload.get(0)[0]);
        MainDocUpdated[4] = MainReviewCreatePayload.get(0)[3];

        Authorise.createPerformanceReview(hrManager, MainReviewCreatePayload.get(0));
        dp.updateReview(getMainReviewDocId(), joinArrays(MainDocUpdated, MainReviewRestPayLoad.get(0)), PastPerformances.get(0), FuturePerformances.get(0));

        ArrayList<String[]> expectedPastPerformance = PastPerformances.get(0);
        ArrayList<String> expectedFuturePerformance = FuturePerformances.get(0);

        Authorise.readPerformanceReview(itEmployee, "ite123", "2020-12-31");
        String[] actualMainDoc = Authorise.readReviewMain(getMainReviewDocId());
        ArrayList<String[]> actualPastPerformance = Authorise.readPastPerformance(getMainReviewDocId());
        ArrayList<String> actualFuturePerformance = Authorise.readFuturePerformance(getMainReviewDocId());

        assertArrayEquals(MainDocUpdated, actualMainDoc);
        assertEquals(expectedFuturePerformance, actualFuturePerformance);
        assertEquals(expectedPastPerformance, actualPastPerformance);
    }

    @Test
    void readPerformanceReviewNonHRonSelf()
    {
        writeReview();
        Authorise.readPerformanceReview(itEmployee, "ite123", "2020-12-31");
        String[] actualMainDoc = Authorise.readReviewMain(getMainReviewDocId());
        ArrayList<String[]> actualPastPerformance = Authorise.readPastPerformance(getMainReviewDocId());
        ArrayList<String> actualFuturePerformance = Authorise.readFuturePerformance(getMainReviewDocId());

        String[] MainDocUpdated = new String[12];
        MainDocUpdated[0] = MainReviewCreatePayload.get(0)[0];
        MainDocUpdated[1] = MainReviewCreatePayload.get(0)[1];
        MainDocUpdated[2] = MainReviewCreatePayload.get(0)[2];
        MainDocUpdated[3] = dp.fetchDirectSupervisor(MainReviewCreatePayload.get(0)[0]);
        MainDocUpdated[4] = MainReviewCreatePayload.get(0)[3];

        ArrayList<String[]> expectedPastPerformance = PastPerformances.get(0);
        ArrayList<String> expectedFuturePerformance = FuturePerformances.get(0);

        assertArrayEquals(MainDocUpdated, actualMainDoc);
        assertEquals(expectedFuturePerformance, actualFuturePerformance);
        assertEquals(expectedPastPerformance, actualPastPerformance);
    }

    @Test
    void readPerformanceReviewNonHRonOther()
    {
        // Write Performance Review
        dp = new DatabaseParser();
        setupReviewMainMandatoryPayloads("hre123");
        setupReviewMainOptionalPayloads();
        setupFuturePerformances();
        setupPastPerformances();

        String[] MainDocUpdated = new String[12];
        MainDocUpdated[0] = MainReviewCreatePayload.get(0)[0];
        MainDocUpdated[1] = MainReviewCreatePayload.get(0)[1];
        MainDocUpdated[2] = MainReviewCreatePayload.get(0)[2];
        MainDocUpdated[3] = dp.fetchDirectSupervisor(MainReviewCreatePayload.get(0)[0]);
        MainDocUpdated[4] = MainReviewCreatePayload.get(0)[3];

        Authorise.createPerformanceReview(hrManager, MainReviewCreatePayload.get(0));
        dp.updateReview(getMainReviewDocId(), joinArrays(MainDocUpdated, MainReviewRestPayLoad.get(0)), PastPerformances.get(0), FuturePerformances.get(0));

        Authorise.readPerformanceReview(itEmployee, "hre123", "2020-12-31");
        String[] actualMainDoc = Authorise.readReviewMain(getMainReviewDocId());
        ArrayList<String[]> actualPastPerformance = Authorise.readPastPerformance(getMainReviewDocId());
        ArrayList<String> actualFuturePerformance = Authorise.readFuturePerformance(getMainReviewDocId());

        ArrayList<String[]> expectedPastPerformance = PastPerformances.get(0);
        ArrayList<String> expectedFuturePerformance = FuturePerformances.get(0);

        assertNull(actualMainDoc);
        assertNotEquals(expectedFuturePerformance, actualFuturePerformance);
        assertNotEquals(expectedPastPerformance, actualPastPerformance);
    }

    @Test
    void updatePerformanceReview() {
        /* To Test:
        [ ] Stakeholders involved in the Review process must be able to sign a review off
        [ ] Reviewee able to make changes to their own document
        [ ] Reviewer(s) able to make changes to a a Review they're involved in
        [ ] Document becoming read only after all signatures have been provided
         */
    }

    // THIS
    // IS
    // WHERE
    // THE
    // TEST
    // DATA
    // SETUP
    // BEGINS
    // AND
    // WE
    // JUST
    // MAKE
    // THIS
    // VISIBLE
    // WHEN
    // SCROLLING
    String[] joinArrays(String[] first, String[] second)
    {
        String[] returned = new String[first.length + second.length];
        for (int i = 0; i < returned.length; i++)
        {
            if (i < first.length)
                returned[i] = first[i];
            else
                returned[i] = second[i-first.length];
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
        MainReviewCreatePayload.add(new String[] { empId, "2020-12-31", "3534c934edef4388b1c404d2d8064a21", "dir123" });
        // 1: Missing Reviewee
        MainReviewCreatePayload.add(new String[] { null, "2020-12-31", "3534c934edef4388b1c404d2d8064a21", "dir123" });
        // 2: Missing Due Date
        MainReviewCreatePayload.add(new String[] { empId, null, "3534c934edef4388b1c404d2d8064a21", "dir123" });
        // 3: Missing docId
        MainReviewCreatePayload.add(new String[] { "hre123", "2020-12-31", null, "dir123" });
        // 4, Missing 2nd Reviewer
        MainReviewCreatePayload.add(new String[] { empId, "2020-12-31", "3534c934edef4388b1c404d2d8064a21", null });
        // 5: All null
        MainReviewCreatePayload.add(new String[] { null, null, null,  null });
        // 6: Expected but with a different second Reviewer
        MainReviewCreatePayload.add(new String[] { empId, "2020-12-31", "3534c934edef4388b1c404d2d8064a21", "hrm123" });

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

    void setupPastPerformances()
    {
        PastPerformanceEntries.add(new String[] { getMainReviewDocId(), "Some objective that is irrelevant to testing", "Some achievement"});
        PastPerformanceEntries.add(new String[] { getMainReviewDocId(), "Some other objective that is irrelevant to testing", "Some other achievement"});
        PastPerformanceEntries.add(new String[] { getMainReviewDocId(), "Another objective that is irrelevant to testing", "Another achievement"});

        // 0: Expected, full payload
        PastPerformances.add(PastPerformanceEntries);
        PastPerformanceEntries.clear();

        PastPerformanceEntries.add(new String[] { null, "Some objective that is irrelevant to testing", "Some achievement"});
        PastPerformanceEntries.add(new String[] { null, "Some other objective that is irrelevant to testing", "Some other achievement"});
        PastPerformanceEntries.add(new String[] { null, "Another objective that is irrelevant to testing", "Another achievement"});

        // 1: invalid document ID, full payload
        PastPerformances.add(PastPerformanceEntries);
        PastPerformanceEntries.clear();

        PastPerformanceEntries.add(new String[] { getMainReviewDocId(), null, "Some achievement"});
        PastPerformanceEntries.add(new String[] { getMainReviewDocId(), null, "Some other achievement"});
        PastPerformanceEntries.add(new String[] { getMainReviewDocId(), null, "Another achievement"});

        // 2: Number set to null. (Doing this because number is a FK in the DB
        PastPerformances.add(PastPerformanceEntries);
        PastPerformanceEntries.clear();

        PastPerformanceEntries.add(new String[] { getMainReviewDocId(), "0", null});
        PastPerformanceEntries.add(new String[] { getMainReviewDocId(), "1", null});
        PastPerformanceEntries.add(new String[] { getMainReviewDocId(), "2", null});

        // 3: Objectives are null
        PastPerformances.add(PastPerformanceEntries);
        PastPerformanceEntries.clear();

        PastPerformanceEntries.add(new String[] { null, null, null});
        PastPerformanceEntries.add(new String[] { null, null, null});
        PastPerformanceEntries.add(new String[] { null, null, null});

        // 4: All content set to null
        PastPerformances.add(PastPerformanceEntries);
        PastPerformanceEntries.clear();
    }

    void setupFuturePerformances()
    {
        FuturePerformanceEntries.add("A future objective");
        FuturePerformanceEntries.add("Another future objective");
        FuturePerformanceEntries.add("Other future objective");

        // 0: Expected
        FuturePerformances.add(FuturePerformanceEntries);
        FuturePerformanceEntries.clear();

        FuturePerformanceEntries.add(null);
        FuturePerformanceEntries.add(null);
        FuturePerformanceEntries.add(null);

        // 1: Only contents containing null
        FuturePerformances.add(FuturePerformanceEntries);
        FuturePerformanceEntries.clear();
    }

    void writeReview()
    {
        dp = new DatabaseParser();
        setupReviewMainMandatoryPayloads("ite123");
        setupReviewMainOptionalPayloads();
        setupFuturePerformances();
        setupPastPerformances();

        String[] MainDocUpdated = new String[12];
        MainDocUpdated[0] = MainReviewCreatePayload.get(0)[0];
        MainDocUpdated[1] = MainReviewCreatePayload.get(0)[1];
        MainDocUpdated[2] = MainReviewCreatePayload.get(0)[2];
        MainDocUpdated[3] = dp.fetchDirectSupervisor(MainReviewCreatePayload.get(0)[0]);
        MainDocUpdated[4] = MainReviewCreatePayload.get(0)[3];

        Authorise.createPerformanceReview(hrManager, MainReviewCreatePayload.get(0));
        dp.updateReview(getMainReviewDocId(), joinArrays(MainDocUpdated, MainReviewRestPayLoad.get(0)), PastPerformances.get(0), FuturePerformances.get(0));
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