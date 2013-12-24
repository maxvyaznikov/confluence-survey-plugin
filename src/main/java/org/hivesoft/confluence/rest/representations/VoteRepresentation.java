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

  public VoteRepresentation(String ballotTitle, String voteChoice, String voteAction) {
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
