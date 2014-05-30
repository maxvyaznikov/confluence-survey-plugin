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
import org.hivesoft.confluence.macros.utils.SurveyUtils;
import org.hivesoft.confluence.macros.vote.VoteMacro;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.rest.exceptions.MacroReconstructionException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
  @Path("/{voteTitle}/choices/{choiceName}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response castVote(@PathParam("pageId") long pageId, @PathParam("voteTitle") String inVoteTitle, @PathParam("choiceName") String inChoiceName, String inVoteAction) throws UnsupportedEncodingException {
    final String ballotTitle = URLDecoder.decode(inVoteTitle, "UTF-8");
    final String choiceName = URLDecoder.decode(inChoiceName, "UTF-8");
    final VoteAction voteAction = VoteAction.fromString(inVoteAction);

    final ContentEntityObject contentEntityObject = pageManager.getById(Long.valueOf(pageId));

    if (contentEntityObject == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    //TODO: reconstruct ballot (surveys/votes)

    //reconstructBallotWithTitle(ballotTitle);

    //surveyManager.recordVote(ballot, contentEntityObject, choiceName, voteAction);

    return Response.ok().build();
  }

  private List<Ballot> reconstructBallotByTitleFromSurveyOrVote(final String ballotTitle, final ContentEntityObject contentEntityObject) throws MacroReconstructionException {
    final List<Ballot> ballotsFound = new ArrayList<Ballot>();
    try {
      xhtmlContent.handleMacroDefinitions(contentEntityObject.getBodyAsString(), new DefaultConversionContext(contentEntityObject.toPageContext()), new MacroDefinitionHandler() {
                @Override
                public void handle(MacroDefinition macroDefinition) {
                  final Map<String, String> parameters = macroDefinition.getParameters();
                  String currentTitle = SurveyUtils.getTitleInMacroParameters(parameters);
                  if (SurveyMacro.SURVEY_MACRO.equals(macroDefinition.getName())) {
                    //TODO: on a survey check the sub-items (ballots)

                    final Survey survey = surveyManager.reconstructSurveyFromPlainTextMacroBody(macroDefinition.getBodyText(), contentEntityObject, macroDefinition.getParameters());
                    //survey.setTitle(surveyTitle);
                    //<ballotsFound.add(survey);
                  } else if (VoteMacro.VOTE_MACRO.equals(macroDefinition.getName())) {
                    //TODO reconstruct the ballot "directly"
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
      throw new MacroReconstructionException("Could not find the specified survey macro with title " + ballotTitle + " on the specified page!");
    }
    return ballotsFound;
  }
}