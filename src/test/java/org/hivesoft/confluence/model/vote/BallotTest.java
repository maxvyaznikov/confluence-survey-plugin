package org.hivesoft.confluence.model.vote;

import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.user.User;
import com.atlassian.user.impl.DefaultUser;
import org.hivesoft.confluence.macros.ConfluenceTestBase;
import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.macros.vote.VoteConfig;
import org.hivesoft.confluence.model.wrapper.AnonymousUser;
import org.hivesoft.confluence.model.wrapper.SurveyUser;
import org.hivesoft.confluence.utils.*;
import org.junit.Test;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BallotTest extends ConfluenceTestBase {

  private Ballot classUnderTest;

  @Test
  public void test_getTitle_success() {
    classUnderTest = new BallotBuilder().title(SOME_BALLOT_TITLE).build();
    assertThat(classUnderTest.getTitle(), is(equalTo(SOME_BALLOT_TITLE)));
  }

  @Test
  public void test_getTitleNoSpace_success() {
    classUnderTest = new BallotBuilder().title(SOME_BALLOT_TITLE + " more spaces").build();

    assertThat(classUnderTest.getTitleNoSpace(), is(equalTo(SOME_BALLOT_TITLE.toLowerCase() + "morespaces")));
  }

  @Test
  public void test_getDescription_success() {
    classUnderTest = new BallotBuilder().build();
    assertEquals("", classUnderTest.getDescription());

    classUnderTest = new BallotBuilder().description("someDescription").build();
    assertThat(classUnderTest.getDescription(), is("someDescription"));
  }

  @Test
  public void test_ballotsWithDefaults_success() {
    classUnderTest = new BallotBuilder().build();
    assertThat(classUnderTest.getConfig(), is(notNullValue()));
  }

  @Test
  public void test_getVoteForExistingUser_success() {
    Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
    someChoice.voteFor(SOME_USER1);

    classUnderTest = new BallotBuilder().title(SOME_BALLOT_TITLE).choices(Arrays.asList(someChoice)).build();

    Choice result = classUnderTest.getChoiceForUser(SOME_USER1);

    assertThat(result, is(someChoice));
    assertThat(classUnderTest.getHasVoted(SOME_USER1), is(true));
  }

  @Test
  public void test_getVoteForNotExistingUser_success() {
    classUnderTest = new BallotBuilder().title(SOME_BALLOT_TITLE).build();
    classUnderTest.getChoices().iterator().next().voteFor(SOME_USER1);

    Choice result = classUnderTest.getChoiceForUser(new DefaultUser("someDifferentNotExistingUser"));

    assertTrue(null == result);
    assertFalse(classUnderTest.getHasVoted(new DefaultUser("someDifferentNotExistingUser")));
  }

  @Test
  public void test_getChoice_success() {
    classUnderTest = new BallotBuilder().title(SOME_BALLOT_TITLE).build();
    final Choice someChoice = classUnderTest.getChoices().iterator().next();

    Choice result = classUnderTest.getChoice(SurveyUtils.getDefaultChoices().get(0).getDescription());

    assertEquals(someChoice, result);
  }

  @Test
  public void test_getChoice_NotExists_failure() {
    classUnderTest = new BallotBuilder().title(SOME_BALLOT_TITLE).build();

    Choice result = classUnderTest.getChoice("NotExistingChoice");

    assertThat(result, is(nullValue()));
  }

  @Test
  public void test_getChoices_DefaultChoices_failure() {
    classUnderTest = new BallotBuilder().title(SOME_BALLOT_TITLE).build();
    Collection<Choice> result = classUnderTest.getChoices();

    assertThat(result.size(), is(5));
  }

  @Test
  public void test_getPercentageOfVoteForChoice_NoVotes_success() {
    classUnderTest = new BallotBuilder().title(SOME_BALLOT_TITLE).build();

    final Choice someChoice = classUnderTest.getChoices().iterator().next();

    final int percentageResult = classUnderTest.getPercentageOfVoteForChoice(someChoice);

    assertThat(percentageResult, is(0));
  }

  @Test
  public void test_getPercentageOfVoteForChoice_success() {
    final Choice someChoice = new Choice("choice1");
    final Choice someChoiceTwo = new Choice("choice2");

    someChoice.voteFor(new DefaultUser("someUserOne"));
    someChoice.voteFor(new DefaultUser("someUserTwo"));
    someChoiceTwo.voteFor(new DefaultUser("someUserThree"));

    classUnderTest = SurveyUtilsTest.createDefaultBallotWithChoices(SOME_BALLOT_TITLE, Arrays.asList(someChoice, someChoiceTwo));

    final int percentageResult = classUnderTest.getPercentageOfVoteForChoice(someChoice);
    final int percentageResultTwo = classUnderTest.getPercentageOfVoteForChoice(someChoiceTwo);

    assertThat(percentageResult, is(66));
    assertThat(percentageResultTwo, is(33));
  }

  @Test
  public void test_getComments_success() {
    Comment someComment = new Comment(SOME_USER1, "some crazy comment for a crazy plugin");

    classUnderTest = new BallotBuilder().title(SOME_BALLOT_TITLE).comments(Arrays.asList(someComment)).build();

    final List<Comment> result = classUnderTest.getComments();

    assertThat(result, hasSize(1));
    assertThat(result.get(0), is(someComment));
  }

  @Test
  public void test_getCommentForUser_success() {
    final String commentString = "some crazy comment for a crazy plugin";
    Comment someComment = new Comment(SOME_USER1, commentString);

    classUnderTest = new BallotBuilder().title(SOME_BALLOT_TITLE).comments(Arrays.asList(someComment)).build();

    final Comment result = classUnderTest.getCommentForUser(SOME_USER1);

    assertThat(result, is(someComment));
    assertThat(someComment.getComment(), is(commentString));
  }

  @Test
  public void test_getCommentForUser_noComment_success() {
    classUnderTest = new BallotBuilder().title("someBallot").build();

    final Comment result = classUnderTest.getCommentForUser(SOME_USER1);

    assertThat(result, is(nullValue()));
  }

  @Test
  public void test_getCurrentValueByIndex_defaultSteps_success() {
    classUnderTest = new BallotBuilder().title(SOME_BALLOT_TITLE).build();

    assertThat(classUnderTest.getCurrentValueByIndex(1), is(2));
    assertThat(classUnderTest.getCurrentValueByIndex(0), is(1));
  }


  @Test
  public void test_getCurrentValueByIndex_customSteps_success() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_START_BOUND, "-3");
    parameters.put(VoteConfig.KEY_ITERATE_STEP, "-2");

    classUnderTest = new BallotBuilder().parameters(parameters).choices(createChoicesWithoutVotes(2)).build();

    assertThat(classUnderTest.getBoundsIfNotDefault(), is("(-3--5)"));
    assertThat(classUnderTest.getCurrentValueByIndex(1), is(-5));
    assertThat(classUnderTest.getCurrentValueByIndex(0), is(-3));

    parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_START_BOUND, "-3");
    parameters.put(VoteConfig.KEY_ITERATE_STEP, "4");

    classUnderTest = new BallotBuilder().parameters(parameters).choices(createChoicesWithoutVotes(3)).build();

    assertThat(classUnderTest.getBoundsIfNotDefault(), is("(-3-5)"));
    assertThat(classUnderTest.getCurrentValueByIndex(2), is(5));
    assertThat(classUnderTest.getCurrentValueByIndex(1), is(1));
    assertThat(classUnderTest.getCurrentValueByIndex(0), is(-3));
  }


  @Test
  public void test_computeAverage_TwoChoicesNoVotes_expectZero_success() {
    classUnderTest = new BallotBuilder().choices(createChoicesWithoutVotes(2)).build();

    final float result = classUnderTest.computeAverage();

    assertThat(result, is(0.0f));
    assertThat(classUnderTest.getAveragePercentage(), is(0));
  }

  @Test
  public void test_computeAverage_TwoChoiceOneVote_success() {
    Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
    someChoice.voteFor(SOME_USER1);
    Choice someChoiceTwo = new Choice(SOME_CHOICE_DESCRIPTION + "TWO");

    classUnderTest = new BallotBuilder().choices(Arrays.asList(someChoice, someChoiceTwo)).build();

    final float result = classUnderTest.computeAverage();

    assertThat(result, is(2.0f));
  }

  @Test
  public void test_computeAverage_ThreeChoicesOneVoteOnSecond_success() {
    Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SOME_CHOICE_DESCRIPTION + "TWO");
    someChoiceTwo.voteFor(SOME_USER2);
    Choice someChoiceThree = new Choice(SOME_CHOICE_DESCRIPTION + "THREE");

    classUnderTest = new BallotBuilder().choices(Arrays.asList(someChoice, someChoiceTwo, someChoiceThree)).build();

    final float result = classUnderTest.computeAverage();

    assertThat(result, is(2.0f));
    assertThat(classUnderTest.getAveragePercentage(), is(50));
  }

  @Test
  public void test_computeAverage_NegativeIterateStepFromPositiveStart_FiveChoicesOneVoteOnFourth_expect40percent_success() {
    Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SOME_CHOICE_DESCRIPTION + "TWO");
    Choice someChoiceThree = new Choice(SOME_CHOICE_DESCRIPTION + "THREE");
    Choice someChoiceFour = new Choice(SOME_CHOICE_DESCRIPTION + "FOUR");
    someChoiceFour.voteFor(SOME_USER2);
    Choice someChoiceFive = new Choice(SOME_CHOICE_DESCRIPTION + "FIVE");

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, SOME_BALLOT_TITLE);
    parameters.put(SurveyConfig.KEY_START_BOUND, "4");
    parameters.put(SurveyConfig.KEY_ITERATE_STEP, "-4");

    classUnderTest = new BallotBuilder().parameters(parameters).choices(Arrays.asList(someChoice, someChoiceTwo, someChoiceThree, someChoiceFour, someChoiceFive)).build();

    final float result = classUnderTest.computeAverage();

    assertThat(result, is(equalTo(0.0f)));
    assertThat(classUnderTest.getAveragePercentage(), is(25));
  }

  @Test
  public void test_computeAverage_ZeroToOne_TwoChoicesTwoOnFirstOneOnSecond_expect50percent_success() {
    Choice someChoice = new Choice("one");
    someChoice.voteFor(SOME_USER1);
    someChoice.voteFor(new SurveyUser("someUserTHREE"));
    Choice someChoiceTwo = new Choice("two");
    someChoiceTwo.voteFor(SOME_USER2);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, SOME_BALLOT_TITLE);
    parameters.put(SurveyConfig.KEY_START_BOUND, "0");
    parameters.put(SurveyConfig.KEY_ITERATE_STEP, "1");

    classUnderTest = new BallotBuilder().parameters(parameters).choices(Arrays.asList(someChoice, someChoiceTwo)).build();

    final float result = classUnderTest.computeAverage();

    assertThat(result, is(equalTo(0.6666667F)));
    assertThat(classUnderTest.getAveragePercentage(), is(equalTo(66)));
  }

  @Test
  public void test_computeFormattedAverage_NegativeIterateStep_ThreeChoicesThreeVotes_success() {
    Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SOME_CHOICE_DESCRIPTION + "TWO");
    someChoiceTwo.voteFor(SOME_USER1);
    someChoiceTwo.voteFor(SOME_USER2);
    Choice someChoiceThree = new Choice(SOME_CHOICE_DESCRIPTION + "THREE");
    someChoiceThree.voteFor(new SurveyUser("someUserTHREE"));

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, SOME_BALLOT_TITLE);
    parameters.put(SurveyConfig.KEY_START_BOUND, "-1");
    parameters.put(SurveyConfig.KEY_ITERATE_STEP, "-3");

    classUnderTest = new BallotBuilder().parameters(parameters).choices(Arrays.asList(someChoice, someChoiceTwo, someChoiceThree)).build();

    final String format = "0.##";
    final String result = classUnderTest.computeFormattedAverage(format);

    assertThat(result, is(new java.text.DecimalFormat(format).format(-3.0)));
  }

  @Test
  public void test_getBounds_Default_success() {
    classUnderTest = new BallotBuilder().choices(createChoicesWithoutVotes(2)).build();

    assertThat(classUnderTest.getLowerBound(), is(SurveyConfig.DEFAULT_START_BOUND));
    assertThat(classUnderTest.getUpperBound(), is(SurveyConfig.DEFAULT_START_BOUND + SurveyConfig.DEFAULT_START_BOUND * (classUnderTest.getChoices().size() - 1)));
  }

  @Test
  public void test_getBounds_NotDefault_success() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, SOME_BALLOT_TITLE);
    parameters.put(SurveyConfig.KEY_START_BOUND, "-1");
    parameters.put(SurveyConfig.KEY_ITERATE_STEP, "-3");

    classUnderTest = new BallotBuilder().parameters(parameters).choices(createChoicesWithoutVotes(2)).build();

    assertThat(classUnderTest.getLowerBound(), is(-4));
    assertThat(classUnderTest.getUpperBound(), is(-1));
  }

  @Test
  public void test_getBoundsIfNotDefault_default_success() {
    classUnderTest = new BallotBuilder().choices(createChoicesWithoutVotes(2)).build();

    assertThat(classUnderTest.getBoundsIfNotDefault(), is(""));
  }

  @Test
  public void test_getBoundsIfNotDefault_notDefault_success() {
    Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SOME_CHOICE_DESCRIPTION + "TWO");

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, SOME_BALLOT_TITLE);
    parameters.put(SurveyConfig.KEY_START_BOUND, "3");
    parameters.put(SurveyConfig.KEY_ITERATE_STEP, "-3");

    classUnderTest = new BallotBuilder().parameters(parameters).choices(Arrays.asList(someChoice, someChoiceTwo)).build();

    assertThat(classUnderTest.getBoundsIfNotDefault(), is(equalTo("(3-0)")));
  }

  @Test
  public void test_getBoundsIfNotDefault_oneDefault_success() {
    Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SOME_CHOICE_DESCRIPTION + "TWO");

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, SOME_BALLOT_TITLE);
    parameters.put(SurveyConfig.KEY_START_BOUND, "3");

    classUnderTest = new BallotBuilder().parameters(parameters).choices(Arrays.asList(someChoice, someChoiceTwo)).build();

    assertThat(classUnderTest.getBoundsIfNotDefault(), is(equalTo("(3-4)")));

    parameters.put(SurveyConfig.KEY_START_BOUND, "1");
    parameters.put(SurveyConfig.KEY_ITERATE_STEP, "3");

    classUnderTest = new BallotBuilder().parameters(parameters).choices(Arrays.asList(someChoice, someChoiceTwo)).build();

    assertThat(classUnderTest.getBoundsIfNotDefault(), is(equalTo("(1-4)")));
  }

  @Test
  public void test_getAllVoters_success() {
    Choice someChoice = new Choice(SOME_CHOICE_DESCRIPTION);
    Choice someChoiceTwo = new Choice(SOME_CHOICE_DESCRIPTION + "TWO");
    someChoiceTwo.voteFor(SOME_USER2);
    Choice someChoiceThree = new Choice(SOME_CHOICE_DESCRIPTION + "THREE");
    final User userThree = new SurveyUser("someUserTHREE");
    someChoiceThree.voteFor(userThree);

    classUnderTest = new BallotBuilder().choices(Arrays.asList(someChoice, someChoiceTwo, someChoiceThree)).build();

    final Collection<User> allVoters = classUnderTest.getAllVoters();

    assertThat(allVoters, containsInAnyOrder(SOME_USER2, userThree));
  }

  @Test
  public void test_getAllPossibleVoters_success() {
    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, "someBallot");
    parameters.put(VoteConfig.KEY_VOTERS, "group1,user2,group3");

    final PermissionEvaluatorImpl mockPermissionEvaluator = mock(PermissionEvaluatorImpl.class);

    final User user11 = new SurveyUser("user11");
    when(mockPermissionEvaluator.getActiveUsersForGroupOrUser("group1")).thenReturn(newArrayList(user11, new SurveyUser("user12"), new SurveyUser("user13")));
    when(mockPermissionEvaluator.getActiveUsersForGroupOrUser("user2")).thenReturn(newArrayList(SOME_USER2));
    final User user31 = new SurveyUser("user31");
    when(mockPermissionEvaluator.getActiveUsersForGroupOrUser("group3")).thenReturn(newArrayList(user31, new SurveyUser("user32")));

    classUnderTest = new Ballot("someBallot", "subTitle", new VoteConfig(mockPermissionEvaluator, parameters), createChoicesWithoutVotes(2), new ArrayList<Comment>());

    List<User> result = classUnderTest.getAllPossibleVoters();

    assertThat(result, containsInAnyOrder(user11, new SurveyUser("user12"), new SurveyUser("user13"), SOME_USER2, user31, new SurveyUser("user32")));
  }

  @Test
  public void test_getAllPendingVoters_success() {
    final User user11 = new SurveyUser("user11");
    final User user12 = new SurveyUser("user12");
    final User user13 = new SurveyUser("user13");

    final User user21 = new SurveyUser("user21");
    final User user22 = new SurveyUser("user22");

    final User user31 = new SurveyUser("user31");
    final User user32 = new SurveyUser("user32");

    final User user4 = new SurveyUser("user4");

    Choice someChoiceOne = new Choice(SOME_CHOICE_DESCRIPTION + "ONE");
    Choice someChoiceTwo = new Choice(SOME_CHOICE_DESCRIPTION + "TWO");

    someChoiceOne.voteFor(user21);
    someChoiceOne.voteFor(user12); // user has voted but is now deactivated
    someChoiceOne.voteFor(user4); // user has voted but is now deleted
    someChoiceTwo.voteFor(user11);
    someChoiceTwo.voteFor(user32);

    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, "someBallotTitle");
    parameters.put(VoteConfig.KEY_VOTERS, "group1, " + user22.getName() + ", group2, group3");

    HashMap<String, List<String>> groupsWithUsers = new HashMap<String, List<String>>();
    groupsWithUsers.put("group1",Arrays.asList(user11.getName(),user12.getName(),user13.getName()));
    groupsWithUsers.put(user22.getName(),Arrays.asList(user22.getName()));
    groupsWithUsers.put("group2",Arrays.asList(user21.getName(),user22.getName()));
    groupsWithUsers.put("group3",Arrays.asList(user31.getName(), user32.getName()));

    PermissionEvaluator permissionEvaluator = new TestPermissionEvaluator.Builder(SOME_USER1)
            .groupsWithUsers(groupsWithUsers)
            .build();

    classUnderTest = new BallotBuilder().parameters(parameters).choices(Arrays.asList(someChoiceOne, someChoiceTwo)).permissionEvaluator(permissionEvaluator).build();

    List<User> result = classUnderTest.getAllPendingVoters();

    assertThat(result, containsInAnyOrder(user13, user22, user31));
  }

  @Test
  public void test_getEmailStringOfPendingVoters_success() {
    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, "someTitle");
    parameters.put(VoteConfig.KEY_VOTERS, SOME_USER1.getName() + "," + SOME_USER2.getName());

    final PermissionEvaluatorImpl mockPermissionEvaluator = mock(PermissionEvaluatorImpl.class);
    when(mockPermissionEvaluator.getActiveUsersForGroupOrUser(SOME_USER1.getName())).thenReturn(newArrayList(SOME_USER1));
    when(mockPermissionEvaluator.getActiveUsersForGroupOrUser(SOME_USER2.getName())).thenReturn(newArrayList(SOME_USER2));
    classUnderTest = new Ballot("someTitle", "", new VoteConfig(mockPermissionEvaluator, parameters), createChoicesWithoutVotes(2), new ArrayList<Comment>());

    String result = classUnderTest.getEmailStringOfPendingVoters();

    assertThat(result, is(SOME_USER1.getEmail() + "," + SOME_USER2.getEmail()));
  }

  @Test
  public void test_getEmailStringOfAllVoters_success() {
    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, "someTitle");
    parameters.put(VoteConfig.KEY_VOTERS, SOME_USER1.getName() + "," + SOME_USER2.getName());

    final PermissionEvaluatorImpl mockPermissionEvaluator = mock(PermissionEvaluatorImpl.class);
    when(mockPermissionEvaluator.getActiveUsersForGroupOrUser(SOME_USER1.getName())).thenReturn(newArrayList(SOME_USER1));
    when(mockPermissionEvaluator.getActiveUsersForGroupOrUser(SOME_USER2.getName())).thenReturn(newArrayList(SOME_USER2));
    classUnderTest = new Ballot("someTitle", "", new VoteConfig(mockPermissionEvaluator, parameters), createChoicesWithoutVotes(2), new ArrayList<Comment>());

    classUnderTest.getChoices().iterator().next().voteFor(SOME_USER1);

    String result = classUnderTest.getEmailStringOfAllVoters();

    assertThat(result, is(SOME_USER1.getEmail()));
  }

  @Test
  public void test_equalsAndHashCode_success() {
    classUnderTest = new BallotBuilder().title(SOME_BALLOT_TITLE).build();
    Ballot classUnderTest2 = new BallotBuilder().title(SOME_BALLOT_TITLE).build();

    assertFalse(classUnderTest.equals("someString"));
    assertTrue(classUnderTest.equals(classUnderTest2));
    assertTrue(classUnderTest.hashCode() == classUnderTest2.hashCode());
    assertTrue(classUnderTest.toString().equals(classUnderTest2.toString()));

    classUnderTest2 = new Ballot(SOME_BALLOT_TITLE, "someDesc", SurveyUtilsTest.createDefaultVoteConfig(new HashMap<String, String>()), SurveyUtils.getDefaultChoices(), new ArrayList<Comment>());
    assertThat(classUnderTest, is(equalTo(classUnderTest2)));
    assertTrue(classUnderTest.hashCode() == classUnderTest2.hashCode());
    assertFalse(classUnderTest.toString().equals(classUnderTest2.toString()));

    classUnderTest2 = new Ballot(SOME_BALLOT_TITLE + "2", "", SurveyUtilsTest.createDefaultVoteConfig(new HashMap<String, String>()), SurveyUtils.getDefaultChoices(), new ArrayList<Comment>());
    assertFalse(classUnderTest.equals(classUnderTest2));
    assertFalse(classUnderTest.hashCode() == classUnderTest2.hashCode());
  }

  @Test
  public void test_getTitleAndDescriptionWithRenderedLinks() {
    classUnderTest = new Ballot("i am a choice to http://google.de but https://www.google.com is also ok", "someLinkDescription: http://www.heise.de", SurveyUtilsTest.createDefaultVoteConfig(new HashMap<String, String>()), SurveyUtils.getDefaultChoices(), new ArrayList<Comment>());
    assertThat(classUnderTest.getTitleWithRenderedLinks(),
            is("i am a choice to <a href=\"http://google.de\" target=\"_blank\">http://google.de</a> but <a href=\"https://www.google.com\" target=\"_blank\">https://www.google.com</a> is also ok"));
    assertThat(classUnderTest.getDescriptionWithRenderedLinks(),
            is("someLinkDescription: <a href=\"http://www.heise.de\" target=\"_blank\">http://www.heise.de</a>"));
  }

  @Test
  public void test_getCanVote_emptyUser_success() {
    classUnderTest = new BallotBuilder()
            .title(SOME_BALLOT_TITLE)
            .permissionEvaluator(new TestPermissionEvaluator.Builder(new AnonymousUser()).build()).build();

    final Boolean canVote = classUnderTest.canVote(null);

    assertThat(canVote, is(false));
  }

  @Test
  public void test_getCanVote_userNotVoted_unrestricted_success() {
    classUnderTest = new BallotBuilder().title(SOME_BALLOT_TITLE).choices(createChoicesWithoutVotes(2)).build();

    final Boolean canVote = classUnderTest.canVote(SOME_USER1);

    assertThat(canVote, is(true));
  }

  @Test
  public void test_getCanVote_userNotVoted_notInVotersList_success() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_VOTERS, "notThisUser, notThisUserEither");

    classUnderTest = new BallotBuilder().parameters(parameters).build();

    final Boolean canVote = classUnderTest.canVote(SOME_USER1);

    assertThat(canVote, is(false));
  }

  @Test
  public void test_getCanVote_inListViaGroup_success() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, SOME_BALLOT_TITLE);

    List<String> voters = Arrays.asList("notThisUser", "notThisUserEither", "userIsInThisGroup");

    parameters.put(VoteConfig.KEY_VOTERS, StringUtils.join(voters, ','));
    parameters.put(VoteConfig.KEY_CHANGEABLE_VOTES, "true");

    HashMap<String, List<String>> groupsWithUsers = new HashMap<String, List<String>>();
    groupsWithUsers.put("userIsInThisGroup", Arrays.asList(SOME_USER1.getName()));
    classUnderTest = new BallotBuilder().parameters(parameters)
            .permissionEvaluator(new TestPermissionEvaluator.Builder(SOME_USER1)
                    .groupsWithUsers(groupsWithUsers).build()).choices(createChoicesWithoutVotes(2)).build();

    final Boolean canVote = classUnderTest.canVote(SOME_USER1);

    assertThat(canVote, is(true));
  }
}
