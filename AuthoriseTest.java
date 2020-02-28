import java.util.zip.DataFormatException;

import static org.junit.jupiter.api.Assertions.*;

class AuthoriseTest {
    User hrEmployee;
    User itEmployee;
    User hrManager;
    @org.junit.jupiter.api.BeforeEach
    void setUp()
    {
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
        DatabaseParser dp = new DatabaseParser();
        dp.deleteSession(hrEmployee.getSessionId());
        dp.sqlUpdate("DELETE * FROM User WHERE employeeId = 'hre123'");
        dp.sqlUpdate("DELETE * FROM PersonalDetails WHERE employeeId = 'hre123'");
        dp.deleteSession(itEmployee.getSessionId());
        dp.sqlUpdate("DELETE * FROM User WHERE employeeId = 'ite123'");
        dp.sqlUpdate("DELETE * FROM PersonalDetails WHERE employeeId = 'ite123'");
        dp.deleteSession(hrManager.getSessionId());
        dp.sqlUpdate("DELETE * FROM User WHERE employeeId = 'hrm123'");
        dp.sqlUpdate("DELETE * FROM PersonalDetails WHERE employeeId = 'hrm123'");
    }

    @org.junit.jupiter.api.Test
    void authorisationAttempt()
    {

    }
}