package org.hivesoft.confluence.macros.vote;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.hivesoft.confluence.macros.utils.SurveyUtilsTest;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.macros.vote.model.Choice;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class VoteMacroTest {

    PageManager mockPageManager = mock(PageManager.class);
    SpaceManager mockSpaceManager = mock(SpaceManager.class);
    ContentPropertyManager mockContentPropertyManager = mock(ContentPropertyManager.class);
    UserAccessor mockUserAccessor = mock(UserAccessor.class);
    UserManager mockUserManager = mock(UserManager.class);
    TemplateRenderer mockTemplateRenderer = mock(TemplateRenderer.class);
    XhtmlContent mockXhtmlContent = mock(XhtmlContent.class);
    PluginSettingsFactory mockPluginSettingsFactory = mock(PluginSettingsFactory.class);

    HttpServletRequest mockRequest = mock(HttpServletRequest.class);

    VoteMacro classUnderTest = new VoteMacro(mockPageManager, mockSpaceManager, mockContentPropertyManager, mockUserAccessor, mockUserManager, mockTemplateRenderer, mockXhtmlContent, mockPluginSettingsFactory);

    @Before
    public void setup() {
        when(mockUserManager.getRemoteUsername()).thenReturn(SurveyUtilsTest.SOME_USER_NAME);

        when(mockRequest.getParameter(VoteMacro.REQUEST_PARAMETER_BALLOT)).thenReturn(SurveyUtilsTest.SOME_BALLOT);
        when(mockRequest.getParameter(VoteMacro.REQUEST_PARAMETER_CHOICE)).thenReturn(SurveyUtilsTest.SOME_CHOICE);
    }

    @Test
    public void test_MacroProperties_success() {
        assertTrue(classUnderTest.hasBody());
        assertFalse(classUnderTest.isInline());
        assertEquals(Macro.BodyType.PLAIN_TEXT, classUnderTest.getBodyType());
        assertEquals(RenderMode.NO_RENDER, classUnderTest.getBodyRenderMode());
        assertEquals(Macro.OutputType.BLOCK, classUnderTest.getOutputType());
    }


    @Test
    public void test_recordVote_noUser_success() {
        Ballot ballot = SurveyUtilsTest.createDefaultBallot();
        when(mockUserManager.getRemoteUsername()).thenReturn("");

        classUnderTest.recordVote(ballot, mockRequest, new Page(), "");

        verify(mockContentPropertyManager, times(0)).setTextProperty(any(ContentEntityObject.class), anyString(), anyString());
    }

    @Test
    public void test_recordVote_freshVote_success() {
        Ballot ballot = SurveyUtilsTest.createDefaultBallot();

        classUnderTest.recordVote(ballot, mockRequest, new Page(), "");

        verify(mockContentPropertyManager, times(1)).setTextProperty(any(ContentEntityObject.class), anyString(), anyString());
    }

    @Test
    public void test_recordVote_alreadyVotedOnDifferentChangeAbleVotesTrue_success() {
        Choice choice = new Choice("already Voted on");
        Ballot ballot = SurveyUtilsTest.createDefaultBallot();
        ballot.addChoice(choice);
        ballot.setChangeableVotes(true);

        choice.voteFor(SurveyUtilsTest.SOME_USER_NAME);

        classUnderTest.recordVote(ballot, mockRequest, new Page(), "");

        verify(mockContentPropertyManager, times(2)).setTextProperty(any(ContentEntityObject.class), anyString(), anyString());
    }

    @Test
    public void test_recordVote_alreadyVotedOnDifferentChangeAbleVotesFalse_success() {
        Choice choice = new Choice("already Voted on");
        Ballot ballot = SurveyUtilsTest.createDefaultBallot();
        ballot.addChoice(choice);

        choice.voteFor(SurveyUtilsTest.SOME_USER_NAME);

        classUnderTest.recordVote(ballot, mockRequest, new Page(), "");

        verify(mockContentPropertyManager, times(0)).setTextProperty(any(ContentEntityObject.class), anyString(), anyString());
    }


}
