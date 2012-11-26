package com.tomclaw.mandarin.icq;

import com.tomclaw.mandarin.main.BuddyGroup;
import com.tomclaw.tcuilite.GroupHeader;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class IcqGroup extends GroupHeader implements BuddyGroup {

  public String userId;
  public int buddyType;
  public int groupId;
  public int buddyId;

  public IcqGroup( String userId ) {
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

  public void updateUiData() {
    this.title = userId;
  }
}
