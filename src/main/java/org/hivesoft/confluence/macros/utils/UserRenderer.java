package org.hivesoft.confluence.macros.utils;

import com.atlassian.user.User;
import org.hivesoft.confluence.macros.vote.VoteConfig;

public class UserRenderer {

  private static final String TEMPLATE_USER_LINK = "<a href=\"%s/display/~%s\" class=\"url fn confluence-userlink\" data-username=\"%s\">%s</a>";

  private final VoteConfig config;

  public UserRenderer(VoteConfig config) {
    this.config = config;
  }

  // TODO for csv export
  public String render(String contextPath, User voter) {
    switch (config.getUserVisualization()) {
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
}
