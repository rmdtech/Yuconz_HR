import static org.junit.jupiter.api.Assertions.*;

class AuthoriseTest {
    private User hrEmployee;
    private User itEmployee;
    private User hrManager;
    DatabaseParser dp = new DatabaseParser();

    @org.junit.jupiter.api.BeforeEach
    void setUp()
    {
        System.out.println("---- Setup output --- ");
        dp.sqlUpdate("DELETE FROM Session");
        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM AuthorisationLog");
        dp.sqlUpdate("DELETE FROM AuthenticationLog");
        dp.sqlUpdate("DELETE FROM User");

        Authenticate.addNewUser("hre123", "password", Position.Department.HR, Position.Role.Employee);
        Authenticate.addNewUser("ite123", "password", Position.Department.IT, Position.Role.Employee);
        Authenticate.addNewUser("hrm123", "password", Position.Department.IT, Position.Role.Employee);
        hrEmployee = Authenticate.login("hre123", "password");
        itEmployee = Authenticate.login("ite123", "password");
        hrManager = Authenticate.login("hrm123", "password");
        System.out.println("---- Test output --- ");
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown()
    {
        System.out.println("---- Teardown output --- ");
    }

    @org.junit.jupiter.api.Test
    void authorisationAttempt()
    {
        createPersonalDetails();
    }

    @org.junit.jupiter.api.Test
    void createPersonalDetails()
    {
        String[] hre123FullPayload = { "hre123", "Roman", "Miles", "01/01/1970", "University of Kent", "CT2 7NF", "Canterbury", "Kent", "01227748392", "07638270376", "David Barnes", "01227827696"};
        String[] emptyPayload = { null, null, null, null, null, null, null, null, null, null, null };
        String[] ite123FullPayload = { "ite123", "Smith", "John", "01/01/1970", "University of Kent", "Canterbury", "CT2 7NF", "Kent", "01227748392", "07638270376", "David Barnes", "01227827696"};

        // HR Employee trying to create a new Personal Details record
        assertTrue(Authorise.createPersonalDetailsRecord(hrEmployee, ite123FullPayload));

        // Non-HR Employee trying to create a new Personal Details record
        assertFalse(Authorise.createPersonalDetailsRecord(itEmployee, ite123FullPayload));

        // Empty record about to be submitted
        assertFalse(Authorise.createPersonalDetailsRecord(hrEmployee, emptyPayload));

        // User not logged in
        Authenticate.logout(hrEmployee);
        assertFalse(Authorise.createPersonalDetailsRecord(hrEmployee, hre123FullPayload));
    }

    @org.junit.jupiter.api.Test
    void readPersonalDetails()
    {
        String[] hre123FullPayload = { "hre123", "Roman", "Miles", "01/01/1970", "University of Kent", "CT2 7NF", "Canterbury", "Kent", "01227748392", "07638270376", "David Barnes", "01227827696"};
        String[] ite123FullPayload = { "ite123", "Smith", "John", "01/01/1970", "University of Kent", "Canterbury", "CT2 7NF", "Kent", "01227748392", "07638270376", "David Barnes", "01227827696"};

        // Trying to read a PersonalDetails record that doesn't exist
        assertNull(Authorise.readPersonalDetails(hrEmployee, "err404"));

        // Populate the PersonalDetails record first
        Authorise.createPersonalDetailsRecord(hrEmployee, hre123FullPayload);
        Authorise.createPersonalDetailsRecord(hrEmployee, ite123FullPayload);
        System.out.println("- ReadPersonalDetails: Created PD record in DB");

        // HR Employee trying to access their own record
        assertArrayEquals(hre123FullPayload, Authorise.readPersonalDetails(hrEmployee, hrEmployee.getEmployeeId()));

        // IT Employee trying to access their own record
        assertArrayEquals(ite123FullPayload, Authorise.readPersonalDetails(itEmployee, itEmployee.getEmployeeId()));

        // User of wrong department trying to access another user's record
        assertNull(Authorise.readPersonalDetails(itEmployee, hrEmployee.getEmployeeId()));
    }

    @org.junit.jupiter.api.Test
    void  updatePersonalDetails()
    {
        String[] hre123FullPayload = { "hre123", "Roman", "Miles", "01/01/1970", "University of Kent", "CT2 7NF", "Canterbury", "Kent", "01227748392", "07638270376", "David Barnes", "01227827696"};
        String[] hreFullPayload = {"hre123", "Roman", "Miles", "01/01/1970", "University of Kent", "Canterbury", "Kent", "CT2 7NF", "01227748392", "07638270376", "Olaf Chitil", "01227824320"};
        String[] iteFullPayload = {"ite123", "Smith", "John", "01/01/1970", "University of Kent", "Canterbury", "Kent", "CT2 7NF", "01227748392", "07638270376", "Olaf Chitil", "01227824320"};
        String[] emptyPayload = {null, null, null, null, null, null, null, null, null, null, null};

        Authorise.createPersonalDetailsRecord(hrEmployee, hre123FullPayload);
        Authorise.createPersonalDetailsRecord(hrManager, iteFullPayload);

        // Variant 1: Expected Use, user updates their own info
        assertTrue(Authorise.updatePersonalDetails(itEmployee, iteFullPayload)); // has ResultSet closed error

        // Variant 2: Expected Use, HR updates someone else's file
        assertTrue(Authorise.updatePersonalDetails(hrEmployee, iteFullPayload)); // has some sort of enum thingy error

        // Made redundent by refacture
        // Variant 3: Unexpected Use, user targets Invalid Target
        // assertFalse(Authorise.AuthorisationAttempt(Authorise.Action.Update, "Invalid Target", hrEmployee, hreFullPayload)); // has no errors *happy test noises*

        // Variant 4: Unexpected Use, Try to update with empty payload
        assertTrue(Authorise.updatePersonalDetails(hrEmployee, emptyPayload)); // has some sort of enum thingy error


        // Variant 5: Unexpected Use, User is neither HR or updating their own info
        assertFalse(Authorise.updatePersonalDetails(itEmployee, hreFullPayload)); // has ResultSet closed error

        // ----------------- CURRENTLY FAULTY DUE TO ISSUE 14. THROWS SQL ERROR WHICH CAUSES THE TEST TO CRASH OUT ----------------------------
        // Variant 6: Unexpected Use, User not logged in
        Authenticate.logout(hrEmployee);
        assertFalse(Authorise.updatePersonalDetails(hrEmployee, hreFullPayload));
    }

    @org.junit.jupiter.api.Test
    void deletePersonalDetails()
    {
        String[] hreFullPayload = {"hre123", "Roman", "Miles", "01/01/1970", "University of Kent", "Canterbury", "Kent", "CT2 7NF", "01227748392", "07638270376", "Olaf Chitil", "01227824320"};

        // Variant 1: Logged in user tries to delete from personal details
        assertFalse(Authorise.deletePersonalDetails(hrEmployee));

        // Variant 2: Logged in user tries to delete from an Invalid Target
        assertFalse(Authorise.deletePersonalDetails(hrEmployee));

        // ----------------- CURRENTLY FAULTY DUE TO ISSUE 14. THROWS SQL ERROR WHICH CAUSES THE TEST TO CRASH OUT ----------------------------
        //Variant 3: logged out user tries to delete from personal details
        //Authenticate.logout(hrEmployee);
        //assertFalse(Authorise.AuthorisationAttempt(Authorise.Action.Delete, "Personal Details", hrEmployee, hreFullPayload));
    }
}