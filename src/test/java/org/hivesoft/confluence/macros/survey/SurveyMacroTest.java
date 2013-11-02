package org.hivesoft.confluence.macros.survey;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.user.impl.DefaultUser;
import com.opensymphony.webwork.views.velocity.VelocityManager;
import org.hivesoft.confluence.admin.callbacks.SurveyPluginSettings;
import org.hivesoft.confluence.macros.VelocityAbstractionHelper;
import org.hivesoft.confluence.macros.survey.model.Survey;
import org.hivesoft.confluence.macros.vote.VoteMacro;
import org.junit.Test;

import java.util.HashMap;

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
    VelocityAbstractionHelper mockVelocityAbstractionHelper = mock(VelocityAbstractionHelper.class);

    ConversionContext mockConversionContext = mock(ConversionContext.class);

    SurveyMacro classUnderTest = new SurveyMacro(mockPageManager, mockSpaceManager, mockContentPropertyManager, mockUserAccessor, mockUserManager, mockTemplateRenderer, mockXhtmlContent, mockPluginSettingsFactory, mockVelocityAbstractionHelper);

    @Test
    public void test_MacroProperties_success() {

        assertTrue(classUnderTest.hasBody());
        assertFalse(classUnderTest.isInline());
        assertEquals(RenderMode.NO_RENDER, classUnderTest.getBodyRenderMode());
    }

    /**
     * Cannot test the result of the velocity content as some elements are not initialized, but the macro is running through
     */
    @Test
    public void test_execute_simpleMacroWithTitle_success() throws Exception {
        final HashMap<String, String> parameters = new HashMap<String, String>();

        ContentEntityObject somePage = new Page();
        somePage.setBodyAsString("{survey}{survey}");
        final PageContext pageContext = new PageContext(somePage);

        when(mockConversionContext.getEntity()).thenReturn(somePage);
        when(mockConversionContext.getPageContext()).thenReturn(pageContext);
        when(mockPluginSettingsFactory.createGlobalSettings()).thenReturn(new SurveyPluginSettings());
        final HashMap<String, Object> contextMap = new HashMap<String, Object>();
        contextMap.put(VelocityManager.ACTION, MacroUtils.getConfluenceActionSupport());
        when(mockVelocityAbstractionHelper.getDefaultVelocityContext()).thenReturn(contextMap);

        final String macroResultAsString = classUnderTest.execute(parameters, "", mockConversionContext);
        //assertTrue(macroResultAsString.contains("someTitle"));
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
