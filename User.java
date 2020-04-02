import java.util.UUID;

public class User{
    private String employeeId;
    private String sessionId;
    private Position.Department department;
    private Position.Role role;
    private String directSupervisor;

    /**
     * Constructor for User
     * @param employeeId String value given from GUI
     * @param sessionId SessionID for this user
     */
    public User(String employeeId, String sessionId)
    {
        this.employeeId= employeeId;
        this.sessionId = sessionId;
        this.directSupervisor = null;
        department = null;
        role = null;
    }

    /**
     * Generates a new UUID
     * @return 32bit long UUID
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
     * @param managerId the Employee ID of this User's Direct Manager
     */
    public void setDirectSupervisor(String managerId)
    {
        directSupervisor = managerId;
    }


    /**
     * Checks whether this User object is logged in
     * @return Logged in status
     */
    public boolean isLoggedIn()
    {
        return Authenticate.isUserLoggedIn(this);
    }

    /**
     * Accessor method for the Employee ID
     * @return the Employee ID
     */
    public String getEmployeeId()
    {
        return employeeId;
    }

    /**
     * Accessor method for the Department
     * @return the Department Enum
     */
    public Position.Department getDepartment()
    {
        return department;
    }

    /**
     * Accessor method for the role
     * @return the Role enum
     */
    public Position.Role getRole()
    {
        return role;
    }

    /**
     * Accessor method for the Session ID
     * @return The Session ID
     */
    public String getSessionId()
    {
        return sessionId;
    }

    /**
     * Accessor method for the Direct Manager's Employee ID
     * @return The Employee ID of the Direct Manager
     */
    public String getDirectSupervisor()
    {
        return directSupervisor;
    }
}