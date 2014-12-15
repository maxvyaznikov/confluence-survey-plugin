package org.hivesoft.confluence.rest.representations;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VoteRepresentationTest {

  @Test
  public void test_gettersSetters_success() {
    VoteRepresentation classUnderTest = new VoteRepresentation("someBallotTitle", "someChoice", "vote");

    assertThat(classUnderTest.getBallotTitle(), is("someBallotTitle"));
    assertThat(classUnderTest.getVoteChoice(), is("someChoice"));
    assertThat(classUnderTest.getVoteAction(), is("vote"));

    VoteRepresentation anotherInstance1 = new VoteRepresentation("someBallotTitle", "someChoice", "vote");
    assertTrue(classUnderTest.equals(anotherInstance1));
    assertFalse(classUnderTest.equals(null));
    assertFalse(classUnderTest.equals("someString"));
    assertThat(classUnderTest.hashCode(), is(anotherInstance1.hashCode()));

    VoteRepresentation notThisBallotTitle = new VoteRepresentation("notThisBallotTitle", "someChoice", "vote");
    assertFalse(classUnderTest.equals(notThisBallotTitle));
    assertThat(classUnderTest.hashCode(), is(not(notThisBallotTitle.hashCode())));

    VoteRepresentation notThisChoice = new VoteRepresentation("someBallotTitle", "notThisChoice", "vote");
    assertFalse(classUnderTest.equals(notThisChoice));
    assertThat(classUnderTest.hashCode(), is(not(notThisChoice.hashCode())));

    VoteRepresentation notThisVote = new VoteRepresentation("someBallotTitle", "someChoice", "notThisVote");
    assertFalse(classUnderTest.equals(notThisVote));
    assertThat(classUnderTest.hashCode(), is(not(notThisVote.hashCode())));
  }
}
