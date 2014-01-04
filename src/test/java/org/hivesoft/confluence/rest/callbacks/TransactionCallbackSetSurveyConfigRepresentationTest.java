package org.hivesoft.confluence.rest.callbacks;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.hivesoft.confluence.rest.AdminResource;
import org.hivesoft.confluence.rest.representations.SurveyConfigRepresentation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionCallbackSetSurveyConfigRepresentationTest {
    PluginSettingsFactory mockPluginsSettingsFactory = mock(PluginSettingsFactory.class);

    private TransactionCallbackSetConfig classUnderTest;

    @Test
    public void test_doInTransaction_defaultIconSet_success() {
        when(mockPluginsSettingsFactory.createGlobalSettings()).thenReturn(new SurveyPluginSettings());

        SurveyConfigRepresentation surveyConfigRepresentation = new SurveyConfigRepresentation();
        surveyConfigRepresentation.setIconSet(AdminResource.SURVEY_PLUGIN_ICON_SET_DEFAULT);

        classUnderTest = new TransactionCallbackSetConfig(mockPluginsSettingsFactory, surveyConfigRepresentation);

        final SurveyConfigRepresentation resultSurveyConfigRepresentation = classUnderTest.doInTransaction();

        assertEquals(surveyConfigRepresentation, resultSurveyConfigRepresentation);
    }

    @Test
    public void test_doInTransaction_noIconSet_success() {
        when(mockPluginsSettingsFactory.createGlobalSettings()).thenReturn(new SurveyPluginSettings());

        SurveyConfigRepresentation surveyConfigRepresentation = new SurveyConfigRepresentation();

        classUnderTest = new TransactionCallbackSetConfig(mockPluginsSettingsFactory, surveyConfigRepresentation);

        final SurveyConfigRepresentation resultSurveyConfigRepresentation = classUnderTest.doInTransaction();

        assertEquals(surveyConfigRepresentation, resultSurveyConfigRepresentation);
    }
}
