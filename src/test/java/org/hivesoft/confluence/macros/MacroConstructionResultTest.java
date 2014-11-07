package org.hivesoft.confluence.macros;

import org.junit.Test;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MacroConstructionResultTest {

  MacroConstructionResult classUnderTest;

  /**
   * Actually this method is used by velocity and tested with the macros but getProblems isn't called within java
   */
  @Test
  public void test_getProblems() throws Exception {
    classUnderTest = new MacroConstructionResult();

    assertThat(classUnderTest.getProblems(), hasSize(0));
    assertThat(classUnderTest.hasProblems(), is(false));

    classUnderTest.addProblems("first problem", "second problem");

    assertThat(classUnderTest.getProblems(), containsInAnyOrder("first problem", "second problem"));
    assertThat(classUnderTest.hasProblems(), is(true));
  }
}