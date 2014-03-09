package org.hivesoft.confluence.macros.utils;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class VelocityAbstractionHelperTest {

  VelocityAbstractionHelper classUnderTest;

  /**
   * The method getDefaultVelocityContext can only be tested if confluence is initialized
   */
  @Test
  public void test_constructor() throws Exception {
    classUnderTest = new VelocityAbstractionHelper();

    assertNotNull(classUnderTest);
  }
}
