package com.tomclaw.mandarin.xmpp;

import com.tomclaw.mandarin.main.BuddyGroup;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class XmppGroup extends BuddyGroup {

  public XmppGroup( String userId ) {
    super( userId );
  }

  public void updateUiData() {
    this.title = userId;
  }
}
