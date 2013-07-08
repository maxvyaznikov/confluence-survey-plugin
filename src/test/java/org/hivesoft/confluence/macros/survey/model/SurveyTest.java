package org.hivesoft.confluence.macros.survey.model;

import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurveyTest {

    private static final String SOME_BALLOT_TITLE = "someBallotTitle";

    @Test
    public void test_getBallot_success() {
        Survey classUnderTest = new Survey();

        Ballot someBallot = new Ballot(SOME_BALLOT_TITLE);
        classUnderTest.addBallot(someBallot);

        final Ballot result = classUnderTest.getBallot(SOME_BALLOT_TITLE);

        assertEquals(someBallot, result);
    }

    @Test
    public void test_getBallotNotFound_failure() {
        Survey classUnderTest = new Survey();

        Ballot someBallot = new Ballot(SOME_BALLOT_TITLE);
        classUnderTest.addBallot(someBallot);

        final Ballot result = classUnderTest.getBallot("BallotNotFound");

        assertNull(result);
    }

    @Test
    public void test_isSurveyComplete_success() {
        Survey classUnderTest = new Survey();

        Ballot mockBallot = mock(Ballot.class);
        classUnderTest.addBallot(mockBallot);

        when(mockBallot.getHasVoted(anyString())).thenReturn(true);

        final boolean completed = classUnderTest.isSurveyComplete("someUsername");

        assertTrue(completed);
    }

    @Test
    public void test_isSurveyComplete_false() {
        Survey classUnderTest = new Survey();

        Ballot mockBallot = mock(Ballot.class);
        classUnderTest.addBallot(mockBallot);

        when(mockBallot.getHasVoted(anyString())).thenReturn(false);

        final boolean completed = classUnderTest.isSurveyComplete("someUsername");

        assertFalse(completed);
    }
}
