package org.hivesoft.confluence.rest;

import com.atlassian.confluence.content.render.xhtml.*;
import com.atlassian.confluence.content.render.xhtml.storage.DefaultContentTransformerFactory;
import com.atlassian.confluence.content.render.xhtml.storage.macro.AlwaysTransformMacroBody;
import com.atlassian.confluence.content.render.xhtml.storage.macro.StorageMacroMarshaller;
import com.atlassian.confluence.content.render.xhtml.storage.macro.StorageMacroUnmarshaller;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.xhtml.api.MacroDefinition;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.user.User;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.macros.survey.model.Survey;
import org.hivesoft.confluence.macros.utils.PermissionEvaluator;
import org.hivesoft.confluence.macros.utils.SurveyManager;
import org.hivesoft.confluence.macros.utils.SurveyUtils;
import org.hivesoft.confluence.macros.utils.SurveyUtilsTest;
import org.hivesoft.confluence.macros.utils.wrapper.SurveyUser;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.macros.vote.model.Comment;
import org.hivesoft.confluence.rest.representations.CSVExportRepresentation;
import org.hivesoft.confluence.rest.representations.LockRepresentation;
import org.hivesoft.confluence.rest.representations.ResetRepresentation;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import javax.xml.stream.XMLOutputFactory;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurveyResourceTest {
  private final static long SOME_PAGE_ID = 123l;
  private final static String SOME_SURVEY_TITLE = "someSurveyTitle";

  TransactionTemplate mockTransactionTemplate = mock(TransactionTemplate.class);
  PageManager mockPageManager = mock(PageManager.class);
  SurveyManager mockSurveyManager = mock(SurveyManager.class);
  EventPublisher mockEventPublisher = mock(EventPublisher.class);

  I18nResolver mockI18nResolver = mock(I18nResolver.class);

  SurveyResource classUnderTest;

  @Before
  public void setup() throws Exception {
    XMLOutputFactory xmlOutputFactory = (XMLOutputFactory) new XmlOutputFactoryFactoryBean(true).getObject();

    final Unmarshaller<MacroDefinition> macroDefinitionUnmarshaller = new StorageMacroUnmarshaller(new DefaultXmlEventReaderFactory(), xmlOutputFactory, new AlwaysTransformMacroBody());
    final DefaultXmlEventReaderFactory xmlEventReaderFactory = new DefaultXmlEventReaderFactory();
    final Marshaller<MacroDefinition> macroDefinitionMarshaller = new StorageMacroMarshaller(xmlOutputFactory);

    final DefaultContentTransformerFactory contentTransformerFactory = new DefaultContentTransformerFactory(macroDefinitionUnmarshaller, macroDefinitionMarshaller, xmlEventReaderFactory, xmlOutputFactory, mockEventPublisher);
    final XhtmlContent xhtmlContent = new DefaultXhtmlContent(null, null, null, null, null, null, null, null, null, null, contentTransformerFactory, null);

    classUnderTest = new SurveyResource(mockTransactionTemplate, mockPageManager, xhtmlContent, mockI18nResolver, mockSurveyManager);
  }

  @Test
  public void test_getCSVExportForSurvey_expectPageNotFound_failure() throws UnsupportedEncodingException {
    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(null);

    final Response response = classUnderTest.getCSVExportForSurvey(SOME_PAGE_ID, new CSVExportRepresentation(SOME_SURVEY_TITLE, null));

    assertThat(Response.Status.NOT_FOUND.getStatusCode(), is(response.getStatus()));
  }

  @Test
  public void test_getCSVExportForSurvey_expectSurveyNotFound_failure() throws UnsupportedEncodingException, XhtmlException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("123");
    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);

    PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);
    when(mockSurveyManager.getPermissionEvaluator()).thenReturn(mockPermissionEvaluator);
    when(mockPermissionEvaluator.canAttachFile(somePage)).thenReturn(true);

    final Response response = classUnderTest.getCSVExportForSurvey(SOME_PAGE_ID, new CSVExportRepresentation(SOME_SURVEY_TITLE, null));

    assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void test_getCSVExportForSurvey_badXhtmlContent_failure() throws UnsupportedEncodingException, XhtmlException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<thisIsNoValidTag>");
    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);

    PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);
    when(mockSurveyManager.getPermissionEvaluator()).thenReturn(mockPermissionEvaluator);
    when(mockPermissionEvaluator.canAttachFile(somePage)).thenReturn(true);

    final Response response = classUnderTest.getCSVExportForSurvey(SOME_PAGE_ID, new CSVExportRepresentation(SOME_SURVEY_TITLE, null));

    assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void test_getCSVExportForSurvey_expectUserDoesNotHaveAddAttachmentPermission_failure() throws UnsupportedEncodingException, XhtmlException {
    Page somePage = new Page();
    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);

    PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);
    when(mockSurveyManager.getPermissionEvaluator()).thenReturn(mockPermissionEvaluator);
    when(mockPermissionEvaluator.canAttachFile(somePage)).thenReturn(false);

    final Response response = classUnderTest.getCSVExportForSurvey(SOME_PAGE_ID, new CSVExportRepresentation(SOME_SURVEY_TITLE, null));

    assertThat(response.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
  }

  @Test
  public void test_getCSVExportForSurvey_success() throws UnsupportedEncodingException, XhtmlException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE + "</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
            "How do you like the modern iconSet?]]></ac:plain-text-body></ac:macro>");
    Survey someSurvey = new Survey(SurveyUtilsTest.createDefaultSurveyConfig(new HashMap<String, String>()));
    final Ballot someBallot = new Ballot("Should this be exported?", "", someSurvey.getConfig(), SurveyUtils.getDefaultChoices(), new ArrayList<Comment>());
    someSurvey.addBallot(someBallot);
    someSurvey.addBallot(new Ballot("How do you like the modern iconSet?", "", someSurvey.getConfig(), SurveyUtils.getDefaultChoices(), new ArrayList<Comment>()));

    PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);
    Attachment mockAttachment = mock(Attachment.class);

    when(mockSurveyManager.getPermissionEvaluator()).thenReturn(mockPermissionEvaluator);
    when(mockPermissionEvaluator.canAttachFile(somePage)).thenReturn(true);

    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);
    when(mockTransactionTemplate.execute(any(TransactionCallback.class))).thenReturn(mockAttachment);

    when(mockSurveyManager.reconstructSurveyFromPlainTextMacroBody(anyString(), eq(somePage), any(Map.class))).thenReturn(someSurvey);

    when(mockAttachment.getContent()).thenReturn(somePage);
    when(mockAttachment.getDownloadPath()).thenReturn("/someUri");

    final Response response = classUnderTest.getCSVExportForSurvey(SOME_PAGE_ID, new CSVExportRepresentation(SOME_SURVEY_TITLE, null));

    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    assertThat((CSVExportRepresentation) response.getEntity(), is(equalTo(new CSVExportRepresentation(SOME_SURVEY_TITLE, "/someUri"))));
  }

  @Test
  public void test_setLocked_expectPageNotFound_failure() throws UnsupportedEncodingException {
    when(mockPageManager.getPage(SOME_PAGE_ID)).thenReturn(null);

    final Response response = classUnderTest.setLocked(SOME_PAGE_ID, new LockRepresentation(SOME_SURVEY_TITLE, false));

    assertThat(Response.Status.NOT_FOUND.getStatusCode(), is(response.getStatus()));
  }

  @Test
  public void test_setLocked_badXhtmlContent_failure() throws UnsupportedEncodingException, XhtmlException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<badTag111><ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE + "</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
            "How do you like the modern iconSet?]]></ac:plain-text-body></ac:macro>");
    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);

    final Response response = classUnderTest.setLocked(SOME_PAGE_ID, new LockRepresentation(SOME_SURVEY_TITLE, false));

    assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void test_setLocked_surveyNotFound_expectResultingRepresentationStillFalse_success() throws UnsupportedEncodingException, XhtmlException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">notThisSurvey</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
            "How do you like the modern iconSet?]]></ac:plain-text-body></ac:macro>");
    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);

    final Response response = classUnderTest.setLocked(SOME_PAGE_ID, new LockRepresentation(SOME_SURVEY_TITLE, false));

    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    assertThat((LockRepresentation) response.getEntity(), is(equalTo(new LockRepresentation(SOME_SURVEY_TITLE, false))));
  }

  @Test
  public void test_setLockedToTrue_wasNotLocked_success() throws UnsupportedEncodingException, XhtmlException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE + "</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
            "How do you like the modern iconSet?]]></ac:plain-text-body></ac:macro>");
    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);

    final Response response = classUnderTest.setLocked(SOME_PAGE_ID, new LockRepresentation(SOME_SURVEY_TITLE, false));

    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    assertThat((LockRepresentation) response.getEntity(), is(equalTo(new LockRepresentation(SOME_SURVEY_TITLE, true))));
  }

  @Test
  public void test_setLockedToFalse_wasLocked_success() throws UnsupportedEncodingException, XhtmlException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE + "</ac:parameter><ac:parameter ac:name=\"locked\">true</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
            "How do you like the modern iconSet?]]></ac:plain-text-body></ac:macro>");
    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);

    final Response response = classUnderTest.setLocked(SOME_PAGE_ID, new LockRepresentation(SOME_SURVEY_TITLE, false));

    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    assertThat((LockRepresentation) response.getEntity(), is(equalTo(new LockRepresentation(SOME_SURVEY_TITLE, false))));
  }

  @Test
  public void test_resetVotes_expectPageNotFound_failure() throws UnsupportedEncodingException {
    when(mockPageManager.getPage(SOME_PAGE_ID)).thenReturn(null);

    final Response response = classUnderTest.resetVotes(SOME_PAGE_ID, new ResetRepresentation(SOME_SURVEY_TITLE, true));

    assertThat(Response.Status.NOT_FOUND.getStatusCode(), is(response.getStatus()));
  }

  @Test
  public void test_resetVotes_surveyNotFound_failure() throws UnsupportedEncodingException, XhtmlException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">notThisSurvey</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
            "How do you like the modern iconSet?]]></ac:plain-text-body></ac:macro>");
    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);

    final Response response = classUnderTest.resetVotes(SOME_PAGE_ID, new ResetRepresentation(SOME_SURVEY_TITLE, true));

    assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void test_resetVotes_badXhtmlContent_failure() throws UnsupportedEncodingException, XhtmlException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<badContent><brokenTag2><ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">notThisSurvey</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
            "How do you like the modern iconSet?]]></ac:plain-text-body></ac:macro>");
    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);

    final Response response = classUnderTest.resetVotes(SOME_PAGE_ID, new ResetRepresentation(SOME_SURVEY_TITLE, true));

    assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void test_resetVotes_managersListEmpty_success() throws UnsupportedEncodingException, XhtmlException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE + "</ac:parameter><ac:parameter ac:name=\"locked\">true</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
            "How do you like the modern iconSet?]]></ac:plain-text-body></ac:macro>");
    Survey someSurvey = new Survey(SurveyUtilsTest.createDefaultSurveyConfig(new HashMap<String, String>()));

    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);
    when(mockSurveyManager.reconstructSurveyFromPlainTextMacroBody(anyString(), eq(somePage), any(Map.class))).thenReturn(someSurvey);
    when(mockSurveyManager.canResetSurvey(someSurvey)).thenReturn(true);

    final Response response = classUnderTest.resetVotes(SOME_PAGE_ID, new ResetRepresentation(SOME_SURVEY_TITLE, true));

    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
  }

  @Test
  public void test_resetVotes_userNotInManagersList_success() throws UnsupportedEncodingException, XhtmlException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE + "</ac:parameter><ac:parameter ac:name=\"locked\">true</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
            "How do you like the modern iconSet?]]></ac:plain-text-body></ac:macro>");


    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(SurveyConfig.KEY_MANAGERS, "notThisUser");
    Survey someSurvey = new Survey(SurveyUtilsTest.createDefaultSurveyConfig(parameters));

    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);
    when(mockSurveyManager.reconstructSurveyFromPlainTextMacroBody(anyString(), eq(somePage), any(Map.class))).thenReturn(someSurvey);
    when(mockSurveyManager.canResetSurvey(someSurvey)).thenReturn(false);

    final Response response = classUnderTest.resetVotes(SOME_PAGE_ID, new ResetRepresentation(SOME_SURVEY_TITLE, true));

    assertThat(response.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
  }

  @Test
  public void test_resetVotes_surveyLocked_success() throws UnsupportedEncodingException, XhtmlException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE + "</ac:parameter><ac:parameter ac:name=\"locked\">true</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
            "How do you like the modern iconSet?]]></ac:plain-text-body></ac:macro>");


    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(SurveyConfig.KEY_LOCKED, "true");
    Survey someSurvey = new Survey(SurveyUtilsTest.createDefaultSurveyConfig(parameters));

    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);
    when(mockSurveyManager.reconstructSurveyFromPlainTextMacroBody(anyString(), eq(somePage), any(Map.class))).thenReturn(someSurvey);
    when(mockSurveyManager.canResetSurvey(someSurvey)).thenReturn(true);

    final Response response = classUnderTest.resetVotes(SOME_PAGE_ID, new ResetRepresentation(SOME_SURVEY_TITLE, true));

    assertThat(response.getStatus(), is(Response.Status.FORBIDDEN.getStatusCode()));
  }
}
