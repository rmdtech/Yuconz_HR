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
    private String sessionId;
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
        sessionId = null;
        department = null;
        role = null;
    }

    /**
     * Creates and writes a new User to the database
     * @param employeeId Their employeeID which will be verified within this function
     * @param password Their chosen password (has no requirements yet)
     * @param department The department which they are assigned to
     * @param role Their role within the company
     * @return If the action has been successful
     */
    public boolean addNewUser(String employeeId, String password, String department, String role)
    {
        Matcher employeeIdMatcher = Pattern.compile("[a-z]{3}[0-9]{3}").matcher(employeeId);
        if (!employeeIdMatcher.matches())
        {
            // notify that the employeeId given is in the wrong format
            return false;
        }
        if(dp.checkEmployeeId(employeeId))
        {
            // notify that this employeeId is already in use
            return false;
        }


        this.department = Position.Department.valueOf(department);
        this.role = Position.Role.valueOf(role);
        dp.newEmployee(employeeId, passwordSalt, sha512Encrypt(password, passwordSalt), department, role);

        return true;
    }

    public boolean updatePassword(String updatedPassword)
    {
        UUID uuid = new UUID(15,0);
        passwordSalt = uuid.toString();
        dp.updatePassword(employeeId, sha512Encrypt(updatedPassword, passwordSalt), passwordSalt);
        return true;
    }

    public boolean isLoggedIn()
    {
        if (sessionId.equals(dp.fetchSessionId(getEmployeeId())))
        {
            return true;
        }
        return false;
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
                sessionId = new UUID(15,0).toString();
                dp.createSession(employeeId, sessionId);
            }
        }
    }

    /**
     * This will log the user out of the system
     */
    public void logout()
    {
        dp.deleteSession(employeeId);
    }

    /**
     * Encrypts a given String using the SHA-512 standard
     * @param password The password to be encrypted
     * @param salt The associated salt
     * @return The encrypted String
     */
    private String sha512Encrypt(String password, String salt)
    {
        // Courtesy of howtodoin.java.ocm
        String hashedString = null;
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes());
            byte[] bytes = md.digest(password.getBytes());
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
     * @return If the passwords match
     */
    private boolean verifyPassword()
    {
        passwordSalt=dp.fetchPasswordSalt(employeeId);
        String authenticationString = sha512Encrypt(password, passwordSalt);
        return dp.fetchEmployeePassword(employeeId).equals(authenticationString);
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

    public String getSessionId()
    {
        return sessionId;
    }
}