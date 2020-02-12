import java.sql.*;


public class DatabaseParser
{

    Connection c = null;
    Statement stmt = null;

    public DatabaseParser()
    {
        try
        {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:databases/yuconz.db");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }
    Boolean recordAuthorisationAttempt(String employeeId, Object deleteMe, String actionAttempted, String actionTarget, Boolean successful)
    {
        try
        {
            stmt = c.createStatement();

            String sql = "INSERT INTO AuthorisationLog" +
                    "(employeeID, actionAttempted, actionTarget, actionSucceeded)" +
                    String.format("VALUES ('%s', '%s', '%s', '%s')", employeeId, actionAttempted, actionTarget, successful);

            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return true;
    }

    Boolean checkEmployeeID(String employeeId)
    {
        return true;
    }

    Boolean getEmployeePassword(String employeeId)
    {
        return true;
    }

    Boolean getPasswordSalt(String employeeId)
    {
        return true;
    }

    String fetchDepartment(String employeeId)
    {
        return "ChangeMe";
    }

    String fetchRole(String employeeId)
    {
        return "ChangeMe";
    }
}
