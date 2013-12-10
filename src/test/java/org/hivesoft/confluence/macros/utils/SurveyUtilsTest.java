package org.hivesoft.confluence.macros.utils;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.user.impl.DefaultUser;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.macros.vote.model.Choice;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurveyUtilsTest {
  private final static DefaultUser SOME_USER1 = new DefaultUser("someUser1", "someUser1 FullName", "some1@testmail.de");

  public static final String BALLOT_AND_CHOICENAME1 = "someBallot.withSomeChoiceName1";
  public static final String BALLOT_AND_CHOICENAME2 = "someBallot.withSomeChoiceName2";
  public static final String BALLOT_AND_CHOICENAME3 = "someBallot.withSomeChoiceName3";

  public static final String SOME_BALLOT = "some Ballot";
  public static final String SOME_CHOICE = "some Choice";
  public static final String SOME_USER_NAME = "john doe";

  ContentPropertyManager mockContentPropertyManager = mock(ContentPropertyManager.class);

  @Test
  public void test_validateMaxStorableKeyLength_success() throws MacroException {
    List<String> ballotAndChoicesWithValidLength = new ArrayList<String>();

    ballotAndChoicesWithValidLength.add(BALLOT_AND_CHOICENAME1);
    ballotAndChoicesWithValidLength.add(BALLOT_AND_CHOICENAME2);
    ballotAndChoicesWithValidLength.add(BALLOT_AND_CHOICENAME3);

    SurveyUtils.validateMaxStorableKeyLength(ballotAndChoicesWithValidLength);
  }

  @Test(expected = MacroException.class)
  public void test_validateMaxStorableKeyLength_exception() throws MacroException {
    List<String> ballotAndChoicesWithValidLength = new ArrayList<String>();

    ballotAndChoicesWithValidLength.add(getRandomString(SurveyUtils.MAX_STORABLE_KEY_LENGTH + 1));
    ballotAndChoicesWithValidLength.add(getRandomString(SurveyUtils.MAX_STORABLE_KEY_LENGTH));

    SurveyUtils.validateMaxStorableKeyLength(ballotAndChoicesWithValidLength);
  }

  @Test
  public void test_getBooleanFromString_success() {
    assertTrue(SurveyUtils.getBooleanFromString("true", false));
    assertFalse(SurveyUtils.getBooleanFromString("false", true));
    assertTrue(SurveyUtils.getBooleanFromString("", true));
    assertFalse(SurveyUtils.getBooleanFromString(null, false));
  }

  @Test
  public void test_reconstructBallot_noChoices_success() throws MacroException {
    final Ballot reconstructedBallot = SurveyUtils.reconstructBallot("someTitle", "", new Page(), mockContentPropertyManager);

    assertEquals("someTitle", reconstructedBallot.getTitle());
  }

  @Test
  public void test_reconstructBallot_someChoices_noVoter_success() throws MacroException {
    String someChoices = "someChoice1\r\nsomeChoice2";

    final Ballot reconstructedBallot = SurveyUtils.reconstructBallot("someTitle", someChoices, new Page(), mockContentPropertyManager);

    assertEquals("someTitle", reconstructedBallot.getTitle());
    assertEquals("someChoice1", reconstructedBallot.getChoice("someChoice1").getDescription());
    assertEquals("someChoice2", reconstructedBallot.getChoice("someChoice2").getDescription());
    assertFalse(reconstructedBallot.getChoice("someChoice1").getHasVotedFor(SOME_USER1.getName()));
  }

  @Test
  public void test_reconstructBallot_someChoices_withVoter_success() throws MacroException {
    String someChoices = "someChoice1\r\nsomeChoice2";

    when(mockContentPropertyManager.getTextProperty(any(ContentEntityObject.class), anyString())).thenReturn(SOME_USER1.getName());
    final Ballot reconstructedBallot = SurveyUtils.reconstructBallot("someTitle", someChoices, new Page(), mockContentPropertyManager);

    assertEquals("someTitle", reconstructedBallot.getTitle());
    assertEquals("someChoice1", reconstructedBallot.getChoice("someChoice1").getDescription());
    assertEquals("someChoice2", reconstructedBallot.getChoice("someChoice2").getDescription());
    assertTrue(reconstructedBallot.getChoice("someChoice1").getHasVotedFor(SOME_USER1.getName()));
  }

  //****** Helper Methods ******
  private static String getRandomString(int length) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < length; i++) {
      sb.append((char) ((int) (Math.random() * 26) + 97));
    }
    return sb.toString();
  }

  public static Ballot createDefaultBallot() {
    Ballot ballot = new Ballot(SOME_BALLOT);
    Choice choice = new Choice(SOME_CHOICE);
    ballot.addChoice(choice);
    return ballot;
  }
}