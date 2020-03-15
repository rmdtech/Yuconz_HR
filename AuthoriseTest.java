import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AuthoriseTest {
    User hrEmployee;
    User hrManager;
    User hrDirector;
    User itEmployee;
    User itManager;
    DatabaseParser dp;

    ArrayList<String[]> mainDocMandatoryPayload = new ArrayList<>();
    ArrayList<String[]> mainDocOptionalPayload = new ArrayList<>();
    ArrayList<ArrayList<String[]>> pastperformanceDataCollection = new ArrayList<>();
    ArrayList<ArrayList<String>> futureperformanceDataCollection = new ArrayList<>();

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

        hrDirector = Authenticate.login("dir123", "password");
        hrManager = Authenticate.login("hrm123", "password");
        hrEmployee = Authenticate.login("hre123", "password");
        itManager = Authenticate.login("itm123", "password");
        itEmployee = Authenticate.login("ite123", "password");

        dp = new DatabaseParser();
        initialiseMainDocMandatoryPayload("ite123", "dir123");
        initialiseMainDocOptionalPayload();
        initialisePastPerformanceCollection();
        initialiseFuturePerformanceCollection();

        System.out.println("\n---- END OF SETUP OUTPUT ----\n");
    }

    @Test
    void createPerformanceReviewBaseCase()
    {
        initialiseMainDocMandatoryPayload("ite123", "dir123");
        assertTrue(Authorise.createPerformanceReview(hrEmployee, mainDocMandatoryPayload.get(0)));
    }

    @Test
    void createPerformanceReviewNonHR()
    {
        initialiseMainDocMandatoryPayload("hre123", "dir123");
        assertFalse(Authorise.createPerformanceReview(itEmployee, mainDocMandatoryPayload.get((0))));
        assertFalse(Authorise.createPerformanceReview(itManager, mainDocMandatoryPayload.get(0)));
    }

    @Test
    void createPerformanceReviewReviewerIsReviewee()
    {
        initialiseMainDocMandatoryPayload("ite123", "itm123");
        assertFalse(Authorise.createPerformanceReview(hrManager, mainDocMandatoryPayload.get(0)));
    }

    @Test
    void createPerformanceReviewHRManagerCreateReview()
    {
        initialiseMainDocMandatoryPayload("itm123", "dir123");
        assertTrue(Authorise.createPerformanceReview(hrManager, mainDocMandatoryPayload.get(0)));
    }

    @Test
    void createPerformanceReviewHRDirectorCreateReview()
    {
        initialiseMainDocMandatoryPayload("hre123", "dir123");
        assertTrue(Authorise.createPerformanceReview(hrDirector, mainDocMandatoryPayload.get(0)));
    }

    @Test
    void createInvalidPerformanceReview()
    {
        initialiseMainDocMandatoryPayload("hre123", "dir123");
        assertFalse(Authorise.createPerformanceReview(hrEmployee, mainDocMandatoryPayload.get(1)));
        assertFalse(Authorise.createPerformanceReview(hrEmployee, mainDocMandatoryPayload.get(2)));
        assertFalse(Authorise.createPerformanceReview(hrEmployee, mainDocMandatoryPayload.get(3)));
        assertFalse(Authorise.createPerformanceReview(hrEmployee, mainDocMandatoryPayload.get(4)));
        mainDocMandatoryPayload.clear();
        initialiseMainDocMandatoryPayload("hre123", null);
        assertFalse(Authorise.createPerformanceReview(hrEmployee, mainDocMandatoryPayload.get(0)));
    }

    @Test
    void readPerformanceReview()
    {
        // Create a review
        String[] mainDoc = insertSupervisor(mainDocMandatoryPayload.get(0));
        dp.createReview(mainDoc);
        dp.updateReview(getMainReviewDocId(), joinArrays(mainDoc, mainDocOptionalPayload.get(0)), pastperformanceDataCollection.get(0), futureperformanceDataCollection.get(0));

        // Set expected data
        String[] expectedMainDoc = joinArrays(mainDoc, mainDocOptionalPayload.get(0));
        ArrayList<String[]> expectedPastPerformance = pastperformanceDataCollection.get(0);
        ArrayList<String> expectedFuturePerformance = futureperformanceDataCollection.get(0);

        // Set actual data
        Authorise.readPerformanceReview(itEmployee, "ite123", "2020-12-31");
        String[] actualMainDoc = Authorise.readReviewMain(getMainReviewDocId());
        ArrayList<String[]> actualPastPerformance = Authorise.readPastPerformance(getMainReviewDocId());
        ArrayList<String> actualFuturePerformance = Authorise.readFuturePerformance(getMainReviewDocId());

        assertArrayEquals(expectedMainDoc, actualMainDoc);
        assertEquals(expectedFuturePerformance, actualFuturePerformance);
        assertArrayEquals(expectedPastPerformance.toArray(), actualPastPerformance.toArray());
    }

    @Test
    void readPerformanceReviewNonHRonSelf()
    {
        // Create a Review
        String[] mainDoc = insertSupervisor(mainDocMandatoryPayload.get(0));
        dp.createReview(mainDoc);
        dp.updateReview(getMainReviewDocId(), joinArrays(mainDoc, mainDocOptionalPayload.get(0)), pastperformanceDataCollection.get(0), futureperformanceDataCollection.get(0));
        // Confirm that we can read from it as expected
        assert(Authorise.readPerformanceReview(itEmployee, "ite123", "2020-12-31"));

        // Set expected data
        String[] expectedMainDoc = joinArrays(mainDoc, mainDocOptionalPayload.get(0));
        ArrayList<String[]> expectedPastPerformance = pastperformanceDataCollection.get(0);
        ArrayList<String> expectedFuturePerformance = futureperformanceDataCollection.get(0);

        // Set received data
        String[] actualMainDoc = dp.fetchReview(getMainReviewDocId());
        ArrayList<String[]> actualPastPerformance = dp.fetchPastPerformance(getMainReviewDocId());
        ArrayList<String> actualFuturePerformance = dp.fetchFuturePerformance(getMainReviewDocId());

        // Confirm that all data received is what we expected
        assertArrayEquals(expectedMainDoc, actualMainDoc);
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
        mainDocMandatoryPayload.clear();
        initialiseMainDocMandatoryPayload("hre123", "dir123");

        // Create a Review
        String[] mainDoc = insertSupervisor(mainDocMandatoryPayload.get(0));
        dp.createReview(mainDoc);
        dp.updateReview(getMainReviewDocId(), joinArrays(mainDoc, mainDocOptionalPayload.get(0)), pastperformanceDataCollection.get(0), futureperformanceDataCollection.get(0));

        assertFalse(Authorise.readPerformanceReview(itManager, "hre123", "2020-12-31"));
    }

    @Test
    void updatePerformanceReviewReviewee()
    {
        // Create a review
        String[] mainDoc = insertSupervisor(mainDocMandatoryPayload.get(0));
        dp.createReview(mainDoc);
        dp.updateReview(getMainReviewDocId(), joinArrays(mainDoc, mainDocOptionalPayload.get(0)), pastperformanceDataCollection.get(0), futureperformanceDataCollection.get(0));

        // Update Past Performance
        ArrayList<String[]> updatedPastPerformance = pastperformanceDataCollection.get(3);

        // Update Future Performance
        ArrayList<String> updatedFuturePerformance = futureperformanceDataCollection.get(2);

        // Test if just updating the information was successful
        assertTrue(Authorise.updatePerformanceReview(itEmployee, joinArrays(mainDoc, mainDocOptionalPayload.get(0)), updatedPastPerformance, updatedFuturePerformance));
        assertArrayEquals(updatedPastPerformance.toArray(), dp.fetchPastPerformance(getMainReviewDocId()).toArray());
        assertEquals(updatedFuturePerformance, dp.fetchFuturePerformance(getMainReviewDocId()));
    }

    @Test
    void updatePerformanceReviewReviewer1()
    {
        // Create a review
        String[] mainDoc = insertSupervisor(mainDocMandatoryPayload.get(0));
        dp.createReview(mainDoc);
        dp.updateReview(getMainReviewDocId(), joinArrays(mainDoc, mainDocOptionalPayload.get(0)), pastperformanceDataCollection.get(0), futureperformanceDataCollection.get(0));

        // Update Past Performance
        ArrayList<String[]> updatedPastPerformance = pastperformanceDataCollection.get(3);

        // Update Future Performance
        ArrayList<String> updatedFuturePerformance = futureperformanceDataCollection.get(2);

        // Test if just updating the information was successful
        assertTrue(Authorise.updatePerformanceReview(itManager, joinArrays(mainDoc, mainDocOptionalPayload.get(0)), updatedPastPerformance, updatedFuturePerformance));
        assertArrayEquals(updatedPastPerformance.toArray(), dp.fetchPastPerformance(getMainReviewDocId()).toArray());
        assertEquals(updatedFuturePerformance, dp.fetchFuturePerformance(getMainReviewDocId()));
    }

    @Test
    void updatePerformanceReviewReviewer2()
    {
        // Create a review
        String[] mainDoc = insertSupervisor(mainDocMandatoryPayload.get(0));
        dp.createReview(mainDoc);
        dp.updateReview(getMainReviewDocId(), joinArrays(mainDoc, mainDocOptionalPayload.get(0)), pastperformanceDataCollection.get(0), futureperformanceDataCollection.get(0));

        // Update Past Performance
        ArrayList<String[]> updatedPastPerformance = pastperformanceDataCollection.get(3);

        // Update Future Performance
        ArrayList<String> updatedFuturePerformance = futureperformanceDataCollection.get(2);

        // Test if just updating the information was successful
        assertTrue(Authorise.updatePerformanceReview(hrDirector, joinArrays(mainDoc, mainDocOptionalPayload.get(0)), updatedPastPerformance, updatedFuturePerformance));
        assertArrayEquals(updatedPastPerformance.toArray(), dp.fetchPastPerformance(getMainReviewDocId()).toArray());
        assertEquals(updatedFuturePerformance, dp.fetchFuturePerformance(getMainReviewDocId()));
    }

    @Test
    void updatePerformanceReviewSignAuthorised()
    {
        // Create a review
        String[] mainDoc = insertSupervisor(mainDocMandatoryPayload.get(0));
        dp.createReview(mainDoc);
        dp.updateReview(getMainReviewDocId(), joinArrays(mainDoc, mainDocOptionalPayload.get(0)), pastperformanceDataCollection.get(0), futureperformanceDataCollection.get(0));

        // Update Past Performance
        ArrayList<String[]> updatedPastPerformance = pastperformanceDataCollection.get(3);

        // Update Future Performance
        ArrayList<String> updatedFuturePerformance = futureperformanceDataCollection.get(2);

        // Get Empty signatures
        String[] testSignatures = mainDocOptionalPayload.get(0);

        // Reviewee
        testSignatures[0] = getCurrentDate();
        assertTrue(Authorise.updatePerformanceReview(itEmployee, joinArrays(mainDoc, testSignatures), updatedPastPerformance, updatedFuturePerformance));
        assertEquals(getCurrentDate(), dp.fetchReview(getMainReviewDocId())[5]);

        // Reviewer 1
        testSignatures[0] = null;
        testSignatures[1] = getCurrentDate();
        assertTrue(Authorise.updatePerformanceReview(itManager, joinArrays(mainDoc, testSignatures), updatedPastPerformance, updatedFuturePerformance));
        assertEquals(getCurrentDate(), dp.fetchReview(getMainReviewDocId())[6]);

        // Reviewer 2
        testSignatures[0] = null;
        testSignatures[1] = null;
        testSignatures[2] = getCurrentDate();
        assertTrue(Authorise.updatePerformanceReview(hrDirector, joinArrays(mainDoc, testSignatures), updatedPastPerformance, updatedFuturePerformance));
        assertEquals(getCurrentDate(), dp.fetchReview(getMainReviewDocId())[7]);
    }

    @Test
    void updatePerformanceReviewSignInUnauthorised()
    {
        // Create a review
        String[] mainDoc = insertSupervisor(mainDocMandatoryPayload.get(0));
        dp.createReview(mainDoc);
        dp.updateReview(getMainReviewDocId(), joinArrays(mainDoc, mainDocOptionalPayload.get(0)), pastperformanceDataCollection.get(0), futureperformanceDataCollection.get(0));


        ArrayList<String[]> updatedPastPerformance = pastperformanceDataCollection.get(3);
        ArrayList<String> updatedFuturePerformance = futureperformanceDataCollection.get(2);

        String[] testSignatures = mainDocOptionalPayload.get(0); // empty signatures

        // Set all signatures to contain a value
        testSignatures[0] = getCurrentDate();
        testSignatures[1] = getCurrentDate();
        testSignatures[2] = getCurrentDate();
        // Reviewer 2 trying to sign for everybody
        assertTrue(Authorise.updatePerformanceReview(hrDirector, joinArrays(mainDoc, testSignatures), updatedPastPerformance, updatedFuturePerformance));
        assertNull(dp.fetchReview(getMainReviewDocId())[5]);
        assertNull(dp.fetchReview(getMainReviewDocId())[6]);
        assertEquals(getCurrentDate(), dp.fetchReview(getMainReviewDocId())[7]);

        // Reset
        testSignatures[0] = null;
        testSignatures[1] = null;
        testSignatures[2] = null;
        dp.updateReview(getMainReviewDocId(), joinArrays(mainDoc, testSignatures), pastperformanceDataCollection.get(0), futureperformanceDataCollection.get(0));
        testSignatures[0] = getCurrentDate();
        testSignatures[1] = getCurrentDate();
        testSignatures[2] = getCurrentDate();

        // Reviewer 1 trying to sign for everybody
        assertTrue(Authorise.updatePerformanceReview(itManager, joinArrays(mainDoc, testSignatures), updatedPastPerformance, updatedFuturePerformance));
        assertNull(dp.fetchReview(getMainReviewDocId())[5]);
        assertEquals(getCurrentDate(), dp.fetchReview(getMainReviewDocId())[6]);
        assertNull(dp.fetchReview(getMainReviewDocId())[7]);

        // Reset
        testSignatures[0] = null;
        testSignatures[1] = null;
        testSignatures[2] = null;
        dp.updateReview(getMainReviewDocId(), joinArrays(mainDoc, testSignatures), pastperformanceDataCollection.get(0), futureperformanceDataCollection.get(0));
        testSignatures[0] = "true";
        testSignatures[1] = "true";
        testSignatures[2] = "true";

        // Reviewee trying to sign for everybody
        assertTrue(Authorise.updatePerformanceReview(itEmployee, joinArrays(mainDoc, testSignatures), updatedPastPerformance, updatedFuturePerformance));
        assertEquals(getCurrentDate(), dp.fetchReview(getMainReviewDocId())[5]);
        assertNull(dp.fetchReview(getMainReviewDocId())[6]);
        assertNull(dp.fetchReview(getMainReviewDocId())[7]);
    }

    @Test
    void updatePerformanceReviewReadOnly()
    {
        // Create a review
        String[] mainDoc = insertSupervisor(mainDocMandatoryPayload.get(0));
        dp.createReview(mainDoc);

        // Set all signatures to be true
        String[] testSignatures = mainDocOptionalPayload.get(0);
        testSignatures[0] = "true";
        testSignatures[1] = "true";
        testSignatures[2] = "true";
        dp.updateReview(getMainReviewDocId(), joinArrays(mainDoc, testSignatures), pastperformanceDataCollection.get(0), futureperformanceDataCollection.get(0));

        ArrayList<String[]> updatedPastPerformance = pastperformanceDataCollection.get(3);
        ArrayList<String> updatedFuturePerformance = futureperformanceDataCollection.get(2);

        assertFalse(Authorise.updatePerformanceReview(itManager, joinArrays(mainDoc, mainDocOptionalPayload.get(0)), updatedPastPerformance, updatedFuturePerformance));
    }







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
        return mainDocMandatoryPayload.get(0)[2];
    }

    /**
     * MainDocMandatory ArrayList<String[]>
     * 0: Expected
     * 1: Missing Reviewee
     * 2: Missing Due Date
     * 3: Missing Document ID
     * 4: All null
     * @param empId the employee ID of the reviewee
     * @param reviewer2 employee ID of the second reviewer
     */
    void initialiseMainDocMandatoryPayload(String empId, String reviewer2)
    {
        // 0: Expected
        mainDocMandatoryPayload.add(new String[] { empId, "2020-12-31", "3534c934edef4388b1c404d2d8064a21", reviewer2 });
        // 1: Missing Reviewee
        mainDocMandatoryPayload.add(new String[] { null, "2020-12-31", "3534c934edef4388b1c404d2d8064a21", reviewer2 });
        // 2: Missing Due Date
        mainDocMandatoryPayload.add(new String[] { empId, null, "3534c934edef4388b1c404d2d8064a21", reviewer2 });
        // 3: Missing docId
        mainDocMandatoryPayload.add(new String[] { "hre123", "2020-12-31", null, reviewer2});
        // 4: All null
        mainDocMandatoryPayload.add(new String[] { null, null, null,  null });
    }

    /**
     * MainDocOptional: ArrayList<String[]>
     * 0: Populated, not signed
     * 1: Populated, all signed
     * 2: Populated, Reviewee signed
     * 3: Populated, Reviewer 1 signed
     * 4: Populated, Reviewer 2 signed
     * 5: No Meeting Date, not signed
     * 6: No comments
     * 7: No Meeting Date or comments
     * 8: All null
     */
    void initialiseMainDocOptionalPayload()
    {
        // 0: Filled, not signed
        mainDocOptionalPayload.add(new String[] { null, null, null, "2020-03-13", "Some text that makes up the summary and is irrelevant for testing", "reviewercomments that are irrelevent for testing", "recommendation that is irrelevant for testing"});
        // 1: Filled, signed
        mainDocOptionalPayload.add(new String[] { getCurrentDate(), getCurrentDate(), getCurrentDate(), "2020-03-13", "Some text that makes up the summary and is irrelevant for testing", "reviewercomments that are irrelevent for testing", "recommendation that is irrelevant for testing"});
        // 2: Filled, Reviewee Signed
        mainDocOptionalPayload.add(new String[] { getCurrentDate(), null, null, "2020-03-13", "Some text that makes up the summary and is irrelevant for testing", "reviewercomments that are irrelevent for testing", "recommendation that is irrelevant for testing"});
        // 3: Filled, Reviewer 1 Signed
        mainDocOptionalPayload.add(new String[] { null, getCurrentDate(), null, "2020-03-13", "Some text that makes up the summary and is irrelevant for testing", "reviewercomments that are irrelevent for testing", "recommendation that is irrelevant for testing"});
        // 4: Filled, Reviewer 2 Signed
        mainDocOptionalPayload.add(new String[] { null, null, getCurrentDate(), "2020-03-13", "Some text that makes up the summary and is irrelevant for testing", "reviewercomments that are irrelevent for testing", "recommendation that is irrelevant for testing"});
        // 5: No date, not signed
        mainDocOptionalPayload.add(new String[] { null, null, null, null, "Some text that makes up the summary and is irrelevant for testing", "reviewercomments that are irrelevent for testing", "recommendation that is irrelevant for testing"});
        // 6: No comments
        mainDocOptionalPayload.add(new String[] { "false", "false", "false", "2020-03-13", null, null, null});
        // 7: No date, no comments
        mainDocOptionalPayload.add(new String[] { "false", "false", "false", null, null, null, null});
        // 8: All null
        mainDocOptionalPayload.add(new String[] { null, null, null, null, null, null, null});
    }

    /**
     * PastPerformance ArrayList<String[]>
     * 0: Expected
     * 1: No Document ID
     * 2: Null
     * 3: Updated
     */
    void initialisePastPerformanceCollection()
    {
        // 0: Expected, full payload
        ArrayList<String[]> case1 = new ArrayList<>();
        case1.add(new String[] { "Some objective that is irrelevant to testing", "Some achievement"});
        case1.add(new String[] { "Some other objective that is irrelevant to testing", "Some other achievement"});
        case1.add(new String[] { "Another objective that is irrelevant to testing", "Another achievement"});
        pastperformanceDataCollection.add(case1);

        // 1: No DocumentID
        ArrayList<String[]> case2 = new ArrayList<>();
        case2.add(new String[] { null, "Some achievement"});
        case2.add(new String[] { null, "Some other achievement"});
        case2.add(new String[] { null, "Another achievement"});
        pastperformanceDataCollection.add(case2);

        // 2: All values null
        ArrayList<String[]> case3 = new ArrayList<>();
        case3.add(new String[] {null, null});
        case3.add(new String[] {null, null});
        case3.add(new String[] {null, null});
        pastperformanceDataCollection.add(case3);

        ArrayList<String[]> case4 = new ArrayList<>();
        case4.add(new String[] { "An updated objective", "With an updated achievement"} );
        case4.add(new String[] { "The last objective and achievement" , " have been deleted"});
        pastperformanceDataCollection.add(case4);
    }

    /**
     * Future Performance ArrayList<String>
     * 0: Expected use case
     * 1: Null only
     * 2: Updated case
     */
    void initialiseFuturePerformanceCollection()
    {
        // 0: Expected
        ArrayList<String> case1 = new ArrayList<>();
        case1.add("A future objective");
        case1.add("Another future objective");
        case1.add("Other future objective");
        futureperformanceDataCollection.add(case1);

        // 1: Only contents containing null
        ArrayList<String> case2 = new ArrayList<>();
        case2.add(null);
        case2.add(null);
        case2.add(null);
        futureperformanceDataCollection.add(case2);

        // 2: Updated
        ArrayList<String> case3 = new ArrayList<>();
        case3.add("An updated achievement");
        case3.add("The last achievement has been removed");
        futureperformanceDataCollection.add(case3);
    }

    /*
        THIS MARKS THE END OF THE ACTUAL TESTS
        ANY METHODS BELOW THIS POINT
        ARE SIMPLY SUPPORT METHODS
     */

    String getCurrentDate()
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    String[] insertSupervisor(String[] mainDocument)
    {
        String[] mainDocUpdated = new String[5];
        mainDocUpdated[0] = mainDocument[0];
        mainDocUpdated[1] = mainDocument[1];
        mainDocUpdated[2] = mainDocument[2];
        mainDocUpdated[3] = dp.fetchDirectSupervisor(mainDocument[0]);
        mainDocUpdated[4] = mainDocument[3];
        return mainDocUpdated;
    }

    void writeReview(String revieweeId, String reviewer2)
    {
        dp = new DatabaseParser();
        initialiseMainDocMandatoryPayload(revieweeId, reviewer2);
        initialiseMainDocOptionalPayload();
        initialiseFuturePerformanceCollection();
        initialisePastPerformanceCollection();

        String[] mainDocUpdated = insertSupervisor(mainDocMandatoryPayload.get(0));

        dp.createReview(mainDocUpdated);
        dp.updateReview(getMainReviewDocId(), joinArrays(mainDocUpdated, mainDocOptionalPayload.get(0)), pastperformanceDataCollection.get(0), futureperformanceDataCollection.get(0));
    }

    private static boolean checkIsFirstBoot()
    {
        File dbFile = new File("./databases/yuconz.db");
        return dbFile.exists();
    }

    private static void dbSetup()
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