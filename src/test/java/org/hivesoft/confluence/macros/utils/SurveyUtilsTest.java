package org.hivesoft.confluence.macros.utils;

import com.atlassian.confluence.macro.MacroExecutionException;
import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.macros.vote.UserVisualization;
import org.hivesoft.confluence.macros.vote.VoteConfig;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.macros.vote.model.Choice;
import org.hivesoft.confluence.macros.vote.model.Comment;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class SurveyUtilsTest {
  public static final String SOME_BALLOT_TITLE = "someBallotTitle";
  public static final String SOME_CHOICE_DESCRIPTION = "someChoiceDescription";
  public static final String SOME_USER_NAME = "john doe";


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
    assertTrue(SurveyUtils.getBooleanFromString("true", false));
    assertFalse(SurveyUtils.getBooleanFromString("false", true));
    assertTrue(SurveyUtils.getBooleanFromString("", true));
    assertFalse(SurveyUtils.getBooleanFromString(null, false));
  }

  @Test
  public void test_getListFromStringCommaSeparated_success() {
    final List<String> emptyList = SurveyUtils.getListFromStringCommaSeparated("");
    assertTrue(emptyList.isEmpty());
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
    // When:
    UserVisualization result = SurveyUtils.getUserVisualizationFromString(null, UserVisualization.PLAIN_LOGIN);

    // Then:
    assertEquals(UserVisualization.PLAIN_LOGIN, result);
  }

  @Test
  public void test_getUserVisualizationFromString_should_return_default_for_unkown_propertyValue() {
    // When:
    UserVisualization result = SurveyUtils.getUserVisualizationFromString("unkown", UserVisualization.LINKED_FULL);

    // Then:
    assertEquals(UserVisualization.LINKED_FULL, result);
  }

  @Test
  public void test_getUserVisualizationFromString_should_return_PLAIN_LOGIN_for_propertyValue_false() {
    // When:
    UserVisualization result = SurveyUtils.getUserVisualizationFromString("false", UserVisualization.LINKED_LOGIN);

    // Then:
    assertEquals(UserVisualization.PLAIN_LOGIN, result);
  }

  @Test
  public void test_getUserVisualizationFromString_should_return_LINKED_LOGIN_for_propertyValue_true() {
    // When:
    UserVisualization result = SurveyUtils.getUserVisualizationFromString("true", UserVisualization.PLAIN_FULL);

    // Then:
    assertEquals(UserVisualization.LINKED_LOGIN, result);
  }

  @Test
  public void test_getUserVisualizationFromString_should_return_LINKED_FULL_for_its_propertyValue() {
    // When:
    UserVisualization result = SurveyUtils.getUserVisualizationFromString("linked user name", UserVisualization.PLAIN_LOGIN);

    // Then:
    assertEquals(UserVisualization.LINKED_FULL, result);
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

  public static Ballot createDefaultBallot(String title) {
    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, title);
    return createBallotWithParameters(parameters);
  }

  public static Ballot createDefaultBallotWithChoices(String title, List<Choice> choices) {
    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, title);
    return createBallotWithParametersAndChoices(parameters, choices);
  }

  public static Ballot createDefaultBallotWithComments(String title, List<Comment> comments) {
    final HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, title);
    return new Ballot(title, "", createDefaultVoteConfig(parameters), SurveyUtils.getDefaultChoices(), comments);
  }

  public static Ballot createBallotWithParameters(Map<String, String> parameters) {
    return createBallotWithParametersAndChoices(parameters, SurveyUtils.getDefaultChoices());
  }

  public static Ballot createBallotWithParametersAndChoices(Map<String, String> parameters, List<Choice> choices) {
    String titleInMacroParameters = SurveyUtils.getTitleInMacroParameters(parameters);
    return new Ballot(titleInMacroParameters, "", createDefaultVoteConfig(parameters), choices, new ArrayList<Comment>());
  }

  public static Choice createdDefaultChoice() {
    return new Choice(SOME_CHOICE_DESCRIPTION);
  }

  public static VoteConfig createDefaultVoteConfig(Map<String, String> parameters) {
    PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);
    return new VoteConfig(mockPermissionEvaluator, parameters);
  }

  public static SurveyConfig createDefaultSurveyConfig(Map<String, String> parameters) {
    PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);
    return new SurveyConfig(mockPermissionEvaluator, parameters);
  }

  public static Map<String, String> createDefaultParametersWithTitle(String title) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, title);
    return parameters;
  }
}