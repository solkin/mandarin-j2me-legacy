package com.tomclaw.mandarin.main;

import com.tomclaw.mandarin.xmpp.Resource;
import com.tomclaw.mandarin.xmpp.XmppItem;
import com.tomclaw.tcuilite.TabItem;
import com.tomclaw.utils.LogUtil;
import java.util.Vector;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class ChatTab extends TabItem {

  public AccountRoot accountRoot;
  public BuddyItem buddyItem;
  public Vector chatItems;
  public int statusFileHash;
  public int msgFileHash;
  public Resource resource;
  public String titleAddon;

  public ChatTab( AccountRoot accountRoot, BuddyItem buddyItem, int statusFileHash, int msgFileHash ) {
    super( buddyItem.getUserNick(), statusFileHash, buddyItem.getLeftImages()[1] );
    this.accountRoot = accountRoot;
    this.buddyItem = buddyItem;
    this.statusFileHash = statusFileHash;
    this.msgFileHash = msgFileHash;
  }

  public ChatTab( AccountRoot accountRoot, BuddyItem buddyItem, Resource resource, int statusFileHash, int msgFileHash ) {
    super( buddyItem.getUserNick(), statusFileHash, buddyItem.getLeftImages()[1] );
    this.accountRoot = accountRoot;
    this.buddyItem = buddyItem;
    this.statusFileHash = statusFileHash;
    this.msgFileHash = msgFileHash;
    this.resource = resource;
  }

  public void updateChatCaption() {
    if ( buddyItem != null ) {
      String resourceTitle = null;
      if ( resource != null ) {
        resourceTitle = resource.resource;
      }
      if ( buddyItem.getUnreadCount( resourceTitle ) == 0 ) {
        this.imageFileHash = statusFileHash;
        if ( buddyItem instanceof XmppItem /*&& ((XmppItem) buddyItem).isGroupChat*/ && resource != null && resource.resource.length() > 0 ) {
          this.imageIndex = resource.statusIndex;
        } else {
          this.imageIndex = buddyItem.getLeftImages()[1];
        }
      } else {
        this.imageFileHash = msgFileHash;
        this.imageIndex = buddyItem.getLeftImages()[0];
      }
      if ( buddyItem.getTypingStatus() ) {
        this.imageFileHash = msgFileHash;
        this.imageIndex = buddyItem.getLeftImages()[0];
      }
      titleAddon = "";
      if ( resource != null ) {
        if ( buddyItem instanceof XmppItem ) {
          if ( ( ( XmppItem ) buddyItem ).isGroupChat && ( resource == null || resource.resource.length() == 0 ) ) {
            titleAddon = " (".concat( String.valueOf( ( ( XmppItem ) buddyItem ).getResourcesCount() - 1 ) ).concat( ")" );
          } else {
            if ( resource.resource.length() > 0 ) {
              titleAddon = " [".concat( resource.resource ).concat( "]" );
            }
          }
        }
      }
      this.title = buddyItem.getUserNick().concat( titleAddon ); // + (resource == null ? "" : " [" + resource.resource + "]");
      LogUtil.outMessage( title );
    }
  }
}
