package org.hivesoft.confluence.macros.survey;

import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.user.impl.DefaultUser;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionContext;
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

    ContentPropertyManager mockContentPropertyManager = mock(ContentPropertyManager.class);

    AddCommentAction classUnderTest;

    @Before
    public void setup() {
        classUnderTest = new AddCommentAction();
        classUnderTest.setContentPropertyManager(mockContentPropertyManager);
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

        when(mockContentPropertyManager.getStringProperty(any(Page.class), anyString())).thenReturn("|" + SOME_USER2.getName() + "|");

        final String returnValue = classUnderTest.execute();

        assertEquals(Action.SUCCESS, returnValue);

        verify(mockContentPropertyManager).setTextProperty(any(Page.class), eq("survey." + someBallotTitle + ".comment." + SOME_USER1.getName()), eq(someComment));
    }

    @Test
    public void test_execute_updateComment_success() {
        final String someBallotTitle = "someBallotName";
        final String someComment = "someComment";
        classUnderTest.setBallotTitle(someBallotTitle);
        classUnderTest.setComment(someComment);
        classUnderTest.setBallotAnchor("someBallotAnchor");

        ActionContext.getContext().put("request", new HashMap<String, String>());

        when(mockContentPropertyManager.getStringProperty(any(Page.class), anyString())).thenReturn("|" + SOME_USER1.getName() + "||" + SOME_USER2.getName() + "|");

        final String returnValue = classUnderTest.execute();

        assertEquals(Action.SUCCESS, returnValue);

        verify(mockContentPropertyManager).setTextProperty(any(Page.class), eq("survey." + someBallotTitle + ".comment." + SOME_USER1.getName()), eq(someComment));
    }

    @Test
    public void test_execute_removeComment_success() {
        final String someBallotTitle = "someBallotName";
        final String someComment = "";
        classUnderTest.setBallotTitle(someBallotTitle);
        classUnderTest.setComment(someComment);
        classUnderTest.setBallotAnchor("someBallotAnchor");

        ActionContext.getContext().put("request", new HashMap<String, String>());

        when(mockContentPropertyManager.getStringProperty(any(Page.class), anyString())).thenReturn("|" + SOME_USER1.getName() + "||" + SOME_USER2.getName() + "|");

        final String returnValue = classUnderTest.execute();

        assertEquals(Action.SUCCESS, returnValue);

        verify(mockContentPropertyManager).setTextProperty(any(Page.class), eq("survey." + someBallotTitle + ".commenters"), eq("|" + SOME_USER2.getName() + "|"));
        verify(mockContentPropertyManager).setTextProperty(any(Page.class), eq("survey." + someBallotTitle + ".comment." + SOME_USER1.getName()), isNull(String.class));
    }

    @Test
    public void test_execute_noBallot_success() {
        classUnderTest.setBallotTitle(null);

        final String returnValue = classUnderTest.execute();

        assertEquals(Action.ERROR, returnValue);
    }

}
