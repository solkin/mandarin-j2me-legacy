package com.tomclaw.mandarin.icq;

import com.tomclaw.mandarin.main.BuddyGroup;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class IcqGroup extends BuddyGroup {

  public int buddyType;
  public int groupId;
  public int buddyId;

  public IcqGroup( String userId ) {
    super( userId );
  }

  public void updateUiData() {
    this.title = userId;
  }
}
