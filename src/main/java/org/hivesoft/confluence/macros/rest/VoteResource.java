package org.hivesoft.confluence.macros.rest;

import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.sal.api.user.UserManager;
import org.hivesoft.confluence.macros.utils.PermissionEvaluator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/votes")
public class VoteResource {

  protected final PageManager pageManager;
  protected final SpaceManager spaceManager;
  protected final ContentPropertyManager contentPropertyManager;
  protected final PermissionEvaluator permissionEvaluator;

  public VoteResource(PageManager pageManager, SpaceManager spaceManager, ContentPropertyManager contentPropertyManager, UserAccessor userAccessor, UserManager userManager) {
    this.pageManager = pageManager;
    this.spaceManager = spaceManager;
    this.contentPropertyManager = contentPropertyManager;
    this.permissionEvaluator = new PermissionEvaluator(userAccessor, userManager);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getVote() {
    return "thanks for calling 111";
  }

  @Path("/{pageId}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getVote(@PathParam("pageId") long pageId) {
    return "thanks for calling from page: " + pageId;
  }
}
