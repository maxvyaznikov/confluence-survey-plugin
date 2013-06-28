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

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * A model object representing an available <code>Choice</code> that can be
 * voted for on a {@link Ballot}.
 * </p>
 */
public class Choice {

    private String description;
    private List<String> votes = new ArrayList<String>();

    /**
     * <p>
     * Create a <code>Choice</code> with the given description.
     * </p>
     *
     * @param description the description of the <code>Choice</code>
     */
    public Choice(String description) {
        this.description = description;
    }

    /**
     * <p>
     * Get the description of this <code>Choice</code>.
     * </p>
     *
     * @return the description of this <code>Choice</code>
     */
    public String getDescription() {
        return description;
    }

    /**
     * <p>
     * Add a vote for this <code>Choice</code>.
     * </p>
     *
     * @param voter the username to add to this <code>Choice</code>
     */
    public void voteFor(String voter) {
        if (!getHasVotedFor(voter)) {
            votes.add(voter);
        }
    }

    /**
     * <p>
     * Remove a vote for this <code>Choice</code>.
     * </p>
     *
     * @param voter the username to remove from this <code>Choice</code>
     */
    public void removeVoteFor(String voter) {
        votes.remove(voter);
    }

    /**
     * <p>
     * Get all of the usernames of people that have voted for this <code>Choice</code>.
     * </p>
     *
     * @return all of the usernames of people that have voted for this <code>Choice</code>
     */
    public Collection<String> getVoters() {
        return votes;
    }

    /**
     * <p>
     * Get a count of the number of votes cast for this <code>Choice</code>.
     * </p>
     *
     * @return the number of votes cast for this <code>Choice</code>
     */
    public int getVoteCount() {
        return votes.size();
    }

    /**
     * <p>
     * Determine is a specific username has voted for this <code>Choice</code>.
     * </p>
     *
     * @param username username of the potential voter
     * @return <code>true</code> if the user previously voted for this
     *         <code>Choice</code>, <code>false</code> otherwise.
     */
    public boolean getHasVotedFor(String username) {
        if (votes.size() > 0) {
            for (Iterator<String> iter = votes.iterator(); iter.hasNext(); ) {
                String vote = (String) iter.next();
                if (vote.equals(username)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * <p>
     * Determines if a <code>Choice</code> is equal to another <code>Choice</code>.
     * Choices are considered equal if their description is the same.
     * </p>
     *
     * @param o the <code>Object</code> to determine equality with this
     *          <code>Choice</code>
     * @return <code>true</code> if the description of the <code>Object</code>
     *         argument is the same as the description of this <code>Choice</code>,
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Choice) {
            return description.equals(((Choice) o).description);
        } else {
            return false;
        }
    }

    /**
     * <p>
     * Return a hash code for the <code>Choice</code>. The hash code for this
     * implementation is simply the hash code of the description.
     * </p>
     *
     * @return hash code of the description
     */
    public int hashCode() {
        return description.hashCode();
    }
}
