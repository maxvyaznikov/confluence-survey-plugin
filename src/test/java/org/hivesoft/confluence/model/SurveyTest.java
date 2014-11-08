package org.hivesoft.confluence.model;

import com.atlassian.user.User;
import com.atlassian.user.impl.DefaultUser;
import org.hamcrest.core.Is;
import org.hivesoft.confluence.macros.ConfluenceTestBase;
import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.model.vote.Ballot;
import org.hivesoft.confluence.model.vote.Comment;
import org.hivesoft.confluence.utils.SurveyUtils;
import org.hivesoft.confluence.utils.SurveyUtilsTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurveyTest extends ConfluenceTestBase {

  Survey classUnderTest;

  @Before
  public void setup() {
    classUnderTest = new Survey(createDefaultSurveyConfig(new HashMap<String, String>()));
  }

  @Test
  public void test_equalsHashCode() {
    Survey classUnderTest2 = new Survey(createDefaultSurveyConfig(new HashMap<String, String>()));

    assertThat(classUnderTest.toString(), is(classUnderTest2.toString()));
  }

  @Test
  public void test_getBallot_success() {
    Ballot someBallot = createDefaultBallot(SOME_BALLOT_TITLE);
    classUnderTest.addBallot(someBallot);

    final Ballot result = classUnderTest.getBallot(SOME_BALLOT_TITLE);

    assertThat(result, is(equalTo(someBallot)));
  }

  @Test
  public void test_getBallotNotFound_failure() {
    Ballot someBallot = createDefaultBallot(SOME_BALLOT_TITLE);
    classUnderTest.addBallot(someBallot);

    final Ballot result = classUnderTest.getBallot("BallotNotFound");

    assertNull(result);
  }

  @Test
  public void test_getBallots_success() {
    Ballot someBallot = createDefaultBallot(SOME_BALLOT_TITLE);
    Ballot someBallot2 = createDefaultBallot(SOME_BALLOT_TITLE + "2");
    final List<Ballot> ballots = Arrays.asList(someBallot, someBallot2);
    classUnderTest.setBallots(ballots);

    final List<Ballot> result = classUnderTest.getBallots();

    assertThat(result, is(equalTo(ballots)));
  }

  @Test
  public void test_isSurveyComplete_success() {
    Ballot mockBallot = mock(Ballot.class);
    classUnderTest.addBallot(mockBallot);

    when(mockBallot.getHasVoted(any(User.class))).thenReturn(true);

    final boolean completed = classUnderTest.isSurveyComplete(new DefaultUser("someUsername"));

    assertTrue(completed);
  }

  @Test
  public void test_isSurveyComplete_failure() {
    Ballot mockBallot = mock(Ballot.class);
    classUnderTest.addBallot(mockBallot);

    when(mockBallot.getHasVoted(any(User.class))).thenReturn(false);

    final boolean completed = classUnderTest.isSurveyComplete(new DefaultUser("someUsername"));

    assertFalse(completed);
  }

  @Test
  public void test_getBallotTitlesWithChoiceNames_success() {
    Ballot someBallot = createDefaultBallot(SOME_BALLOT_TITLE);
    Ballot someBallot2 = createDefaultBallot(SOME_BALLOT_TITLE + "2");

    final List<Ballot> ballots = Arrays.asList(someBallot, someBallot2);
    classUnderTest.setBallots(ballots);

    final List<String> result = classUnderTest.getBallotTitlesWithChoiceNames();

    assertThat(result.get(0), is(equalTo(SOME_BALLOT_TITLE + "." + SurveyUtils.getDefaultChoices().get(0).getDescription())));
  }

  @Test
  public void test_getTitleWithRenderedLinks_success(){
    HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(SurveyConfig.KEY_TITLE, "i am a survey to http://google.de but https://www.google.com is also ok");

    classUnderTest = new Survey(createDefaultSurveyConfig(parameters));

    assertThat(classUnderTest.getTitleWithRenderedLinks(),
            is("i am a survey to <a href=\"http://google.de\" target=\"_blank\">http://google.de</a> but <a href=\"https://www.google.com\" target=\"_blank\">https://www.google.com</a> is also ok"));
  }
}
