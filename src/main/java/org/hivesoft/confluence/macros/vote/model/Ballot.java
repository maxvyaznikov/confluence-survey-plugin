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
package org.hivesoft.confluence.macros.vote.model;

import java.util.*;

/**
 * A model object representing a voting ballot. Ballots can have several {@link Choice}s that can be voted on.
 * Each <code>Choice</code> can be assigned zero or more votes.
 */
public class Ballot {

    public static final int DEFAULT_START_BOUND = 1;
    public static final int DEFAULT_ITERATE_STEP = 1;

    private String description = "";
    private String title;
    private Map<String, Choice> choices = new LinkedHashMap<String, Choice>();
    private Map<String, Comment> comments = new LinkedHashMap<String, Comment>();
    private boolean changeableVotes = false;
    private boolean visibleVoters = false;
    private int startBound = DEFAULT_START_BOUND; //1.1.7.1 calculate for each ballot starting by 1
    private int iterateStep = DEFAULT_ITERATE_STEP; //iterating Step, so usually it is 1 .. till choices.upperbound

    /**
     * Create a <code>Ballot</code>, specifying the title.
     *
     * @param title the title for the <code>Ballot</code>
     */
    public Ballot(String title) {
        this.title = title;
    }

    /**
     * Get the title assigned to the <code>Ballot</code>.
     *
     * @return title assigned to the <code>Ballot</code>
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the Survey Title for linkage (no Space), and make it lowercase . required for comments
     */
    public String getTitleNoSpace() {
        return title.replaceAll(" ", "").toLowerCase();
    }

    /**
     * Add an available {@link Choice} for this <code>Ballot</code>.
     *
     * @param choice a {@link Choice} for this <code>Ballot</code>
     */
    public void addChoice(Choice choice) {
        choices.put(choice.getDescription(), choice);
    }

    /**
     * Determine if a user has already voted.
     *
     * @param username the username of the prospective voter
     * @return <code>true</code> if the user has already voted on this <code>Ballot</code>, <code>false</code> if s/he has not.
     */
    public boolean getHasVoted(String username) {
        return getVote(username) != null;
    }

    /**
     * Return whether or not users should be allowed to change their vote once it has been cast.
     *
     * @return <code>true</code> if users can change their vote; <code>false</code> (default) otherwise.
     */
    public boolean isChangeableVotes() {
        return changeableVotes;
    }

    /**
     * Set whether or not this ballot should allow users to change their vote once it has been cast.
     *
     * @param changeableVotes <code>true</code> if users can change their vote; <code>false</code> (default) otherwise.
     */
    public void setChangeableVotes(boolean changeableVotes) {
        this.changeableVotes = changeableVotes;
    }

    /**
     * Return whether or not users should be allowed to see voters in clear text
     *
     * @return <code>true</code> if users can see voters; <code>false</code> (default) otherwise.
     */
    public boolean isVisibleVoters() {
        return visibleVoters;
    }

    /**
     * Set whether or not this ballot should allow users to see the voters in clear text
     *
     * @param visibleVoters <code>true</code> if users can see voters; <code>false</code> (default) otherwise.
     */
    public void setVisibleVoters(boolean visibleVoters) {
        this.visibleVoters = visibleVoters;
    }

    /**
     * Get the choice voted for by a particular user
     *
     * @param username the username whose vote is needed
     * @return <code>Choice</code> that the user voted on, <code>null</code> if username has not voted.
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
     * Retrieve a {@link Choice} on the <code>Ballot</code> based on its description.
     *
     * @param description the description of the {@link Choice} to be retrieved
     * @return the {@link Choice} associated with the description
     */
    public Choice getChoice(String description) {
        return choices.get(description);
    }

    /**
     * Get all of the {@link Choice}s belonging to this <code>Ballot</code>.
     *
     * @return all of the {@link Choice}s belonging to this <code>Ballot</code>
     */
    public Collection<Choice> getChoices() {
        return choices.values();
    }

    /**
     * Get a count of all votes that have been cast on the <code>Ballot</code>.
     *
     * @return a count of all votes that have been cast
     */
    public int getTotalVoteCount() {
        int totalVotes = 0;
        Collection<Choice> col = choices.values();
        for (Choice choice : col) {
            totalVotes += choice.getVoteCount();
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
            return (100 * choice.getVoteCount()) / totalVoteCount;
        } else {
            return 0;
        }
    }

    /**
     * Set the description for this ballot.
     *
     * @return A String description of this ballot.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the description for this ballot.
     *
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
     * <p>
     * Get all of the comments entered on this ballot.
     * </p>
     *
     * @return All comments entered for this ballot.
     */
    public Map<String, Comment> getComments() {
        return comments;
    }

    /**
     * Set the StartBound for calculations (average)
     *
     * @param iStartBound - the Iteration shall start with
     */
    public void setStartBound(int iStartBound) {
        startBound = iStartBound;
    }

    /**
     * Get the StartBound for calculations (average)
     *
     * @return The StartBound for this ballots calcu/iteration.
     */
    public int getStartBound() {
        return startBound;
    }

    /**
     * Set the iterateStep for calculations (average)
     *
     * @param iIterateStep the Iteration shall iterate with
     */
    public void setIterateStep(int iIterateStep) {
        iterateStep = iIterateStep;
    }

    /**
     * Get the Iterations Step for calculations (average)
     *
     * @return The iteration Step for this ballots calcu/iteration.
     */
    public int getIterateStep() {
        return iterateStep;
    }

    /**
     * @return The calculated EndBound Value (out of StartBound + iteration-steps)
     */
    public int getEndBound() {
        return startBound + (choices.size() - 1) * iterateStep;
    }

    /**
     * @return The calculated <code>real<code> LowerBound Value
     */
    public int getLowerBound() {
        return getStartBound() > getEndBound() ? getEndBound() : getStartBound();
    }

    /**
     * @return The calculated <code>real<code> UpperBound Value
     */
    public int getUpperBound() {
        return getStartBound() > getEndBound() ? getStartBound() : getEndBound();
    }

    public int getAveragePercentage(float average) {
        return (int) (average - getLowerBound() + iterateStep) * 100 / (getUpperBound() - getLowerBound() + iterateStep);
    }

    /**
     * @return The bounds for this ballot if different then the default
     */
    public String getBoundsIfNotDefault() {
        return (startBound == 1 && iterateStep == 1) ? "" : "(" + getStartBound() + "-" + getEndBound() + ")";
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
        int iCur = startBound + (choices.size() - 1) * iterateStep;
        for (Choice choice : choices) {
            total += iCur * choice.getVoteCount();
            iCur -= iterateStep;
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

        if (!description.equals(ballot.description)) return false;
        if (!title.equals(ballot.title)) return false;

        return true;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Ballot{" +
                "description='" + description + '\'' +
                ", title='" + title + '\'' +
                ", choices=" + choices +
                ", comments=" + comments +
                ", changeableVotes=" + changeableVotes +
                ", visibleVoters=" + visibleVoters +
                ", startBound=" + startBound +
                ", iterateStep=" + iterateStep +
                '}';
    }
}
