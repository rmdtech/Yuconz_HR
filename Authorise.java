/**
 * Constructor for class Authorise
 */
public class Authorise
{
    public enum Action{
        Create("Create"),
        Read("Read"),
        Update("Update"),
        Delete("Delete");
        public final String label;
        Action (String label)
        {
            this.label = label;
        }
        public String toString()
        {
            return label;
        }
    }

    /**
     * This calls the similarly named function in the DatabaseParser to write any details into the database after creating a new record
     * @param details Personal details, which may be null at this point
     * @return whether the action has been successful or not
     */
    private static boolean createPersonalDetailsRecord(String[] details)
    {
        DatabaseParser dp = new DatabaseParser();
        if (details[0] == null)
        {
            return false;
        }
        return dp.createPersonalDetailsRecord(details, User.generateSalt());
    }

    /**
     * This calls the similarly named function in the DatabaseParser to update any details in the associated record for an Employee
     * @param details The updated details. Only those that need to be updated have to be set
     * @return whether the operation has been successful or not
     */
    private static boolean updatePersonalDetails(String[] details)
    {
        DatabaseParser dp = new DatabaseParser();
        String[] currentDetails = dp.fetchPersonalDetails(details[0]);
        for (int i = 0; i < details.length; i++)
        {
            if (details[i] == null)
            {
                details[i] = currentDetails[i];
            }
        }
        return dp.updatePersonalDetails(details);
    }

    /**
     * Any action must first be authorised by this method
     * @param action Authorise.Action Enum (CRUD)
     * @param target The target document ("Personal Details" or "Performance Review")
     * @param user The User that is trying to perform this action
     * @param payload Any information that may be associated with this action
     * @return whether the action has been successful or not
     */
    public static boolean AuthorisationAttempt(Action action, String target, User user, String[] payload) {
        DatabaseParser dp = new DatabaseParser();
        if (user.isLoggedIn()) {
            String[] response;
            switch(action.toString())
            {
                case("Create"):
                    if (target.equals("Personal Details"))
                    {
                        if (user.getDepartment().equals(Position.Department.HR))
                        {
                            if (createPersonalDetailsRecord(payload))
                            {
                                dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), target, true);
                                return true;
                            }
                            return false;
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
                        response = dp.fetchPersonalDetailsPermissions(user.getEmployeeId());
                        Position.Department requiredDpt = Position.Department.valueOf(response[0]);
                        int minimumLevel = Integer.getInteger(response[1]);
                        String associatedEmployee = response[2];
                        if ((user.getDepartment().equals(requiredDpt) && user.getRole().getLevel() >= minimumLevel) || associatedEmployee.equals(user.getEmployeeId()))
                        {
                            dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), target, true);
                            return true;
                        }
                        else
                        {
                            dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), target, false);
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
                        response = dp.fetchPersonalDetailsPermissions(user.getEmployeeId());
                        Position.Department requiredDpt = Position.Department.valueOf(response[0]);
                        int minimumLevel = Integer.getInteger(response[1]);
                        String associatedEmployee = response[2];
                        if ((user.getDepartment().equals(requiredDpt) && user.getRole().getLevel() >= minimumLevel) || associatedEmployee.equals(user.getEmployeeId()))
                        {
                            dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), target, true);
                            updatePersonalDetails(payload);
                            return true;
                        }
                        else
                        {
                            dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), target, false);
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
                        dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), target, false);
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
                    break;
            }
        }
        System.out.println(user.getEmployeeId() + " was not logged in");
        return false;
    }
}
