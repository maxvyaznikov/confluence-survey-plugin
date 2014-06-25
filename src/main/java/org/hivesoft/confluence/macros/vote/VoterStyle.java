package org.hivesoft.confluence.macros.vote;

import java.util.Arrays;
import java.util.List;

public enum VoterStyle {
  PLAIN_LOGIN("plain login name"),
  PLAIN_FULL("plain user name"),
  LINKED_LOGIN("linked login name"),
  LINKED_FULL("linked user name");

  /** Visible for testing */
  final String propertyValue;

  private VoterStyle(String propertyValue) {

    this.propertyValue = propertyValue;
  }

  /** @return the found {@link VoterStyle} with the given {@code propertyValue} or {@code null} if not found*/
  public static VoterStyle getFor(String propertyValue) {
    for (VoterStyle style : VoterStyle.values()) {
      if (style.propertyValue.equals(propertyValue)) {
        return style;
      }
    }
    return null;
  }
}
