public class Position {
    public enum Role {
        Employee("Employee"),
        Manager("Manager"),
        Director("Director");

        public final String label;

        private Role(String label)
        {
            this.label = label;
        }
    }

    public enum Department{
        Admin("Administration"),
        SM("Sales and Marketing"),
        IT("Information Technology"),
        BI("Business Intelligence"),
        MC("Management Consultency"),
        HR("Human Resources");

        public final String label;

        private Department(String label)
        {
            this.label = label;
        }
    }
}

