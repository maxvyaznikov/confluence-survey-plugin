package org.hivesoft.confluence.macros.vote.model;

import com.atlassian.user.User;
import com.atlassian.user.impl.DefaultUser;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;

public class ChoiceTest {

  private static final String SOME_CHOICE_DESCRIPTION = "someChoiceSomeoneCanVoteOn";
  private static final String SOME_EXISTING_USER_NAME_ONE = "someExistingUserName";
  private static final String SOME_EXISTING_USER_NAME_TWO = "someOtherExistingUserName";

  private static final User SOME_USER_ONE = new DefaultUser(SOME_EXISTING_USER_NAME_ONE, "someExistingFullName1", "someMail@one.com");
  private static final User SOME_USER_TWO = new DefaultUser(SOME_EXISTING_USER_NAME_TWO, "someExistingFullName2", "someMail@two.com");

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
  public void test_getEmailStringOfAllVoters_noVotes_success() {
    final String emails = classUnderTest.getEmailStringOfAllVoters();

    assertThat(emails, is(""));
  }

  @Test
  public void test_getEmailStringOfAllVoters_oneEmail_success() {
    classUnderTest.voteFor(SOME_USER_ONE);

    final String emails = classUnderTest.getEmailStringOfAllVoters();

    assertThat(emails, containsString(SOME_USER_ONE.getEmail()));
  }

  @Test
  public void test_getEmailStringOfAllVoters_emailNull_success() {
    classUnderTest.voteFor(new DefaultUser("sampleUserWithoutEmail"));

    final String emails = classUnderTest.getEmailStringOfAllVoters();

    assertThat(emails, is(""));
  }

  @Test
  public void test_voteFor_secondTimeSameUser_success() {
    classUnderTest.voteFor(SOME_USER_ONE);
    classUnderTest.voteFor(SOME_USER_ONE);

    final boolean hasVotedFor = classUnderTest.getHasVotedFor(SOME_USER_ONE);
    assertThat(hasVotedFor, is(true));
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
