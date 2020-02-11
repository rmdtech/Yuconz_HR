public class Role {
    public enum Element {
        Employee("Employee"),
        HRExmployee("HR Employee"),
        Manager("Manager"),
        Reviewer("Reviewer"),
        Director("Director");

        public final String label;

        private Element(String label) {
            this.label = label;
        }
    }
}

