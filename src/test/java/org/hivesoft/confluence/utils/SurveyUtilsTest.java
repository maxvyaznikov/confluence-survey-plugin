package org.hivesoft.confluence.utils;

import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.hivesoft.confluence.macros.ConfluenceTestBase;
import org.hivesoft.confluence.model.enums.UserVisualization;
import org.hivesoft.confluence.rest.AdminResource;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurveyUtilsTest extends ConfluenceTestBase {

  @Test
  public void test_validateMaxStorableKeyLength_success() throws MacroExecutionException {
    List<String> ballotAndChoicesWithValidLength = listOfBallotsWithValidKeyLength(3);

    final List<String> violatingMaxStorableKeyLengthItems = SurveyUtils.getViolatingMaxStorableKeyLengthItems(ballotAndChoicesWithValidLength);

    assertThat(violatingMaxStorableKeyLengthItems.size(), is(equalTo(0)));
  }

  @Test
  public void test_validateMaxStorableKeyLength_failure() throws MacroExecutionException {
    List<String> ballotAndChoicesWithInValidLength = listOfBallotsWithValidKeyLength(1);

    ballotAndChoicesWithInValidLength.add(getRandomString(SurveyUtils.MAX_STORABLE_KEY_LENGTH + 1));

    final List<String> violatingMaxStorableKeyLengthItems = SurveyUtils.getViolatingMaxStorableKeyLengthItems(ballotAndChoicesWithInValidLength);

    assertThat(violatingMaxStorableKeyLengthItems.size(), is(equalTo(1)));
  }

  @Test
  public void test_getBooleanFromString_success() {
    assertThat(SurveyUtils.getBooleanFromString("true", false), is(true));
    assertThat(SurveyUtils.getBooleanFromString("false", true), is(false));
    assertThat(SurveyUtils.getBooleanFromString("", true), is(true));
    assertThat(SurveyUtils.getBooleanFromString(null, false), is(false));
  }

  @Test
  public void test_getListFromStringCommaSeparated_success() {
    final List<String> emptyList = SurveyUtils.getListFromStringCommaSeparated("");
    assertThat(emptyList.isEmpty(), is(true));
    final List<String> oneElement = SurveyUtils.getListFromStringCommaSeparated("User1 User2");
    assertThat(oneElement, hasItem("User1 User2"));
    final List<String> twoElements = SurveyUtils.getListFromStringCommaSeparated("User1, User2");
    assertThat(twoElements, hasItems("User1", "User2"));
  }

  @Test
  public void test_getDescriptionWithRenderedLinks() {
    assertThat(SurveyUtils.enrichStringWithHttpPattern("i am a choice to http://google.de"), is("i am a choice to <a href=\"http://google.de\" target=\"_blank\">http://google.de</a>"));
    assertThat(SurveyUtils.enrichStringWithHttpPattern("i am a choice to http://google.de but https://www.google.com is also ok"),
            is("i am a choice to <a href=\"http://google.de\" target=\"_blank\">http://google.de</a> but <a href=\"https://www.google.com\" target=\"_blank\">https://www.google.com</a> is also ok"));
    assertThat(SurveyUtils.enrichStringWithHttpPattern("no link here"), is("no link here"));
    assertThat(SurveyUtils.enrichStringWithHttpPattern("<a href=\"#\">i am a tag</a> that's not valid but http://google.com is"), is("&lt;a href=&quot;#&quot;&gt;i am a tag&lt;/a&gt; that's not valid but <a href=\"http://google.com\" target=\"_blank\">http://google.com</a> is"));
  }

  @Test
  public void test_getUserVisualizationFromString_should_return_default_for_null() {
    UserVisualization result = SurveyUtils.getUserVisualizationFromString(null, UserVisualization.PLAIN_LOGIN);

    assertThat(result, is(UserVisualization.PLAIN_LOGIN));
  }

  @Test
  public void test_getUserVisualizationFromString_should_return_default_for_unknown_propertyValue() {
    UserVisualization result = SurveyUtils.getUserVisualizationFromString("unknown", UserVisualization.LINKED_FULL);

    assertThat(result, is(UserVisualization.LINKED_FULL));
  }

  @Test
  public void test_getUserVisualizationFromString_should_return_LINKED_FULL_for_its_propertyValue() {
    UserVisualization result = SurveyUtils.getUserVisualizationFromString("linked user name", UserVisualization.PLAIN_LOGIN);

    assertThat(result, is(UserVisualization.LINKED_FULL));
  }

  @Test
  public void test_getIconSetFromPluginSettings_noSettingsFound_success(){
    PluginSettingsFactory mockPluginSettingsFactory = mock(PluginSettingsFactory.class);

    when(mockPluginSettingsFactory.createGlobalSettings()).thenReturn(new PluginSettings() {
      @Override
      public Object get(String key) {
        return null;
      }

      @Override
      public Object put(String key, Object value) {
        return null;
      }

      @Override
      public Object remove(String key) {
        return null;
      }
    });

    String result = SurveyUtils.getIconSetFromPluginSettings(mockPluginSettingsFactory);

    assertThat(result, is(AdminResource.SURVEY_PLUGIN_ICON_SET_DEFAULT));
  }

  @Test
  public void test_getIconSetFromPluginSettings_settingsFound_success(){
    PluginSettingsFactory mockPluginSettingsFactory = mock(PluginSettingsFactory.class);

    when(mockPluginSettingsFactory.createGlobalSettings()).thenReturn(new PluginSettings() {
      @Override
      public Object get(String key) {
        return "aDifferentIconSet";
      }

      @Override
      public Object put(String key, Object value) {
        return null;
      }

      @Override
      public Object remove(String key) {
        return null;
      }
    });

    String result = SurveyUtils.getIconSetFromPluginSettings(mockPluginSettingsFactory);

    assertThat(result, is("aDifferentIconSet"));
  }

  //****** Helper Methods ******

  private List<String> listOfBallotsWithValidKeyLength(int count) {
    List<String> ballotAndChoiceNames = new ArrayList<String>();

    for (int i = 0; i < count; i++) {
      ballotAndChoiceNames.add("someBallot.withSomeChoiceName" + i);
    }
    return ballotAndChoiceNames;
  }

  private static String getRandomString(int length) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < length; i++) {
      sb.append((char) ((int) (Math.random() * 26) + 97));
    }
    return sb.toString();
  }
}