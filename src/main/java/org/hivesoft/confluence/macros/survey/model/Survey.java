/**
 * Copyright (c) 2006-2013, Confluence Community
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hivesoft.confluence.macros.survey.model;

import org.hivesoft.confluence.macros.vote.model.Ballot;
import org.hivesoft.confluence.macros.vote.model.Choice;

import java.util.ArrayList;
import java.util.List;

/**
 * A model object representing a survey. Surveys can have several {@link Ballot}s that can be voted on.
 * Each <code>Ballot</code> has {@link Choice}s that can be assigned zero or more votes.
 */
public class Survey {

    private String title;
    private List<Ballot> ballots = new ArrayList<Ballot>();

    private boolean changeableVotes = false;
    private boolean visibleVoters = false;
    private SurveySummary surveySummary = SurveySummary.Top;

    /**
     * <p>
     * Add a voting ballot to this survey.
     * </p>
     *
     * @param ballot The ballot to add to the back of the survey.
     */
    public void addBallot(Ballot ballot) {
        ballot.setChangeableVotes(changeableVotes);
        ballots.add(ballot);
    }

    /**
     * <p>
     * Get a particular ballot based on it's title.
     * </p>
     *
     * @param title The title of the desired ballot.
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
     * <p>
     * Get all <code>Ballot</code>s that are part of this survey.
     * </p>
     *
     * @return A <code>List</code> of <code>Ballot</code>s.
     */
    public List<Ballot> getBallots() {
        return ballots;
    }

    /**
     * <p>
     * Set the <code>Ballot</code>s that are part of this survey.
     * </p>
     *
     * @param ballots A <code>List</code> of <code>Ballot</code>s.
     */
    public void setBallots(List<Ballot> ballots) {
        this.ballots = ballots;
    }

    /**
     * <p>
     * Indicates whether the survey is complete for a particular user
     * based on whether or not the user has registered a vote for
     * each ballot.
     * </p>
     *
     * @param username The user whose survey we need status for.
     * @return <code>true</code> if the user has voted on all ballots; <code>false</code> otherwise.
     */
    public boolean isSurveyComplete(String username) {
        for (Ballot ballot : ballots) {
            if (!ballot.getHasVoted(username)) {
                return false;
            }
        }

        return true;
    }

    /**
     * <p>
     * Return whether or not users should be allowed to change
     * their vote once it has been cast. This survey is not
     * responsible for enforcing this behaviour. It merely
     * tracks whether it should be allowed or not.
     * </p>
     *
     * @return <code>true</code> if users can change their vote; <code>false</code> (default) otherwise.
     */
    public boolean isChangeableVotes() {
        return changeableVotes;
    }

    /**
     * <p>
     * Set whether or not this survey should allow users to change
     * their votes once they have been cast. This survey is not
     * responsible for enforcing this behaviour. It merely
     * tracks whether it should be allowed or not.
     * </p>
     *
     * @param changeableVotes <code>true</code> if users can change their vote; <code>false</code> (default) otherwise.
     */
    public void setChangeableVotes(boolean changeableVotes) {
        this.changeableVotes = changeableVotes;

        for (Ballot ballot : ballots) {
            ballot.setChangeableVotes(changeableVotes);
        }
    }

    /**
     * <p>
     * Return whether or not users should be allowed to see the voters in clear text
     * </p>
     *
     * @return <code>true</code> if users can change their vote; <code>false</code> (default) otherwise.
     */
    public boolean isVisibleVoters() {
        return visibleVoters;
    }

    /**
     * <p>
     * Set whether or not this survey should allow users to see the voted users
     * </p>
     *
     * @param visibleVoters <code>true</code> if users can see voted users in clear text; <code>false</code> (default) otherwise.
     */
    public void setVisibleVoters(boolean visibleVoters) {
        this.visibleVoters = visibleVoters;

        for (Ballot ballot : ballots) {
            ballot.setVisibleVoters(visibleVoters);
        }
    }


    /**
     * <p>
     * Set the Start Bound and iterating step for each ballot (can be overriden by each one if necessary)
     * </p>
     *
     * @param startBound defaults <code>1</code>, iterateStep defaults <code>1</code>.
     */
    public void setStartBoundAndIterateStep(int startBound, int iterateStep) {
        for (Ballot ballot : ballots) {
            ballot.setStartBound(startBound);
            ballot.setIterateStep(iterateStep);
        }
    }

    /**
     * Set whether or not the surveySummary of the survey should be displayed @param surveySummary Flag to indicate surveySummary display
     */
    public void setSurveySummary(SurveySummary surveySummary) {
        this.surveySummary = surveySummary;
    }

    /**
     * Flag to indicate surveySummary display
     */
    public SurveySummary getSurveySummary() {
        return surveySummary;
    }

    public void setTitle(String inTitle) {
        title = inTitle;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Get the BallotTitles incl. the names of all choices. To check the length of the key to be stored
     */
    public List<String> getBallotTitlesWithChoiceNames() {
        List<String> ballotChoiceNames = new ArrayList<String>();

        for (Ballot ballot : ballots) {
            for (Choice choice : ballot.getChoices()) {
                ballotChoiceNames.add(ballot.getTitle() + "." + choice.getDescription());
            }
        }
        return ballotChoiceNames;
    }
}
