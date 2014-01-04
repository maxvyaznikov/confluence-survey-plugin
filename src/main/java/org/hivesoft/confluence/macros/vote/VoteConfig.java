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
}
