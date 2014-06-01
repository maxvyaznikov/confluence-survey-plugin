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
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import org.hivesoft.confluence.macros.enums.VoteAction;
import org.hivesoft.confluence.macros.survey.model.Survey;
import org.hivesoft.confluence.macros.utils.SurveyManager;
import org.hivesoft.confluence.macros.utils.SurveyUtils;
import org.hivesoft.confluence.macros.utils.SurveyUtilsTest;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.macros.vote.model.Comment;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import javax.xml.stream.XMLOutputFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VoteResourceTest {

  public static final long SOME_PAGE_ID = 123L;
  public static final String SOME_SURVEY_TITLE = "someSurveyTitle";
  public static final String SOME_BALLOT_TITLE = "someBallotTitle";

  VoteResource classUnderTest;

  private final TransactionTemplate mockTransactionTemplate = mock(TransactionTemplate.class);
  private final PageManager mockPageManager = mock(PageManager.class);
  private final EventPublisher mockEventPublisher = mock(EventPublisher.class);
  private final I18nResolver mockI18nResolver = mock(I18nResolver.class);
  private final SurveyManager mockSurveyManager = mock(SurveyManager.class);

  @Before
  public void setup() throws Exception {
    XMLOutputFactory xmlOutputFactory = (XMLOutputFactory) new XmlOutputFactoryFactoryBean(true).getObject();

    final Unmarshaller<MacroDefinition> macroDefinitionUnmarshaller = new StorageMacroUnmarshaller(new DefaultXmlEventReaderFactory(), xmlOutputFactory, new AlwaysTransformMacroBody());
    final DefaultXmlEventReaderFactory xmlEventReaderFactory = new DefaultXmlEventReaderFactory();
    final Marshaller<MacroDefinition> macroDefinitionMarshaller = new StorageMacroMarshaller(xmlOutputFactory);

    final DefaultContentTransformerFactory contentTransformerFactory = new DefaultContentTransformerFactory(macroDefinitionUnmarshaller, macroDefinitionMarshaller, xmlEventReaderFactory, xmlOutputFactory, mockEventPublisher);
    final XhtmlContent xhtmlContent = new DefaultXhtmlContent(null, null, null, null, null, null, null, null, null, null, contentTransformerFactory, null);

    classUnderTest = new VoteResource(mockTransactionTemplate, mockPageManager, xhtmlContent, mockI18nResolver, mockSurveyManager);
  }

  @Test
  public void test_castVote_entityNotFound() throws Exception {
    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(null);

    final Response response = classUnderTest.castVote(SOME_PAGE_ID, "someTitle", "someChoiceName", VoteAction.VOTE.name());

    assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
  }

  @Test
  public void test_castVote_badXhtmlContent() throws Exception {
    final Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<badHtmlContent>");
    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);

    final Response response = classUnderTest.castVote(SOME_PAGE_ID, "someTitle", "someChoiceName", VoteAction.VOTE.name());

    assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void test_castVote_foundBallotWithinSurvey_success() throws Exception {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE + "</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
            "How do you like the modern iconSet?]]></ac:plain-text-body></ac:macro>");
    Survey someSurvey = new Survey(SurveyUtilsTest.createDefaultSurveyConfig(new HashMap<String, String>()));
    final Ballot someBallot = new Ballot("Should this be exported?", "", someSurvey.getConfig(), SurveyUtils.getDefaultChoices(), new ArrayList<Comment>());
    someSurvey.addBallot(someBallot);
    someSurvey.addBallot(new Ballot("How do you like the modern iconSet?", "", someSurvey.getConfig(), SurveyUtils.getDefaultChoices(), new ArrayList<Comment>()));

    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);
    when(mockSurveyManager.reconstructSurveyFromPlainTextMacroBody(anyString(), eq(somePage), any(Map.class))).thenReturn(someSurvey);

    final Response response = classUnderTest.castVote(SOME_PAGE_ID, "How do you like the modern iconSet?", "someChoiceName", VoteAction.VOTE.name());

    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
  }

  @Test
  public void test_castVote_foundBallotDirectlyWithinVote_success() throws Exception {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<ac:macro ac:name=\"vote\"><ac:parameter ac:name=\"title\">" + SOME_BALLOT_TITLE + "</ac:parameter><ac:plain-text-body><![CDATA[Choice1\n" +
            "Choice2]]></ac:plain-text-body></ac:macro>");
    final Ballot someBallot = new Ballot(SOME_BALLOT_TITLE, "", SurveyUtilsTest.createDefaultVoteConfig(new HashMap<String, String>()), SurveyUtils.getDefaultChoices(), new ArrayList<Comment>());

    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);
    when(mockSurveyManager.reconstructBallotFromPlainTextMacroBody(any(Map.class), anyString(), eq(somePage))).thenReturn(someBallot);

    final Response response = classUnderTest.castVote(SOME_PAGE_ID, SOME_BALLOT_TITLE, "Choice2", VoteAction.VOTE.name());

    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
  }
}
