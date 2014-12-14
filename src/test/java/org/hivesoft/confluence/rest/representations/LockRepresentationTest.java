package org.hivesoft.confluence.rest.representations;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LockRepresentationTest {
  private LockRepresentation classUnderTest;

  @Test
  public void test_gettersSetters_success() {
    classUnderTest = new LockRepresentation("someTitle", false);
    assertThat(false, is(equalTo(classUnderTest.isLocked())));

    assertTrue(classUnderTest.equals(new LockRepresentation("someTitle", false)));
    assertFalse(classUnderTest.equals(new LockRepresentation("someTitle", true)));
    assertFalse(classUnderTest.equals(null));
    assertFalse(classUnderTest.equals("someString"));
  }
}
