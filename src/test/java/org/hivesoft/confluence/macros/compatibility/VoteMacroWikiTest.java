package org.hivesoft.confluence.macros.compatibility;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.MacroException;
import org.hivesoft.confluence.macros.vote.VoteMacro;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VoteMacroWikiTest {

  private final VoteMacro mockVoteMacro = mock(VoteMacro.class);

  private VoteMacroWiki classUnderTest;

  @Test
  public void test_properties_success() {
    classUnderTest = new VoteMacroWiki(mockVoteMacro);

    assertThat(classUnderTest.getBodyRenderMode(), is(RenderMode.NO_RENDER));
    assertThat(classUnderTest.hasBody(), is(true));
  }

  @Test
  public void test_execute_success() throws MacroExecutionException, MacroException {
    when(mockVoteMacro.execute(any(Map.class), anyString(), any(ConversionContext.class))).thenReturn("someString");

    classUnderTest = new VoteMacroWiki(mockVoteMacro);

    final String renderedResult = classUnderTest.execute(new HashMap(), "some thing", new RenderContext());
    assertThat(renderedResult, is("someString"));
  }

  @Test(expected = MacroException.class)
  public void test_execute_exception() throws MacroExecutionException, MacroException {
    when(mockVoteMacro.execute(any(Map.class), anyString(), any(ConversionContext.class))).thenThrow(new MacroExecutionException("someException"));

    classUnderTest = new VoteMacroWiki(mockVoteMacro);

    classUnderTest.execute(new HashMap(), "some thing", new RenderContext());
  }
}
