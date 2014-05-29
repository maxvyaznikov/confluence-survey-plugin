package org.hivesoft.confluence.rest;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import org.hivesoft.confluence.macros.enums.VoteAction;
import org.hivesoft.confluence.macros.utils.SurveyManager;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VoteResourceTest {

  VoteResource classUnderTest;

  private final TransactionTemplate mockTransactionTemplate = mock(TransactionTemplate.class);
  private final PageManager mockPageManager = mock(PageManager.class);
  private final XhtmlContent mockXhtmlContent = mock(XhtmlContent.class);
  private final I18nResolver mockI18NResolver = mock(I18nResolver.class);
  private final SurveyManager mockSurveyManager = mock(SurveyManager.class);

  @Test
  public void test_castVote_entityNotFound() throws Exception {
    when(mockPageManager.getById(123L)).thenReturn(null);

    classUnderTest = new VoteResource(mockTransactionTemplate, mockPageManager, mockXhtmlContent, mockI18NResolver, mockSurveyManager);

    final Response response = classUnderTest.castVote(123L, "someTitle", "someChoiceName", VoteAction.VOTE.name());

    assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
  }

  @Test
  public void test_castVote() throws Exception {
    when(mockPageManager.getById(123L)).thenReturn(new Page());

    classUnderTest = new VoteResource(mockTransactionTemplate, mockPageManager, mockXhtmlContent, mockI18NResolver, mockSurveyManager);

    final Response response = classUnderTest.castVote(123L, "someTitle", "someChoiceName", VoteAction.VOTE.name());

    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
  }
}
