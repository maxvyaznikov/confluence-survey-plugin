package org.hivesoft.confluence.macros.survey;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.user.User;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionContext;
import org.hivesoft.confluence.macros.ConfluenceTestBase;
import org.hivesoft.confluence.utils.SurveyManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

public class AddCommentActionTest extends ConfluenceTestBase {
  private final SurveyManager mockSurveyManager = mock(SurveyManager.class);

  private AddCommentAction classUnderTest;

  @Before
  public void setup() {
    AuthenticatedUserThreadLocal.setUser(SOME_USER1);

    classUnderTest = new AddCommentAction();
    classUnderTest.setSurveyManager(mockSurveyManager);
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

    assertThat(returnValue, is(Action.SUCCESS));

    verify(mockSurveyManager).storeComment(eq(someBallotTitle), eq(someComment), any(User.class), any(Page.class));
  }

  @Test
  public void test_execute_noBallot_success() {
    classUnderTest.setBallotTitle(null);

    final String returnValue = classUnderTest.execute();

    assertThat(returnValue, is(Action.ERROR));
  }

}
