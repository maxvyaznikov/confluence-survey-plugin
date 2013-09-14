package org.hivesoft.confluence.admin;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Path("/")
public class AdminResource {
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
    public Response get(@Context HttpServletRequest request) {
        final UserProfile remoteUser = userManager.getRemoteUser();
        if (remoteUser == null || !userManager.isSystemAdmin(remoteUser.getUserKey())) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
                Config config = new Config();
                config.setName((String) settings.get(Config.class.getName() + ".name"));

                String time = (String) settings.get(Config.class.getName() + ".time");
                if (time != null) {
                    config.setTime(Integer.parseInt(time));
                }
                return config;
            }
        })).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final Config config, @Context HttpServletRequest request) {
        final UserProfile remoteUser = userManager.getRemoteUser();
        if (remoteUser == null || !userManager.isSystemAdmin(remoteUser.getUserKey())) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
                pluginSettings.put(Config.class.getName() + ".name", config.getName());
                pluginSettings.put(Config.class.getName() + ".time", Integer.toString(config.getTime()));
                return null;
            }
        });
        return Response.noContent().build();
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class Config {

        @XmlElement
        private String name;
        @XmlElement
        private int time;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }
    }
}