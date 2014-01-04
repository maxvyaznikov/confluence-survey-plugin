package org.hivesoft.confluence.macros.vote;

import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.macros.utils.PermissionEvaluator;
import org.hivesoft.confluence.macros.utils.SurveyUtils;

import java.util.List;
import java.util.Map;

public class VoteConfig {
  public static final int DEFAULT_START_BOUND = 1;
  public static final int DEFAULT_ITERATE_STEP = 1;

  public static final String KEY_TITLE = "title";
  public static final String KEY_RENDER_TITLE_LEVEL = "renderTitleLevel";
  public static final String KEY_CHANGEABLE_VOTES = "changeableVotes";
  public static final String KEY_VOTERS = "voters";
  private static final String KEY_VIEWERS = "viewers";
  private static final String KEY_SHOW_COMMENTS = "showComments";
  private static final String KEY_VISIBLE_VOTERS = "visibleVoters";
  private static final String KEY_VISIBLE_VOTERS_WIKI = "visibleVotersWiki";
  private static final String KEY_LOCKED = "locked";
  public static final String KEY_START_BOUND = "startBound";
  public static final String KEY_ITERATE_STEP = "iterateStep";

  private int renderTitleLevel;
  private boolean changeableVotes;
  private List<String> voters;
  private List<String> viewers;
  private boolean showComments;
  private boolean locked;
  private boolean visibleVoters;
  private boolean visibleVotersWiki;

  private boolean canSeeResults;
  private boolean canTakeSurvey;

  protected int startBound = DEFAULT_START_BOUND;
  protected int iterateStep = DEFAULT_ITERATE_STEP;

  public VoteConfig(PermissionEvaluator permissionEvaluator, Map<String, String> parameters) {
    renderTitleLevel = SurveyUtils.getIntegerFromString((String) parameters.get(KEY_RENDER_TITLE_LEVEL), 3);
    changeableVotes = Boolean.parseBoolean((String) parameters.get(KEY_CHANGEABLE_VOTES));
    voters = SurveyUtils.getListFromStringCommaSeparated(StringUtils.defaultString((String) parameters.get(KEY_VOTERS)));
    viewers = SurveyUtils.getListFromStringCommaSeparated(StringUtils.defaultString((String) parameters.get(KEY_VIEWERS)));
    showComments = SurveyUtils.getBooleanFromString((String) parameters.get(KEY_SHOW_COMMENTS), false);
    locked = SurveyUtils.getBooleanFromString((String) parameters.get(KEY_LOCKED), false);

    final String remoteUsername = permissionEvaluator.getRemoteUsername();

    if (viewers.isEmpty() && locked) {
      canSeeResults = true;
    } else {
      canSeeResults = permissionEvaluator.isPermissionListEmptyOrContainsGivenUser(viewers, remoteUsername);
    }

    canTakeSurvey = permissionEvaluator.isPermissionListEmptyOrContainsGivenUser(voters, remoteUsername);
    visibleVoters = permissionEvaluator.getCanSeeVoters((String) parameters.get(KEY_VISIBLE_VOTERS), canSeeResults);
    visibleVotersWiki = SurveyUtils.getBooleanFromString((String) parameters.get(KEY_VISIBLE_VOTERS_WIKI), false);

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

  public VoteConfig(SurveyConfig surveyConfig) {
    renderTitleLevel = surveyConfig.getRenderTitleLevel();
    if (renderTitleLevel > 0) {
      renderTitleLevel++;
    }

    changeableVotes = surveyConfig.isChangeableVotes();
    voters = surveyConfig.getVoters();
    viewers = surveyConfig.getViewers();
    showComments = surveyConfig.isShowComments();
    locked = surveyConfig.isLocked();

    canSeeResults = surveyConfig.isCanSeeResults();
    canTakeSurvey = surveyConfig.isCanTakeSurvey();
    visibleVoters = surveyConfig.isVisibleVoters();
    visibleVotersWiki = surveyConfig.isVisibleVotersWiki();
  }

  public int getRenderTitleLevel() {
    return renderTitleLevel;
  }

  public boolean isChangeableVotes() {
    return changeableVotes;
  }

  public List<String> getVoters() {
    return voters;
  }

  public List<String> getViewers() {
    return viewers;
  }

  public boolean isShowComments() {
    return showComments;
  }

  public boolean isLocked() {
    return locked;
  }

  public boolean isVisibleVoters() {
    return visibleVoters;
  }

  public boolean isVisibleVotersWiki() {
    return visibleVotersWiki;
  }

  public boolean isCanSeeResults() {
    return canSeeResults;
  }

  public boolean isCanTakeSurvey() {
    return canTakeSurvey;
  }

  public int getStartBound() {
    return startBound;
  }

  public int getIterateStep() {
    return iterateStep;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    VoteConfig that = (VoteConfig) o;

    if (canSeeResults != that.canSeeResults) return false;
    if (canTakeSurvey != that.canTakeSurvey) return false;
    if (changeableVotes != that.changeableVotes) return false;
    if (iterateStep != that.iterateStep) return false;
    if (locked != that.locked) return false;
    if (renderTitleLevel != that.renderTitleLevel) return false;
    if (showComments != that.showComments) return false;
    if (startBound != that.startBound) return false;
    if (visibleVoters != that.visibleVoters) return false;
    if (visibleVotersWiki != that.visibleVotersWiki) return false;
    if (!viewers.equals(that.viewers)) return false;
    if (!voters.equals(that.voters)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = renderTitleLevel;
    result = 31 * result + (changeableVotes ? 1 : 0);
    result = 31 * result + voters.hashCode();
    result = 31 * result + viewers.hashCode();
    result = 31 * result + (showComments ? 1 : 0);
    result = 31 * result + (locked ? 1 : 0);
    result = 31 * result + (visibleVoters ? 1 : 0);
    result = 31 * result + (visibleVotersWiki ? 1 : 0);
    result = 31 * result + (canSeeResults ? 1 : 0);
    result = 31 * result + (canTakeSurvey ? 1 : 0);
    result = 31 * result + startBound;
    result = 31 * result + iterateStep;
    return result;
  }

  @Override
  public String toString() {
    return "VoteConfig{" +
            "renderTitleLevel=" + renderTitleLevel +
            ", changeableVotes=" + changeableVotes +
            ", voters=" + voters +
            ", viewers=" + viewers +
            ", showComments=" + showComments +
            ", locked=" + locked +
            ", visibleVoters=" + visibleVoters +
            ", visibleVotersWiki=" + visibleVotersWiki +
            ", canSeeResults=" + canSeeResults +
            ", canTakeSurvey=" + canTakeSurvey +
            ", startBound=" + startBound +
            ", iterateStep=" + iterateStep +
            '}';
  }
}
