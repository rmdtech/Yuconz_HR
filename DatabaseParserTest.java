import static org.junit.jupiter.api.Assertions.*;

class DatabaseParserTest {
    DatabaseParser dp = new DatabaseParser();

    @org.junit.jupiter.api.Test
    void createValidPersonalDetailsRecord() {
        String[] payload = {"abc123","Smith","John","1992-03-28", "14 York Road", "Canterbury", "Kent", "CT1 3TA",
                "01634289431", "07499274509", "Jane Smith", "07286308174"};
        assertTrue(
                dp.createPersonalDetailsRecord(payload, "33d8bd019d4b4662bdbc8efd6048d1d9")
        );
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