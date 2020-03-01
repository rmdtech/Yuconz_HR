import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticateTest {
    DatabaseParser dp = new DatabaseParser();

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        dp.sqlUpdate("DELETE FROM Session");
        dp.sqlUpdate("DELETE FROM AuthorisationLog");
        dp.sqlUpdate("DELETE FROM AuthenticationLog");
        dp.sqlUpdate("DELETE FROM User");
    }


    String sqlRead(String query, String coloumn) {
        String row = "";
        dp.sqlRead(query);
        try {
            dp.result.next(); // only ever be one result, while loop not required
            row = dp.result.getString(coloumn);
            dp.result.close();
            dp.stmt.close();
        } catch (SQLException e) {

        }
        return row;
    }

    @org.junit.jupiter.api.Test
    void sha512encrypt() {
        assertEquals("fa6a2185b3e0a9a85ef41ffb67ef3c1fb6f74980f8ebf970e4e72e353ed9537d593083c201dfd6e43e1c8a7aac2bc8dbb119c7dfb7d4b8f131111395bd70e97f", Authenticate.sha512Encrypt("password", "salt"));
    }

    @org.junit.jupiter.api.Test
    void testAddNewValidUser()
    {
        assert(Authenticate.addNewUser("abc123", "password", Position.Department.IT, Position.Role.Employee));
    }

    @org.junit.jupiter.api.Test
    void testAddNewLongEmployeeId()
    {
        assertFalse(Authenticate.addNewUser("invalidName", "password", Position.Department.IT, Position.Role.Employee));
    }

    @org.junit.jupiter.api.Test
    void testAddNewShortEmployeeId()
    {
        assertFalse(Authenticate.addNewUser("ab12", "password", Position.Department.IT, Position.Role.Employee));
    }

    @org.junit.jupiter.api.Test
    void testAddNewInvalidEmployeeId()
    {
        assertFalse(Authenticate.addNewUser("abcdef", "password", Position.Department.IT, Position.Role.Employee));
    }

    @org.junit.jupiter.api.Test
    void testAddExistingUser()
    {
        Authenticate.addNewUser("abc123", "password", Position.Department.IT, Position.Role.Employee);
        assertFalse(Authenticate.addNewUser("abc123", "password", Position.Department.IT, Position.Role.Employee));
    }

    @org.junit.jupiter.api.Test
    void testValidLogin() {
        Authenticate.addNewUser("abc123", "password", Position.Department.HR, Position.Role.Employee);
        assertNotNull(Authenticate.login("abc123", "password"));
    }

    @org.junit.jupiter.api.Test
    void testInvalidPassword() {
        Authenticate.addNewUser("abc123", "password", Position.Department.HR, Position.Role.Employee);
        assertNull(Authenticate.login("abc123", "wrongpassword"));
    }

    @org.junit.jupiter.api.Test
    void testInvalidUserId() {
        assertNull(Authenticate.login("err404", "password"));
    }

    @org.junit.jupiter.api.Test
    void testLogout() {
        Authenticate.addNewUser("abc123", "password", Position.Department.HR, Position.Role.Employee);
        User testUser = Authenticate.login("abc123", "password");

        String beforeLogout = sqlRead("SELECT * FROM Session", "employeeId");

        Authenticate.logout(testUser);

        String afterLogout = sqlRead("SELECT * FROM Session", "employeeId");

        assertNotEquals(beforeLogout, afterLogout);
    }
}