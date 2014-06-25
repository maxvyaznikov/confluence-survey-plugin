package org.hivesoft.confluence.macros.vote;

public enum UserVisualization {
  PLAIN_LOGIN("plain login name"),
  PLAIN_FULL("plain user name"),
  LINKED_LOGIN("linked login name"),
  LINKED_FULL("linked user name");

  /** Visible for testing */
  final String propertyValue;

  private UserVisualization(String propertyValue) {

    this.propertyValue = propertyValue;
  }

  /** @return the found {@link UserVisualization} with the given {@code propertyValue} or {@code null} if not found*/
  public static UserVisualization getFor(String propertyValue) {
    for (UserVisualization style : UserVisualization.values()) {
      if (style.propertyValue.equals(propertyValue)) {
        return style;
      }
    }
    return null;
  }
}
