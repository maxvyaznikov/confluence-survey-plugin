package org.hivesoft.confluence.utils;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.user.User;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.model.wrapper.SurveyUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestPermissionEvaluator implements PermissionEvaluator {

  private final User currentUser;
  private final boolean canAttachFile;
  private final boolean canCreatePage;
  private final Map<String, List<String>> groupsWithUsers;

  private TestPermissionEvaluator(User currentUser, boolean canAttachFile, boolean canCreatePage, Map<String, List<String>> groupsWithUsers) {
    this.currentUser = currentUser;
    this.canAttachFile = canAttachFile;
    this.canCreatePage = canCreatePage;
    this.groupsWithUsers = groupsWithUsers;
  }

  @Override
  public User getRemoteUser() {
    return currentUser;
  }

  @Override
  public String getRemoteUsername() {
    return currentUser.getName();
  }

  @Override
  public User getUserByName(String userName) {
    return currentUser;
  }

  @Override
  public boolean canAttachFile(ContentEntityObject contentEntityObject) {
    return canAttachFile;
  }

  @Override
  public boolean canCreatePage(ContentEntityObject contentEntityObject) {
    return canCreatePage;
  }

  @Override
  public boolean isPermissionListEmptyOrContainsGivenUser(List<String> listOfUsersOrGroups, User user) {
    if (user == null) {
      return false;
    }

    if (listOfUsersOrGroups.isEmpty() || listOfUsersOrGroups.contains(user.getName())) {
      return true;
    }

    for (String userOrGroup : listOfUsersOrGroups) {
      if (groupsWithUsers.containsKey(userOrGroup)) {
        if (groupsWithUsers.get(userOrGroup).contains(user.getName())) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public boolean canSeeVoters(String visibleVoters, boolean canSeeResults) {
    return !(!canSeeResults || StringUtils.isBlank(visibleVoters)) && Boolean.parseBoolean(visibleVoters);
  }

  @Override
  public List<User> getActiveUsersForGroupOrUser(String userOrGroupName) {
    List<User> users = new ArrayList<User>();
    if (groupsWithUsers.get(userOrGroupName) != null) {
      for (String user : groupsWithUsers.get(userOrGroupName)) {
        users.add(new SurveyUser(user));
      }
    }
    return users;
  }

  public static class Builder {

    private final User currentUser;
    private boolean canAttachFile = false;
    private boolean canCreatePage = false;
    private List<User> activeUsers = new ArrayList<User>();
    private Map<String, List<String>> groupsWithUsers = new HashMap<String, List<String>>();

    public Builder(User currentUser) {
      this.currentUser = currentUser;
    }

    public Builder canAttachFile(boolean canAttachFile) {
      this.canAttachFile = canAttachFile;
      return this;
    }

    public Builder canCreatePage(boolean canCreatePage) {
      this.canCreatePage = canCreatePage;
      return this;
    }

    public Builder groupsWithUsers(Map<String, List<String>> groupsWithUsers) {
      this.groupsWithUsers = groupsWithUsers;
      return this;
    }

    public TestPermissionEvaluator build() {
      return new TestPermissionEvaluator(currentUser, canAttachFile, canCreatePage, groupsWithUsers);
    }
  }
}
