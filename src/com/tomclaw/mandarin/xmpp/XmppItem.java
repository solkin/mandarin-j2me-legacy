package com.tomclaw.mandarin.xmpp;

import com.tomclaw.mandarin.main.BuddyItem;
import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.tcuilite.ChatItem;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class XmppItem extends BuddyItem {

  /**
   * Buddy types
   */
  public static final int NORMAL_BUDDY = 0x0002;
  /**
   * Variables
   */
  public Hashtable resources = new Hashtable();
  public boolean isGroupChat = false;
  public String groupChatNick = null;
  public String groupChatSubject = null;

  public XmppItem() {
    super( "" );
  }

  public XmppItem( String userId ) {
    super( userId );
  }

  public XmppItem( String userId, String name ) {
    super( userId );
    setUserNick( name );
  }

  public int getUnreadCount() {
    int unreadCount = 0;
    Enumeration elements = resources.elements();
    while ( elements.hasMoreElements() ) {
      unreadCount += ( ( Resource ) elements.nextElement() ).unreadCount;
    }
    return unreadCount;
  }

  public int getUnreadCount( String resource ) {
    return getResource( resource ).unreadCount;
  }

  public void setUnreadCount( int unreadCount, String resource ) {
    getResource( resource ).unreadCount = unreadCount;
  }

  public void updateUiData() {
    this.title = ( userNick == null || userNick.length() == 0 ) ? userId
            : userNick;
    if ( resources.size() > 1 ) {
      title += " (" + ( isGroupChat ? ( resources.size() - 1 )
              : resources.size() ) + ")";
    }
    int chatImage = -1;
    weight = 0;
    int status = getStatusId();
    if ( status != XmppStatusUtil.offlineIndex && MidletMain.isSortOnline ) {
      weight = -2;
    }
    if ( getUnreadCount() > 0 ) {
      chatImage = ChatItem.TYPE_PLAIN_MSG;
      if ( MidletMain.isRaiseUnread ) {
        weight = -3;
      }
    }
    imageLeftIndex = new int[]{ chatImage, status };
    imageRightIndex = new int[]{ -1, -1, -1, -1, -1 };
    if ( MidletMain.chatFrame != null ) {
      isBold = ( MidletMain.chatFrame.getChatTab( userId ) != null
              || MidletMain.getBoolean( MidletMain.uniquest,
              String.valueOf( "xmpp" + getUserId().hashCode() ), "ON_TOP" ) );
      weight = isBold ? -3 : weight;
    }
    try {
      if ( MidletMain.uniquest.getGroup( String.valueOf( "xmpp"
              + getUserId().hashCode() ) ) != null ) {
        imageRightIndex[4] = 25;
      }
    } catch ( Throwable ex ) {
    }
  }

  public Resource getResource( String resource ) {
    Resource t_resource = ( Resource ) resources.get( resource );
    if ( t_resource == null ) {
      t_resource = new Resource( resource );
      resources.put( resource, t_resource );
    }
    return t_resource;
  }

  public int getResourcesCount() {
    return resources.size();
  }

  public Resource getUnreadResource() {
    Enumeration elements = resources.elements();
    Resource resource;
    while ( elements.hasMoreElements() ) {
      resource = ( Resource ) elements.nextElement();
      if ( resource.unreadCount > 0 ) {
        return resource;
      }
    }
    return null;
  }

  public Resource getDefaultResource() {
    if ( getResourcesCount() == 1 ) {
      return ( Resource ) resources.elements().nextElement();
    } else {
      return getResource( "" );
    }
  }

  public boolean removeResource( String resource ) {
    return ( resources.remove( resource ) != null );
  }

  public int getStatusId() {
    if ( isGroupChat ) {
      return XmppStatusUtil.groupChatIndex;
    } else {
      if ( !resources.isEmpty() ) {
        Resource resource;
        Enumeration elements = resources.elements();
        while ( elements.hasMoreElements() ) {
          resource = ( Resource ) elements.nextElement();
          if ( resource.status != XmppStatusUtil.offlineIndex ) {
            return resource.status;
          }
        }
      }
    }
    return XmppStatusUtil.offlineIndex;
  }

  public void offlineResources() {
    if ( !resources.isEmpty() ) {
      Resource resource;
      Enumeration elements = resources.elements();
      while ( elements.hasMoreElements() ) {
        resource = ( Resource ) elements.nextElement();
        resource.status = XmppStatusUtil.offlineIndex;
      }
    }
  }

  public int getBuddyType() {
    return NORMAL_BUDDY;
  }
}
