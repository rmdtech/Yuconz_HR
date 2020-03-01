import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthoriseTest {

    DatabaseParser dp = new DatabaseParser();

    @BeforeEach
    void setUp()
    {
        dp.sqlUpdate("DELETE FROM Session");
        dp.sqlUpdate("DELETE FROM AuthorisationLog");
        dp.sqlUpdate("DELETE FROM AuthenticationLog");
        dp.sqlUpdate("DELETE FROM User");
    }

    @Test
    void authorisationAttemptNoLogin()
    {
        Authenticate.addNewUser("abc123", "password", Position.Department.IT, Position.Role.Employee);
        User testUser = Authenticate.login("abc123", "password");
        Authenticate.logout(testUser);
        assertFalse(Authorise.AuthorisationAttempt("Read", "abc123 - Personal Details", Position.Department.HR, Position.Role.Employee, testUser));
    }

    @Test
    void authorisationAttemptWrongDepartment()
    {
        Authenticate.addNewUser("abc123", "password", Position.Department.BI, Position.Role.Employee);
        User testUser = Authenticate.login("abc456", "password");
        assertFalse(Authorise.AuthorisationAttempt("Read", "abc456 - Personal Details", Position.Department.HR, Position.Role.Employee, testUser));
    }

    @Test
    void authorisationAttemptWrongRole()
    {
        Authenticate.addNewUser("abc123", "password", Position.Department.HR, Position.Role.Employee);
        User testUser = Authenticate.login("abc123", "password");
        assertFalse(Authorise.AuthorisationAttempt("Read", "abc123 - Personal Details", Position.Department.HR, Position.Role.Manager, testUser));
    }

    @Test
    void authorisationAttemptTrue()
    {
        Authenticate.addNewUser("abc123", "password", Position.Department.HR, Position.Role.Employee);
        User testUser = Authenticate.login("abc123", "password");
        assertTrue(Authorise.AuthorisationAttempt("Read", "abc123 - Personal Details", Position.Department.HR, Position.Role.Employee, testUser));
    }
}