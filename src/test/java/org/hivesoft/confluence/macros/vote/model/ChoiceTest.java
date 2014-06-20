package org.hivesoft.confluence.macros.vote.model;

import com.atlassian.user.User;
import com.atlassian.user.impl.DefaultUser;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ChoiceTest {

  private static final String SOME_CHOICE_DESCRIPTION = "someChoiceSomeoneCanVoteOn";
  private static final String SOME_EXISTING_USER_NAME_ONE = "someExistingUserName";
  private static final String SOME_EXISTING_USER_NAME_TWO = "someOtherExistingUserName";

  private static final User SOME_USER_ONE = new DefaultUser(SOME_EXISTING_USER_NAME_ONE);
  private static final User SOME_USER_TWO = new DefaultUser(SOME_EXISTING_USER_NAME_TWO);

  Choice classUnderTest;

  @Before
  public void setup() {
    classUnderTest = new Choice(SOME_CHOICE_DESCRIPTION);
  }

  @Test
  public void test_getHasVotedFor_success() {
    classUnderTest.voteFor(SOME_USER_ONE);
    classUnderTest.voteFor(SOME_USER_TWO);

    final boolean hasVotedFor = classUnderTest.getHasVotedFor(SOME_USER_ONE);
    final boolean hasVotedForTwo = classUnderTest.getHasVotedFor(SOME_USER_TWO);

    assertTrue(hasVotedFor);
    assertTrue(hasVotedForTwo);
  }

  @Test
  public void test_getHasVotedFor_failure() {
    classUnderTest.voteFor(SOME_USER_ONE);

    final boolean hasVotedFor = classUnderTest.getHasVotedFor(SOME_USER_TWO);
    assertFalse(hasVotedFor);
  }

  @Test
  public void test_voteFor_secondTimeSameUser_success() {
    classUnderTest.voteFor(SOME_USER_ONE);
    classUnderTest.voteFor(SOME_USER_ONE);

    final boolean hasVotedFor = classUnderTest.getHasVotedFor(SOME_USER_ONE);
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

  @Test
  public void test_getDescriptionWithRenderedLinks() {
    classUnderTest = new Choice("i am a choice to http://google.de but https://www.google.com is also ok");
    assertThat(classUnderTest.getDescriptionWithRenderedLinks(),
            is("i am a choice to <a href=\"http://google.de\" target=\"_blank\">http://google.de</a> but <a href=\"https://www.google.com\" target=\"_blank\">https://www.google.com</a> is also ok"));
  }
}
