import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
    static int firstReviewerIdIndex = 2;
    static int secondReviewerIdIndex = 3;
    static int documentIdIndex = 4;
    static int revieweeSignatureIndex = 5;
    static int reviewer1SignatureIndex = 6;
    static int reviewer2SignatureIndex = 7;
    static int meetingDateIndex = 8;
    static int performanceSummaryIndex = 9;
    static int reviewerCommentsIndex = 10;
    static int recommendationsIndex = 11;
    static HashMap<Integer, String[]> pastPerformance = new HashMap<>();
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
        //0: revieweeId           		[String]
        //1: dueBy                		[Date]
        //2: firstReviewerId      		[String, empId]
        //3: secondReviewerId     	[String, empId]
        //4: documentId          		[String]
        if (!dp.checkEmployeeId(content[0]))
        {
            System.out.println("Invalid employeeId provided");
            return false;
        }

        Pattern dateRegex = Pattern.compile("[2][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]-*");
        Matcher dateMatch = dateRegex.matcher(content[1]);
        if (!dateMatch.matches())
        {
            System.out.println("Date format provided is not valid\n   Use 'yyyy-mm-dd");
            return false;
        }

        // Need to check that these are on a higher level than employee
        if (!dp.checkEmployeeId(content[2]))
        {
            System.out.println("Invalid employeeId given for the first reviewer");
            return false;
        }
        if (!dp.checkEmployeeId(content[3]))
        {
            System.out.println("Invalid employeeId given for the second reviewer");
            return false;
        }

        if (AuthorisationAttempt(Action.Create, "Performance Review", user, content))
        {
            // Generate a documentID for this review
            content[4] = User.generateSalt();
            return dp.createReview(content);
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
            dp.recordAuthorisationAttempt(user.getEmployeeId(), Action.Update.label, "Personal Details", true);
            return dp.fetchPersonalDetails(pdEmpId);
        }
        dp.recordAuthorisationAttempt(user.getEmployeeId(), Action.Update.label, "Personal Details", false);
        return null;
    }

    /**
     * Returns an array containing the information stored in a Review record
     * @param user the user logged in and performing the action
     * @param revieweeId the employeeId of the user who's review is trying to be accessed (makes composite key)
     * @param dueBy the year of the review for that employeeId that is trying to be accessed (makes composite key)
     * @return a string array containing the full review as at current point
     */
    public static String[] readPerformanceReview(User user, String revieweeId, String dueBy)
    {
        String docId = dp.fetchReviewDocumentId(revieweeId, dueBy);
        if (AuthorisationAttempt(Action.Read, "Performance Review", user, new String[] {docId}))
        {
            return dp.readReview(docId);
        }
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

    public static boolean updatePerformanceReview(User user, String[] updatedDocument, HashMap<Integer, String[]> updatedPastPerformance, ArrayList<String> updatedFuturePerformance)
    {
        String docId = dp.fetchReviewDocumentId(updatedDocument[0], updatedDocument[1]);
        String[] currentDocument = dp.readReview(docId);
        pastPerformance = dp.readPastPerformance(docId);
        futurePerformance = dp.readFuturePerformance(docId);
        int pastSize = pastPerformance.size();
        int futureSize = futurePerformance.size();


        boolean justSigning = true;
        for (int i = meetingDateIndex; i < currentDocument.length-1; i++)
        {
            // Are there differences in the main document?
            if (!currentDocument[i].equals(updatedDocument[i]))
            {
                justSigning = false;
                break;
            }
            // Are there differences in the future Performance section?
            if (!futurePerformance.equals(updatedFuturePerformance))
            {
                justSigning = false;
                break;
            }
        }

        if (justSigning)
        {
            // Are there differences in the past Performance section?
            for (int i = 0; i < pastSize; i++)
            {
                if (Arrays.equals(pastPerformance.get(i), updatedPastPerformance.get(i)))
                {
                    justSigning = false;
                    break;
                }
            }
        }

        // If there are any signatures on this document, remove them as it will be updated and need to be reviewed again
        if (!justSigning)
        {
            currentDocument[revieweeSignatureIndex] = null;
            currentDocument[reviewer1SignatureIndex] = null;
            currentDocument[reviewer2SignatureIndex] = null;
        }

        // Only allow users to sign their own signature box
        if (dp.isReviewee(user.getEmployeeId()) && updatedDocument[revieweeSignatureIndex] != null)
        {
            currentDocument[revieweeSignatureIndex] = updatedDocument[revieweeSignatureIndex];
        }
        else if (dp.isReviewer(user.getEmployeeId()) && ... && updatedDocument[reviewer1SignatureIndex] != null)
        {
            // need a way of finding out if this is the line manager or not
            currentDocument[reviewer1SignatureIndex] = updatedDocument[reviewer1SignatureIndex];
        }
        else if (dp.isReviewer(user.getEmployeeId()) && updatedDocument[reviewer2SignatureIndex] != null)
        {
            // 2nd Reviewer as they are not a line manager
            currentDocument[reviewer2SignatureIndex] = updatedDocument[reviewer2SignatureIndex];
        }

        // Overwrite any changes
        if (!justSigning)
        {
            for (int i = meetingDateIndex; i < currentDocument.length - 1; i++)
            {
                currentDocument[i] = updatedDocument[i];
            }
        }

        if (AuthorisationAttempt(Action.Update, "Performance Review", user, docId))
        {
            return dp.updateReview(docId, currentDocument);
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
                        if (user.getDepartment().equals(...)  )
                        {
                            if (payload[0] != null && dp.checkEmployeeId(payload[2]) && dp.checkEmployeeId(payload[3]))
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
                    break;

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
                            if (dp.isReviewee(payload[0], user.getEmployeeId()) || dp.isReviewer(payload[0], user.getEmployeeId()) || user.getDepartment().equals(Position.Department.HR)))
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
                    break;

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
                                String[] content = dp.readReview(payload[0]);
                                // Has this already been signed off?
                                if (content[5].equals("true") && content[6].equals("true") && content[7].equals("true"))
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
                        dp.recordAuthorisationAttempt(user.getEmployeeId(), action.toString(), null, null);
                        return false;
                    }
                    break;

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
