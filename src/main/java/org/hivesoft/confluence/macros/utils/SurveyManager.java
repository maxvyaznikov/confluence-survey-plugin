package org.hivesoft.confluence.macros.utils;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.extras.common.log.Logger;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.survey.model.Survey;
import org.hivesoft.confluence.macros.vote.VoteMacro;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.macros.vote.model.Choice;
import org.hivesoft.confluence.macros.vote.model.Comment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

public class SurveyManager {
  private static final Logger.Log LOG = Logger.getInstance(SurveyManager.class);

  private final static String[] defaultBallotLabels = new String[]{"5-Outstanding", "4-More Than Satisfactory", "3-Satisfactory", "2-Less Than Satisfactory", "1-Unsatisfactory"};
  private final static String[] defaultOldBallotLabels = new String[]{"5 - Outstanding", "4 - More Than Satisfactory", "3 - Satisfactory", "2 - Less Than Satisfactory", "1 - Unsatisfactory"};

  private final ContentPropertyManager contentPropertyManager;

  public SurveyManager(ContentPropertyManager contentPropertyManager) {
    this.contentPropertyManager = contentPropertyManager;
  }

  /**
   * This method will take the data from the macros parameters, body, and page data to reconstruct a ballot object with all of the choices and previously cast votes populated.
   * This method will probably only work from a VoteMacro context
   *
   * @param title The title of the ballot
   * @param body  The rendered body of the macro.
   * @return A fully populated ballot object.
   */
  public Ballot reconstructBallot(String title, String body, ContentEntityObject contentObject) {
    Ballot ballot = new Ballot(title);
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
    return ballot;
  }

  /**
   * Create a survey object for the given macro body pre-populated with all choices that have previously been made by the users.
   *
   * @param body    The rendered body of the macro.
   * @param choices The list of potential choices, or null for the default choices
   * @return The survey object, pre-filled with the correct data.
   */
  public Survey createSurvey(String body, ContentEntityObject contentObject, String choices) {
    Survey survey = new Survey();

    if (StringUtils.isBlank(body)) {
      return survey;
    }

    // Reconstruct all of the votes that have been cast so far
    for (StringTokenizer stringTokenizer = new StringTokenizer(body, "\r\n"); stringTokenizer.hasMoreTokens(); ) {
      String line = StringUtils.chomp(stringTokenizer.nextToken().trim());

      // the parameter given list must override the inline Labels if none are there
      String[] ballotLabels = null;
      if (choices != null) {
        ballotLabels = choices.split(",");
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

        int customChoice;
        ArrayList<String> myChoicesList = new ArrayList<String>();
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
          if (i < defaultBallotLabels.length && ballotLabels[i].equalsIgnoreCase(defaultBallotLabels[i])) {
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
   * This is a helper method to set the content property value for a particular vote choice once it has been updated.
   *
   * @param choice        The choice that has been updated.
   * @param ballotTitle   The title of the ballot that the choice belongs to.
   * @param contentObject The content object for the current macro.
   */
  public void setVoteContentProperty(Choice choice, String ballotTitle, ContentEntityObject contentObject) {
    String propertyKey = VoteMacro.VOTE_PREFIX + ballotTitle + "." + choice.getDescription();

    if (choice.getVoteCount() == 0) {
      contentPropertyManager.setTextProperty(contentObject, propertyKey, null);
    } else {
      Collection<String> voters = choice.getVoters();
      String propertyValue = StringUtils.join(voters, ",");
      // store property to text, for votes larger than usernames.length * votes > 255 chars
      contentPropertyManager.setTextProperty(contentObject, propertyKey, propertyValue);
    }
  }
}
