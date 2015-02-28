package org.hivesoft.confluence.macros.survey;

import org.hivesoft.confluence.macros.vote.VoteConfig;
import org.hivesoft.confluence.model.SurveySummary;
import org.hivesoft.confluence.model.enums.UserVisualization;
import org.hivesoft.confluence.utils.PermissionEvaluatorImpl;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class SurveyConfigTest {

  private SurveyConfig classUnderTest;

  @Test
  public void test_createWithDefaultParameters_success() {
    PermissionEvaluatorImpl mockPermissionEvaluator = mock(PermissionEvaluatorImpl.class);
    Map<String, String> parameters = new HashMap<String, String>();
    classUnderTest = new SurveyConfig(mockPermissionEvaluator, parameters);

    assertThat(classUnderTest.getRenderTitleLevel(), is(equalTo(2)));
    assertThat(classUnderTest.getChoices().size(), is(equalTo(0)));
    assertThat(classUnderTest.isChangeableVotes(), is(equalTo(false)));
    assertThat(classUnderTest.getSurveySummary(), is(equalTo(SurveySummary.Top)));
    assertThat(classUnderTest.isShowComments(), is(equalTo(true)));
    assertThat(classUnderTest.getStartBound(), is(equalTo(1)));
    assertThat(classUnderTest.getIterateStep(), is(equalTo(1)));
    assertThat(classUnderTest.getVoters().size(), is(equalTo(0)));
    assertThat(classUnderTest.getViewers().size(), is(equalTo(0)));
    assertThat(classUnderTest.isVisibleVoters(), is(equalTo(false)));
    assertThat(classUnderTest.isLocked(), is(equalTo(false)));
    assertThat(classUnderTest.isShowCondensed(), is(equalTo(false)));
    assertThat(classUnderTest.getUserRenderer(), is(notNullValue()));
    assertThat(classUnderTest.getUserRenderer().getUserVisualization(), is(equalTo(UserVisualization.PLAIN_LOGIN)));
  }

  @Test
  public void test_renderTitleLevel_success() {

    classUnderTest = createSurveyConfigWithRenderTitleLevel(2);

    assertEquals(2, classUnderTest.getRenderTitleLevel());
    assertEquals(3, new VoteConfig(classUnderTest).getRenderTitleLevel());
    assertEquals(3, classUnderTest.getRenderTitleLevelAdjustedOrZero(1));

    classUnderTest = createSurveyConfigWithRenderTitleLevel(0);

    assertEquals(0, classUnderTest.getRenderTitleLevel());
    assertEquals(0, new VoteConfig(classUnderTest).getRenderTitleLevel());
    assertEquals(0, classUnderTest.getRenderTitleLevelAdjustedOrZero(1));

    classUnderTest = createSurveyConfigWithRenderTitleLevel(3);

    assertEquals(3, classUnderTest.getRenderTitleLevel());
    assertEquals(4, new VoteConfig(classUnderTest).getRenderTitleLevel());
    assertEquals(4, classUnderTest.getRenderTitleLevelAdjustedOrZero(1));
  }

  @Test
  public void test_TitleAndSummary_success() {
    classUnderTest = createSurveyConfigWithSurveySummary(SurveySummary.Top);
    assertThat(classUnderTest.getSurveySummary(), is(SurveySummary.Top));
    classUnderTest = createSurveyConfigWithSurveySummary(SurveySummary.None);
    assertThat(classUnderTest.getSurveySummary(), is(SurveySummary.None));
    classUnderTest = createSurveyConfigWithSurveySummary(SurveySummary.Bottom);
    assertThat(classUnderTest.getSurveySummary(), is(SurveySummary.Bottom));
  }

  @Test
  public void test_migrateParameters() {
    Map<String, String> migratedParameters = SurveyConfig.migrateParameters(createOldSurveySummaryParameters(SurveySummary.Top));
    assertThat(migratedParameters, is(createSurveySummaryParameters(SurveySummary.Top)));
    migratedParameters = SurveyConfig.migrateParameters(createOldSurveySummaryParameters(SurveySummary.Bottom));
    assertThat(migratedParameters, is(createSurveySummaryParameters(SurveySummary.Bottom)));
    migratedParameters = SurveyConfig.migrateParameters(createOldSurveySummaryParameters(SurveySummary.None));
    assertThat(migratedParameters, is(createSurveySummaryParameters(SurveySummary.None)));
  }

  private static SurveyConfig createSurveyConfigWithRenderTitleLevel(int level) {
    PermissionEvaluatorImpl mockPermissionEvaluator = mock(PermissionEvaluatorImpl.class);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(SurveyConfig.KEY_RENDER_TITLE_LEVEL, String.valueOf(level));
    return new SurveyConfig(mockPermissionEvaluator, parameters);
  }

  private static SurveyConfig createSurveyConfigWithSurveySummary(SurveySummary surveySummary) {
    PermissionEvaluatorImpl mockPermissionEvaluator = mock(PermissionEvaluatorImpl.class);

    return new SurveyConfig(mockPermissionEvaluator, createSurveySummaryParameters(surveySummary));
  }

  private static Map<String, String> createSurveySummaryParameters(SurveySummary surveySummary) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(SurveyConfig.KEY_SHOW_SUMMARY, surveySummary.name());
    return parameters;
  }

  private static Map<String, String> createOldSurveySummaryParameters(SurveySummary summary) {
    Map<String, String> parameters = new HashMap<String, String>();
    if (summary == SurveySummary.None) {
      parameters.put(SurveyConfig.KEY_SHOW_SUMMARY, String.valueOf(false));
    } else if (summary == SurveySummary.Bottom) {
      parameters.put(SurveyConfig.KEY_SHOW_LAST, String.valueOf(true));
    } else {
      parameters.put(SurveyConfig.KEY_SHOW_SUMMARY, String.valueOf(true));
      parameters.put(SurveyConfig.KEY_SHOW_LAST, String.valueOf(false));
    }
    return parameters;
  }
}
