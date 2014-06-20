/**
 * Copyright (c) 2006-2014, Confluence Community
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hivesoft.confluence.rest;

import com.atlassian.confluence.content.render.xhtml.DefaultConversionContext;
import com.atlassian.confluence.content.render.xhtml.XhtmlException;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.xhtml.api.MacroDefinition;
import com.atlassian.confluence.xhtml.api.MacroDefinitionHandler;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import org.hivesoft.confluence.macros.enums.VoteAction;
import org.hivesoft.confluence.macros.survey.SurveyMacro;
import org.hivesoft.confluence.macros.survey.model.Survey;
import org.hivesoft.confluence.macros.utils.SurveyManager;
import org.hivesoft.confluence.macros.vote.VoteMacro;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.rest.exceptions.MacroReconstructionException;
import org.hivesoft.confluence.rest.representations.VoteRepresentation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/pages/{pageId}/votes")
public class VoteResource {
  private static final Logger.Log LOG = Logger.getInstance(VoteResource.class);

  protected final TransactionTemplate transactionTemplate;
  protected final PageManager pageManager;
  protected final XhtmlContent xhtmlContent;
  protected final I18nResolver i18nResolver;
  protected final SurveyManager surveyManager;

  public VoteResource(TransactionTemplate transactionTemplate, PageManager pageManager, XhtmlContent xhtmlContent, I18nResolver i18nResolver, SurveyManager surveyManager) {
    this.transactionTemplate = transactionTemplate;
    this.pageManager = pageManager;
    this.surveyManager = surveyManager;
    this.xhtmlContent = xhtmlContent;
    this.i18nResolver = i18nResolver;
  }

  @POST
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response castVote(@PathParam("pageId") long contentId, VoteRepresentation voteRepresentation) throws UnsupportedEncodingException {
    LOG.debug("Entered VoteResource->castVote with pageId=" + contentId + ", voteRepresentation=" + voteRepresentation);

    final String ballotTitle = URLDecoder.decode(voteRepresentation.getBallotTitle(), "UTF-8");
    final String choiceName = URLDecoder.decode(voteRepresentation.getVoteChoice(), "UTF-8");
    final VoteAction voteAction = VoteAction.fromString(voteRepresentation.getVoteAction());

    final ContentEntityObject contentEntityObject = pageManager.getById(Long.valueOf(contentId));

    if (contentEntityObject == null) {
      return Response.status(Response.Status.NOT_FOUND).entity("The contentEntity with id: " + contentId + " was not found").build();
    }

    try {
      final Ballot ballot = reconstructBallotByTitleFromSurveyOrVote(ballotTitle, contentEntityObject);
      surveyManager.recordVote(ballot, contentEntityObject, choiceName, voteAction);
    } catch (MacroReconstructionException e) {
      return Response.status(Response.Status.BAD_REQUEST).entity("There was a problem finding the specified ballot: " + e.getMessage()).build();
    }

    return Response.ok().build();
  }

  private Ballot reconstructBallotByTitleFromSurveyOrVote(final String ballotTitle, final ContentEntityObject contentEntityObject) throws MacroReconstructionException {
    final List<Ballot> ballotsFound = new ArrayList<Ballot>();
    try {
      xhtmlContent.handleMacroDefinitions(contentEntityObject.getBodyAsString(), new DefaultConversionContext(contentEntityObject.toPageContext()), new MacroDefinitionHandler() {
                @Override
                public void handle(MacroDefinition macroDefinition) {
                  final Map<String, String> parameters = macroDefinition.getParameters();
                  if (SurveyMacro.SURVEY_MACRO.equals(macroDefinition.getName())) {
                    final Survey survey = surveyManager.reconstructSurveyFromPlainTextMacroBody(macroDefinition.getBodyText(), contentEntityObject, parameters);
                    final Ballot ballot = survey.getBallot(ballotTitle);
                    if (null != ballot) {
                      ballotsFound.add(ballot);
                    }
                  } else if (VoteMacro.VOTE_MACRO.equals(macroDefinition.getName())) {
                    final Ballot ballot = surveyManager.reconstructBallotFromPlainTextMacroBody(parameters, macroDefinition.getBodyText(), contentEntityObject);
                    if (ballot.getTitle().equals(ballotTitle)) {
                      ballotsFound.add(ballot);
                    }
                  }
                }
              }
      );
    } catch (XhtmlException e) {
      final String message = "There was a problem while parsing the Xhtml content: " + e.getMessage() + " for ballotTitle: " + ballotTitle;
      LOG.error(message, e);
      throw new MacroReconstructionException(message, e);
    }

    if (ballotsFound.isEmpty()) {
      throw new MacroReconstructionException("Could not find the specified ballot with title " + ballotTitle + " on the specified page!");
    } else if (ballotsFound.size() > 1) {
      throw new MacroReconstructionException("Found more than one ballot with title: " + ballotTitle + ". No Vote will be cast.");
    }
    return ballotsFound.get(0);
  }
}