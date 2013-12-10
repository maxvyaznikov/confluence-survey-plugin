package org.hivesoft.confluence.rest;

import com.atlassian.confluence.content.render.xhtml.DefaultConversionContext;
import com.atlassian.confluence.content.render.xhtml.XhtmlException;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.xhtml.api.MacroDefinition;
import com.atlassian.confluence.xhtml.api.MacroDefinitionHandler;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.sal.api.user.UserManager;
import org.hivesoft.confluence.macros.utils.PermissionEvaluator;
import org.hivesoft.confluence.macros.utils.SurveyUtils;
import org.hivesoft.confluence.macros.vote.VoteMacro;
import org.hivesoft.confluence.macros.vote.model.Ballot;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/pages/{pageId}/votes")
public class VoteResource {
  private static final Logger.Log LOG = Logger.getInstance(VoteResource.class);

  protected final PageManager pageManager;
  protected final SpaceManager spaceManager;
  protected final ContentPropertyManager contentPropertyManager;
  protected final PermissionEvaluator permissionEvaluator;
  protected final XhtmlContent xhtmlContent;

  public VoteResource(PageManager pageManager, SpaceManager spaceManager, ContentPropertyManager contentPropertyManager, UserAccessor userAccessor, UserManager userManager, XhtmlContent xhtmlContent) {
    this.pageManager = pageManager;
    this.spaceManager = spaceManager;
    this.contentPropertyManager = contentPropertyManager;
    this.permissionEvaluator = new PermissionEvaluator(userAccessor, userManager);
    this.xhtmlContent = xhtmlContent;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getVotesForPage(@PathParam("pageId") String pageId) {
    return "thanks for calling from Page: " + pageId;
  }

  @Path("/{voteTitle}/export")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getExportForBallotTitle(@PathParam("pageId") final long pageId, @PathParam("voteTitle") final String voteTitle) {

    //todo: reconstruct ballot so the export can be done
    try {
      final String ballotTitle = URLDecoder.decode(voteTitle, "UTF-8");
      final Page page = pageManager.getPage(pageId);
      final List<Ballot> ballots = new ArrayList<Ballot>();
      LOG.info("Found page with id=" + pageId + " and title=" + page.getTitle());
      xhtmlContent.handleMacroDefinitions(page.getBodyAsString(), new DefaultConversionContext(page.getContentEntityObject().toPageContext()), new MacroDefinitionHandler() {
        @Override
        public void handle(MacroDefinition macroDefinition) {
          if (VoteMacro.VOTE_MACRO.equals(macroDefinition.getName())) {
            final Map<String, String> parameters = macroDefinition.getParameters();
            String currentTitle = null;
            try {
              currentTitle = SurveyUtils.getVoteTitle(parameters);
            } catch (MacroException e) {
              e.printStackTrace();
            }
            LOG.info("ballotTitle for export=" + ballotTitle + ", currentTitle to check is=" + currentTitle);
            if (ballotTitle.equalsIgnoreCase(currentTitle)) {
              LOG.info("Try to reconstructBallot...");
              ballots.add(SurveyUtils.reconstructBallot(ballotTitle, macroDefinition.getBodyText(), page.getContentEntityObject(), contentPropertyManager));
            }
          }
        }
      });

      if (!ballots.isEmpty()) {
        return "hallelulja";
      }
      return "thanks for calling from page: " + pageId + " and ballot with title: " + ballotTitle;
    } catch (XhtmlException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    return "Found an unexpected problem!";
  }
}
