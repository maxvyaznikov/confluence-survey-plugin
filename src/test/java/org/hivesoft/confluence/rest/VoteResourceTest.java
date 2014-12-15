package org.hivesoft.confluence.rest;

import com.atlassian.confluence.content.render.xhtml.*;
import com.atlassian.confluence.content.render.xhtml.storage.DefaultContentTransformerFactory;
import com.atlassian.confluence.content.render.xhtml.storage.macro.AlwaysTransformMacroBody;
import com.atlassian.confluence.content.render.xhtml.storage.macro.StorageMacroMarshaller;
import com.atlassian.confluence.content.render.xhtml.storage.macro.StorageMacroUnmarshaller;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.xhtml.api.MacroDefinition;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.event.api.EventPublisher;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.ConfluenceTestBase;
import org.hivesoft.confluence.model.Survey;
import org.hivesoft.confluence.model.enums.VoteAction;
import org.hivesoft.confluence.model.vote.Ballot;
import org.hivesoft.confluence.model.vote.Comment;
import org.hivesoft.confluence.rest.representations.VoteRepresentation;
import org.hivesoft.confluence.utils.SurveyManager;
import org.hivesoft.confluence.utils.SurveyUtils;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import javax.xml.stream.XMLOutputFactory;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VoteResourceTest extends ConfluenceTestBase {

  private final PageManager mockPageManager = mock(PageManager.class);
  private final EventPublisher mockEventPublisher = mock(EventPublisher.class);
  private final SurveyManager mockSurveyManager = mock(SurveyManager.class);

  private VoteResource classUnderTest;

  @Before
  public void setup() throws Exception {
    XMLOutputFactory xmlOutputFactory = (XMLOutputFactory) new XmlOutputFactoryFactoryBean(true).getObject();

    final Unmarshaller<MacroDefinition> macroDefinitionUnmarshaller = new StorageMacroUnmarshaller(new DefaultXmlEventReaderFactory(), xmlOutputFactory, new AlwaysTransformMacroBody());
    final DefaultXmlEventReaderFactory xmlEventReaderFactory = new DefaultXmlEventReaderFactory();
    final Marshaller<MacroDefinition> macroDefinitionMarshaller = new StorageMacroMarshaller(xmlOutputFactory);

    final DefaultContentTransformerFactory contentTransformerFactory = new DefaultContentTransformerFactory(macroDefinitionUnmarshaller, macroDefinitionMarshaller, xmlEventReaderFactory, xmlOutputFactory, mockEventPublisher);
    final XhtmlContent xhtmlContent = new DefaultXhtmlContent(null, null, null, null, null, null, null, null, null, null, contentTransformerFactory, null);

    classUnderTest = new VoteResource(mockPageManager, xhtmlContent, mockSurveyManager);
  }

  @Test
  public void test_castVote_entityNotFound() throws UnsupportedEncodingException {
    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(null);

    VoteRepresentation voteRepresentation = new VoteRepresentation("someTitle", "someChoiceName", VoteAction.VOTE.name());

    final Response response = classUnderTest.castVote(SOME_PAGE_ID, voteRepresentation);

    assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    assertThat(response.getEntity(), notNullValue());
  }

  @Test
  public void test_castVote_badXhtmlContent() throws UnsupportedEncodingException {
    final Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<badHtmlContent>");
    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);

    VoteRepresentation voteRepresentation = new VoteRepresentation("someTitle", "someChoiceName", VoteAction.VOTE.name());

    final Response response = classUnderTest.castVote(SOME_PAGE_ID, voteRepresentation);

    assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    assertThat(response.getEntity(), notNullValue());
  }

  @Test
  public void test_castVote_foundNoBallot_success() throws UnsupportedEncodingException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE + "</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
            "How do you like the modern iconSet?]]></ac:plain-text-body></ac:macro>");

    Survey someSurvey = new Survey(createDefaultSurveyConfig(new HashMap<String, String>()));
    final Ballot someBallot = new Ballot("Should this be exported?", "", someSurvey.getConfig(), SurveyUtils.getDefaultChoices(), new ArrayList<Comment>());
    someSurvey.addBallot(someBallot);
    someSurvey.addBallot(new Ballot("How do you like the modern iconSet?", "", someSurvey.getConfig(), SurveyUtils.getDefaultChoices(), new ArrayList<Comment>()));

    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);
    when(mockSurveyManager.reconstructSurveyFromPlainTextMacroBody(anyString(), eq(somePage), any(Map.class))).thenReturn(someSurvey);

    VoteRepresentation voteRepresentation = new VoteRepresentation("This is not the ballot you are looking for!", "someChoice", VoteAction.VOTE.name());

    final Response response = classUnderTest.castVote(SOME_PAGE_ID, voteRepresentation);

    assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    assertThat(((String) response.getEntity()), containsString("problem"));
  }

  @Test
  public void test_castVote_foundTwoDuplicateBallots_success() throws UnsupportedEncodingException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);

    List<String> ballots = Arrays.asList(new String[]{"Should this be exported?", "How do you like the modern iconSet?", "How do you like the modern iconSet?"});

    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE +
            "</ac:parameter><ac:plain-text-body><![CDATA[" + StringUtils.join(ballots, '\n') + "]]>" +
            "</ac:plain-text-body></ac:macro><ac:macro ac:name=\"vote\"><ac:parameter ac:name=\"title\">" + SOME_BALLOT_TITLE + "</ac:parameter><ac:plain-text-body><![CDATA[Choice1\n" +
            "Choice2]]></ac:plain-text-body></ac:macro>");

    Survey someSurvey = new Survey(createDefaultSurveyConfig(new HashMap<String, String>()));

    for (String ballotTitle : ballots) {
      someSurvey.addBallot(new Ballot(ballotTitle, "", someSurvey.getConfig(), SurveyUtils.getDefaultChoices(), new ArrayList<Comment>()));
    }

    Ballot someBallot = new Ballot(ballots.get(1), "bla", someSurvey.getConfig(), SurveyUtils.getDefaultChoices(), new ArrayList<Comment>());

    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);
    when(mockSurveyManager.reconstructSurveyFromPlainTextMacroBody(anyString(), eq(somePage), any(Map.class))).thenReturn(someSurvey);
    when(mockSurveyManager.reconstructBallotFromPlainTextMacroBody(any(Map.class), anyString(), eq(somePage))).thenReturn(someBallot);

    VoteRepresentation voteRepresentation = new VoteRepresentation("How do you like the modern iconSet?", "someChoice", VoteAction.VOTE.name());

    final Response response = classUnderTest.castVote(SOME_PAGE_ID, voteRepresentation);

    assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    assertThat(((String) response.getEntity()), containsString("problem"));
  }

  @Test
  public void test_castVote_foundBallotWithinSurvey_success() throws UnsupportedEncodingException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE + "</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
            "How do you like the modern iconSet?]]></ac:plain-text-body></ac:macro>");
    Survey someSurvey = new Survey(createDefaultSurveyConfig(new HashMap<String, String>()));
    final Ballot someBallot = new Ballot("Should this be exported?", "", someSurvey.getConfig(), SurveyUtils.getDefaultChoices(), new ArrayList<Comment>());
    someSurvey.addBallot(someBallot);
    someSurvey.addBallot(new Ballot("How do you like the modern iconSet?", "", someSurvey.getConfig(), SurveyUtils.getDefaultChoices(), new ArrayList<Comment>()));

    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);
    when(mockSurveyManager.reconstructSurveyFromPlainTextMacroBody(anyString(), eq(somePage), any(Map.class))).thenReturn(someSurvey);
    final String choiceName = SurveyUtils.getDefaultChoices().get(0).getDescription();
    when(mockSurveyManager.recordVote(someBallot, somePage, choiceName, VoteAction.VOTE)).thenReturn(VoteAction.CHANGEVOTE);

    VoteRepresentation voteRepresentation = new VoteRepresentation("Should this be exported?", choiceName, VoteAction.VOTE.name());

    final Response response = classUnderTest.castVote(SOME_PAGE_ID, voteRepresentation);

    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    assertThat(((VoteRepresentation) response.getEntity()).getVoteAction(), is(VoteAction.CHANGEVOTE.name()));
  }

  @Test
  public void test_castVote_foundBallotDirectlyWithinVote_success() throws UnsupportedEncodingException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<ac:macro ac:name=\"vote\"><ac:parameter ac:name=\"title\">" + SOME_BALLOT_TITLE + "</ac:parameter><ac:plain-text-body><![CDATA[Choice1\n" +
            "Choice2]]></ac:plain-text-body></ac:macro>");
    final Ballot someBallot = new Ballot(SOME_BALLOT_TITLE, "", createDefaultVoteConfig(new HashMap<String, String>()), SurveyUtils.getDefaultChoices(), new ArrayList<Comment>());

    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);
    when(mockSurveyManager.reconstructBallotFromPlainTextMacroBody(any(Map.class), anyString(), eq(somePage))).thenReturn(someBallot);
    when(mockSurveyManager.recordVote(someBallot, somePage, "Choice2", VoteAction.VOTE)).thenReturn(VoteAction.VOTE);

    VoteRepresentation voteRepresentation = new VoteRepresentation(SOME_BALLOT_TITLE, "Choice2", VoteAction.VOTE.name());

    final Response response = classUnderTest.castVote(SOME_PAGE_ID, voteRepresentation);

    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    assertThat(((VoteRepresentation) response.getEntity()).getVoteAction(), is(VoteAction.VOTE.name()));
  }
}
