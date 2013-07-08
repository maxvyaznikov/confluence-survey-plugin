package org.hivesoft.confluence.macros.vote.model;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class BallotTest {

    private static final String SOME_BALLOT_TITLE = "someBallotTitle";
    private static final String SOME_CHOICE_DESCRIPTION = "someChoice";
    private static final String SOME_EXISTING_USER_NAME = "someExistingUser";

    @Before
    public void setup() {
    }

    @Test
    public void test_getTitle_success() {
        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);

        assertEquals(SOME_BALLOT_TITLE, classUnderTest.getTitle());
    }

    @Test
    public void test_getDescription_success() {
        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);

        assertEquals("", classUnderTest.getDescription());

        classUnderTest.setDescription("someDescription");
        assertEquals("someDescription", classUnderTest.getDescription());
    }

    @Test
    public void test_isChangeableVotes_DefaultFalse_success() {
        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);

        assertEquals(false, classUnderTest.isChangeableVotes());
    }

    @Test
    public void test_isChangeableVotes_SetTrue_success() {
        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);
        classUnderTest.setChangeableVotes(true);

        assertEquals(true, classUnderTest.isChangeableVotes());
    }

    @Test
    public void test_isVisibleVoters_DefaultFalse_success() {
        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);

        assertEquals(false, classUnderTest.isVisibleVoters());
    }

    @Test
    public void test_isVisibleVoters_SetTrue_success() {
        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);
        classUnderTest.setVisibleVoters(true);

        assertEquals(true, classUnderTest.isVisibleVoters());
    }

    @Test
    public void test_getVoteForExistingUser_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
        someChoice.voteFor(SOME_EXISTING_USER_NAME);

        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);
        classUnderTest.addChoice(someChoice);

        Choice result = classUnderTest.getVote(SOME_EXISTING_USER_NAME);

        assertEquals(someChoice, result);
        assertTrue(classUnderTest.getHasVoted(SOME_EXISTING_USER_NAME));
    }

    @Test
    public void test_getVoteForNotExistingUser_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
        someChoice.voteFor(SOME_EXISTING_USER_NAME);

        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);
        classUnderTest.addChoice(someChoice);

        Choice result = classUnderTest.getVote("someDifferentNotExistingUser");

        assertTrue(null == result);
        assertFalse(classUnderTest.getHasVoted("someDifferentNotExistingUser"));
    }

    @Test
    public void test_getChoice_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);

        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);
        classUnderTest.addChoice(someChoice);

        Choice result = classUnderTest.getChoice(SOME_CHOICE_DESCRIPTION);

        assertEquals(someChoice, result);
    }

    @Test
    public void test_getChoice_NotExists_failure() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);

        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);
        classUnderTest.addChoice(someChoice);

        Choice result = classUnderTest.getChoice("NotExistingChoice");

        assertNull(result);
    }

    @Test
    public void test_getChoices_NoChoices_failure() {
        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);

        Choice[] result = classUnderTest.getChoices();

        assertNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_AddComment_NoUser_exception() {
        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);

        Comment someComment = new Comment();

        classUnderTest.addComment(someComment);
    }

    @Test
    public void test_Comments_success() {
        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);

        Comment someComment = new Comment(SOME_EXISTING_USER_NAME, "some crazy comment for a crazy plugin");

        classUnderTest.addComment(someComment);

        final Map<String, Comment> result = classUnderTest.getComments();

        assertEquals(1, result.size());
        assertEquals(someComment, result.get(someComment.getUsername()));
    }

    @Test
    public void test_getCommentForUser_success() {
        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);

        Comment someComment = new Comment(SOME_EXISTING_USER_NAME, "some crazy comment for a crazy plugin");

        classUnderTest.addComment(someComment);

        final Comment result = classUnderTest.getCommentForUser(SOME_EXISTING_USER_NAME);

        assertEquals(someComment, result);
    }

    @Test
    public void test_computeAverage_OneChoiceNoVotes_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);

        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);
        classUnderTest.addChoice(someChoice);

        final float result = classUnderTest.computeAverage();

        assertEquals(0.0f, result, 0.0f);
    }

    @Test
    public void test_computeAverage_OneChoiceOneVote_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
        someChoice.voteFor(SOME_EXISTING_USER_NAME);

        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);
        classUnderTest.addChoice(someChoice);

        final float result = classUnderTest.computeAverage();

        assertEquals(1.0f, result, 0.0f);
    }

    @Test
    public void test_computeAverage_ThreeChoicesOneVoteOnSecond_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
        Choice someChoiceTwo = new Choice(SOME_CHOICE_DESCRIPTION + "TWO");
        someChoiceTwo.voteFor(SOME_EXISTING_USER_NAME + "TWO");
        Choice someChoiceThree = new Choice(SOME_CHOICE_DESCRIPTION + "THREE");

        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);
        classUnderTest.addChoice(someChoice);
        classUnderTest.addChoice(someChoiceTwo);
        classUnderTest.addChoice(someChoiceThree);

        final float result = classUnderTest.computeAverage();

        assertEquals(2.0f, result, 0.0f);
    }

    @Test
    public void test_computeFormattedAverage_NegativeIterateStep_ThreeChoicesTwoVotes_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
        Choice someChoiceTwo = new Choice(SOME_CHOICE_DESCRIPTION + "TWO");
        someChoiceTwo.voteFor(SOME_EXISTING_USER_NAME + "TWO");
        Choice someChoiceThree = new Choice(SOME_CHOICE_DESCRIPTION + "THREE");
        someChoiceThree.voteFor(SOME_EXISTING_USER_NAME + "THREE");

        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);
        classUnderTest.addChoice(someChoice);
        classUnderTest.addChoice(someChoiceTwo);
        classUnderTest.addChoice(someChoiceThree);
        classUnderTest.setStartBound(-1);
        classUnderTest.setIterateStep(-3);

        final String result = classUnderTest.computeFormatedAverage("0.##");

        assertEquals("-2.5", result);
    }
}
