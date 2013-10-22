package org.hivesoft.confluence.admin.callbacks;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.admin.AdminResource;
import org.hivesoft.confluence.admin.representations.SurveyConfig;

public class TransactionCallbackGetConfig implements com.atlassian.sal.api.transaction.TransactionCallback {

    PluginSettingsFactory pluginSettingsFactory;

    public TransactionCallbackGetConfig(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SurveyConfig doInTransaction() {
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        SurveyConfig surveyConfig = new SurveyConfig();
        surveyConfig.setIconSet((String) settings.get(AdminResource.SURVEY_PLUGIN_KEY_ICON_SET));
        if (StringUtils.isBlank(surveyConfig.getIconSet())) {
            surveyConfig.setIconSet(AdminResource.SURVEY_PLUGIN_ICON_SET_DEFAULT);
        }
        return surveyConfig;
    }
}
