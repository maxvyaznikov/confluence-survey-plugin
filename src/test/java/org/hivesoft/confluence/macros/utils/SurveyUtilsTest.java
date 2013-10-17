package org.hivesoft.confluence.macros.utils;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.renderer.v2.macro.MacroException;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.macros.vote.model.Choice;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurveyUtilsTest {

    public static final String BALLOT_AND_CHOICENAME1 = "someBallot.withSomeChoiceName1";
    public static final String BALLOT_AND_CHOICENAME2 = "someBallot.withSomeChoiceName2";
    public static final String BALLOT_AND_CHOICENAME3 = "someBallot.withSomeChoiceName3";

    public static final String SOME_BALLOT = "some Ballot";
    public static final String SOME_CHOICE = "some Choice";
    public static final String SOME_USER_NAME = "john doe";

    UserAccessor mockUserAccessor = mock(UserAccessor.class);

    @Test
    public void test_validateMaxStorableKeyLength_success() throws MacroException {
        List<String> ballotAndChoicesWithValidLength = new ArrayList<String>();

        ballotAndChoicesWithValidLength.add(BALLOT_AND_CHOICENAME1);
        ballotAndChoicesWithValidLength.add(BALLOT_AND_CHOICENAME2);
        ballotAndChoicesWithValidLength.add(BALLOT_AND_CHOICENAME3);

        SurveyUtils.validateMaxStorableKeyLength(ballotAndChoicesWithValidLength);
    }

    @Test(expected = MacroException.class)
    public void test_validateMaxStorableKeyLength_exception() throws MacroException {
        List<String> ballotAndChoicesWithValidLength = new ArrayList<String>();

        ballotAndChoicesWithValidLength.add(getRandomString(SurveyUtils.MAX_STORABLE_KEY_LENGTH + 1));
        ballotAndChoicesWithValidLength.add(getRandomString(SurveyUtils.MAX_STORABLE_KEY_LENGTH));

        SurveyUtils.validateMaxStorableKeyLength(ballotAndChoicesWithValidLength);
    }

    @Test
    public void test_getBooleanFromString_success() {
        assertTrue(SurveyUtils.getBooleanFromString("true", false));
        assertFalse(SurveyUtils.getBooleanFromString("false", true));
        assertTrue(SurveyUtils.getBooleanFromString("", true));
        assertFalse(SurveyUtils.getBooleanFromString(null, false));
    }

    @Test
    public void test_getCanPerformAction_Anonymous_failure() {
        final Boolean canPerformAction = SurveyUtils.getCanPerformAction(mockUserAccessor, "", "");
        assertEquals(Boolean.FALSE, canPerformAction);
    }

    @Test
    public void test_getCanPerformAction_KnownNotRestricted_success() {
        final Boolean canPerformAction = SurveyUtils.getCanPerformAction(mockUserAccessor, "", "KnownUser");
        assertEquals(Boolean.TRUE, canPerformAction);
    }

    @Test
    public void test_getCanPerformAction_isInList_success() {
        final Boolean canPerformAction = SurveyUtils.getCanPerformAction(mockUserAccessor, "KnownUser, AnotherUser", "KnownUser");
        assertEquals(Boolean.TRUE, canPerformAction);
    }

    @Test
    public void test_getCanPerformAction_notIsInList_success() {
        final Boolean canPerformAction = SurveyUtils.getCanPerformAction(mockUserAccessor, "NotThisUser, AnotherUser", "KnownUser");
        assertEquals(Boolean.FALSE, canPerformAction);
    }

    @Test
    public void test_getCanPerformAction_isMemberInGroup_success() {
        when(mockUserAccessor.hasMembership("IsAGroup", "KnownUser")).thenReturn(true);
        final Boolean canPerformAction = SurveyUtils.getCanPerformAction(mockUserAccessor, "IsAGroup, AnotherUser", "KnownUser");
        assertEquals(Boolean.TRUE, canPerformAction);
    }

    @Test
    public void test_canSeeResults_NoUserFound_success() {
        final Boolean canSeeResults = SurveyUtils.getCanSeeResults(mockUserAccessor, null, null, "", null);
        assertFalse(canSeeResults);
    }

    @Test
    public void test_canSeeResults_noRestrictionsButHasNotVotedYet_success() {
        final Boolean canSeeResults = SurveyUtils.getCanSeeResults(mockUserAccessor, "", "", SOME_USER_NAME, createDefaultBallot());
        assertFalse(canSeeResults);
    }

    @Test
    public void test_canSeeResults_noRestrictionsAndHasVoted_success() {
        final Ballot defaultBallot = createDefaultBallot();
        defaultBallot.getChoices().iterator().next().voteFor(SOME_USER_NAME);
        final Boolean canSeeResults = SurveyUtils.getCanSeeResults(mockUserAccessor, "", "", SOME_USER_NAME, defaultBallot);
        assertTrue(canSeeResults);
    }

    @Test
    public void test_canSeeResults_notInListOfViewers_success() {
        final Ballot defaultBallot = createDefaultBallot();
        final Boolean canSeeResults = SurveyUtils.getCanSeeResults(mockUserAccessor, "notThisUser, notThisUserEither", "", SOME_USER_NAME, defaultBallot);
        assertFalse(canSeeResults);
    }

    @Test
    public void test_canSeeResults_inListOfViewersButNotYetVoted_success() {
        final Ballot defaultBallot = createDefaultBallot();
        final Boolean canSeeResults = SurveyUtils.getCanSeeResults(mockUserAccessor, SOME_USER_NAME + ", notThisUserEither", "", SOME_USER_NAME, defaultBallot);
        assertFalse(canSeeResults);
    }

    @Test
    public void test_canSeeResults_inListOfViewersAndHasVoted_success() {
        final Ballot defaultBallot = createDefaultBallot();
        defaultBallot.getChoices().iterator().next().voteFor(SOME_USER_NAME);
        final Boolean canSeeResults = SurveyUtils.getCanSeeResults(mockUserAccessor, SOME_USER_NAME + ", notThisUserEither", "", SOME_USER_NAME, defaultBallot);
        assertTrue(canSeeResults);
    }

    @Test
    public void test_canSeeResults_inListOfViewersButNotInListOfVoters_success() {
        final Ballot defaultBallot = createDefaultBallot();
        final Boolean canSeeResults = SurveyUtils.getCanSeeResults(mockUserAccessor, SOME_USER_NAME + ", notThisUserEither", "notThisUser", SOME_USER_NAME, defaultBallot);
        assertTrue(canSeeResults);
    }

    @Test
    public void test_canSeeResults_inListOfViewersAndInListOfVotersButHasNotVoted_success() {
        final Ballot defaultBallot = createDefaultBallot();
        final Boolean canSeeResults = SurveyUtils.getCanSeeResults(mockUserAccessor, SOME_USER_NAME + ", notThisUserEither", SOME_USER_NAME + ", notThisUser", SOME_USER_NAME, defaultBallot);
        assertFalse(canSeeResults);
    }

    @Test
    public void test_canSeeResults_inListOfViewersViaGroupMembershipAndHasVoted_success() {
        when(mockUserAccessor.hasMembership("someGroup", SOME_USER_NAME)).thenReturn(true);
        final Ballot defaultBallot = createDefaultBallot();
        defaultBallot.getChoices().iterator().next().voteFor(SOME_USER_NAME);
        final Boolean canSeeResults = SurveyUtils.getCanSeeResults(mockUserAccessor, "someGroup, notThisUserEither", "", SOME_USER_NAME, defaultBallot);
        assertTrue(canSeeResults);
    }

    @Test
    public void test_canSeeResults_inListOfViewersViaGroupMembershipAndVotersGroupAndHasVoted_success() {
        when(mockUserAccessor.hasMembership("someGroup", SOME_USER_NAME)).thenReturn(true);
        final Ballot defaultBallot = createDefaultBallot();
        defaultBallot.getChoices().iterator().next().voteFor(SOME_USER_NAME);
        final Boolean canSeeResults = SurveyUtils.getCanSeeResults(mockUserAccessor, "someGroup, notThisUserEither", "someGroup", SOME_USER_NAME, defaultBallot);
        assertTrue(canSeeResults);
    }

    @Test
    public void test_getCanSeeVoters_success() {
        //cant see results
        assertFalse(SurveyUtils.getCanSeeVoters("true", false));
        //visibleVoters Parameter null
        assertFalse(SurveyUtils.getCanSeeVoters(null, true));
        //visibleVoters Parameter not "true"
        assertFalse(SurveyUtils.getCanSeeVoters("something", true));
        //visibleVoters Parameter "true"
        assertTrue(SurveyUtils.getCanSeeVoters("true", true));
    }

    //****** Helper Methods ******
    private static String getRandomString(int length) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            sb.append((char) ((int) (Math.random() * 26) + 97));
        }
        return sb.toString();
    }

    public static Ballot createDefaultBallot() {
        Ballot ballot = new Ballot(SOME_BALLOT);
        Choice choice = new Choice(SOME_CHOICE);
        ballot.addChoice(choice);
        return ballot;
    }
}