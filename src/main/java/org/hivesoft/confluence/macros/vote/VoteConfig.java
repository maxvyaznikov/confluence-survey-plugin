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
import com.atlassian.user.User;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.macros.utils.PermissionEvaluator;
import org.hivesoft.confluence.macros.utils.SurveyUtils;
import org.hivesoft.confluence.macros.utils.UserRenderer;

import java.util.*;

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
  protected static final String KEY_VISIBLE_VOTERS_WIKI = "visibleVotersWiki"; // old key for userVisualization
  protected static final String KEY_USER_VISUALIZATION = "userVisualization";
  public static final String KEY_LOCKED = "locked";
  public static final String KEY_START_BOUND = "startBound";
  public static final String KEY_ITERATE_STEP = "iterateStep";
  public static final String KEY_SHOW_CONDENSED = "showCondensed";
  protected static final String KEY_ANONYMOUS_MODE = "anonymousMode";

  private final int renderTitleLevel;
  private final boolean changeableVotes;
  private final List<String> voters;
  private final List<String> viewers;
  private final List<String> managers;
  private final boolean showComments;
  private final boolean locked;
  private final boolean visibleVoters;
  private final boolean visiblePendingVoters;
  private final boolean showCondensed;
  private final boolean anonymous;

  private final boolean canSeeResults;
  private final boolean canTakeSurvey;
  private final boolean canManageSurvey;

  private final int startBound;
  private final int iterateStep;

  protected final List<String> renderProblems = new ArrayList<String>();

  protected final PermissionEvaluator permissionEvaluator;
  protected final UserRenderer userRenderer;

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
    anonymous = SurveyUtils.getBooleanFromString(parameters.get(KEY_ANONYMOUS_MODE), false);

    final User remoteUser = permissionEvaluator.getRemoteUser();

    canSeeResults = permissionEvaluator.isPermissionListEmptyOrContainsGivenUser(viewers, remoteUser);
    canTakeSurvey = permissionEvaluator.isPermissionListEmptyOrContainsGivenUser(voters, remoteUser);
    canManageSurvey = permissionEvaluator.isPermissionListEmptyOrContainsGivenUser(managers, remoteUser);

    visibleVoters = permissionEvaluator.canSeeVoters(parameters.get(KEY_VISIBLE_VOTERS), canSeeResults) && canManageSurvey;
    visiblePendingVoters = permissionEvaluator.canSeeVoters(parameters.get(KEY_VISIBLE_PENDING_VOTERS), canSeeResults) && !voters.isEmpty();

    this.startBound = SurveyUtils.getIntegerFromString(parameters.get(KEY_START_BOUND), DEFAULT_START_BOUND);
    this.iterateStep = SurveyUtils.getIntegerFromString(parameters.get(KEY_ITERATE_STEP), DEFAULT_ITERATE_STEP);

    UserVisualization userVisualization = SurveyUtils.getUserVisualizationFromString(parameters.get(KEY_USER_VISUALIZATION), null);
    if (userVisualization == null) {
      // default and backwards compatibility for version <= 2.8.0
      boolean visibleVotersWiki = SurveyUtils.getBooleanFromString(parameters.get(KEY_VISIBLE_VOTERS_WIKI), false);
      if (visibleVotersWiki) {
        userVisualization = UserVisualization.LINKED_LOGIN;
      } else {
        userVisualization = UserVisualization.PLAIN_LOGIN;
      }
    }
    this.userRenderer = new UserRenderer(userVisualization);
  }

  public VoteConfig(SurveyConfig surveyConfig) {
    this.permissionEvaluator = surveyConfig.permissionEvaluator;

    renderTitleLevel = surveyConfig.getRenderTitleLevel() > 0 ? surveyConfig.getRenderTitleLevel() + 1 : 0;

    changeableVotes = surveyConfig.isChangeableVotes();
    voters = surveyConfig.getVoters();
    viewers = surveyConfig.getViewers();
    managers = surveyConfig.getManagers();
    showComments = surveyConfig.isShowComments();
    locked = surveyConfig.isLocked();
    showCondensed = surveyConfig.isShowCondensed();
    anonymous = surveyConfig.isAnonymous();

    canSeeResults = surveyConfig.isCanSeeResults();
    canTakeSurvey = surveyConfig.isCanTakeSurvey();
    canManageSurvey = surveyConfig.isCanManageSurvey();

    visibleVoters = surveyConfig.isVisibleVoters();
    visiblePendingVoters = surveyConfig.isVisiblePendingVoters();

    startBound = surveyConfig.getStartBound();
    iterateStep = surveyConfig.getIterateStep();

    userRenderer = surveyConfig.getUserRenderer();
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

  public boolean isAnonymous() {
    return anonymous;
  }

  public int getStartBound() {
    return startBound;
  }

  public int getIterateStep() {
    return iterateStep;
  }

  public UserRenderer getUserRenderer() {
    return userRenderer;
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

  public List<User> getAllPossibleVoters() {
    Set<User> users = Sets.newHashSet();
    for (String configuredVoter : voters) {
      users.addAll(permissionEvaluator.getActiveUsersForGroupOrUser(configuredVoter));
    }
    return Lists.newArrayList(users);
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
            ", showCondensed=" + showCondensed +
            ", anonymous=" + anonymous +
            ", canSeeResults=" + canSeeResults +
            ", canTakeSurvey=" + canTakeSurvey +
            ", canManageSurvey=" + canManageSurvey +
            ", startBound=" + startBound +
            ", iterateStep=" + iterateStep +
            ", renderProblems=" + renderProblems +
            ", userRenderer=" + userRenderer +
            '}';
  }
}