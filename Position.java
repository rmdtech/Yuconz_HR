public class Position {
    public enum Department{
        Admin("Admin"),
        SalesAndMarketing("SalesAndMarketing"),
        IT("IT"), // Information Technology
        BI("BI"), // Business Intelligence
        MC("MC"), // Management Consultancy
        HR("HR"); // Human Resources

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

        Role(int level, String label) {
            this.level = level;
            this.label = label;
        }

        public String toString() {
            return label;
        }

        public int getLevel() {
            return level;
        }

        public Role getFromLevel(int level)
        {
            for (Role pos : Position.Role.values()) {
                if (pos.level == level) {
                    return pos;
                }
            }
            return null;
        }
    }
}

