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
package org.hivesoft.confluence.rest.callbacks;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.apache.commons.lang3.StringUtils;
import org.hivesoft.confluence.rest.AdminResource;
import org.hivesoft.confluence.rest.representations.SurveyConfigRepresentation;

public class TransactionCallbackGetConfig implements com.atlassian.sal.api.transaction.TransactionCallback {

    PluginSettingsFactory pluginSettingsFactory;

    public TransactionCallbackGetConfig(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SurveyConfigRepresentation doInTransaction() {
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        SurveyConfigRepresentation surveyConfigRepresentation = new SurveyConfigRepresentation();
        surveyConfigRepresentation.setIconSet((String) settings.get(AdminResource.SURVEY_PLUGIN_KEY_ICON_SET));
        if (StringUtils.isBlank(surveyConfigRepresentation.getIconSet())) {
            surveyConfigRepresentation.setIconSet(AdminResource.SURVEY_PLUGIN_ICON_SET_DEFAULT);
        }
        return surveyConfigRepresentation;
    }
}
