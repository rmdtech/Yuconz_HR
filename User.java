import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class User{

    private String employeeId;
    private String password;
    private String passwordSalt;
    private Position.Department department;
    private Position.Role role;

    // Does this need to be an object?
    private DatabaseParser dp = new DatabaseParser();

    /**
     * Constructor for User
     * @param employeeId String value given from GUI
     * @param password Encrypted password provided from GUI
     */
    public User(String employeeId, String password)
    {
        this.employeeId= employeeId;
        this.password = password;
        this.passwordSalt = null;
        department = null;
        role = null;
    }

    public boolean addNewUser(String employeeId, String password, String department, String role)
    {
        Matcher employeeIdMatcher = Pattern.compile("[a-z]{3}[0-9]{3}").matcher(employeeId);
        if (!employeeIdMatcher.matches())
        {
            // notify that the employeeId given is in the wrong format
            return false;
        }
        UUID uuid = new UUID(15,0);
        passwordSalt = uuid.toString();
        if (verifyPassword())
        {
            this.department = Position.Department.valueOf(department);
            this.role = Position.Role.valueOf(role);
            dp.newEmployee(employeeId, passwordSalt, sha512Encrypt(password+passwordSalt), department, role);
            return true;
        }
        return false;
    }
    /**
     * Encrypts a given String using the SHA-512 standard
     * @param input The String to be encrypted
     * @return The encrypted String
     */
    private String sha512Encrypt(@NotNull String input)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * This will log the user into the system
     */
    public void login()
    {
        if (dp.checkEmployeeId(employeeId))
        {
            if (verifyPassword())
            {
               department=Position.Department.valueOf(dp.fetchDepartment(employeeId));
               role=Position.Role.valueOf(dp.fetchRole(employeeId));
                // login
            }
        }
    }
private boolean verifyPassword()
{
    passwordSalt=dp.fetchPasswordSalt(employeeId);
    String authenticationString = sha512Encrypt(password + passwordSalt);
    return dp.fetchEmployeePassword(employeeId).equals(authenticationString);
}
    /**
     * This will log the user out of the system
     */
    public void logout()
    {

    }


    // Database fetching roles
    /**
     * This will get the department associated with an employeeID from the database
     */
    private void fetchDepartment()
    {
        department = Position.Department.valueOf(dp.fetchDepartment(employeeId));
    }

    /**
     * This will get the role associated with an employeeID from the database
     */
    private void fetchRole()
    {
        role = Position.Role.valueOf(dp.fetchRole(employeeId));
    }


    /**
     * Accessor method for the employeeID
     * @return the employeeID
     */
    public String getEmployeeId()
    {
        return employeeId;
    }

    /**
     * Accessor method for the department
     * @return the department enum
     */
    public Position.Department getDepartment()
    {
        return department;
    }

    /**
     * Accessor method for the role
     * @return the role enum
     */
    public Position.Role getRole()
    {
        return role;
    }

}