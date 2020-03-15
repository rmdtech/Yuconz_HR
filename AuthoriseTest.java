import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

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
    ArrayList<ArrayList<String[]>> PastPerformances = new ArrayList<>();
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

        // Printing the values to confirm tests are correct
        System.out.println("MainDoc:        " + Arrays.toString(MainDocUpdated));
        System.out.println("actualDoc:      " + Arrays.toString(actualMainDoc));

        System.out.println("expectedFuture: " + expectedFuturePerformance);
        System.out.println("actualFuture:   " + actualFuturePerformance);

        System.out.println("expectedPast:   ");
        for (int i = 0; i < expectedPastPerformance.size(); i++)
            System.out.println("- " + expectedPastPerformance.get(i)[0] + " " + expectedPastPerformance.get(i)[1]);

        System.out.println("actualPast:     ");
        for (int i = 0; i < actualPastPerformance.size(); i++)
            System.out.println("- " + actualPastPerformance.get(i)[0] + " " + actualPastPerformance.get(i)[1]);

        assertArrayEquals(MainDocUpdated, actualMainDoc);
        assertEquals(expectedFuturePerformance, actualFuturePerformance);
        assertArrayEquals(expectedPastPerformance.toArray(), actualPastPerformance.toArray());
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
        System.out.println("MainDoc:        " + Arrays.toString(MainDocUpdated));
        System.out.println("actualDoc:      " + Arrays.toString(actualMainDoc));
        System.out.println("expectedFuture: " + expectedFuturePerformance);
        System.out.println("actualFuture:   " + actualFuturePerformance);
        System.out.println("expectedPast:   ");
        for (int i = 0; i < expectedPastPerformance.size(); i++)
            System.out.println("- " + expectedPastPerformance.get(i)[0] + " " + expectedPastPerformance.get(i)[1]);

        System.out.println("actualPast:     ");
        for (int i = 0; i < actualPastPerformance.size(); i++)
            System.out.println("- " + actualPastPerformance.get(i)[0] + " " + actualPastPerformance.get(i)[1]);

        for (int i = 0; i < expectedFuturePerformance.size(); i++)
        {
            assertEquals(expectedFuturePerformance.get(i), actualFuturePerformance.get(i));
        }
        for (int i = 0; i < expectedPastPerformance.size(); i++)
        {
            assertArrayEquals(expectedPastPerformance.get(i), actualPastPerformance.get(i));
        }
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

        String[] actualMainDoc = null;
        ArrayList<String[]> actualPastPerformance = new ArrayList<String[]>();
        ArrayList<String> actualFuturePerformance = new ArrayList<String>();

        assertFalse(Authorise.readPerformanceReview(itEmployee, "hre123", "2020-12-31"));

    }

    @Test
    void updatePerformanceReviewReviewee() {
        dp = new DatabaseParser();
        setupReviewMainMandatoryPayloads("ite123");
        setupReviewMainOptionalPayloads();
        setupPastPerformances();
        setupFuturePerformances();

        // Create a review
        String[] MainDoc = new String[5];
        MainDoc[0] = MainReviewCreatePayload.get(0)[0];
        MainDoc[1] = MainReviewCreatePayload.get(0)[1];
        MainDoc[2] = MainReviewCreatePayload.get(0)[2];
        MainDoc[3] = dp.fetchDirectSupervisor("ite123");
        MainDoc[4] = MainReviewCreatePayload.get(0)[3];
        dp.createReview(MainDoc);
        dp.updateReview(getMainReviewDocId(), joinArrays(MainDoc, MainReviewRestPayLoad.get(0)), PastPerformances.get(0), FuturePerformances.get(0));

        // Update Past Performance
        ArrayList<String[]> updatedPastPerformance = new ArrayList<>();
        updatedPastPerformance.add(new String[] { "An updated objective", "With an updated achievement"} );
        updatedPastPerformance.add(new String[] { "The last objective and achievement" , " have been deleted"});

        // Update Future Performance
        ArrayList<String> updatedFuturePerformance = new ArrayList<String>();
        updatedFuturePerformance.add("An updated achievement");
        updatedFuturePerformance.add("The last achievement has been removed");

        // Test if just updating the information was successful
        assertTrue(Authorise.updatePerformanceReview(itEmployee, joinArrays(MainDoc, MainReviewRestPayLoad.get(0)), updatedPastPerformance, updatedFuturePerformance));
        assertArrayEquals(updatedPastPerformance.toArray(), dp.fetchPastPerformance(getMainReviewDocId()).toArray());
        assertEquals(updatedFuturePerformance, dp.fetchFuturePerformance(getMainReviewDocId()));

        /* To Test:
        [x] Stakeholders involved in the Review process must be able to sign a review off
        [x] Reviewee able to make changes to their own document
        [x] Reviewer(s) able to make changes to a a Review they're involved in
        [x] Document becoming read only after all signatures have been provided
         */
    }

    @Test
    void updatePerformanceReviewReviewer1()
    {
        dp = new DatabaseParser();
        setupReviewMainMandatoryPayloads("ite123");
        setupReviewMainOptionalPayloads();
        setupPastPerformances();
        setupFuturePerformances();

        // Create a review
        String[] MainDoc = new String[5];
        MainDoc[0] = MainReviewCreatePayload.get(0)[0];
        MainDoc[1] = MainReviewCreatePayload.get(0)[1];
        MainDoc[2] = MainReviewCreatePayload.get(0)[2];
        MainDoc[3] = dp.fetchDirectSupervisor("ite123");
        MainDoc[4] = MainReviewCreatePayload.get(0)[3];
        dp.createReview(MainDoc);
        dp.updateReview(getMainReviewDocId(), joinArrays(MainDoc, MainReviewRestPayLoad.get(0)), PastPerformances.get(0), FuturePerformances.get(0));

        // Update Past Performance
        ArrayList<String[]> updatedPastPerformance = new ArrayList<>();
        updatedPastPerformance.add(new String[] { "An updated objective", "With an updated achievement"} );
        updatedPastPerformance.add(new String[] { "The last objective and achievement" , " have been deleted"});

        // Update Future Performance
        ArrayList<String> updatedFuturePerformance = new ArrayList<String>();
        updatedFuturePerformance.add("An updated achievement");
        updatedFuturePerformance.add("The last achievement has been removed");

        // Test if just updating the information was successful
        assertTrue(Authorise.updatePerformanceReview(itManager, joinArrays(MainDoc, MainReviewRestPayLoad.get(0)), updatedPastPerformance, updatedFuturePerformance));
        assertArrayEquals(updatedPastPerformance.toArray(), dp.fetchPastPerformance(getMainReviewDocId()).toArray());
        assertEquals(updatedFuturePerformance, dp.fetchFuturePerformance(getMainReviewDocId()));
    }

    @Test
    void updatePerformanceReviewReviewer2()
    {
        dp = new DatabaseParser();
        setupReviewMainMandatoryPayloads("ite123");
        setupReviewMainOptionalPayloads();
        setupPastPerformances();
        setupFuturePerformances();

        // Create a review
        String[] MainDoc = new String[5];
        MainDoc[0] = MainReviewCreatePayload.get(0)[0];
        MainDoc[1] = MainReviewCreatePayload.get(0)[1];
        MainDoc[2] = MainReviewCreatePayload.get(0)[2];
        MainDoc[3] = dp.fetchDirectSupervisor("ite123");
        MainDoc[4] = MainReviewCreatePayload.get(0)[3];
        dp.createReview(MainDoc);
        dp.updateReview(getMainReviewDocId(), joinArrays(MainDoc, MainReviewRestPayLoad.get(0)), PastPerformances.get(0), FuturePerformances.get(0));

        // Update Past Performance
        ArrayList<String[]> updatedPastPerformance = new ArrayList<>();
        updatedPastPerformance.add(new String[] { "An updated objective", "With an updated achievement"} );
        updatedPastPerformance.add(new String[] { "The last objective and achievement" , " have been deleted"});

        // Update Future Performance
        ArrayList<String> updatedFuturePerformance = new ArrayList<String>();
        updatedFuturePerformance.add("An updated achievement");
        updatedFuturePerformance.add("The last achievement has been removed");

        // Test if just updating the information was successful
        assertTrue(Authorise.updatePerformanceReview(miles , joinArrays(MainDoc, MainReviewRestPayLoad.get(0)), updatedPastPerformance, updatedFuturePerformance));
        assertArrayEquals(updatedPastPerformance.toArray(), dp.fetchPastPerformance(getMainReviewDocId()).toArray());
        assertEquals(updatedFuturePerformance, dp.fetchFuturePerformance(getMainReviewDocId()));
    }

    @Test
    void updatePerformanceReviewSignAuthorised()
    {
        dp = new DatabaseParser();
        setupReviewMainMandatoryPayloads("ite123");
        setupReviewMainOptionalPayloads();
        setupPastPerformances();
        setupFuturePerformances();

        // Create a review
        String[] MainDoc = new String[5];
        MainDoc[0] = MainReviewCreatePayload.get(0)[0];
        MainDoc[1] = MainReviewCreatePayload.get(0)[1];
        MainDoc[2] = MainReviewCreatePayload.get(0)[2];
        MainDoc[3] = dp.fetchDirectSupervisor("ite123");
        MainDoc[4] = MainReviewCreatePayload.get(0)[3];
        dp.createReview(MainDoc);
        dp.updateReview(getMainReviewDocId(), joinArrays(MainDoc, MainReviewRestPayLoad.get(0)), PastPerformances.get(0), FuturePerformances.get(0));

        // Update Past Performance
        ArrayList<String[]> updatedPastPerformance = new ArrayList<>();
        updatedPastPerformance.add(new String[] { "An updated objective", "With an updated achievement"} );
        updatedPastPerformance.add(new String[] { "The last objective and achievement" , " have been deleted"});

        // Update Future Performance
        ArrayList<String> updatedFuturePerformance = new ArrayList<String>();
        updatedFuturePerformance.add("An updated achievement");
        updatedFuturePerformance.add("The last achievement has been removed");

        String[] testSignatures = MainReviewRestPayLoad.get(0);

        // Reviewee
        testSignatures[0] = getCurrentDate();
        assertTrue(Authorise.updatePerformanceReview(itEmployee, joinArrays(MainDoc, testSignatures), updatedPastPerformance, updatedFuturePerformance));
        assertEquals(getCurrentDate(), dp.fetchReview(getMainReviewDocId())[5]);

        // Reviewer 1
        testSignatures[1] = getCurrentDate();
        assertTrue(Authorise.updatePerformanceReview(itManager, joinArrays(MainDoc, testSignatures), updatedPastPerformance, updatedFuturePerformance));
        assertEquals(getCurrentDate(), dp.fetchReview(getMainReviewDocId())[6]);

        // Reviewer 2
        testSignatures[2] = getCurrentDate();
        assertTrue(Authorise.updatePerformanceReview(miles, joinArrays(MainDoc, testSignatures), updatedPastPerformance, updatedFuturePerformance));
        assertEquals(getCurrentDate(), dp.fetchReview(getMainReviewDocId())[7]);
    }

    @Test
    void updatePerformanceReviewSignInUnauthorised()
    {
        dp = new DatabaseParser();
        setupReviewMainMandatoryPayloads("ite123");
        setupReviewMainOptionalPayloads();
        setupPastPerformances();
        setupFuturePerformances();

        // Create a review
        String[] MainDoc = new String[5];
        MainDoc[0] = MainReviewCreatePayload.get(0)[0];
        MainDoc[1] = MainReviewCreatePayload.get(0)[1];
        MainDoc[2] = MainReviewCreatePayload.get(0)[2];
        MainDoc[3] = dp.fetchDirectSupervisor("ite123");
        MainDoc[4] = MainReviewCreatePayload.get(0)[3];
        dp.createReview(MainDoc);
        dp.updateReview(getMainReviewDocId(), joinArrays(MainDoc, MainReviewRestPayLoad.get(0)), PastPerformances.get(0), FuturePerformances.get(0));

        // Update Past Performance
        ArrayList<String[]> updatedPastPerformance = new ArrayList<>();
        updatedPastPerformance.add(new String[] { "An updated objective", "With an updated achievement"} );
        updatedPastPerformance.add(new String[] { "The last objective and achievement" , " have been deleted"});

        // Update Future Performance
        ArrayList<String> updatedFuturePerformance = new ArrayList<String>();
        updatedFuturePerformance.add("An updated achievement");
        updatedFuturePerformance.add("The last achievement has been removed");

        String[] testSignatures = MainReviewRestPayLoad.get(0);

        // 2nd Reviewer
        testSignatures[0] = getCurrentDate();
        testSignatures[1] = getCurrentDate();
        testSignatures[2] = getCurrentDate();
        System.out.println(testSignatures[2]);

        assertTrue(Authorise.updatePerformanceReview(miles, joinArrays(MainDoc, testSignatures), updatedPastPerformance, updatedFuturePerformance));
        assertNull(dp.fetchReview(getMainReviewDocId())[5]);
        assertNull(dp.fetchReview(getMainReviewDocId())[6]);
        assertEquals(getCurrentDate(), dp.fetchReview(getMainReviewDocId())[7]);

        // Reset
        testSignatures[0] = null;
        testSignatures[1] = null;
        testSignatures[2] = null;
        dp.updateReview(getMainReviewDocId(), joinArrays(MainDoc, testSignatures), PastPerformances.get(0), FuturePerformances.get(0));
        testSignatures[0] = getCurrentDate();
        testSignatures[1] = getCurrentDate();
        testSignatures[2] = getCurrentDate();

        // Reviewer 1
        assertTrue(Authorise.updatePerformanceReview(itManager, joinArrays(MainDoc, testSignatures), updatedPastPerformance, updatedFuturePerformance));
        assertNull(dp.fetchReview(getMainReviewDocId())[5]);
        assertEquals(getCurrentDate(), dp.fetchReview(getMainReviewDocId())[6]);
        assertNull(dp.fetchReview(getMainReviewDocId())[7]);

        testSignatures[0] = null;
        testSignatures[1] = null;
        testSignatures[2] = null;
        dp.updateReview(getMainReviewDocId(), joinArrays(MainDoc, testSignatures), PastPerformances.get(0), FuturePerformances.get(0));
        testSignatures[0] = getCurrentDate();
        testSignatures[1] = getCurrentDate();
        testSignatures[2] = getCurrentDate();

        // Reviewee
        assertTrue(Authorise.updatePerformanceReview(itEmployee, joinArrays(MainDoc, testSignatures), updatedPastPerformance, updatedFuturePerformance));
        assertEquals(getCurrentDate(), dp.fetchReview(getMainReviewDocId())[5]);
        assertNull(dp.fetchReview(getMainReviewDocId())[6]);
        assertNull(dp.fetchReview(getMainReviewDocId())[7]);
    }

    @Test
    void updatePerformanceReviewReadOnly()
    {
        dp = new DatabaseParser();
        setupReviewMainMandatoryPayloads("ite123");
        setupReviewMainOptionalPayloads();
        setupPastPerformances();
        setupFuturePerformances();

        // Create a review
        String[] MainDoc = new String[5];
        MainDoc[0] = MainReviewCreatePayload.get(0)[0];
        MainDoc[1] = MainReviewCreatePayload.get(0)[1];
        MainDoc[2] = MainReviewCreatePayload.get(0)[2];
        MainDoc[3] = dp.fetchDirectSupervisor("ite123");
        MainDoc[4] = MainReviewCreatePayload.get(0)[3];
        dp.createReview(MainDoc);

        // Set all signatures to be true
        String[] testSignatures = MainReviewRestPayLoad.get(0);
        testSignatures[0] = "true";
        testSignatures[1] = "true";
        testSignatures[2] = "true";
        dp.updateReview(getMainReviewDocId(), joinArrays(MainDoc, testSignatures), PastPerformances.get(0), FuturePerformances.get(0));

        // Update Past Performance
        ArrayList<String[]> updatedPastPerformance = new ArrayList<>();
        updatedPastPerformance.add(new String[] { "An updated objective", "With an updated achievement"} );
        updatedPastPerformance.add(new String[] { "The last objective and achievement" , " have been deleted"});

        // Update Future Performance
        ArrayList<String> updatedFuturePerformance = new ArrayList<String>();
        updatedFuturePerformance.add("An updated achievement");
        updatedFuturePerformance.add("The last achievement has been removed");

        assertFalse(Authorise.updatePerformanceReview(itManager, joinArrays(MainDoc, MainReviewRestPayLoad.get(0)), updatedPastPerformance, updatedFuturePerformance));
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
        ArrayList<String[]> case1 = new ArrayList<>();
        case1.add(new String[] { "Some objective that is irrelevant to testing", "Some achievement"});
        case1.add(new String[] { "Some other objective that is irrelevant to testing", "Some other achievement"});
        case1.add(new String[] { "Another objective that is irrelevant to testing", "Another achievement"});

        // 0: Expected, full payload
        PastPerformances.add(case1);

        ArrayList<String[]> case2 = new ArrayList<>();

        case2.add(new String[] { null, "Some achievement"});
        case2.add(new String[] { null, "Some other achievement"});
        case2.add(new String[] { null, "Another achievement"});
        // 1: invalid document ID, full payload
        PastPerformances.add(case2);

        // 2: Number set to null. (Doing this because number is a FK in the DB

        ArrayList<String[]> case3 = new ArrayList<>();
        case3.add(new String[] {null, null});
        case3.add(new String[] {null, null});
        case3.add(new String[] {null, null});
        PastPerformances.add(case3);


    }

    void setupFuturePerformances()
    {
        ArrayList<String> case1 = new ArrayList<>();
        case1.add("A future objective");
        case1.add("Another future objective");
        case1.add("Other future objective");

        // 0: Expected
        FuturePerformances.add(case1);

        ArrayList<String> case2 = new ArrayList<>();
        case2.add("A future objective");
        case2.add("Another future objective");
        case2.add("Other future objective");

        // 1: Only contents containing null
        FuturePerformances.add(case2);

    }

    String getCurrentDate()
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
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