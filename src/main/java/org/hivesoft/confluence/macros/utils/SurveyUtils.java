package org.hivesoft.confluence.macros.utils;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.renderer.v2.macro.MacroException;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.survey.model.Survey;
import org.hivesoft.confluence.macros.vote.VoteMacro;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.macros.vote.model.Choice;
import org.hivesoft.confluence.macros.vote.model.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class SurveyUtils {
  private static final Logger.Log LOG = Logger.getInstance(SurveyUtils.class);

  private final static String[] defaultBallotLabels = new String[]{"5-Outstanding", "4-More Than Satisfactory", "3-Satisfactory", "2-Less Than Satisfactory", "1-Unsatisfactory"};
  private final static String[] defaultOldBallotLabels = new String[]{"5 - Outstanding", "4 - More Than Satisfactory", "3 - Satisfactory", "2 - Less Than Satisfactory", "1 - Unsatisfactory"};

  // 1.1.7.7 define the max length that is storable to the propertyEntry-key field
  protected static final int MAX_STORABLE_KEY_LENGTH = 200;

  public static void validateMaxStorableKeyLength(List<String> ballotAndChoiceNames) throws MacroException {
    // 1.1.7.7 ballot title and choices too long will crash the system if exceeding 200 chars for entity_key. So check this on rendering
    String strExceedsKeyItems = "";
    for (String ballotChoiceKey : ballotAndChoiceNames) {
      try {
        // 1.1.7.8 check for unicode-characters. They consume more space than they sometimes are allowed. add 5 to the calculated length (prefix for vote)
        if (ballotChoiceKey.getBytes("UTF-8").length + VoteMacro.VOTE_PREFIX.length() > MAX_STORABLE_KEY_LENGTH) {
          if (strExceedsKeyItems == "")
            strExceedsKeyItems += ", ";
          strExceedsKeyItems += ballotChoiceKey + " Length: " + (ballotChoiceKey.getBytes("UTF-8").length + VoteMacro.VOTE_PREFIX.length());
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
  }

  /**
   * Get the boolean value of a String and fallback to the defaultValue if its not a boolean
   */
  public static boolean getBooleanFromString(String stringToParse, boolean defaultValue) {
    if (StringUtils.defaultString(stringToParse).equals("")) {
      return defaultValue;
    } else {
      return Boolean.valueOf(stringToParse);
    }
  }

  /**
   * This method will take the data from the macros parameters, body, and page data to reconstruct a ballot object with all of the choices and previously cast votes populated.
   *
   * @param title         The title of the ballot
   * @param body          The rendered body of the macro.
   * @param contentObject The page content object where cast votes are stored.
   * @return A fully populated ballot object.
   */
  public static Ballot reconstructBallot(String title, String body, ContentEntityObject contentObject, ContentPropertyManager contentPropertyManager) {
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

  public static String getTitleInMacroParameters(Map<String, String> parameters) throws MacroException {
    String ballotTitle = StringUtils.defaultString(parameters.get(VoteMacro.KEY_TITLE)).trim();
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
   * Create a survey object for the given macro body pre-populated with all choices that have previously been made by the users.
   *
   * @param body          The rendered body of the macro.
   * @param contentObject The content object from which the votes can be read.
   * @return The survey object, pre-filled with the correct data.
   */
  public static Survey createSurvey(String body, ContentEntityObject contentObject, String choices, ContentPropertyManager contentPropertyManager) {
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
}
