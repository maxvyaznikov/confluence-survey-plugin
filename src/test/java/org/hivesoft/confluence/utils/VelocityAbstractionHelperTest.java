package org.hivesoft.confluence.utils;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class VelocityAbstractionHelperTest {

  /**
   * The method getDefaultVelocityContext can only be tested if confluence is initialized
   */
  @Test
  public void test_constructor() throws Exception {
    VelocityAbstractionHelper classUnderTest = new VelocityAbstractionHelper();

    assertThat(classUnderTest, is(not(nullValue())));
  }
}
