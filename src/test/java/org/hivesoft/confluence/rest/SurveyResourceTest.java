package org.hivesoft.confluence.rest;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.XhtmlException;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.xhtml.api.MacroDefinitionHandler;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurveyResourceTest {

  private final static long SOME_PAGE_ID = 123l;
  private final static String SOME_SURVEY_TITLE = "someSurveyTitle";

  TransactionTemplate mockTransactionTemplate = mock(TransactionTemplate.class);
  PageManager mockPageManager = mock(PageManager.class);
  ContentPropertyManager mockContentPropertyManager = mock(ContentPropertyManager.class);
  XhtmlContent mockXhtmlContent = mock(XhtmlContent.class);
  I18nResolver mockI18nResolver = mock(I18nResolver.class);


  SurveyResource classUnderTest;

  @Before
  public void setup() {
    classUnderTest = new SurveyResource(mockTransactionTemplate, mockPageManager, mockContentPropertyManager, mockXhtmlContent, mockI18nResolver);
  }

  @Test
  public void test_getCSVExportForSurvey_expectPageNotFound_failure() throws UnsupportedEncodingException {
    when(mockPageManager.getPage(SOME_PAGE_ID)).thenReturn(null);

    final Response response = classUnderTest.getCSVExportForSurvey(SOME_PAGE_ID, SOME_SURVEY_TITLE);

    assertThat(Response.Status.NOT_FOUND.getStatusCode(), is(response.getStatus()));
  }

  @Test
  public void test_getCSVExportForSurvey_expectSurveyIsNotFound_failure() throws UnsupportedEncodingException, XhtmlException {
    Page somePage = new Page();
    somePage.setId(SOME_PAGE_ID);
    //research how to test handleMacroDefinitions
    //somePage.setBodyAsString("<ac:structured-macro ac:name=\"survey\"><ac:parameter ac:name=\"title\">" + SOME_SURVEY_TITLE + "</ac:parameter><ac:parameter ac:name=\"changeableVotes\">true</ac:parameter><ac:plain-text-body><![CDATA[Should this be exported?\n" +
    //        "How do you like the modern iconSet?]]></ac:plain-text-body></ac:structured-macro>");
    when(mockPageManager.getPage(SOME_PAGE_ID)).thenReturn(somePage);
    //when(mockXhtmlContent.handleMacroDefinitions(anyString(),any(ConversionContext.class), any(MacroDefinitionHandler.class)).

    final Response response = classUnderTest.getCSVExportForSurvey(SOME_PAGE_ID, SOME_SURVEY_TITLE);

    assertThat(Response.Status.BAD_REQUEST.getStatusCode(), is(response.getStatus()));
  }
}
