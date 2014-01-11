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

  private SurveySummary surveySummary = SurveySummary.Top;
  private List<String> choices;

  public SurveyConfig(PermissionEvaluator permissionEvaluator, Map<String, String> parameters) {
    super(permissionEvaluator, parameters);
    //start: parse again some parameters which are different on survey than on vote
    renderTitleLevel = SurveyUtils.getIntegerFromString((String) parameters.get(KEY_RENDER_TITLE_LEVEL), 2);
    showComments = SurveyUtils.getBooleanFromString((String) parameters.get(KEY_SHOW_COMMENTS), true);
    //end
    choices = SurveyUtils.getListFromStringCommaSeparated((String) parameters.get(KEY_CHOICES));

    if (!SurveyUtils.getBooleanFromString((String) parameters.get(KEY_SHOW_SUMMARY), true)) {
      surveySummary = SurveySummary.None;
    } else {
      if (SurveyUtils.getBooleanFromString((String) parameters.get(KEY_SHOW_LAST), false)) {
        surveySummary = SurveySummary.Bottom;
      }
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

  @Override
  public String toString() {
    return "SurveyConfig{" +
            "surveySummary=" + surveySummary +
            ", choices=" + choices +
            ", super=" + super.toString() +
            '}';
  }
}
