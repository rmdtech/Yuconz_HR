import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticateTest {

    @Test
    Integer login() {
        int failures = 0;
        System.out.println("\n----- Login tests -----");
        System.out.println("   Test: Correct Login 'abc123' 'password'");
        User abc123 = Authenticate.login("abc123", "password");
        if (abc123 != null)
        {
            if (abc123.getEmployeeId().equals("abc123"))
            {
                if (abc123.getSessionId() != null)
                {
                    System.out.println("   [Status] SessionID: " + abc123.getSessionId());
                    System.out.println("     [✓]    User was logged in");
                }
                else
                {
                    System.out.println("     [x]    User object created, but no sessionID written");
                    failures++;
                }
            }
            else
            {
                System.out.println("     [x]    Logged in user is: " + abc123.getEmployeeId());
                failures++;
            }
        }
        else
        {
            System.out.println("     [x]    Test failed. User Element not returned");
            failures++;
        }
        System.out.println("   Correct Login tests concluded");

        System.out.println("\n   Test: Incorrect password");
        User wrongPassword = Authenticate.login("abc123", "wrongpassword");
        if (wrongPassword == null)
        {
            System.out.println("     [✓]    User not logged in");
        }
        else
        {
            System.out.println("     [x]    Test failed. User has been logged in");
            System.out.println(wrongPassword.getEmployeeId());
            failures++;
        }
        System.out.println("   Incorrect password tests concluded\n");

        System.out.println("   Test: Incorrect employeeId");
        User wrongEmpID = Authenticate.login("err404", "password");
        if (wrongEmpID == null)
        {
            System.out.println("     [✓]    User not logged in");
        }
        else
        {
            System.out.println("     [x]    Test failed. User has been logged in");
            System.out.println(wrongEmpID.getEmployeeId());
            failures++;
        }
        System.out.println("   Incorrect employeeID tests concluded");
        return failures;
    }

    @Test
    Integer logout() {
        int failures = 0;
        System.out.println("\n----- Logout tests -----");
        System.out.println("   [Status] Loggin in user 'abc123'");
        User abc123 = Authenticate.login("abc123", "password");
        System.out.println("   [Status] sessionID: " + abc123.getSessionId());
        Authenticate.logout(abc123);
        System.out.print("MANUALLY CHECK IF SESSIONID IS STILL IN THE DATABASE");
        System.out.println("Logout test requires manual verification");
        return failures;

    }

    @Test
    Integer addNewUser() {
        int fails = 0;
        String password = "password";

        System.out.println("\n----- addNewUser tests -----");
        System.out.println("   Test: Add valid employeeID");
        String validEmpID = newEmployeeID();
        if (Authenticate.addNewUser(validEmpID, password, Position.Department.IT, Position.Role.Employee))
        {
            System.out.println("     [✓]    employeeId Written: " + validEmpID);
        }
        else
        {
            System.out.println("     [x]    " + validEmpID + " has not been added");
            fails++;
        }
        System.out.println("   valid employeeID test concluded");

        System.out.println("\n   Test: Add invalid (long) employeeID");
        if(Authenticate.addNewUser("invalidName", password, Position.Department.IT, Position.Role.Employee))
        {
            System.out.println("     [x]    invalidName has been added when it should have not");
            fails++;
        }
        else
        {
            System.out.println("     [✓]    invalidName has not been added");
        }
        System.out.println("   add invalid (long) employeeID test concluded");

        System.out.println("\n   Test: Add already existing employeeID");
        if(Authenticate.addNewUser("abc123", password, Position.Department.IT, Position.Role.Employee))
        {
            System.out.println("     [x]    already existing employee has been overwritten");
            fails++;
        }
        else
        {
            System.out.println("     [✓]    already existing entry has been rejected");
        }
        System.out.println("   add already existing employeeID test concluded");

        System.out.println("\n   Test: Add invalid, but same sized employeeID");
        if(Authenticate.addNewUser("testin", password, Position.Department.IT, Position.Role.Employee))
        {
            System.out.println("     [x]    testin has been added when it should have not");
            fails++;
        }
        else
        {
            System.out.println("     [✓]    testin has not been added");
        }
        System.out.println("   add invalid, but same sized employeeID test concluded");
        return fails;
    }

    @Test
    boolean sha512Encrypt() {
        System.out.println("\n----- Encryption -----");
        System.out.println("   Test: Encrypt known value");
        String testPassword = "password";
        String testSalt = "salt";
        String expected = "fa6a2185b3e0a9a85ef41ffb67ef3c1fb6f74980f8ebf970e4e72e353ed9537d593083c201dfd6e43e1c8a7aac2bc8dbb119c7dfb7d4b8f131111395bd70e97f";
        String generated = Authenticate.sha512Encrypt(testPassword, testSalt);
        if (generated.equals(expected))
        {
            System.out.println("      [✓]    Expected String matches generated String");
            System.out.println("All encyrption tests have passed");
            return true;
        }
        System.out.println("      [x]    Strings don't match");
        System.out.println("         Expected: " + expected);
        System.out.println("        Generated: " + generated);
        return false;

    }

    Integer randomNumericID()
    {
        double x = (Math.random()*((999-100)+1))+100;
        return (int)x;
    }

    String newEmployeeID()
    {
        // For valid employeeId, make sure this test ID hasn't already been used
        DatabaseParser dp = new DatabaseParser();
        String employeeId = "tes" + randomNumericID();
        while(dp.checkEmployeeId(employeeId))
        {
            employeeId = "tes" + randomNumericID();
        }
        return employeeId;
    }

}