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
package org.hivesoft.confluence.macros.survey;

import com.atlassian.confluence.pages.actions.AbstractPageAction;
import com.opensymphony.xwork.ActionContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hivesoft.confluence.macros.utils.SurveyManager;

import java.util.Map;

/**
 * This is an action class to handle the comment post for a survey ballot since the page display showing
 * the macro cannot handle post data.
 */
public class AddCommentAction extends AbstractPageAction {
  private static final Logger LOG = Logger.getLogger(AddCommentAction.class);
  private SurveyManager surveyManager;
  private String ballotTitle;
  private String ballotAnchor;
  private String comment;

  /**
   * This method gets called when a post request is received and will add the comment to the indicated ballot.
   *
   * @return a string constant indicating success or failure.
   */
  public String execute() {
    if (getRemoteUser() == null || StringUtils.isBlank(ballotTitle)) {
      return ERROR;
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Entered AddCommentAction with ballotTitle=" + ballotTitle + ", ballotAnchor=" + ballotAnchor + ", comment=" + comment);
    }

    surveyManager.storeComment(ballotTitle, comment, getRemoteUser(), getPage());

    ((Map) ActionContext.getContext().get("request")).put("surveySection", ballotAnchor);
    return SUCCESS;
  }


  /**
   * This is a binding method for the ballot title request parameter.
   */
  public void setBallotTitle(String ballotTitle) {
    this.ballotTitle = ballotTitle;
  }

  /**
   * This is a binding method for the ballot anchor request parameter.
   */
  public void setBallotAnchor(String ballotAnchor) {
    this.ballotAnchor = ballotAnchor;
  }

  /**
   * This is a binding method for the comment request parameter.
   */
  public void setComment(String comment) {
    this.comment = comment;
  }


  /**
   * Injection method for confluence to provide this action with a SurveyManager.
   */
  public void setSurveyManager(SurveyManager surveyManager) {
    this.surveyManager = surveyManager;
  }
}
