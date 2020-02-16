public class Position {
    public enum Department{
        Admin("Admin"),
        SalesAndMarketing("SalesAndMarketing"),
        IT("IT"),
        BI("BI"),
        MC("MC"),
        HR("HR");

        public final String label;

        Department(String label)
        {
            this.label = label;
        }

        public String toString()
        {
            return label;
        }
    }

    public enum Role {
        Employee(0, "Employee"),
        Manager(1, "Manager"),
        Director(2, "Director");
        public final int level;
        public final String label;

        Role(int level, String label)

        {
            this.level = level;
            this.label = label;
        }
        public String toString()
        {
            return label;
        }
        public int getLevel()
        {
            return level;
        }
    }
}

