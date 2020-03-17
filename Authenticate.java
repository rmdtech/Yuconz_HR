import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

public class Authenticate{
    private static DatabaseParser dp = new DatabaseParser();

    /**
     * This will create a sessionID for this user and add it to the local memory of logged in users
     * @param employeeId the employeeID of the User that is to be logged in
     * @param password the password of the User that is to be logged in
     * @return the user object of them who is now logged in
     */
    public static User login(String employeeId, String password)
    {
        if (dp.checkEmployeeId(employeeId))
        {
            if (verifyPassword(employeeId, password))
            {
                User newUser = new User(employeeId, UUID.randomUUID().toString().replace("-", ""));
                newUser.setDepartment(dp.fetchDepartment(employeeId));
                newUser.setRole(dp.fetchRole(employeeId));
                newUser.setDirectSupervisor(dp.fetchDirectSupervisor(newUser.getEmployeeId()));
                dp.createSession(employeeId, newUser.getSessionId());
                return newUser;
            }
        }
        return null;
    }

    /**
     * This will log the user out of the system
     * @param user the user object that is to be logged out
     */
    public static void logout(User user)
    {
        dp.deleteSession(user.getSessionId());
    }

    /**
     * Creates and writes a new User to the database
     * @param employeeId Their employeeID which will be verified within this function
     * @param password Their chosen password (has no requirements yet)
     * @param department The department which they are assigned to
     * @param role Their role within the company
     * @return If the action has been successful
     */
    public static boolean addNewUser(String employeeId, String password, String supervisor, Position.Department department, Position.Role role)
    {
        String passwordSalt = User.generateSalt();

        Matcher employeeIdMatcher = Pattern.compile("[a-z]{3}[0-9]{3}").matcher(employeeId);
        if (!employeeIdMatcher.matches())
        {
            System.out.println(employeeId + " is not valid.");
            return false;
        }
        if(dp.checkEmployeeId(employeeId))
        {
            System.out.println("An employee with ID: "+ employeeId + " already exists");
            return false;
        }
        if (supervisor != null && !dp.checkEmployeeId(supervisor))
        {
            System.out.println("No supervisor with this ID exists");
            return false;
        }
        if (dp.fetchRole(supervisor) != null && (dp.fetchRole(supervisor).level <= (Position.Role.Employee.level)))
        {
            System.out.println("Supervisor cannot be of the same or lower level than the User you are trying to add");
            return false;
        }

        dp.newEmployee(employeeId, passwordSalt, sha512Encrypt(password, passwordSalt), supervisor ,department.label, role.label);
        System.out.println("New User has been created and added to the Database");
        return true;
    }

    /**
     * Returns a given User object based on the employeeId given
     * @param user The user object that should be checked
     * @return The User with such employee ID. If not found, this is null
     */
    public static boolean isUserLoggedIn(User user)
    {
        return dp.isLoggedIn(user.getEmployeeId(), user.getSessionId());
    }

    /**
     * Encrypts a given String using the SHA-512 standard
     * @param password The password to be encrypted
     * @param salt The associated salt
     * @return The encrypted String
     */
    public static String sha512Encrypt(String password, String salt)
    {
        // Courtesy of howtodoin.java.com
        String hashedString = null;
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(password.getBytes());
            byte[] bytes = md.digest(salt.getBytes());
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            hashedString = sb.toString();
            return hashedString;
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return hashedString;
        }
    }

    /**
     * This verifies the password given in the constructor with the salt stored in the Database.
     * @param employeeId the employeeId of the user that is to in logged in
     * @param password the password of the user that is to be logged in
     * @return If the passwords match
     */
    private static boolean verifyPassword(String employeeId, String password)
    {
        String passwordSalt=dp.fetchPasswordSalt(employeeId);
        String authenticationString = sha512Encrypt(password, passwordSalt);
        return dp.fetchEmployeePassword(employeeId).equals(authenticationString);
    }
}