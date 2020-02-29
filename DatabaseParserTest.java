import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseParserTest {
    DatabaseParser dp = new DatabaseParser();

    String[] createDemoEmployee()
    {
        dp.newEmployee("abc123",
                "5583f71e39554054b1aa1ce16fd520f2",
                "8c78ab1466456a50c569849e39ec9909eaf3fce9a7ee91660da9d4da4a18fe10f93842473b05d38a828c2e0e130b7fed1dc4cef83459a8bda95162c87abf19ce",
                "HR",
                "0");
        String[] payload = {"abc123","Smith","John","1992-03-28", "14 York Road", "Canterbury", "Kent", "CT1 3TA",
                "01634289431", "07499274509", "Jane Smith", "07286308174"};
        dp.createPersonalDetailsRecord(payload, "33d8bd019d4b4662bdbc8efd6048d1d9");
        return payload;
    }


    @org.junit.jupiter.api.Test
    void createValidPersonalDetailsRecord() {
        dp.newEmployee("abc123",
                "5583f71e39554054b1aa1ce16fd520f2",
                "8c78ab1466456a50c569849e39ec9909eaf3fce9a7ee91660da9d4da4a18fe10f93842473b05d38a828c2e0e130b7fed1dc4cef83459a8bda95162c87abf19ce",
                "HR",
                "0");
        String[] payload = {"abc123","Smith","John","1992-03-28", "14 York Road", "Canterbury", "Kent", "CT1 3TA",
                "01634289431", "07499274509", "Jane Smith", "07286308174"};
        assertTrue(
                dp.createPersonalDetailsRecord(payload, "33d8bd019d4b4662bdbc8efd6048d1d9")
        );

        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");
    }

    @org.junit.jupiter.api.Test
    void createDuplicatePersonalDetailsRecord()
    {
        String[] payload = createDemoEmployee();

        assertFalse(
                dp.createPersonalDetailsRecord(payload, "33d8bd019d4b4662bdbc8efd6048d1d9")
        );

        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");
    }

    @org.junit.jupiter.api.Test
    void updatePersonalDetails()
    {
        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");

        dp.newEmployee("abc123",
                "5583f71e39554054b1aa1ce16fd520f2",
                "8c78ab1466456a50c569849e39ec9909eaf3fce9a7ee91660da9d4da4a18fe10f93842473b05d38a828c2e0e130b7fed1dc4cef83459a8bda95162c87abf19ce",
                "HR",
                "0");
        String[] payload = {"abc123","Smith","John","1992-03-28", "14 York Road", "Canterbury", "Kent", "CT1 3TA",
                "01634289431", "07499274509", "Jane Smith", "07286308174"};
        dp.createPersonalDetailsRecord(payload, "33d8bd019d4b4662bdbc8efd6048d1d9");
        String[] newPayload = {"abc123","Smith","John","1992-03-28", "14 York Road", "Canterbury", "Kent", "CT1 3TA",
                "01622761848", "07499274509", "Jane Smith", "07286308174"};

        dp.updatePersonalDetails(newPayload);

        dp.sqlRead("SELECT telephoneNumber FROM PersonalDetails WHERE employeeId = 'abc123'");

        String res = null;
        try {
            dp.result.next();
            res = dp.result.getString("telephoneNumber");

        }
        catch (SQLException e) {}


        assertEquals(res, "01622761848");

        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");
    }

    @org.junit.jupiter.api.Test
    void updateTwoPersonalDetailsRecords()
    {
        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");

        dp.newEmployee("abc123",
                "5583f71e39554054b1aa1ce16fd520f2",
                "8c78ab1466456a50c569849e39ec9909eaf3fce9a7ee91660da9d4da4a18fe10f93842473b05d38a828c2e0e130b7fed1dc4cef83459a8bda95162c87abf19ce",
                "HR",
                "0");
        String[] payload = {"abc123","Smith","John","1992-03-28", "14 York Road", "Canterbury", "Kent", "CT1 3TA",
                "01634289431", "07499274509", "Jane Smith", "07286308174"};
        dp.createPersonalDetailsRecord(payload, "33d8bd019d4b4662bdbc8efd6048d1d9");
        String[] newPayload = {"abc123","Smith","John","1992-03-28", "14 York Road", "Canterbury", "Kent", "CT1 3TA",
                "01622761848", "07499274509", "Roger Bosch", "07117698761"};

        dp.updatePersonalDetails(newPayload);

        dp.sqlRead("SELECT emergencyContact, emergencyContactNumber FROM PersonalDetails WHERE employeeId = 'abc123'");

        String res1 = null;
        String res2 = null;
        try {
            dp.result.next();
            res1 = dp.result.getString("emergencyContact");
            res2 = dp.result.getString("emergencyContactNumber");

        }
        catch (SQLException e) {}


        assertEquals(res1, "Roger Bosch");
        assertEquals(res2, "07117698761");

        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
        dp.sqlUpdate("DELETE FROM User");
    }

    @org.junit.jupiter.api.Test
    void fetchPersonalDetailsPermissions() {
    }

    @org.junit.jupiter.api.Test
    void fetchPersonalDetails() {
    }
}