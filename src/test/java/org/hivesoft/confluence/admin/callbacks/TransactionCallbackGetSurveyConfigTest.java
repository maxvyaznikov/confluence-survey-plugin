package org.hivesoft.confluence.admin.callbacks;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.hivesoft.confluence.admin.AdminResource;
import org.hivesoft.confluence.admin.representations.SurveyConfig;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionCallbackGetSurveyConfigTest {

    PluginSettingsFactory mockPluginsSettingsFactory = mock(PluginSettingsFactory.class);

    private TransactionCallbackGetConfig classUnderTest;

    @Test
    public void test_doInTransaction_success() {
        when(mockPluginsSettingsFactory.createGlobalSettings()).thenReturn(new SurveyPluginSettings());

        classUnderTest = new TransactionCallbackGetConfig(mockPluginsSettingsFactory);

        final SurveyConfig surveyConfig = classUnderTest.doInTransaction();

        SurveyConfig expectedSurveyConfig = new SurveyConfig();
        expectedSurveyConfig.setIconSet(AdminResource.SURVEY_PLUGIN_ICON_SET_DEFAULT);

        Assert.assertEquals(expectedSurveyConfig, surveyConfig);
    }


}
