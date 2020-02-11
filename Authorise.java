import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

public class Authorise{
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    public static boolean AuthorisationAttempt(String action, Position.Department requiredDpt, Position.Role requiredRole, User user){
        DatabaseParser dp = new DatabaseParser();
        LocalDateTime now = LocalDateTime.now();
        String logMessage = "[" + user.getEmployeeID() + " at " + dateFormat.format(now) + "] " + action;

        if (requiredDpt.equals(user.getDepartment()) && requiredRole.equals(user.getRole())){
            logMessage += " AUTHORISED";
            dp.recordAuthorisationAttempt(logMessage);
            return true;
        }
        else{
            logMessage += " DENIED.\nLevel required: " + requiredDpt.toString() + " " + requiredRole.toString() + "\n " + user.getEmployeeID() + " is " + user.getDepartment().toString() + " " + user.getRole().toString();
            dp.recordAuthorisationAttempt(logMessage);
            return false;
        }
    }
}