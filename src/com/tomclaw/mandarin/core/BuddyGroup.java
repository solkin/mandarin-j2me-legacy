package com.tomclaw.mandarin.core;

import com.tomclaw.tcuilite.GroupHeader;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public abstract class BuddyGroup extends GroupHeader {

  public String userId;

  public BuddyGroup() {
    this( "" );
  }

  public BuddyGroup( String userId ) {
    super( userId );
    this.userId = userId;
    isCollapsed = true;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId( String userId ) {
    this.userId = userId;
  }

  public abstract void updateUiData();
}
