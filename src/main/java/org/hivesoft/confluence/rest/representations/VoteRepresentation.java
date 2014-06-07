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
package org.hivesoft.confluence.rest.representations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class VoteRepresentation {

  @XmlElement
  private String ballotTitle;
  @XmlElement
  private String voteChoice;
  @XmlElement
  private String voteAction;

  public VoteRepresentation() {
    //for jaxb
  }

  public VoteRepresentation(String ballotTitle, String voteChoice, String voteAction) {
    this();
    this.ballotTitle = ballotTitle;
    this.voteChoice = voteChoice;
    this.voteAction = voteAction;
  }

  public String getBallotTitle() {
    return ballotTitle;
  }

  public String getVoteChoice() {
    return voteChoice;
  }

  public String getVoteAction() {
    return voteAction;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof VoteRepresentation)) return false;

    VoteRepresentation that = (VoteRepresentation) o;

    return that.getBallotTitle().equalsIgnoreCase(ballotTitle) && that.getVoteChoice().equalsIgnoreCase(voteChoice) && that.getVoteAction().equalsIgnoreCase(voteAction);
  }
}
