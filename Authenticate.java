import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Authenticate{
    private static DatabaseParser dp = new DatabaseParser();

    /**
     * Creates a Session for this user and returns a User object
     * @param employeeId the Employee ID of the User that is to be logged in
     * @param password the Password of the User that is to be logged in
     * @return the User object of them who is now logged in
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
     * Log a User out of the system
     * @param user the User object that is to be logged out
     */
    public static void logout(User user)
    {
        dp.deleteSession(user.getSessionId());
    }

    /**
     * Writes a new User to the database
     * @param employeeId a User's Employee ID
     * @param password a User's password
     * @param department a User's Department
     * @param role a User's Role
     * @return whether or not the operation has been successful
     */
    public static boolean addNewUser(String employeeId, String password, String supervisor, Position.Department department, Position.Role role)
    {
        String passwordSalt = User.generateUUID();

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
        if (dp.fetchRole(supervisor) != null && dp.fetchRole(supervisor).level <= (Position.Role.Employee.level))
        {
            System.out.println("Supervisor cannot be of the same or lower level than the User you are trying to add");
            return false;
        }

        dp.newEmployee(employeeId, passwordSalt, sha512Encrypt(password, passwordSalt), supervisor ,department.label, role.label);
        System.out.println("New User has been created and added to the Database");
        return true;
    }

    /**
     * Checks if a certain User has active sessions
     * @param user the User object that should be checked
     * @return whether or not this User is logged in
     */
    public static boolean isUserLoggedIn(User user)
    {
        return dp.isLoggedIn(user.getEmployeeId(), user.getSessionId());
    }

    /**
     * Encrypts a given String using the SHA-512 standard
     * @param password the string to be encrypted
     * @param salt the associated salt
     * @return the encrypted string
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
     * Checks if the given credentials match those stored in the Database
     * @param employeeId the employeeId of the User
     * @param password the password of the User
     * @return whether or not the credentials are correct
     */
    private static boolean verifyPassword(String employeeId, String password)
    {
        String passwordSalt=dp.fetchPasswordSalt(employeeId);
        String authenticationString = sha512Encrypt(password, passwordSalt);
        return dp.fetchEmployeePassword(employeeId).equals(authenticationString);
    }
}