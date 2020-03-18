import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    static int revieweeIdIndex = 0;
    static int dueByIndex = 1;
    static int documentIdIndex = 2;
    static int firstReviewerIdIndex = 3;
    static int secondReviewerIdIndex = 4;
    static int revieweeSignatureIndex = 5;
    static int reviewer1SignatureIndex = 6;
    static int reviewer2SignatureIndex = 7;
    static int meetingDateIndex = 8;
    static int performanceSummaryIndex = 9;
    static int reviewerCommentsIndex = 10;
    static int recommendationsIndex = 11;
    static ArrayList<String[]> pastPerformance = new ArrayList<>();
    static ArrayList<String> futurePerformance = new ArrayList<>();

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
            return dp.createPersonalDetailsRecord(details, User.generateUUID());
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
        if (user == null)
        {
            System.out.println("No User provided");
            return false;
        }

        if (!dp.checkEmployeeId(content[revieweeIdIndex]))
        {
            System.out.println("Invalid employeeId provided");
            return false;
        }
        String firstReviewer = dp.fetchDirectSupervisor(content[revieweeIdIndex]);

        if ((firstReviewer + content[secondReviewerIdIndex - 1]).contains(content[revieweeIdIndex]))
        {
            System.out.println("Reviewee can't also be a reviewer");
            return false;
        }

        if (content[dueByIndex] == null)
        {
            System.out.println("Due-date has not been set");
            return false;
        }

        Pattern dateRegex = Pattern.compile("[2][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]");
        Matcher dateMatch = dateRegex.matcher(content[dueByIndex]);
        if (!dateMatch.matches())
        {
            System.out.println("Date format provided is not valid\n   Use 'yyyy-mm-dd");
            return false;
        }

        if (content[documentIdIndex] == null)
        {
            System.out.println("No document ID has been provided");
            return false;
        }

        if (!dp.checkEmployeeId(firstReviewer))
        {
            System.out.println(firstReviewer + " is no longer registered on the system");
            return false;
        }

        if (!dp.checkEmployeeId(content[secondReviewerIdIndex - 1]))
        {
            System.out.println("Invalid employeeId given for the second reviewer");
            return false;
        }
        String[] payload = new String[content.length + 1];
        payload[0] = content[0];
        payload[1] = content[1];
        payload[2] = content[2];
        payload[3] = firstReviewer;
        payload[4] = content[3];

        if (AuthorisationAttempt(Action.Create, "Performance Review", user, payload))
        {
            // Generate a documentID for this review
            // content[documentIdIndex] = User.generateUUID();
            return dp.createReview(payload);
        }
        return false;
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
            return dp.fetchPersonalDetails(pdEmpId);
        }
        return null;
    }

    /**
     * Returns an array containing the information stored in a Review record
     * @param user the user logged in and performing the action
     * @param revieweeId the employeeId of the user who's review is trying to be accessed (makes composite key)
     * @param dueBy the year of the review for that employeeId that is trying to be accessed (makes composite key)
     * @return a string array containing the full review as at current point
     *         [0] Main document
     *         [1] PastPerformance
     *         [2] FuturePerformance
     */
    public static boolean readPerformanceReview(User user, String revieweeId, String dueBy)
    {
        String docId = dp.fetchReviewDocumentId(revieweeId, dueBy);
        if (AuthorisationAttempt(Action.Read, "Performance Review", user, new String[] {docId}))
        {
            return true;
        }
        return false;
    }

    public static String[] readReviewMain(String documentId)
    {
        return dp.fetchReview(documentId);
    }

    public static ArrayList<String[]> readPastPerformance(String documentId)
    {
        return dp.fetchPastPerformance(documentId);
    }

    public static ArrayList<String> readFuturePerformance(String documentId)
    {
        return dp.fetchFuturePerformance(documentId);
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
            return dp.updatePersonalDetails(details);
        }

        return false;
    }

    public static boolean updatePerformanceReview(User user, String[] updatedDocument, ArrayList<String[]> updatedPastPerformance, ArrayList<String> updatedFuturePerformance)
    {
        String docId = dp.fetchReviewDocumentId(updatedDocument[0], updatedDocument[1]);
        String[] currentMainDocument = dp.fetchReview(docId);

        // Only allow users to sign their own signature box
        if (currentMainDocument[revieweeIdIndex].equals(user.getEmployeeId()) && updatedDocument[revieweeSignatureIndex] != null)
        {
            System.out.println("Reviewee signature accepted");
        }
        else
        {
            updatedDocument[revieweeSignatureIndex] = currentMainDocument[revieweeSignatureIndex];
            System.out.println("Cannot overwrite signature on " + currentMainDocument[revieweeIdIndex] + "'s behalf");
        }

        // If this Reviewer is the reviewee's Line Manager -> first reviewer
        if (currentMainDocument[firstReviewerIdIndex].equals(user.getEmployeeId()) && updatedDocument[reviewer1SignatureIndex] != null)
        {
            System.out.println("Direct Manager's signature accepted");
        }
        else
        {
            updatedDocument[reviewer1SignatureIndex] = currentMainDocument[reviewer1SignatureIndex];
            System.out.println("Cannot overwrite signature on " + currentMainDocument[firstReviewerIdIndex] + "'s behalf");
        }

        // If this Reviewer is just another Reviewer
        if (currentMainDocument[secondReviewerIdIndex].equals(user.getEmployeeId()) && updatedDocument[reviewer2SignatureIndex] != null)
        {
            System.out.println("Second Reviewer's signature accepted");
        }
        else
        {
            updatedDocument[reviewer2SignatureIndex] = currentMainDocument[reviewer2SignatureIndex];
            System.out.println("Cannot overwrite signature on " + currentMainDocument[secondReviewerIdIndex] + "'s behalf");
        }

        if (currentMainDocument[revieweeSignatureIndex] != null && currentMainDocument[reviewer1SignatureIndex] != null && currentMainDocument[reviewer2SignatureIndex] != null)
        {
            System.out.println("This Review has already been completed and cannot be updated");
            return false;
        }

        if (AuthorisationAttempt(Action.Update, "Performance Review", user, new String[] { docId }))
        {
            return dp.updateReview(docId, updatedDocument, updatedPastPerformance, updatedFuturePerformance);
        }
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
     *                Create Performance Review - [revieweeId, dueBy, reviewer1, reviewer2, documentId]
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
                            if (payload[revieweeIdIndex] != null && dp.checkEmployeeId(payload[firstReviewerIdIndex]) && dp.checkEmployeeId(payload[secondReviewerIdIndex]))
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
                                if (content[revieweeSignatureIndex] != null && content[reviewer1SignatureIndex] != null && content[reviewer2SignatureIndex] != null)
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
                case("Delete"):
                    if (target.equals("Personal Details"))
                    {
                        return false;
                    }
                    // Stage 5
                    else if (target.equals("Performance Review"))
                    {
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

    /**
     * Gets all reviews currently in the Database. Only to be used by HR Staff
     * @param user The currently logged in member of HR
     * @return An ArrayList containing all the keys required to fetch a review
     */
    public static ArrayList<String[]> getAllReviews(User user)
    {
        if (user.getDepartment().equals(Position.Department.HR))
        {
            return dp.fetchAllReviewKeys();
        }
        else
        {
            dp.recordAuthorisationAttempt(user.getEmployeeId(), Action.Read.toString(), "UI Restraint error - tried loading all Reviews", false);
            return null;
        }
    }

    /**
     * Returns all reviews where a User is registered as a Reviewer (only to be used by managers or above)
     * @param user
     * @return an ArrayList containing all the keys required to fetch a certain review
     */
    public static ArrayList<String[]> getReviewsAsReviewer(User user)
    {
        ArrayList<String[]> allReviews = dp.fetchAllReviewKeys();
        ArrayList<String[]> reviewerReviews = new ArrayList<>();
        if (user.getRole().level > 0)
        {
            for (int i = 0; i < allReviews.size(); i++)
            {
                String docId = dp.fetchReviewDocumentId(allReviews.get(i)[0], allReviews.get(i)[1]);
                if (dp.isReviewer(docId, user.getEmployeeId()))
                {
                    reviewerReviews.add(allReviews.get(i));
                }
            }
        }
        else
        {
            dp.recordAuthorisationAttempt(user.getEmployeeId(), Action.Read.toString(), "UI Error - tried accessing all reviews where User is a Reviewer", false);
        }
        return reviewerReviews;
    }

    /**
     * Returns all reviews of a certain User
     * @param user the User currently logged in, trying to read their own reviews
     * @return an ArrayList containing all the keys fetch all relevant reviews
     */
    public static ArrayList<String[]> getReviewsAsReviewee(User user)
    {
        ArrayList<String[]> allReviews = dp.fetchAllReviewKeys();
        ArrayList<String[]> revieweeReviews = new ArrayList<>();

        for (int i = 0; i < allReviews.size(); i++)
        {
            String docId = dp.fetchReviewDocumentId(allReviews.get(i)[0], allReviews.get(i)[1]);
            if (dp.isReviewee(docId, user.getEmployeeId()))
            {
                revieweeReviews.add(allReviews.get(i));
            }
        }
        return revieweeReviews;
    }

}
