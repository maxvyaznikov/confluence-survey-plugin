package org.hivesoft.confluence.rest;

import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import org.hivesoft.confluence.macros.utils.SurveyManager;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class VoteResourceTest {

  VoteResource classUnderTest;

  @Test
  public void test_castVote() throws Exception {
    classUnderTest = new VoteResource(mock(TransactionTemplate.class), mock(PageManager.class), mock(XhtmlContent.class), mock(I18nResolver.class), mock(SurveyManager.class));

    final Response response = classUnderTest.castVote(123L, "someTitle", "someChoiceName");

    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
  }
}
