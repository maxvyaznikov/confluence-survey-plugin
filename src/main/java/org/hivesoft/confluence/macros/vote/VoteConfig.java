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
package org.hivesoft.confluence.macros.vote;

import com.atlassian.confluence.pages.AbstractPage;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.macros.utils.PermissionEvaluator;
import org.hivesoft.confluence.macros.utils.SurveyUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class VoteConfig {
  public static final int DEFAULT_START_BOUND = 1;
  public static final int DEFAULT_ITERATE_STEP = 1;

  public static final String KEY_TITLE = "title";
  public static final String KEY_RENDER_TITLE_LEVEL = "renderTitleLevel";
  public static final String KEY_CHANGEABLE_VOTES = "changeableVotes";
  public static final String KEY_VOTERS = "voters";
  protected static final String KEY_VIEWERS = "viewers";
  public static final String KEY_MANAGERS = "managers";
  protected static final String KEY_SHOW_COMMENTS = "showComments";
  protected static final String KEY_VISIBLE_VOTERS = "visibleVoters";
  protected static final String KEY_VISIBLE_PENDING_VOTERS = "visiblePendingVoters";
  protected static final String KEY_VISIBLE_VOTERS_WIKI = "visibleVotersWiki";
  public static final String KEY_LOCKED = "locked";
  public static final String KEY_START_BOUND = "startBound";
  public static final String KEY_ITERATE_STEP = "iterateStep";
  public static final String KEY_SHOW_CONDENSED = "showCondensed";

  protected int renderTitleLevel;
  private boolean changeableVotes;
  private List<String> voters;
  private List<String> viewers;
  private List<String> managers;
  protected boolean showComments;
  private boolean locked;
  private boolean visibleVoters;
  private final boolean visiblePendingVoters;
  private boolean showCondensed;

  private boolean visibleVotersWiki;
  private boolean canSeeResults;
  private boolean canTakeSurvey;

  private boolean canManageSurvey;

  protected int startBound = DEFAULT_START_BOUND;
  protected int iterateStep = DEFAULT_ITERATE_STEP;

  protected List<String> renderProblems = new ArrayList<String>();
  protected final PermissionEvaluator permissionEvaluator;

  public VoteConfig(PermissionEvaluator permissionEvaluator, Map<String, String> parameters) {
    this.permissionEvaluator = permissionEvaluator;

    renderTitleLevel = SurveyUtils.getIntegerFromString(parameters.get(KEY_RENDER_TITLE_LEVEL), 3);
    changeableVotes = Boolean.parseBoolean(parameters.get(KEY_CHANGEABLE_VOTES));
    voters = SurveyUtils.getListFromStringCommaSeparated(StringUtils.defaultString(parameters.get(KEY_VOTERS)));
    viewers = SurveyUtils.getListFromStringCommaSeparated(StringUtils.defaultString(parameters.get(KEY_VIEWERS)));
    managers = SurveyUtils.getListFromStringCommaSeparated(StringUtils.defaultString(parameters.get(KEY_MANAGERS)));
    showComments = SurveyUtils.getBooleanFromString(parameters.get(KEY_SHOW_COMMENTS), false);
    locked = SurveyUtils.getBooleanFromString(parameters.get(KEY_LOCKED), false);
    showCondensed = SurveyUtils.getBooleanFromString(parameters.get(KEY_SHOW_CONDENSED), false);

    final String remoteUsername = permissionEvaluator.getRemoteUsername();

    canSeeResults = permissionEvaluator.isPermissionListEmptyOrContainsGivenUser(viewers, remoteUsername);
    canTakeSurvey = permissionEvaluator.isPermissionListEmptyOrContainsGivenUser(voters, remoteUsername);
    canManageSurvey = permissionEvaluator.isPermissionListEmptyOrContainsGivenUser(managers, remoteUsername);

    visibleVoters = permissionEvaluator.getCanSeeVoters(parameters.get(KEY_VISIBLE_VOTERS), canSeeResults) && canManageSurvey;
    visiblePendingVoters = permissionEvaluator.getCanSeeVoters(parameters.get(KEY_VISIBLE_PENDING_VOTERS), canSeeResults) && !voters.isEmpty();
    visibleVotersWiki = SurveyUtils.getBooleanFromString(parameters.get(KEY_VISIBLE_VOTERS_WIKI), false);

    this.startBound = SurveyUtils.getIntegerFromString(parameters.get(KEY_START_BOUND), DEFAULT_START_BOUND);
    this.iterateStep = SurveyUtils.getIntegerFromString(parameters.get(KEY_ITERATE_STEP), DEFAULT_ITERATE_STEP);
  }

  public VoteConfig(SurveyConfig surveyConfig) {
    this.permissionEvaluator = surveyConfig.permissionEvaluator;
    renderTitleLevel = surveyConfig.getRenderTitleLevel();
    if (renderTitleLevel > 0) {
      renderTitleLevel++;
    }

    changeableVotes = surveyConfig.isChangeableVotes();
    voters = surveyConfig.getVoters();
    viewers = surveyConfig.getViewers();
    managers = surveyConfig.getManagers();
    showComments = surveyConfig.isShowComments();
    locked = surveyConfig.isLocked();
    showCondensed = surveyConfig.isShowCondensed();

    canSeeResults = surveyConfig.isCanSeeResults();
    canTakeSurvey = surveyConfig.isCanTakeSurvey();
    canManageSurvey = surveyConfig.isCanManageSurvey();

    visibleVoters = surveyConfig.isVisibleVoters();
    visiblePendingVoters = surveyConfig.isVisiblePendingVoters();
    visibleVotersWiki = surveyConfig.isVisibleVotersWiki();

    startBound = surveyConfig.getStartBound();
    iterateStep = surveyConfig.getIterateStep();
  }

  public void addRenderProblems(String... problem) {
    renderProblems.addAll(Arrays.asList(problem));
  }

  public int getRenderTitleLevel() {
    return renderTitleLevel;
  }

  public Boolean isChangeableVotes() {
    return changeableVotes;
  }

  public List<String> getVoters() {
    return voters;
  }

  public List<String> getViewers() {
    return viewers;
  }

  public List<String> getManagers() {
    return managers;
  }

  public Boolean isShowComments() {
    return showComments;
  }

  public Boolean isLocked() {
    return locked;
  }

  public Boolean isVisibleVoters() {
    return visibleVoters;
  }

  public boolean isVisiblePendingVoters() {
    return visiblePendingVoters;
  }

  public Boolean isVisibleVotersWiki() {
    return visibleVotersWiki;
  }

  public Boolean isCanSeeResults() {
    return canSeeResults;
  }

  public Boolean isCanTakeSurvey() {
    return canTakeSurvey;
  }

  public boolean isCanManageSurvey() {
    return canManageSurvey;
  }

  public boolean isShowCondensed() {
    return showCondensed;
  }

  public int getStartBound() {
    return startBound;
  }

  public int getIterateStep() {
    return iterateStep;
  }

  public Boolean canAttachFile(AbstractPage page) {
    return permissionEvaluator.canAttachFile(page);
  }

  public Boolean canCreatePage(AbstractPage page) {
    return permissionEvaluator.canCreatePage(page);
  }

  public List<String> getRenderProblems() {
    return renderProblems;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    VoteConfig that = (VoteConfig) o;

    if (canManageSurvey != that.canManageSurvey) return false;
    if (canSeeResults != that.canSeeResults) return false;
    if (canTakeSurvey != that.canTakeSurvey) return false;
    if (changeableVotes != that.changeableVotes) return false;
    if (iterateStep != that.iterateStep) return false;
    if (locked != that.locked) return false;
    if (renderTitleLevel != that.renderTitleLevel) return false;
    if (showComments != that.showComments) return false;
    if (showCondensed != that.showCondensed) return false;
    if (startBound != that.startBound) return false;
    if (visibleVoters != that.visibleVoters) return false;
    if (visiblePendingVoters != that.visiblePendingVoters) return false;
    if (visibleVotersWiki != that.visibleVotersWiki) return false;
    if (managers != null ? !managers.equals(that.managers) : that.managers != null) return false;
    if (renderProblems != null ? !renderProblems.equals(that.renderProblems) : that.renderProblems != null) return false;
    if (viewers != null ? !viewers.equals(that.viewers) : that.viewers != null) return false;
    if (voters != null ? !voters.equals(that.voters) : that.voters != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = renderTitleLevel;
    result = 31 * result + (changeableVotes ? 1 : 0);
    result = 31 * result + (voters != null ? voters.hashCode() : 0);
    result = 31 * result + (viewers != null ? viewers.hashCode() : 0);
    result = 31 * result + (managers != null ? managers.hashCode() : 0);
    result = 31 * result + (showComments ? 1 : 0);
    result = 31 * result + (locked ? 1 : 0);
    result = 31 * result + (visibleVoters ? 1 : 0);
    result = 31 * result + (visiblePendingVoters ? 1 : 0);
    result = 31 * result + (visibleVotersWiki ? 1 : 0);
    result = 31 * result + (canSeeResults ? 1 : 0);
    result = 31 * result + (canTakeSurvey ? 1 : 0);
    result = 31 * result + (canManageSurvey ? 1 : 0);
    result = 31 * result + startBound;
    result = 31 * result + iterateStep;
    result = 31 * result + (renderProblems != null ? renderProblems.hashCode() : 0);
    result = 31 * result + (showCondensed ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    return "VoteConfig{" +
            "renderTitleLevel=" + renderTitleLevel +
            ", changeableVotes=" + changeableVotes +
            ", voters=" + voters +
            ", viewers=" + viewers +
            ", managers=" + managers +
            ", showComments=" + showComments +
            ", locked=" + locked +
            ", visibleVoters=" + visibleVoters +
            ", visiblePendingVoters=" + visiblePendingVoters +
            ", visibleVotersWiki=" + visibleVotersWiki +
            ", canSeeResults=" + canSeeResults +
            ", canTakeSurvey=" + canTakeSurvey +
            ", canManageSurvey=" + canManageSurvey +
            ", startBound=" + startBound +
            ", iterateStep=" + iterateStep +
            ", renderProblems=" + renderProblems +
            ", showCondensed=" + showCondensed +
            '}';
  }
}
