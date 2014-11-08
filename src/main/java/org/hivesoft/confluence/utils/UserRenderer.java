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
package org.hivesoft.confluence.utils;

import com.atlassian.user.User;
import org.hivesoft.confluence.model.enums.UserVisualization;

public class UserRenderer {

  private static final String TEMPLATE_USER_LINK = "<a href=\"%s/display/~%s\" class=\"url fn confluence-userlink\" data-username=\"%s\">%s</a>";

  private final UserVisualization userVisualization;

  public UserRenderer(UserVisualization userVisualization) {
    this.userVisualization = userVisualization;
  }

  public UserVisualization getUserVisualization() {
    return userVisualization;
  }

  public String render(String contextPath, User voter) {
    switch (userVisualization) {
      case LINKED_LOGIN:
        return String.format(TEMPLATE_USER_LINK, contextPath, voter.getName(), voter.getName(), voter.getName());
      case LINKED_FULL:
        return String.format(TEMPLATE_USER_LINK, contextPath, voter.getName(), voter.getName(), voter.getFullName());
      case PLAIN_FULL:
        return voter.getFullName();
      case PLAIN_LOGIN:
      default:
        return voter.getName();
    }
  }

  public String renderForCsv(User voter) {
    switch (userVisualization) {
      case LINKED_FULL:
      case PLAIN_FULL:
        return voter.getFullName();
      case LINKED_LOGIN:
      case PLAIN_LOGIN:
      default:
        return voter.getName();
    }
  }

  @Override
  public String toString() {
    return "UserRenderer{userVisualization=" + userVisualization + '}';
  }
}
