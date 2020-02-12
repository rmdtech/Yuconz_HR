import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User{

    private String employeeID;
    private String password;
    private String passwordSalt;
    private Position.Department department;
    private Position.Role role;

    // Does this need to be an object?
    private DatabaseParser dp = new DatabaseParser();

    /**
     * Constructor for User
     * @param employeeID String value given from GUI
     * @param password Encrypted password provided from GUI
     */
    public User(String employeeID, String password)
    {
        this.employeeID = employeeID;
        this.password = password;
        this.passwordSalt = null;
        department = null;
        role = null;
    }

    private String sha512Encrypt(String input)
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
        if (dp.checkEmployeeID(employeeID) == true)
        {
            passwordSalt=dp.fetchPasswordSalt(employeeID);
            String authenticationString = sha512Encrypt(password + passwordSalt);

            if (dp.fetchEmployeePassword(employeeID).equals(authenticationString))
            {
                // login
            }
        }
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
        department = Position.Department.valueOf(dp.fetchDepartment(employeeID));
    }

    /**
     * This will get the role associated with an employeeID from the database
     */
    private void fetchRole()
    {
        role = Position.Role.valueOf(dp.fetchRole(employeeID));
    }


    /**
     * Accessor method for the employeeID
     * @return the employeeID
     */
    public String getEmployeeID()
    {
        return employeeID;
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