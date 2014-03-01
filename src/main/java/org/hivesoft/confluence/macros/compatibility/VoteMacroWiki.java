package org.hivesoft.confluence.macros.compatibility;

import com.atlassian.confluence.content.render.xhtml.DefaultConversionContext;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import org.hivesoft.confluence.macros.vote.VoteMacro;

import java.util.Map;

public class VoteMacroWiki extends BaseMacro {

  VoteMacro voteMacro;

  public VoteMacroWiki(VoteMacro voteMacro) {
    this.voteMacro = voteMacro;
  }

  @Override
  public boolean hasBody() {
    return true;
  }

  @Override
  public RenderMode getBodyRenderMode() {
    return RenderMode.NO_RENDER;
  }

  @Override
  public String execute(Map parameters, String body, RenderContext renderContext) throws MacroException {
    try {
      return voteMacro.execute(parameters, body, new DefaultConversionContext(renderContext));
    } catch (MacroExecutionException e) {
      throw new MacroException(e);
    }
  }
}
