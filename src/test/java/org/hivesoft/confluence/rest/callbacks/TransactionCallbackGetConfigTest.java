package org.hivesoft.confluence.rest.callbacks;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.hivesoft.confluence.rest.AdminResource;
import org.hivesoft.confluence.rest.callbacks.delegation.SurveyPluginSettings;
import org.hivesoft.confluence.rest.representations.SurveyConfigRepresentation;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionCallbackGetConfigTest {

  PluginSettingsFactory mockPluginsSettingsFactory = mock(PluginSettingsFactory.class);

  private TransactionCallbackGetConfig classUnderTest;

  @Test
  public void test_doInTransaction_emptyDefaultSettings_success() {
    when(mockPluginsSettingsFactory.createGlobalSettings()).thenReturn(new SurveyPluginSettings());

    classUnderTest = new TransactionCallbackGetConfig(mockPluginsSettingsFactory);

    SurveyConfigRepresentation expectedSurveyConfigRepresentation = new SurveyConfigRepresentation();
    expectedSurveyConfigRepresentation.setIconSet(AdminResource.SURVEY_PLUGIN_ICON_SET_DEFAULT);

    final SurveyConfigRepresentation surveyConfigRepresentation = classUnderTest.doInTransaction();

    assertThat(expectedSurveyConfigRepresentation, is(surveyConfigRepresentation));
  }


  @Test
  public void test_doInTransaction_existentPluginSettings_success() {
    final SurveyPluginSettings surveyPluginSettings = new SurveyPluginSettings();
    surveyPluginSettings.put(AdminResource.SURVEY_PLUGIN_KEY_ICON_SET, "someIconSet");
    when(mockPluginsSettingsFactory.createGlobalSettings()).thenReturn(surveyPluginSettings);

    classUnderTest = new TransactionCallbackGetConfig(mockPluginsSettingsFactory);

    final SurveyConfigRepresentation surveyConfigRepresentation = classUnderTest.doInTransaction();

    SurveyConfigRepresentation expectedSurveyConfigRepresentation = new SurveyConfigRepresentation();
    expectedSurveyConfigRepresentation.setIconSet("someIconSet");

    assertThat(expectedSurveyConfigRepresentation, is(surveyConfigRepresentation));
  }
}
