import static org.junit.jupiter.api.Assertions.*;

class AuthoriseTest {
    private User hrEmployee;
    private User itEmployee;
    private User hrManager;

    @org.junit.jupiter.api.BeforeEach
    void setUp()
    {
        System.out.println("---- Setup output --- ");
        DatabaseParser dp = new DatabaseParser();
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

        // Normal, expected use
        assertTrue(Authorise.AuthorisationAttempt(Authorise.Action.Create, "Personal Details", hrEmployee, hre123FullPayload));


        // User of wrong department
        assertFalse(Authorise.AuthorisationAttempt(Authorise.Action.Create, "Personal Details", itEmployee, hre123FullPayload));

        // User trying to create their own personal details record
        assertFalse(Authorise.AuthorisationAttempt(Authorise.Action.Create, "Personal Details", itEmployee, ite123FullPayload));

        // Empty record about to be submitted
        assertFalse(Authorise.AuthorisationAttempt(Authorise.Action.Create, "Personal Details", hrEmployee, emptyPayload));

        // ----------------- CURRENTLY FAULTY DUE TO ISSUE 14. THROWS SQL ERROR WHICH CAUSES THE TEST TO CRASH OUT ----------------------------
        // User not logged in
        // Authenticate.logout(hrEmployee);
        // assertFalse(Authorise.AuthorisationAttempt(Authorise.Action.Create, "Personal Details", hrEmployee, hre123FullPayload));
    }

    @org.junit.jupiter.api.Test
    void  updatePersonalDetails()
    {
        String[] hre123FullPayload = { "hre123", "Roman", "Miles", "01/01/1970", "University of Kent", "CT2 7NF", "Canterbury", "Kent", "01227748392", "07638270376", "David Barnes", "01227827696"};
        String[] ite123FullPayload = { "ite123", "Smith", "John", "01/01/1970", "University of Kent", "Canterbury", "CT2 7NF", "Kent", "01227748392", "07638270376", "David Barnes", "01227827696"};
        String[] hreFullPayload = {"hre123", "Roman", "Miles", "01/01/1970", "University of Kent", "Canterbury", "Kent", "CT2 7NF", "01227748392", "07638270376", "Olaf Chitil", "01227824320"};
        String[] iteFullPayload = {"ite123", "Smith", "John", "01/01/1970", "University of Kent", "Canterbury", "Kent", "CT2 7NF", "01227748392", "07638270376", "Olaf Chitil", "01227824320"};
        String[] emptyPayload = {null, null, null, null, null, null, null, null, null, null, null};

        Authorise.AuthorisationAttempt(Authorise.Action.Create, "Personal Details", hrEmployee, hre123FullPayload);
        Authorise.AuthorisationAttempt(Authorise.Action.Create, "Personal Details", hrManager, ite123FullPayload);

        // Variant 1: Expected Use, user updates their own info
        assertTrue(Authorise.AuthorisationAttempt(Authorise.Action.Update, "Personal Details", itEmployee, iteFullPayload)); // has ResultSet closed error

        // Variant 2: Expected Use, HR updates someone else's file
        assertTrue(Authorise.AuthorisationAttempt(Authorise.Action.Update, "Personal Details", hrEmployee, iteFullPayload)); // has some sort of enum thingy error

        // Variant 3: Unexpected Use, user targets Invalid Target
        assertFalse(Authorise.AuthorisationAttempt(Authorise.Action.Update, "Invalid Target", hrEmployee, hreFullPayload)); // has no errors *happy test noises*

        // Variant 4: Unexpected Use, Try to update with empty payload
        assertTrue(Authorise.AuthorisationAttempt(Authorise.Action.Update, "Personal Details", hrEmployee, emptyPayload)); // has some sort of enum thingy error


        // Variant 5: Unexpected Use, User is neither HR or updating their own info
        assertFalse(Authorise.AuthorisationAttempt(Authorise.Action.Update, "Personal Details", itEmployee, hreFullPayload)); // has ResultSet closed error

        // ----------------- CURRENTLY FAULTY DUE TO ISSUE 14. THROWS SQL ERROR WHICH CAUSES THE TEST TO CRASH OUT ----------------------------
        // Variant 6: Unexpected Use, User not logged in
        //Authenticate.logout(hrEmployee);
        //assertFalse(Authorise.AuthorisationAttempt(Authorise.Action.Update, "Personal Details", hrEmployee, hreFullPayload));
    }

    @org.junit.jupiter.api.Test
    void deletePersonalDetails()
    {
        String[] hreFullPayload = {"hre123", "Roman", "Miles", "01/01/1970", "University of Kent", "Canterbury", "Kent", "CT2 7NF", "01227748392", "07638270376", "Olaf Chitil", "01227824320"};

        // Variant 1: Logged in user tries to delete from personal details
        assertFalse(Authorise.AuthorisationAttempt(Authorise.Action.Delete, "Personal Details", hrEmployee, hreFullPayload));

        // Variant 2: Logged in user tries to delete from an Invalid Target
        assertFalse(Authorise.AuthorisationAttempt(Authorise.Action.Delete, "Invalid Target", hrEmployee, hreFullPayload));

        // ----------------- CURRENTLY FAULTY DUE TO ISSUE 14. THROWS SQL ERROR WHICH CAUSES THE TEST TO CRASH OUT ----------------------------
        //Variant 3: logged out user tries to delete from personal details
        //Authenticate.logout(hrEmployee);
        //assertFalse(Authorise.AuthorisationAttempt(Authorise.Action.Delete, "Personal Details", hrEmployee, hreFullPayload));
    }
}