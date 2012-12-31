package com.tomclaw.mandarin.xmpp;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class Resource {

  public int statusIndex = XmppStatusUtil.offlineIndex;
  public String statusText = null;
  public String resource = null;
  public int unreadCount = 0;
  public String caps = null;
  public String ver = null;

  public Resource( String resource ) {
    this.resource = resource;
  }
}
