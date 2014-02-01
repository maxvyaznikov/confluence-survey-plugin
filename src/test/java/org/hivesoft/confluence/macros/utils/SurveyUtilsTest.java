package org.hivesoft.confluence.macros.utils;

import com.atlassian.renderer.v2.macro.MacroException;
import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.macros.vote.VoteConfig;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.macros.vote.model.Choice;
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
  public static final String BALLOT_AND_CHOICE_NAME1 = "someBallot.withSomeChoiceName1";
  public static final String BALLOT_AND_CHOICE_NAME2 = "someBallot.withSomeChoiceName2";
  public static final String BALLOT_AND_CHOICE_NAME3 = "someBallot.withSomeChoiceName3";

  public static final String SOME_BALLOT_TITLE = "someBallotTitle";
  public static final String SOME_CHOICE_DESCRIPTION = "someChoiceDescription";
  public static final String SOME_USER_NAME = "john doe";


  @Test
  public void test_validateMaxStorableKeyLength_success() throws MacroException {
    List<String> ballotAndChoicesWithValidLength = new ArrayList<String>();

    ballotAndChoicesWithValidLength.add(BALLOT_AND_CHOICE_NAME1);
    ballotAndChoicesWithValidLength.add(BALLOT_AND_CHOICE_NAME2);
    ballotAndChoicesWithValidLength.add(BALLOT_AND_CHOICE_NAME3);

    final List<String> violatingMaxStorableKeyLengthItems = SurveyUtils.getViolatingMaxStorableKeyLengthItems(ballotAndChoicesWithValidLength);
    assertThat(violatingMaxStorableKeyLengthItems.size(), is(equalTo(0)));
  }

  @Test
  public void test_validateMaxStorableKeyLength_failure() throws MacroException {
    List<String> ballotAndChoicesWithValidLength = new ArrayList<String>();

    ballotAndChoicesWithValidLength.add(getRandomString(SurveyUtils.MAX_STORABLE_KEY_LENGTH + 1));
    ballotAndChoicesWithValidLength.add(getRandomString(SurveyUtils.MAX_STORABLE_KEY_LENGTH / 2));

    final List<String> violatingMaxStorableKeyLengthItems = SurveyUtils.getViolatingMaxStorableKeyLengthItems(ballotAndChoicesWithValidLength);

    System.out.println(violatingMaxStorableKeyLengthItems);
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


  //****** Helper Methods ******
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

  public static Ballot createBallotWithParameters(Map<String, String> parameters) {
    String titleInMacroParameters = SOME_BALLOT_TITLE;
    try {
      titleInMacroParameters = SurveyUtils.getTitleInMacroParameters(parameters);
    } catch (MacroException e) {
      e.printStackTrace();
    }
    Ballot ballot = new Ballot(titleInMacroParameters, createDefaultVoteConfig(parameters));
    //Choice choice = new Choice(SOME_CHOICE_DESCRIPTION);
    //ballot.addChoice(choice);
    return ballot;
  }

  public static Choice createdDefaultChoice() {
    return new Choice(SOME_CHOICE_DESCRIPTION);
  }

  public static VoteConfig createDefaultVoteConfig(Map<String, String> parameters) {
    PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);
    VoteConfig voteConfig = new VoteConfig(mockPermissionEvaluator, parameters);
    return voteConfig;
  }

  public static SurveyConfig createDefaultSurveyConfig(Map<String, String> parameters) {
    PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);
    SurveyConfig surveyConfig = new SurveyConfig(mockPermissionEvaluator, parameters);
    return surveyConfig;
  }

  public static Map<String, String> createDefaultParametersWithTitle(String title) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(VoteConfig.KEY_TITLE, title);
    return parameters;
  }
}