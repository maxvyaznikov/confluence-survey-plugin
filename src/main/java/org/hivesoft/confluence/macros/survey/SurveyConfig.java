package org.hivesoft.confluence.macros.survey;

import org.hivesoft.confluence.macros.survey.model.SurveySummary;
import org.hivesoft.confluence.macros.utils.PermissionEvaluator;
import org.hivesoft.confluence.macros.utils.SurveyUtils;
import org.hivesoft.confluence.macros.vote.VoteConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurveyConfig extends VoteConfig {
  public static final String KEY_CHOICES = "choices";
  public static final String KEY_SHOW_SUMMARY = "showSummary";
  public static final String KEY_SHOW_LAST = "showLast";  // old key as showSummary was a boolean field

  private SurveySummary surveySummary;
  private List<String> choices;

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
