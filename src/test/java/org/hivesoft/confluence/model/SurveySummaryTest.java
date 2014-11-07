package org.hivesoft.confluence.model;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SurveySummaryTest {

  @Test
  public void test_getForNull_expectNone() {
    SurveySummary result = SurveySummary.getFor(null);

    assertThat(result, is(SurveySummary.Top));
  }

  @Test
  public void test_getForSomething_expectNone() {
    SurveySummary result = SurveySummary.getFor("something");

    assertThat(result, is(SurveySummary.Top));
  }

  @Test
  public void test_getForBottom_expectBottom() {
    SurveySummary surveySummary = SurveySummary.Bottom;

    SurveySummary result = SurveySummary.getFor(surveySummary.name());

    assertThat(result, is(SurveySummary.Bottom));
  }
}