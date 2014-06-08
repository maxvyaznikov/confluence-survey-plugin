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

import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.vote.VoteConfig;
import org.hivesoft.confluence.macros.vote.VoteMacro;
import org.hivesoft.confluence.macros.vote.model.Choice;
import org.hivesoft.confluence.rest.AdminResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SurveyUtils {
  final static List<String> DEFAULT_OLD_CHOICE_NAMES = new ArrayList<String>(Arrays.asList("5 - Outstanding", "4 - More Than Satisfactory", "3 - Satisfactory", "2 - Less Than Satisfactory", "1 - Unsatisfactory"));
  final static List<String> DEFAULT_CHOICE_NAMES = new ArrayList<String>(Arrays.asList("5-Outstanding", "4-More Than Satisfactory", "3-Satisfactory", "2-Less Than Satisfactory", "1-Unsatisfactory"));

  private static final String REGEX_COMMA_SEPARATED_STRINGS = "\\s*,[,\\s]*";
  protected static final int MAX_STORABLE_KEY_LENGTH = 200;

  private SurveyUtils() {
  }

  /**
   * ballot title and choices must no exceed 200 chars for their entity_key
   * check for unicode-characters. They consume more space than they sometimes are allowed. add 5 to the calculated length (prefix for vote)
   */
  public static List<String> getViolatingMaxStorableKeyLengthItems(List<String> ballotAndChoiceNames) throws MacroExecutionException {
    List<String> exceedingKeyItems = new ArrayList<String>();
    for (String ballotChoiceKey : ballotAndChoiceNames) {
      try {
        if (ballotChoiceKey.getBytes("UTF-8").length + VoteMacro.VOTE_PREFIX.length() > MAX_STORABLE_KEY_LENGTH) {
          exceedingKeyItems.add("Choice to long: " + ballotChoiceKey + " Length: " + (ballotChoiceKey.getBytes("UTF-8").length + VoteMacro.VOTE_PREFIX.length() + " (allowed: " + MAX_STORABLE_KEY_LENGTH + ")"));
        }
      } catch (java.io.UnsupportedEncodingException e) {
        throw new MacroExecutionException(e);
      }
    }
    return exceedingKeyItems;
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

  public static String getTitleInMacroParameters(Map<String, String> parameters) {
    String ballotTitle = StringUtils.defaultString(parameters.get(VoteConfig.KEY_TITLE)).trim();
    if (StringUtils.isBlank(ballotTitle)) {
      //in case of the vote macro there was the possibility to pass the title anonymously
      ballotTitle = StringUtils.defaultString(parameters.get("0")).trim();
    }
    return ballotTitle;
  }

  public static String getIconSetFromPluginSettings(PluginSettingsFactory pluginSettingsFactory) {
    PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
    String iconSet = (String) settings.get(AdminResource.SURVEY_PLUGIN_KEY_ICON_SET);
    if (StringUtils.isBlank(iconSet)) {
      iconSet = AdminResource.SURVEY_PLUGIN_ICON_SET_DEFAULT;
    }
    return iconSet;
  }

  public static List<Choice> getDefaultChoices() {
    List<Choice> choices = new ArrayList<Choice>();
    for (String choiceName : DEFAULT_CHOICE_NAMES) {
      choices.add(new Choice(choiceName));
    }
    return choices;
  }

  public static String enrichStringWithHttpPattern(String stringToEnrich) {
    final Pattern urlPattern = Pattern.compile("(https?://[\\da-z\\.-]+\\.[a-z\\.]{2,6}[/\\w\\.-;=]*/?\\??[a-z0-9=&]*)");
    final Matcher matcher = urlPattern.matcher(stringToEnrich);

    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(result, "<a href=\"" + matcher.group() + "\">" + matcher.group() + "</a>");
    }
    matcher.appendTail(result);
    return result.toString();
  }
}
