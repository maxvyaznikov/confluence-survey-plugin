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

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.user.Group;
import com.atlassian.user.User;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.macros.utils.wrapper.AnonymousUser;
import org.hivesoft.confluence.macros.utils.wrapper.SurveyUser;
import org.hivesoft.confluence.macros.vote.model.Ballot;

import java.util.ArrayList;
import java.util.List;

public class PermissionEvaluator {

  final UserAccessor userAccessor;
  final UserManager userManager;
  final PermissionManager permissionManager;

  public PermissionEvaluator(UserAccessor userAccessor, UserManager userManager, PermissionManager permissionManager) {
    this.userAccessor = userAccessor;
    this.userManager = userManager;
    this.permissionManager = permissionManager;
  }

  public User getRemoteUser() {
    return getUserByName(getRemoteUsername());
  }

  /**
   * BE AWARE: userManager.getRemoteUserName() may return null if the user is anonymous. Mitigate in macro loading
   */
  private String getRemoteUsername() {
    return userManager.getRemoteUsername();
  }

  /**
   * Always return a useFull user object!
   */
  public User getUserByName(String userName) {
    final User user = userAccessor.getUser(userName);
    if (null == user) {
      if (userName == null) {
        return new AnonymousUser();
      }
      return new SurveyUser(userName);
    }
    return new SurveyUser(user);
  }

  public boolean canAttachFile(ContentEntityObject contentEntityObject) {
    return permissionManager.hasCreatePermission(getRemoteUser(), contentEntityObject, Attachment.class);
  }

  public boolean canCreatePage(ContentEntityObject contentEntityObject) {
    return permissionManager.hasPermission(getRemoteUser(), Permission.EDIT, contentEntityObject);
  }

  public Boolean isPermissionListEmptyOrContainsGivenUser(List<String> listOfUsersOrGroups, User user) {
    if (null == user) {
      return Boolean.FALSE;
    }

    if (listOfUsersOrGroups.isEmpty() || listOfUsersOrGroups.contains(user.getName())) {
      return Boolean.TRUE;
    }

    for (String permittedElement : listOfUsersOrGroups) {
      if (userAccessor.hasMembership(permittedElement.trim(), user.getName())) {
        return Boolean.TRUE;
      }
    }
    return Boolean.FALSE;
  }

  public boolean canSeeVoters(String visibleVoters, boolean canSeeResults) {
    return !(!canSeeResults || StringUtils.isBlank(visibleVoters)) && Boolean.parseBoolean(visibleVoters);
  }

  /**
   * Determine if a user is authorized to cast a vote, taking into account whether they are a voter (either explicitly or implicitly)
   * and whether or not they have already cast a vote. Only logged in users can vote.
   *
   * @param user   the user about to see the ballot.
   * @param ballot the ballot that is about to be shown.
   * @return <code>true</code> if the user can cast a vote, <code>false</code> if they cannot.
   */
  public Boolean canVote(User user, Ballot ballot) {
    return isPermissionListEmptyOrContainsGivenUser(ballot.getConfig().getVoters(), user) && (!ballot.getHasVoted(user) || ballot.getConfig().isChangeableVotes());
  }

  public List<User> getActiveUsersForGroupOrUser(String userOrGroupName) {
    List<User> users = new ArrayList<User>();
    Group group = userAccessor.getGroup(userOrGroupName);
    if (group == null) {
      final User user = userAccessor.getUser(userOrGroupName);
      if (user != null && !userAccessor.isDeactivated(user)) {
        users.add(new SurveyUser(user));
      }
    } else {
      for (String userName : userAccessor.getMemberNamesAsList(group)) {
        final User user = userAccessor.getUser(userName);
        if (!userAccessor.isDeactivated(user)) {
          users.add(new SurveyUser(user));
        }
      }
    }
    return users;
  }


}
