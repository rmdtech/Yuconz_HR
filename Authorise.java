/**
 * Constructor for class Authorise
 */
public class Authorise
{
    /**
     * Each CRUD operation is stored as an Enum to eliminate human error
     */
    public enum Action{
        Create("Create"),
        Read("Read"),
        Update("Update"),
        Delete("Delete");

        public final String label;

        /**
         * Constructor for an operation Enum
         * @param label The label with which this Enum is to be initialised with
         */
        Action (String label)
        {
            this.label = label;
        }

        /**
         * Get the String value of an operation
         * @return the String value of an operation
         */
        public String toString()
        {
            return label;
        }
    }

    static DatabaseParser dp = new DatabaseParser();

    /**
     * Creates a new Personal Details record for a member of Staff
     * @param user the user performing the action
     * @param details the information that is to be written into the Personal Details record [employeeID, surname, name, date of birth, address, city, postcode, telephoneNumber, mobileNumber, emergency contact, emergency contact number]
     * @return whether or not the action has been successful
     */
    public static boolean createPersonalDetailsRecord(User user, String[] details)
    {
        if (AuthorisationAttempt(Action.Create, "Personal Details", user, details))
        {
            if (details[0] == null)
            {
                return false;
            }
            return dp.createPersonalDetailsRecord(details, User.generateSalt());
        }
        return false;
    }

    /**
     * Read the Personal Details record of a certain member of Staff
     * @param user the user that is performing the action
     * @param pdEmpId the employeeId of which the user is accessing the Personal Details record from
     * @return a String array containing the Personal Details record [employeeID, surname, name, date of birth, address, city, postcode, telephoneNumber, mobileNumber, emergency contact, emergency contact number]
     */
    public static String[] readPersonalDetails (User user, String pdEmpId)
    {
        if (AuthorisationAttempt(Action.Read, "Personal Details", user, new String[] { pdEmpId } ))
        {
            dp.recordAuthorisationAttempt(user.getEmployeeId(), Action.Update.label, "Personal Details", true);
            return dp.fetchPersonalDetails(pdEmpId);
        }
        dp.recordAuthorisationAttempt(user.getEmployeeId(), Action.Update.label, "Personal Details", false);
        return null;
    }

    /**
     * Updates the Personal Details record for a User.
     * @param user the user that performs the operation
     * @param details the payload that is to be sent to the Database. [employeeID, surname, name, date of birth, address, city, postcode, telephoneNumber, mobileNumber, emergency contact, emergency contact number]
     * @return whether or not the operation has been successful
     */
    public static boolean updatePersonalDetails(User user, String[] details)
    {
        if (details == null || details[0] == null)
        {
            dp.recordAuthorisationAttempt(user.getEmployeeId(), Action.Create.label, "Personal Details", true);
            return false;
        }

        String[] currentDetails = dp.fetchPersonalDetails(details[0]);

        for (int i = 0; i < details.length; i++)
        {
            if (details[i] == null)
            {
                details[i] = currentDetails[i];
            }
        }

        if (AuthorisationAttempt(Action.Update, "Personal Details", user, details))
        {
            dp.recordAuthorisationAttempt(user.getEmployeeId(), Action.Create.label, "Personal Details", true);
            return dp.updatePersonalDetails(details);
        }

        dp.recordAuthorisationAttempt(user.getEmployeeId(), Action.Create.label, "Personal Details", false);
        return false;
    }

    /**
     * Records that a User attempted to delete a Document
     * @param user the user attemping the operation
     * @return whether or not the operation has been successful
     */
    public static boolean deletePersonalDetails(User user)
    {
        AuthorisationAttempt(Action.Delete, "Personal Details", user,null );
        return false;
    }

    /**
     * Any action must first be authorised by this method
     * @param action Authorise.Action Enum (CRUD)
     * @param target The target document ("Personal Details" or "Performance Review")
     * @param user The User that is trying to perform this action
     * @param payload Any information that may be associated with this action
     * @return whether the action has been successful or not
     */
    private static boolean AuthorisationAttempt(Action action, String target, User user, String[] payload)
    {
        if (user.isLoggedIn())
        {
            switch(action.toString())
            {
                case("Create"):
                    if (target.equals("Personal Details"))
                    {
                        if (user.getDepartment().equals(Position.Department.HR))
                        {
                            return true;
                        }
                        System.out.println(user.getEmployeeId() + " was not of the right department");
                        return false;
                    }
                    // Stage 5
                    else if (target.equals("Performance Review"))
                    {

                    }
                    else
                    {
                        System.out.println("Internal error: The given target '" + target + "' has bot been recognised");
                        return false;
                    }
                    break;

                case("Read"):
                    if (target.equals("Personal Details"))
                    {
                        String associatedEmployee = payload[0];
                        Position.Department requiredDpt = Position.Department.HR;

                        if (user.getDepartment().equals(requiredDpt)  || associatedEmployee.equals(user.getEmployeeId()))
                        {
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    }
                    // Stage 5
                    else if (target.equals("Performance Review"))
                    {

                    }
                    else
                    {
                        System.out.println("Internal error: The given target '" + target + "' has bot been recognised");
                        return false;
                    }
                    break;

                case("Update"):
                    if (target.equals("Personal Details"))
                    {
                        Position.Department requiredDpt = Position.Department.HR;
                        String associatedEmployee = payload[0];

                        if (user.getDepartment().equals(requiredDpt) || associatedEmployee.equals(user.getEmployeeId()))
                        {
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    }
                    // Stage 5
                    else if (target.equals("Performance Review"))
                    {

                    }
                    else
                    {
                        System.out.println("Internal error: The given target '" + target + "' has bot been recognised");
                        return false;
                    }
                    break;

                case("Delete"):
                    if (target.equals("Personal Details"))
                    {
                        return false;
                    }
                    // Stage 5
                    else if (target.equals("Performance Review"))
                    {

                    }
                    else
                    {
                        System.out.println("Internal error: The given target '" + target + "' has bot been recognised");
                        return false;
                    }
                    break;

                default:
                    System.out.println("The given operation was not recognised");
                    break;
            }
        }
        System.out.println(user.getEmployeeId() + " was not logged in");
        return false;
    }
}
