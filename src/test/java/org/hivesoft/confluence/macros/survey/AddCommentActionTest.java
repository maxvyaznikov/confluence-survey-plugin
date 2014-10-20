package org.hivesoft.confluence.macros.survey;

import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.user.impl.DefaultUser;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionContext;
import org.hivesoft.confluence.macros.utils.SurveyManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class AddCommentActionTest {
  private final static DefaultUser SOME_USER1 = new DefaultUser("someUser1", "someUser1 FullName", "some1@testmail.de");
  private final static DefaultUser SOME_USER2 = new DefaultUser("someUser2", "someUser2 FullName", "some2@testmail.de");

  private SurveyManager mockSurveyManager = mock(SurveyManager.class);

  private AddCommentAction classUnderTest;

  @Before
  public void setup() {
    classUnderTest = new AddCommentAction();
    classUnderTest.setSurveyManager(mockSurveyManager);
    AuthenticatedUserThreadLocal.setUser(SOME_USER1);
  }

  @After
  public void tearDown() {
    AuthenticatedUserThreadLocal.setUser(null);

  }

  @Test
  public void test_execute_addComment_success() {
    final String someBallotTitle = "someBallotName";
    final String someComment = "someComment";

    classUnderTest.setBallotTitle(someBallotTitle);
    classUnderTest.setComment(someComment);
    classUnderTest.setBallotAnchor("someBallotAnchor");

    ActionContext.getContext().put("request", new HashMap<String, String>());

    final String returnValue = classUnderTest.execute();

    assertEquals(Action.SUCCESS, returnValue);

    verify(mockSurveyManager).storeComment(eq(someBallotTitle), eq(someComment), any(Page.class));
  }

  @Test
  public void test_execute_noBallot_success() {
    classUnderTest.setBallotTitle(null);

    final String returnValue = classUnderTest.execute();

    assertEquals(Action.ERROR, returnValue);
  }

}
