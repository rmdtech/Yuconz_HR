public class AuthoriseTest
{
    public static Integer testAuthorisation()
    {
        int fails = 0;
        System.out.println("\n----- Authorisation -----");
        System.out.println("   Test: Attempt action when not logged in (no sessionID)");
        User nullSessionIdUser = new User(AuthenticateTest.newEmployeeID(), null);
        nullSessionIdUser.setDepartment(Position.Department.HR);
        nullSessionIdUser.setRole(Position.Role.Employee);
        if (Authorise.AuthorisationAttempt("Read with no session ID", "abc123 - Personal Details", Position.Department.HR, Position.Role.Employee, nullSessionIdUser))
        {
            System.out.println("      [x]    " + nullSessionIdUser.getEmployeeId() + " was not logged in");
            fails++;
        }
        else
        {
            System.out.println("      [✓]    Action has been rejected, as expected");
        }
        System.out.println("   Attempt action when not logged in (no sessionID) concluded");

        System.out.println("   Test: Attempt action when not logged in (no sessionID)");
        User invalidSessionIdUser = new User(AuthenticateTest.newEmployeeID(), "invalidSessionID");
        invalidSessionIdUser.setDepartment(Position.Department.HR);
        invalidSessionIdUser.setRole(Position.Role.Employee);
        if (Authorise.AuthorisationAttempt("Read with invalid sessionId", "abc123 - Personal Details", Position.Department.HR, Position.Role.Employee, invalidSessionIdUser))
        {
            System.out.println("      [x]    " + invalidSessionIdUser.getEmployeeId() + " was not logged in");
            fails++;
        }
        else
        {
            System.out.println("      [✓]    Action has been rejected, as expected");
        }
        System.out.println("   Attempt action when not logged in (invalid sessionID) concluded");

        System.out.println("   Test: Attempt action from wrong department");
        String wrongDepartmentID = AuthenticateTest.newEmployeeID();
        Authenticate.addNewUser(wrongDepartmentID, "password", Position.Department.BI, Position.Role.Employee);
        User  wrongDepartmentUser = Authenticate.login(wrongDepartmentID, "password");
        if (Authorise.AuthorisationAttempt("Read from wrong department", "abc123 - Personal Details", Position.Department.HR, Position.Role.Employee, wrongDepartmentUser))
        {
            System.out.println("      [x]    " + wrongDepartmentUser.getEmployeeId() + " was in the wrong department but action got authorised");
            fails++;
        }
        else
        {
            System.out.println("      [✓]    Action has been rejected, as expected");
        }
        System.out.println("   Attempt action from wrong department concluded");

        System.out.println("   Test: Attempt action from wrong role");
        String wrongRoleID = AuthenticateTest.newEmployeeID();
        Authenticate.addNewUser(wrongRoleID, "password", Position.Department.BI, Position.Role.Employee);
        User  wrongRoleUser = Authenticate.login(wrongDepartmentID, "password");
        if (Authorise.AuthorisationAttempt("Read from wrong department", "abc123 - Personal Details", Position.Department.HR, Position.Role.Employee, wrongRoleUser))
        {
            System.out.println("      [x]    " + wrongRoleUser.getEmployeeId() + " did not meet the min. role but still got authorised");
            fails++;
        }
        else
        {
            System.out.println("      [✓]    Action has been rejected, as expected");
        }
        System.out.println("   Attempt action from wrong role concluded");

        System.out.println("   Test: Attempt action with uninitiated User");
        String incompleteUserId = AuthenticateTest.newEmployeeID();
        User  incompleteUser = new User(incompleteUserId, "invalidSession");
        if (Authorise.AuthorisationAttempt("Read from wrong department", "abc123 - Personal Details", Position.Department.HR, Position.Role.Employee, incompleteUser))
        {
            System.out.println("      [x]    " + incompleteUser.getEmployeeId() + " user is not initialised properly yet action to accepted");
            fails++;
        }
        else
        {
            System.out.println("      [✓]    Action has been rejected, as expected");
        }
        System.out.println("   Attempt action with uninitiated User concluded");

        System.out.println("   Test: Attempt action with valid User");
        User trueUser = Authenticate.login("abc123", "password");
        if (Authorise.AuthorisationAttempt("Read from wrong department", "abc123 - Personal Details", Position.Department.HR, Position.Role.Employee, trueUser))
        {
            System.out.println("      [✓]    Action has been authorised as intended");
        }
        else
        {
            System.out.println("      [x]    Action got denied despite valid credentials");
            fails++;
        }
        System.out.println("   Attempt action with valid User concluded");
        System.out.println("All Authorise tests have finished");
        return fails;
    }
}
