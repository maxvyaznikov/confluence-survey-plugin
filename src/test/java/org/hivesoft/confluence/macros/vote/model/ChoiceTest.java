package org.hivesoft.confluence.macros.vote.model;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChoiceTest {

    private static final String SOME_CHOICE_DESCRIPTION = "someChoiceSomeoneCanVoteOn";
    private static final String SOME_EXISTING_USER_NAME = "someExistingUserName";
    private static final String SOME_EXISTING_USER_NAME_TWO = "someOtherExistingUserName";

    @Test
    public void test_getHasVotedFor_success() {
        Choice classUnderTest = new Choice(SOME_CHOICE_DESCRIPTION);
        classUnderTest.voteFor(SOME_EXISTING_USER_NAME);
        classUnderTest.voteFor(SOME_EXISTING_USER_NAME_TWO);

        final boolean hasVotedFor = classUnderTest.getHasVotedFor(SOME_EXISTING_USER_NAME);
        final boolean hasVotedForTwo = classUnderTest.getHasVotedFor(SOME_EXISTING_USER_NAME_TWO);

        assertTrue(hasVotedFor);
        assertTrue(hasVotedForTwo);
    }

    @Test
    public void test_getHasVotedFor_failure() {
        Choice classUnderTest = new Choice(SOME_CHOICE_DESCRIPTION);
        classUnderTest.voteFor(SOME_EXISTING_USER_NAME);

        final boolean hasVotedFor = classUnderTest.getHasVotedFor(SOME_EXISTING_USER_NAME_TWO);
        assertFalse(hasVotedFor);
    }
}
