package org.hivesoft.confluence.rest.callbacks;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import org.apache.commons.lang.StringUtils;
import org.hivesoft.confluence.rest.AdminResource;
import org.hivesoft.confluence.rest.representations.SurveyConfig;

public class TransactionCallbackSetConfig implements TransactionCallback {

    private PluginSettingsFactory pluginSettingsFactory;
    private SurveyConfig surveyConfig;

    public TransactionCallbackSetConfig(PluginSettingsFactory pluginSettingsFactory, SurveyConfig surveyConfig) {
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.surveyConfig = surveyConfig;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SurveyConfig doInTransaction() {
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        if (StringUtils.isBlank(surveyConfig.getIconSet())) {
            surveyConfig.setIconSet(AdminResource.SURVEY_PLUGIN_ICON_SET_DEFAULT);
        } else {
            if (surveyConfig.getIconSet().startsWith("is-")) {
                surveyConfig.setIconSet(surveyConfig.getIconSet().substring("is-".length()));
            }
        }
        pluginSettings.put(AdminResource.SURVEY_PLUGIN_KEY_ICON_SET, surveyConfig.getIconSet());
        return surveyConfig;
    }
}
