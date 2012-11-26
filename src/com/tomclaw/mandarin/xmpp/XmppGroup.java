package com.tomclaw.mandarin.xmpp;

import com.tomclaw.mandarin.main.BuddyGroup;
import com.tomclaw.tcuilite.GroupHeader;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class XmppGroup extends GroupHeader implements BuddyGroup {

  public String name;

  public XmppGroup( String name ) {
    super( name );
    this.name = name;
    isCollapsed = true;
  }

  public String getUserId() {
    return name;
  }

  public void setUserId( String name ) {
    this.name = name;
  }

  public void updateUiData() {
    this.title = name;
  }
}
