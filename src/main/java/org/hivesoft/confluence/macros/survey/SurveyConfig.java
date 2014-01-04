package org.hivesoft.confluence.macros.survey;

import org.hivesoft.confluence.macros.survey.model.SurveySummary;
import org.hivesoft.confluence.macros.utils.PermissionEvaluator;
import org.hivesoft.confluence.macros.utils.SurveyUtils;
import org.hivesoft.confluence.macros.vote.VoteConfig;

import java.util.List;
import java.util.Map;

public class SurveyConfig extends VoteConfig {
  public static final String KEY_CHOICES = "choices";
  public static final String KEY_SHOW_SUMMARY = "showSummary";
  public static final String KEY_SHOW_LAST = "showLast";
  public static final String KEY_START_BOUND = "startBound";
  public static final String KEY_ITERATE_STEP = "iterateStep";

  private SurveySummary surveySummary = SurveySummary.Top;
  private List<String> choices;

  public SurveyConfig(PermissionEvaluator permissionEvaluator, Map<String, String> parameters) {
    super(permissionEvaluator, parameters);

    choices = SurveyUtils.getListFromStringCommaSeparated((String) parameters.get(KEY_CHOICES));

    if (!SurveyUtils.getBooleanFromString((String) parameters.get(KEY_SHOW_SUMMARY), true)) {
      surveySummary = SurveySummary.None;
    } else {
      if (SurveyUtils.getBooleanFromString((String) parameters.get(KEY_SHOW_LAST), false)) {
        surveySummary = SurveySummary.Bottom;
      }
    }

    int startBound = DEFAULT_START_BOUND;
    String sTmpParam = (String) parameters.get(KEY_START_BOUND);
    if (sTmpParam != null) {
      startBound = Integer.valueOf(sTmpParam);
    }
    int iterateStep = DEFAULT_ITERATE_STEP;
    sTmpParam = (String) parameters.get(KEY_ITERATE_STEP);
    if (sTmpParam != null) {
      iterateStep = Integer.valueOf(sTmpParam);
    }
    if (startBound != DEFAULT_START_BOUND || iterateStep != DEFAULT_ITERATE_STEP) {
      this.startBound = startBound;
      this.iterateStep = iterateStep;
    }
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
}
