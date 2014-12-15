package org.hivesoft.confluence.rest.representations;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LockRepresentationTest {

  @Test
  public void test_gettersSetters_success() {
    LockRepresentation classUnderTest = new LockRepresentation("someTitle", false);

    assertThat(classUnderTest.isLocked(), is(equalTo(false)));

    classUnderTest.setLocked(true);

    assertThat(classUnderTest.isLocked(), is(equalTo(true)));
    assertThat(classUnderTest.getTitle(), is("someTitle"));
  }
}
