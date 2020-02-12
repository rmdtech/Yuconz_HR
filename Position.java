public class Position {
    public enum Department{
        Admin("Administration"),
        SM("Sales and Marketing"),
        IT("Information Technology"),
        BI("Business Intelligence"),
        MC("Management Consultancy"),
        HR("Human Resources");

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
        Employee(0),
        Manager(1),
        Director(2);
        public final int level;
        Role(int level)
        {
            this.level = level;
        }
        public int getLevel()
        {
            return level;
        }
    }
}

