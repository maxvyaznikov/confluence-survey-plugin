package org.hivesoft.confluence.macros.utils;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.user.UserManager;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.vote.model.Ballot;

import java.util.Arrays;

public class PermissionEvaluator {

    final UserAccessor userAccessor;
    final UserManager userManager;

    public PermissionEvaluator(UserAccessor userAccessor, UserManager userManager) {
        this.userAccessor = userAccessor;
        this.userManager = userManager;
    }

    public String getRemoteUsername() {
        return userManager.getRemoteUsername();
    }

    /**
     * Determine if a user is authorized to perform an action based on the provided list of names.
     *
     * @param permitted the list of usernames allowed to perform the action. If blank, everyone is permitted.
     * @param username  the username of the candidate user.
     * @return <code>true</code> if the user can see the results, <code>false</code> if they cannot.
     */
    public Boolean getCanPerformAction(String permitted, String username) {
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
    public Boolean getCanSeeResults(String viewers, String voters, String username, Ballot ballot) {
        // You can't see results if we don't know who you are
        if (StringUtils.isBlank(username)) {
            return Boolean.FALSE;
        }

        // If you're not a viewer, you can't see results
        boolean isViewer = StringUtils.isBlank(viewers) || Arrays.asList(viewers.split(",")).contains(username);
        if (!isViewer) {
            // 1.1.7.2: next try one of the entries is a group. Check whether the user is in this group!
            for (String currentUser : viewers.split(",")) {
                // guess the current element is a group /if (com.atlassian.confluence.user.DefaultUserAccessor.hasMembership(lUsers[ino], username))
                if (userAccessor.hasMembership(currentUser, username)) {
                    isViewer = true;
                    break;
                }
            }
            if (!isViewer)
                return Boolean.FALSE;
        }

        // If you're a viewer but not a voter, then you can always see the results
        boolean isVoter = StringUtils.isBlank(voters) || Arrays.asList(voters.split(",")).contains(username);
        if (!isVoter) {
            // 1.1.7.2: next try one of the entries is a group. Check whether the user is in this group!
            for (String currentUser : voters.split(",")) {
                // guess the current element is a group /if (com.atlassian.confluence.user.DefaultUserAccessor.hasMembership(lUsers[ino], username))
                if (userAccessor.hasMembership(currentUser, username)) {
                    isVoter = true;
                    break;
                }
            }
            if (!isVoter)
                return Boolean.TRUE;
        }

        // If you are a voter, then you have to vote to see the results
        return Boolean.valueOf(ballot.getHasVoted(username));
    }

    public Boolean getCanSeeVoters(String visibleVoters, Boolean canSeeResults) {
        if (canSeeResults == Boolean.FALSE)
            return Boolean.FALSE;
        if (StringUtils.isBlank(visibleVoters))
            return Boolean.FALSE;
        if ("true".equals(visibleVoters))
            return Boolean.TRUE;
        return Boolean.FALSE;
    }

    /**
     * Determine if a user is authorized to cast a vote, taking into account whether they are a voter (either explicitly or implicitly)
     * and whether or not they have already cast a vote. Only logged in users can vote.
     *
     * @param voters   The list of usernames allowed to vote. If blank, all users can vote.
     * @param username the username of the user about to see the ballot.
     * @param ballot   the ballot that is about to be shown.
     * @return <code>true</code> if the user can cast a vote, <code>false</code> if they cannot.
     */
    public Boolean getCanVote(String voters, String username, Ballot ballot) {
        if (!TextUtils.stringSet(username)) {
            return Boolean.FALSE;
        }

        boolean isVoter = !TextUtils.stringSet(voters) || Arrays.asList(voters.split(",")).contains(username);
        if (!isVoter) { // user is not permitted via username
            // 1.1.7.2: next try one of the entries is a group. Check whether the user is in this group!
            for (String currentUser : voters.split(",")) {
                // guess the current element is a group /if (com.atlassian.confluence.user.DefaultUserAccessor.hasMembership(lUsers[ino], username))
                if (userAccessor.hasMembership(currentUser, username)) {
                    isVoter = true;
                    break;
                }
            }

            if (!isVoter) // user is not permitted via groupname either
                return Boolean.FALSE;
        }

        return Boolean.valueOf(!ballot.getHasVoted(username) || ballot.isChangeableVotes());
    }
}
