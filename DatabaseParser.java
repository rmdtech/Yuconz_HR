import java.sql.*;


public class DatabaseParser
{
    Connection c = null;
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
    Boolean recordAuthorisationAttempt(String employeeId, Object deleteMe, Object deleteMe2, Object deleteMe3, Boolean Successful)
    {
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
