package org.hivesoft.confluence.macros.enums;

import org.apache.commons.lang3.StringUtils;

public enum VoteAction {
  VOTE,
  UNVOTE,
  NONE;

  public static VoteAction fromString(String action) {
    if(StringUtils.isBlank(action)){
      return NONE;
    }
    try {
      return VoteAction.valueOf(action.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return NONE;
    }
  }
}
