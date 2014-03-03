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

public class TransactionCallbackSetConfigTest {
  PluginSettingsFactory mockPluginsSettingsFactory = mock(PluginSettingsFactory.class);

  private TransactionCallbackSetConfig classUnderTest;

  @Test
  public void test_doInTransaction_defaultIconSet_success() {
    when(mockPluginsSettingsFactory.createGlobalSettings()).thenReturn(new SurveyPluginSettings());

    SurveyConfigRepresentation surveyConfigRepresentation = new SurveyConfigRepresentation();
    surveyConfigRepresentation.setIconSet(AdminResource.SURVEY_PLUGIN_ICON_SET_DEFAULT);

    classUnderTest = new TransactionCallbackSetConfig(mockPluginsSettingsFactory, surveyConfigRepresentation);

    final SurveyConfigRepresentation resultSurveyConfigRepresentation = classUnderTest.doInTransaction();

    assertThat(surveyConfigRepresentation, is(resultSurveyConfigRepresentation));
  }

  @Test
  public void test_doInTransaction_noIconSet_success() {
    when(mockPluginsSettingsFactory.createGlobalSettings()).thenReturn(new SurveyPluginSettings());

    SurveyConfigRepresentation surveyConfigRepresentation = new SurveyConfigRepresentation();

    classUnderTest = new TransactionCallbackSetConfig(mockPluginsSettingsFactory, surveyConfigRepresentation);

    final SurveyConfigRepresentation resultSurveyConfigRepresentation = classUnderTest.doInTransaction();

    assertThat(surveyConfigRepresentation, is(resultSurveyConfigRepresentation));
  }

  @Test
  public void test_doInTransaction_customIconSet_success() {
    when(mockPluginsSettingsFactory.createGlobalSettings()).thenReturn(new SurveyPluginSettings());

    SurveyConfigRepresentation surveyConfigRepresentation = new SurveyConfigRepresentation();
    surveyConfigRepresentation.setIconSet("is-customIconSet");

    classUnderTest = new TransactionCallbackSetConfig(mockPluginsSettingsFactory, surveyConfigRepresentation);

    final SurveyConfigRepresentation resultSurveyConfigRepresentation = classUnderTest.doInTransaction();

    assertThat(surveyConfigRepresentation, is(resultSurveyConfigRepresentation));
  }
}
