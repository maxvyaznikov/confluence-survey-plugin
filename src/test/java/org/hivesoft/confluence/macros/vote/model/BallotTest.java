package org.hivesoft.confluence.macros.vote.model;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.*;

public class BallotTest {

    private static final String SOME_BALLOT_TITLE = "someBallotTitle";
    private static final String SOME_CHOICE_DESCRIPTION = "someChoice";
    private static final String SOME_EXISTING_USER_NAME = "someExistingUser";

    Ballot classUnderTest;


    @Before
    public void setup() {
        classUnderTest = new Ballot(SOME_BALLOT_TITLE);
    }

    @Test
    public void test_getTitle_success() {
        assertEquals(SOME_BALLOT_TITLE, classUnderTest.getTitle());
    }

    @Test
    public void test_getTitleNoSpace_success() {
        classUnderTest = new Ballot(SOME_BALLOT_TITLE + " " + " more spaces");

        assertEquals(SOME_BALLOT_TITLE.toLowerCase() + "morespaces", classUnderTest.getTitleNoSpace());
    }

    @Test
    public void test_getDescription_success() {
        assertEquals("", classUnderTest.getDescription());

        classUnderTest.setDescription("someDescription");
        assertEquals("someDescription", classUnderTest.getDescription());
    }

    @Test
    public void test_isChangeableVotes_DefaultFalse_success() {
        assertEquals(false, classUnderTest.isChangeableVotes());
    }

    @Test
    public void test_isChangeableVotes_SetTrue_success() {
        classUnderTest.setChangeableVotes(true);

        assertEquals(true, classUnderTest.isChangeableVotes());
    }

    @Test
    public void test_isVisibleVoters_DefaultFalse_success() {
        assertEquals(false, classUnderTest.isVisibleVoters());
    }

    @Test
    public void test_isVisibleVoters_SetTrue_success() {
        classUnderTest.setVisibleVoters(true);

        assertEquals(true, classUnderTest.isVisibleVoters());
    }

    @Test
    public void test_getVoteForExistingUser_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
        someChoice.voteFor(SOME_EXISTING_USER_NAME);

        classUnderTest.addChoice(someChoice);

        Choice result = classUnderTest.getVote(SOME_EXISTING_USER_NAME);

        assertEquals(someChoice, result);
        assertTrue(classUnderTest.getHasVoted(SOME_EXISTING_USER_NAME));
    }

    @Test
    public void test_getVoteForNotExistingUser_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
        someChoice.voteFor(SOME_EXISTING_USER_NAME);

        classUnderTest.addChoice(someChoice);

        Choice result = classUnderTest.getVote("someDifferentNotExistingUser");

        assertTrue(null == result);
        assertFalse(classUnderTest.getHasVoted("someDifferentNotExistingUser"));
    }

    @Test
    public void test_getChoice_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);

        classUnderTest.addChoice(someChoice);

        Choice result = classUnderTest.getChoice(SOME_CHOICE_DESCRIPTION);

        assertEquals(someChoice, result);
    }

    @Test
    public void test_getChoice_NotExists_failure() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);

        classUnderTest.addChoice(someChoice);

        Choice result = classUnderTest.getChoice("NotExistingChoice");

        assertNull(result);
    }

    @Test
    public void test_getChoices_NoChoices_failure() {
        Collection<Choice> result = classUnderTest.getChoices();

        assertEquals(0, result.size());
    }

    @Test
    public void test_getPercentageOfVoteForChoice_NoVotes_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);

        classUnderTest.addChoice(someChoice);

        final int percentageResult = classUnderTest.getPercentageOfVoteForChoice(someChoice);

        assertEquals(0, percentageResult);
    }

    @Test
    public void test_getPercentageOfVoteForChoice_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
        Choice someChoiceTwo = new Choice(SOME_CHOICE_DESCRIPTION + "two");

        classUnderTest.addChoice(someChoice);
        classUnderTest.addChoice(someChoiceTwo);

        someChoice.voteFor("someUserOne");
        someChoice.voteFor("someUserTwo");
        someChoiceTwo.voteFor("someUserThree");

        final int percentageResult = classUnderTest.getPercentageOfVoteForChoice(someChoice);
        final int percentageResultTwo = classUnderTest.getPercentageOfVoteForChoice(someChoiceTwo);

        assertEquals(66, percentageResult);
        assertEquals(33, percentageResultTwo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_AddComment_NoUser_exception() {
        Comment someComment = new Comment();

        classUnderTest.addComment(someComment);
    }

    @Test
    public void test_Comments_success() {
        Comment someComment = new Comment(SOME_EXISTING_USER_NAME, "some crazy comment for a crazy plugin");

        classUnderTest.addComment(someComment);

        final Map<String, Comment> result = classUnderTest.getComments();

        assertEquals(1, result.size());
        assertEquals(someComment, result.get(someComment.getUsername()));
    }

    @Test
    public void test_getCommentForUser_success() {
        final String comment = "some crazy comment for a crazy plugin";
        Comment someComment = new Comment(SOME_EXISTING_USER_NAME, comment);

        classUnderTest.addComment(someComment);

        final Comment result = classUnderTest.getCommentForUser(SOME_EXISTING_USER_NAME);

        assertEquals(someComment, result);
        assertEquals(comment, someComment.getComment());
    }

    @Test
    public void test_computeAverage_OneChoiceNoVotes_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);

        classUnderTest.addChoice(someChoice);

        final float result = classUnderTest.computeAverage();

        assertEquals(0.0f, result, 0.0f);
    }

    @Test
    public void test_computeAverage_OneChoiceOneVote_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
        someChoice.voteFor(SOME_EXISTING_USER_NAME);

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

        classUnderTest.addChoice(someChoice);
        classUnderTest.addChoice(someChoiceTwo);
        classUnderTest.addChoice(someChoiceThree);

        final float result = classUnderTest.computeAverage();

        assertEquals(2.0f, result, 0.0f);
        assertEquals(66, classUnderTest.getAveragePercentage(result));
    }

    @Test
    public void test_computeFormattedAverage_NegativeIterateStep_ThreeChoicesTwoVotes_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
        Choice someChoiceTwo = new Choice(SOME_CHOICE_DESCRIPTION + "TWO");
        someChoiceTwo.voteFor(SOME_EXISTING_USER_NAME + "TWO");
        Choice someChoiceThree = new Choice(SOME_CHOICE_DESCRIPTION + "THREE");
        someChoiceThree.voteFor(SOME_EXISTING_USER_NAME + "THREE");

        classUnderTest.addChoice(someChoice);
        classUnderTest.addChoice(someChoiceTwo);
        classUnderTest.addChoice(someChoiceThree);
        classUnderTest.setStartBound(-1);
        classUnderTest.setIterateStep(-3);

        final String format = "0.##";
        final String result = classUnderTest.computeFormatedAverage(format);

        assertEquals(new java.text.DecimalFormat(format).format(-2.5), result);
    }

    @Test
    public void test_getBounds_Default_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
        Choice someChoiceTwo = new Choice(SOME_CHOICE_DESCRIPTION + "TWO");

        classUnderTest.addChoice(someChoice);
        classUnderTest.addChoice(someChoiceTwo);

        assertEquals(Ballot.DEFAULT_START_BOUND, classUnderTest.getLowerBound());
        assertEquals(Ballot.DEFAULT_START_BOUND + Ballot.DEFAULT_START_BOUND * (classUnderTest.getChoices().size() - 1), classUnderTest.getUpperBound());
    }

    @Test
    public void test_getBoundsIfNotDefault_default_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
        Choice someChoiceTwo = new Choice(SOME_CHOICE_DESCRIPTION + "TWO");

        classUnderTest.addChoice(someChoice);
        classUnderTest.addChoice(someChoiceTwo);

        assertEquals("", classUnderTest.getBoundsIfNotDefault());
    }

    @Test
    public void test_getBoundsIfNotDefault_notDefault_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
        Choice someChoiceTwo = new Choice(SOME_CHOICE_DESCRIPTION + "TWO");

        classUnderTest.addChoice(someChoice);
        classUnderTest.addChoice(someChoiceTwo);

        classUnderTest.setStartBound(3);
        classUnderTest.setIterateStep(-3);

        assertEquals("(3-0)", classUnderTest.getBoundsIfNotDefault());
    }

    @Test
    public void test_getAllVoters_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
        Choice someChoiceTwo = new Choice(SOME_CHOICE_DESCRIPTION + "TWO");
        someChoiceTwo.voteFor(SOME_EXISTING_USER_NAME + "TWO");
        Choice someChoiceThree = new Choice(SOME_CHOICE_DESCRIPTION + "THREE");
        someChoiceThree.voteFor(SOME_EXISTING_USER_NAME + "THREE");

        classUnderTest.addChoice(someChoice);
        classUnderTest.addChoice(someChoiceTwo);
        classUnderTest.addChoice(someChoiceThree);

        Collection<String> userList = new ArrayList<String>();
        userList.add(SOME_EXISTING_USER_NAME + "TWO");
        userList.add(SOME_EXISTING_USER_NAME + "THREE");

        final Collection<String> allVoters = classUnderTest.getAllVoters();

        assertEquals(userList, allVoters);
    }

    @Test
    public void test_equalsAndHashCode() {
        Ballot classUnderTest2 = new Ballot(SOME_BALLOT_TITLE);

        assertFalse(classUnderTest.equals("someString"));
        assertTrue(classUnderTest.equals(classUnderTest2));
        assertTrue(classUnderTest.hashCode() == classUnderTest2.hashCode());
        assertTrue(classUnderTest.toString().equals(classUnderTest2.toString()));

        classUnderTest2.setDescription("someDesc2");
        assertFalse(classUnderTest.equals(classUnderTest2));
        assertFalse(classUnderTest.hashCode() == classUnderTest2.hashCode());
        assertFalse(classUnderTest.toString().equals(classUnderTest2.toString()));

        classUnderTest2 = new Ballot(SOME_BALLOT_TITLE + "2");
        assertFalse(classUnderTest.equals(classUnderTest2));
        assertFalse(classUnderTest.hashCode() == classUnderTest2.hashCode());
    }
}
