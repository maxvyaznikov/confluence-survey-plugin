package org.hivesoft.confluence.macros.utils;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.Comment;
import com.atlassian.confluence.pages.Page;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.user.User;
import com.atlassian.user.impl.DefaultUser;
import org.hivesoft.confluence.macros.ConfluenceTestBase;
import org.hivesoft.confluence.macros.enums.VoteAction;
import org.hivesoft.confluence.macros.survey.model.Survey;
import org.hivesoft.confluence.macros.utils.wrapper.SurveyUser;
import org.hivesoft.confluence.macros.vote.VoteConfig;
import org.hivesoft.confluence.macros.vote.VoteMacro;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.macros.vote.model.Choice;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
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
  ContentPropertyManager mockContentPropertyManager = mock(ContentPropertyManager.class);
  PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);

  SurveyManager classUnderTest;

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
    return SurveyUtilsTest.createDefaultParametersWithTitle("someTitle");
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

    when(mockContentPropertyManager.getTextProperty(somePage, VoteMacro.VOTE_PREFIX + someBallotTitle1 + ".5-Outstanding")).thenReturn(SOME_USER1.getName());
    when(mockPermissionEvaluator.getUserByName(SOME_USER1.getName())).thenReturn(SOME_USER1);

    final Survey returnedSurvey = classUnderTest.reconstructSurveyFromPlainTextMacroBody(someBallotTitle1 + "\r\n" + someBallotTitle2, somePage, parametersWithTitle());

    assertThat(returnedSurvey.getBallot(someBallotTitle1).getTitle(), is(someBallotTitle1));
    assertThat(returnedSurvey.getBallot(someBallotTitle2).getTitle(), is(someBallotTitle2));
    assertThat(returnedSurvey.getBallot(someBallotTitle1).getChoice("5-Outstanding").getHasVotedFor(SOME_USER1), is(true));
  }

  @Test
  public void test_recordVote_noUser_success() {
    Ballot ballot = SurveyUtilsTest.createDefaultBallot(SurveyUtilsTest.SOME_BALLOT_TITLE);
    when(mockPermissionEvaluator.getRemoteUser()).thenReturn(new SurveyUser("someUser"));

    classUnderTest.recordVote(ballot, new Page(), "someChoiceDoesn'tMatter", VoteAction.VOTE);

    verify(mockContentPropertyManager, times(0)).setTextProperty(any(ContentEntityObject.class), anyString(), anyString());
  }

  @Test
  public void test_recordVote_freshVote_success() {
    Choice choiceToVoteOn = SurveyUtilsTest.createdDefaultChoice();
    Ballot ballot = SurveyUtilsTest.createDefaultBallotWithChoices(SurveyUtilsTest.SOME_BALLOT_TITLE, Arrays.asList(choiceToVoteOn));

    when(mockPermissionEvaluator.canVote(any(User.class), any(Ballot.class))).thenReturn(true);

    classUnderTest.recordVote(ballot, new Page(), choiceToVoteOn.getDescription(), VoteAction.VOTE);

    verify(mockContentPropertyManager, times(1)).setTextProperty(any(ContentEntityObject.class), anyString(), anyString());
  }

  @Test
  public void test_recordVote_alreadyVotedOnDifferentChangeAbleVotesTrue_success() {
    Choice choiceAlreadyVotedOn = new Choice("already Voted on");
    Choice choiceToVoteOn = new Choice(SurveyUtilsTest.SOME_CHOICE_DESCRIPTION);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, SurveyUtilsTest.SOME_BALLOT_TITLE);
    parameters.put(VoteConfig.KEY_CHANGEABLE_VOTES, "true");

    Ballot ballot = SurveyUtilsTest.createBallotWithParametersAndChoices(parameters, Arrays.asList(choiceAlreadyVotedOn, choiceToVoteOn));

    choiceAlreadyVotedOn.voteFor(SOME_USER1);

    when(mockPermissionEvaluator.canVote(any(User.class), any(Ballot.class))).thenReturn(true);

    classUnderTest.recordVote(ballot, new Page(), choiceToVoteOn.getDescription(), VoteAction.VOTE);

    verify(mockContentPropertyManager, times(2)).setTextProperty(any(ContentEntityObject.class), anyString(), anyString());
  }

  @Test
  public void test_recordVote_alreadyVotedOnUnvoteChangeAbleVotesTrue_success() {
    Choice choiceAlreadyVotedOn = new Choice("already Voted on");

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, SurveyUtilsTest.SOME_BALLOT_TITLE);
    parameters.put(VoteConfig.KEY_CHANGEABLE_VOTES, "true");

    Ballot ballot = SurveyUtilsTest.createBallotWithParametersAndChoices(parameters, Arrays.asList(choiceAlreadyVotedOn));

    choiceAlreadyVotedOn.voteFor(SOME_USER1);

    when(mockPermissionEvaluator.canVote(any(User.class), any(Ballot.class))).thenReturn(true);

    classUnderTest.recordVote(ballot, new Page(), choiceAlreadyVotedOn.getDescription(), VoteAction.UNVOTE);

    verify(mockContentPropertyManager, times(1)).setTextProperty(any(ContentEntityObject.class), anyString(), anyString());
  }

  @Test
  public void test_recordVote_alreadyVotedOnDifferentChangeAbleVotesFalse_success() {
    Ballot ballot = SurveyUtilsTest.createDefaultBallot(SurveyUtilsTest.SOME_BALLOT_TITLE);

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
}
