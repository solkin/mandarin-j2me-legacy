package com.tomclaw.mandarin.xmpp;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class Identity {

  public String category;
  public String type;
  public String name;

  public Identity( String category, String type, String name ) {
    this.category = category;
    this.type = type;
    this.name = name;
  }
}
