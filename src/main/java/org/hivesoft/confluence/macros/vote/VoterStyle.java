package org.hivesoft.confluence.macros.vote;

import java.util.Arrays;
import java.util.List;

// TODO test
public enum VoterStyle {
  // @formatter:off
  PLAIN_LOGIN( "plain login name",  "false"),
  PLAIN_FULL(  "plain user name"),
  LINKED_LOGIN("linked login name", "true" ),
  LINKED_FULL( "linked user name");
  // @formatter:on

  private final String propertyValue;
  private final List<String> aliasPropertyValues;

  private VoterStyle(String propertyValue, String... aliasPropertyValues) {

    this.propertyValue = propertyValue;
    this.aliasPropertyValues = Arrays.asList(aliasPropertyValues);
  }

  private boolean contains(String propertyValue) {
    if (this.propertyValue.equals(propertyValue)) {
      return true;
    }
    for (String alias : this.aliasPropertyValues) {
      if (alias.equals(propertyValue)) {
        return true;
      }
    }
    return false;
  }

  public static VoterStyle getFor(String propertyValue) {
    for (VoterStyle style : VoterStyle.values()) {
      if (style.contains(propertyValue)) {
        return style;
      }
    }
    return null;
  }
}
