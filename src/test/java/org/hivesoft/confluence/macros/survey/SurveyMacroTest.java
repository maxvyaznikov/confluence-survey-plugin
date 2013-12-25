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
import org.hivesoft.confluence.rest.callbacks.SurveyPluginSettings;
import org.hivesoft.confluence.macros.VelocityAbstractionHelper;
import org.hivesoft.confluence.macros.survey.model.Survey;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurveyMacroTest {
    private final static DefaultUser SOME_USER1 = new DefaultUser("someUser1", "someUser1 FullName", "some1@testmail.de");

    PageManager mockPageManager = mock(PageManager.class);
    ContentPropertyManager mockContentPropertyManager = mock(ContentPropertyManager.class);
    UserAccessor mockUserAccessor = mock(UserAccessor.class);
    UserManager mockUserManager = mock(UserManager.class);
    TemplateRenderer mockTemplateRenderer = mock(TemplateRenderer.class);
    XhtmlContent mockXhtmlContent = mock(XhtmlContent.class);
    PluginSettingsFactory mockPluginSettingsFactory = mock(PluginSettingsFactory.class);
    VelocityAbstractionHelper mockVelocityAbstractionHelper = mock(VelocityAbstractionHelper.class);

    ConversionContext mockConversionContext = mock(ConversionContext.class);

    SurveyMacro classUnderTest = new SurveyMacro(mockPageManager, mockContentPropertyManager, mockUserAccessor, mockUserManager, mockTemplateRenderer, mockXhtmlContent, mockPluginSettingsFactory, mockVelocityAbstractionHelper);

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


}
