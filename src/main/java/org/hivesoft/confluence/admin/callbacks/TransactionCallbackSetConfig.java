package org.hivesoft.confluence.admin.callbacks;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import org.apache.commons.lang.StringUtils;
import org.hivesoft.confluence.admin.AdminResource;
import org.hivesoft.confluence.admin.representations.Config;

public class TransactionCallbackSetConfig implements TransactionCallback {

    private PluginSettingsFactory pluginSettingsFactory;
    private Config config;

    public TransactionCallbackSetConfig(PluginSettingsFactory pluginSettingsFactory, Config config) {
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.config = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Config doInTransaction() {
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        if (StringUtils.isBlank(config.getIconSet())) {
            config.setIconSet(AdminResource.SURVEY_PLUGIN_ICON_SET_DEFAULT);
        } else {
            if (config.getIconSet().startsWith("is-")) {
                config.setIconSet(config.getIconSet().substring("is-".length()));
            }
        }
        pluginSettings.put(AdminResource.SURVEY_PLUGIN_KEY_ICON_SET, config.getIconSet());
        return config;
    }
}
