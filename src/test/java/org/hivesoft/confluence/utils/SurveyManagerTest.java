package org.hivesoft.confluence.utils;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.Comment;
import com.atlassian.confluence.pages.Page;
import com.atlassian.renderer.v2.macro.MacroException;
import com.opensymphony.xwork.ActionContext;
import org.hivesoft.confluence.macros.ConfluenceTestBase;
import org.hivesoft.confluence.macros.vote.VoteConfig;
import org.hivesoft.confluence.macros.vote.VoteMacro;
import org.hivesoft.confluence.model.Survey;
import org.hivesoft.confluence.model.enums.VoteAction;
import org.hivesoft.confluence.model.vote.Ballot;
import org.hivesoft.confluence.model.vote.Choice;
import org.hivesoft.confluence.model.wrapper.SurveyUser;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class SurveyManagerTest extends ConfluenceTestBase {
  private final ContentPropertyManager mockContentPropertyManager = mock(ContentPropertyManager.class);
  private final PermissionEvaluatorImpl mockPermissionEvaluator = mock(PermissionEvaluatorImpl.class);

  private SurveyManager classUnderTest;

  @Before
  public void setup() {
    when(mockPermissionEvaluator.getRemoteUser()).thenReturn(SOME_USER1);

    classUnderTest = new SurveyManager(mockContentPropertyManager, mockPermissionEvaluator);
  }

  @Test
  public void test_reconstructBallot_noChoices_success() throws MacroException {
    final Ballot reconstructedBallot = classUnderTest.reconstructBallotFromPlainTextMacroBody(parametersWithTitle(), "", new Page());

    assertThat(reconstructedBallot.getTitle(), is("someTitle"));
    assertThat(reconstructedBallot.getChoices(), hasSize(0));
  }

  private static Map<String, String> parametersWithTitle() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, "someTitle");
    return parameters;
  }

  @Test
  public void test_reconstructBallot_someChoices_noVoter_success() throws MacroException {
    String someChoices = "someChoice1\r\nsomeChoice2";

    final Ballot reconstructedBallot = classUnderTest.reconstructBallotFromPlainTextMacroBody(parametersWithTitle(), someChoices, new Page());

    assertThat(reconstructedBallot.getTitle(), is("someTitle"));
    assertThat(reconstructedBallot.getChoice("someChoice1").getDescription(), is("someChoice1"));
    assertThat(reconstructedBallot.getChoice("someChoice2").getDescription(), is("someChoice2"));
    assertThat(reconstructedBallot.getChoice("someChoice1").getVoters(), hasSize(0));
  }

  @Test
  public void test_reconstructBallot_someChoices_withVoter_success() throws MacroException {
    String someChoices = "someChoice1\r\nsomeChoice2";

    when(mockContentPropertyManager.getTextProperty(any(ContentEntityObject.class), anyString())).thenReturn(SOME_USER1.getName());
    when(mockPermissionEvaluator.getUserByName(SOME_USER1.getName())).thenReturn(SOME_USER1);

    final Ballot reconstructedBallot = classUnderTest.reconstructBallotFromPlainTextMacroBody(parametersWithTitle(), someChoices, new Page());

    assertThat(reconstructedBallot.getTitle(), is("someTitle"));
    assertThat(reconstructedBallot.getChoice("someChoice1").getDescription(), is("someChoice1"));
    assertThat(reconstructedBallot.getChoice("someChoice2").getDescription(), is("someChoice2"));
    assertThat(reconstructedBallot.getChoice("someChoice1").getVoters(), hasSize(1));
    assertThat(reconstructedBallot.getChoice("someChoice1").getHasVotedFor(SOME_USER1), is(true));
  }

  @Test
  public void test_reconstructSurvey_noParametersWithTitle_success() {
    final Survey returnedSurvey = classUnderTest.reconstructSurveyFromPlainTextMacroBody("", new Page(), parametersWithTitle());

    assertThat(returnedSurvey.getBallots(), hasSize(0));
    assertThat(returnedSurvey.getTitle(), is("someTitle"));
  }

  @Test
  public void test_reconstructSurvey_oneParameterDefaultChoices_success() {
    final String someBallotTitle1 = "someBallotTitle1";
    final Survey returnedSurvey = classUnderTest.reconstructSurveyFromPlainTextMacroBody(someBallotTitle1, new Page(), new HashMap<String, String>());

    assertThat(returnedSurvey.getBallot(someBallotTitle1).getTitle(), is(someBallotTitle1));
    assertThat(returnedSurvey.getBallot(someBallotTitle1).getChoices(), hasSize(5));
    assertThat(returnedSurvey.getTitle(), is(""));
  }

  @Test
  public void test_reconstructSurvey_oneParameterCustomChoices_success() {
    final String someBallotTitle1 = "someBallotTitle1";
    final Survey returnedSurvey = classUnderTest.reconstructSurveyFromPlainTextMacroBody(someBallotTitle1 + " - subTitle - choice1 - choice2", new Page(), new HashMap<String, String>());

    assertThat(returnedSurvey.getBallot(someBallotTitle1).getTitle(), is(someBallotTitle1));
    assertThat(returnedSurvey.getBallot(someBallotTitle1).getChoices(), hasSize(2));
    assertThat(returnedSurvey.getBallot(someBallotTitle1).getChoices().iterator().next().getDescription(), is(equalTo("choice1")));
  }

  @Test
  public void test_reconstructSurvey_twoParameters_success() {
    final String someBallotTitle1 = "someBallotTitle1";
    final String someBallotTitle2 = "someBallotTitle2";
    final String someBallotDescription1 = "someBallotDescription1";

    final Survey returnedSurvey = classUnderTest.reconstructSurveyFromPlainTextMacroBody(someBallotTitle1 + " - " + someBallotDescription1 + "\r\n" + someBallotTitle2, new Page(), parametersWithTitle());

    assertThat(returnedSurvey.getBallot(someBallotTitle1).getTitle(), is(equalTo(someBallotTitle1)));
    assertThat(returnedSurvey.getBallot(someBallotTitle1).getDescription(), is(equalTo(someBallotDescription1)));
    assertThat(returnedSurvey.getBallot(someBallotTitle2).getTitle(), is(equalTo(someBallotTitle2)));
    assertThat(returnedSurvey.getBallot(someBallotTitle2).getChoices(), hasSize(5));
  }

  @Test
  public void test_reconstructSurvey_twoParametersWithCommenter_success() {
    final String someBallotTitle1 = "someBallotTitle1";
    final String someBallotTitle2 = "someBallotTitle2";

    final Page somePage = new Page();

    final String userName1 = SOME_USER1.getName();
    final String userName2 = SOME_USER2.getName();
    final String commentUsers = userName1 + SurveyManager.COMMENTERS_SEPARATOR + userName2;
    final String commentForUser1 = "someComment";
    final String commentForUser2 = "another Comment";
    when(mockContentPropertyManager.getTextProperty(somePage, "survey." + someBallotTitle1 + ".commenters")).thenReturn(commentUsers);
    when(mockContentPropertyManager.getTextProperty(somePage, "survey." + someBallotTitle1 + ".comment." + userName1)).thenReturn(commentForUser1);
    when(mockContentPropertyManager.getTextProperty(somePage, "survey." + someBallotTitle1 + ".comment." + userName2)).thenReturn(commentForUser2);
    when(mockPermissionEvaluator.getUserByName(userName1)).thenReturn(SOME_USER1);
    when(mockPermissionEvaluator.getUserByName(userName2)).thenReturn(SOME_USER2);

    final Survey returnedSurvey = classUnderTest.reconstructSurveyFromPlainTextMacroBody(someBallotTitle1 + "\r\n" + someBallotTitle2, somePage, parametersWithTitle());

    final Ballot returnedBallot1 = returnedSurvey.getBallot(someBallotTitle1);

    assertThat(returnedBallot1.getTitle(), is(someBallotTitle1));
    assertThat(returnedSurvey.getBallot(someBallotTitle2).getTitle(), is(someBallotTitle2));
    assertThat(returnedBallot1.getCommentForUser(SOME_USER1).getComment(), is(commentForUser1));
    assertThat(returnedBallot1.getCommentForUser(SOME_USER2).getComment(), is(commentForUser2));
    assertThat(returnedBallot1.getComments(), hasSize(2));
  }

  @Test
  public void test_reconstructSurvey_twoParametersWithVotes_success() {
    final String someBallotTitle1 = "someBallotTitle1";
    final String someBallotTitle2 = "someBallotTitle2";

    final Page somePage = new Page();

    when(mockContentPropertyManager.getTextProperty(somePage, VoteMacro.VOTE_STORAGE_PREFIX + someBallotTitle1 + ".5-Outstanding")).thenReturn(SOME_USER1.getName());
    when(mockPermissionEvaluator.getUserByName(SOME_USER1.getName())).thenReturn(SOME_USER1);

    final Survey returnedSurvey = classUnderTest.reconstructSurveyFromPlainTextMacroBody(someBallotTitle1 + "\r\n" + someBallotTitle2, somePage, parametersWithTitle());

    assertThat(returnedSurvey.getBallot(someBallotTitle1).getTitle(), is(someBallotTitle1));
    assertThat(returnedSurvey.getBallot(someBallotTitle2).getTitle(), is(someBallotTitle2));
    assertThat(returnedSurvey.getBallot(someBallotTitle1).getChoice("5-Outstanding").getHasVotedFor(SOME_USER1), is(true));
  }

  @Test
  public void test_recordVote_noUser_success() {
    Ballot ballot = new BallotBuilder().build();
    when(mockPermissionEvaluator.getRemoteUser()).thenReturn(new SurveyUser("someUser"));

    classUnderTest.recordVote(ballot, new Page(), "someChoiceDoesn'tMatter", VoteAction.VOTE);

    verify(mockContentPropertyManager, times(0)).setTextProperty(any(ContentEntityObject.class), anyString(), anyString());
  }

  @Test
  public void test_recordVote_freshVote_success() {
    List<Choice> choicesWithoutVotes = createChoicesWithoutVotes(2);
    Choice choiceToVoteOn = choicesWithoutVotes.get(0);

    Ballot ballot = new BallotBuilder().title(SOME_BALLOT_TITLE).choices(choicesWithoutVotes).build();

    classUnderTest.recordVote(ballot, new Page(), choiceToVoteOn.getDescription(), VoteAction.VOTE);

    verify(mockContentPropertyManager, times(1)).setTextProperty(any(ContentEntityObject.class), anyString(), anyString());
  }

  @Test
  public void test_recordVote_alreadyVotedOnDifferentChangeAbleVotesTrue_success() {
    Choice choiceAlreadyVotedOn = new Choice("already Voted on");
    Choice choiceToVoteOn = new Choice(SOME_CHOICE_DESCRIPTION);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, SOME_BALLOT_TITLE);
    parameters.put(VoteConfig.KEY_CHANGEABLE_VOTES, "true");

    Ballot ballot = new BallotBuilder().parameters(parameters).choices(Arrays.asList(choiceAlreadyVotedOn, choiceToVoteOn)).build();

    choiceAlreadyVotedOn.voteFor(SOME_USER1);

    //when(mockPermissionEvaluator.canVote(any(User.class), any(Ballot.class))).thenReturn(true);

    classUnderTest.recordVote(ballot, new Page(), choiceToVoteOn.getDescription(), VoteAction.VOTE);

    verify(mockContentPropertyManager, times(2)).setTextProperty(any(ContentEntityObject.class), anyString(), anyString());
  }

  @Test
  public void test_recordVote_alreadyVotedOnUnvoteChangeAbleVotesTrue_success() {
    Choice choiceAlreadyVotedOn = new Choice("already Voted on");

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, SOME_BALLOT_TITLE);
    parameters.put(VoteConfig.KEY_CHANGEABLE_VOTES, "true");

    Ballot ballot = new BallotBuilder().parameters(parameters).choices(Arrays.asList(choiceAlreadyVotedOn)).build();

    choiceAlreadyVotedOn.voteFor(SOME_USER1);

    //when(mockPermissionEvaluator.canVote(any(User.class), any(Ballot.class))).thenReturn(true);

    classUnderTest.recordVote(ballot, new Page(), choiceAlreadyVotedOn.getDescription(), VoteAction.UNVOTE);

    verify(mockContentPropertyManager, times(1)).setTextProperty(any(ContentEntityObject.class), anyString(), anyString());
  }

  @Test
  public void test_recordVote_alreadyVotedOnDifferentChangeAbleVotesFalse_success() {
    Ballot ballot = new BallotBuilder().build();

    ballot.getChoices().iterator().next().voteFor(SOME_USER1);

    classUnderTest.recordVote(ballot, new Page(), "choiceDoesn'tMatter", VoteAction.VOTE);

    verify(mockContentPropertyManager, times(0)).setTextProperty(any(ContentEntityObject.class), anyString(), anyString());
  }

  @Test
  public void test_getPageEntityFromConversionContext_startingWithComment() {
    ConversionContext mockConversionContext = mock(ConversionContext.class);

    Page page = new Page();
    page.setId(123L);
    final Comment comment = new Comment();
    comment.setOwner(page);
    when(mockConversionContext.getEntity()).thenReturn(comment);

    final ContentEntityObject pageEntity = classUnderTest.getPageEntityFromConversionContext(mockConversionContext);

    assertThat(pageEntity, is(instanceOf(Page.class)));
  }

  @Test
  public void test_getPageEntityFromConversionContext_startingWithPage() {
    ConversionContext mockConversionContext = mock(ConversionContext.class);

    Page page = new Page();
    page.setId(123L);
    when(mockConversionContext.getEntity()).thenReturn(page);

    final ContentEntityObject pageEntity = classUnderTest.getPageEntityFromConversionContext(mockConversionContext);

    assertThat(pageEntity, is(instanceOf(Page.class)));
  }

  @Test
  public void test_resetVotes_votesAndCommentsReset_success() {
    final Survey survey = new Survey(SurveyUtilsTest.createDefaultSurveyConfig(new HashMap<String, String>()));
    final String someBallotTitle = "someBallot";
    final Ballot someBallot = new BallotBuilder().title(someBallotTitle).build();
    someBallot.getChoices().iterator().next().voteFor(SOME_USER1);
    someBallot.getChoices().iterator().next().voteFor(SOME_USER2);
    survey.addBallot(someBallot);

    assertThat(survey.getBallot(someBallotTitle).getAllVoters().size(), is(2));

    final Page somePage = new Page();

    when(mockContentPropertyManager.getTextProperty(somePage, "survey." + someBallotTitle + ".commenters")).thenReturn("|" + SOME_USER1.getName() + "|" + SOME_USER2.getName() + "|");
    when(mockContentPropertyManager.getTextProperty(somePage, "survey." + someBallotTitle + ".comment." + SOME_USER1.getName())).thenReturn("some comment 123");
    when(mockContentPropertyManager.getTextProperty(somePage, "survey." + someBallotTitle + ".comment." + SOME_USER2.getName())).thenReturn("some comment 456");
    when(mockPermissionEvaluator.getUserByName(SOME_USER1.getName())).thenReturn(SOME_USER1);
    when(mockPermissionEvaluator.getUserByName(SOME_USER2.getName())).thenReturn(SOME_USER2);

    classUnderTest.resetVotes(survey, somePage);

    for (Choice choice : someBallot.getChoices()) {
      verify(mockContentPropertyManager).setTextProperty(somePage, VoteMacro.VOTE_STORAGE_PREFIX + someBallotTitle + "." + choice.getDescription(), null);
    }
    verify(mockContentPropertyManager).setTextProperty(somePage, "survey." + someBallotTitle + ".comment." + SOME_USER1.getName(), null);
    verify(mockContentPropertyManager).setTextProperty(somePage, "survey." + someBallotTitle + ".commenters", SOME_USER2.getName() + "|");
    verify(mockContentPropertyManager).setTextProperty(somePage, "survey." + someBallotTitle + ".comment." + SOME_USER2.getName(), null);
    verify(mockContentPropertyManager).setTextProperty(somePage, "survey." + someBallotTitle + ".commenters", "|" + SOME_USER1.getName());
  }

  @Test
  public void test_storeComment_success() {
    final String someBallotTitle = "someBallotName";
    final String someComment = "someComment";

    when(mockContentPropertyManager.getStringProperty(any(Page.class), anyString())).thenReturn("");

    classUnderTest.storeComment(someBallotTitle, someComment, SOME_USER1, new Page());

    verify(mockContentPropertyManager).setTextProperty(any(Page.class), eq("survey." + someBallotTitle + ".comment." + SOME_USER1.getName()), eq(someComment));
  }

  @Test
  public void test_storeComment_stringExists_addComment_success() {
    final String someBallotTitle = "someBallotName";
    final String someComment = "someComment";

    when(mockContentPropertyManager.getStringProperty(any(Page.class), anyString())).thenReturn("|" + SOME_USER2.getName() + "|");

    classUnderTest.storeComment(someBallotTitle, someComment, SOME_USER1, new Page());

    verify(mockContentPropertyManager).setTextProperty(any(Page.class), eq("survey." + someBallotTitle + ".comment." + SOME_USER1.getName()), eq(someComment));
  }

  @Test
  public void test_storeComment_stringExists_updateComment_success() {
    final String someBallotTitle = "someBallotName";
    final String someComment = "someComment";

    ActionContext.getContext().put("request", new HashMap<String, String>());

    when(mockContentPropertyManager.getStringProperty(any(Page.class), anyString())).thenReturn("|" + SOME_USER1.getName() + "||" + SOME_USER2.getName() + "|");

    classUnderTest.storeComment(someBallotTitle, someComment, SOME_USER1, new Page());

    verify(mockContentPropertyManager).setTextProperty(any(Page.class), eq("survey." + someBallotTitle + ".comment." + SOME_USER1.getName()), eq(someComment));
  }

  @Test
  public void test_storeComment_stringExists_removeComment_success() {
    final String someBallotTitle = "someBallotName";
    final String someComment = "";

    when(mockContentPropertyManager.getStringProperty(any(Page.class), anyString())).thenReturn("|" + SOME_USER1.getName() + "||" + SOME_USER2.getName() + "|");

    classUnderTest.storeComment(someBallotTitle, someComment, SOME_USER1, new Page());

    verify(mockContentPropertyManager).setTextProperty(any(Page.class), eq("survey." + someBallotTitle + ".commenters"), eq("|" + SOME_USER2.getName() + "|"));
    verify(mockContentPropertyManager).setTextProperty(any(Page.class), eq("survey." + someBallotTitle + ".comment." + SOME_USER1.getName()), isNull(String.class));
  }

  @Test
  public void test_canCreatePage_success() {
    ContentEntityObject contentEntityObject = new Page();

    when(mockPermissionEvaluator.canCreatePage(contentEntityObject)).thenReturn(true);

    boolean result = classUnderTest.canCreatePage(contentEntityObject);

    assertThat(result, is(true));
  }

  @Test
  public void test_canResetSurvey_success() {
    Survey survey = new Survey(SurveyUtilsTest.createDefaultSurveyConfig(new HashMap<String, String>()));

    when(mockPermissionEvaluator.getRemoteUser()).thenReturn(SOME_USER1);
    when(mockPermissionEvaluator.isPermissionListEmptyOrContainsGivenUser(any(List.class), eq(SOME_USER1))).thenReturn(true);

    boolean result = classUnderTest.canResetSurvey(survey);

    assertThat(result, is(true));
  }

  @Test
  public void test_canAttachFile_success() {
    ContentEntityObject contentEntityObject = new Page();

    when(mockPermissionEvaluator.canAttachFile(contentEntityObject)).thenReturn(true);

    boolean result = classUnderTest.canAttachFile(contentEntityObject);
    assertThat(result, is(true));
  }

  @Test
  public void test_migrateOldDefaultVotesIfPresent_success() {
    Page contentObject = new Page();

    String oldChoiceName = SurveyUtils.DEFAULT_OLD_CHOICE_NAMES.get(0);
    String newChoiceName = SurveyUtils.DEFAULT_CHOICE_NAMES.get(0);
    String someBallotTitle = SOME_BALLOT_TITLE;

    String oldKey = VoteMacro.VOTE_STORAGE_PREFIX + someBallotTitle + "." + oldChoiceName;
    String newKey = VoteMacro.VOTE_STORAGE_PREFIX + someBallotTitle + "." + newChoiceName;

    when(mockContentPropertyManager.getTextProperty(contentObject, oldKey)).thenReturn("user1,user2");

    classUnderTest.migrateOldDefaultVotesIfPresent(contentObject, someBallotTitle, newChoiceName);

    verify(mockContentPropertyManager).setTextProperty(contentObject, newKey, "user1,user2");
    verify(mockContentPropertyManager).setTextProperty(contentObject, oldKey, null);
  }
}
