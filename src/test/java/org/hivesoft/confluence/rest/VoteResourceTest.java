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
import org.hivesoft.confluence.macros.utils.SurveyManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.Response;
import javax.xml.stream.XMLOutputFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VoteResourceTest {

  public static final long SOME_PAGE_ID = 123L;
  public static final String SOME_SURVEY_TITLE = "someSurveyTitle";
  public static final String SOME_BALLOT_TITLE = "someBallotTitle";

  VoteResource classUnderTest;

  private final TransactionTemplate mockTransactionTemplate = mock(TransactionTemplate.class);
  private final PageManager mockPageManager = mock(PageManager.class);
  EventPublisher mockEventPublisher = mock(EventPublisher.class);
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

  @Ignore
  @Test
  public void test_castVote() throws Exception {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    somePage.setBodyAsString("<ac:macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE + "</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
            "How do you like the modern iconSet?]]></ac:plain-text-body></ac:macro>");
    when(mockPageManager.getById(SOME_PAGE_ID)).thenReturn(somePage);

    final Response response = classUnderTest.castVote(SOME_PAGE_ID, "How do you like the modern iconSet?", "someChoiceName", VoteAction.VOTE.name());

    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
  }
}
