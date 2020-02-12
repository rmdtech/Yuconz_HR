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

    void runSql(String sql)
    {
        try
        {
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    Boolean recordAuthorisationAttempt(String employeeId, Object deleteMe, String actionAttempted, String actionTarget, Boolean successful)
    {
        runSql("INSERT INTO AuthorisationLog" +
                "(employeeID, actionAttempted, actionTarget, actionSucceeded)" +
                String.format("VALUES ('%s', '%s', '%s', '%s')", employeeId, actionAttempted, actionTarget, successful)
        );
        return true;
    }

    Boolean checkEmployeeId(String employeeId)
    {
        return true;
    }

    String fetchEmployeePassword(String employeeId)
    {
        return "ChangeMe";
    }

    String fetchPasswordSalt(String employeeId)
    {
        return "changeMe";
    }

    String fetchDepartment(String employeeId)
    {
        return "ChangeMe";
    }

    String fetchRole(String employeeId)
    {
        return "ChangeMe";
    }

    void newEmployee(String id, String salt, String hashedPassword, String department, String role)
    {

    }
}
