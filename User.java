public class User{

    private String employeeNumber;
    private String password;
    private String accessLevel;

    private DatabaseParser dp = new DatabaseParser();

    public User(String employeeNumber, String password){

        /* Want to take employeenumber and password through some kind of user input
         * Password would also be handed over in an encrypted form so it can be checked against the DB record
        */
        this.employeeNumber = employeeNumber;
        this.password = password;
    }

    /**
     * This will log the user into the system
     */
    public void login(){
        // Call the database parser and ask it if this employeeNumber exists within the database
        if (dp.getEmployeeNumber(employeeNumber) != null){
            // Call the database parser and ask it to read the encrypted password stored in the database
            if (dp.getEmployeePassword(employeeNumber).equals(password)){
                // do something that indicates the login status
            }
        }
    }

    public void logout(){

    }
}