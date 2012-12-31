package com.tomclaw.mandarin.main;

import java.util.Hashtable;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class BuddyInfo {

  public Hashtable buddyHash;
  public int reqSeqNum;
  public String buddyId;
  public String nickName;

  public BuddyInfo( String buddyId, String nickName, int reqSeqNum ) {
    buddyHash = new Hashtable();
    this.buddyId = buddyId;
    this.nickName = nickName;
    this.reqSeqNum = reqSeqNum;
  }
}
