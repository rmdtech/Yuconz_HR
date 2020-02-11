import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

public class Authorise{
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    public static boolean AuthorisationAttempt(String action, Role required, User user){
        DatabaseParser dp = new DatabaseParser();
        LocalDateTime now = LocalDateTime.now();
        String logMessage = "[" + user.getEmployeeNumber() + " at " + dateFormat.format(now) + "] " + action;

        if (required.equals(user.getAccessLevel())){
            logMessage += " AUTHORISED";
            dp.recordAuthorisationAttempt(logMessage);
            return true;
        }
        else{
            logMessage += " DENIED.\nLevel required: " + required.toString() + user.getEmployeeNumber() + " is " + user.getAccessLevel().toString();
            dp.recordAuthorisationAttempt(logMessage);
            return false;
        }
    }
}