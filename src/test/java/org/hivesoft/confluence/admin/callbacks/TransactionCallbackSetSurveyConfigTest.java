package org.hivesoft.confluence.admin.callbacks;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.hivesoft.confluence.admin.AdminResource;
import org.hivesoft.confluence.admin.representations.SurveyConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionCallbackSetSurveyConfigTest {
    PluginSettingsFactory mockPluginsSettingsFactory = mock(PluginSettingsFactory.class);

    private TransactionCallbackSetConfig classUnderTest;

    @Test
    public void test_doInTransaction_defaultIconSet_success() {
        when(mockPluginsSettingsFactory.createGlobalSettings()).thenReturn(new SurveyPluginSettings());

        SurveyConfig surveyConfig = new SurveyConfig();
        surveyConfig.setIconSet(AdminResource.SURVEY_PLUGIN_ICON_SET_DEFAULT);

        classUnderTest = new TransactionCallbackSetConfig(mockPluginsSettingsFactory, surveyConfig);

        final SurveyConfig resultSurveyConfig = classUnderTest.doInTransaction();

        assertEquals(surveyConfig, resultSurveyConfig);
    }

    @Test
    public void test_doInTransaction_noIconSet_success() {
        when(mockPluginsSettingsFactory.createGlobalSettings()).thenReturn(new SurveyPluginSettings());

        SurveyConfig surveyConfig = new SurveyConfig();

        classUnderTest = new TransactionCallbackSetConfig(mockPluginsSettingsFactory, surveyConfig);

        final SurveyConfig resultSurveyConfig = classUnderTest.doInTransaction();

        assertEquals(surveyConfig, resultSurveyConfig);
    }
}
