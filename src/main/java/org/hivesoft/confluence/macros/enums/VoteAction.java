package org.hivesoft.confluence.macros.enums;

public enum VoteAction {
  VOTE,
  UNVOTE,
  NONE;

  public static VoteAction fromString(String action) {
    try {
      return VoteAction.valueOf(action.trim().toUpperCase());
    } catch (NullPointerException e) {
      return NONE;
    } catch (IllegalArgumentException e) {
      return NONE;
    }
  }
}
