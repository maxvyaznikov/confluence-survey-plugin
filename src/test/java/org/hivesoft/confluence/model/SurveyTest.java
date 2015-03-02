package org.hivesoft.confluence.model;

import org.hivesoft.confluence.macros.ConfluenceTestBase;
import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.model.vote.Ballot;
import org.hivesoft.confluence.model.vote.Choice;
import org.hivesoft.confluence.utils.SurveyUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class SurveyTest extends ConfluenceTestBase {

  private Survey classUnderTest;

  @Test
  public void test_toString() {
    classUnderTest = new SurveyBuilder().build();

    Survey classUnderTest2 = new SurveyBuilder().build();

    assertThat(classUnderTest.toString(), is(classUnderTest2.toString()));
  }

  @Test
  public void test_getBallot_success() {
    classUnderTest = new SurveyBuilder().build();

    Ballot someBallot = new BallotBuilder().title(SOME_BALLOT_TITLE).build();

    classUnderTest.addBallot(someBallot);

    final Ballot result = classUnderTest.getBallot(SOME_BALLOT_TITLE);

    assertThat(result, is(equalTo(someBallot)));
  }

  @Test
  public void test_getBallotNotFound_failure() {
    classUnderTest = new SurveyBuilder().build();

    Ballot someBallot = new BallotBuilder().title(SOME_BALLOT_TITLE).build();

    classUnderTest.addBallot(someBallot);

    final Ballot result = classUnderTest.getBallot("BallotNotFound");

    assertThat(result, is(nullValue()));
  }

  @Test
  public void test_getBallots_success() {
    classUnderTest = new SurveyBuilder().build();

    Ballot someBallot = new BallotBuilder().title(SOME_BALLOT_TITLE).build();
    Ballot someBallot2 = new BallotBuilder().title(SOME_BALLOT_TITLE + "2").build();

    classUnderTest.addBallot(someBallot);
    classUnderTest.addBallot(someBallot2);

    final List<Ballot> result = classUnderTest.getBallots();

    assertThat(result, is(equalTo(Arrays.asList(someBallot, someBallot2))));
  }

  @Test
  public void test_isSurveyComplete_success() {
    classUnderTest = new SurveyBuilder().build();

    Choice choice1 = new Choice("firstChoice");
    Choice choice2 = new Choice("secondChoice");
    choice1.voteFor(SOME_USER1);

    Ballot someBallot = new BallotBuilder().choices(Arrays.asList(choice1, choice2)).build();

    classUnderTest.addBallot(someBallot);

    final boolean completed = classUnderTest.isSurveyComplete(SOME_USER1);

    assertThat(completed, is(true));
  }

  @Test
  public void test_isSurveyComplete_failure() {
    classUnderTest = new SurveyBuilder().build();

    Choice choice1 = new Choice("firstChoice");
    Choice choice2 = new Choice("secondChoice");

    Ballot someBallot = new BallotBuilder().choices(Arrays.asList(choice1, choice2)).build();

    classUnderTest.addBallot(someBallot);

    final boolean completed = classUnderTest.isSurveyComplete(SOME_USER1);

    assertThat(completed, is(false));
  }

  @Test
  public void test_getBallotTitlesWithChoiceNames_success() {
    classUnderTest = new SurveyBuilder().build();

    Ballot someBallot = new BallotBuilder().title(SOME_BALLOT_TITLE).build();
    Ballot someBallot2 = new BallotBuilder().title(SOME_BALLOT_TITLE + "2").build();

    classUnderTest.addBallot(someBallot);
    classUnderTest.addBallot(someBallot2);

    final List<String> result = classUnderTest.getBallotTitlesWithChoiceNames();

    assertThat(result.get(0), is(equalTo(SOME_BALLOT_TITLE + "." + SurveyUtils.getDefaultChoices().get(0).getDescription())));
  }

  @Test
  public void test_getTitleWithRenderedLinks_success() {

    HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(SurveyConfig.KEY_TITLE, "i am a survey to http://google.de but https://www.google.com is also ok");

    classUnderTest = new SurveyBuilder().parameters(parameters).build();

    assertThat(classUnderTest.getTitleWithRenderedLinks(),
            is("i am a survey to <a href=\"http://google.de\" target=\"_blank\">http://google.de</a> but <a href=\"https://www.google.com\" target=\"_blank\">https://www.google.com</a> is also ok"));
  }
}
