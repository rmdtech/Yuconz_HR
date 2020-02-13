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

    void sqlUpdate(String sql)
    {
        try
        {
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }

    ResultSet sqlRead(String sql)
    {
        try
        {
            stmt = c.createStatement();
            ResultSet result = stmt.executeQuery(sql);

            stmt.close();
            return result;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            System.exit(0);
            return null; //keeps the compiler happy
        }
    }

    void recordAuthorisationAttempt(String employeeId, String actionAttempted, String actionTarget, Boolean successful)
    {
        sqlUpdate("INSERT INTO AuthorisationLog" +
                "(employeeID, actionAttempted, actionTarget, actionSucceeded)" +
                String.format("VALUES ('%s', '%s', '%s', '%s');", employeeId, actionAttempted, actionTarget, successful)
        );
    }

    Boolean checkEmployeeId(String employeeId)
    {
        ResultSet result = sqlRead("SELECT * FROM User" +
                String.format("WHERE employeeId = '%s';", employeeId));
        try
        {
            while (result.next())
            {
               System.out.println(result.getString("employeeId"));
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
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

    String fetchSessionId(String employeeId)
    {
        return "changeMe";
    }

    void createSession(String employeeId, String sessionId)
    {

    }

    void deleteSession(String employeeId)
    {

    }

    void updatePassword(String employeeId, String newPassword, String salt)
    {

    }
}
