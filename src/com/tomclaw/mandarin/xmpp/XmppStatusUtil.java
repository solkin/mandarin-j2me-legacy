package com.tomclaw.mandarin.xmpp;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class XmppStatusUtil {

  public static String[] statuses = new String[]{ "offline", "online", "away",
    "chat", "invisible", "xa", "dnd" };
  public static int offlineIndex = 0;
  public static int onlineIndex = 1;
  public static int groupChatIndex = 7;

  public static int getStatusIndex( String t_status ) {
    for ( int c = 0; c < statuses.length; c++ ) {
      if ( statuses[c].equals( t_status ) ) {
        return c;
      }
    }
    return 0;
  }

  public static int getStatusCount() {
    return statuses.length;
  }

  public static String getStatusDescr( int index ) {
    if ( index < statuses.length ) {
      return "XMPP_STATUS_" + statuses[index].toUpperCase();
    } else {
      return "XMPP_STATUS_OFFLINE";
    }
  }
}
