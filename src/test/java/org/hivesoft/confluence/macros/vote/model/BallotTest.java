package org.hivesoft.confluence.macros.vote.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BallotTest {

    private static final String SOME_BALLOT_TITLE = "someBallotTitle";
    private static final String SOME_CHOICE_DESCRIPTION = "someChoice";
    private static final String SOME_EXISTING_USER_NAME = "someExistingUser";

    @Before
    public void setup() {
    }

    @Test
    public void test_getVoteForExistingUser_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
        someChoice.voteFor(SOME_EXISTING_USER_NAME);

        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);
        classUnderTest.addChoice(someChoice);

        Choice result = classUnderTest.getVote(SOME_EXISTING_USER_NAME);

        assertEquals(someChoice, result);
    }

    @Test
    public void test_getVoteForNotExistingUser_success() {
        Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
        someChoice.voteFor(SOME_EXISTING_USER_NAME);

        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);
        classUnderTest.addChoice(someChoice);

        Choice result = classUnderTest.getVote("someDifferentNotExistingUser");

        assertTrue(null == result);
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
        classUnderTest.setIterateStep(-3);

        final String result = classUnderTest.computeFormatedAverage("0.##");

        assertEquals("-0.5", result);
    }


}
