package org.hivesoft.confluence.macros.vote.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChoiceTest {

  private static final String SOME_CHOICE_DESCRIPTION = "someChoiceSomeoneCanVoteOn";
  private static final String SOME_EXISTING_USER_NAME = "someExistingUserName";
  private static final String SOME_EXISTING_USER_NAME_TWO = "someOtherExistingUserName";

  Choice classUnderTest;

  @Before
  public void setup() {
    classUnderTest = new Choice(SOME_CHOICE_DESCRIPTION);
  }

  @Test
  public void test_getHasVotedFor_success() {
    classUnderTest.voteFor(SOME_EXISTING_USER_NAME);
    classUnderTest.voteFor(SOME_EXISTING_USER_NAME_TWO);

    final boolean hasVotedFor = classUnderTest.getHasVotedFor(SOME_EXISTING_USER_NAME);
    final boolean hasVotedForTwo = classUnderTest.getHasVotedFor(SOME_EXISTING_USER_NAME_TWO);

    assertTrue(hasVotedFor);
    assertTrue(hasVotedForTwo);
  }

  @Test
  public void test_getHasVotedFor_failure() {
    classUnderTest.voteFor(SOME_EXISTING_USER_NAME);

    final boolean hasVotedFor = classUnderTest.getHasVotedFor(SOME_EXISTING_USER_NAME_TWO);
    assertFalse(hasVotedFor);
  }

  @Test
  public void test_voteFor_secondTimeSameUser_success() {
    classUnderTest.voteFor(SOME_EXISTING_USER_NAME);
    classUnderTest.voteFor(SOME_EXISTING_USER_NAME);

    final boolean hasVotedFor = classUnderTest.getHasVotedFor(SOME_EXISTING_USER_NAME);
    assertTrue(hasVotedFor);
  }

  @Test
  public void test_equalsAndHashCode() {
    Choice classUnderTest2 = new Choice(SOME_CHOICE_DESCRIPTION);

    assertFalse(classUnderTest.equals("someString"));
    assertTrue(classUnderTest.equals(classUnderTest2));
    assertTrue(classUnderTest.hashCode() == classUnderTest2.hashCode());
    assertTrue(classUnderTest.toString().equals(classUnderTest2.toString()));
  }
}
