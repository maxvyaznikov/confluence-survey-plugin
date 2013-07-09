package org.hivesoft.confluence.macros.survey.model;

import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.macros.vote.model.Choice;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurveyTest {

    private static final String SOME_BALLOT_TITLE = "someBallotTitle";

    Survey classUnderTest;

    @Before
    public void setup() {
        classUnderTest = new Survey();
    }

    @Test
    public void test_getBallot_success() {
        Ballot someBallot = new Ballot(SOME_BALLOT_TITLE);
        classUnderTest.addBallot(someBallot);

        final Ballot result = classUnderTest.getBallot(SOME_BALLOT_TITLE);

        assertEquals(someBallot, result);
    }

    @Test
    public void test_getBallotNotFound_failure() {
        Ballot someBallot = new Ballot(SOME_BALLOT_TITLE);
        classUnderTest.addBallot(someBallot);

        final Ballot result = classUnderTest.getBallot("BallotNotFound");

        assertNull(result);
    }

    @Test
    public void test_getBallots_success() {
        Ballot someBallot = new Ballot(SOME_BALLOT_TITLE);
        Ballot someBallot2 = new Ballot(SOME_BALLOT_TITLE + "2");
        final List<Ballot> ballots = Arrays.asList(someBallot, someBallot2);
        classUnderTest.setBallots(ballots);

        final List<Ballot> result = classUnderTest.getBallots();

        assertEquals(ballots, result);
    }

    @Test
    public void test_isSurveyComplete_success() {
        Ballot mockBallot = mock(Ballot.class);
        classUnderTest.addBallot(mockBallot);

        when(mockBallot.getHasVoted(anyString())).thenReturn(true);

        final boolean completed = classUnderTest.isSurveyComplete("someUsername");

        assertTrue(completed);
    }

    @Test
    public void test_isSurveyComplete_failure() {
        Ballot mockBallot = mock(Ballot.class);
        classUnderTest.addBallot(mockBallot);

        when(mockBallot.getHasVoted(anyString())).thenReturn(false);

        final boolean completed = classUnderTest.isSurveyComplete("someUsername");

        assertFalse(completed);
    }


    @Test
    public void test_isChangeableVotes_success() {
        Ballot someBallot = new Ballot(SOME_BALLOT_TITLE);
        Ballot someBallot2 = new Ballot(SOME_BALLOT_TITLE + "2");

        final List<Ballot> ballots = Arrays.asList(someBallot, someBallot2);
        classUnderTest.setBallots(ballots);

        assertFalse(classUnderTest.isChangeableVotes());
        assertFalse(classUnderTest.getBallot(SOME_BALLOT_TITLE).isChangeableVotes());

        classUnderTest.setChangeableVotes(true);

        assertTrue(classUnderTest.isChangeableVotes());
        assertTrue(classUnderTest.getBallot(SOME_BALLOT_TITLE).isChangeableVotes());
    }

    @Test
    public void test_isVisibleVoters_success() {
        Ballot someBallot = new Ballot(SOME_BALLOT_TITLE);
        Ballot someBallot2 = new Ballot(SOME_BALLOT_TITLE + "2");

        final List<Ballot> ballots = Arrays.asList(someBallot, someBallot2);
        classUnderTest.setBallots(ballots);

        assertFalse(classUnderTest.isVisibleVoters());
        assertFalse(classUnderTest.getBallot(SOME_BALLOT_TITLE).isVisibleVoters());

        classUnderTest.setVisibleVoters(true);

        assertTrue(classUnderTest.isVisibleVoters());
        assertTrue(classUnderTest.getBallot(SOME_BALLOT_TITLE).isVisibleVoters());
    }

    @Test
    public void test_setStartBoundAndIterateStep_success() {
        Ballot someBallot = new Ballot(SOME_BALLOT_TITLE);
        Ballot someBallot2 = new Ballot(SOME_BALLOT_TITLE + "2");

        final List<Ballot> ballots = Arrays.asList(someBallot, someBallot2);
        classUnderTest.setBallots(ballots);

        assertEquals(Ballot.DEFAULT_START_BOUND, classUnderTest.getBallot(SOME_BALLOT_TITLE).getStartBound());
        assertEquals(Ballot.DEFAULT_ITERATE_STEP, classUnderTest.getBallot(SOME_BALLOT_TITLE + "2").getIterateStep());

        classUnderTest.setStartBoundAndIterateStep(4, 6);

        assertEquals(4, classUnderTest.getBallot(SOME_BALLOT_TITLE).getStartBound());
        assertEquals(6, classUnderTest.getBallot(SOME_BALLOT_TITLE + "2").getIterateStep());
    }

    @Test
    public void test_TitleAndSummary_success() {
        classUnderTest.setTitle("someTitle");
        assertEquals("someTitle", classUnderTest.getTitle());

        assertTrue(classUnderTest.isSummaryDisplay());
        classUnderTest.setSummaryDisplay(false);
        assertFalse(classUnderTest.isSummaryDisplay());
    }

    @Test
    public void test_getBallotTitlesWithChoiceNames_success() {
        Ballot someBallot = new Ballot(SOME_BALLOT_TITLE);
        Ballot someBallot2 = new Ballot(SOME_BALLOT_TITLE + "2");

        Choice choice = new Choice("someChoice");
        someBallot.addChoice(choice);

        final List<Ballot> ballots = Arrays.asList(someBallot, someBallot2);
        classUnderTest.setBallots(ballots);

        final List<String> result = classUnderTest.getBallotTitlesWithChoiceNames();

        assertEquals(SOME_BALLOT_TITLE + ".someChoice", result.get(0));
    }
}
