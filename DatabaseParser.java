import org.sqlite.SQLiteConfig;

import java.sql.*;

public class DatabaseParser
{

    Connection c = null;
    Statement stmt = null;
    ResultSet result = null;
    SQLiteConfig config = null;

    public DatabaseParser()
    {
        try
        {
            Class.forName("org.sqlite.JDBC");
            config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            c = DriverManager.getConnection("jdbc:sqlite:databases/yuconz.db", config.toProperties());
            c.createStatement().execute("PRAGMA foreign_keys = ON");

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }

    /**
     * Runs SQL code that modifies the database in any way including creating, updating and deleting records
     * @param sql the SQL String to be executed on the database
     */
    boolean sqlUpdate(String sql)
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
            return false;
        }
        return true;
    }

    /**
     * Runs SQL code that reads the database in any way and stores the result in the 'result' field variable
     * @param sql the SQL String to be executed on the database
     */
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

    void setupDatabase()
    {
        sqlUpdate("CREATE TABLE User (\n" +
                "\temployeeId string PRIMARY KEY,\n" +
                "\trole varchar NOT NULL,\n" +
                "\tdepartment varchar NOT NULL,\n" +
                "\thashedPassword varchar NOT NULL,\n" +
                "\tsalt string NOT NULL\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE AuthenticationLog (\n" +
                "\temployeeId string PRIMARY KEY,\n" +
                "\ttimestamp timestamp NOT NULL,\n" +
                "    FOREIGN KEY (employeeId) REFERENCES User (employeeId)\n" +
                "        ON UPDATE RESTRICT\n" +
                "        ON DELETE RESTRICT\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE AuthorisationLog (\n" +
                "\temployeeId string PRIMARY KEY,\n" +
                "\ttimestamp timestamp NOT NULL,\n" +
                "\tactionAttempted varchar NOT NULL,\n" +
                "\tactionTarget varchar NOT NULL,\n" +
                "\tactionSucceeded boolean NOT NULL,\n" +
                "    FOREIGN KEY (employeeId) REFERENCES User (employeeId)\n" +
                "        ON UPDATE RESTRICT\n" +
                "        ON DELETE RESTRICT\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE Session (\n" +
                "\tsessionId string PRIMARY KEY,\n" +
                "\temployeeId string NOT NULL,\n" +
                "\ttimestamp timestamp NOT NULL,\n" +
                "    FOREIGN KEY (employeeId) REFERENCES User (employeeId)\n" +
                "        ON UPDATE RESTRICT\n" +
                "        ON DELETE RESTRICT\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE PersonalDetails (\n" +
                "\temployeeId string PRIMARY KEY,\n" +
                "\tsurname varchar NOT NULL,\n" +
                "\tname varchar NOT NULL,\n" +
                "\tdateOfBirth date NOT NULL,\n" +
                "\taddress varchar NOT NULL,\n" +
                "\tcity varchar NOT NULL,\n" +
                "\tcounty varchar NOT NULL,\n" +
                "\tpostcode varchar NOT NULL,\n" +
                "\ttelephoneNumber varchar NOT NULL,\n" +
                "\tmobileNumber varchar NOT NULL,\n" +
                "\temergencyContact varchar NOT NULL,\n" +
                "\temergencyContactNumber varchar NOT NULL,\n" +
                "\tdocumentId string NOT NULL,\n" +
                "    FOREIGN KEY (employeeId) REFERENCES User (employeeId)\n" +
                "        ON UPDATE RESTRICT\n" +
                "        ON DELETE RESTRICT,\n" +
                "    FOREIGN KEY (documentId) REFERENCES Documents (documentId)\n" +
                "        ON UPDATE RESTRICT\n" +
                "        ON DELETE RESTRICT\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE Documents (\n" +
                "\tdocumentId string PRIMARY KEY,\n" +
                "\tcreationTimestamp datetime NOT NULL,\n" +
                "\tlastAccessed datetime NOT NULL\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE Permissions (\n" +
                "\tdocumentId string PRIMARY KEY,\n" +
                "\thr integer,\n" +
                "\tit integer,\n" +
                "\tsales integer,\n" +
                "\tadmin integer,\n" +
                "\tbi integer,\n" +
                "\tmc integer,\n" +
                "    FOREIGN KEY (documentId) REFERENCES Documents (documentId)\n" +
                "        ON UPDATE RESTRICT\n" +
                "        ON DELETE RESTRICT\n" +
                ");\n");
    }

    /**
     * Creates a new record in the User table for a new employee
     * @param employeeId the employeeId of the new user to be stored in the database
     * @param salt the salt that was used to hash the password
     * @param hashedPassword the password + the salt hashed using SHA512
     * @param department the department the new employee is registered to
     * @param role the role of the new employee
     * @return whether or not the operation has been successful
     */
    boolean newEmployee(String employeeId, String salt, String hashedPassword, String department, String role)
    {
        if (!checkEmployeeId(employeeId))
        {
            sqlUpdate("INSERT INTO User" +
                    "(employeeID, hashedPassword, salt, role, department)" +
                    String.format("VALUES ('%s', '%s', '%s', '%s', '%s');", employeeId, hashedPassword, salt, role, department)
            );
            return true;
        }
        else
        {
            return false;
        }
    }


    /**
     * Updates the hashed password and salt stored in the database for a given employee
     * @param employeeId the user's employeeId
     * @param newPassword the user's new password
     * @param salt the newly generated salt used when hashing the user's password
     * @return whether or not the operation has been successful
     */
    /*
    boolean updatePassword(String employeeId, String newPassword, String salt)
    {
        if (checkEmployeeId(employeeId) == true)
        {
            sqlUpdate("UPDATE User " +
                    String.format("SET hashedPassword = '%s', salt = '%s' " , newPassword, salt) +
                    String.format("WHERE employeeId = '%s';", employeeId)
            );
            return true;
        }
        else
        {
            return false;
        }
    }
*/
    /**
     * Checks to see if an employeeId exists in the User table
     * @param employeeId the employeeId to check
     * @return whether or not the employeeId was found
     */
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
            return false;
        }
    }

    /**
     * Fetches the salted and hashed password from the database for a given employeeId
     * @param employeeId the employeeId of the user who's password needs fetching
     * @return the user's password if the user exists
     */
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
            return null; // keep compiler happy
        }
    }

    /**
     * Fetches the password salt for a given employeeId
     * @param employeeId the employeeId of the user who's salt is being fetched
     * @return the salt if the user exists
     */
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
            return null; // keep compiler happy
        }
    }

    /**
     * Fetches the department of the user with  matching employeeId
     * @param employeeId the employeeId of the user who's department is being fetched
     * @return the department of the user if they exist
     */
    Position.Department fetchDepartment(String employeeId)
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
            // if (departmentEnum == null)
            // There is a typo in the database
            return Position.Department.valueOf(department);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null; // keep compiler happy
        }
    }

    /**
     * Fetches the role of the user with  matching employeeId
     * @param employeeId the employeeId of the user who's role is being fetched
     * @return the enum of the role of this user if they exist
     */
    Position.Role fetchRole(String employeeId)
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
            // if (roleEnum == null)
            // This means there is a typo in the Database
            return Position.Role.valueOf(role);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null; // keep compiler happy
        }
    }

    /**
     * creates a session for a newly logged in user
     * @param employeeId the employeeId of the user who's creating a session
     * @param sessionId the UUID generated for the session
     */
    void createSession(String employeeId, String sessionId)
    {
        sqlUpdate("INSERT INTO Session" +
                "(sessionId, employeeID, timestamp)" +
                String.format("VALUES ('%s', '%s', CURRENT_TIME);", sessionId, employeeId)
        );
    }

    /**
     * Deletes a session with the given sessionId from the database
     * @param sessionId the sessionId of the session to delete
     * @return whether or not the operation has been successful
     */
    boolean deleteSession(String sessionId)
    {
        /* To-Do:
           Please check if the session ID has not already been deleted and return accordingly with true/false
         */
        sqlUpdate("DELETE FROM Session " +
                String.format("WHERE sessionId = '%s';", sessionId)
        );
        return true;
    }

    /**
     * Records an authorisation attempt in the AuthorisationLog table
     * @param employeeId the employeeId of the user who's being authorised
     * @param actionAttempted the crud operation "create", "read", "update", "delete" that the user attempted on the target
     * @param actionTarget the target the user attempted to modify
     * @param successful whether or not the user had sufficient permissions to perform the attempted action
     */
    void recordAuthorisationAttempt(String employeeId, String actionAttempted, String actionTarget, Boolean successful)
    {
        sqlUpdate("INSERT INTO AuthorisationLog" +
                "(employeeID, timestamp, actionAttempted, actionTarget, actionSucceeded)" +
                String.format("VALUES ('%s', CURRENT_TIME, '%s', '%s', '%s');", employeeId, actionAttempted, actionTarget, successful)
        );
    }

    /**
     * Records a login in the AuthenticationLog table
     * @param employeeId the employeeId of the user who logged in
     */
    void recordAuthentication(String employeeId)
    {
        sqlUpdate("INSERT INTO AuthenticationLog" +
                "(employeeID, timestamp)" +
                String.format("VALUES ('%s', CURRENT_TIME);", employeeId)
        );
    }

    /**
     * Payload:
     * [0] employeeId
     * [1] surname
     * [2] name
     * [3] dateOfBirth
     * [4] address
     * [5] city
     * [6] county
     * [7] postcode
     * [8] telephoneNumber
     * [9] mobileNumber
     * [10] emergencyContact
     * [11] emergencyContactNumber
     * Full payload to be expected on Update
     **/

    /**
     * creates a new record in the PersonalDetails table
     * @param payload the payload of employee details
     * @param newDocumentId the newly generated UUID for documentId
     * @return true
     */
    boolean createPersonalDetailsRecord(String[] payload, String newDocumentId)
    {
        if(!sqlUpdate("INSERT INTO Documents " +
                "(documentId, creationTimestamp, lastAccessed) " +
                String.format("VALUES ('%s', CURRENT_TIME, CURRENT_TIME);", newDocumentId)
        ))
        {
            return false;
        }

        if(!sqlUpdate("INSERT INTO Permissions " +
                "(documentId, hr, it, sales, admin, bi, mc) " +
                String.format("VALUES ('%s', %s, %s, %s, %s, %s, %s);",
                        newDocumentId, 0, null, null, null, null, null
                ))
        )
        {
            return false;
        }

        if(!sqlUpdate("INSERT INTO PersonalDetails" +
                "(employeeId, " +
                "surname, " +
                "name, " +
                "dateOfBirth, " +
                "address, " +
                "city, " +
                "county, " +
                "postcode, " +
                "telephoneNumber, " +
                "mobileNumber, " +
                "emergencyContact, " +
                "emergencyContactNumber, " +
                "documentId)" +
                String.format("VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                        payload[0], payload[1], payload[2], payload[3], payload[4], payload[5],
                        payload[6], payload[7], payload[8], payload[9], payload[10], payload[11], newDocumentId
                )
        ))
        {
            return false;
        }
        return true;
    }

    /**
     * updates a PersonalDetails record for a given employee
     * @param payload the payload of employee details
     * @return true
     */
    boolean updatePersonalDetails(String[] payload)
    {
        sqlUpdate("UPDATE PersonalDetails " +
                String.format("SET surname = '%s', " +
                                "name= '%s', " +
                                "dateOfBirth = '%s', " +
                                "address = '%s', " +
                                "city = '%s', " +
                                "county = '%s', " +
                                "postcode = '%s', " +
                                "telephoneNumber = '%s', " +
                                "mobileNumber = '%s', " +
                                "emergencyContact = '%s', " +
                                "emergencyContactNumber = '%s' " +
                                "WHERE employeeId = '%s'",
                        payload[1], payload[2], payload[3], payload[4], payload[5], payload[6],
                        payload[7], payload[8], payload[9], payload[10], payload[11], payload[0]
                )
        );
        return true;
    }

    /**
     * fetches personal details permissions from the database for a given employee
     * @param employeeId the employee to fetch permissions about
     * @return perms array
     */
    String[] fetchPersonalDetailsPermissions(String employeeId)
    {

        String[] perms = new String[6];

        sqlRead("SELECT * FROM Permissions " +
                "WHERE Permissions.documentId = ( " +
                "SELECT PersonalDetails.documentId " +
                "FROM PersonalDetails " +
                String.format("WHERE PersonalDetails.employeeId = '%s')", employeeId)
        );
        try
        {
            result.next(); // only ever be one result, while loop not required
            perms[0] = result.getString("hr");
            perms[1] = result.getString("it");
            perms[2] = result.getString("sales");
            perms[3] = result.getString("admin");
            perms[4] = result.getString("bi");
            perms[5] = result.getString("mc");
            result.close();
            stmt.close();
            // if (roleEnum == null)
            // This means there is a typo in the Database
            return perms;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null; // keep compiler happy
        }
    }

    /**
     * fetches personal details from the database for a given employee
     * @param employeeId the employee to fetch details for
     * @return payload array
     */
    String[] fetchPersonalDetails(String employeeId)
    {

        String[] payload = new String[12];

        sqlRead("SELECT * FROM PersonalDetails " +
                String.format("WHERE employeeId = '%s'", employeeId)
        );
        try
        {
            result.next(); // only ever be one result, while loop not required
            payload[0] = result.getString("employeeId");
            payload[1] = result.getString("surname");
            payload[2] = result.getString("name");
            payload[3] = result.getString("dateOfBirth");
            payload[4] = result.getString("address");
            payload[5] = result.getString("city");
            payload[6] = result.getString("county");
            payload[7] = result.getString("postcode");
            payload[8] = result.getString("telephoneNumber");
            payload[9] = result.getString("mobileNumber");
            payload[10] = result.getString("emergencyContact");
            payload[11] = result.getString("emergencyContactNumber");
            result.close();
            stmt.close();
            // if (roleEnum == null)
            // This means there is a typo in the Database
            return payload;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null; // keep compiler happy
        }
    }

    Boolean isLoggedIn(String employeeId, String sessionId)
    {
        sqlRead("SELECT employeeId, sessionId FROM Session " +
                String.format("WHERE employeeId = '%s', sessionId = '%s'", employeeId, sessionId)
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
            return false;
        }
    }
}
