/**
 * Copyright (c) 2006-2015, Confluence Community
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * <p/>
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hivesoft.confluence.macros.survey;

import org.hivesoft.confluence.macros.vote.VoteConfig;
import org.hivesoft.confluence.model.SurveySummary;
import org.hivesoft.confluence.utils.PermissionEvaluator;
import org.hivesoft.confluence.utils.SurveyUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurveyConfig extends VoteConfig {
  private static final String KEY_CHOICES = "choices";
  public static final String KEY_SHOW_SUMMARY = "showSummary";
  public static final String KEY_SHOW_LAST = "showLast";  // old key as showSummary was a boolean field

  private final SurveySummary surveySummary;
  private final List<String> choices;

  public SurveyConfig(PermissionEvaluator permissionEvaluator, Map<String, String> parameters) {
    super(permissionEvaluator, getModifiedSurveyParameters(parameters));
    choices = SurveyUtils.getListFromStringCommaSeparated(parameters.get(KEY_CHOICES));

    surveySummary = SurveySummary.getFor(parameters.get(KEY_SHOW_SUMMARY));
  }

  private static Map<String, String> getModifiedSurveyParameters(Map<String, String> parameters) {
    Map<String, String> modifiedParameters = new HashMap<String, String>(parameters);
    modifiedParameters.put(KEY_RENDER_TITLE_LEVEL, SurveyUtils.getIntegerFromString(parameters.get(KEY_RENDER_TITLE_LEVEL), 2) + "");
    modifiedParameters.put(KEY_SHOW_COMMENTS, SurveyUtils.getBooleanFromString(parameters.get(KEY_SHOW_COMMENTS), true) + "");
    return modifiedParameters;
  }

  public SurveySummary getSurveySummary() {
    return surveySummary;
  }

  public List<String> getChoices() {
    return choices;
  }

  /**
   * Velocity does not work really well with simple data types so provide a special method for it
   */
  public int getRenderTitleLevelAdjustedOrZero(int addSubLevel) {
    if (getRenderTitleLevel() == 0)
      return getRenderTitleLevel();
    return getRenderTitleLevel() + addSubLevel;
  }

  /**
   * migrate v.2.8.0 to v.2.8.1
   */
  public static Map<String, String> migrateParameters(Map<String, String> parameters) {
    Map<String, String> newParameters = new HashMap<String, String>(parameters);
    if (newParameters.containsKey(KEY_SHOW_LAST) || "false".equals(newParameters.get(KEY_SHOW_SUMMARY))) {
      SurveySummary surveySummary;
      if (!SurveyUtils.getBooleanFromString(newParameters.get(KEY_SHOW_SUMMARY), true)) {
        surveySummary = SurveySummary.None;
      } else {
        if (SurveyUtils.getBooleanFromString(newParameters.get(KEY_SHOW_LAST), false)) {
          surveySummary = SurveySummary.Bottom;
        } else {
          surveySummary = SurveySummary.Top;
        }
      }
      newParameters.remove(KEY_SHOW_LAST);
      newParameters.put(KEY_SHOW_SUMMARY, surveySummary.name());
    }
    return newParameters;
  }

  @Override
  public String toString() {
    return "SurveyConfig{" +
            "surveySummary=" + surveySummary +
            ", choices=" + choices +
            ", super=" + super.toString() +
            '}';
  }
}
