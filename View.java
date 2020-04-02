import java.util.ArrayList;

public class View
{
    static DatabaseParser dp = new DatabaseParser();
    /**
     * Gets all reviews currently in the Database. Only to be used by HR Staff
     * @param user The currently logged in member of HR
     * @return An ArrayList containing all the keys required to fetch a review
     */
    static ArrayList<String[]> getAllReviews(User user)
    {
        if (user.getDepartment().equals(Position.Department.HR))
        {
            return dp.fetchAllReviewKeys();
        }
        else
        {
            dp.recordAuthorisationAttempt(user.getEmployeeId(), Authorise.Action.Read.toString(), "UI Restraint error - tried loading all Reviews", false);
            return null;
        }
    }

    /**
     * Returns the keys for last year's Review for a given Employee
     * @param user the user logged in performing the action
     * @param employeeId the employee in question of who's review shall be returned
     * @return the Document ID for the Review wanting to be accessed
     */
    static String getPreviousReview(User user, String employeeId)
    {
        if (user.getDepartment().equals(Position.Department.HR) || user.getEmployeeId().equals(employeeId))
        {
            ArrayList<String[]> allKeys = dp.fetchAllReviewKeys();
            ArrayList<String[]> empIdsReviews = new ArrayList<>();
            for (int i = 0; i < allKeys.size(); i++)
            {
                if (allKeys.get(i)[0].equals(employeeId))
                {
                    empIdsReviews.add(allKeys.get(i));
                }
            }

            int currentYear = 0;
            for (int i = 0; i < empIdsReviews.size(); i++)
            {
                if (Integer.parseInt(empIdsReviews.get(i)[1].substring(0, 4)) > currentYear)
                {
                    currentYear = Integer.parseInt(empIdsReviews.get(i)[1].substring(0, 4));
                }
            }
            for (int i = 0; i < empIdsReviews.size(); i++)
            {
                if (Integer.parseInt(empIdsReviews.get(i)[1].substring(0, 4)) == (currentYear -1))
                {
                    return dp.fetchReviewDocumentId(empIdsReviews.get(i)[0], empIdsReviews.get(i)[1]);
                }
            }
        }
        return null;
    }

    /**
     * Gets all completed reviews currently in the Database. Only to be used by HR Staff
     * @param user The currently logged in member of HR
     * @return An ArrayList containing all the main elements of a completed review
     */
    static ArrayList<String[]> getAllCompletedReviewKeys(User user)
    {
        if (user.getDepartment().equals(Position.Department.HR))
        {
            ArrayList<String[]> allReviews = dp.fetchAllReviewKeys();
            ArrayList<String[]> completedReviewKeys = new ArrayList<>();
            for(int i = 0; i < allReviews.size(); i++)
            {
                String[] currentKeys = allReviews.get(i);
                String[] current = dp.fetchReview(dp.fetchReviewDocumentId(currentKeys[0], currentKeys[1]));
                if (current[5] != null && current[6] != null && current[7] != null)
                {
                    completedReviewKeys.add(currentKeys);
                }
            }
            return completedReviewKeys;
        }
        else
        {
            dp.recordAuthorisationAttempt(user.getEmployeeId(), Authorise.Action.Read.toString(), "UI Restraint error - tried loading all completed Reviews", false);
            return null;
        }
    }

    /**
     * Returns all reviews where a User is registered as a Reviewer (only to be used by managers or above)
     * @param user the User object that is performing this action
     * @return an ArrayList containing all the keys required to fetch a certain review
     */
    static ArrayList<String[]> getReviewsAsReviewer(User user)
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
            dp.recordAuthorisationAttempt(user.getEmployeeId(), Authorise.Action.Read.toString(), "UI Error - tried accessing all reviews where User is a Reviewer", false);
        }
        return reviewerReviews;
    }

    /**
     * Returns all reviews of a certain User
     * @param user the User currently logged in, trying to read their own reviews
     * @return an ArrayList containing all the keys fetch all relevant reviews
     */
    static ArrayList<String[]> getReviewsAsReviewee(User user)
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

    /**
     * Gets all reviews currently in the Database. Only to be used by HR Staff
     * @param user The currently logged in member of HR
     * @return An ArrayList containing all the keys required to fetch a review
     */
    static ArrayList<String> getAllUsers(User user)
    {
        if (user.getDepartment().equals(Position.Department.HR))
        {
            return dp.fetchAllUsers();
        }
        else
        {
            dp.recordAuthorisationAttempt(user.getEmployeeId(), Authorise.Action.Read.toString(), "UI Restraint error - tried loading all Users", false);
            return null;
        }
    }

    /**
     * Returns the full name of a given employee based on their name
     * @param employeeId the employeeId of the user
     * @return an array in form of [0] First Name [1] Last name
     */
    static String[] getUserName(String employeeId)
    {
        return new String[]{ dp.fetchPersonalDetails(employeeId)[2], dp.fetchPersonalDetails(employeeId)[1]};
    }

    /**
     * Returns the Department Enum of a certain Employee
     * @param employeeId the employeeID for which the department is to be gotten
     * @return the Department Enum. Use Position.Department.label to get its proper String value
     */
    static Position.Department getDepartment(String employeeId)
    {
        return dp.fetchDepartment(employeeId);
    }

    /**
     * Returns the Role Enum of a certain Employee
     * @param employeeId the employeeID for which the role is to be gotten
     * @return the Role Enum. Use Position.Role.label to get its proper String value
     */
    static Position.Role getRole(String employeeId)
    {
        return dp.fetchRole(employeeId);
    }

    /**
     * Returns the Direct Supervisor of a certain Employee
     * @param employeeId the employeeID for which the supervisor is to be gotten
     * @return the employeeId of that supervisor
     */
    static String getDirectSupervisor(String employeeId)
    {
        return dp.fetchDirectSupervisor(employeeId);
    }

    /**
     * Returns an array containing the information stored in a Review record
     * @param revieweeId the employeeId of the user who's review is trying to be accessed (makes composite key)
     * @param dueBy the year of the review for that employeeId that is trying to be accessed (makes composite key)
     * @return the documentID
     */
    public static String getReviewDocId(String revieweeId, String dueBy)
    {
        String docId = dp.fetchReviewDocumentId(revieweeId, dueBy);
        return docId;
    }
}
