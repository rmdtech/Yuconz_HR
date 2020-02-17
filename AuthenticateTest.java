class AuthenticateTest {
    public static boolean testEncryption()
    {
        System.out.println("\n----- Encryption -----");
        String testPassword = "password";
        String testSalt = "salt";
        String expected = "fa6a2185b3e0a9a85ef41ffb67ef3c1fb6f74980f8ebf970e4e72e353ed9537d593083c201dfd6e43e1c8a7aac2bc8dbb119c7dfb7d4b8f131111395bd70e97f";
        String generated = Authenticate.sha512Encrypt(testPassword, testSalt);
        if (generated.equals(expected))
        {
            System.out.println("  [✓]    Expected String matches generated String");
            return true;
        }
        System.out.println("  [x]    Strings don't match");
        System.out.println("         Expected: " + expected);
        System.out.println("        Generated: " + generated);
        return false;
    }



    public static Integer testLogin()
    {
        int failures = 0;
        System.out.println("\n----- Login tests -----");
        System.out.println("  Test: Correct Login 'abc123' 'password'");
        User abc123 = Authenticate.login("abc123", "password");
        if (abc123 != null)
        {
            if (abc123.getEmployeeId().equals("abc123"))
            {
                System.out.println("  [✓]    User was logged in");
            }
            System.out.println("  [x]    Logged in user is: " + abc123.getEmployeeId());
            failures++;
        }
        else
        {
            System.out.println("  [x]    Test failed. User Element not returned");
            failures++;
        }
        System.out.println("  Correct Login tests concluded\n");

        System.out.println("\n  Test: Incorrect password");
        User wrongPassword = Authenticate.login("abc123", "wrongpassword");
        if (wrongPassword == null)
        {
            System.out.println("  [✓]    User not logged in");
        }
        else
        {
            System.out.println("  [x]    Test failed. User has been logged in");
            System.out.println(wrongPassword.getEmployeeId());
            failures++;
        }
        System.out.println("  Incorrect password tests concluded\n");

        System.out.println("  Test: Incorrect employeeId");
        User wrongEmpID = Authenticate.login("err404", "password");
        if (wrongEmpID == null)
        {
            System.out.println("  [✓]    User not logged in");
        }
        else
        {
            System.out.println("  [x]    Test failed. User has been logged in");
            System.out.println(wrongEmpID.getEmployeeId());
            failures++;
        }
        System.out.println("  Incorrect employeeID tests concluded");
        return failures;
    }
}
