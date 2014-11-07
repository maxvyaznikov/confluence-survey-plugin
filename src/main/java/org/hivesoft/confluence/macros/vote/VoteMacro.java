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
package org.hivesoft.confluence.macros.vote;

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
import com.atlassian.extras.common.log.Logger;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.survey.SurveyMacro;
import org.hivesoft.confluence.utils.SurveyManager;
import org.hivesoft.confluence.utils.SurveyUtils;
import org.hivesoft.confluence.utils.VelocityAbstractionHelper;
import org.hivesoft.confluence.model.vote.Ballot;
import org.hivesoft.confluence.model.vote.Choice;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This macro defines a simple voting mechanism against a particular topic. Users may only vote once (unless changeable votes is true), can only vote for one choice, and cannot see the overall results
 * until after they have voted.
 */
public class VoteMacro implements Macro {
  private static final Logger.Log LOG = Logger.getInstance(VoteMacro.class);

  public static final String VOTE_MACRO = "vote";

  // prefix vote to make a vote unique in the text properties
  public static final String VOTE_PREFIX = "vote.";

  private final PluginSettingsFactory pluginSettingsFactory;
  private final SurveyManager surveyManager;
  private final TemplateRenderer renderer;
  private final XhtmlContent xhtmlContent;
  private final VelocityAbstractionHelper velocityAbstractionHelper;

  public VoteMacro(SurveyManager surveyManager, TemplateRenderer renderer, XhtmlContent xhtmlContent, PluginSettingsFactory pluginSettingsFactory, VelocityAbstractionHelper velocityAbstractionHelper) {
    this.surveyManager = surveyManager;
    this.renderer = renderer;
    this.xhtmlContent = xhtmlContent;
    this.pluginSettingsFactory = pluginSettingsFactory;
    this.velocityAbstractionHelper = velocityAbstractionHelper;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public BodyType getBodyType() {
    return BodyType.PLAIN_TEXT;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OutputType getOutputType() {
    return OutputType.BLOCK;
  }

  /**
   * New Confluence 4 xhtml stuff {@inheritDoc}
   */
  @Override
  @RequiresFormat(value = Format.View)
  public String execute(Map<String, String> parameters, String body, ConversionContext conversionContext) throws MacroExecutionException {
    final List<String> voteMacroTitles = new ArrayList<String>();
    final String voteMacroTitle = SurveyUtils.getTitleInMacroParameters(parameters);
    if (StringUtils.isBlank(voteMacroTitle)) {
      final String message = "Title parameter is mandatory but was not present!";
      LOG.error(message);
      throw new MacroExecutionException(message);
    }
    if (conversionContext.getEntity() == null) {
      throw new MacroExecutionException("The survey could not be rendered. Probably this is not a persistable ContentObject");
    }
    try {
      LOG.info("Try executing " + VOTE_MACRO + "-macro XHtml Style with title: '" + voteMacroTitle + "' and body: '" + body + "'");

      //FIXME: migrate question+answer to the uniqueId parameter or a random number if not present (and then stored as uniqueId)
      //1. determine if it has old votes and comments stored to migrate
      xhtmlContent.handleMacroDefinitions(conversionContext.getEntity().getBodyAsString(), conversionContext, new MacroDefinitionHandler() {
        @Override
        public void handle(MacroDefinition macroDefinition) {
          if (VOTE_MACRO.equals(macroDefinition.getName())) {
            final Map<String, String> parameters = macroDefinition.getParameters();
            String currentTitle = StringUtils.defaultString(parameters.get(VoteConfig.KEY_TITLE)).trim();
            if (voteMacroTitle.equalsIgnoreCase(currentTitle)) {
              if (voteMacroTitles.contains(currentTitle)) {
                LOG.info("A " + VOTE_MACRO + "-macro must not have the same title / question: " + currentTitle + " on the same page!");
                if (voteMacroTitles.contains(voteMacroTitle)) {
                  voteMacroTitles.add(voteMacroTitle + ".vote");
                }
              } else {
                voteMacroTitles.add(currentTitle);
              }
            }
          }
          if (SurveyMacro.SURVEY_MACRO.equals(macroDefinition.getName())) {
            final String[] lines = macroDefinition.getBodyText().split("\n");
            for (String line : lines) {
              if (StringUtils.isNotBlank(line)) {
                final String currentBallotTitle = line.split("\\-")[0].trim();
                if (voteMacroTitle.equalsIgnoreCase(currentBallotTitle)) {
                  if (voteMacroTitles.contains(currentBallotTitle)) {
                    LOG.info("A survey-macro should not have the same ballot as this vote " + currentBallotTitle);
                    if (voteMacroTitles.contains(voteMacroTitle)) {
                      voteMacroTitles.add(voteMacroTitle + ".survey");
                    }
                  } else {
                    voteMacroTitles.add(currentBallotTitle);
                  }
                }
              }
            }
          }
        }
      });
      if (voteMacroTitles.contains(voteMacroTitle + ".vote") || voteMacroTitles.contains(voteMacroTitle + ".survey")) {
        throw new MacroExecutionException("The " + VOTE_MACRO + "-macro with title '" + voteMacroTitle + "' exists more then one time on this page or has the same question than a survey. That is not allowed. Please change one of them!");
      }
    } catch (XhtmlException e) {
      throw new MacroExecutionException(e);
    }

    final ContentEntityObject contentObject = conversionContext.getEntity(); // surveyManager.getPageEntityFromConversionContext(conversionContext);

    Ballot ballot = surveyManager.reconstructBallotFromPlainTextMacroBody(parameters, body, contentObject);

    if (conversionContext.getEntity() instanceof Comment) {
      ballot.getConfig().addRenderProblems("Voting within comments is currently unsupported. See https://github.com/drohne1673/confluence-survey-plugin/issues/25 for details");
    }

    final List<String> noneUniqueTitles = new ArrayList<String>();
    if (ballot.getChoices().size() != 0) {
      for (Choice choice : ballot.getChoices()) {
        if (noneUniqueTitles.contains(choice.getDescription())) {
          ballot.getConfig().addRenderProblems("The choice-descriptions must be unique! The row starting with title of '" + choice.getDescription() + "' violated that. Please rename your choices to unique answers!");
        } else {
          noneUniqueTitles.add(choice.getDescription());
        }
      }
    }   //don't render a error (that's not nice), render a warning element within velocity

    final List<String> violatingMaxStorableKeyLengthItems = SurveyUtils.getViolatingMaxStorableKeyLengthItems(ballot.getBallotTitlesWithChoiceNames());
    ballot.getConfig().addRenderProblems(violatingMaxStorableKeyLengthItems.toArray(new String[violatingMaxStorableKeyLengthItems.size()]));

    // now create a simple velocity context and render a template for the output
    Map<String, Object> contextMap = velocityAbstractionHelper.getDefaultVelocityContext(); // MacroUtils.defaultVelocityContext();
    contextMap.put("content", contentObject);
    contextMap.put("ballot", ballot);
    contextMap.put("iconSet", SurveyUtils.getIconSetFromPluginSettings(pluginSettingsFactory));
    contextMap.put("currentUser", surveyManager.getCurrentUser());

    String templateToRender = "templates/macros/vote/votemacro-renderproblems.vm";
    if (ballot.getConfig().getRenderProblems().isEmpty()) {
      templateToRender="templates/macros/vote/votemacro.vm";
    }

    try {
      StringWriter renderedTemplate = new StringWriter();
      renderer.render(templateToRender, contextMap, renderedTemplate);
      return renderedTemplate.toString();
    } catch (IOException e) {
      final String message = "Error while trying to display Ballot!";
      LOG.error(message, e);
      throw new MacroExecutionException(message, e);
    }
  }
}
