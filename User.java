public class User{

    private String employeeID;
    private String password;
    private String passwordSalt;
    private Position.Department department;
    private Position.Role role;

    private DatabaseParser dp = new DatabaseParser();

    /**
     * Constructor for User
     * @param employeeID String value given from GUI
     * @param password Encrypted password provided from GUI
     * @param passwordSalt UUID provided from the GUI
     */
    public User(String employeeID, String password, String passwordSalt)
    {
        this.employeeID = employeeID;
        this.password = password;
        this.passwordSalt = passwordSalt;
        department = null;
        role = null;
    }

    /**
     * This will log the user into the system
     */
    public void login()
    {
        // Call the database parser and ask it if this employeeNumber exists within the database
        if (dp.checkEmployeeID(employeeID) == true)
        {
            // Call the database parser and ask it to read the encrypted password stored in the database
            if (dp.getEmployeePassword(employeeID).equals(password))
            {
                // Call the database parser and ask it to read the salt tied to this password
                if (dp.getPasswordSalt(employeeID).equals(passwordSalt))
                {
                    // do something that indicates the login status
                    getRoles();
                }
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