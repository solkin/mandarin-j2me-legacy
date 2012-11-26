package com.tomclaw.mandarin.mmp;

import com.tomclaw.mandarin.main.BuddyGroup;
import com.tomclaw.tcuilite.GroupHeader;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class MmpGroup extends GroupHeader implements BuddyGroup {

  public int flags;
  public String userId;
  public int buddyType;
  public int groupId;
  public int buddyId;
  public long contactId;

  public MmpGroup( String userId ) {
    super( userId );
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId( String userId ) {
    this.userId = userId;
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
