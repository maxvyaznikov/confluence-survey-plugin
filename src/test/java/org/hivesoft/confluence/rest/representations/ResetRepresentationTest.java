package org.hivesoft.confluence.rest.representations;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ResetRepresentationTest {

  @Test
  public void test_gettersSetters_success() {
    ResetRepresentation classUnderTest = new ResetRepresentation("someTitle", false);

    assertThat(classUnderTest.isReset(), is(false));
    assertThat(classUnderTest.getTitle(), is("someTitle"));
  }
}
