
public class Main
{
    public static Integer testAuthenticate()
    {
        int fails = 0;
        if (!AuthenticateTest.testEncryption())
        {
            fails++;
        }

        int loginTestsFailed = AuthenticateTest.testLogin();
        if (loginTestsFailed == 0)
        {
            System.out.println("All login tests have passed");
        }
        else
        {
            System.out.println("\n" + loginTestsFailed + " login tests have failed");
            fails += loginTestsFailed;
        }
        AuthenticateTest.testLogout();

        int addUserTestsFailed = AuthenticateTest.testAddNewUser();
        if (addUserTestsFailed == 0)
        {
            System.out.println("All addUser tests have passed");
        }
        else
        {
            System.out.println("\n " + addUserTestsFailed + " addUser tests have failed");
            fails += addUserTestsFailed;
        }
        return fails;
    }

    public static Integer testAuthorise()
    {
        return AuthoriseTest.testAuthorisation();
    }
    public static void main(String[] args)
    {
        System.out.println("===== Unit testing Authenticate =====");
        int authenticateFails = testAuthenticate();
        System.out.println("===== " + authenticateFails + " test fails in Authenticate =====");

        System.out.println("===== Unit testing Authorise =====");
        int authoriseFails = testAuthorise();
        System.out.println("===== " + authoriseFails + " test fails in Authenticate =====");
    }
}