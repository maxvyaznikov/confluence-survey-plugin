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

    private static final String SOME_BALLOT = "some Ballot";
    private static final String SOME_CHOICE = "some Choice";
    private static final String SOME_USER_NAME = "john doe";

    VoteMacro classUnderTest = new VoteMacro(mockPageManager, mockSpaceManager, mockContentPropertyManager, mockUserAccessor, mockUserManager, mockTemplateRenderer, mockXhtmlContent, mockPluginSettingsFactory);

    @Before
    public void setup() {
        when(mockUserManager.getRemoteUsername()).thenReturn(SOME_USER_NAME);

        when(mockRequest.getParameter(VoteMacro.REQUEST_PARAMETER_BALLOT)).thenReturn(SOME_BALLOT);
        when(mockRequest.getParameter(VoteMacro.REQUEST_PARAMETER_CHOICE)).thenReturn(SOME_CHOICE);
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
    public void test_canSeeResults_NoUserFound_success() {
        final Boolean canSeeResults = classUnderTest.getCanSeeResults(null, null, "", null);
        assertFalse(canSeeResults);
    }

    @Test
    public void test_canSeeResults_noRestrictionsButHasNotVotedYet_success() {
        final Boolean canSeeResults = classUnderTest.getCanSeeResults("", "", SOME_USER_NAME, createDefaultBallot());
        assertFalse(canSeeResults);
    }

    @Test
    public void test_canSeeResults_noRestrictionsAndHasVoted_success() {
        final Ballot defaultBallot = createDefaultBallot();
        defaultBallot.getChoices().iterator().next().voteFor(SOME_USER_NAME);
        final Boolean canSeeResults = classUnderTest.getCanSeeResults("", "", SOME_USER_NAME, defaultBallot);
        assertTrue(canSeeResults);
    }

    @Test
    public void test_getCanSeeVoters_success() {
        //cant see results
        assertFalse(classUnderTest.getCanSeeVoters("true", false));
        //visibleVoters Parameter null
        assertFalse(classUnderTest.getCanSeeVoters(null, true));
        //visibleVoters Parameter not "true"
        assertFalse(classUnderTest.getCanSeeVoters("something", true));
        //visibleVoters Parameter "true"
        assertTrue(classUnderTest.getCanSeeVoters("true", true));
    }

    @Test
    public void test_recordVote_noUser_success() {
        Ballot ballot = createDefaultBallot();
        when(mockUserManager.getRemoteUsername()).thenReturn("");

        classUnderTest.recordVote(ballot, mockRequest, new Page(), "");

        verify(mockContentPropertyManager, times(0)).setTextProperty(any(ContentEntityObject.class), anyString(), anyString());
    }

    @Test
    public void test_recordVote_freshVote_success() {
        Ballot ballot = createDefaultBallot();

        classUnderTest.recordVote(ballot, mockRequest, new Page(), "");

        verify(mockContentPropertyManager, times(1)).setTextProperty(any(ContentEntityObject.class), anyString(), anyString());
    }

    @Test
    public void test_recordVote_alreadyVotedOnDifferentChangeAbleVotesTrue_success() {
        Choice choice = new Choice("already Voted on");
        Ballot ballot = createDefaultBallot();
        ballot.addChoice(choice);
        ballot.setChangeableVotes(true);

        choice.voteFor(SOME_USER_NAME);

        classUnderTest.recordVote(ballot, mockRequest, new Page(), "");

        verify(mockContentPropertyManager, times(2)).setTextProperty(any(ContentEntityObject.class), anyString(), anyString());
    }

    @Test
    public void test_recordVote_alreadyVotedOnDifferentChangeAbleVotesFalse_success() {
        Choice choice = new Choice("already Voted on");
        Ballot ballot = createDefaultBallot();
        ballot.addChoice(choice);

        choice.voteFor(SOME_USER_NAME);

        classUnderTest.recordVote(ballot, mockRequest, new Page(), "");

        verify(mockContentPropertyManager, times(0)).setTextProperty(any(ContentEntityObject.class), anyString(), anyString());
    }

    //****** Helper Methods ******
    private Ballot createDefaultBallot() {
        Ballot ballot = new Ballot(SOME_BALLOT);
        Choice choice = new Choice(SOME_CHOICE);
        ballot.addChoice(choice);
        return ballot;
    }
}
