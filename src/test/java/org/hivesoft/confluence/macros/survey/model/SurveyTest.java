package org.hivesoft.confluence.macros.survey.model;

import org.hivesoft.confluence.macros.utils.SurveyUtilsTest;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.macros.vote.model.Choice;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurveyTest {

  private static final String SOME_BALLOT_TITLE = "someBallotTitle";

  Survey classUnderTest;

  @Before
  public void setup() {
    classUnderTest = new Survey(SurveyUtilsTest.createDefaultSurveyConfig(new HashMap<String, String>()));
  }

  @Test
  public void test_getBallot_success() {
    Ballot someBallot = SurveyUtilsTest.createDefaultBallot(SOME_BALLOT_TITLE);
    classUnderTest.addBallot(someBallot);

    final Ballot result = classUnderTest.getBallot(SOME_BALLOT_TITLE);

    assertThat(result, is(equalTo(someBallot)));
  }

  @Test
  public void test_getBallotNotFound_failure() {
    Ballot someBallot = SurveyUtilsTest.createDefaultBallot(SOME_BALLOT_TITLE);
    classUnderTest.addBallot(someBallot);

    final Ballot result = classUnderTest.getBallot("BallotNotFound");

    assertNull(result);
  }

  @Test
  public void test_getBallots_success() {
    Ballot someBallot = SurveyUtilsTest.createDefaultBallot(SOME_BALLOT_TITLE);
    Ballot someBallot2 = SurveyUtilsTest.createDefaultBallot(SOME_BALLOT_TITLE + "2");
    final List<Ballot> ballots = Arrays.asList(someBallot, someBallot2);
    classUnderTest.setBallots(ballots);

    final List<Ballot> result = classUnderTest.getBallots();

    assertThat(result, is(equalTo(ballots)));
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
  public void test_getBallotTitlesWithChoiceNames_success() {
    Ballot someBallot = SurveyUtilsTest.createDefaultBallot(SOME_BALLOT_TITLE);
    Ballot someBallot2 = SurveyUtilsTest.createDefaultBallot(SOME_BALLOT_TITLE + "2");

    Choice choice = new Choice("someChoice");
    someBallot.addChoice(choice);

    final List<Ballot> ballots = Arrays.asList(someBallot, someBallot2);
    classUnderTest.setBallots(ballots);

    final List<String> result = classUnderTest.getBallotTitlesWithChoiceNames();

    assertThat(result.get(0), is(equalTo(SOME_BALLOT_TITLE + ".someChoice")));
  }
}
