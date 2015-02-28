package org.hivesoft.confluence.utils;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.user.User;

import java.util.List;

public interface PermissionEvaluator {
  User getRemoteUser();

  String getRemoteUsername();

  User getUserByName(String userName);

  boolean canAttachFile(ContentEntityObject contentEntityObject);

  boolean canCreatePage(ContentEntityObject contentEntityObject);

  boolean isPermissionListEmptyOrContainsGivenUser(List<String> listOfUsersOrGroups, User user);

  boolean canSeeVoters(String visibleVoters, boolean canSeeResults);

  List<User> getActiveUsersForGroupOrUser(String userOrGroupName);
}
