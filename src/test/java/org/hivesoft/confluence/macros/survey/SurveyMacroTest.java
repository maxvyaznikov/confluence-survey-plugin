package org.hivesoft.confluence.macros.survey;

import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.user.impl.DefaultUser;
import org.hivesoft.confluence.macros.survey.model.Survey;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurveyMacroTest {
    private final static DefaultUser SOME_USER1 = new DefaultUser("someUser1", "someUser1 FullName", "some1@testmail.de");

    PageManager mockPageManager = mock(PageManager.class);
    SpaceManager mockSpaceManager = mock(SpaceManager.class);
    ContentPropertyManager mockContentPropertyManager = mock(ContentPropertyManager.class);
    UserAccessor mockUserAccessor = mock(UserAccessor.class);
    UserManager mockUserManager = mock(UserManager.class);
    TemplateRenderer mockTemplateRenderer = mock(TemplateRenderer.class);
    XhtmlContent mockXhtmlContent = mock(XhtmlContent.class);
    PluginSettingsFactory mockPluginSettingsFactory = mock(PluginSettingsFactory.class);

    SurveyMacro classUnderTest = new SurveyMacro(mockPageManager, mockSpaceManager, mockContentPropertyManager, mockUserAccessor, mockUserManager, mockTemplateRenderer, mockXhtmlContent, mockPluginSettingsFactory);

    @Test
    public void test_MacroProperties_success() {

        assertTrue(classUnderTest.hasBody());
        assertFalse(classUnderTest.isInline());
        assertEquals(RenderMode.NO_RENDER, classUnderTest.getBodyRenderMode());
    }

    @Test
    public void test_createSurvey_noParameters_success() {
        final Survey returnedSurvey = classUnderTest.createSurvey("", new Page(), "");

        assertEquals(0, returnedSurvey.getBallots().size());
    }

    @Test
    public void test_createSurvey_oneParameter_success() {
        final String someBallotTitle1 = "someBallotTitle1";
        final Survey returnedSurvey = classUnderTest.createSurvey(someBallotTitle1, new Page(), null);

        assertEquals(someBallotTitle1, returnedSurvey.getBallot(someBallotTitle1).getTitle());
    }

    @Test
    public void test_createSurvey_twoParameters_success() {
        final String someBallotTitle1 = "someBallotTitle1";
        final String someBallotTitle2 = "someBallotTitle2";
        final String someBallotDescription1 = "someBallotDescription1";
        final Survey returnedSurvey = classUnderTest.createSurvey(someBallotTitle1 + " - " + someBallotDescription1 + "\r\n" + someBallotTitle2, new Page(), "");

        assertEquals(someBallotTitle1, returnedSurvey.getBallot(someBallotTitle1).getTitle());
        assertEquals(someBallotDescription1, returnedSurvey.getBallot(someBallotTitle1).getDescription());
        assertEquals(someBallotTitle2, returnedSurvey.getBallot(someBallotTitle2).getTitle());
    }

    @Test
    public void test_createSurvey_twoParametersWithCommenter_success() {
        final String someBallotTitle1 = "someBallotTitle1";
        final String someBallotTitle2 = "someBallotTitle2";

        final Page somePage = new Page();

        when(mockContentPropertyManager.getTextProperty(somePage, "survey." + someBallotTitle1 + ".commenters")).thenReturn(SOME_USER1.getName());
        when(mockContentPropertyManager.getTextProperty(somePage, "survey." + someBallotTitle1 + ".comment." + SOME_USER1.getName())).thenReturn("someComment");

        final Survey returnedSurvey = classUnderTest.createSurvey(someBallotTitle1 + "\r\n" + someBallotTitle2, somePage, "");

        assertEquals(someBallotTitle1, returnedSurvey.getBallot(someBallotTitle1).getTitle());
        assertEquals(someBallotTitle2, returnedSurvey.getBallot(someBallotTitle2).getTitle());
        assertEquals("someComment", returnedSurvey.getBallot(someBallotTitle1).getCommentForUser(SOME_USER1.getName()).getComment());
    }
}
