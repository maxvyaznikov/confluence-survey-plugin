package org.hivesoft.confluence.rest;

import com.atlassian.confluence.content.render.xhtml.*;
import com.atlassian.confluence.content.render.xhtml.storage.DefaultContentTransformerFactory;
import com.atlassian.confluence.content.render.xhtml.storage.macro.AlwaysTransformMacroBody;
import com.atlassian.confluence.content.render.xhtml.storage.macro.StorageMacroMarshaller;
import com.atlassian.confluence.content.render.xhtml.storage.macro.StorageMacroUnmarshaller;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.xhtml.api.MacroDefinition;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import org.hivesoft.confluence.macros.utils.PermissionEvaluator;
import org.hivesoft.confluence.rest.representations.CSVExportRepresentation;
import org.hivesoft.confluence.rest.representations.LockRepresentation;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import javax.xml.stream.XMLOutputFactory;
import java.io.UnsupportedEncodingException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurveyResourceTest {
  private final static long SOME_PAGE_ID = 123l;
  private final static String SOME_SURVEY_TITLE = "someSurveyTitle";

  TransactionTemplate mockTransactionTemplate = mock(TransactionTemplate.class);
  PageManager mockPageManager = mock(PageManager.class);
  ContentPropertyManager mockContentPropertyManager = mock(ContentPropertyManager.class);
  EventPublisher mockEventPublisher = mock(EventPublisher.class);
  PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);

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

    classUnderTest = new SurveyResource(mockTransactionTemplate, mockPageManager, mockContentPropertyManager, xhtmlContent, mockI18nResolver, mockPermissionEvaluator);
  }

  @Test
  public void test_getCSVExportForSurvey_expectPageNotFound_failure() throws UnsupportedEncodingException {
    when(mockPageManager.getPage(SOME_PAGE_ID)).thenReturn(null);

    final Response response = classUnderTest.getCSVExportForSurvey(SOME_PAGE_ID, SOME_SURVEY_TITLE);

    assertThat(Response.Status.NOT_FOUND.getStatusCode(), is(response.getStatus()));
  }

  @Test
  public void test_getCSVExportForSurvey_expectSurveyNotFound_failure() throws UnsupportedEncodingException, XhtmlException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("123");
    when(mockPageManager.getPage(SOME_PAGE_ID)).thenReturn(somePage);

    when(mockPermissionEvaluator.canAttachFile(somePage)).thenReturn(true);

    final Response response = classUnderTest.getCSVExportForSurvey(SOME_PAGE_ID, SOME_SURVEY_TITLE);

    assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void test_getCSVExportForSurvey_expectUserDoesNotHaveAddAttachmentPermission_failure() throws UnsupportedEncodingException, XhtmlException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("123");
    when(mockPageManager.getPage(SOME_PAGE_ID)).thenReturn(somePage);

    when(mockPermissionEvaluator.canAttachFile(somePage)).thenReturn(false);

    final Response response = classUnderTest.getCSVExportForSurvey(SOME_PAGE_ID, SOME_SURVEY_TITLE);

    assertThat(response.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
  }

  @Test
  public void test_getCSVExportForSurvey_success() throws UnsupportedEncodingException, XhtmlException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE + "</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
            "How do you like the modern iconSet?]]></ac:plain-text-body></ac:macro>");

    when(mockPermissionEvaluator.canAttachFile(somePage)).thenReturn(true);

    Attachment mockAttachment = mock(Attachment.class);
    when(mockPageManager.getPage(SOME_PAGE_ID)).thenReturn(somePage);
    when(mockTransactionTemplate.execute(any(TransactionCallback.class))).thenReturn(mockAttachment);
    when(mockAttachment.getContent()).thenReturn(somePage);
    when(mockAttachment.getDownloadPath()).thenReturn("/someUri");

    final Response response = classUnderTest.getCSVExportForSurvey(SOME_PAGE_ID, SOME_SURVEY_TITLE);

    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    assertThat((CSVExportRepresentation) response.getEntity(), is(equalTo(new CSVExportRepresentation("/someUri"))));
  }

  @Test
  public void test_setLocked_expectPageNotFound_failure() throws UnsupportedEncodingException {
    when(mockPageManager.getPage(SOME_PAGE_ID)).thenReturn(null);

    final Response response = classUnderTest.setLocked(SOME_PAGE_ID, SOME_SURVEY_TITLE);

    assertThat(Response.Status.NOT_FOUND.getStatusCode(), is(response.getStatus()));
  }

  @Test
  public void test_setLockedToTrue_wasNotLocked_success() throws UnsupportedEncodingException, XhtmlException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE + "</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
            "How do you like the modern iconSet?]]></ac:plain-text-body></ac:macro>");
    when(mockPageManager.getPage(SOME_PAGE_ID)).thenReturn(somePage);

    final Response response = classUnderTest.setLocked(SOME_PAGE_ID, SOME_SURVEY_TITLE);

    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    assertThat((LockRepresentation) response.getEntity(), is(equalTo(new LockRepresentation(true))));
  }

  @Test
  public void test_setLockedToFalse_wasLocked_success() throws UnsupportedEncodingException, XhtmlException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE + "</ac:parameter><ac:parameter ac:name=\"locked\">true</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
            "How do you like the modern iconSet?]]></ac:plain-text-body></ac:macro>");
    when(mockPageManager.getPage(SOME_PAGE_ID)).thenReturn(somePage);

    final Response response = classUnderTest.setLocked(SOME_PAGE_ID, SOME_SURVEY_TITLE);

    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    assertThat((LockRepresentation) response.getEntity(), is(equalTo(new LockRepresentation(false))));
  }
}
