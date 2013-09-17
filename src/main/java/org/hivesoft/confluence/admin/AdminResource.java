package org.hivesoft.confluence.admin;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
        final String remoteUser = userManager.getRemoteUsername();
        if (remoteUser == null || !userManager.isSystemAdmin(remoteUser)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
                Config config = new Config();
                config.setIconSet((String) settings.get(SURVEY_PLUGIN_KEY_ICON_SET));
                if (StringUtils.isBlank(config.getIconSet())) {
                    config.setIconSet(SURVEY_PLUGIN_ICON_SET_DEFAULT);
                }
                return config;
            }
        })).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putConfig(final Config config) {
        final String remoteUser = userManager.getRemoteUsername();
        if (remoteUser == null || !userManager.isSystemAdmin(remoteUser)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
                if (StringUtils.isBlank(config.getIconSet())) {
                    config.setIconSet(SURVEY_PLUGIN_ICON_SET_DEFAULT);
                } else {
                    if (config.getIconSet().startsWith("is-")) {
                        config.setIconSet(config.getIconSet().substring("is-".length()));
                    }
                }
                pluginSettings.put(SURVEY_PLUGIN_KEY_ICON_SET, config.getIconSet());
                return null;
            }
        });
        return Response.noContent().build();
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class Config {

        @XmlElement
        private String iconSet;

        public String getIconSet() {
            return iconSet;
        }

        public void setIconSet(String iconSet) {
            this.iconSet = iconSet;
        }
    }
}