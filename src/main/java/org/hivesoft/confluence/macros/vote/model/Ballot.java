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
package org.hivesoft.confluence.macros.vote.model;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.user.Group;
import com.atlassian.user.User;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.hivesoft.confluence.macros.vote.VoteConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A model object representing a voting ballot. Ballots can have several {@link Choice}s that can be voted on.
 * Each <code>Choice</code> can be assigned zero or more votes.
 */
public class Ballot {
  private final String title;
  private final String description;
  private final VoteConfig config;

  private final List<Choice> choices;
  private final List<Comment> comments;

  public Ballot(String title, String description, VoteConfig config, List<Choice> choices, List<Comment> comments) {
    this.title = title;
    this.description=description;
    this.config = config;
    this.choices = choices;
    this.comments = comments;
  }

  /**
   * @return the title of the Ballot
   */
  public String getTitle() {
    return title;
  }

  /**
   * @return the Survey Title for linkage (no Space), and make it lowercase . required for comments
   */
  public String getTitleNoSpace() {
    return title.replaceAll(" ", "").toLowerCase();
  }

  public String getDescription() {
    return description;
  }

  public VoteConfig getConfig() {
    return config;
  }

  /**
   * @param username the username whose vote is needed
   * @return the {@link Choice} the user voted on or <code>null</code> if username has not voted.
   */
  public Choice getChoiceForUserName(String username) {
    for (Choice choice : choices) {
      if (choice.getHasVotedFor(username)) {
        return choice;
      }
    }
    return null;
  }

  /**
   * @param username the username of the prospective voter
   * @return true if the user has already voted
   */
  public boolean getHasVoted(String username) {
    return getChoiceForUserName(username) != null;
  }

  /**
   * @param description the description of the {@link Choice} to be retrieved
   * @return the {@link Choice} associated with the description
   */
  public Choice getChoice(String description) {
    for (Choice choice : choices) {
      if (description.equals(choice.getDescription())) {
        return choice;
      }
    }
    return null;
  }

  /**
   * @return all of the {@link Choice}s belonging to this <code>Ballot</code>
   */
  public Collection<Choice> getChoices() {
    return choices;
  }

  /**
   * Get the comment entered by a particular user.
   *
   * @param username The name of the user whose comment is to be returned.
   * @return The requested user's comment or null if not present.
   */
  public Comment getCommentForUser(String username) {
    for (Comment comment : comments) {
      if (username.equals(comment.getUsername())) {
        return comment;
      }
    }
    return null;
  }

  /**
   * @return All comments entered for this ballot.
   */
  public List<Comment> getComments() {
    return comments;
  }

  /**
   * Return <code>Voters</code> containing the stored voters.
   *
   * @return Voters of the ballot
   */
  public Collection<String> getAllVoters() {
    List<String> voters = new ArrayList<String>();
    for (Choice choice : choices) {
      Collection<String> choiceVoters = choice.getVoters();
      voters.addAll(choiceVoters);
    }
    return voters;
  }

  /**
   * Return possible <code>voters</code> determined by searching
   * {@link VoteConfig#getVoters()} for configured users and all activated users
   * of configured groups.
   *
   * @param userAccessor to check whether {@code voter} is a Groups or a Users
   *                     as well as retrieved members of groups.
   * @return all possible Voters of the ballot. Empty list if no {@code voters}
   * are configured. Never {@code null}.
   */
  public Collection<String> getAllPossibleVoters(UserAccessor userAccessor) {
    List<String> userNames = Lists.newArrayList();
    for (String configuredVoter : config.getVoters()) {
      Group group = userAccessor.getGroup(configuredVoter);
      if (group == null) {
        userNames.add(configuredVoter);
      } else {
        for (String userName : userAccessor.getMemberNamesAsList(group)) {
          if (!userAccessor.isDeactivated(userName)) {
            userNames.add(userName);
          }
        }
      }
    }
    return userNames;
  }

  /**
   * Return pending {@code voters} determined by
   * {@link #getAllPossibleVoters(UserAccessor)} - {@link #getAllVoters()}.
   *
   * @param userAccessor to check whether {@code voter} is a Groups or a Users
   *                     as well as retrieved members of groups.
   * @return all pending {@code voters} of the ballot. Never {@code null}.
   */
  public Collection<String> getAllPendingVoters(UserAccessor userAccessor) {
    Collection<String> result = getAllPossibleVoters(userAccessor);
    Iterables.removeAll(result, getAllVoters());
    return result;
  }

  /**
   * Return comma separated list of email addresses for supplied {@code voters}
   * determined by {@link #getAllPendingVoters(UserAccessor)}. {@code Voters}
   * who cannot be found as users, will be skipped.
   *
   * @param userAccessor to retrieved {@link User} for the supplied
   *                     {@code voters}.
   * @param voters       a list of all {@code voters} whose email addresses should be
   *                     included
   * @return comma separated email addresses of supplied {@code voters}. Never
   * {@code null}.
   */
  public String getEmailStringFor(UserAccessor userAccessor, Collection<String> voters) {
    List<String> emails = new ArrayList<String>();
    for (String voter : voters) {
      User user = userAccessor.getUser(voter);
      if (user != null) {
        emails.add(user.getEmail());
      }
    }
    return Joiner.on(',').join(emails);
  }

  /**
   * @return The calculated EndBound Value (out of StartBound + iteration-steps)
   */
  public int getEndBound() {
    return config.getStartBound() + (choices.size() - 1) * config.getIterateStep();
  }

  /**
   * @return The calculated <code>real<code> LowerBound Value
   */
  public int getLowerBound() {
    return config.getStartBound() > getEndBound() ? getEndBound() : config.getStartBound();
  }

  /**
   * @return The calculated <code>real<code> UpperBound Value
   */
  public int getUpperBound() {
    return config.getStartBound() > getEndBound() ? config.getStartBound() : getEndBound();
  }

  public int getCurrentValueByIndex(int index) {
    return config.getStartBound() + config.getIterateStep() * index;
  }

  public int getAveragePercentage() {
    int choiceIndex = choices.size() - 1;
    int totalVoteCount = 0;
    int weightedChoices = 0;
    for (Choice choice : choices) {
      final int voteCount = choice.getVoters().size();
      weightedChoices += choiceIndex * voteCount;
      choiceIndex--;
      totalVoteCount += voteCount;
    }
    if (totalVoteCount == 0) {
      return 0;
    }
    return weightedChoices * 100 / ((choices.size() - 1) * totalVoteCount);
  }

  /**
   * @return The bounds for this ballot if different then the default
   */
  public String getBoundsIfNotDefault() {
    return (config.getStartBound() == 1 && config.getIterateStep() == 1) ? "" : "(" + config.getStartBound() + "-" + getEndBound() + ")";
  }

  /**
   * @return a count of all votes that have been cast
   */
  public int getTotalVoteCount() {
    int totalVotes = 0;
    for (Choice choice : choices) {
      totalVotes += choice.getVoters().size();
    }
    return totalVotes;
  }

  /**
   * Get the percentage of the total vote represented by the provided {@link Choice}.
   *
   * @param choice - the {@link Choice} to determine the vote percentage of
   * @return the percentage of the total vote represented by the provided {@link Choice}. The percentage is given as a whole number, rather than a floating point number.
   */
  public int getPercentageOfVoteForChoice(Choice choice) {
    int totalVoteCount = getTotalVoteCount();
    if (totalVoteCount != 0) {
      return (100 * choice.getVoters().size()) / totalVoteCount;
    } else {
      return 0;
    }
  }

  /**
   * Compute the average response value based on the order of the choices in the ballot. The choice values go from choices.length to 1.
   *
   * @return The calculated average response.
   */
  public float computeAverage() {
    int total = 0;
    int totalVoteCount = getTotalVoteCount();
    if (totalVoteCount == 0) {
      return 0.0f;
    }

    Collection<Choice> choices = this.getChoices();
    //the first choice gets the highest number, so calculate last
    int iCur = config.getStartBound() + (choices.size() - 1) * config.getIterateStep();
    for (Choice choice : choices) {
      total += iCur * choice.getVoters().size();
      iCur -= config.getIterateStep();
    }
    return ((float) total) / totalVoteCount;
  }

  /**
   * format the output to a default of 2 digits, format like "0.##"
   */
  public String computeFormattedAverage(String format) {
    return new java.text.DecimalFormat(format).format((double) computeAverage());
  }

  /**
   * get the length of all storeAble keys
   */
  public List<String> getBallotTitlesWithChoiceNames() {
    List<String> ballotChoiceNames = new ArrayList<String>();
    for (Choice choice : getChoices()) {
      ballotChoiceNames.add(title + "." + choice.getDescription());
    }
    return ballotChoiceNames;
  }

  /**
   * Ballots are considered equal if their title is the same for both ballots.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Ballot)) return false;
    return title.equals(((Ballot) o).title);
  }

  @Override
  public int hashCode() {
    return title.hashCode();
  }

  @Override
  public String toString() {
    return "Ballot{" +
            "title='" + title + '\'' +
            ", description='" + description + '\'' +
            ", choices=" + choices +
            ", comments=" + comments +
            ", config=" + config +
            '}';
  }
}
