package org.hivesoft.confluence.macros.vote;

import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class VoteMacroTest {


    PageManager mockPageManager = mock(PageManager.class);
    SpaceManager mockSpaceManager = mock(SpaceManager.class);
    ContentPropertyManager mockContentPropertyManager = mock(ContentPropertyManager.class);
    UserAccessor mockUserAccessor = mock(UserAccessor.class);
    TemplateRenderer mockTemplateRenderer = mock(TemplateRenderer.class);
    XhtmlContent mockXhtmlContent = mock(XhtmlContent.class);

    VoteMacro classUnderTest = new VoteMacro(mockPageManager, mockSpaceManager, mockContentPropertyManager, mockUserAccessor, mockTemplateRenderer, mockXhtmlContent);

    @Test
    public void test_MacroProperties_success() {
        assertTrue(classUnderTest.hasBody());
        assertFalse(classUnderTest.isInline());
        assertEquals(Macro.BodyType.PLAIN_TEXT, classUnderTest.getBodyType());
        assertEquals(RenderMode.NO_RENDER, classUnderTest.getBodyRenderMode());
        assertEquals(Macro.OutputType.BLOCK, classUnderTest.getOutputType());
    }


}
