import java.util.UUID;

public class User{
    private String employeeId;
    private String sessionId;
    private Position.Department department;
    private Position.Role role;
    private String directSupervisor;
    private String firstName;
    private String lastName;
    private DatabaseParser dp;

    /**
     * Constructor for User
     * @param employeeId String value given from GUI
     * @param sessionId SessionID for this user
     */
    public User(String employeeId, String sessionId)
    {
        dp = new DatabaseParser();
        this.employeeId= employeeId;
        this.sessionId = sessionId;
        this.directSupervisor = null;
        department = null;
        role = null;
    }

    /**
     * This method returns a new Salt
     * @return new unique salt
     */
    public static String generateUUID()
    {
        return  UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Mutator method for the Department Enum
     * @param department enum of Position.Department
     */
    public void setDepartment(Position.Department department)
    {
        this.department = department;
    }

    /**
     * Mutator method for the Role Enum
     * @param role enum of Position.Role
     */
    public void setRole(Position.Role role)
    {
        this.role = role;
    }

    /**
     * Mutator method for the Direct Manager
     * @param managerId the employeeId of their Manager
     */
    public void setDirectSupervisor(String managerId)
    {
        directSupervisor = managerId;
    }


    /**
     * This method calls the Authenticate class which will run checks if the password is valid and update this in
     * the database accordingly
     * @param updatedPassword The desired new password for this User
     * @return Whether or not this operation was successful
     */
    /*
    public boolean updatePassword(String updatedPassword)
    {
        return Authenticate.updatePassword(employeeId, updatedPassword, generateSalt());
    }
    */
    /**
     * Checks whether this User object is logged in
     * @return Logged in status
     */
    public boolean isLoggedIn()
    {
        return Authenticate.isUserLoggedIn(this);
    }

    /**
     * Accessor method for the employeeID
     * @return the employeeID
     */
    public String getEmployeeId()
    {
        return employeeId;
    }

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

    /**
     * Accessor method for the sessionID
     * @return The Sesssion ID
     */
    public String getSessionId()
    {
        return sessionId;
    }

    /**
     * Accessor method for the directManagerID
     * @return The employeeId of the direct manager
     */
    public String getDirectSupervisor()
    {
        return directSupervisor;
    }
}