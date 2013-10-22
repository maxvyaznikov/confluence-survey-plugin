package org.hivesoft.confluence.macros.utils;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.user.UserManager;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PermissionEvaluatorTest {
    public static final String SOME_USER_NAME = "john doe";

    UserAccessor mockUserAccessor = mock(UserAccessor.class);
    UserManager mockUserManager = mock(UserManager.class);

    PermissionEvaluator classUnderTest;

    @Before
    public void setup() {
        classUnderTest = new PermissionEvaluator(mockUserAccessor, mockUserManager);
    }

    @Test
    public void test_getCanPerformAction_Anonymous_failure() {
        final Boolean canPerformAction = classUnderTest.getCanPerformAction("", "");
        assertEquals(Boolean.FALSE, canPerformAction);
    }

    @Test
    public void test_getCanPerformAction_KnownNotRestricted_success() {
        final Boolean canPerformAction = classUnderTest.getCanPerformAction("", "KnownUser");
        assertEquals(Boolean.TRUE, canPerformAction);
    }

    @Test
    public void test_getCanPerformAction_isInList_success() {
        final Boolean canPerformAction = classUnderTest.getCanPerformAction("KnownUser, AnotherUser", "KnownUser");
        assertEquals(Boolean.TRUE, canPerformAction);
    }

    @Test
    public void test_getCanPerformAction_notIsInList_success() {
        final Boolean canPerformAction = classUnderTest.getCanPerformAction("NotThisUser, AnotherUser", "KnownUser");
        assertEquals(Boolean.FALSE, canPerformAction);
    }

    @Test
    public void test_getCanPerformAction_isMemberInGroup_success() {
        when(mockUserAccessor.hasMembership("IsAGroup", "KnownUser")).thenReturn(true);
        final Boolean canPerformAction = classUnderTest.getCanPerformAction("IsAGroup, AnotherUser", "KnownUser");
        assertEquals(Boolean.TRUE, canPerformAction);
    }

    @Test
    public void test_canSeeResults_NoUserFound_success() {
        final Boolean canSeeResults = classUnderTest.getCanSeeResults(null, null, "", null);
        assertFalse(canSeeResults);
    }

    @Test
    public void test_canSeeResults_noRestrictionsButHasNotVotedYet_success() {
        final Boolean canSeeResults = classUnderTest.getCanSeeResults("", "", SOME_USER_NAME, SurveyUtilsTest.createDefaultBallot());
        assertFalse(canSeeResults);
    }

    @Test
    public void test_canSeeResults_noRestrictionsAndHasVoted_success() {
        final Ballot defaultBallot = SurveyUtilsTest.createDefaultBallot();
        defaultBallot.getChoices().iterator().next().voteFor(SOME_USER_NAME);
        final Boolean canSeeResults = classUnderTest.getCanSeeResults("", "", SOME_USER_NAME, defaultBallot);
        assertTrue(canSeeResults);
    }

    @Test
    public void test_canSeeResults_notInListOfViewers_success() {
        final Ballot defaultBallot = SurveyUtilsTest.createDefaultBallot();
        final Boolean canSeeResults = classUnderTest.getCanSeeResults("notThisUser, notThisUserEither", "", SOME_USER_NAME, defaultBallot);
        assertFalse(canSeeResults);
    }

    @Test
    public void test_canSeeResults_inListOfViewersButNotYetVoted_success() {
        final Ballot defaultBallot = SurveyUtilsTest.createDefaultBallot();
        final Boolean canSeeResults = classUnderTest.getCanSeeResults(SOME_USER_NAME + ", notThisUserEither", "", SOME_USER_NAME, defaultBallot);
        assertFalse(canSeeResults);
    }

    @Test
    public void test_canSeeResults_inListOfViewersAndHasVoted_success() {
        final Ballot defaultBallot = SurveyUtilsTest.createDefaultBallot();
        defaultBallot.getChoices().iterator().next().voteFor(SOME_USER_NAME);
        final Boolean canSeeResults = classUnderTest.getCanSeeResults(SOME_USER_NAME + ", notThisUserEither", "", SOME_USER_NAME, defaultBallot);
        assertTrue(canSeeResults);
    }

    @Test
    public void test_canSeeResults_inListOfViewersButNotInListOfVoters_success() {
        final Ballot defaultBallot = SurveyUtilsTest.createDefaultBallot();
        final Boolean canSeeResults = classUnderTest.getCanSeeResults(SOME_USER_NAME + ", notThisUserEither", "notThisUser", SOME_USER_NAME, defaultBallot);
        assertTrue(canSeeResults);
    }

    @Test
    public void test_canSeeResults_inListOfViewersAndInListOfVotersButHasNotVoted_success() {
        final Ballot defaultBallot = SurveyUtilsTest.createDefaultBallot();
        final Boolean canSeeResults = classUnderTest.getCanSeeResults(SOME_USER_NAME + ", notThisUserEither", SOME_USER_NAME + ", notThisUser", SOME_USER_NAME, defaultBallot);
        assertFalse(canSeeResults);
    }

    @Test
    public void test_canSeeResults_inListOfViewersViaGroupMembershipAndHasVoted_success() {
        when(mockUserAccessor.hasMembership("someGroup", SOME_USER_NAME)).thenReturn(true);
        final Ballot defaultBallot = SurveyUtilsTest.createDefaultBallot();
        defaultBallot.getChoices().iterator().next().voteFor(SOME_USER_NAME);
        final Boolean canSeeResults = classUnderTest.getCanSeeResults("someGroup, notThisUserEither", "", SOME_USER_NAME, defaultBallot);
        assertTrue(canSeeResults);
    }

    @Test
    public void test_canSeeResults_inListOfViewersViaGroupMembershipAndVotersGroupAndHasVoted_success() {
        when(mockUserAccessor.hasMembership("someGroup", SOME_USER_NAME)).thenReturn(true);
        final Ballot defaultBallot = SurveyUtilsTest.createDefaultBallot();
        defaultBallot.getChoices().iterator().next().voteFor(SOME_USER_NAME);
        final Boolean canSeeResults = classUnderTest.getCanSeeResults("someGroup, notThisUserEither", "someGroup", SOME_USER_NAME, defaultBallot);
        assertTrue(canSeeResults);
    }

    @Test
    public void test_getCanSeeVoters_success() {
        //cant see results
        assertFalse(classUnderTest.getCanSeeVoters("true", false));
        //visibleVoters Parameter null
        assertFalse(classUnderTest.getCanSeeVoters(null, true));
        //visibleVoters Parameter not "true"
        assertFalse(classUnderTest.getCanSeeVoters("something", true));
        //visibleVoters Parameter "true"
        assertTrue(classUnderTest.getCanSeeVoters("true", true));
    }
}
