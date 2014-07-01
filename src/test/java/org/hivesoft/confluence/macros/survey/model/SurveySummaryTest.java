package org.hivesoft.confluence.macros.survey.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SurveySummaryTest {

  @Test
  public void test_getFor_should_return_null_for_propertyValue_null() {
    // When:
    SurveySummary result = SurveySummary.getFor(null);

    // Then:
    assertEquals(null, result);
  }

  @Test
  public void test_getFor_should_return_null_for_unknown_propertyValue() {
    // When:
    SurveySummary result = SurveySummary.getFor("unknown");

    // Then:
    assertEquals(null, result);
  }

  @Test
  public void test_getFor_should_return_BOTTOM_SurveySummary_for_its_propertyValue() {
    // Given:
    SurveySummary surveySummary = SurveySummary.Bottom;

    // When:
    SurveySummary result = SurveySummary.getFor(surveySummary.propertyValue);

    // Then:
    assertEquals(surveySummary, result);
  }
}