package org.hivesoft.confluence.macros.enums;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class VoteActionTest {

  @Test
  public void test_nullCase() {
    assertThat(VoteAction.fromString(null), is(VoteAction.NONE));
  }

  @Test
  public void test_emptyStringCase() {
    assertThat(VoteAction.fromString(""), is(VoteAction.NONE));
  }

  @Test
  public void test_unknownEnumValueCase() {
    assertThat(VoteAction.fromString("unknownAction"), is(VoteAction.NONE));
  }
}
