import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Constructor for class Authorise
 */
public class Authorise
{
    /**
     * Each kind of operation is stored as an Enum to eliminate human error
     */
    public enum Action{
        Create("Create"),
        Read("Read"),
        Update("Update");

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
    static int rIndexRevieweeId, pIndexEmployeeId = 0;
    static int rIndexDueBy, pIndexSurname = 1;
    static int rIndexDocumentId, pIndexFirstName = 2;
    static int rIndexReviewer1Id, pIndexDoB = 3;
    static int rIndexReviewer2Id, pIndexAddress = 4;
    static int rIndexRevieweeSignature, pIndexCity = 5;
    static int rIndexReviewer1Signature, pIndexPostCode = 6;
    static int rIndexReviewer2Signature = 7;

    /**
     * Creates a new Personal Details record for an Employee
     * @param user logged in User performing the operation
     * @param personalDetails the content of this Personal Details record
     *                        [0] Employee ID
     *                        [1] Surname
     *                        [2] First Name
     *                        [3] Date of Birth (yyyy-mm-dd)
     *                        [4] Address
     *                        [5] City
     *                        [6] Postcode
     *                        [7] Telephone Number
     *                        [8] Mobile Phone Number
     *                        [9] Emergency Contact Name
     *                        [10] Emergency Contact Phone Number
     * @return whether or not the action has been successful and written to the Database
     */
    public static boolean createPersonalDetailsRecord(User user, String[] personalDetails)
    {
        if (AuthorisationAttempt(Action.Create, "Personal Details", user, personalDetails))
        {
            if (personalDetails[pIndexEmployeeId] == null)
            {
                return false;
            }
            return dp.createPersonalDetailsRecord(personalDetails, User.generateUUID());
        }
        return false;
    }

    /**
     * Creates a Review record. revieweeId and reviewers are mandatory fields
     * @param user the user logged in and trying to perform the action
     * @param reviewContent the initial, mandatory reviewContent for this review
     *                [0] Employee ID
     *                [1] Due By Date (yyyy-mm-dd)
     *                [2] Document ID (see User.generateUUID)
     *                [3] Second Reviewer ID
     * @return whether the operation has been successful or not
     */
    public static boolean createPerformanceReview(User user, String[] reviewContent)
    {
        if (user == null)
        {
            System.out.println("No User provided");
            return false;
        }

        if (!dp.checkEmployeeId(reviewContent[rIndexRevieweeId]))
        {
            System.out.println("Invalid employeeId provided");
            return false;
        }
        String firstReviewer = dp.fetchDirectSupervisor(reviewContent[rIndexRevieweeId]);

        if ((firstReviewer + reviewContent[rIndexReviewer2Id - 2]).contains(reviewContent[rIndexRevieweeId]))
        {
            System.out.println("Reviewee can't also be a reviewer");
            return false;
        }

        if (reviewContent[rIndexDueBy] == null)
        {
            System.out.println("Due By Date has not been set");
            return false;
        }

        Pattern dateRegex = Pattern.compile("[2][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]");
        Matcher dateMatch = dateRegex.matcher(reviewContent[rIndexDueBy]);
        if (!dateMatch.matches())
        {
            System.out.println("Date format provided is not valid\n   Use 'yyyy-mm-dd'");
            return false;
        }

        if (!dp.checkEmployeeId(firstReviewer))
        {
            System.out.println(firstReviewer + " is no longer registered on the system");
            return false;
        }

        if (!dp.checkEmployeeId(reviewContent[rIndexReviewer2Id - 2]))
        {
            System.out.println("Invalid employeeId given for the second reviewer");
            return false;
        }

        String[] payload = new String[reviewContent.length + 2];
        payload[rIndexRevieweeId] = reviewContent[rIndexRevieweeId];
        payload[rIndexDueBy] = reviewContent[rIndexDueBy];
        payload[rIndexDocumentId] = User.generateUUID();
        payload[rIndexReviewer1Id] = firstReviewer;
        payload[rIndexReviewer2Id] = reviewContent[rIndexReviewer2Id - 2];

        if (AuthorisationAttempt(Action.Create, "Performance Review", user, payload))
        {
            return dp.createReview(payload);
        }
        return false;
    }

    /**
     * Read the Personal Details Record of a certain Employee
     * @param user logged in User performing this action
     * @param employeeId the Employee ID of the Employee for whom the Personal Details record should be accessed
     * @return a string array containing the Personal Details record
     *                [0] Employee ID
     *                [1] Surname
     *                [2] First Name
     *                [3] Date of Birth (yyyy-mm-dd)
     *                [4] Address
     *                [5] City
     *                [6] Postcode
     *                [7] Telephone Number
     *                [8] Mobile Number
     *                [9] Emergency Contact
     *                [10] Emergency Contact Phone Number
     */
    public static String[] readPersonalDetails (User user, String employeeId)
    {
        if (AuthorisationAttempt(Action.Read, "Personal Details", user, new String[] { employeeId } ))
        {
            return dp.fetchPersonalDetails(employeeId);
        }
        return null;
    }

    /**
     * Checks whether a User is authorised to access a Performance Review
     * @param user the User logged in and performing the action
     * @param revieweeId the Employee ID of the Reviewee
     * @param dueBy the Due By Date of this Review
     * @return whether this User is authorised to access this Performance Review
     */
    public static boolean readPerformanceReview(User user, String revieweeId, String dueBy)
    {
        return AuthorisationAttempt(Action.Read, "Performance Review", user, new String[]{ dp.fetchReviewDocumentId(revieweeId, dueBy) });
    }

    /**
     * Returns the main elements of a Performance Review. To be used after running readPerformanceReview
     * @param documentId the Document ID of the Review that is to be accessed
     * @return a string array containing the main content of this Review
     *                [0] Reviewee Employee ID
     *                [1] Due By Date
     *                [2] Document ID
     *                [3] Reviewer 1 Employee ID (Direct Supervisor)
     *                [4] Reviewer 2 Employee ID (Allocated)
     *                [5] Reviewee Signature (yyyy-mm-dd)
     *                [6] Reviewer 1 Signature (yyyy-mm-dd)
     *                [7] Reviewer 2 Signature (yyyy-mm-dd)
     *                [8] Meeting Date (obsolete(?))
     *                [9] Performance Summary
     *                [10] Reviewer Comments
     *                [11] Recommendation
     **/
    public static String[] readReviewMain(String documentId)
    {
        return dp.fetchReview(documentId);
    }

    /**
     * Returns the past performance elements of a Performance Review. To be used after running readPerformanceReview
     * @param documentId the Document ID of the Review that is to be accessed
     * @return an ArrayList of string arrays
     *                [0] [0] Document ID
     *                [1] [0-n] Objective Number
     *                [2] [0-n] Objectives
     *                [3] [0-n] Achievements
     */
    public static ArrayList<String[]> readPastPerformance(String documentId)
    {
        return dp.fetchPastPerformance(documentId);
    }

    /**
     * Returns the future performance elements of a Performance Review. To be used after running readPerformanceReview
     * @param documentId the Document ID of the Review that is to be accessed
     * @return an ArrayList of strings
     *                [0] Document ID
     *                [1] Objective Number
     *                [2] Objective
     */
    public static ArrayList<String> readFuturePerformance(String documentId)
    {
        return dp.fetchFuturePerformance(documentId);
    }

    /**
     * Updates the Personal Details record of a User.
     * @param user logged in User object performing the action
     * @param details the full updated Personal Details record that is to be written to the Database
     *                [0] Employee ID
     *                [1] Surname
     *                [2] First Name
     *                [3] Date of Birth (yyyy-mm-dd)
     *                [4] Address
     *                [5] City
     *                [6] Postcode
     *                [7] Telephone Number
     *                [8] Mobile Number
     *                [9] Emergency Contact
     *                [10] Emergency Contact Phone Number
     * @return whether this operation has been successful or not
     */
    public static boolean updatePersonalDetails(User user, String[] details)
    {
        if (details == null || details[pIndexEmployeeId] == null)
        {
            dp.recordAuthorisationAttempt(user.getEmployeeId(), Action.Create.label, "Personal Details", true);
            return false;
        }

        String[] currentDetails = dp.fetchPersonalDetails(details[pIndexEmployeeId]);

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
     * Updates a Performance Review
     * @param user the logged in User object performing the action
     * @param updatedDocument a full updated main document element
     *                [0] Reviewee Employee ID
     *                [1] Due By Date
     *                [2] Document ID
     *                [3] Reviewer 1 Employee ID (Direct Supervisor)
     *                [4] Reviewer 2 Employee ID (Allocated)
     *                [5] Reviewee Signature (yyyy-mm-dd)
     *                [6] Reviewer 1 Signature (yyyy-mm-dd)
     *                [7] Reviewer 2 Signature (yyyy-mm-dd)
     *                [8] Meeting Date (obsolete(?))
     *                [9] Performance Summary
     *                [10] Reviewer Comments
     *                [11] Recommendation
     * @param updatedPastPerformance a full updated Past Performance ArrayList of String Arrays
     *                [0] [0] Document ID
     *                [1] [0-n] Objective Number
     *                [2] [0-n] Objectives
     *                [3] [0-n] Achievements
     * @param updatedFuturePerformance a full updated Future Performance ArrayList of Strings
     *                [0] Document ID
     *                [1] Objective Number
     *                [2] Objective
     * @return whether this operation has been successful
     */
    public static boolean updatePerformanceReview(User user, String[] updatedDocument, ArrayList<String[]> updatedPastPerformance, ArrayList<String> updatedFuturePerformance)
    {
        String docId = dp.fetchReviewDocumentId(updatedDocument[0], updatedDocument[1]);
        String[] currentMainDocument = dp.fetchReview(docId);

        if (currentMainDocument[rIndexReviewer1Signature] != null && currentMainDocument[rIndexRevieweeSignature] != null && currentMainDocument[rIndexReviewer2Signature] != null)
        {
            System.out.println("This Review has already been completed and cannot be updated");
            return false;
        }

        // Reject other users signing this box
        if (!currentMainDocument[rIndexRevieweeId].equals(user.getEmployeeId()))
        {
            System.out.println("Cannot overwrite signature on " + currentMainDocument[rIndexRevieweeId] + "'s behalf");
            updatedDocument[rIndexRevieweeSignature] = currentMainDocument[rIndexRevieweeSignature];
        }
        // Cannot un-sign Review
        else if (currentMainDocument[rIndexRevieweeId].equals(user.getEmployeeId()) && (currentMainDocument[rIndexRevieweeSignature] != null && updatedDocument[rIndexRevieweeSignature].equals("false")))
        {
            System.out.println("You have already signed this review off. Cannot un-sign document");
            updatedDocument[rIndexRevieweeSignature] = currentMainDocument[rIndexRevieweeSignature];
        }



        // If this Reviewer is the reviewee's Line Manager -> first reviewer
        if (!currentMainDocument[rIndexReviewer1Id].equals(user.getEmployeeId()))
        {
            updatedDocument[rIndexReviewer1Signature] = currentMainDocument[rIndexReviewer1Signature];
            System.out.println("Cannot overwrite signature on " + currentMainDocument[rIndexReviewer1Id] + "'s behalf");
        }
        // Cannot un-sign Review
        else if (currentMainDocument[rIndexReviewer1Id].equals(user.getEmployeeId()) && (currentMainDocument[rIndexReviewer1Signature] != null && updatedDocument[rIndexReviewer1Signature].equals("false")))
        {
            System.out.println("You have already signed this review off. Cannot un-sign document");
            updatedDocument[rIndexReviewer1Signature] = currentMainDocument[rIndexReviewer1Signature];
        }


        // If this Reviewer is just another Reviewer
        if (!currentMainDocument[rIndexReviewer2Id].equals(user.getEmployeeId()))
        {
            updatedDocument[rIndexReviewer2Signature] = currentMainDocument[rIndexReviewer2Signature];
            System.out.println("Cannot overwrite signature on " + currentMainDocument[rIndexReviewer2Id] + "'s behalf");
        }
        // Cannot un-sign Review
        else if (currentMainDocument[rIndexReviewer2Id].equals(user.getEmployeeId()) && (currentMainDocument[rIndexReviewer2Signature] != null && updatedDocument[rIndexReviewer2Signature].equals("false")))
        {
            updatedDocument[rIndexReviewer2Signature] = currentMainDocument[rIndexReviewer2Signature];
            System.out.println("Cannot overwrite signature on " + currentMainDocument[rIndexReviewer2Id] + "'s behalf");
        }

        if (AuthorisationAttempt(Action.Update, "Performance Review", user, new String[] { docId }))
        {
            return dp.updateReview(docId, updatedDocument, updatedPastPerformance, updatedFuturePerformance);
        }
        return false;
    }

    /**
     * Any action must first be authorised by this method
     * @param action Authorise.Action Enum (CRUD)
     * @param target The target document ("Personal Details" or "Performance Review")
     * @param user The User that is trying to perform this action
     * @param payload Any information that may be associated with this action.
     *                Create Personal Details - null
     *                Create Performance Review - [revieweeId, dueBy, reviewer1, reviewer2, documentId]
     *                Read Personal Details - Associated Employee in position 0
     *                Read Performance Review - Document ID in position 0
     *                Update Personal Details - Associated Employee in position 0
     *                Update Performance Review -
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
                            dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), "Personal Details", true);
                            return true;
                        }
                        dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), "Personal Details", false);
                        System.out.println(user.getEmployeeId() + " was not of the right department");
                        return false;
                    }
                    else if (target.equals("Performance Review"))
                    {
                        if (user.getDepartment().equals(Position.Department.HR))
                        {
                            if (payload[rIndexRevieweeId] != null && dp.checkEmployeeId(payload[rIndexReviewer1Id]) && dp.checkEmployeeId(payload[rIndexReviewer2Id]))
                            {
                                dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), "Performance Review", true);
                                return true;
                            }
                            System.out.println("Interal error: Invalid employeeIds were passed from Authorise.createPerformanceReview");
                            dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), "Performance Review", false);
                            return false;
                        }
                        System.out.println(user.getEmployeeId() + " did not have the required permissions");
                        dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), "Performance Review", false);
                        return false;
                    }
                    else
                    {
                        System.out.println("Internal error: The given target '" + target + "' has bot been recognised");
                        dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), "Performance Review", null);
                        return false;
                    }
                case("Read"):
                    if (target.equals("Personal Details"))
                    {
                        String associatedEmployee = payload[0];
                        Position.Department requiredDpt = Position.Department.HR;

                        if (user.getDepartment().equals(requiredDpt)  || associatedEmployee.equals(user.getEmployeeId()))
                        {
                            dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), "Personal Details", true);
                            return true;
                        }
                        else
                        {
                            dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), "Personal Details", false);
                            return false;
                        }
                    }
                    else if (target.equals("Performance Review"))
                    {
                        // docID = payload[0]
                        if (payload[0] != null)
                        {
                            // If this user has read access on the requested document
                            if (dp.isReviewee(payload[0], user.getEmployeeId()) || dp.isReviewer(payload[0], user.getEmployeeId()) || user.getDepartment().equals(Position.Department.HR))
                            {
                                dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), "Performance Review", true);
                                return true;
                            }
                            System.out.println("You do not have the required permissions to access this file");
                            dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), "Performance Review", false);
                            return false;
                        }
                        System.out.println("Internal Error: No document has been selected when requesting access permission");
                        dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), "Performance Review", null);
                        return false;
                    }
                    else
                    {
                        System.out.println("Internal error: The given target '" + target + "' has bot been recognised");
                        dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), null, null);
                        return false;
                    }
                case("Update"):
                    if (target.equals("Personal Details"))
                    {
                        Position.Department requiredDpt = Position.Department.HR;
                        String associatedEmployee = payload[0];

                        if (user.getDepartment().equals(requiredDpt) || associatedEmployee.equals(user.getEmployeeId()))
                        {
                            dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), "Personal Details", true);
                            return true;
                        }
                        else
                        {
                            dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), "Personal Details", false);
                            return false;
                        }
                    }
                    else if (target.equals("Performance Review"))
                    {
                        // String docId = payload[0];
                        if (payload[0] != null)
                        {
                            // Is the user a review participant or from HR?
                            if (dp.isReviewee(payload[0], user.getEmployeeId()) || dp.isReviewer(payload[0], user.getEmployeeId()) || user.getDepartment().equals(Position.Department.HR))
                            {
                                String[] content = dp.fetchReview(payload[0]);
                                // Has this already been signed off?
                                if (content[rIndexRevieweeSignature] != null && content[rIndexReviewer1Signature] != null && content[rIndexReviewer2Signature] != null)
                                {
                                    System.out.println("Changes to this review are not allowed as it has been signed off already");
                                    dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), "Performance Review", false);
                                    return false;
                                }
                                dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), "Performance Review", true);
                                return true;
                            }
                        }
                        System.out.println("Internal Error: No documentId provided when attempting to update review");
                        dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), "Performance Review", null);
                        return false;
                    }
                    else
                    {
                        System.out.println("Internal error: The given target '" + target + "' has bot been recognised");
                        dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), null, null);
                        return false;
                    }
                default:
                    System.out.println("The given operation was not recognised");
                    dp.recordAuthorisationAttempt(user.getEmployeeId(), null, null, null);
                    break;
            }
        }
        System.out.println(user.getEmployeeId() + " was not logged in");
        dp.recordAuthorisationAttempt("User that was not logged in", action.toString(), null, null);
        return false;
    }
}