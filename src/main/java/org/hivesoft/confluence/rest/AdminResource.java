/**
 * Copyright (c) 2006-2015, Confluence Community
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hivesoft.confluence.rest;

import com.atlassian.extras.common.log.Logger;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.rest.callbacks.TransactionCallbackGetConfig;
import org.hivesoft.confluence.rest.callbacks.TransactionCallbackSetConfig;
import org.hivesoft.confluence.rest.representations.SurveyConfigRepresentation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/admin")
public class AdminResource {
  private static final Logger.Log LOG = Logger.getInstance(AdminResource.class);

  public final static String SURVEY_PLUGIN_KEY_ICON_SET = "survey-plugin.iconSet";
  public final static String SURVEY_PLUGIN_ICON_SET_DEFAULT = "default";

  private final UserManager userManager;
  private final PluginSettingsFactory pluginSettingsFactory;
  private final TransactionTemplate transactionTemplate;

  public AdminResource(UserManager userManager, PluginSettingsFactory pluginSettingsFactory, TransactionTemplate transactionTemplate) {
    this.userManager = userManager;
    this.pluginSettingsFactory = pluginSettingsFactory;
    this.transactionTemplate = transactionTemplate;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getConfig() {
    if (isAdmin()) {
      return Response.ok(transactionTemplate.execute(new TransactionCallbackGetConfig(pluginSettingsFactory))).build();
    }
    return Response.status(Response.Status.UNAUTHORIZED).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public Response putConfig(final SurveyConfigRepresentation surveyConfigRepresentation) {
    if (isAdmin()) {
      LOG.debug("setting iconSet to: " + surveyConfigRepresentation);

      transactionTemplate.execute(new TransactionCallbackSetConfig(pluginSettingsFactory, surveyConfigRepresentation));
      return Response.noContent().build();
    }
    return Response.status(Response.Status.UNAUTHORIZED).build();
  }

  private boolean isAdmin() {
    final String remoteUser = userManager.getRemoteUsername();
    return StringUtils.isNotBlank(remoteUser) && userManager.isSystemAdmin(remoteUser);
  }
}