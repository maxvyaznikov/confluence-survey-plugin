package org.hivesoft.confluence.rest.representations;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VoteRepresentationTest {
  VoteRepresentation classUnderTest;

  @Test
  public void test_gettersSetters_success() {
    classUnderTest = new VoteRepresentation("someBallotTitle", "someChoice", "vote");
    assertThat("someBallotTitle", is(equalTo(classUnderTest.getBallotTitle())));
    assertThat("someChoice", is(equalTo(classUnderTest.getVoteChoice())));
    assertThat("vote", is(equalTo(classUnderTest.getVoteAction())));

    assertTrue(classUnderTest.equals(new VoteRepresentation("someBallotTitle", "someChoice", "vote")));
    assertFalse(classUnderTest.equals(null));
    assertFalse(classUnderTest.equals("someString"));
    assertFalse(classUnderTest.equals(new VoteRepresentation("notThisBallotTitle", "someChoice", "vote")));
    assertFalse(classUnderTest.equals(new VoteRepresentation("someBallotTitle", "notThisChoice", "vote")));
    assertFalse(classUnderTest.equals(new VoteRepresentation("someBallotTitle", "someChoice", "notThisVote")));
  }
}
