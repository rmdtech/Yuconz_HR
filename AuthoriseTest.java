import java.lang.reflect.Array;

import static org.junit.jupiter.api.Assertions.*;

class AuthoriseTest {

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
    }

    @org.junit.jupiter.api.Test
    Integer authorisationAttempt() {
        int failures = 0;
        String[] payload1 = {"hrm123","Roman", "Miles", "01/01/1970", "University of Kent", "Canterbury", "Kent", "CT2 7NF", "01227748392", "07638270376", "David Barnes", "01227827696"};
        String[] payload2 = {null, null, null, null, null, null, null, null, null, null, null, null};
        User user = Authenticate.login("hrm123", "password");
        DatabaseParser dp = new DatabaseParser();
        String[] response;
        response = dp.fetchPersonalDetailsPermissions(user.getEmployeeId());
        Position.Department requiredDpt = Position.Department.valueOf(response[0]);
        int minimumLevel = Integer.getInteger(response[1]);
        String associatedEmployee = response[2];

        System.out.println("\n----- Update Contact Info Tests -----");
        System.out.println("   Test: Update valid personal details");
        Boolean authAttempt1 = Authorise.AuthorisationAttempt(Authorise.Action.Update, "Personal Details", user, payload1);
        if(authAttempt1)
        {
            System.out.println("     [✓]    Personal Details updated successfully");
        }
        else
        {
            if(!(associatedEmployee.equals(user.getEmployeeId())))
            {
                System.out.println("     [x]    Test failed. User is not associated employee");
                failures++;
            }
            else if(!(user.getDepartment().equals(requiredDpt)))
            {
                System.out.println("     [x]    Test failed. User is not from the required department");
                failures++;
            }
            else if(!(user.getRole().getLevel() >= minimumLevel))
            {
                System.out.println("     [x]    Test failed. User does not have the minimum level required");
                failures++;
            }
        }

        System.out.println("   Test: Update performance review");
        Boolean authAttempt2 = Authorise.AuthorisationAttempt(Authorise.Action.Update, "Performance Review", user, payload1);
        if(authAttempt2 == null)
        {

        }
        return failures;
    }
}