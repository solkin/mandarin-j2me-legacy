package com.tomclaw.mandarin.xmpp;

import com.tomclaw.tcuilite.ListItem;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class Bookmark extends ListItem {

  public String jid;
  public String name;
  public String nick;
  public String password;
  public boolean minimize;
  public boolean autojoin;

  public Bookmark() {
    jid = "";
    name = "";
    nick = "";
    password = "";
    minimize = false;
    autojoin = false;
  }

  public Bookmark( String jid, String name, String minimize, String autojoin ) {
    this.jid = jid;
    this.name = name;
    this.title = name;
    if ( minimize != null && ( minimize.equals( "1" ) || minimize.equals( "true" ) ) ) {
      this.minimize = true;
    } else {
      this.minimize = false;
    }
    if ( autojoin != null && ( autojoin.equals( "1" ) || autojoin.equals( "true" ) ) ) {
      this.autojoin = true;
    } else {
      this.autojoin = false;
    }
  }
}
