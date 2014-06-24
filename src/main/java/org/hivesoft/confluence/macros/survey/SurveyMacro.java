/**
 * Copyright (c) 2006-2014, Confluence Community
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hivesoft.confluence.macros.survey;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.XhtmlException;
import com.atlassian.confluence.content.render.xhtml.macro.annotation.Format;
import com.atlassian.confluence.content.render.xhtml.macro.annotation.RequiresFormat;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.pages.Comment;
import com.atlassian.confluence.xhtml.api.MacroDefinition;
import com.atlassian.confluence.xhtml.api.MacroDefinitionHandler;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hivesoft.confluence.macros.survey.model.Survey;
import org.hivesoft.confluence.macros.utils.SurveyManager;
import org.hivesoft.confluence.macros.utils.SurveyUtils;
import org.hivesoft.confluence.macros.utils.VelocityAbstractionHelper;
import org.hivesoft.confluence.macros.utils.VoterRenderer;
import org.hivesoft.confluence.macros.vote.model.Ballot;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This macro defines a simple survey mechanism against set of topics using the vote macro as its collection mechanism.
 */
public class SurveyMacro implements Macro {
  private static final Logger LOG = Logger.getLogger(SurveyMacro.class);

  public static final String SURVEY_MACRO = "survey";

  private final PluginSettingsFactory pluginSettingsFactory;
  private final SurveyManager surveyManager;
  private final TemplateRenderer renderer;
  private final XhtmlContent xhtmlContent;
  private final VelocityAbstractionHelper velocityAbstractionHelper;

  public SurveyMacro(PluginSettingsFactory pluginSettingsFactory, SurveyManager surveyManager, TemplateRenderer renderer, XhtmlContent xhtmlContent, VelocityAbstractionHelper velocityAbstractionHelper) {
    this.pluginSettingsFactory = pluginSettingsFactory;
    this.surveyManager = surveyManager;
    this.renderer = renderer;
    this.xhtmlContent = xhtmlContent;
    this.velocityAbstractionHelper = velocityAbstractionHelper;
  }

  /**
   * New Confluence 4 xhtml stuff {@inheritDoc}
   */
  @Override
  @RequiresFormat(value = Format.View)
  public String execute(Map<String, String> parameters, String body, ConversionContext conversionContext) throws MacroExecutionException {
    final List<String> macros = new ArrayList<String>();
    try {
      final String surveyMacroTitle = StringUtils.defaultString(parameters.get(SurveyConfig.KEY_TITLE)).trim();
      if (LOG.isInfoEnabled()) {
        LOG.info("Try executing " + SURVEY_MACRO + "-macro XHtml Style with title: '" + surveyMacroTitle + "' body: '" + body + "'");
      }
      if (conversionContext.getEntity() == null) {
        throw new MacroExecutionException("The survey could not be rendered. Probably this is not a persistable ContentObject");
      }
      xhtmlContent.handleMacroDefinitions(conversionContext.getEntity().getBodyAsString(), conversionContext, new MacroDefinitionHandler() {
        @Override
        public void handle(MacroDefinition macroDefinition) {
          if (SURVEY_MACRO.equals(macroDefinition.getName())) {
            final Map<String, String> parameters = macroDefinition.getParameters();
            String currentTitle = StringUtils.defaultString(parameters.get(SurveyConfig.KEY_TITLE)).trim();
            if (!StringUtils.isBlank(currentTitle)) {
              if (macros.contains(currentTitle)) {
                LOG.warn("A " + SURVEY_MACRO + "-macro should not have the same title " + currentTitle + " on the same page! In newer version it may become mandatory / unique.");
              } else {
                macros.add(currentTitle);
              }
            }
          }
        }
      });
    } catch (XhtmlException e) {
      throw new MacroExecutionException(e);
    }

    ContentEntityObject contentObject = conversionContext.getEntity(); // surveyManager.getPageEntityFromConversionContext(conversionContext);

    Survey survey = surveyManager.reconstructSurveyFromPlainTextMacroBody(body, contentObject, parameters);

    if (conversionContext.getEntity() instanceof Comment) {
      survey.getConfig().addRenderProblems("Voting within comments is currently unsupported. See https://github.com/drohne1673/confluence-survey-plugin/issues/25 for details");
    }

    final List<String> noneUniqueTitles = new ArrayList<String>();
    for (Ballot ballot : survey.getBallots()) {
      if (noneUniqueTitles.contains(ballot.getTitle())) {
        survey.getConfig().addRenderProblems("The ballot-titles must be unique! The row starting with title of '" + ballot.getTitle() + "' violated that. Please rename your choices to unique answers!");
      } else {
        noneUniqueTitles.add(ballot.getTitle());
      }
    }

    final List<String> violatingMaxStorableKeyLengthItems = SurveyUtils.getViolatingMaxStorableKeyLengthItems(survey.getBallotTitlesWithChoiceNames());
    survey.getConfig().addRenderProblems(violatingMaxStorableKeyLengthItems.toArray(new String[violatingMaxStorableKeyLengthItems.size()]));

    // now create a simple velocity context and render a template for the output
    Map<String, Object> contextMap = velocityAbstractionHelper.getDefaultVelocityContext(); // MacroUtils.defaultVelocityContext();
    contextMap.put("voterRenderer", new VoterRenderer());
    contextMap.put("content", contentObject);
    contextMap.put("survey", survey);
    contextMap.put("iconSet", SurveyUtils.getIconSetFromPluginSettings(pluginSettingsFactory));
    contextMap.put("currentUser", surveyManager.getCurrentUser());

    try {
      StringWriter renderedTemplate = new StringWriter();
      if (!survey.getConfig().getRenderProblems().isEmpty()) {
        renderer.render("templates/macros/survey/surveymacro-renderproblems.vm", contextMap, renderedTemplate);
      } else if (survey.getConfig().isCanSeeResults() || survey.getConfig().isCanTakeSurvey()) {
        renderer.render("templates/macros/survey/surveymacro.vm", contextMap, renderedTemplate);
      } else {
        renderer.render("templates/macros/survey/surveymacro-denied.vm", contextMap, renderedTemplate);
      }
      return renderedTemplate.toString();
    } catch (Exception e) {
      LOG.error("Error while trying to display Survey!", e);
      throw new MacroExecutionException(e);
    }
  }

  @Override
  public BodyType getBodyType() {
    return BodyType.PLAIN_TEXT;
  }

  @Override
  public OutputType getOutputType() {
    return OutputType.BLOCK;
  }
}
