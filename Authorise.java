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

    private static void createPersonalDetailsRecord(String[] details)
    {
        DatabaseParser dp = new DatabaseParser();
        dp.createPersonalDetailsRecord(details);
        // do validation on payload
    }

    private static void updatePersonalDetails(String[] details)
    {
        DatabaseParser dp = new DatabaseParser();
        dp.createPersonalDetailsRecord(details);
        // do validation on payload
    }

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
                            dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), target, true);
                            createPersonalDetailsRecord(payload);
                            return true;
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
                case("Read"):
                    if (target.equals("Personal Details"))
                    {
                        response = dp.getPersonalDetailsPermissions(user.getEmployeeId());
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
                        response = dp.getPersonalDetailsPermissions(user.getEmployeeId());
                        Position.Department requiredDpt = Position.Department.valueOf(response[0]);
                        int minimumLevel = Integer.getInteger(response[1]);
                        if (user.getDepartment().equals(requiredDpt) && user.getRole().getLevel() >= minimumLevel)
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