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

  public String getBallotTitle() {
    return ballotTitle;
  }

  public void setBallotTitle(String ballotTitle) {
    this.ballotTitle = ballotTitle;
  }

  public String getVoteChoice() {
    return voteChoice;
  }

  public void setVoteChoice(String voteChoice) {
    this.voteChoice = voteChoice;
  }

  public String getVoteAction() {
    return voteAction;
  }

  public void setVoteAction(String voteAction) {
    this.voteAction = voteAction;
  }
}
