import static org.junit.jupiter.api.Assertions.*;

class DatabaseParserTest {
    DatabaseParser dp = new DatabaseParser();


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
        dp.sqlUpdate("DELETE FROM User");
        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
    }

    @org.junit.jupiter.api.Test
    void createDuplicatePersonalDetailsRecord()
    {
        dp.newEmployee("abc123",
                "5583f71e39554054b1aa1ce16fd520f2",
                "8c78ab1466456a50c569849e39ec9909eaf3fce9a7ee91660da9d4da4a18fe10f93842473b05d38a828c2e0e130b7fed1dc4cef83459a8bda95162c87abf19ce",
                "HR",
                "0");
        String[] payload = {"abc123","Smith","John","1992-03-28", "14 York Road", "Canterbury", "Kent", "CT1 3TA",
                "01634289431", "07499274509", "Jane Smith", "07286308174"};
        dp.createPersonalDetailsRecord(payload, "33d8bd019d4b4662bdbc8efd6048d1d9");
        dp.newEmployee("abc123",
                "5583f71e39554054b1aa1ce16fd520f2",
                "8c78ab1466456a50c569849e39ec9909eaf3fce9a7ee91660da9d4da4a18fe10f93842473b05d38a828c2e0e130b7fed1dc4cef83459a8bda95162c87abf19ce",
                "HR",
                "0");
        assertFalse(
                dp.createPersonalDetailsRecord(payload, "33d8bd019d4b4662bdbc8efd6048d1d9")
        );
        dp.sqlUpdate("DELETE FROM User");
        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
    }

    @org.junit.jupiter.api.Test
    void createIncompletePersonalDetails()
    {
        dp.newEmployee("abc123",
                "5583f71e39554054b1aa1ce16fd520f2",
                "8c78ab1466456a50c569849e39ec9909eaf3fce9a7ee91660da9d4da4a18fe10f93842473b05d38a828c2e0e130b7fed1dc4cef83459a8bda95162c87abf19ce",
                "HR",
                "0");
        String[] payload = {"abc123","Smith","John","1992-03-28", null, null, null, null,
                null, null, null, null};
        assertFalse(
                dp.createPersonalDetailsRecord(payload, "33d8bd019d4b4662bdbc8efd6048d1d9")
        );
        dp.sqlUpdate("DELETE FROM User");
        dp.sqlUpdate("DELETE FROM PersonalDetails");
        dp.sqlUpdate("DELETE FROM Permissions");
        dp.sqlUpdate("DELETE FROM Documents");
    }

    @org.junit.jupiter.api.Test
    void updatePersonalDetails() {
    }

    @org.junit.jupiter.api.Test
    void fetchPersonalDetailsPermissions() {
    }

    @org.junit.jupiter.api.Test
    void fetchPersonalDetails() {
    }
}