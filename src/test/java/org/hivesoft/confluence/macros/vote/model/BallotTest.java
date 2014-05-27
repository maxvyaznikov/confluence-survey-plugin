package org.hivesoft.confluence.macros.vote.model;

import com.atlassian.user.User;
import com.atlassian.user.impl.DefaultUser;
import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.macros.utils.PermissionEvaluator;
import org.hivesoft.confluence.macros.utils.SurveyUtils;
import org.hivesoft.confluence.macros.utils.SurveyUtilsTest;
import org.hivesoft.confluence.macros.vote.VoteConfig;
import org.junit.Test;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BallotTest {

  private static final String SOME_EXISTING_USER_NAME = "someExistingUser";

  Ballot classUnderTest;

  @Test
  public void test_getTitle_success() {
    classUnderTest = SurveyUtilsTest.createDefaultBallot(SurveyUtilsTest.SOME_BALLOT_TITLE);
    assertThat(classUnderTest.getTitle(), is(equalTo(SurveyUtilsTest.SOME_BALLOT_TITLE)));
  }

  @Test
  public void test_getTitleNoSpace_success() {
    classUnderTest = SurveyUtilsTest.createDefaultBallot(SurveyUtilsTest.SOME_BALLOT_TITLE + " more spaces");

    assertThat(classUnderTest.getTitleNoSpace(), is(equalTo(SurveyUtilsTest.SOME_BALLOT_TITLE.toLowerCase() + "morespaces")));
  }

  @Test
  public void test_getDescription_success() {
    classUnderTest = SurveyUtilsTest.createDefaultBallot(SurveyUtilsTest.SOME_BALLOT_TITLE);
    assertEquals("", classUnderTest.getDescription());

    classUnderTest = new Ballot(SurveyUtilsTest.SOME_BALLOT_TITLE, "someDescription", SurveyUtilsTest.createDefaultVoteConfig(new HashMap<String, String>()), SurveyUtils.getDefaultChoices(), new ArrayList<Comment>());
    assertEquals("someDescription", classUnderTest.getDescription());
  }

  @Test
  public void test_ballotsWithDefaults_success() {
    classUnderTest = SurveyUtilsTest.createDefaultBallot(SurveyUtilsTest.SOME_BALLOT_TITLE);
    assertNotNull(classUnderTest.getConfig());
  }

  @Test
  public void test_getVoteForExistingUser_success() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    someChoice.voteFor(SOME_EXISTING_USER_NAME);

    classUnderTest = SurveyUtilsTest.createDefaultBallotWithChoices(SurveyUtilsTest.SOME_BALLOT_TITLE, Arrays.asList(someChoice));

    Choice result = classUnderTest.getChoiceForUserName(SOME_EXISTING_USER_NAME);

    assertEquals(someChoice, result);
    assertTrue(classUnderTest.getHasVoted(SOME_EXISTING_USER_NAME));
  }

  @Test
  public void test_getVoteForNotExistingUser_success() {
    classUnderTest = SurveyUtilsTest.createDefaultBallot(SurveyUtilsTest.SOME_BALLOT_TITLE);
    classUnderTest.getChoices().iterator().next().voteFor(SOME_EXISTING_USER_NAME);

    Choice result = classUnderTest.getChoiceForUserName("someDifferentNotExistingUser");

    assertTrue(null == result);
    assertFalse(classUnderTest.getHasVoted("someDifferentNotExistingUser"));
  }

  @Test
  public void test_getChoice_success() {
    classUnderTest = SurveyUtilsTest.createDefaultBallot(SurveyUtilsTest.SOME_BALLOT_TITLE);
    final Choice someChoice = classUnderTest.getChoices().iterator().next();

    Choice result = classUnderTest.getChoice(SurveyUtils.getDefaultChoices().get(0).getDescription());

    assertEquals(someChoice, result);
  }

  @Test
  public void test_getChoice_NotExists_failure() {
    classUnderTest = SurveyUtilsTest.createDefaultBallot(SurveyUtilsTest.SOME_BALLOT_TITLE);

    Choice result = classUnderTest.getChoice("NotExistingChoice");

    assertNull(result);
  }

  @Test
  public void test_getChoices_DefaultChoices_failure() {
    classUnderTest = SurveyUtilsTest.createDefaultBallot(SurveyUtilsTest.SOME_BALLOT_TITLE);
    Collection<Choice> result = classUnderTest.getChoices();

    assertThat(result.size(), is(5));
  }

  @Test
  public void test_getPercentageOfVoteForChoice_NoVotes_success() {
    classUnderTest = SurveyUtilsTest.createDefaultBallot(SurveyUtilsTest.SOME_BALLOT_TITLE);

    final Choice someChoice = classUnderTest.getChoices().iterator().next();

    final int percentageResult = classUnderTest.getPercentageOfVoteForChoice(someChoice);

    assertThat(percentageResult, is(0));
  }

  @Test
  public void test_getPercentageOfVoteForChoice_success() {
    final Choice someChoice = new Choice("choice1");
    final Choice someChoiceTwo = new Choice("choice2");

    someChoice.voteFor("someUserOne");
    someChoice.voteFor("someUserTwo");
    someChoiceTwo.voteFor("someUserThree");

    classUnderTest = SurveyUtilsTest.createDefaultBallotWithChoices(SurveyUtilsTest.SOME_BALLOT_TITLE, Arrays.asList(someChoice, someChoiceTwo));

    final int percentageResult = classUnderTest.getPercentageOfVoteForChoice(someChoice);
    final int percentageResultTwo = classUnderTest.getPercentageOfVoteForChoice(someChoiceTwo);

    assertThat(percentageResult, is(66));
    assertThat(percentageResultTwo, is(33));
  }

  @Test
  public void test_getComments_success() {
    Comment someComment = new Comment(SOME_EXISTING_USER_NAME, "some crazy comment for a crazy plugin");

    classUnderTest = SurveyUtilsTest.createDefaultBallotWithComments("someBallot", Arrays.asList(someComment));

    final List<Comment> result = classUnderTest.getComments();

    assertEquals(1, result.size());
    assertEquals(someComment, result.get(0));
  }

  @Test
  public void test_getCommentForUser_success() {
    final String comment = "some crazy comment for a crazy plugin";
    Comment someComment = new Comment(SOME_EXISTING_USER_NAME, comment);

    classUnderTest = SurveyUtilsTest.createDefaultBallotWithComments("someBallot", Arrays.asList(someComment));

    final Comment result = classUnderTest.getCommentForUser(SOME_EXISTING_USER_NAME);

    assertEquals(someComment, result);
    assertEquals(comment, someComment.getComment());
  }

  @Test
  public void test_getCurrentValueByIndex_defaultSteps_success() {
    classUnderTest = SurveyUtilsTest.createDefaultBallot(SurveyUtilsTest.SOME_BALLOT_TITLE);

    assertThat(classUnderTest.getCurrentValueByIndex(1), is(2));
    assertThat(classUnderTest.getCurrentValueByIndex(0), is(1));
  }

  private List<Choice> createChoicesWithoutVotes(int count) {
    List<Choice> choices = new ArrayList<Choice>();
    for (int i = 0; i < count; i++) {
      choices.add(new Choice("someChoice" + i));
    }
    return choices;
  }

  @Test
  public void test_getCurrentValueByIndex_customSteps_success() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_START_BOUND, "-3");
    parameters.put(VoteConfig.KEY_ITERATE_STEP, "-2");

    classUnderTest = SurveyUtilsTest.createBallotWithParametersAndChoices(parameters, createChoicesWithoutVotes(2));

    assertThat(classUnderTest.getBoundsIfNotDefault(), is("(-3--5)"));
    assertThat(classUnderTest.getCurrentValueByIndex(1), is(-5));
    assertThat(classUnderTest.getCurrentValueByIndex(0), is(-3));

    parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_START_BOUND, "-3");
    parameters.put(VoteConfig.KEY_ITERATE_STEP, "4");

    classUnderTest = SurveyUtilsTest.createBallotWithParametersAndChoices(parameters, createChoicesWithoutVotes(3));

    assertThat(classUnderTest.getBoundsIfNotDefault(), is("(-3-5)"));
    assertThat(classUnderTest.getCurrentValueByIndex(2), is(5));
    assertThat(classUnderTest.getCurrentValueByIndex(1), is(1));
    assertThat(classUnderTest.getCurrentValueByIndex(0), is(-3));
  }


  @Test
  public void test_computeAverage_TwoChoicesNoVotes_expectZero_success() {
    classUnderTest = SurveyUtilsTest.createDefaultBallotWithChoices(SurveyUtilsTest.SOME_BALLOT_TITLE, createChoicesWithoutVotes(2));

    final float result = classUnderTest.computeAverage();

    assertEquals(0.0f, result, 0.0f);
    assertThat(classUnderTest.getAveragePercentage(), is(0));
  }

  @Test
  public void test_computeAverage_TwoChoiceOneVote_success() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    someChoice.voteFor(SOME_EXISTING_USER_NAME);
    Choice someChoiceTwo = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "TWO");

    classUnderTest = SurveyUtilsTest.createDefaultBallotWithChoices(SurveyUtilsTest.SOME_BALLOT_TITLE, Arrays.asList(someChoice, someChoiceTwo));

    final float result = classUnderTest.computeAverage();

    assertEquals(2.0f, result, 0.0f);
  }

  @Test
  public void test_computeAverage_ThreeChoicesOneVoteOnSecond_success() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "TWO");
    someChoiceTwo.voteFor(SOME_EXISTING_USER_NAME + "TWO");
    Choice someChoiceThree = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "THREE");

    classUnderTest = SurveyUtilsTest.createDefaultBallotWithChoices(SurveyUtilsTest.SOME_BALLOT_TITLE, Arrays.asList(someChoice, someChoiceTwo, someChoiceThree));

    final float result = classUnderTest.computeAverage();

    assertEquals(2.0f, result, 0.0f);
    assertThat(classUnderTest.getAveragePercentage(), is(50));
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

    classUnderTest = SurveyUtilsTest.createBallotWithParametersAndChoices(parameters, Arrays.asList(someChoice, someChoiceTwo, someChoiceThree, someChoiceFour, someChoiceFive));

    final float result = classUnderTest.computeAverage();

    assertThat(result, is(equalTo(0.0f)));
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

    classUnderTest = SurveyUtilsTest.createBallotWithParametersAndChoices(parameters, Arrays.asList(someChoice, someChoiceTwo));

    final float result = classUnderTest.computeAverage();

    assertThat(result, is(equalTo(0.6666667F)));
    assertThat(classUnderTest.getAveragePercentage(), is(equalTo(66)));
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

    classUnderTest = SurveyUtilsTest.createBallotWithParametersAndChoices(parameters, Arrays.asList(someChoice, someChoiceTwo, someChoiceThree));

    final String format = "0.##";
    final String result = classUnderTest.computeFormattedAverage(format);

    assertEquals(new java.text.DecimalFormat(format).format(-3.0), result);
  }

  @Test
  public void test_getBounds_Default_success() {
    classUnderTest = SurveyUtilsTest.createDefaultBallotWithChoices("someBallot", createChoicesWithoutVotes(2));

    assertEquals(SurveyConfig.DEFAULT_START_BOUND, classUnderTest.getLowerBound());
    assertEquals(SurveyConfig.DEFAULT_START_BOUND + SurveyConfig.DEFAULT_START_BOUND * (classUnderTest.getChoices().size() - 1), classUnderTest.getUpperBound());
  }

  @Test
  public void test_getBoundsIfNotDefault_default_success() {
    classUnderTest = SurveyUtilsTest.createDefaultBallotWithChoices("someBallot", createChoicesWithoutVotes(2));

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

    classUnderTest = SurveyUtilsTest.createBallotWithParametersAndChoices(parameters, Arrays.asList(someChoice, someChoiceTwo));

    assertThat(classUnderTest.getBoundsIfNotDefault(), is(equalTo("(3-0)")));
  }

  @Test
  public void test_getBoundsIfNotDefault_oneDefault_success() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "TWO");

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, SurveyUtilsTest.SOME_BALLOT_TITLE);
    parameters.put(SurveyConfig.KEY_START_BOUND, "3");

    classUnderTest = SurveyUtilsTest.createBallotWithParametersAndChoices(parameters, Arrays.asList(someChoice, someChoiceTwo));

    assertThat(classUnderTest.getBoundsIfNotDefault(), is(equalTo("(3-4)")));

    parameters.put(SurveyConfig.KEY_START_BOUND, "1");
    parameters.put(SurveyConfig.KEY_ITERATE_STEP, "3");

    classUnderTest = SurveyUtilsTest.createBallotWithParametersAndChoices(parameters, Arrays.asList(someChoice, someChoiceTwo));

    assertThat(classUnderTest.getBoundsIfNotDefault(), is(equalTo("(1-4)")));
  }

  @Test
  public void test_getAllVoters_success() {
    Choice someChoice = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "TWO");
    someChoiceTwo.voteFor(SOME_EXISTING_USER_NAME + "TWO");
    Choice someChoiceThree = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "THREE");
    someChoiceThree.voteFor(SOME_EXISTING_USER_NAME + "THREE");

    classUnderTest = SurveyUtilsTest.createDefaultBallotWithChoices("someBallot", Arrays.asList(someChoice, someChoiceTwo, someChoiceThree));

    Collection<String> userList = new ArrayList<String>();
    userList.add(SOME_EXISTING_USER_NAME + "TWO");
    userList.add(SOME_EXISTING_USER_NAME + "THREE");

    final Collection<String> allVoters = classUnderTest.getAllVoters();

    assertEquals(userList, allVoters);
  }

  @Test
  public void test_getAllPossibleVoters_success() {
    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, "someBallot");
    parameters.put(VoteConfig.KEY_VOTERS, "group1,user2,group3");

    final PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);

    when(mockPermissionEvaluator.getActiveUsersForGroupOrUser("group1")).thenReturn(newArrayList("user11", "user12", "user13"));
    when(mockPermissionEvaluator.getActiveUsersForGroupOrUser("user2")).thenReturn(newArrayList("user2"));
    when(mockPermissionEvaluator.getActiveUsersForGroupOrUser("group3")).thenReturn(newArrayList("user31", "user32"));

    classUnderTest = new Ballot("someBallot", "subTitle", new VoteConfig(mockPermissionEvaluator, parameters), createChoicesWithoutVotes(2), new ArrayList<Comment>());

    // When:
    List<String> result = classUnderTest.getAllPossibleVoters();

    // Then:
    assertThat(result, containsInAnyOrder("user11", "user12", "user13", "user2", "user31", "user32"));
  }

  @Test
  public void test_getAllPendingVoters_success() {
    Choice someChoiceOne = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "ONE");
    someChoiceOne.voteFor("user21");
    Choice someChoiceTwo = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION + "TWO");
    someChoiceTwo.voteFor("user11");
    someChoiceOne.voteFor("user12"); // user has voted but is now deactivated
    someChoiceOne.voteFor("user4"); // user has voted but is now deleted
    someChoiceTwo.voteFor("user32");


    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, "someBallotTitle");
    parameters.put(VoteConfig.KEY_VOTERS, "group1, user2, group3");

    final PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);
    classUnderTest = new Ballot("someBallotTitle", "", new VoteConfig(mockPermissionEvaluator, parameters), newArrayList(someChoiceOne, someChoiceTwo), new ArrayList<Comment>());

    when(mockPermissionEvaluator.getActiveUsersForGroupOrUser("group1")).thenReturn(newArrayList("user11", "user12", "user13"));
    when(mockPermissionEvaluator.getActiveUsersForGroupOrUser("user2")).thenReturn(newArrayList("user2"));
    when(mockPermissionEvaluator.getActiveUsersForGroupOrUser("group3")).thenReturn(newArrayList("user31", "user32"));

    Collection<String> result = classUnderTest.getAllPendingVoters();

    assertThat(result, containsInAnyOrder("user13", "user2", "user31"));
  }

  @Test
  public void test_getEmailStringFor_success() {

    // Given:
    User user1 = new DefaultUser("user1", "user one", "user1@example.com");
    User user2 = new DefaultUser("user2", "user two", "user2@ext.example.com");

    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, "someTitle");
    parameters.put(VoteConfig.KEY_VOTERS, user1.getName() + "," + user2.getName());

    final PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);
    when(mockPermissionEvaluator.getActiveUsersForGroupOrUser(user1.getName())).thenReturn(newArrayList(user1.getName()));
    when(mockPermissionEvaluator.getActiveUsersForGroupOrUser(user2.getName())).thenReturn(newArrayList(user2.getName()));
    when(mockPermissionEvaluator.getUserByName(user1.getName())).thenReturn(user1);
    when(mockPermissionEvaluator.getUserByName(user2.getName())).thenReturn(user2);
    classUnderTest = new Ballot("someTitle", "", new VoteConfig(mockPermissionEvaluator, parameters), createChoicesWithoutVotes(2), new ArrayList<Comment>());

    // When:
    String result = classUnderTest.getEmailStringOfPendingVoters();

    // Then:
    assertThat(result, is("user1@example.com,user2@ext.example.com"));
  }


  @Test
  public void test_equalsAndHashCode_success() {
    classUnderTest = SurveyUtilsTest.createDefaultBallot(SurveyUtilsTest.SOME_BALLOT_TITLE);
    Ballot classUnderTest2 = new Ballot(SurveyUtilsTest.SOME_BALLOT_TITLE, "", SurveyUtilsTest.createDefaultVoteConfig(new HashMap<String, String>()), SurveyUtils.getDefaultChoices(), new ArrayList<Comment>());

    assertFalse(classUnderTest.equals("someString"));
    assertTrue(classUnderTest.equals(classUnderTest2));
    assertTrue(classUnderTest.hashCode() == classUnderTest2.hashCode());
    assertTrue(classUnderTest.toString().equals(classUnderTest2.toString()));

    classUnderTest2 = new Ballot(SurveyUtilsTest.SOME_BALLOT_TITLE, "someDesc", SurveyUtilsTest.createDefaultVoteConfig(new HashMap<String, String>()), SurveyUtils.getDefaultChoices(), new ArrayList<Comment>());
    assertThat(classUnderTest, is(equalTo(classUnderTest2)));
    assertTrue(classUnderTest.hashCode() == classUnderTest2.hashCode());
    assertFalse(classUnderTest.toString().equals(classUnderTest2.toString()));

    classUnderTest2 = new Ballot(SurveyUtilsTest.SOME_BALLOT_TITLE + "2", "", SurveyUtilsTest.createDefaultVoteConfig(new HashMap<String, String>()), SurveyUtils.getDefaultChoices(), new ArrayList<Comment>());
    assertFalse(classUnderTest.equals(classUnderTest2));
    assertFalse(classUnderTest.hashCode() == classUnderTest2.hashCode());
  }
}
