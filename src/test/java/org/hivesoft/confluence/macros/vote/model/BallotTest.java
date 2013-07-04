package org.hivesoft.confluence.macros.vote.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BallotTest {

    private static final String SOME_BALLOT_TITLE = "someBallotTitle";
    private static final String SOME_EXISTING_USER_NAME = "someExistingUser";

    @Before
    public void setup() {
    }

    @Test
    public void test_getVoteForExistingUser_success() {
        Choice someChoice = new Choice("someChoice");
        someChoice.voteFor(SOME_EXISTING_USER_NAME);

        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);
        classUnderTest.addChoice(someChoice);

        Choice result = classUnderTest.getVote(SOME_EXISTING_USER_NAME);

        assertEquals(someChoice, result);
    }

    @Test
    public void test_getVoteForNotExistingUser_success() {
        Choice someChoice = new Choice("someChoice");
        someChoice.voteFor(SOME_EXISTING_USER_NAME);

        Ballot classUnderTest = new Ballot(SOME_BALLOT_TITLE);
        classUnderTest.addChoice(someChoice);

        Choice result = classUnderTest.getVote("someDifferentNotExistingUser");

        assertTrue(null == result);
    }
}
