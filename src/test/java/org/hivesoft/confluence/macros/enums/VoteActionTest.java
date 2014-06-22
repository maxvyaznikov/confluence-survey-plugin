package org.hivesoft.confluence.macros.enums;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VoteActionTest {

  @Test
  public void test_nullCase() {
    assertEquals(VoteAction.NONE, VoteAction.fromString(null));
  }

  @Test
  public void test_emptyStringCase() {
    assertEquals(VoteAction.NONE, VoteAction.fromString(""));
  }
}
