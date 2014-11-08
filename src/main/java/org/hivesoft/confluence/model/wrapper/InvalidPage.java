package org.hivesoft.confluence.model.wrapper;

import com.atlassian.confluence.pages.AbstractPage;

import javax.ws.rs.core.Response;

public class InvalidPage extends AbstractPage {

  Response.Status status;
  String message;

  public InvalidPage(Response.Status status, String message) {
    this.status = status;
    this.message = message;
  }

  @Override
  public String getType() {
    return null;
  }

  @Override
  public String getUrlPath() {
    return null;
  }

  @Override
  public String getNameForComparison() {
    return null;
  }

  @Override
  public String getLinkWikiMarkup() {
    return null;
  }

  public Response toResponse() {
    return Response.status(status).entity(message).build();
  }
}
