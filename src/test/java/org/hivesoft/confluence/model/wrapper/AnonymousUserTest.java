package org.hivesoft.confluence.model.wrapper;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AnonymousUserTest {

  @Test
  public void test_staticValues() {
    AnonymousUser classUnderTest = new AnonymousUser();

    assertThat(classUnderTest.getEmail(), is("NoOne@hivesoft.org"));
    assertThat(classUnderTest.getFullName(), is("I am not the User you are looking for"));
    assertThat(classUnderTest.getName(), is("Anonymous"));
  }

}