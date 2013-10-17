package org.hivesoft.confluence.admin;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import org.hivesoft.confluence.admin.callbacks.TransactionCallbackGetConfig;
import org.hivesoft.confluence.admin.callbacks.TransactionCallbackSetConfig;
import org.hivesoft.confluence.admin.representations.Config;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class AdminResource {
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
        if (isUserAdmin()) return Response.status(Response.Status.UNAUTHORIZED).build();

        return Response.ok(transactionTemplate.execute(new TransactionCallbackGetConfig(pluginSettingsFactory))).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putConfig(final Config config) {
        if (isUserAdmin()) return Response.status(Response.Status.UNAUTHORIZED).build();

        transactionTemplate.execute(new TransactionCallbackSetConfig(pluginSettingsFactory, config));
        return Response.noContent().build();
    }

    private boolean isUserAdmin() {
        final String remoteUser = userManager.getRemoteUsername();
        if (remoteUser == null || !userManager.isSystemAdmin(remoteUser)) {
            return true;
        }
        return false;
    }
}