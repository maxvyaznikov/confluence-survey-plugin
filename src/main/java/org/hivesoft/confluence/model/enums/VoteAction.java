package org.hivesoft.confluence.model.enums;

import org.apache.commons.lang3.StringUtils;

public enum VoteAction {
  VOTE,
  UNVOTE,
  CHANGEVOTE,
  NONE;

  public static VoteAction fromString(String action) {
    if (StringUtils.isBlank(action)) {
      return NONE;
    }
    try {
      return VoteAction.valueOf(action.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return NONE;
    }
  }

  public static VoteAction fromChange(int change) {
    if (change == 0) {
      return CHANGEVOTE;
    } else if (change > 0) {
      return VOTE;
    }
    return UNVOTE;
  }
}
