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
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.velocity.VelocityUtils;
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
import com.opensymphony.util.TextUtils;
import com.opensymphony.webwork.ServletActionContext;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.admin.AdminResource;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.macros.vote.model.Choice;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * This macro defines a simple voting mechanism against a particular topic. Users may only vote once (unless changeable votes is true), can only vote for one choice, and cannot see the overall results
 * until after they have voted.
 */
public class VoteMacro extends BaseMacro implements Macro {
    private static final Logger.Log LOG = Logger.getInstance(VoteMacro.class);

    private static final String VOTE_MACRO = "vote";

    public static final String REQUEST_PARAMETER_BALLOT = "ballot.title";
    public static final String REQUEST_PARAMETER_CHOICE = "vote.choice";

    protected static final String KEY_TITLE = "title";
    protected static final String KEY_RENDER_TITLE_LEVEL = "renderTitleLevel";
    protected static final String KEY_CHANGEABLE_VOTES = "changeableVotes";
    protected static final String KEY_VOTERS = "voters";
    protected static final String KEY_VIEWERS = "viewers";
    protected static final String KEY_VISIBLE_VOTERS = "visibleVoters";
    protected static final String KEY_VISIBLE_VOTERS_WIKI = "visibleVotersWiki";
    protected static final String KEY_LOCKED = "locked";

    // 1.1.7.7 define the max length that is storable to the propertyentry-key field
    protected static final int MAX_STORABLE_KEY_LENGTH = 200;

    // confluence 4 cant render dynamic content anymore so set wiki by default to false (was true in older confluence versions)
    protected static final boolean IS_VISIBLE_VOTERS_WIKI = false;

    // prefix vote to make a vote unique in the textproperties
    protected static final String VOTE_PREFIX = "vote.";


    protected final PluginSettingsFactory pluginSettingsFactory;
    protected final PageManager pageManager;
    protected final SpaceManager spaceManager;
    protected final ContentPropertyManager contentPropertyManager;
    protected final UserAccessor userAccessor;
    protected final UserManager userManager;
    protected final TemplateRenderer renderer;
    protected final XhtmlContent xhtmlContent;

    public VoteMacro(PageManager pageManager, SpaceManager spaceManager, ContentPropertyManager contentPropertyManager, UserAccessor userAccessor, UserManager userManager, TemplateRenderer renderer, XhtmlContent xhtmlContent, PluginSettingsFactory pluginSettingsFactory) {
        this.pageManager = pageManager;
        this.spaceManager = spaceManager;
        this.contentPropertyManager = contentPropertyManager;
        this.userAccessor = userAccessor;
        this.userManager = userManager;
        this.renderer = renderer;
        this.xhtmlContent = xhtmlContent;
        this.pluginSettingsFactory = pluginSettingsFactory;
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
     * Body should not be rendered, as we want it to control ourselfs
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
            final String voteMacroTitle = getTitle(parameters);
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
     * <p>
     * Get the HTML rendering of this macro.
     * </p>
     *
     * @param parameters    Any parameters passed into this macro. This macro expects is a single, unnamed parameter.
     * @param body          The rendered body of the macro
     * @param renderContext The render context for the current page rendering.
     * @return String respresenting the HTML rendering of this macro
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

        // Rebuild the model for this ballot
        @SuppressWarnings("unchecked")
        Ballot ballot = reconstructBallot(parameters, body, contentObject);

        final List<String> noneUniqueTitles = new ArrayList<String>();
        if (ballot.getChoices().size() == 0) {
            //throw new MacroException("The list of choices may not be empty. On which items do you want to vote on?");
            //don't render a error (that's not nice), render a warning list within velocity
        } else {
            for (Choice choice : ballot.getChoices()) {
                if (noneUniqueTitles.contains(choice.getDescription())) {
                    throw new MacroException("The choice-descriptions must be unique! The row starting with title of '" + choice.getDescription() + "' violated that. Please rename your choices to unique answers!");
                } else {
                    noneUniqueTitles.add(choice.getDescription());
                }
            }
        }

        // 1.1.7.7 ballot title and choices too long will crash the system if exceeding 200 chars for entity_key. So check this on rendering
        String strExceedsKeyItems = "";
        String strTemp = "";
        Collection<Choice> choices = ballot.getChoices();
        for (Choice choice : choices) {
            strTemp = ballot.getTitle() + "." + choice.getDescription();
            try {
                // 1.1.7.8 check for unicode-characters. They consume more space
                // than they sometimes are allowed.
                if (strTemp.getBytes("UTF-8").length + VOTE_PREFIX.length() > MAX_STORABLE_KEY_LENGTH) {
                    if (!strExceedsKeyItems.equals("")) {
                        strExceedsKeyItems += ", ";
                    }
                    strExceedsKeyItems += strTemp + " Length: " + (strTemp.getBytes("UTF-8").length + VOTE_PREFIX.length());
                }
            } catch (java.io.UnsupportedEncodingException e) {
                throw new MacroException(e);
            }
        }
        if (strExceedsKeyItems != "") {
            String logMessage = "Error detected Length of BallotTitle and Choices are to long to be stored to the database (MaxLength:" + MAX_STORABLE_KEY_LENGTH + "). Problematic Names: "
                    + strExceedsKeyItems + "!";
            LOG.error(logMessage);
            throw new MacroException(logMessage);
        }

        // check if any request parameters came in to complete or uncomplete tasks
        HttpServletRequest request = ServletActionContext.getRequest();

        final String remoteUsername = userManager.getRemoteUsername();
        if (request != null) {
            recordVote(ballot, request, contentObject, (String) parameters.get(KEY_VOTERS));
        }

        String renderTitleLevel = (String) parameters.get(KEY_RENDER_TITLE_LEVEL);
        if (StringUtils.isBlank(renderTitleLevel)) {
            renderTitleLevel = "3";
        } else {
            if (Integer.valueOf(renderTitleLevel) == 0) {
                renderTitleLevel = "";
            }
        }

        // now create a simple velocity context and render a template for the output
        Map<String, Object> contextMap = MacroUtils.defaultVelocityContext();
        contextMap.put("ballot", ballot);
        contextMap.put("content", contentObject);
        contextMap.put(KEY_RENDER_TITLE_LEVEL, renderTitleLevel);
        contextMap.put("iconSet", iconSet);
        // 1.1.8.1 somehow content.toPageContext isnt working anymore...
        contextMap.put("context", renderContext);

        // 1.1.5 add flag (default=false) for locking the Survey (no more voting)
        String locked = StringUtils.defaultString((String) parameters.get(KEY_LOCKED));
        contextMap.put(KEY_LOCKED, Boolean.valueOf(locked));

        // 1.1.5 if viewers not defined and vote is locked then he may see the result && !TextUtils.stringSet(remoteUser) doesnt matter
        Boolean canSeeResults = Boolean.FALSE;
        if (!TextUtils.stringSet((String) parameters.get(KEY_VIEWERS)) && Boolean.valueOf(locked).booleanValue()) {
            canSeeResults = Boolean.TRUE;
        } else {
            canSeeResults = getCanSeeResults((String) parameters.get(KEY_VIEWERS), (String) parameters.get(KEY_VOTERS), remoteUsername, ballot);
        }
        contextMap.put("canSeeResults", canSeeResults);

        // 1.1.7.5 can see voters (clear text of the usernames)
        Boolean canSeeVoters = getCanSeeVoters((String) parameters.get(KEY_VISIBLE_VOTERS), canSeeResults);
        contextMap.put("canSeeVoters", canSeeVoters);
        ballot.setVisibleVoters(canSeeVoters == Boolean.TRUE);

        // 1.1.7.5 if voters will be displayed, will they be rendered like
        // confluence-profile-links? (default)
        String votersWiki = (String) parameters.get(KEY_VISIBLE_VOTERS_WIKI);
        if (votersWiki != null) {
            contextMap.put(KEY_VISIBLE_VOTERS_WIKI, Boolean.valueOf(votersWiki));
        } else {
            contextMap.put(KEY_VISIBLE_VOTERS_WIKI, Boolean.valueOf(IS_VISIBLE_VOTERS_WIKI));
        }

        Boolean canVote = getCanVote((String) parameters.get(KEY_VOTERS), remoteUsername, ballot);
        contextMap.put("canVote", canVote);

        try {
            return VelocityUtils.getRenderedTemplate("templates/macros/vote/votemacro.vm", contextMap);
        } catch (Exception e) {
            LOG.error("Error while trying to display Ballot!", e);
            throw new MacroException(e);
        }
    }

    /**
     * This method will take the data from the macros parameters, body, and page data to reconstruct a ballot object with all of the choices and previously cast votes populated.
     *
     * @param parameters    The macro parameters.
     * @param body          The rendered body of the macro.
     * @param contentObject The page content object where cast votes are stored.
     * @return A fully populated ballot object.
     */
    protected Ballot reconstructBallot(Map<String, String> parameters, String body, ContentEntityObject contentObject) throws MacroException {
        Ballot ballot = new Ballot(getTitle(parameters));

        for (StringTokenizer stringTokenizer = new StringTokenizer(body, "\r\n"); stringTokenizer.hasMoreTokens(); ) {
            // 1.1.6: added trim(), otherwise it can happen that empty lines (spaces) get valid options
            String line = StringUtils.chomp(stringTokenizer.nextToken().trim());

            if (!StringUtils.isBlank(line) && line.length() > 0 && Character.getNumericValue(line.toCharArray()[0]) > -1) {
                Choice choice = new Choice(line);
                ballot.addChoice(choice);

                String votes = contentPropertyManager.getTextProperty(contentObject, VOTE_PREFIX + ballot.getTitle() + "." + line);

                if (TextUtils.stringSet(votes)) {

                    for (StringTokenizer voteTokenizer = new StringTokenizer(votes, ","); voteTokenizer.hasMoreTokens(); ) {
                        choice.voteFor(voteTokenizer.nextToken());
                    }
                }
            }
        }

        ballot.setChangeableVotes(Boolean.parseBoolean(parameters.get(KEY_CHANGEABLE_VOTES)));

        return ballot;
    }

    private String getTitle(Map<String, String> parameters) throws MacroException {
        String ballotTitle = StringUtils.defaultString(parameters.get(KEY_TITLE)).trim();
        if (StringUtils.isBlank(ballotTitle)) {
            ballotTitle = StringUtils.defaultString(parameters.get("0")).trim();
            if (StringUtils.isBlank(ballotTitle)) {
                // neither Parameter 0 is present nor title-Parameter could be found
                String logMessage = "Error: Please pass Parameter-0 or title-Argument (Required)!";
                LOG.error(logMessage);
                throw new MacroException(logMessage);
            }
        }
        return ballotTitle;
    }

    /**
     * This is a helper method to set the content property value for a particular vote choice once it has been updated.
     *
     * @param choice        The choice that has been updated.
     * @param ballotTitle   The title of the ballot that the choice belongs to.
     * @param contentObject The content object for the current macro.
     */
    protected void setVoteContentProperty(Choice choice, String ballotTitle, ContentEntityObject contentObject) {
        String propertyKey = VOTE_PREFIX + ballotTitle + "." + choice.getDescription();

        if (choice.getVoteCount() == 0) {
            contentPropertyManager.setTextProperty(contentObject, propertyKey, null);
        } else {
            Collection<String> voters = choice.getVoters();
            String propertyValue = StringUtils.join(voters, ",");
            // store property to text, for votes larger than usernames.length * votes > 255 chars
            contentPropertyManager.setTextProperty(contentObject, propertyKey, propertyValue);
        }
    }

    /**
     * <p>
     * Determine if a user is authorized to see the vote results. A user is only eligable to see results if they are specified as a viewer either implicitly (no viewers are specified) or explicitly
     * (the user is specified in the viewers parameter. A user who is specified as a viewer can see the results in two cases:
     * <ol>
     * <li>The user is not allowed to vote.</li>
     * <li>The user is allowed to vote and has already cast a vote.</li>
     * </ol>
     * </p>
     *
     * @param viewers  The list of usernames allowed to see the results after voting. If blank, all users can see results.
     * @param voters   The list of usernames allowed to vote. If blank, all users can vote.
     * @param username The username of the user about to see results.
     * @param ballot   The ballot whose results are about to be shown.
     * @return <code>true</code> if the user can see the results, <code>false</code> if they cannot.
     */
    protected Boolean getCanSeeResults(String viewers, String voters, String username, Ballot ballot) {
        // You can't see results if we don't know who you are
        if (StringUtils.isBlank(username)) {
            return Boolean.FALSE;
        }

        // If you're not a viewer, you can't see results
        boolean isViewer = StringUtils.isBlank(viewers) || Arrays.asList(viewers.split(",")).contains(username);
        if (!isViewer) {
            // 1.1.7.2: next try one of the entries is a group. Check whether the user is in this group!
            String[] lUsers = viewers.split(",");
            for (int ino = 0; ino < lUsers.length; ino++) {
                // guess the current element is a group /if (com.atlassian.confluence.user.DefaultUserAccessor.hasMembership(lUsers[ino], username))
                if (userAccessor.hasMembership(lUsers[ino], username)) {
                    isViewer = true;
                    ino = lUsers.length; // end the iteration
                }
            }
            if (!isViewer)
                return Boolean.FALSE;
        }

        // If you're a viewer but not a voter, then you can always see the results
        boolean isVoter = StringUtils.isBlank(voters) || Arrays.asList(voters.split(",")).contains(username);
        if (!isVoter) {
            // 1.1.7.2: next try one of the entries is a group. Check whether the user is in this group!
            String[] lUsers = voters.split(",");
            for (int ino = 0; ino < lUsers.length; ino++) {
                // guess the current element is a group /if (com.atlassian.confluence.user.DefaultUserAccessor.hasMembership(lUsers[ino], username))
                if (userAccessor.hasMembership(lUsers[ino], username)) {
                    isVoter = true;
                    ino = lUsers.length; // end the iteration
                }
            }
            if (!isVoter)
                return Boolean.TRUE;
        }

        // If you are a voter, then you have to vote to see the results
        return Boolean.valueOf(ballot.getHasVoted(username));
    }

    /*
     * 1.1.7.5
     */
    protected Boolean getCanSeeVoters(String visibleVoters, Boolean canSeeResults) {
        if (canSeeResults == Boolean.FALSE)
            return Boolean.FALSE;
        if (visibleVoters == null || !TextUtils.stringSet(visibleVoters))
            return Boolean.FALSE;
        if ("true".equals(visibleVoters))
            return Boolean.TRUE;
        return Boolean.FALSE;
    }

    /**
     * <p>
     * Determine if a user is authorized to cast a vote, taking into account whether they are a voter (either explicitly or implicitly) and whether or not they have already cast a vote. Only logged in
     * users can vote.
     * </p>
     *
     * @param voters   The list of usernames allowed to vote. If blank, all users can vote.
     * @param username the username of the user about to see the ballot.
     * @param ballot   the ballot that is about to be shown.
     * @return <code>true</code> if the user can cast a vote, <code>false</code> if they cannot.
     */
    protected Boolean getCanVote(String voters, String username, Ballot ballot) {
        if (!TextUtils.stringSet(username)) {
            return Boolean.FALSE;
        }

        boolean isVoter = !TextUtils.stringSet(voters) || Arrays.asList(voters.split(",")).contains(username);
        if (!isVoter) { // user is not permitted via username
            // 1.1.7.2: next try one of the entries is a group. Check whether the user is in this group!
            String[] lUsers = voters.split(",");
            for (int ino = 0; ino < lUsers.length; ino++) {
                // guess the current element is a group /if (com.atlassian.confluence.user.DefaultUserAccessor.hasMembership(lUsers[ino], username))
                if (userAccessor.hasMembership(lUsers[ino], username)) {
                    isVoter = true;
                    ino = lUsers.length; // end the iteration
                }
            }

            if (!isVoter) // user is not permitted via groupname either
                return Boolean.FALSE;
        }

        return Boolean.valueOf(!ballot.getHasVoted(username) || ballot.isChangeableVotes());
    }

    /**
     * <p>
     * If there is a vote in the request, store it in this page for the given ballot.
     * </p>
     *
     * @param ballot        The ballot being voted on.
     * @param request       The request where the vote and username can be found.
     * @param contentObject The content object where any votes should be stored.
     * @param voters        The list of usernames allowed to vote. If blank, all users can vote.
     */
    protected void recordVote(Ballot ballot, HttpServletRequest request, ContentEntityObject contentObject, String voters) {
        final String remoteUsername = userManager.getRemoteUsername();
        String requestBallotTitle = request.getParameter(REQUEST_PARAMETER_BALLOT);
        String requestChoice = request.getParameter(REQUEST_PARAMETER_CHOICE);

        // If there is a choice, make sure the vote is for this ballot and this user can vote
        if (requestChoice != null && ballot.getTitle().equals(requestBallotTitle) && getCanVote(voters, remoteUsername, ballot).booleanValue()) {

            // If this is a re-vote situation, then unvote first
            Choice previousChoice = ballot.getVote(remoteUsername);
            if (previousChoice != null && ballot.isChangeableVotes()) {
                previousChoice.removeVoteFor(remoteUsername);
                setVoteContentProperty(previousChoice, ballot.getTitle(), contentObject);
            }

            Choice choice = ballot.getChoice(requestChoice);

            if (choice != null) {
                if (!choice.equals(previousChoice)) {
                    choice.voteFor(remoteUsername);
                    setVoteContentProperty(choice, ballot.getTitle(), contentObject);
                }
            }
        }
    }
}
