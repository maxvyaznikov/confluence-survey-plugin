package org.hivesoft.confluence.macros.utils;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.renderer.v2.macro.MacroException;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.vote.VoteMacro;
import org.hivesoft.confluence.macros.vote.model.Ballot;

import java.util.Arrays;
import java.util.List;

public class SurveyUtils {
    private static final Logger.Log LOG = Logger.getInstance(SurveyUtils.class);

    // 1.1.7.7 define the max length that is storable to the propertyEntry-key field
    protected static final int MAX_STORABLE_KEY_LENGTH = 200;

    public static void validateMaxStorableKeyLength(List<String> ballotAndChoiceNames) throws MacroException {
        // 1.1.7.7 ballot title and choices too long will crash the system if exceeding 200 chars for entity_key. So check this on rendering
        String strExceedsKeyItems = "";
        for (String ballotChoiceKey : ballotAndChoiceNames) {
            try {
                // 1.1.7.8 check for unicode-characters. They consume more space than they sometimes are allowed. add 5 to the calculated length (prefix for vote)
                if (ballotChoiceKey.getBytes("UTF-8").length + VoteMacro.VOTE_PREFIX.length() > MAX_STORABLE_KEY_LENGTH) {
                    if (strExceedsKeyItems == "")
                        strExceedsKeyItems += ", ";
                    strExceedsKeyItems += ballotChoiceKey + " Length: " + (ballotChoiceKey.getBytes("UTF-8").length + VoteMacro.VOTE_PREFIX.length());
                }
            } catch (java.io.UnsupportedEncodingException e) {
                throw new MacroException(e);
            }
        }

        if (strExceedsKeyItems != "") {
            final String message = "Error detected Length of BallotTitle and Choices are to long to be stored to the database (MaxLength:" + MAX_STORABLE_KEY_LENGTH + "). Problematic Names: " + strExceedsKeyItems + "!";
            LOG.error(message);
            throw new MacroException(message);
        }
    }

    /**
     * Get the boolean value of a String and fallback to the defaultValue if its not a boolean
     */
    public static boolean getBooleanFromString(String stringToParse, boolean defaultValue) {
        if (StringUtils.defaultString(stringToParse).equals("")) {
            return defaultValue;
        } else {
            return Boolean.valueOf(stringToParse);
        }
    }

    /**
     * Determine if a user is authorized to perform an action based on the provided list of names.
     *
     * @param permitted the list of usernames allowed to perform the action. If blank, everyone is permitted.
     * @param username  the username of the candidate user.
     * @return <code>true</code> if the user can see the results, <code>false</code> if they cannot.
     */
    public static Boolean getCanPerformAction(UserAccessor userAccessor, String permitted, String username) {
        if (StringUtils.isBlank(username)) {
            return Boolean.FALSE;
        }

        if (StringUtils.isBlank(permitted)) {
            return Boolean.TRUE;
        }

        String[] permittedList = permitted.split(",");
        // if the logged in user matches a entry, it is granted
        if (Arrays.asList(permittedList).contains(username)) {
            return Boolean.TRUE;
        }

        // 1.1.7.2: next try one of the entries is a group. Check whether the user is in this group!
        for (String permittedElement : permittedList) {
            if (userAccessor.hasMembership(permittedElement, username)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /**
     * <p>
     * Determine if a user is authorized to see the vote results. A user is only eligible to see results if they are specified as a viewer either implicitly (no viewers are specified) or explicitly
     * (the user is specified in the viewers parameter. A user who is specified as a viewer can see the results in two cases:
     * <ol>
     * <li>The user is not allowed to vote.</li>
     * <li>The user is allowed to vote and has already cast a vote.</li>
     * </ol>
     * </p>
     *
     * @param viewers  The list of userNames allowed to see the results after voting. If blank, all users can see results.
     * @param voters   The list of userNames allowed to vote. If blank, all users can vote.
     * @param username The username of the user about to see results.
     * @param ballot   The ballot whose results are about to be shown.
     * @return <code>true</code> if the user can see the results, <code>false</code> if they cannot.
     */
    public static Boolean getCanSeeResults(UserAccessor userAccessor, String viewers, String voters, String username, Ballot ballot) {
        // You can't see results if we don't know who you are
        if (StringUtils.isBlank(username)) {
            return Boolean.FALSE;
        }

        // If you're not a viewer, you can't see results
        boolean isViewer = StringUtils.isBlank(viewers) || Arrays.asList(viewers.split(",")).contains(username);
        if (!isViewer) {
            // 1.1.7.2: next try one of the entries is a group. Check whether the user is in this group!
            String[] lUsers = viewers.split(",");
            for (int ino = 0; ino < lUsers.length; ino++) {
                // guess the current element is a group /if (com.atlassian.confluence.user.DefaultUserAccessor.hasMembership(lUsers[ino], username))
                if (userAccessor.hasMembership(lUsers[ino], username)) {
                    isViewer = true;
                    ino = lUsers.length; // end the iteration
                }
            }
            if (!isViewer)
                return Boolean.FALSE;
        }

        // If you're a viewer but not a voter, then you can always see the results
        boolean isVoter = StringUtils.isBlank(voters) || Arrays.asList(voters.split(",")).contains(username);
        if (!isVoter) {
            // 1.1.7.2: next try one of the entries is a group. Check whether the user is in this group!
            String[] lUsers = voters.split(",");
            for (int ino = 0; ino < lUsers.length; ino++) {
                // guess the current element is a group /if (com.atlassian.confluence.user.DefaultUserAccessor.hasMembership(lUsers[ino], username))
                if (userAccessor.hasMembership(lUsers[ino], username)) {
                    isVoter = true;
                    ino = lUsers.length; // end the iteration
                }
            }
            if (!isVoter)
                return Boolean.TRUE;
        }

        // If you are a voter, then you have to vote to see the results
        return Boolean.valueOf(ballot.getHasVoted(username));
    }

    public static Boolean getCanSeeVoters(String visibleVoters, Boolean canSeeResults) {
        if (canSeeResults == Boolean.FALSE)
            return Boolean.FALSE;
        if (StringUtils.isBlank(visibleVoters))
            return Boolean.FALSE;
        if ("true".equals(visibleVoters))
            return Boolean.TRUE;
        return Boolean.FALSE;
    }
}
