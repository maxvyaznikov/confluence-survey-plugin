package org.hivesoft.confluence.macros.utils;

import com.atlassian.user.User;
import org.hivesoft.confluence.macros.vote.UserVisualization;

public class UserRenderer {

  private static final String TEMPLATE_USER_LINK = "<a href=\"%s/display/~%s\" class=\"url fn confluence-userlink\" data-username=\"%s\">%s</a>";

  private final UserVisualization userVisualization;

  public UserRenderer(UserVisualization userVisualization) {
    this.userVisualization = userVisualization;
  }

  public UserVisualization getUserVisualization() {
    return userVisualization;
  }

  public String render(String contextPath, User voter) {
    switch (userVisualization) {
      case LINKED_LOGIN:
        return String.format(TEMPLATE_USER_LINK, contextPath, voter.getName(), voter.getName(), voter.getName());
      case LINKED_FULL:
        return String.format(TEMPLATE_USER_LINK, contextPath, voter.getName(), voter.getName(), voter.getFullName());
      case PLAIN_FULL:
        return voter.getFullName();
      case PLAIN_LOGIN:
      default:
        return voter.getName();
    }
  }

  public String renderForCsv(User voter) {
    switch (userVisualization) {
      case LINKED_FULL:
      case PLAIN_FULL:
        return voter.getFullName();
      case LINKED_LOGIN:
      case PLAIN_LOGIN:
      default:
        return voter.getName();
    }
  }

  @Override
  public String toString() {
    return "UserRenderer{userVisualization=" + userVisualization + '}';
  }
}
