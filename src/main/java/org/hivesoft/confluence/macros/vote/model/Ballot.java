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

import org.hivesoft.confluence.macros.vote.VoteConfig;

import java.util.*;

/**
 * A model object representing a voting ballot. Ballots can have several {@link Choice}s that can be voted on.
 * Each <code>Choice</code> can be assigned zero or more votes.
 */
public class Ballot {
  private String title;
  private String description = "";
  private Map<String, Choice> choices = new LinkedHashMap<String, Choice>();
  private Map<String, Comment> comments = new LinkedHashMap<String, Comment>();
  private VoteConfig config;

  /**
   * @param title the title for the <code>Ballot</code>
   */
  public Ballot(String title, VoteConfig config) {
    this.title = title;
    this.config = config;
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

  /**
   * @param choice a {@link Choice} for this <code>Ballot</code>
   */
  public void addChoice(Choice choice) {
    choices.put(choice.getDescription(), choice);
  }

  /**
   * @param username the username of the prospective voter
   * @return true if the user has already voted
   */
  public boolean getHasVoted(String username) {
    return getVote(username) != null;
  }

  /**
   * @param username the username whose vote is needed
   * @return the {@link Choice} the user voted on or <code>null</code> if username has not voted.
   */
  public Choice getVote(String username) {
    Collection<Choice> userChoices = choices.values();
    for (Choice choice : userChoices) {
      if (choice.getHasVotedFor(username)) {
        return choice;
      }
    }
    return null;
  }

  /**
   * @param description the description of the {@link Choice} to be retrieved
   * @return the {@link Choice} associated with the description
   */
  public Choice getChoice(String description) {
    return choices.get(description);
  }

  /**
   * @return all of the {@link Choice}s belonging to this <code>Ballot</code>
   */
  public Collection<Choice> getChoices() {
    return choices.values();
  }

  public VoteConfig getConfig() {
    return config;
  }

  /**
   * @return a count of all votes that have been cast
   */
  public int getTotalVoteCount() {
    int totalVotes = 0;
    Collection<Choice> col = choices.values();
    for (Choice choice : col) {
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
   * @return the description of this ballot.
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description - A String description of this ballot.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Add a user's comment to this ballot. Add comments need to have a username associated with them before adding to a ballot.
   *
   * @param comment - The comment to add.
   */
  public void addComment(Comment comment) {
    if (comment.getUsername() == null) {
      throw new IllegalArgumentException("All comments must have a username.");
    }

    comments.put(comment.getUsername(), comment);
  }

  /**
   * Get the comment entered by a particular user.
   *
   * @param username The name of the user whose comment is to be returned.
   * @return The requested user's comment or null if not present.
   */
  public Comment getCommentForUser(String username) {
    return comments.get(username);
  }

  /**
   * @return All comments entered for this ballot.
   */
  public Map<String, Comment> getComments() {
    return comments;
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

  public int getAveragePercentage(float average) {
    if (config.getIterateStep() < 0)
      return (int) (average - getLowerBound() - config.getIterateStep()) * 100 / (getUpperBound() - getLowerBound() - config.getIterateStep());
    else
      return (int) (average - getLowerBound() + config.getIterateStep()) * 100 / (getUpperBound() - getLowerBound() + config.getIterateStep());
  }

  /**
   * @return The bounds for this ballot if different then the default
   */
  public String getBoundsIfNotDefault() {
    return (config.getStartBound() == 1 && config.getIterateStep() == 1) ? "" : "(" + config.getStartBound() + "-" + getEndBound() + ")";
  }

  /**
   * Return <code>Voters</code> containing the stored voters.
   *
   * @return Voters of the ballot
   */
  public Collection<String> getAllVoters() {
    List<String> voters = new ArrayList<String>();
    for (Choice choice : choices.values()) {
      Collection<String> choiceVoters = choice.getVoters();
      voters.addAll(choiceVoters);
    }
    return voters;
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
  public String computeFormatedAverage(String format) {
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
   * Determines if a <code>Ballot</code> is equal to another <code>Ballot</code>. Ballots are considered equal if their title is the same for both ballots.
   *
   * @param o the <code>Object</code> to determine equality with this <code>Ballot</code>
   * @return <code>true</code> if the ballot title of the <code>Object</code> argument is the same as the title of this <code>Ballot</code>, <code>false</code> otherwise.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Ballot)) return false;

    Ballot ballot = (Ballot) o;

    return description.equals(ballot.description) && title.equals(ballot.title);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    int result = description.hashCode();
    result = 31 * result + title.hashCode();
    return result;
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
