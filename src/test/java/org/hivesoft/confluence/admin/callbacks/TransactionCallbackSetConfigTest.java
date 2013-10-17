package org.hivesoft.confluence.admin.callbacks;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.hivesoft.confluence.admin.AdminResource;
import org.hivesoft.confluence.admin.representations.Config;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionCallbackSetConfigTest {
    PluginSettingsFactory mockPluginsSettingsFactory = mock(PluginSettingsFactory.class);

    private TransactionCallbackSetConfig classUnderTest;

    @Test
    public void test_doInTransaction_defaultIconSet_success() {
        when(mockPluginsSettingsFactory.createGlobalSettings()).thenReturn(new SurveyPluginSettings());

        Config config = new Config();
        config.setIconSet(AdminResource.SURVEY_PLUGIN_ICON_SET_DEFAULT);

        classUnderTest = new TransactionCallbackSetConfig(mockPluginsSettingsFactory, config);

        final Config resultConfig = classUnderTest.doInTransaction();

        assertEquals(config, resultConfig);
    }

    @Test
    public void test_doInTransaction_noIconSet_success() {
        when(mockPluginsSettingsFactory.createGlobalSettings()).thenReturn(new SurveyPluginSettings());

        Config config = new Config();

        classUnderTest = new TransactionCallbackSetConfig(mockPluginsSettingsFactory, config);

        final Config resultConfig = classUnderTest.doInTransaction();

        assertEquals(config, resultConfig);
    }
}
