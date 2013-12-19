package org.hivesoft.confluence.rest;

import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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

  @Test
  public void test_getCSVExportForSurvey_expectPageNotFound_failure() throws UnsupportedEncodingException {
    when(mockPageManager.getPage(SOME_PAGE_ID)).thenReturn(null);
    classUnderTest = new SurveyResource(mockTransactionTemplate, mockPageManager, mockContentPropertyManager, mockXhtmlContent, mockI18nResolver);

    final Response response = classUnderTest.getCSVExportForSurvey(SOME_PAGE_ID, SOME_SURVEY_TITLE);

    assertThat(Response.Status.NOT_FOUND.getStatusCode(), is(response.getStatus()));
  }

}
