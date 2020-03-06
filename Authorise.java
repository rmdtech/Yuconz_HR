/**
 * Constructor for class Authorise
 */
public class Authorise
{
    /**
     * Each CRUD operation is stored as an Enum to eliminate human error
     */
    public enum Action{
        Create("Create"),
        Read("Read"),
        Update("Update"),
        Delete("Delete");

        public final String label;

        /**
         * Constructor for an operation Enum
         * @param label The label with which this Enum is to be initialised with
         */
        Action (String label)
        {
            this.label = label;
        }

        /**
         * Get the String value of an operation
         * @return the String value of an operation
         */
        public String toString()
        {
            return label;
        }
    }

    static DatabaseParser dp = new DatabaseParser();

    /**
     * Creates a new Personal Details record for a member of Staff
     * @param user the user performing the action
     * @param details the information that is to be written into the Personal Details record [employeeID, surname, name, date of birth, address, city, postcode, telephoneNumber, mobileNumber, emergency contact, emergency contact number]
     * @return whether or not the action has been successful
     */
    public static boolean createPersonalDetailsRecord(User user, String[] details)
    {
        if (AuthorisationAttempt(Action.Create, "Personal Details", user, details))
        {
            if (details[0] == null)
            {
                return false;
            }
            return dp.createPersonalDetailsRecord(details, User.generateSalt());
        }
        return false;
    }

    /**
     * Creates a Review record. revieweeId and reviewers are mandatory fields
     * @param user the user logged in and trying to perform the action
     * @param content the initial, mandatory content for this review
     * @return whether the operation has been successful or not
     */
    public static boolean createPerformanceReview(User user, String[] content)
    {
        /*
        0: revieweeId           [String]
        1: dueBy                [Date]
        2: meetingDate          [Date]
        3: firstReviewerId      [String, empId]
        4: secondReviewerId     [String, empId]
        5: revieweeSigned       [Boolean]
        6: firstReviewerSigned  [Boolean]
        7: secondReviewerSigned [Boolean]
        8: performanceSurvey    [String]
        9: reviewerComments    [String]
        10: recommendations     [String]
        11: documentId          [String]
         */
        if (AuthorisationAttempt(Action.Create, "Performance Review", user, content))
        {
            // Report back when a date has been set to be in the past? Might be a test case
            // Generate a documentID for this review
            content[11] = User.generateSalt();

            return dp.createReview(content);
        }
        return false;
    }

    /**
     * Returns an array containing the information stored in a Review record
     * @param user the user logged in and performing the action
     * @param revieweeId the employeeId of the user who's review is trying to be accessed (makes composite key)
     * @param year the year of the review for that employeeId that is trying to be accessed (makes composite key)
     * @return a string array containing the full review as at current point
     */
    public static String[] readPerformanceReview(User user, String revieweeId, String year)
    {
        String docId = dp.fetchDocumentId(revieweeId, year);
        if (AuthorisationAttempt(Action.Read, "Performance Review", user, new String[] {docId}))
        {
            return dp.readReview(docId);
        }
        return null;
    }

    /**
     * Read the Personal Details record of a certain member of Staff
     * @param user the user that is performing the action
     * @param pdEmpId the employeeId of which the user is accessing the Personal Details record from
     * @return a String array containing the Personal Details record [employeeID, surname, name, date of birth, address, city, postcode, telephoneNumber, mobileNumber, emergency contact, emergency contact number]
     */
    public static String[] readPersonalDetails (User user, String pdEmpId)
    {
        if (AuthorisationAttempt(Action.Read, "Personal Details", user, new String[] { pdEmpId } ))
        {
            dp.recordAuthorisationAttempt(user.getEmployeeId(), Action.Update.label, "Personal Details", true);
            return dp.fetchPersonalDetails(pdEmpId);
        }
        dp.recordAuthorisationAttempt(user.getEmployeeId(), Action.Update.label, "Personal Details", false);
        return null;
    }

    /**
     * Updates the Personal Details record for a User.
     * @param user the user that performs the operation
     * @param details the payload that is to be sent to the Database. [employeeID, surname, name, date of birth, address, city, postcode, telephoneNumber, mobileNumber, emergency contact, emergency contact number]
     * @return whether or not the operation has been successful
     */
    public static boolean updatePersonalDetails(User user, String[] details)
    {
        if (details == null || details[0] == null)
        {
            dp.recordAuthorisationAttempt(user.getEmployeeId(), Action.Create.label, "Personal Details", true);
            return false;
        }

        String[] currentDetails = dp.fetchPersonalDetails(details[0]);

        for (int i = 0; i < details.length; i++)
        {
            if (details[i] == null)
            {
                details[i] = currentDetails[i];
            }
        }

        if (AuthorisationAttempt(Action.Update, "Personal Details", user, details))
        {
            dp.recordAuthorisationAttempt(user.getEmployeeId(), Action.Create.label, "Personal Details", true);
            return dp.updatePersonalDetails(details);
        }

        dp.recordAuthorisationAttempt(user.getEmployeeId(), Action.Create.label, "Personal Details", false);
        return false;
    }

    /**
     * Records that a User attempted to delete a Document
     * @param user the user attemping the operation
     * @return whether or not the operation has been successful
     */
    public static boolean deletePersonalDetails(User user)
    {
        AuthorisationAttempt(Action.Delete, "Personal Details", user,null );
        return false;
    }

    /**
     * Any action must first be authorised by this method
     * @param action Authorise.Action Enum (CRUD)
     * @param target The target document ("Personal Details" or "Performance Review")
     * @param user The User that is trying to perform this action
     * @param payload Any information that may be associated with this action.
     *                Create Personal Details - null
     *                Create Performance Review - full structure with Reviewee [0] and Reviewers in [3 and 4]
     *                Read Personal Details - Associated Employee in position 0
     *                Read Performance Review - Document ID in position 0
     *                Update Personal Details - Associated Employee in position 0
     *                Update Performance Review -
     *                Delete * - null
     * @return whether the action has been successful or not
     */
    private static boolean AuthorisationAttempt(Action action, String target, User user, String[] payload)
    {
        if (user.isLoggedIn())
        {
            switch(action.toString())
            {
                case("Create"):
                    if (target.equals("Personal Details"))
                    {
                        if (user.getDepartment().equals(Position.Department.HR))
                        {
                            return true;
                        }
                        System.out.println(user.getEmployeeId() + " was not of the right department");
                        return false;
                    }
                    else if (target.equals("Performance Review"))
                    {
                        if (user.getDepartment().equals(...)  )
                        {
                            if (payload[0] != null && dp.checkEmployeeId(payload[3]) && dp.checkEmployeeId(payload[4]))
                            {
                                return true;
                            }
                            System.out.println("Invalid employeeIds were provided for either the reviewee or one of the reviewers");
                            return false;
                        }
                        System.out.println(user.getEmployeeId() + " did not have the required permissions");
                        return false;
                    }
                    else
                    {
                        System.out.println("Internal error: The given target '" + target + "' has bot been recognised");
                        return false;
                    }
                    break;

                case("Read"):
                    if (target.equals("Personal Details"))
                    {
                        String associatedEmployee = payload[0];
                        Position.Department requiredDpt = Position.Department.HR;

                        if (user.getDepartment().equals(requiredDpt)  || associatedEmployee.equals(user.getEmployeeId()))
                        {
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    }
                    else if (target.equals("Performance Review"))
                    {
                        // docID = payload[0]
                        if (payload[0] != null)
                        {
                            // If this user has read access on the requested document
                            if (dp.isReviewee(payload[0], user.getEmployeeId()) || dp.isReviewer(payload[0], user.getEmployeeId()) || user.getDepartment().equals(Position.Department.HR)))
                            {
                                return true;
                            }
                            System.out.println("You don not have the required permissions to access this file");
                            return false;
                        }
                        System.out.println("Internal Error: No document has been selected when requesting access permission");
                        return false;
                    }
                    else
                    {
                        System.out.println("Internal error: The given target '" + target + "' has bot been recognised");
                        return false;
                    }
                    break;

                case("Update"):
                    if (target.equals("Personal Details"))
                    {
                        Position.Department requiredDpt = Position.Department.HR;
                        String associatedEmployee = payload[0];

                        if (user.getDepartment().equals(requiredDpt) || associatedEmployee.equals(user.getEmployeeId()))
                        {
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    }
                    // Stage 5
                    else if (target.equals("Performance Review"))
                    {
                        // If HR, or if reviewee at any point BEFORE sign-off
                        // If reviewer DURING meeting
                        //
                        if (user.getDepartment().equals(Position.Department.HR) || )
                    }
                    else
                    {
                        System.out.println("Internal error: The given target '" + target + "' has bot been recognised");
                        return false;
                    }
                    break;

                case("Delete"):
                    if (target.equals("Personal Details"))
                    {
                        return false;
                    }
                    // Stage 5
                    else if (target.equals("Performance Review"))
                    {

                    }
                    else
                    {
                        System.out.println("Internal error: The given target '" + target + "' has bot been recognised");
                        return false;
                    }
                    break;

                default:
                    System.out.println("The given operation was not recognised");
                    break;
            }
        }
        System.out.println(user.getEmployeeId() + " was not logged in");
        return false;
    }
}
