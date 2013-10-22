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
package org.hivesoft.confluence.macros.survey;

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
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.confluence.xhtml.api.MacroDefinition;
import com.atlassian.confluence.xhtml.api.MacroDefinitionHandler;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.opensymphony.util.TextUtils;
import com.opensymphony.webwork.ServletActionContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hivesoft.confluence.admin.AdminResource;
import org.hivesoft.confluence.macros.survey.model.Survey;
import org.hivesoft.confluence.macros.survey.model.SurveySummary;
import org.hivesoft.confluence.macros.utils.SurveyUtils;
import org.hivesoft.confluence.macros.vote.VoteMacro;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.macros.vote.model.Choice;
import org.hivesoft.confluence.macros.vote.model.Comment;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * This macro defines a simple survey mechanism against set of topics using the vote macro as its collection mechanism.
 */
public class SurveyMacro extends VoteMacro implements Macro {
    private static final Logger LOG = Logger.getLogger(SurveyMacro.class);

    private static final String SURVEY_MACRO = "survey";

    private static final String KEY_CHOICES = "choices";
    private static final String KEY_SHOW_SUMMARY = "showSummary";
    //private static final String KEY_SHOW_TOP_SUMMARY = "showTopSummary"; if showSummary is true and last is not, then obviously it must be top
    private static final String KEY_SHOW_LAST = "showLast";
    private static final String KEY_SHOW_COMMENTS = "showComments";
    private static final String KEY_START_BOUND = "startBound";
    private static final String KEY_ITERATE_STEP = "iterateStep";

    private final static String[] defaultBallotLabels = new String[]{"5-Outstanding", "4-More Than Satisfactory", "3-Satisfactory", "2-Less Than Satisfactory", "1-Unsatisfactory"};
    private final static String[] defaultOldBallotLabels = new String[]{"5 - Outstanding", "4 - More Than Satisfactory", "3 - Satisfactory", "2 - Less Than Satisfactory", "1 - Unsatisfactory"};

    private String[] ballotLabels = null; // 1.1.3 allow a self defined ballotLabels-List
    private ArrayList<String> myChoicesList = null;

    public SurveyMacro(PageManager pageManager, SpaceManager spaceManager, ContentPropertyManager contentPropertyManager, UserAccessor userAccessor, UserManager userManager, TemplateRenderer renderer, XhtmlContent xhtmlContent, PluginSettingsFactory pluginSettingsFactory) {
        super(pageManager, spaceManager, contentPropertyManager, userAccessor, userManager, renderer, xhtmlContent, pluginSettingsFactory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInline() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasBody() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
            final String surveyMacroTitle = StringUtils.defaultString(parameters.get(KEY_TITLE)).trim();
            LOG.info("Try executing " + SURVEY_MACRO + "-macro XHtml Style with title: '" + surveyMacroTitle + "' body: '" + body + "'");
            xhtmlContent.handleMacroDefinitions(conversionContext.getEntity().getBodyAsString(), conversionContext, new MacroDefinitionHandler() {
                @Override
                public void handle(MacroDefinition macroDefinition) {
                    if (SURVEY_MACRO.equals(macroDefinition.getName())) {
                        final Map<String, String> parameters = macroDefinition.getParameters();
                        String currentTitle = StringUtils.defaultString(parameters.get(KEY_TITLE)).trim();
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

        try {
            return execute(parameters, body, (RenderContext) conversionContext.getPageContext());
        } catch (MacroException e) {
            throw new MacroExecutionException(e);
        }
    }


    /**
     * Get the HTML rendering of this macro, Confluence V. 3 and less.
     *
     * @param parameters    Any parameters passed into this macro. This macro expects is a single, unnamed parameter.
     * @param body          The rendered body of the macro
     * @param renderContext The render context for the current page rendering.
     * @return String respresenting the HTML rendering of this macro
     * @throws MacroException If an exception occurs rendering the HTML
     */
    @Override
    public String execute(Map parameters, String body, RenderContext renderContext) throws MacroException {
        // retrieve a reference to the body object this macro is in
        ContentEntityObject contentObject = ((PageContext) renderContext).getEntity();

        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

        String iconSet = (String) settings.get(AdminResource.SURVEY_PLUGIN_KEY_ICON_SET);
        if (StringUtils.isBlank(iconSet)) {
            iconSet = AdminResource.SURVEY_PLUGIN_ICON_SET_DEFAULT;
        }

        // Create the survey model, 1.1.3 add the parameters map
        Survey survey = createSurvey(body, contentObject, (String) parameters.get(KEY_CHOICES));

        final List<String> noneUniqueTitles = new ArrayList<String>();
        for (Ballot ballot : survey.getBallots()) {
            if (noneUniqueTitles.contains(ballot.getTitle())) {
                throw new MacroException("The ballot-titles must be unique! The row starting with title of '" + ballot.getTitle() + "' violated that. Please rename your choices to unique answers!");
            } else {
                noneUniqueTitles.add(ballot.getTitle());
            }
        }

        SurveyUtils.validateMaxStorableKeyLength(survey.getBallotTitlesWithChoiceNames());

        survey.setTitle(StringUtils.defaultString((String) parameters.get(KEY_TITLE)).trim());
        survey.setChangeableVotes(SurveyUtils.getBooleanFromString((String) parameters.get(KEY_CHANGEABLE_VOTES), false));

        // 1.1.7.1: default with 5 options and a step 1 .. 1..5 (or ordered 5..1)
        int startBound = Ballot.DEFAULT_START_BOUND;
        String sTmpParam = (String) parameters.get(KEY_START_BOUND);
        if (sTmpParam != null) {
            startBound = Integer.valueOf(sTmpParam).intValue();
        }
        int iterateStep = Ballot.DEFAULT_ITERATE_STEP;
        sTmpParam = (String) parameters.get(KEY_ITERATE_STEP);
        if (sTmpParam != null) {
            iterateStep = Integer.valueOf(sTmpParam).intValue();
        }
        // Hardcoded default is 1 and 1, if it is different, set it for each ballot, let default otherwise
        if (startBound != Ballot.DEFAULT_START_BOUND || iterateStep != Ballot.DEFAULT_ITERATE_STEP) {
            survey.setStartBoundAndIterateStep(startBound, iterateStep);
        }

        SurveySummary surveySummary = SurveySummary.Top;
        if (!SurveyUtils.getBooleanFromString((String) parameters.get(KEY_SHOW_SUMMARY), true)) {
            surveySummary = SurveySummary.None;
        } else {
            if (SurveyUtils.getBooleanFromString((String) parameters.get(KEY_SHOW_LAST), false)) {
                surveySummary = SurveySummary.Bottom;
            }
        }
        survey.setSurveySummary(surveySummary);

        // check if any request parameters came in to vote on a ballot
        HttpServletRequest request = ServletActionContext.getRequest();

        if (request != null) {
            // Try to retrieve the proper ballot
            Ballot ballot = survey.getBallot(request.getParameter(REQUEST_PARAMETER_BALLOT));

            // If there is a ballot found, cast the vote using our super's method
            if (ballot != null) {
                recordVote(ballot, request, contentObject, (String) parameters.get(KEY_VOTERS));
            }
        }

        String renderTitleLevel = "3";
        String surveyRenderTitleLevel = (String) parameters.get(KEY_RENDER_TITLE_LEVEL);
        if (StringUtils.isBlank(surveyRenderTitleLevel)) {
            surveyRenderTitleLevel = "2";
        } else {
            if (Integer.valueOf(surveyRenderTitleLevel) == 0) {
                surveyRenderTitleLevel = "";
                renderTitleLevel = "";
            } else {
                renderTitleLevel = (Integer.valueOf(surveyRenderTitleLevel) + 1) + "";
            }
        }

        final String remoteUsername = permissionEvaluator.getRemoteUsername();

        Boolean canSeeResults;

        if (!TextUtils.stringSet((String) parameters.get(KEY_VIEWERS)) && Boolean.valueOf(StringUtils.defaultString((String) parameters.get(KEY_LOCKED))).booleanValue()) {
            canSeeResults = Boolean.TRUE;
        } else {
            canSeeResults = permissionEvaluator.getCanPerformAction((String) parameters.get(KEY_VIEWERS), remoteUsername);
        }

        Boolean canTakeSurvey = permissionEvaluator.getCanPerformAction((String) parameters.get(KEY_VOTERS), remoteUsername);
        Boolean canSeeVoters = permissionEvaluator.getCanSeeVoters((String) parameters.get(KEY_VISIBLE_VOTERS), canSeeResults);
        // survey parameter must be passed through
        if (canSeeVoters == Boolean.TRUE) { // default is false, so only set if true
            survey.setVisibleVoters(true);
        }

        // now create a simple velocity context and render a template for the output
        Map<String, Object> contextMap = MacroUtils.defaultVelocityContext();
        contextMap.put("survey", survey);
        contextMap.put("content", contentObject);
        contextMap.put("context", renderContext);
        contextMap.put("iconSet", iconSet);
        contextMap.put("surveyRenderTitleLevel", surveyRenderTitleLevel);
        contextMap.put(KEY_RENDER_TITLE_LEVEL, renderTitleLevel);
        contextMap.put(KEY_SHOW_COMMENTS, SurveyUtils.getBooleanFromString((String) parameters.get(KEY_SHOW_COMMENTS), true));
        contextMap.put(KEY_LOCKED, SurveyUtils.getBooleanFromString((String) parameters.get(KEY_LOCKED), false));
        contextMap.put(KEY_VISIBLE_VOTERS_WIKI, SurveyUtils.getBooleanFromString((String) parameters.get(KEY_VISIBLE_VOTERS_WIKI), false));
        contextMap.put("canSeeSurveyResults", canSeeResults);
        contextMap.put("canTakeSurvey", canTakeSurvey);
        contextMap.put("canSeeSurveyVoters", canSeeVoters);

        try {
            if (canSeeResults.booleanValue() || canTakeSurvey.booleanValue()) {
                return VelocityUtils.getRenderedTemplate("templates/macros/survey/surveymacro.vm", contextMap);
            }

            return VelocityUtils.getRenderedTemplate("templates/macros/survey/surveymacro-denied.vm", contextMap);
        } catch (Exception e) {
            LOG.error("Error while trying to display Survey!", e);
            throw new MacroException(e);
        }
    }

    /**
     * Create a survey object for the given macro body pre-populated with all choices that have previously been made by the users.
     *
     * @param body          The rendered body of the macro.
     * @param contentObject The content object from which the votes can be read.
     * @return The survey object, pre-filled with the correct data.
     */
    protected Survey createSurvey(String body, ContentEntityObject contentObject, String choices) {
        Survey survey = new Survey();

        if (StringUtils.isBlank(body)) {
            return survey;
        }

        String tmpBallotLabels = choices;

        // Reconstruct all of the votes that have been cast so far
        for (StringTokenizer stringTokenizer = new StringTokenizer(body, "\r\n"); stringTokenizer.hasMoreTokens(); ) {
            String line = StringUtils.chomp(stringTokenizer.nextToken().trim());

            // the parameter given list must override the inline Labels if none are there
            if (tmpBallotLabels != null) {
                ballotLabels = tmpBallotLabels.split(",");
            } else {
                ballotLabels = null; // null the ballotLabels, will be filled later
            }

            if ((!StringUtils.isBlank(line) && Character.getNumericValue(line.toCharArray()[0]) > -1) || line.length() > 1) {
                String[] titleDescription = line.split("\\-", -1);

                Ballot ballot = new Ballot(titleDescription[0].trim());
                survey.addBallot(ballot);

                if (titleDescription.length > 1) {
                    ballot.setDescription(titleDescription[1].trim());
                }

                try {
                    String commenters = contentPropertyManager.getTextProperty(contentObject, "survey." + ballot.getTitle() + ".commenters");

                    if (TextUtils.stringSet(commenters)) {
                        for (StringTokenizer commenterTokenizer = new StringTokenizer(commenters, "|"); commenterTokenizer.hasMoreTokens(); ) {
                            String commenter = commenterTokenizer.nextToken();
                            if (TextUtils.stringSet(commenter)) {
                                String comment = contentPropertyManager.getTextProperty(contentObject, "survey." + ballot.getTitle() + ".comment." + commenter);
                                ballot.addComment(new Comment(commenter, comment));
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Try contentPropertyManager: " + contentPropertyManager + " or contentEntity: " + contentObject + " or ballot was broken: " + ballot, e);
                }

                int customChoice = 0;
                myChoicesList = new ArrayList<String>();
                for (customChoice = 2; customChoice < titleDescription.length; customChoice++) {
                    String temp = titleDescription[customChoice];
                    myChoicesList.add(temp);
                }

                // 1.1.3: first take the list inLine
                if (myChoicesList.size() > 1) { // should be a minimum of 2 choices
                    ballotLabels = new String[myChoicesList.size()];
                    myChoicesList.toArray(ballotLabels);
                } else {
                    if (ballotLabels == null) { // second was there no parameterList?
                        ballotLabels = defaultBallotLabels;
                    }
                    // 3rd if there was a parameterList it will be in ballotLabels by default
                }

                // Load all of the choices and votes into the ballot
                for (int i = 0; i < ballotLabels.length; i++) {
                    Choice choice = new Choice(ballotLabels[i]);
                    // 1.1.7.6 if this ballot is a default one, check whether there are old default items and convert see CSRVY-21 for details
                    if (i < defaultBallotLabels.length && ballotLabels[i] == defaultBallotLabels[i]) {
                        // check for old votes
                        String votes = contentPropertyManager.getTextProperty(contentObject, "vote." + ballot.getTitle() + "." + defaultOldBallotLabels[i]);
                        if (TextUtils.stringSet(votes)) {
                            // if present save the new (spaces reduced one)
                            contentPropertyManager.setTextProperty(contentObject, "vote." + ballot.getTitle() + "." + defaultBallotLabels[i], votes);
                            // delete the old key
                            contentPropertyManager.setTextProperty(contentObject, "vote." + ballot.getTitle() + "." + defaultOldBallotLabels[i], null);
                        }
                    }
                    ballot.addChoice(choice);

                    // changed string to TextProperty
                    String votes = contentPropertyManager.getTextProperty(contentObject, "vote." + ballot.getTitle() + "." + choice.getDescription());
                    if (TextUtils.stringSet(votes)) {
                        for (StringTokenizer voteTokenizer = new StringTokenizer(votes, ","); voteTokenizer.hasMoreTokens(); ) {
                            choice.voteFor(voteTokenizer.nextToken());
                        }
                    }
                }
            }
        }

        return survey;
    }
}
