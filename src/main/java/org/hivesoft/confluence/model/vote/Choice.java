/**
 * Copyright (c) 2006-2014, Confluence Community
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hivesoft.confluence.model.vote;

import com.atlassian.user.User;
import org.apache.commons.lang.StringUtils;
import org.hivesoft.confluence.utils.SurveyUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A vote object representing an available <code>Choice</code> that can be voted for on a {@link Ballot}.
 */
public class Choice {

  private final String description;
  private final List<User> voters;

  public static Choice emptyChoice(Choice choiceWithVotes) {
    return new Choice(choiceWithVotes.getDescription(), Collections.<User>emptyList());
  }

  /**
   * @param description the description of the <code>Choice</code>
   */
  public Choice(String description) {
    this(description, new ArrayList<User>());
  }

  private Choice(String description, List<User> voters) {
    this.description = description;
    this.voters = voters;
  }

  /**
   * @return the description of this <code>Choice</code>
   */
  public String getDescription() {
    return description;
  }

  public String getDescriptionWithRenderedLinks() {
    return SurveyUtils.enrichStringWithHttpPattern(description);
  }

  /**
   * @param voter the username to add to this <code>Choice</code>
   */
  public void voteFor(User voter) {
    if (!getHasVotedFor(voter)) {
      voters.add(voter);
    }
  }

  public void removeVoteFor(User voter) {
    voters.remove(voter);
  }

  public List<User> getVoters() {
    return voters;
  }

  public boolean getHasVotedFor(User user) {
    return voters.contains(user);
  }

  public String getEmailStringOfAllVoters() {
    List<String> emails = new ArrayList<String>();
    for (User voter : voters) {
      emails.add(voter.getEmail());
    }
    return StringUtils.join(emails, ',');
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object o) {
    return this == o || o instanceof Choice && description.equals(((Choice) o).getDescription());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return description.hashCode();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "Choice{" +
            "description='" + description + '\'' +
            ", voters=" + voters +
            '}';
  }
}
