package org.hivesoft.confluence.rest.representations;

public class ResetRepresentation {
  String title;
  boolean reset;

  public ResetRepresentation() {
    //for jaxb
  }

  public ResetRepresentation(String title, boolean reset) {
    this();
    this.title = title;
    this.reset = reset;
  }

  public String getTitle() {
    return title;
  }

  public boolean isReset() {
    return reset;
  }
}
