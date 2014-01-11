package org.hivesoft.confluence.macros.utils;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.user.impl.DefaultUser;
import org.hivesoft.confluence.macros.survey.model.Survey;
import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurveyManagerTest {
  private final static DefaultUser SOME_USER1 = new DefaultUser("someUser1", "someUser1 FullName", "some1@testmail.de");

  ContentPropertyManager mockContentPropertyManager = mock(ContentPropertyManager.class);
  PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);

  SurveyManager classUnderTest;

  @Before
  public void setup() {
    classUnderTest = new SurveyManager(mockContentPropertyManager, mockPermissionEvaluator);
  }

  @Test
  public void test_reconstructBallot_noChoices_success() throws MacroException {
    final Ballot reconstructedBallot = classUnderTest.reconstructBallot(SurveyUtilsTest.createDefaultParametersWithTitle("someTitle"), "", new Page());

    assertEquals("someTitle", reconstructedBallot.getTitle());
  }

  @Test
  public void test_reconstructBallot_someChoices_noVoter_success() throws MacroException {
    String someChoices = "someChoice1\r\nsomeChoice2";

    final Ballot reconstructedBallot = classUnderTest.reconstructBallot(SurveyUtilsTest.createDefaultParametersWithTitle("someTitle"), someChoices, new Page());

    assertEquals("someTitle", reconstructedBallot.getTitle());
    assertEquals("someChoice1", reconstructedBallot.getChoice("someChoice1").getDescription());
    assertEquals("someChoice2", reconstructedBallot.getChoice("someChoice2").getDescription());
    assertFalse(reconstructedBallot.getChoice("someChoice1").getHasVotedFor(SOME_USER1.getName()));
  }

  @Test
  public void test_reconstructBallot_someChoices_withVoter_success() throws MacroException {
    String someChoices = "someChoice1\r\nsomeChoice2";

    when(mockContentPropertyManager.getTextProperty(any(ContentEntityObject.class), anyString())).thenReturn(SOME_USER1.getName());
    final Ballot reconstructedBallot = classUnderTest.reconstructBallot(SurveyUtilsTest.createDefaultParametersWithTitle("someTitle"), someChoices, new Page());

    assertEquals("someTitle", reconstructedBallot.getTitle());
    assertEquals("someChoice1", reconstructedBallot.getChoice("someChoice1").getDescription());
    assertEquals("someChoice2", reconstructedBallot.getChoice("someChoice2").getDescription());
    assertTrue(reconstructedBallot.getChoice("someChoice1").getHasVotedFor(SOME_USER1.getName()));
  }

  @Test
  public void test_createSurvey_noParametersWithTitle_success() {
    final Survey returnedSurvey = classUnderTest.createSurvey("", new Page(), SurveyUtilsTest.createDefaultParametersWithTitle("someTitle"));

    assertEquals(0, returnedSurvey.getBallots().size());
    assertThat(returnedSurvey.getTitle(), is(equalTo("someTitle")));
  }

  @Test
  public void test_createSurvey_oneParameterDefaultChoices_success() {
    final String someBallotTitle1 = "someBallotTitle1";
    final Survey returnedSurvey = classUnderTest.createSurvey(someBallotTitle1, new Page(), new HashMap<String, String>());

    assertEquals(someBallotTitle1, returnedSurvey.getBallot(someBallotTitle1).getTitle());
    assertThat(returnedSurvey.getBallot(someBallotTitle1).getChoices().size(), is(5));
  }

  @Test
  public void test_createSurvey_oneParameterCustomChoices_success() {
    final String someBallotTitle1 = "someBallotTitle1";
    final Survey returnedSurvey = classUnderTest.createSurvey(someBallotTitle1 + " - subTitle - choice1 - choice2", new Page(), new HashMap<String, String>());

    assertEquals(someBallotTitle1, returnedSurvey.getBallot(someBallotTitle1).getTitle());
    assertThat(returnedSurvey.getBallot(someBallotTitle1).getChoices().size(), is(2));
    assertThat(returnedSurvey.getBallot(someBallotTitle1).getChoices().iterator().next().getDescription(), is(equalTo("choice1")));
  }

  @Test
  public void test_createSurvey_twoParameters_success() {
    final String someBallotTitle1 = "someBallotTitle1";
    final String someBallotTitle2 = "someBallotTitle2";
    final String someBallotDescription1 = "someBallotDescription1";
    final Survey returnedSurvey = classUnderTest.createSurvey(someBallotTitle1 + " - " + someBallotDescription1 + "\r\n" + someBallotTitle2, new Page(), SurveyUtilsTest.createDefaultParametersWithTitle("someTitle"));

    assertEquals(someBallotTitle1, returnedSurvey.getBallot(someBallotTitle1).getTitle());
    assertEquals(someBallotDescription1, returnedSurvey.getBallot(someBallotTitle1).getDescription());
    assertEquals(someBallotTitle2, returnedSurvey.getBallot(someBallotTitle2).getTitle());
    assertThat(returnedSurvey.getBallot(someBallotTitle2).getChoices().size(), is(5));
  }

  @Test
  public void test_createSurvey_twoParametersWithCommenter_success() {
    final String someBallotTitle1 = "someBallotTitle1";
    final String someBallotTitle2 = "someBallotTitle2";

    final Page somePage = new Page();

    when(mockContentPropertyManager.getTextProperty(somePage, "survey." + someBallotTitle1 + ".commenters")).thenReturn(SOME_USER1.getName());
    when(mockContentPropertyManager.getTextProperty(somePage, "survey." + someBallotTitle1 + ".comment." + SOME_USER1.getName())).thenReturn("someComment");

    final Survey returnedSurvey = classUnderTest.createSurvey(someBallotTitle1 + "\r\n" + someBallotTitle2, somePage, SurveyUtilsTest.createDefaultParametersWithTitle("someTitle"));

    assertEquals(someBallotTitle1, returnedSurvey.getBallot(someBallotTitle1).getTitle());
    assertEquals(someBallotTitle2, returnedSurvey.getBallot(someBallotTitle2).getTitle());
    assertEquals("someComment", returnedSurvey.getBallot(someBallotTitle1).getCommentForUser(SOME_USER1.getName()).getComment());
  }
}
