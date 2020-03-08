import java.io.File;

public class Main {
    public static void main(String[] args) {
        if(checkIsFirstBoot())
        {
            DatabaseParser dp = new DatabaseParser();
            dp.setupDatabase();
        }
    }

    static boolean checkIsFirstBoot()
    {
        File dbFile = new File("./databases/yuconz.db");
        return !dbFile.exists();
    }
}
