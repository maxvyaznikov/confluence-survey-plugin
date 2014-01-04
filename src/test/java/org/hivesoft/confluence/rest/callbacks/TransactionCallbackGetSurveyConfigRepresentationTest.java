package org.hivesoft.confluence.rest.callbacks;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.hivesoft.confluence.rest.AdminResource;
import org.hivesoft.confluence.rest.representations.SurveyConfigRepresentation;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionCallbackGetSurveyConfigRepresentationTest {

    PluginSettingsFactory mockPluginsSettingsFactory = mock(PluginSettingsFactory.class);

    private TransactionCallbackGetConfig classUnderTest;

    @Test
    public void test_doInTransaction_success() {
        when(mockPluginsSettingsFactory.createGlobalSettings()).thenReturn(new SurveyPluginSettings());

        classUnderTest = new TransactionCallbackGetConfig(mockPluginsSettingsFactory);

        final SurveyConfigRepresentation surveyConfigRepresentation = classUnderTest.doInTransaction();

        SurveyConfigRepresentation expectedSurveyConfigRepresentation = new SurveyConfigRepresentation();
        expectedSurveyConfigRepresentation.setIconSet(AdminResource.SURVEY_PLUGIN_ICON_SET_DEFAULT);

        Assert.assertEquals(expectedSurveyConfigRepresentation, surveyConfigRepresentation);
    }


}
