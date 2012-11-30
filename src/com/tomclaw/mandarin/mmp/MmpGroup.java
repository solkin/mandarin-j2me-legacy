package com.tomclaw.mandarin.mmp;

import com.tomclaw.mandarin.main.BuddyGroup;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class MmpGroup extends BuddyGroup {

  public int flags;
  public int buddyType;
  public int groupId;
  public int buddyId;
  public long contactId;

  public MmpGroup( String userId ) {
    super( userId );
  }

  public void updateUiData() {
    if ( userId != null ) {
      this.title = userId;
    }
  }

  public int getId() {
    return flags >> 24;
  }
}
