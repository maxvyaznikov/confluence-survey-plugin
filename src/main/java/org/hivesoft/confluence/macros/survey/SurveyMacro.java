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
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.gzipfilter.org.tuckey.web.filters.urlrewrite.utils.StringUtils;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.opensymphony.util.TextUtils;
import com.opensymphony.webwork.ServletActionContext;
import org.apache.log4j.Logger;
import org.hivesoft.confluence.macros.survey.model.Survey;
import org.hivesoft.confluence.macros.vote.VoteMacro;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.macros.vote.model.Choice;
import org.hivesoft.confluence.macros.vote.model.Comment;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * <p>
 * This macro defines a simple survey mechanism against set of topics using the vote macro as its collection mechanism.
 * </p>
 */
public class SurveyMacro extends VoteMacro implements Macro {
    private static final Logger LOG = Logger.getLogger(SurveyMacro.class);

    private static final String KEY_SHOW_SUMMARY = "showSummary";


    private final static String[] defaultBallotLabels = new String[]{"5-Outstanding", "4-More Than Satisfactory", "3-Satisfactory", "2-Less Than Satisfactory", "1-Unsatisfactory"};
    private final static String[] defaultOldBallotLabels = new String[]{"5 - Outstanding", "4 - More Than Satisfactory", "3 - Satisfactory", "2 - Less Than Satisfactory", "1 - Unsatisfactory"};

    public SurveyMacro(PageManager pageManager, SpaceManager spaceManager, ContentPropertyManager contentPropertyManager, UserAccessor userAccessor, TemplateRenderer renderer, XhtmlContent xhtmlContent, PluginSettingsFactory pluginSettingsFactory) {
        super(pageManager, spaceManager, contentPropertyManager, userAccessor, renderer, xhtmlContent, pluginSettingsFactory);
    }

    private String[] ballotLabels = null; // 1.1.3 allow a self defined ballotLabels-List
    private ArrayList<String> myChoicesList = null;

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
    public String execute(Map<String, String> parameters, String body, ConversionContext context) throws MacroExecutionException {
        LOG.info("Try executing macro XHtml Style with body: " + body);
        try {
            return execute(parameters, body, (RenderContext) context.getPageContext());
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

        //TODO: think of some useful plugin settings

        // Create the survey model, 1.1.3 add the parameters map
        Survey survey = createSurvey(body, contentObject, (String) parameters.get("choices"));

        // 1.1.7.7 ballot title and choices too long will crash the system if exceeding 200 chars for entity_key. So check this on rendering
        String strExceedsKeyItems = "";
        for (String ballotChoiceKey : survey.getBallotTitlesWithChoiceNames()) {
            try {
                // 1.1.7.8 check for unicode-characters. They consume more space than they sometimes are allowed. add 5 to the calculated length (prefix for vote)
                if (ballotChoiceKey.getBytes("UTF-8").length + VOTE_PREFIX.length() > MAX_STORABLE_KEY_LENGTH) {
                    if (strExceedsKeyItems == "")
                        strExceedsKeyItems += ", ";
                    strExceedsKeyItems += ballotChoiceKey + " Length: " + (ballotChoiceKey.getBytes("UTF-8").length + VOTE_PREFIX.length());
                }
            } catch (java.io.UnsupportedEncodingException e) {
                throw new MacroException(e);
            }
        }
        if (strExceedsKeyItems != "") {
            final String message = "Error detected Length of BallotTitle and Choices are to long to be stored to the database (MaxLength:" + MAX_STORABLE_KEY_LENGTH + "). Problematic Names: " + strExceedsKeyItems + "!";
            LOG.error(message);
            throw new MacroException(message);
        }

        // Let the Survey have a Title (to have it more compact)
        String title = (String) parameters.get("title");
        if (!StringUtils.isBlank(title)) {
            survey.setTitle(title);
        }

        // If this macro is configured to allow users to change their vote, let the ballot know

        survey.setChangeableVotes(Boolean.parseBoolean((String) parameters.get(KEY_CHANGEABLE_VOTES)));

        // 1.1.7 Show Summary last
        Boolean bTopSummary = Boolean.TRUE;
        // if the user wishes to show the summary at the end
        String topSummary = (String) parameters.get("showTopSummary");
        if (topSummary != null) {
            bTopSummary = Boolean.valueOf(topSummary);
        }

        topSummary = (String) parameters.get("showLast");
        if (topSummary != null) {
            bTopSummary = !Boolean.valueOf(topSummary);
        }

        // 1.1.7.1: default with 5 options and a step 1 .. 1..5 (or ordered 5..1)
        int startBound = Ballot.DEFAULT_START_BOUND;
        String sTmpParam = (String) parameters.get("startBound");
        if (sTmpParam != null) {
            startBound = Integer.valueOf(sTmpParam).intValue();
        }
        int iterateStep = Ballot.DEFAULT_ITERATE_STEP;
        sTmpParam = (String) parameters.get("iterateStep");
        if (sTmpParam != null) {
            iterateStep = Integer.valueOf(sTmpParam).intValue();
        }
        // Hardcoded default is 1 and 1, if it is different, set it for each ballot, let default otherwise
        if (startBound != Ballot.DEFAULT_START_BOUND || iterateStep != Ballot.DEFAULT_ITERATE_STEP) {
            survey.setStartBoundAndIterateStep(startBound, iterateStep);
        }

        // Check if the macro has disabled the display of summary

        survey.setSummaryDisplay(Boolean.parseBoolean((String) parameters.get(KEY_SHOW_SUMMARY)));

        // check if any request parameters came in to vote on a ballot
        HttpServletRequest request = ServletActionContext.getRequest();
        String remoteUser = null;
        if (request != null) {
            remoteUser = request.getRemoteUser();

            // Try to retrieve the proper ballot
            Ballot ballot = survey.getBallot(request.getParameter("ballot.title"));

            // If there is a ballot found, cast the vote using our super's method
            if (ballot != null) {
                recordVote(ballot, request, contentObject, (String) parameters.get("voters"));
            }
        }

        String renderTitleLevel = "3";
        String surveyRenderTitleLevel = (String) parameters.get("renderTitleLevel");
        if (StringUtils.isBlank(surveyRenderTitleLevel)) {
            surveyRenderTitleLevel = "2";
            renderTitleLevel = "3";
        } else {
            if (Integer.valueOf(surveyRenderTitleLevel) == 0) {
                surveyRenderTitleLevel = "";
                renderTitleLevel = "";
            } else {
                renderTitleLevel = (Integer.valueOf(surveyRenderTitleLevel) + 1) + "";
            }
        }

        // now create a simple velocity context and render a template for the output
        Map<String, Object> contextMap = MacroUtils.defaultVelocityContext();
        contextMap.put("survey", survey);
        contextMap.put("content", contentObject);
        contextMap.put("macro", this);
        contextMap.put("showTopSummary", bTopSummary);
        contextMap.put("surveyRenderTitleLevel", surveyRenderTitleLevel);
        contextMap.put("renderTitleLevel", renderTitleLevel);
        // 1.1.8.1 somehow content.toPageContext isnt working anymore...
        contextMap.put("context", renderContext);

        // 1.1.4 add flag (default=true) for Showing Comments
        String showComments = (String) parameters.get("showComments");
        if (showComments != null) {
            contextMap.put("showComments", Boolean.valueOf(showComments));
        } else {
            contextMap.put("showComments", Boolean.valueOf(true));
        }

        // 1.1.5 add flag (default=false) for locking the Survey (no more
        // voting)
        String locked = (String) parameters.get("locked");
        if (locked != null) {
            contextMap.put("locked", Boolean.valueOf(locked));
        } else {
            contextMap.put("locked", Boolean.valueOf(false));
        }

        Boolean canSeeResults = Boolean.FALSE;
        // 1.1.4 Viewing is permitted by default for everyone
        // 1.1.5 the survey must be closed and viewers=empty (doesnt matter whether anonymous or not)
        // if viewers is not specifed and survey is locked
        if (!TextUtils.stringSet((String) parameters.get("viewers")) && Boolean.valueOf(locked).booleanValue()) {
            canSeeResults = Boolean.TRUE;
        } else {
            canSeeResults = getCanPerformAction((String) parameters.get("viewers"), remoteUser);
        }
        contextMap.put("canSeeSurveyResults", canSeeResults);

        Boolean canTakeSurvey = getCanPerformAction((String) parameters.get("voters"), remoteUser);
        contextMap.put("canTakeSurvey", canTakeSurvey);

        // 1.1.7.5 see voters
        Boolean canSeeVoters = getCanSeeVoters((String) parameters.get("visibleVoters"), canSeeResults);
        contextMap.put("canSeeSurveyVoters", canSeeVoters);

        // 1.1.7.5 if voters will be displayed, will they be rendered like
        // confluence-profile-links? (default)
        // globally valid in all vm's
        String votersWiki = (String) parameters.get("visibleVotersWiki");
        if (votersWiki != null) {
            contextMap.put("visibleVotersWiki", Boolean.valueOf(votersWiki));
        } else {
            contextMap.put("visibleVotersWiki", Boolean.valueOf(true));
        }
        // survey parameter must be passed through
        if (canSeeVoters == Boolean.TRUE) { // default is false, so only set if true
            survey.setVisibleVoters(true);
        }

        try {
            if (canSeeResults.booleanValue() || canTakeSurvey.booleanValue()) {
                return VelocityUtils.getRenderedTemplate("templates/extra/survey/surveymacro.vm", contextMap);
            }

            return VelocityUtils.getRenderedTemplate("templates/extra/survey/surveymacro-denied.vm", contextMap);
        } catch (Exception e) {
            LOG.error("Error while trying to display Ballot!", e);
            throw new MacroException(e);
        }
    }

    /**
     * <p>
     * Determine if a user is authorized to perform an action based on the provided list of names.
     * </p>
     *
     * @param permitted the list of usernames allowed to perform the action. If blank, everyone is permitted.
     * @param username  the username of the candidate user.
     * @return <code>true</code> if the user can see the results, <code>false</code> if they cannot.
     */
    protected Boolean getCanPerformAction(String permitted, String username) {
        // You aren't permitted if we don't know who you are
        if (!TextUtils.stringSet(username)) {
            return Boolean.FALSE;
        }

        // if it is not restricted you are granted
        if (!TextUtils.stringSet(permitted))
            return Boolean.TRUE;

        // if the logged in user matches a entry, it is granted
        if (Arrays.asList(permitted.split(",")).contains(username))
            return Boolean.TRUE;

        // 1.1.7.2: next try one of the entries is a group. Check whether the user is in this group!
        String[] lUsers = permitted.split(",");
        for (int ino = 0; ino < lUsers.length; ino++) {
            // guess the current element is a group /if (com.atlassian.confluence.user.DefaultUserAccessor.hasMembership(lUsers[ino], username))
            if (userAccessor.hasMembership(lUsers[ino], username))
                return Boolean.TRUE;
        }
        return Boolean.FALSE;

        // If you're not in the list or the list is not set, you aren't permitted /return Boolean.valueOf(!TextUtils.stringSet(permitted) ||
        // Arrays.asList(permitted.split(",")).contains(username));
    }

    /**
     * <p>
     * Create a survey object for the given macro body pre-populated with all choices that have previously been made by the users.
     * </p>
     *
     * @param body          The rendered body of the macro.
     * @param contentObject The content object from which the votes can be read.
     * @return The survey object, pre-filled with the correct data.
     */
    protected Survey createSurvey(String body, ContentEntityObject contentObject, String choices) {
        Survey survey = new Survey();

        if (!TextUtils.stringSet(body)) {
            return survey;
        }

        // 1.1.3: See what the choices-Parameter contains
        String tmpBallotLabels = choices;

        // Reconstruct all of the votes that have been cast so far
        for (StringTokenizer stringTokenizer = new StringTokenizer(body, "\r\n"); stringTokenizer.hasMoreTokens(); ) {
            String line = stringTokenizer.nextToken();

            // the parameter given list must override the inline Labels if none are there
            if (tmpBallotLabels != null) {
                ballotLabels = tmpBallotLabels.split(",");
            } else {
                ballotLabels = null; // null the ballotLabels, will be filled later
            }

            if (TextUtils.stringSet(line)) {
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
                    // 1.1.7.6 if this ballot is a default one, check whether
                    // there are old default items and convert
                    // see CSRVY-21 for details
                    // 1.1.8.2 override choices can now extend the
                    // defaultBallots (will then not compared starting by
                    // defaultb.length)
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

    /**
     * <p>
     * Helper method used by the velocity code since it can't determine the length of an array.
     * </p>
     *
     * @param array The array whose length is needed.
     * @return The length of the array as an Integer.
     */
    public Integer getArrayLength(Object array) {
        if (array == null) {
            return new Integer(0);
        }

        if (!array.getClass().isArray()) {
            throw new IllegalArgumentException(array.getClass().getName() + " is not an array.");
        }

        return new Integer(Array.getLength(array));
    }
}
