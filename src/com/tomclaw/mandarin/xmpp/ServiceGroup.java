package com.tomclaw.mandarin.xmpp;

import com.tomclaw.tcuilite.GroupHeader;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class ServiceGroup extends GroupHeader {

  public String category = null;

  public ServiceGroup( String category, String title ) {
    super( title );
    this.category = category;
  }
}
