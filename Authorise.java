import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

/**
 * Constructor for class Authorise
 */
public class Authorise{
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    /**
     * Method to print authorisation attempts into the database
     * @param action String of the action that was tried to perform
     * @param requiredDpt Position.Department enum required to perform this action
     * @param requiredRole Position.Role enum minimum level required to perform this action
     * @param user User object of the currently logged in user attempting this action
     * @return Whether or not the attempted action was successful
     */
    public static boolean AuthorisationAttempt(String action, String target, Position.Department requiredDpt, Position.Role requiredRole, User user)
    {
        DatabaseParser dp = new DatabaseParser();
        LocalDateTime now = LocalDateTime.now();

        if (requiredDpt.equals(user.getDepartment()) && requiredRole.getLevel() < user.getRole().getLevel())
        {
            dp.recordAuthorisationAttempt(user.getEmployeeID(), dateFormat.format(now), action, target, true);
            return true;
        }
        else
        {
            dp.recordAuthorisationAttempt(user.getEmployeeID(), dateFormat.format(now), action, target, false);
            return false;
        }
    }
}