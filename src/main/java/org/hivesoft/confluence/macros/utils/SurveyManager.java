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
package org.hivesoft.confluence.macros.utils;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.extras.common.log.Logger;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.macros.survey.model.Survey;
import org.hivesoft.confluence.macros.vote.VoteConfig;
import org.hivesoft.confluence.macros.vote.VoteMacro;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.macros.vote.model.Choice;
import org.hivesoft.confluence.macros.vote.model.Comment;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class SurveyManager {
  private static final Logger.Log LOG = Logger.getInstance(SurveyManager.class);

  private final static int SURVEY_BALLOT_INDEX_TITLE = 0;
  private final static int SURVEY_BALLOT_INDEX_SUB_TITLE = 1;
  private final static int SURVEY_BALLOT_INDEX_START_INLINE_CHOICES = 2;

  private final static int MINIMUM_CHOICES_COUNT = 2;
  protected static final char COMMENTERS_SEPARATOR = '|';

  private final ContentPropertyManager contentPropertyManager;
  private final PermissionEvaluator permissionEvaluator;

  public SurveyManager(ContentPropertyManager contentPropertyManager, PermissionEvaluator permissionEvaluator) {
    this.contentPropertyManager = contentPropertyManager;
    this.permissionEvaluator = permissionEvaluator;
  }

  public PermissionEvaluator getPermissionEvaluator() {
    return permissionEvaluator;
  }

  /**
   * This method will take the data from the macros parameters, body, and page data to reconstruct a ballot object with all of the choices and previously cast votes populated.
   * This method will probably only work from a VoteMacro context
   */
  public Ballot reconstructBallotFromPlainTextMacroBody(Map<String, String> parameters, String plainTextMacroBody, ContentEntityObject contentObject) {
    final String ballotTitle = SurveyUtils.getTitleInMacroParameters(parameters);
    List<Choice> choices = new ArrayList<Choice>();

    for (StringTokenizer stringTokenizer = new StringTokenizer(plainTextMacroBody, "\r\n"); stringTokenizer.hasMoreTokens(); ) {
      String line = StringUtils.chomp(stringTokenizer.nextToken().trim());

      if (!StringUtils.isBlank(line) && ((line.length() == 1 && Character.getNumericValue(line.toCharArray()[0]) > -1) || line.length() > 1)) {
        Choice choice = new Choice(line);

        String votes = contentPropertyManager.getTextProperty(contentObject, VoteMacro.VOTE_PREFIX + ballotTitle + "." + line);

        if (StringUtils.isNotBlank(votes)) {
          for (StringTokenizer voteTokenizer = new StringTokenizer(votes, ","); voteTokenizer.hasMoreTokens(); ) {
            choice.voteFor(voteTokenizer.nextToken());
          }
        }

        choices.add(choice);
      }
    }
    final List<Comment> comments = loadCommentsForBallot(contentObject, ballotTitle);

    return new Ballot(ballotTitle, "", new VoteConfig(permissionEvaluator, parameters), choices, comments);
  }

  /**
   * Create a survey object for the given macro body pre-populated with all choices that have previously been made by the users.
   */
  public Survey reconstructSurveyFromPlainTextMacroBody(String plainTextMacroBody, ContentEntityObject contentObject, Map<String, String> parameters) {
    Survey survey = new Survey(new SurveyConfig(permissionEvaluator, parameters));

    survey.setTitle(SurveyUtils.getTitleInMacroParameters(parameters));

    if (StringUtils.isBlank(plainTextMacroBody)) {
      return survey;
    }

    for (StringTokenizer stringTokenizer = new StringTokenizer(plainTextMacroBody, "\r\n"); stringTokenizer.hasMoreTokens(); ) {
      String line = StringUtils.chomp(stringTokenizer.nextToken().trim());

      if ((!StringUtils.isBlank(line) && Character.getNumericValue(line.toCharArray()[0]) > -1) || line.length() > 1) {
        Ballot ballot = reconstructBallotFromSurveyRow(contentObject, survey, line.split("\\-", -1));
        survey.addBallot(ballot);
      }
    }

    return survey;
  }

  private Ballot reconstructBallotFromSurveyRow(ContentEntityObject contentObject, Survey survey, String[] lineElements) {
    final String ballotTitle = lineElements[SURVEY_BALLOT_INDEX_TITLE].trim();
    final VoteConfig config = new VoteConfig(survey.getConfig());
    final ArrayList<Choice> choices = new ArrayList<Choice>();

    List<String> inlineChoiceNames = new ArrayList<String>();
    if (!config.isShowCondensed()) { // only count inline elements if its not condensed!
      for (int customChoice = SURVEY_BALLOT_INDEX_START_INLINE_CHOICES; customChoice < lineElements.length; customChoice++) {
        inlineChoiceNames.add(lineElements[customChoice].trim());
      }
    }

    List<String> choiceNames = survey.getConfig().getChoices();
    if (inlineChoiceNames.size() >= MINIMUM_CHOICES_COUNT) {
      choiceNames = inlineChoiceNames;
    } else if (choiceNames.isEmpty()) {
      choiceNames = SurveyUtils.DEFAULT_CHOICE_NAMES;
    }

    for (String choiceName : choiceNames) {
      Choice choice = new Choice(choiceName);

      migrateOldDefaultVotesIfPresent(contentObject, ballotTitle, choiceName);

      String votes = contentPropertyManager.getTextProperty(contentObject, VoteMacro.VOTE_PREFIX + ballotTitle + "." + choice.getDescription());
      if (StringUtils.isNotBlank(votes)) {
        for (StringTokenizer voteTokenizer = new StringTokenizer(votes, ","); voteTokenizer.hasMoreTokens(); ) {
          choice.voteFor(voteTokenizer.nextToken());
        }
      }
      choices.add(choice);
    }

    final List<Comment> comments = loadCommentsForBallot(contentObject, ballotTitle);

    String description = "";
    if (lineElements.length > SURVEY_BALLOT_INDEX_SUB_TITLE) {
      description = lineElements[SURVEY_BALLOT_INDEX_SUB_TITLE].trim();
    }

    return new Ballot(ballotTitle, description, config, choices, comments);
  }

  /**
   * if this ballot is a default one, check whether there are old default items and convert see CSRVY-21 for details
   */
  private void migrateOldDefaultVotesIfPresent(ContentEntityObject contentObject, String ballotTitle, String choiceName) {
    if (SurveyUtils.DEFAULT_CHOICE_NAMES.contains(choiceName)) {
      int defaultIndex = SurveyUtils.DEFAULT_CHOICE_NAMES.indexOf(choiceName);

      final String oldVotes = contentPropertyManager.getTextProperty(contentObject, VoteMacro.VOTE_PREFIX + ballotTitle + "." + SurveyUtils.DEFAULT_OLD_CHOICE_NAMES.get(defaultIndex));
      if (StringUtils.isNotBlank(oldVotes)) {
        contentPropertyManager.setTextProperty(contentObject, VoteMacro.VOTE_PREFIX + ballotTitle + "." + SurveyUtils.DEFAULT_CHOICE_NAMES.get(defaultIndex), oldVotes);
        contentPropertyManager.setTextProperty(contentObject, VoteMacro.VOTE_PREFIX + ballotTitle + "." + SurveyUtils.DEFAULT_OLD_CHOICE_NAMES.get(defaultIndex), null);
      }
    }
  }

  private List<Comment> loadCommentsForBallot(ContentEntityObject contentObject, String ballotTitle) {
    List<Comment> comments = new ArrayList<Comment>();
    final String commenters = contentPropertyManager.getTextProperty(contentObject, "survey." + ballotTitle + ".commenters");

    if (StringUtils.isNotBlank(commenters)) {
      for (String commenter : StringUtils.split(commenters, COMMENTERS_SEPARATOR)) {
        String comment = contentPropertyManager.getTextProperty(contentObject, "survey." + ballotTitle + ".comment." + commenter);
        comments.add(new Comment(commenter, comment));
      }
    }
    return comments;
  }

  public void storeVotersForChoice(Choice choice, String ballotTitle, ContentEntityObject contentObject) {
    String propertyKey = VoteMacro.VOTE_PREFIX + ballotTitle + "." + choice.getDescription();

    if (choice.getVoters().size() == 0) {
      contentPropertyManager.setTextProperty(contentObject, propertyKey, null);
    } else {
      Collection<String> voters = choice.getVoters();
      String propertyValue = StringUtils.join(voters, ",");
      contentPropertyManager.setTextProperty(contentObject, propertyKey, propertyValue);
    }
  }

  @Deprecated //for implicit voting on page queries
  public void recordVote(Ballot ballot, HttpServletRequest request, ContentEntityObject contentObject) {
    String requestBallotTitle = request.getParameter(VoteMacro.REQUEST_PARAMETER_BALLOT);
    String requestChoice = request.getParameter(VoteMacro.REQUEST_PARAMETER_CHOICE);
    String requestVoteAction = request.getParameter(VoteMacro.REQUEST_PARAMETER_VOTE_ACTION);

    recordVote(ballot, contentObject, requestBallotTitle, requestChoice, requestVoteAction);
  }

  public void recordVote(Ballot ballot, ContentEntityObject contentObject, String requestBallotTitle, String requestChoice, String requestVoteAction) {
    LOG.debug("recordVote: found Ballot-Title=" + requestBallotTitle + ", choice=" + requestChoice + ", action=" + requestVoteAction);
    final String remoteUsername = permissionEvaluator.getRemoteUsername();

    // If there is a choice, make sure the vote is for this ballot and this user can vote
    if (requestChoice != null && ballot.getTitle().equals(requestBallotTitle) && permissionEvaluator.getCanVote(remoteUsername, ballot)) {

      // If this is a re-vote situation, then unvote first
      Choice previousChoice = ballot.getChoiceForUserName(remoteUsername);
      if (previousChoice != null && ballot.getConfig().isChangeableVotes()) {
        previousChoice.removeVoteFor(remoteUsername);
        storeVotersForChoice(previousChoice, ballot.getTitle(), contentObject);
      }

      Choice choice = ballot.getChoice(requestChoice);

      if (choice != null && "vote".equalsIgnoreCase(requestVoteAction)) {
        LOG.debug("recordVote: found choice in requestChoice: " + choice.getDescription());
        choice.voteFor(remoteUsername);
        storeVotersForChoice(choice, ballot.getTitle(), contentObject);
      }
    }
  }

  /**
   * Make sure the contentEntityObject is of type Page, as we need a actual Page to store/retrieve data
   */
  public ContentEntityObject getPageEntityFromConversionContext(ConversionContext conversionContext) {
    ContentEntityObject contentObject = conversionContext.getEntity();

    if (contentObject instanceof com.atlassian.confluence.pages.Comment) {
      return ((com.atlassian.confluence.pages.Comment) contentObject).getOwner();
    }

    return contentObject;
  }
}
