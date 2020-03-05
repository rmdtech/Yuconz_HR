public class Position {
    public enum Department{
        Admin("Admin"),
        SalesAndMarketing("SalesAndMarketing"),
        IT("IT"), // Information Technology
        BI("BI"), // Business Intelligence
        MC("MC"), // Management Consultancy
        HR("HR"); // Human Resources

        public final String label;

        /**
         * Constructor for a Department
         * @param label the name of this Department
         */
        Department(String label)
        {
            this.label = label;
        }

        /**
         * Converts the Department into a String only to be used when writing to the Database
         * @return the String version of this Department
         */
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

        /**
         * Constructor for a Role
         * @param level the level 0-2 of this role within the company
         * @param label the name of this role
         */
        Role(int level, String label)
        {
            this.level = level;
            this.label = label;
        }

        /**
         * Converts the Role into a String only to be used when writing to the Database
         * @return the String version of this Role
         */
        public String toString()
        {
            return label;
        }

        /**
         * Returns the level of a Role
         * @return 0-2 the level of this Role within the company
         */
        public int getLevel()
        {
            return level;
        }

        /**
         * Returns a Role Enum based off the level
         * @param level 0-2 the level of the Role that is to be returned
         * @return the Role Enum with the corresponding level
         */
        public Role getFromLevel(int level)
        {
            for (Role pos : Position.Role.values())
            {
                if (pos.level == level)
                {
                    return pos;
                }
            }
            return null;
        }
    }
}

