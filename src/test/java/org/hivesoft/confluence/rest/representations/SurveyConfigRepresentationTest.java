package org.hivesoft.confluence.rest.representations;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class SurveyConfigRepresentationTest {

  @Test
  public void test_equals_success() {
    SurveyConfigRepresentation classUnderTest = new SurveyConfigRepresentation();

    assertFalse(classUnderTest.equals(null));
    assertFalse(classUnderTest.equals("someString"));
    assertThat(classUnderTest.toString(), is(equalTo(new SurveyConfigRepresentation().toString())));
  }
}
