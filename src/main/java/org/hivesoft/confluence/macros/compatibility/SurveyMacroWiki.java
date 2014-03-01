package org.hivesoft.confluence.macros.compatibility;

import com.atlassian.confluence.content.render.xhtml.DefaultConversionContext;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.macros.survey.SurveyMacro;

import java.util.Map;

public class SurveyMacroWiki extends BaseMacro {

  SurveyMacro surveyMacro;

  public SurveyMacroWiki(SurveyMacro surveyMacro) {
    this.surveyMacro = surveyMacro;
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
      //the new survey-menu functionality does not work with legacy
      parameters.put(SurveyConfig.KEY_MANAGERS,"DisablingFunctionalityToLegacyMacros");
      return surveyMacro.execute(parameters, body, new DefaultConversionContext(renderContext));
    } catch (MacroExecutionException e) {
      throw new MacroException(e);
    }
  }
}
