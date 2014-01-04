package org.hivesoft.confluence.macros.survey;

import org.hivesoft.confluence.macros.survey.model.SurveySummary;
import org.hivesoft.confluence.macros.utils.PermissionEvaluator;
import org.hivesoft.confluence.macros.vote.VoteConfig;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class SurveyConfigTest {


  SurveyConfig classUnderTest;

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
    classUnderTest = createSurveyConfigWithSurveySummaryTop(SurveySummary.Top);
    assertEquals(SurveySummary.Top, classUnderTest.getSurveySummary());
    classUnderTest = createSurveyConfigWithSurveySummaryTop(SurveySummary.None);
    assertEquals(SurveySummary.None, classUnderTest.getSurveySummary());
    classUnderTest = createSurveyConfigWithSurveySummaryTop(SurveySummary.Bottom);
    assertEquals(SurveySummary.Bottom, classUnderTest.getSurveySummary());
  }

  private static SurveyConfig createSurveyConfigWithRenderTitleLevel(int level) {
    PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(SurveyConfig.KEY_RENDER_TITLE_LEVEL, String.valueOf(level));
    return new SurveyConfig(mockPermissionEvaluator, parameters);
  }

  private static SurveyConfig createSurveyConfigWithSurveySummaryTop(SurveySummary summary) {
    PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);

    Map<String, String> parameters = new HashMap<String, String>();
    if (summary == SurveySummary.None) {
      parameters.put(SurveyConfig.KEY_SHOW_SUMMARY, String.valueOf(false));
    } else if (summary == SurveySummary.Bottom) {
      parameters.put(SurveyConfig.KEY_SHOW_SUMMARY, String.valueOf(true));
    } else {
      parameters.put(SurveyConfig.KEY_SHOW_SUMMARY, String.valueOf(true));
      parameters.put(SurveyConfig.KEY_SHOW_LAST, String.valueOf(true));
    }
    return new SurveyConfig(mockPermissionEvaluator, parameters);
  }
}
