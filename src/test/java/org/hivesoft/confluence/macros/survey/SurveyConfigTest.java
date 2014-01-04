package org.hivesoft.confluence.macros.survey;

import org.hivesoft.confluence.macros.survey.model.SurveySummary;
import org.hivesoft.confluence.macros.utils.PermissionEvaluator;
import org.hivesoft.confluence.macros.vote.VoteConfig;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    classUnderTest = createSurveyConfigWithSurveySummary(SurveySummary.Top);
    assertEquals(SurveySummary.Top, classUnderTest.getSurveySummary());
    classUnderTest = createSurveyConfigWithSurveySummary(SurveySummary.None);
    assertEquals(SurveySummary.None, classUnderTest.getSurveySummary());
    classUnderTest = createSurveyConfigWithSurveySummary(SurveySummary.Bottom);
    assertEquals(SurveySummary.Bottom, classUnderTest.getSurveySummary());
  }

  @Test
  public void test_equalsAndHashCode() {
    classUnderTest = createSurveyConfigWithSurveySummary(SurveySummary.Top);
    VoteConfig classUnderTest2 = createSurveyConfigWithSurveySummary(SurveySummary.Top);

    assertFalse(classUnderTest.equals(null));
    assertFalse(classUnderTest.equals("someString"));
    assertThat(classUnderTest, is(equalTo(classUnderTest2)));
    assertThat(classUnderTest.hashCode(), is(equalTo(classUnderTest2.hashCode())));
  }

  private static SurveyConfig createSurveyConfigWithRenderTitleLevel(int level) {
    PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(SurveyConfig.KEY_RENDER_TITLE_LEVEL, String.valueOf(level));
    return new SurveyConfig(mockPermissionEvaluator, parameters);
  }

  private static SurveyConfig createSurveyConfigWithSurveySummary(SurveySummary summary) {
    PermissionEvaluator mockPermissionEvaluator = mock(PermissionEvaluator.class);

    Map<String, String> parameters = new HashMap<String, String>();
    if (summary == SurveySummary.None) {
      parameters.put(SurveyConfig.KEY_SHOW_SUMMARY, String.valueOf(false));
    } else if (summary == SurveySummary.Bottom) {
      parameters.put(SurveyConfig.KEY_SHOW_SUMMARY, String.valueOf(true));
      parameters.put(SurveyConfig.KEY_SHOW_LAST, String.valueOf(true));
    } else {
      parameters.put(SurveyConfig.KEY_SHOW_SUMMARY, String.valueOf(true));
    }
    return new SurveyConfig(mockPermissionEvaluator, parameters);
  }
}