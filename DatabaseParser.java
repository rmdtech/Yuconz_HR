import java.sql.*;


public class DatabaseParser
{

    Connection c = null;
    Statement stmt = null;
    ResultSet result = null;

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

    void sqlRead(String sql)
    {
        try
        {
            stmt = c.createStatement();
            result = stmt.executeQuery(sql);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            System.exit(0);
        }

        catch(Exception e)
        {
            System.out.println("Error Occurred");
            e.printStackTrace();
            System.exit(0);
        }
    }

    void recordAuthorisationAttempt(String employeeId, String actionAttempted, String actionTarget, Boolean successful)
    {
        sqlUpdate("INSERT INTO AuthorisationLog" +
                "(employeeID, actionAttempted, actionTarget, actionSucceeded)" +
                String.format("VALUES ('%s', CURRENT_TIME, '%s', '%s', '%s');", employeeId, actionAttempted, actionTarget, successful)
        );
    }

    Boolean checkEmployeeId(String employeeId)
    {
        sqlRead("SELECT employeeId FROM User " +
                String.format("WHERE employeeId = '%s'", employeeId)
        );

        try
        {
            Boolean isPresent = result.isBeforeFirst();
            result.close();
            stmt.close();
            return isPresent;
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
        sqlRead("SELECT hashedPassword FROM User " +
                String.format("WHERE employeeId = '%s'", employeeId)
        );
        try
        {
            result.next(); // only ever be one result, while loop not required
            String hashedPassword = result.getString("hashedPassword");
            result.close();
            stmt.close();
            return hashedPassword;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            System.exit(0);
            return null; // keep compiler happy
        }
    }

    String fetchPasswordSalt(String employeeId)
    {
        sqlRead("SELECT salt FROM User " +
                String.format("WHERE employeeId = '%s'", employeeId)
        );
        try
        {
            result.next(); // only ever be one result, while loop not required
            String salt = result.getString("salt");
            result.close();
            stmt.close();
            return salt;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            System.exit(0);
            return null; // keep compiler happy
        }
    }

    String fetchDepartment(String employeeId)
    {
        sqlRead("SELECT department FROM User " +
                String.format("WHERE employeeId = '%s'", employeeId)
        );
        try
        {
            result.next(); // only ever be one result, while loop not required
            String department = result.getString("department");
            result.close();
            stmt.close();
            return department;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            System.exit(0);
            return null; // keep compiler happy
        }
    }

    String fetchRole(String employeeId)
    {
        sqlRead("SELECT role FROM User " +
                String.format("WHERE employeeId = '%s'", employeeId)
        );
        try
        {
            result.next(); // only ever be one result, while loop not required
            String role = result.getString("role");
            result.close();
            stmt.close();
            return role;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            System.exit(0);
            return null; // keep compiler happy
        }
    }

    void newEmployee(String employeeId, String salt, String hashedPassword, String department, String role)
    {
        sqlUpdate("INSERT INTO User" +
                "(employeeID, hashedPassword, salt, role, department)" +
                String.format("VALUES ('%s', '%s', '%s', '%s', '%s');", employeeId, hashedPassword, salt, role, department)
        );
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
