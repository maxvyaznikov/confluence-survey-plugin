/**
 * Copyright (c) 2006-2013, Confluence Community
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
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.xhtml.api.MacroDefinition;
import com.atlassian.confluence.xhtml.api.MacroDefinitionHandler;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.opensymphony.webwork.ServletActionContext;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.VelocityAbstractionHelper;
import org.hivesoft.confluence.macros.utils.PermissionEvaluator;
import org.hivesoft.confluence.macros.utils.SurveyManager;
import org.hivesoft.confluence.macros.utils.SurveyUtils;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.macros.vote.model.Choice;
import org.hivesoft.confluence.rest.AdminResource;

import javax.servlet.http.HttpServletRequest;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This macro defines a simple voting mechanism against a particular topic. Users may only vote once (unless changeable votes is true), can only vote for one choice, and cannot see the overall results
 * until after they have voted.
 */
public class VoteMacro extends BaseMacro implements Macro {
  private static final Logger.Log LOG = Logger.getInstance(VoteMacro.class);

  public static final String VOTE_MACRO = "vote";

  public static final String REQUEST_PARAMETER_BALLOT = "ballot.title";
  public static final String REQUEST_PARAMETER_CHOICE = "vote.choice";
  public static final String REQUEST_PARAMETER_VOTE_ACTION = "vote.action";

  // prefix vote to make a vote unique in the textproperties
  public static final String VOTE_PREFIX = "vote.";

  public static final String KEY_TITLE = "title";
  protected static final String KEY_RENDER_TITLE_LEVEL = "renderTitleLevel";
  protected static final String KEY_CHANGEABLE_VOTES = "changeableVotes";
  protected static final String KEY_VOTERS = "voters";
  protected static final String KEY_VIEWERS = "viewers";
  protected static final String KEY_SHOW_COMMENTS = "showComments";
  protected static final String KEY_VISIBLE_VOTERS = "visibleVoters";
  protected static final String KEY_VISIBLE_VOTERS_WIKI = "visibleVotersWiki";
  protected static final String KEY_LOCKED = "locked";

  protected final PluginSettingsFactory pluginSettingsFactory;
  protected final PageManager pageManager;
  protected final SurveyManager surveyManager;
  protected final PermissionEvaluator permissionEvaluator;
  protected final TemplateRenderer renderer;
  protected final XhtmlContent xhtmlContent;
  protected final VelocityAbstractionHelper velocityAbstractionHelper;

  public VoteMacro(PageManager pageManager, ContentPropertyManager contentPropertyManager, UserAccessor userAccessor, UserManager userManager, TemplateRenderer renderer, XhtmlContent xhtmlContent, PluginSettingsFactory pluginSettingsFactory, VelocityAbstractionHelper velocityAbstractionHelper) {
    this.pageManager = pageManager;
    this.surveyManager = new SurveyManager(contentPropertyManager);
    this.permissionEvaluator = new PermissionEvaluator(userAccessor, userManager);
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
   * This macro generates tabular data, which is not an inline element.
   */
  @Override
  public boolean isInline() {
    return false;
  }

  /**
   * The vote choices are the body of this macro.
   */
  @Override
  public boolean hasBody() {
    return true;
  }

  /**
   * Body should not be rendered, as we want to control it ourselves
   */
  public RenderMode getBodyRenderMode() {
    return RenderMode.NO_RENDER;
  }

  /**
   * New Confluence 4 xhtml stuff {@inheritDoc}
   */
  @Override
  @RequiresFormat(value = Format.View)
  public String execute(Map<String, String> parameters, String body, ConversionContext conversionContext) throws MacroExecutionException {
    final List<String> macros = new ArrayList<String>();
    try {
      final String voteMacroTitle = SurveyUtils.getTitleInMacroParameters(parameters);
      LOG.info("Try executing " + VOTE_MACRO + "-macro XHtml Style with title: '" + voteMacroTitle + "' and body: '" + body + "'");
      xhtmlContent.handleMacroDefinitions(conversionContext.getEntity().getBodyAsString(), conversionContext, new MacroDefinitionHandler() {
        @Override
        public void handle(MacroDefinition macroDefinition) {
          if (VOTE_MACRO.equals(macroDefinition.getName())) {
            final Map<String, String> parameters = macroDefinition.getParameters();
            String currentTitle = StringUtils.defaultString(parameters.get(KEY_TITLE)).trim();
            if (!StringUtils.isBlank(currentTitle)) {
              if (macros.contains(currentTitle)) {
                LOG.info("A " + VOTE_MACRO + "-macro should not have the same title " + currentTitle + " on the same page! In newer version it may become mandatory / unique.");
                if (macros.contains(voteMacroTitle)) {
                  macros.add(voteMacroTitle.toUpperCase());
                }
              } else {
                macros.add(currentTitle);
              }
            }
          }
        }
      });
      if (macros.contains(voteMacroTitle.toUpperCase())) {
        throw new MacroExecutionException("The " + VOTE_MACRO + "-macro with title '" + voteMacroTitle + "' exists more then one time on this page. That is not allowed. Please change one of them!");
      }
    } catch (XhtmlException e) {
      throw new MacroExecutionException(e);
    } catch (MacroException e) {
      throw new MacroExecutionException(e);
    }

    try {
      return execute(parameters, body, (RenderContext) conversionContext.getPageContext());
    } catch (MacroException e) {
      throw new MacroExecutionException(e);
    }
  }

  /**
   * Get the HTML rendering of this macro.
   *
   * @param parameters    Any parameters passed into this macro.
   * @param body          The raw body of the macro
   * @param renderContext The render context for the current page rendering.
   * @return String representing the HTML rendering of this macro
   * @throws MacroException If an exception occurs rendering the HTML
   */
  @Override
  public String execute(@SuppressWarnings("rawtypes") Map parameters, String body, RenderContext renderContext) throws MacroException {
    // retrieve a reference to the body object this macro is in
    ContentEntityObject contentObject = ((PageContext) renderContext).getEntity();

    PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
    String iconSet = (String) settings.get(AdminResource.SURVEY_PLUGIN_KEY_ICON_SET);
    if (StringUtils.isBlank(iconSet)) {
      iconSet = AdminResource.SURVEY_PLUGIN_ICON_SET_DEFAULT;
    }

    String title = SurveyUtils.getTitleInMacroParameters(parameters);
    // Rebuild the model for this ballot
    Ballot ballot = surveyManager.reconstructBallot(title, body, contentObject);
    ballot.setChangeableVotes(Boolean.parseBoolean((String) parameters.get(KEY_CHANGEABLE_VOTES)));

    final List<String> noneUniqueTitles = new ArrayList<String>();
    if (ballot.getChoices().size() != 0) {
      for (Choice choice : ballot.getChoices()) {
        if (noneUniqueTitles.contains(choice.getDescription())) {
          throw new MacroException("The choice-descriptions must be unique! The row starting with title of '" + choice.getDescription() + "' violated that. Please rename your choices to unique answers!");
        } else {
          noneUniqueTitles.add(choice.getDescription());
        }
      }
    }   //don't render a error (that's not nice), render a warning element within velocity

    SurveyUtils.validateMaxStorableKeyLength(ballot.getBallotTitlesWithChoiceNames());

    String renderTitleLevel = (String) parameters.get(KEY_RENDER_TITLE_LEVEL);
    if (!StringUtils.isBlank(renderTitleLevel)) {
      ballot.setRenderTitleLevel(Integer.valueOf(renderTitleLevel));
    }

    ballot.setLocked(SurveyUtils.getBooleanFromString((String) parameters.get(KEY_LOCKED), false));
    ballot.setVisibleComments(SurveyUtils.getBooleanFromString((String) parameters.get(KEY_SHOW_COMMENTS), false));

    final String remoteUsername = permissionEvaluator.getRemoteUsername();

    Boolean canSeeResults;
    if (StringUtils.isBlank((String) parameters.get(KEY_VIEWERS)) && ballot.isLocked()) {
      canSeeResults = Boolean.TRUE;
    } else {
      canSeeResults = permissionEvaluator.getCanPerformAction((String) parameters.get(KEY_VIEWERS), remoteUsername);
    }

    Boolean canTakeSurvey = permissionEvaluator.getCanPerformAction((String) parameters.get(KEY_VOTERS), remoteUsername);
    ballot.setVisibleVoters(permissionEvaluator.getCanSeeVoters((String) parameters.get(KEY_VISIBLE_VOTERS), canSeeResults));

    // check if any request parameters came in to complete or uncomplete tasks
    HttpServletRequest request = ServletActionContext.getRequest();

    if (request != null && ballot.getTitle().equals(request.getParameter(REQUEST_PARAMETER_BALLOT))) {
      recordVote(ballot, request, contentObject, (String) parameters.get(KEY_VOTERS));
    }

    // now create a simple velocity context and render a template for the output
    Map<String, Object> contextMap = velocityAbstractionHelper.getDefaultVelocityContext(); // MacroUtils.defaultVelocityContext();
    contextMap.put("content", contentObject);
    contextMap.put("context", renderContext);
    contextMap.put("ballot", ballot);
    contextMap.put("iconSet", iconSet);
    contextMap.put("canSeeSurveyResults", canSeeResults);
    contextMap.put("canTakeSurvey", canTakeSurvey);
    contextMap.put(KEY_VISIBLE_VOTERS_WIKI, SurveyUtils.getBooleanFromString((String) parameters.get(KEY_VISIBLE_VOTERS_WIKI), false));

    try {
      StringWriter renderedTemplate = new StringWriter();
      renderer.render("templates/macros/vote/votemacro.vm", contextMap, renderedTemplate);
      return renderedTemplate.toString();
      // return VelocityUtils.getRenderedTemplate("templates/macros/vote/votemacro.vm", contextMap);
    } catch (Exception e) {
      LOG.error("Error while trying to display Ballot!", e);
      throw new MacroException(e);
    }
  }

  /**
   * If there is a vote in the request, store it in this page for the given ballot.
   *
   * @param ballot        The ballot being voted on.
   * @param request       The request where the vote and username can be found.
   * @param contentObject The content object where any votes should be stored.
   * @param voters        The list of usernames allowed to vote. If blank, all users can vote.
   */
  protected void recordVote(Ballot ballot, HttpServletRequest request, ContentEntityObject contentObject, String voters) {
    final String remoteUsername = permissionEvaluator.getRemoteUsername();
    String requestBallotTitle = request.getParameter(REQUEST_PARAMETER_BALLOT);
    String requestChoice = request.getParameter(REQUEST_PARAMETER_CHOICE);
    String requestVoteAction = request.getParameter(REQUEST_PARAMETER_VOTE_ACTION);

    LOG.debug("recordVote: found Ballot-Title=" + requestBallotTitle + ", choice=" + requestChoice + ", action=" + requestVoteAction);

    // If there is a choice, make sure the vote is for this ballot and this user can vote
    if (requestChoice != null && ballot.getTitle().equals(requestBallotTitle) && permissionEvaluator.getCanVote(voters, remoteUsername, ballot)) {

      // If this is a re-vote situation, then unvote first
      Choice previousChoice = ballot.getVote(remoteUsername);
      if (previousChoice != null && ballot.isChangeableVotes()) {
        previousChoice.removeVoteFor(remoteUsername);
        surveyManager.setVoteContentProperty(previousChoice, ballot.getTitle(), contentObject);
      }

      Choice choice = ballot.getChoice(requestChoice);

      if (choice != null && "vote".equalsIgnoreCase(requestVoteAction)) {
        LOG.debug("recordVote: found choice in requestChoice: " + choice.getDescription());
        choice.voteFor(remoteUsername);
        surveyManager.setVoteContentProperty(choice, ballot.getTitle(), contentObject);
      }
    }
  }
}
