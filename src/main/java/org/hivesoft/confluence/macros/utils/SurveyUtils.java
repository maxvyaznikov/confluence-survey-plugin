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

import com.atlassian.extras.common.log.Logger;
import com.atlassian.renderer.v2.macro.MacroException;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.vote.VoteConfig;
import org.hivesoft.confluence.macros.vote.VoteMacro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SurveyUtils {
  private static final Logger.Log LOG = Logger.getInstance(SurveyUtils.class);

  private static final String REGEX_COMMA_SEPARATED_STRINGS = "\\s*,[,\\s]*";
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

  public static int getIntegerFromString(String stringToParse, int defaultValue) {
    if (StringUtils.defaultString(stringToParse).equals("")) {
      return defaultValue;
    } else {
      return Integer.valueOf(stringToParse);
    }
  }


  public static List<String> getListFromStringCommaSeparated(String stringToParse) {
    if (StringUtils.isBlank(stringToParse)) {
      return new ArrayList<String>();
    }
    return Arrays.asList(stringToParse.split(REGEX_COMMA_SEPARATED_STRINGS));
  }

  public static String getTitleInMacroParameters(Map<String, String> parameters) throws MacroException {
    String ballotTitle = StringUtils.defaultString(parameters.get(VoteConfig.KEY_TITLE)).trim();
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
}
