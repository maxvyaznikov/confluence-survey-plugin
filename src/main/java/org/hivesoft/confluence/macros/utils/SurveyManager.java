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
package org.hivesoft.confluence.macros.utils;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.renderer.v2.macro.MacroException;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.macros.survey.model.Survey;
import org.hivesoft.confluence.macros.vote.VoteConfig;
import org.hivesoft.confluence.macros.vote.VoteMacro;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.macros.vote.model.Choice;
import org.hivesoft.confluence.macros.vote.model.Comment;

import java.util.*;

public class SurveyManager {
  private static final Logger.Log LOG = Logger.getInstance(SurveyManager.class);

  private final static List<String> defaultBallotLabels = new ArrayList<String>(Arrays.asList("5-Outstanding", "4-More Than Satisfactory", "3-Satisfactory", "2-Less Than Satisfactory", "1-Unsatisfactory"));
  private final static List<String> defaultOldBallotLabels = new ArrayList<String>(Arrays.asList("5 - Outstanding", "4 - More Than Satisfactory", "3 - Satisfactory", "2 - Less Than Satisfactory", "1 - Unsatisfactory"));

  private final ContentPropertyManager contentPropertyManager;
  private final PermissionEvaluator permissionEvaluator;

  public SurveyManager(ContentPropertyManager contentPropertyManager, PermissionEvaluator permissionEvaluator) {
    this.contentPropertyManager = contentPropertyManager;
    this.permissionEvaluator = permissionEvaluator;
  }

  /**
   * This method will take the data from the macros parameters, body, and page data to reconstruct a ballot object with all of the choices and previously cast votes populated.
   * This method will probably only work from a VoteMacro context
   *
   * @param body The rendered body of the macro.
   * @return A fully populated ballot object.
   */
  public Ballot reconstructBallot(Map<String, String> parameters, String body, ContentEntityObject contentObject) throws MacroException {
    Ballot ballot = new Ballot(SurveyUtils.getTitleInMacroParameters(parameters), new VoteConfig(permissionEvaluator, parameters));
    for (StringTokenizer stringTokenizer = new StringTokenizer(body, "\r\n"); stringTokenizer.hasMoreTokens(); ) {
      String line = StringUtils.chomp(stringTokenizer.nextToken().trim());

      if (!StringUtils.isBlank(line) && ((line.length() == 1 && Character.getNumericValue(line.toCharArray()[0]) > -1) || line.length() > 1)) {
        Choice choice = new Choice(line);
        ballot.addChoice(choice);

        String votes = contentPropertyManager.getTextProperty(contentObject, VoteMacro.VOTE_PREFIX + ballot.getTitle() + "." + line);

        if (TextUtils.stringSet(votes)) {
          for (StringTokenizer voteTokenizer = new StringTokenizer(votes, ","); voteTokenizer.hasMoreTokens(); ) {
            choice.voteFor(voteTokenizer.nextToken());
          }
        }
      }
    }

    enrichBallotWithComments(contentObject, ballot);

    return ballot;
  }

  /**
   * Create a survey object for the given macro body pre-populated with all choices that have previously been made by the users.
   *
   * @param body The rendered body of the macro.
   * @return The survey object, pre-filled with the correct data.
   */
  public Survey createSurvey(String body, ContentEntityObject contentObject, Map<String, String> parameters) {
    Survey survey = new Survey(new SurveyConfig(permissionEvaluator, parameters));

    try {
      survey.setTitle(SurveyUtils.getTitleInMacroParameters(parameters));
    } catch (MacroException e) {
      //title of survey is currently NOT mandatory
    }

    if (StringUtils.isBlank(body)) {
      return survey;
    }

    // Reconstruct all of the votes that have been cast so far
    for (StringTokenizer stringTokenizer = new StringTokenizer(body, "\r\n"); stringTokenizer.hasMoreTokens(); ) {
      String line = StringUtils.chomp(stringTokenizer.nextToken().trim());

      // the parameter given list must override the inline Labels if none are there
      List<String> ballotLabels = survey.getConfig().getChoices();

      if ((!StringUtils.isBlank(line) && Character.getNumericValue(line.toCharArray()[0]) > -1) || line.length() > 1) {
        String[] lineElements = line.split("\\-", -1);

        Ballot ballot = new Ballot(lineElements[0].trim(), new VoteConfig(survey.getConfig()));
        survey.addBallot(ballot);

        //second element is the subtitle or description
        if (lineElements.length > 1) {
          ballot.setDescription(lineElements[1].trim());
        }

        enrichBallotWithComments(contentObject, ballot);

        int customChoice;
        ArrayList<String> myChoicesList = new ArrayList<String>();
        for (customChoice = 2; customChoice < lineElements.length; customChoice++) {
          String temp = lineElements[customChoice].trim();
          myChoicesList.add(temp);
        }

        // 1.1.3: first take the list inLine
        if (myChoicesList.size() > 1) { // should be a minimum of 2 choices
          ballotLabels = myChoicesList;
        } else {
          if (ballotLabels.isEmpty()) { // second was there no parameterList?
            ballotLabels = defaultBallotLabels;
          }
          // 3rd if there was a parameterList it will be in ballotLabels by default
        }

        // Load all of the choices and votes into the ballot
        for (String ballotLabel : ballotLabels) {
          Choice choice = new Choice(ballotLabel);
          // 1.1.7.6 if this ballot is a default one, check whether there are old default items and convert see CSRVY-21 for details
          if (defaultBallotLabels.contains(ballotLabel)) {
            int defaultIndex = defaultBallotLabels.indexOf(ballotLabel);
            // check for old votes
            String votes = contentPropertyManager.getTextProperty(contentObject, "vote." + ballot.getTitle() + "." + defaultOldBallotLabels.get(defaultIndex));
            if (TextUtils.stringSet(votes)) {
              // if present save the new (spaces reduced one)
              contentPropertyManager.setTextProperty(contentObject, "vote." + ballot.getTitle() + "." + defaultBallotLabels.get(defaultIndex), votes);
              // delete the old key
              contentPropertyManager.setTextProperty(contentObject, "vote." + ballot.getTitle() + "." + defaultOldBallotLabels.get(defaultIndex), null);
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

  private void enrichBallotWithComments(ContentEntityObject contentObject, final Ballot ballot) {
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
  }

  /**
   * This is a helper method to set the content property value for a particular vote choice once it has been updated.
   *
   * @param choice        The choice that has been updated.
   * @param ballotTitle   The title of the ballot that the choice belongs to.
   * @param contentObject The content object for the current macro.
   */
  public void setVoteContentProperty(Choice choice, String ballotTitle, ContentEntityObject contentObject) {
    String propertyKey = VoteMacro.VOTE_PREFIX + ballotTitle + "." + choice.getDescription();

    if (choice.getVoters().size() == 0) {
      contentPropertyManager.setTextProperty(contentObject, propertyKey, null);
    } else {
      Collection<String> voters = choice.getVoters();
      String propertyValue = StringUtils.join(voters, ",");
      // store property to text, for votes larger than usernames.length * votes > 255 chars
      contentPropertyManager.setTextProperty(contentObject, propertyKey, propertyValue);
    }
  }
}
