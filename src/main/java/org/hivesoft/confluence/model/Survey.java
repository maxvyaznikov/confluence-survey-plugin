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
package org.hivesoft.confluence.model;

import com.atlassian.user.User;
import org.hivesoft.confluence.macros.survey.SurveyConfig;
import org.hivesoft.confluence.utils.SurveyUtils;
import org.hivesoft.confluence.model.vote.Ballot;
import org.hivesoft.confluence.model.vote.Choice;

import java.util.ArrayList;
import java.util.List;

/**
 * A vote object representing a survey. Surveys can have several {@link Ballot}s that can be voted on.
 * Each <code>Ballot</code> has {@link Choice}s that can be assigned zero or more votes.
 */
public class Survey {

  private String title;
  private List<Ballot> ballots = new ArrayList<Ballot>();

  private SurveyConfig config;

  public Survey(SurveyConfig config) {
    this.config = config;
  }

  /**
   * @param ballot to add to the survey.
   */
  public void addBallot(Ballot ballot) {
    ballots.add(ballot);
  }

  /**
   * @param title of the desired ballot.
   * @return The requested <code>Ballot</code> or <code>null</code> if not found.
   */
  public Ballot getBallot(String title) {
    for (Ballot ballot : ballots) {
      if (ballot.getTitle().equals(title)) {
        return ballot;
      }
    }
    return null;
  }

  /**
   * @return the list of ballots
   */
  public List<Ballot> getBallots() {
    return ballots;
  }

  /**
   * @param ballots to set
   */
  public void setBallots(List<Ballot> ballots) {
    this.ballots = ballots;
  }

  /**
   * Indicates whether the survey is complete for a particular user based on whether or not the user has registered a vote for each ballot.
   *
   * @param user The user whose survey we need status for.
   * @return <code>true</code> if the user has voted on all ballots; <code>false</code> otherwise.
   */
  public boolean isSurveyComplete(User user) {
    for (Ballot ballot : ballots) {
      if (!ballot.getHasVoted(user)) {
        return false;
      }
    }

    return true;
  }

  public void setTitle(String inTitle) {
    title = inTitle;
  }

  public String getTitle() {
    return title;
  }

  public String getTitleWithRenderedLinks() {
    return SurveyUtils.enrichStringWithHttpPattern(title);
  }

  public SurveyConfig getConfig() {
    return config;
  }

  /**
   * Get the BallotTitles incl. the names of all choices. To check the length of the key to be stored
   */
  public List<String> getBallotTitlesWithChoiceNames() {
    List<String> ballotChoiceNames = new ArrayList<String>();

    for (Ballot ballot : ballots) {
      ballotChoiceNames.addAll(ballot.getBallotTitlesWithChoiceNames());
    }
    return ballotChoiceNames;
  }

  @Override
  public String toString() {
    return "Survey{" +
            "title='" + title + '\'' +
            ", ballots=" + ballots +
            ", config=" + config +
            '}';
  }
}
