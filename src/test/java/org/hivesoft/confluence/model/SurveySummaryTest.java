package org.hivesoft.confluence.model;

import org.hamcrest.core.Is;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

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

  @Test
  public void test_valueOf() {
    SurveySummary surveySummary = SurveySummary.valueOf(SurveySummary.Bottom.name());

    assertThat(surveySummary, Is.is(SurveySummary.Bottom));

    try {
      SurveySummary.valueOf("unknown");
      fail("should have thrown exception");
    } catch (IllegalArgumentException e) {
      //try catch to spare another test method for a method we don't use anyway
    }
  }
}