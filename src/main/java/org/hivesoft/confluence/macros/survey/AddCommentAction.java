/**
 * Copyright (c) 2006-2013, Confluence Community
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hivesoft.confluence.macros.survey;

import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.actions.AbstractPageAction;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * <p>
 * This is an action class to handle the comment post for
 * a survey ballot since the page display showing
 * the macro cannot handle post data.
 * </p>
 */
public class AddCommentAction extends AbstractPageAction {
    private static final Logger LOG = Logger.getLogger(AddCommentAction.class);
    protected ContentPropertyManager contentPropertyManager;
    protected String ballotTitle;
    protected String ballotAnchor;
    protected String comment;

    /**
     * This method gets called when a post request is received and will add the comment to the indicated ballot.
     *
     * @return a string constant indicating success or failure.
     */
    public String execute() {
        if (getRemoteUser() == null || !TextUtils.stringSet(ballotTitle)) {
            return ERROR;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Entered AddCommentAction with ballotTitle=" + ballotTitle + ", ballotAnchor=" + ballotAnchor + ", comment=" + comment);
        }

        String username = getRemoteUser().getName();
        String commentersPropertyName = "survey." + ballotTitle + ".commenters";
        String commentPropertyName = "survey." + ballotTitle + ".comment." + username;

        String usernameRegex = "\\|" + username + "\\|";

        String commenters = contentPropertyManager.getStringProperty(getPage(), commentersPropertyName);
        //safely store string stored items into text
        if (!StringUtils.isBlank(commenters)) {
            if (StringUtils.isBlank(contentPropertyManager.getTextProperty(getPage(), commentersPropertyName))) {
                contentPropertyManager.setTextProperty(getPage(), commentersPropertyName, commenters);
                contentPropertyManager.setStringProperty(getPage(), commentersPropertyName, null);
            }
        } else {
            commenters = contentPropertyManager.getTextProperty(getPage(), commentersPropertyName);
        }

        if (!StringUtils.isBlank(comment)) {
            if (TextUtils.stringSet(commenters) && !commenters.matches(".*" + usernameRegex + ".*")) {
                commenters += "|" + username + "|";
            } else if (!TextUtils.stringSet(commenters)) {
                commenters = "|" + username + "|";
            }
            contentPropertyManager.setTextProperty(getPage(), commentPropertyName, comment);
        } else if (TextUtils.stringSet(commenters) && commenters.matches(".*" + usernameRegex + ".*")) {
            commenters = commenters.replaceAll(usernameRegex, "");
            contentPropertyManager.setTextProperty(getPage(), commentPropertyName, null);
        }

        //contentPropertyManager.setStringProperty(getPage(), commentersPropertyName, commenters);
        //if there are more commenters than 255 chars
        contentPropertyManager.setTextProperty(getPage(), commentersPropertyName, commenters);

        ((Map) ActionContext.getContext().get("request")).put("surveySection", ballotAnchor);
        return SUCCESS;
    }

    /**
     * This is a binding method for the ballot title request parameter.
     *
     * @param ballotTitle The ballotTitle request parameter.
     */
    public void setBallotTitle(String ballotTitle) {
        this.ballotTitle = ballotTitle;
    }

    /**
     * This is a binding method for the ballot anchor request parameter.
     *
     * @param ballotAnchor The ballotAnchor request parameter.
     */
    public void setBallotAnchor(String ballotAnchor) {
        this.ballotAnchor = ballotAnchor;
    }

    /**
     * This is a binding method for the comment request parameter.
     *
     * @param comment The comment request parameter.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Injection method for confluence to provide this action with a ContentPropertyManager.
     *
     * @param contentPropertyManager The manager to access page properties.
     */
    public void setContentPropertyManager(ContentPropertyManager contentPropertyManager) {
        this.contentPropertyManager = contentPropertyManager;
    }
}
