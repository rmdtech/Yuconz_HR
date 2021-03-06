import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.ArrayList;

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

    /**
     * creates all the tables in the database required
     */
    void setupDatabase()
    {
        sqlUpdate("CREATE TABLE User (\n" +
                "    employeeId string PRIMARY KEY,\n" +
                "    role varchar NOT NULL,\n" +
                "    department varchar,\n" +
                "    hashedPassword varchar NOT NULL,\n" +
                "    salt string NOT NULL,\n" +
                "    directSupervisor string,\n" +
                "    FOREIGN KEY (directSupervisor) REFERENCES User(employeeId),\n" +
                "    CHECK (length(employeeId) = 6)\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE AuthenticationLog (\n" +
                "    employeeId string,\n" +
                "    timestamp timestamp,\n" +
                "    PRIMARY KEY (employeeId, timestamp),\n" +
                "    FOREIGN KEY (employeeId) REFERENCES User(employeeId)\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE AuthorisationLog (\n" +
                "    employeeId string,\n" +
                "    timestamp timestamp,\n" +
                "    actionAttempted varchar NOT NULL,\n" +
                "    actionTarget varchar NOT NULL,\n" +
                "    actionSucceeded boolean NOT NULL,\n" +
                "    PRIMARY KEY(employeeId, timestamp),\n" +
                "    FOREIGN KEY (employeeId) REFERENCES User(employeeId)\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE Session (\n" +
                "    sessionId string PRIMARY KEY,\n" +
                "    employeeId string NOT NULL,\n" +
                "    timestamp timestamp NOT NULL,\n" +
                "    FOREIGN KEY (employeeId) REFERENCES User(employeeId),\n" +
                "    CHECK (length(sessionId) = 32)\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE PersonalDetails (\n" +
                "    employeeId string PRIMARY KEY,\n" +
                "    surname varchar NOT NULL,\n" +
                "    name varchar NOT NULL,\n" +
                "    dateOfBirth date NOT NULL,\n" +
                "    address varchar NOT NULL,\n" +
                "    city varchar NOT NULL,\n" +
                "    county varchar NOT NULL,\n" +
                "    postcode varchar NOT NULL,\n" +
                "    telephoneNumber varchar NOT NULL,\n" +
                "    mobileNumber varchar NOT NULL,\n" +
                "    emergencyContact varchar NOT NULL,\n" +
                "    emergencyContactNumber varchar NOT NULL,\n" +
                "    documentId string NOT NULL UNIQUE,\n" +
                "    FOREIGN KEY (employeeId) REFERENCES User(employeeId),\n" +
                "    FOREIGN KEY (documentId) REFERENCES Documents(documentId)\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE Documents (\n" +
                "    documentId string PRIMARY KEY,\n" +
                "    creationTimestamp datetime NOT NULL,\n" +
                "    CHECK (length(documentId) = 32)\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE FuturePerformance (\n" +
                "    documentId string,\n" +
                "    num integer,\n" +
                "    objective text NOT NULL,\n" +
                "    PRIMARY KEY (documentId, num),\n" +
                "    FOREIGN KEY (documentId) REFERENCES Documents(documentId)\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE PastPerformance (\n" +
                "    documentId string,\n" +
                "    num integer,\n" +
                "    objective text NOT NULL,\n" +
                "    achievement text NOT NULL,\n" +
                "    PRIMARY KEY (documentId, num),\n" +
                "    FOREIGN KEY (documentId) REFERENCES Documents(documentId)\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE Review (\n" +
                "    revieweeId string,\n" +
                "    dueBy date,\n" +
                "    documentId string NOT NULL UNIQUE,\n" +
                "    firstReviewerId string NOT NULL,\n" +
                "    secondReviewerId string NOT NULL,\n" +
                "    revieweeSigned date,\n" +
                "    firstReviewerSigned date,\n" +
                "    secondReviewerSigned date,\n" +
                "    meetingDate date,\n" +
                "    performanceSummary text,\n" +
                "    reviewerComments text,\n" +
                "    recommendation string,\n" +
                "    PRIMARY KEY (revieweeId, dueBy),\n" +
                "    FOREIGN KEY (revieweeId) REFERENCES User(employeeId),\n" +
                "    FOREIGN KEY (firstReviewerId) REFERENCES User(employeeId),\n" +
                "    FOREIGN KEY (secondReviewerId) REFERENCES User(employeeId),\n" +
                "    FOREIGN KEY (documentId) REFERENCES Documents(documentId)\n" +
                ");\n");
    }


    String[] filterNulls(String[] payload)
    {
        int x = 0;
        String[] newPayload = new String[payload.length];
        for (String param : payload)
        {
            if(param == null)
            {
                newPayload[x] = "NULL";
            }
            else
            {
                newPayload[x] = "'" + param + "'";
            }
            x++;
        }
        return newPayload;
    }

    String[] filterTruesToCurrentDate(String[] payload)
    {
        int x = 0;
        String[] newPayload = new String[payload.length];
        for (String param : payload)
        {
            if(param.equals("'true'"))
            {
                newPayload[x] = "CURRENT_DATE";
            }
            else if(param.equals("'false'"))
            {
                newPayload[x] = "NULL";
            }
            else
            {
                newPayload[x] = param;
            }
            x++;
        }
        return newPayload;
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
    boolean newEmployee(String employeeId, String salt, String hashedPassword, String directSupervisor, String department, String role)
    {
        if (!checkEmployeeId(employeeId))
        {
            String[] payload = filterNulls(new String[]{employeeId, hashedPassword, salt, role, department, directSupervisor});
            sqlUpdate("INSERT INTO User" +
                    "(employeeID, hashedPassword, salt, role, department, directSupervisor)" +
                    String.format("VALUES (%s, %s, %s, %s, %s, %s);", payload[0], payload[1], payload[2], payload[3], payload[4], payload[5])
            );
            return true;
        }
        else
        {
            return false;
        }
    }

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
            if(result.next()) // only ever be one result, while loop not required
            {
                String role = result.getString("role");
                result.close();
                stmt.close();
                // if (roleEnum == null)
                // This means there is a typo in the Database
                return Position.Role.valueOf(role);
            }
            else
            {
                result.close();
                stmt.close();
                return null;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null; // keep compiler happy
        }
    }

    /**
     * fetches the directSupervisor of a given user from the database
     * @param employeeId the employeeId to get the supervisor of
     * @return the supervisor requested
     */
    String fetchDirectSupervisor(String employeeId)
    {
        sqlRead("SELECT directSupervisor FROM User " +
                String.format("WHERE employeeId = '%s'", employeeId)
        );
        try
        {
            result.next(); // only ever be one result, while loop not required
            String supervisor = result.getString("directSupervisor");
            result.close();
            stmt.close();
            // if (roleEnum == null)
            // This means there is a typo in the Database
            return supervisor;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null; // keep compiler happy
        }
    }

    /**
     * returns an ArrayList of all employeeIds in the database
     * @return payload of employeeIds
     */
    ArrayList<String> fetchAllUsers()
    {
        ArrayList<String> payload = new ArrayList<>();
        sqlRead("SELECT employeeId FROM User");

        try
        {
            while(result.next())
            {
                payload.add(result.getString("employeeId"));
            }
            return payload;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
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

    /*
      Payload:
      [0] employeeId
      [1] surname
      [2] name
      [3] dateOfBirth
      [4] address
      [5] city
      [6] county
      [7] postcode
      [8] telephoneNumber
      [9] mobileNumber
      [10] emergencyContact
      [11] emergencyContactNumber
      Full payload to be expected on Update
     */

    /**
     * creates a new record in the PersonalDetails table
     * @param payload the payload of employee details
     * @param newDocumentId the newly generated UUID for documentId
     * @return true
     */
    boolean createPersonalDetailsRecord(String[] payload, String newDocumentId)
    {
        if(!sqlUpdate("INSERT INTO Documents " +
                "(documentId, creationTimestamp) " +
                String.format("VALUES ('%s', CURRENT_TIME);", newDocumentId)
        ))
        {
            return false;
        }

        return sqlUpdate("INSERT INTO PersonalDetails" +
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
        );
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
            if(result.next()) // only ever be one result, while loop not required
            {
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
            }
            else
            {
                result.close();
                stmt.close();
                return null;
            }
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

    /**
     * Checks if a user is logged in.
     * @param employeeId the employeeId to check
     * @param sessionId the sessionId to check
     * @return user's logged in state (true/false)
     */
    Boolean isLoggedIn(String employeeId, String sessionId)
    {
        sqlRead("SELECT employeeId, sessionId FROM Session " +
                String.format("WHERE employeeId = '%s' AND sessionId = '%s'", employeeId, sessionId)
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
     * creates a new, blank review document in the database
     * @param payload the payload of data to insert
     * @return successful (true/false)
     */
    Boolean createReview(String[] payload)
    {
        if(!sqlUpdate("INSERT INTO Documents " +
                "(documentId, creationTimestamp) " +
                String.format("VALUES ('%s', CURRENT_TIME)", payload[2]))
        )
        {
            return false;
        }

        return sqlUpdate("INSERT INTO Review " +
                "(revieweeId, dueBy, documentId, firstReviewerId, secondReviewerId)" +
                String.format("VALUES ('%s', '%s', '%s', '%s', '%s')", payload[0], payload[1], payload[2], payload[3], payload[4]));
    }

    /**
     * fetches the documentId of a given review
     * @param revieweeId the reviewee of the review
     * @param dueBy the due date the review should be completed by
     * @return the requested documentId
     */
    String fetchReviewDocumentId(String revieweeId, String dueBy)
    {
        sqlRead("SELECT documentId FROM Review " +
                String.format("WHERE revieweeId = '%s' AND dueBy = '%s'", revieweeId, dueBy)
        );
        try
        {
            result.next(); // only ever be one result, while loop not required
            String documentId = result.getString("documentId");
            result.close();
            stmt.close();
            return documentId;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null; // keep compiler happy
        }
    }

    /**
     * checks if a given employee is  a reviewer on a given review
     * @param documentId the review document to check
     * @param employeeId the employeeId to check for the given review
     * @return whether the employee is a reviewer (true/false)
     */
    boolean isReviewer(String documentId, String employeeId)
    {
        sqlRead("SELECT documentId FROM Review " +
                String.format("WHERE documentId = '%s' " +
                        "AND (firstReviewerId = '%s' OR secondReviewerId = '%s')", documentId, employeeId, employeeId)
        );

        try
        {
            boolean isPresent = result.isBeforeFirst();
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
     * checks if a given employee is  a reviewee on a given review
     * @param documentId the review document to check
     * @param employeeId the employeeId to check for the given review
     * @return whether the employee is a reviewee (true/false)
     */
    boolean isReviewee(String documentId, String employeeId)
    {
        sqlRead("SELECT documentId FROM Review " +
                String.format("WHERE documentId = '%s' " +
                        "AND revieweeId = '%s'", documentId, employeeId)
        );

        try
        {
            boolean isPresent = result.isBeforeFirst();
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
     * fetches the content of an entire review
     * @param documentId the documentId of the review to fetch
     * @return payload containing the data of the review
     */
    String[] fetchReview(String documentId)
    {
        String[] payload = new String[12];

        sqlRead("SELECT * FROM Review " +
                String.format("WHERE documentId = '%s'", documentId)
        );
        try
        {
            if(result.next()) // only ever be one result, while loop not required
            {
                payload[0] = result.getString("revieweeId");
                payload[1] = result.getString("dueBy");
                payload[2] = result.getString("documentID");
                payload[3] = result.getString("firstReviewerId");
                payload[4] = result.getString("secondReviewerId");
                payload[5] = result.getString("revieweeSigned");
                payload[6] = result.getString("firstReviewerSigned");
                payload[7] = result.getString("secondReviewerSigned");
                payload[8] = result.getString("meetingDate");
                payload[9] = result.getString("performanceSummary");
                payload[10] = result.getString("reviewerComments");
                payload[11] = result.getString("recommendation");
            }
            else
            {
                result.close();
                stmt.close();
                return null;
            }
            result.close();
            stmt.close();
            return payload;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null; // keep compiler happy
        }
    }

    /**
     * fetches the associated pastPerformance records for a given review
     * @param documentId the documentId of the review to fetch
     * @return pastPerformance records for the given review
     */
    ArrayList<String[]> fetchPastPerformance(String documentId)
    {
        ArrayList<String[]> payload = new ArrayList<>();

        sqlRead("SELECT * FROM PastPerformance " +
                String.format("WHERE documentId = '%s' ", documentId) +
                "ORDER BY num"
        );

        try
        {
            while(result.next())
            {
                String[] pair = new String[2];

                pair[0] = result.getString("objective");
                pair[1] = result.getString("achievement");
                payload.add(pair);
            }
            result.close();
            stmt.close();
            return payload;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * fetches the associated futurePerformance records for a given review
     * @param documentId the documentId of the review to fetch
     * @return futurePerformance records for the given review
     */
    ArrayList<String> fetchFuturePerformance(String documentId)
    {
        ArrayList<String> payload = new ArrayList<>();

        sqlRead("SELECT * FROM FuturePerformance " +
                String.format("WHERE documentId = '%s' ", documentId) +
                "ORDER BY num"
        );

        try
        {
            while(result.next())
            {
                payload.add(result.getString("objective"));
            }
            result.close();
            stmt.close();
            return payload;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /*
    0: revieweeId[String]
    1: dueBy [Date]
    2: documentId [String]
    3: firstReviewerId [String, empId]
    4: secondReviewerId [String, empId]
    5: revieweeSigned [Boolean]
    6: firstReviewerSigned [Boolean]
    7: secondReviewerSigned	[Boolean]
    8: meetingDate [Date]
    9: performanceSummary [String]
    10: reviewerComments [String]
    11: recommendations [String]
    */

    /**
     * updates a review using a given payload
     * @param documentId the documentId of the review to update
     * @param payload the payload containing the data to insert
     * @param updatedPastPerformance the payload containing the new pastPerformance records to insert
     * @param updatedFuturePerformance the payload containing the new futurePerformance records to insert
     * @return successful (true/false)
     */
    boolean updateReview(String documentId, String[] payload, ArrayList<String[]> updatedPastPerformance, ArrayList<String> updatedFuturePerformance)
    {
        payload = filterNulls(payload);
        payload = filterTruesToCurrentDate(payload);
        if(!sqlUpdate("UPDATE Review " +
                String.format("SET firstReviewerId = %s, " +
                                "secondReviewerId= %s, " +
                                "revieweeSigned = %s, " +
                                "firstReviewerSigned = %s, " +
                                "secondReviewerSigned = %s, " +
                                "meetingDate = %s, " +
                                "performanceSummary = %s, " +
                                "reviewerComments = %s, " +
                                "recommendation = %s " +
                                "WHERE documentId = '%s'",
                        payload[3], payload[4], payload[5], payload[6], payload[7], payload[8],
                        payload[9], payload[10], payload[11], documentId
                )
        ))
        {
            return false;
        }

        if(!sqlUpdate("DELETE FROM PastPerformance " +
                String.format("WHERE documentId = '%s'", documentId))
        ) // records must be deleted before being updated as row may be removed during editing
        {
            return false;
        }

        int i = 0;
        for(String[] record : updatedPastPerformance)
        {
            if(!sqlUpdate("INSERT INTO PastPerformance " +
                    "(documentId, num, objective, achievement) " +
                    String.format("VALUES ('%s', %d, '%s', '%s')", documentId, i, record[0], record[1])))
            {
                return false;
            }
            i++;
        }

        if(!sqlUpdate("DELETE FROM FuturePerformance " +
                String.format("WHERE documentId = '%s'", documentId))
        ) // records must be deleted before being updated as row may be removed during editing
        {
            return false;
        }

        i = 0;
        for(String record : updatedFuturePerformance)
        {
            if(!sqlUpdate("INSERT INTO FuturePerformance " +
                    "(documentId, num, objective) " +
                    String.format("VALUES ('%s', %d, '%s')", documentId, i, record)))
            {
                return false;
            }
            i++;
        }

        return true;
    }

    /**
     * fetches all the pairs of employeeIds and dueBys for reviews in the database
     * @return payload of pairs (String Arrays) of employeeIds and dueBys
     */
    ArrayList<String[]> fetchAllReviewKeys()
    {
        ArrayList<String[]> payload = new ArrayList<>();
        sqlRead("SELECT revieweeId, dueBy FROM Review ORDER BY dueBy");

        try
        {
            while(result.next())
            {
                String[] pair = new String[2];

                pair[0] = result.getString("revieweeId");
                pair[1] = result.getString("dueBy");
                payload.add(pair);
            }
            return payload;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }

    }
}
