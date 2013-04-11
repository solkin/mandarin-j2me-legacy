package com.tomclaw.mandarin.mmp;

import com.tomclaw.mandarin.core.BuddyGroup;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class MmpGroup extends BuddyGroup {

  public int flags;
  public long contactId;

  public MmpGroup() {
    super();
  }

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
