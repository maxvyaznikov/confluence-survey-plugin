package org.hivesoft.confluence.macros.vote.model;

import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.macros.utils.SurveyUtilsTest;
import org.hivesoft.confluence.macros.vote.VoteConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class BallotTest {

  private static final String SOME_EXISTING_USER_NAME = "someExistingUser";

  Ballot classUnderTest;

  @Before
  public void setup() {
    classUnderTest = SurveyUtilsTest.createDefaultBallot(SurveyUtilsTest.SOME_BALLOT_TITLE);
  }

  @Test
  public void test_getTitle_success() {
    assertThat(classUnderTest.getTitle(), is(equalTo(SurveyUtilsTest.SOME_BALLOT_TITLE)));
  }

  @Test
  public void test_getTitleNoSpace_success() {
    classUnderTest = SurveyUtilsTest.createDefaultBallot(SurveyUtilsTest.SOME_BALLOT_TITLE + " more spaces");

    assertThat(classUnderTest.getTitleNoSpace(), is(equalTo(SurveyUtilsTest.SOME_BALLOT_TITLE.toLowerCase() + "morespaces")));
  }

  @Test
  public void test_getDescription_success() {
    assertEquals("", classUnderTest.getDescription());

    classUnderTest.setDescription("someDescription");
    assertEquals("someDescription", classUnderTest.getDescription());
  }

  @Test
  public void test_ballotsWithDefaults_success() {
    assertNotNull(classUnderTest.getConfig());
  }

  @Test
  public void test_getVoteForExistingUser_success() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    someChoice.voteFor(SOME_EXISTING_USER_NAME);

    classUnderTest.addChoice(someChoice);

    Choice result = classUnderTest.getChoiceForUserName(SOME_EXISTING_USER_NAME);

    assertEquals(someChoice, result);
    assertTrue(classUnderTest.getHasVoted(SOME_EXISTING_USER_NAME));
  }

  @Test
  public void test_getVoteForNotExistingUser_success() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    someChoice.voteFor(SOME_EXISTING_USER_NAME);

    classUnderTest.addChoice(someChoice);

    Choice result = classUnderTest.getChoiceForUserName("someDifferentNotExistingUser");

    assertTrue(null == result);
    assertFalse(classUnderTest.getHasVoted("someDifferentNotExistingUser"));
  }

  @Test
  public void test_getChoice_success() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);

    classUnderTest.addChoice(someChoice);

    Choice result = classUnderTest.getChoice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);

    assertEquals(someChoice, result);
  }

  @Test
  public void test_getChoice_NotExists_failure() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);

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
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "TWO");

    classUnderTest.addChoice(someChoice);
    classUnderTest.addChoice(someChoiceTwo);

    final int percentageResult = classUnderTest.getPercentageOfVoteForChoice(someChoice);

    assertThat(percentageResult, is(0));
  }

  @Test
  public void test_getPercentageOfVoteForChoice_success() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "two");

    classUnderTest.addChoice(someChoice);
    classUnderTest.addChoice(someChoiceTwo);

    someChoice.voteFor("someUserOne");
    someChoice.voteFor("someUserTwo");
    someChoiceTwo.voteFor("someUserThree");

    final int percentageResult = classUnderTest.getPercentageOfVoteForChoice(someChoice);
    final int percentageResultTwo = classUnderTest.getPercentageOfVoteForChoice(someChoiceTwo);

    assertThat(percentageResult, is(66));
    assertThat(percentageResultTwo, is(33));
  }

  @Test
  public void test_getComments_success() {
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
  public void test_computeAverage_TwoChoicesNoVotes_expectZero_success() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "TWO");

    classUnderTest.addChoice(someChoice);
    classUnderTest.addChoice(someChoiceTwo);

    final float result = classUnderTest.computeAverage();

    assertEquals(0.0f, result, 0.0f);
  }

  @Test
  public void test_computeAverage_TwoChoiceOneVote_success() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    someChoice.voteFor(SOME_EXISTING_USER_NAME);
    Choice someChoiceTwo = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "TWO");

    classUnderTest.addChoice(someChoice);    //2 * 1 => 2
    classUnderTest.addChoice(someChoiceTwo); //1 * 0 => 0

    final float result = classUnderTest.computeAverage();

    assertEquals(2.0f, result, 0.0f);
  }

  @Test
  public void test_computeAverage_ThreeChoicesOneVoteOnSecond_success() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "TWO");
    someChoiceTwo.voteFor(SOME_EXISTING_USER_NAME + "TWO");
    Choice someChoiceThree = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "THREE");

    classUnderTest.addChoice(someChoice);      //3 - 100%
    classUnderTest.addChoice(someChoiceTwo);   //2 -  66%
    classUnderTest.addChoice(someChoiceThree); //1 -  33%

    final float result = classUnderTest.computeAverage();

    assertEquals(2.0f, result, 0.0f);
    assertEquals(66, classUnderTest.getAveragePercentage(result));
  }

  @Test
  public void test_computeAverage_NegativeIterateStepFromPositiveStart_FiveChoicesOneVoteOnFourth_expect40percent_success() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "TWO");
    Choice someChoiceThree = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "THREE");
    Choice someChoiceFour = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "FOUR");
    someChoiceFour.voteFor(SOME_EXISTING_USER_NAME + "TWO");
    Choice someChoiceFive = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "FIVE");

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, SurveyUtilsTest.SOME_BALLOT_TITLE);
    parameters.put(SurveyConfig.KEY_START_BOUND, "4");
    parameters.put(SurveyConfig.KEY_ITERATE_STEP, "-4");

    classUnderTest = SurveyUtilsTest.createBallotWithParameters(parameters);

    classUnderTest.addChoice(someChoice);      //-12 -> 100%
    classUnderTest.addChoice(someChoiceTwo);   //- 8 -> 80%
    classUnderTest.addChoice(someChoiceThree); //- 4 -> 60
    classUnderTest.addChoice(someChoiceFour);  //  0 -> 40
    classUnderTest.addChoice(someChoiceFive);  //  4 -> 20

    final float result = classUnderTest.computeAverage();

    assertThat(result, is(equalTo(0.0f)));
    assertThat(classUnderTest.getAveragePercentage(result), is(equalTo(40)));
    assertThat(classUnderTest.getAveragePercentage(), is(25));
  }

  @Test
  public void test_computeAverage_ZeroToOne_TwoChoicesTwoOnFirstOneOnSecond_expect50percent_success() {
    Choice someChoice = new Choice("one");
    someChoice.voteFor(SOME_EXISTING_USER_NAME + "ONE");
    someChoice.voteFor(SOME_EXISTING_USER_NAME + "THREE");
    Choice someChoiceTwo = new Choice("two");
    someChoiceTwo.voteFor(SOME_EXISTING_USER_NAME + "TWO");

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, SurveyUtilsTest.SOME_BALLOT_TITLE);
    parameters.put(SurveyConfig.KEY_START_BOUND, "0");
    parameters.put(SurveyConfig.KEY_ITERATE_STEP, "1");

    classUnderTest = SurveyUtilsTest.createBallotWithParameters(parameters);

    classUnderTest.addChoice(someChoice);      //1 -> 100%
    classUnderTest.addChoice(someChoiceTwo);   //0 -> 50%

    final float result = classUnderTest.computeAverage();

    assertThat(result, is(equalTo(0.6666667F)));
    assertThat(classUnderTest.getAveragePercentage(result), is(equalTo(83)));
  }

  @Test
  public void test_computeFormattedAverage_NegativeIterateStep_ThreeChoicesThreeVotes_success() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "TWO");
    someChoiceTwo.voteFor(SOME_EXISTING_USER_NAME + "ONE");
    someChoiceTwo.voteFor(SOME_EXISTING_USER_NAME + "TWO");
    Choice someChoiceThree = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "THREE");
    someChoiceThree.voteFor(SOME_EXISTING_USER_NAME + "THREE");

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, SurveyUtilsTest.SOME_BALLOT_TITLE);
    parameters.put(SurveyConfig.KEY_START_BOUND, "-1");
    parameters.put(SurveyConfig.KEY_ITERATE_STEP, "-3");

    classUnderTest = SurveyUtilsTest.createBallotWithParameters(parameters);

    classUnderTest.addChoice(someChoice);      //-7 ->
    classUnderTest.addChoice(someChoiceTwo);   //-4 ->
    classUnderTest.addChoice(someChoiceThree); //-1 ->

    final String format = "0.##";
    final String result = classUnderTest.computeFormattedAverage(format);

    assertEquals(new java.text.DecimalFormat(format).format(-3.0), result);
  }

  @Test
  public void test_getBounds_Default_success() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "TWO");

    classUnderTest.addChoice(someChoice);
    classUnderTest.addChoice(someChoiceTwo);

    assertEquals(SurveyConfig.DEFAULT_START_BOUND, classUnderTest.getLowerBound());
    assertEquals(SurveyConfig.DEFAULT_START_BOUND + SurveyConfig.DEFAULT_START_BOUND * (classUnderTest.getChoices().size() - 1), classUnderTest.getUpperBound());
  }

  @Test
  public void test_getBoundsIfNotDefault_default_success() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "TWO");

    classUnderTest.addChoice(someChoice);
    classUnderTest.addChoice(someChoiceTwo);

    assertEquals("", classUnderTest.getBoundsIfNotDefault());
  }

  @Test
  public void test_getBoundsIfNotDefault_notDefault_success() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "TWO");

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, SurveyUtilsTest.SOME_BALLOT_TITLE);
    parameters.put(SurveyConfig.KEY_START_BOUND, "3");
    parameters.put(SurveyConfig.KEY_ITERATE_STEP, "-3");

    classUnderTest = SurveyUtilsTest.createBallotWithParameters(parameters);

    classUnderTest.addChoice(someChoice);
    classUnderTest.addChoice(someChoiceTwo);

    assertThat(classUnderTest.getBoundsIfNotDefault(), is(equalTo("(3-0)")));
  }

  @Test
  public void test_getBoundsIfNotDefault_oneDefault_success() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "TWO");

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, SurveyUtilsTest.SOME_BALLOT_TITLE);
    parameters.put(SurveyConfig.KEY_START_BOUND, "3");

    classUnderTest = SurveyUtilsTest.createBallotWithParameters(parameters);

    classUnderTest.addChoice(someChoice);
    classUnderTest.addChoice(someChoiceTwo);

    assertThat(classUnderTest.getBoundsIfNotDefault(), is(equalTo("(3-4)")));

    parameters.put(SurveyConfig.KEY_START_BOUND, "1");
    parameters.put(SurveyConfig.KEY_ITERATE_STEP, "3");

    classUnderTest = SurveyUtilsTest.createBallotWithParameters(parameters);

    classUnderTest.addChoice(someChoice);
    classUnderTest.addChoice(someChoiceTwo);

    assertThat(classUnderTest.getBoundsIfNotDefault(), is(equalTo("(1-4)")));
  }

  @Test
  public void test_getAllVoters_success() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "TWO");
    someChoiceTwo.voteFor(SOME_EXISTING_USER_NAME + "TWO");
    Choice someChoiceThree = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "THREE");
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
  public void test_equalsAndHashCode_success() {
    Ballot classUnderTest2 = new Ballot(SurveyUtilsTest.SOME_BALLOT_TITLE, SurveyUtilsTest.createDefaultVoteConfig(new HashMap<String, String>()));

    assertFalse(classUnderTest.equals("someString"));
    assertTrue(classUnderTest.equals(classUnderTest2));
    assertTrue(classUnderTest.hashCode() == classUnderTest2.hashCode());
    assertTrue(classUnderTest.toString().equals(classUnderTest2.toString()));

    classUnderTest2.setDescription("someDesc2"); //only title matters
    assertThat(classUnderTest, is(equalTo(classUnderTest2)));
    assertTrue(classUnderTest.hashCode() == classUnderTest2.hashCode());
    assertFalse(classUnderTest.toString().equals(classUnderTest2.toString()));

    classUnderTest2 = new Ballot(SurveyUtilsTest.SOME_BALLOT_TITLE + "2", SurveyUtilsTest.createDefaultVoteConfig(new HashMap<String, String>()));
    assertFalse(classUnderTest.equals(classUnderTest2));
    assertFalse(classUnderTest.hashCode() == classUnderTest2.hashCode());
  }
}
