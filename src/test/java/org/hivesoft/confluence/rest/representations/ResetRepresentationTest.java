package org.hivesoft.confluence.rest.representations;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ResetRepresentationTest {
  ResetRepresentation classUnderTest;

  @Test
  public void test_gettersSetters_success() {
    classUnderTest = new ResetRepresentation("someTitle", false);
    assertThat(false, is(equalTo(classUnderTest.isReset())));

  }
}
