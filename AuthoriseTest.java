import static org.junit.jupiter.api.Assertions.*;

class AuthoriseTest {
    private User hrEmployee;
    private User itEmployee;
    private User hrManager;

    @org.junit.jupiter.api.BeforeEach
    void setUp()
    {
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
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown()
    {

    }

    @org.junit.jupiter.api.Test
    void authorisationAttempt()
    {
        createPersonalDetails();
    }

    @org.junit.jupiter.api.Test
    void createPersonalDetails()
    {
        setUp();
        System.out.println("---- Test output here --- ");
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
        Authenticate.logout(hrEmployee);
        assertFalse(Authorise.AuthorisationAttempt(Authorise.Action.Create, "Personal Details", hrEmployee, hre123FullPayload));
    }
}