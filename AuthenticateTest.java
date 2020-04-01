import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.File;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticateTest {

    DatabaseParser dp;
    Connection c = null;
    Statement stmt = null;
    ResultSet result = null;

    @BeforeEach
    void setUp()
    {
        dbSetup();
    }

    @Test
    void sha512encrypt()
    {
        assertEquals("fa6a2185b3e0a9a85ef41ffb67ef3c1fb6f74980f8ebf970e4e72e353ed9537d593083c201dfd6e43e1c8a7aac2bc8dbb119c7dfb7d4b8f131111395bd70e97f", Authenticate.sha512Encrypt("password", "salt"));
    }

    @Test
    void addUserNoSupervisor()
    {
        assert(Authenticate.addNewUser("dir123","password", null, Position.Department.IT, Position.Role.Director));
    }

    @Test
    void addUser()
    {
        assert(Authenticate.addNewUser("dir123","password", null, Position.Department.IT, Position.Role.Director));
        assert(Authenticate.addNewUser("itm123","password", "dir123", Position.Department.IT, Position.Role.Manager));
    }

    @Test
    void addUserSupervisorLowerLevel()
    {
        assert(Authenticate.addNewUser("itm123","password", null, Position.Department.IT, Position.Role.Manager));
        assertFalse(Authenticate.addNewUser("itm456","password", "dir123", Position.Department.IT, Position.Role.Manager));
    }

    @Test
    void addNewLongEmployeeId()
    {
        assertFalse(Authenticate.addNewUser("invalidName", "password", null, Position.Department.IT, Position.Role.Employee));
    }

    @Test
    void addNewShortEmployeeId()
    {
        assertFalse(Authenticate.addNewUser("ab12", "password", null, Position.Department.IT, Position.Role.Employee));
    }

    @Test
    void addNewInvalidEmployeeId()
    {
        assertFalse(Authenticate.addNewUser("abcdef", "password", null, Position.Department.IT, Position.Role.Employee));
    }

    @Test
    void addExistingUser()
    {
        Authenticate.addNewUser("abc123", "password", null, Position.Department.IT, Position.Role.Employee);
        assertFalse(Authenticate.addNewUser("abc123", "password", null, Position.Department.IT, Position.Role.Employee));
    }

    @Test
    void validLogin()
    {
        Authenticate.addNewUser("abc123", "password", null, Position.Department.HR, Position.Role.Employee);
        assertNotNull(Authenticate.login("abc123", "password"));
    }

    @Test
    void loginInvalidUserId()
    {
        assertNull(Authenticate.login("err404", "password"));
    }

    @Test
    void logout() {
        Authenticate.addNewUser("abc123", "password", null, Position.Department.HR, Position.Role.Employee);
        User testUser = Authenticate.login("abc123", "password");
        String beforeLogout = sqlRead("SELECT * FROM Session;", "employeeId");
        Authenticate.logout(testUser);
        String afterLogout = sqlRead("SELECT * FROM Session;", "employeeId");

        assertNotEquals(beforeLogout, afterLogout);
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

    String sqlRead(String sql, String column)
    {
        dp = new DatabaseParser();
        String row = "";
        dp.sqlRead(sql);
        try{
            dp.result.next();
            row = dp.result.getString(column);
            dp.result.close();
            dp.stmt.close();
        }
        catch (SQLException e)
        {

        }
        return row;
    }
}