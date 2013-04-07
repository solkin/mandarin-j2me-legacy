package com.tomclaw.mandarin.xmpp;

import com.tomclaw.mandarin.main.ActionExec;
import com.tomclaw.mandarin.main.InfoFrame;
import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import java.io.IOException;
import java.util.Vector;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class GroupChatUsersFrame extends Window {

  private XmppAccountRoot xmppAccountRoot;
  private String groupChatJid;
  private boolean isRole;
  private Tab tab;
  private List participantList;
  private List moderatorList;
  private List memberList;
  private List adminList;
  private List ownerList;
  private List outcastList;
  private String requestId = "";

  public GroupChatUsersFrame( final XmppAccountRoot xmppAccountRoot,
          final String groupChatJid, final boolean isRole ) {
    super( MidletMain.screen );
    this.xmppAccountRoot = xmppAccountRoot;
    this.isRole = isRole;
    this.groupChatJid = groupChatJid;
    /** Header **/
    this.header = new Header( Localization.getMessage( "GROUP_CHAT_USERS_FRAME" ) );
    /** Creating soft **/
    soft = new Soft( MidletMain.screen );
    /** Left soft items **/
    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {

      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    final PopupItem infoPopupItem = new PopupItem(
            Localization.getMessage( "INFO_AFFILIATION" ) ) {

      public void actionPerformed() {
        if ( ( ( List ) tab.gObject ).selectedIndex != -1
                && ( ( List ) tab.gObject ).selectedIndex < ( ( List ) tab.gObject ).items.size() ) {
          GroupChatUser groupChatUser = ( GroupChatUser ) ( ( List ) tab.gObject ).items.elementAt(
                  ( ( List ) tab.gObject ).selectedIndex );
          String[] param = null;
          String[] value = null;
          if ( groupChatUser.affiliation != null ) {
            param = new String[]{ "AFFILIATION_JID", "NICK", "AFFILIATION_REASON", "AFFILIATION" };
            value = new String[]{ groupChatUser.jid, groupChatUser.nick,
              groupChatUser.reason == null ? Localization.getMessage( "NO_REASON" ) : groupChatUser.reason,
              Localization.getMessage( groupChatUser.affiliation.toUpperCase() ) };
          } else if ( groupChatUser.role != null ) {
            param = new String[]{ "AFFILIATION_JID", "NICK", "AFFILIATION_REASON", "ROLE" };
            value = new String[]{ groupChatUser.jid, groupChatUser.nick, groupChatUser.reason == null ? Localization.getMessage( "NO_REASON" ) : groupChatUser.reason,
              Localization.getMessage( groupChatUser.role.toUpperCase() ) };
          }
          InfoFrame infoFrame = new InfoFrame( param, value );
          infoFrame.s_prevWindow = GroupChatUsersFrame.this;
          MidletMain.screen.setActiveWindow( infoFrame );
        }
      }
    };
    PopupItem roleItem = new PopupItem( Localization.getMessage( "ROLE_INFO" ) ) {

      public void actionPerformed() {
        infoPopupItem.actionPerformed();
      }
    };
    PopupItem affiliationItem = new PopupItem( Localization.getMessage( "AFFILIATION_ITEM" ) );
    affiliationItem.addSubItem( new PopupItem( Localization.getMessage( "ADD_AFFILIATION" ) ) {

      public void actionPerformed() {
        MidletMain.affiliationAddFrame = new AffiliationAddFrame( xmppAccountRoot, groupChatJid );
        MidletMain.affiliationAddFrame.s_prevWindow = GroupChatUsersFrame.this;
        MidletMain.screen.setActiveWindow( MidletMain.affiliationAddFrame );
      }
    } );
    affiliationItem.addSubItem( new PopupItem( Localization.getMessage( "REMOVE_AFFILIATION" ) ) {

      public void actionPerformed() {
        try {
          removeItem();
        } catch ( IOException ex ) {
          ActionExec.showFail( Localization.getMessage( "IO_EXCEPTION" ) );
        }
      }
    } );
    affiliationItem.addSubItem( infoPopupItem );
    /** Creating tab **/
    tab = new Tab( MidletMain.screen );
    if ( isRole ) {
      participantList = new List();
      moderatorList = new List();
      tab.addTabItem( new TabItem( Localization.getMessage( "GROUP_CHAT_PARTICIPANT" ), 0, -1 ) );
      tab.addTabItem( new TabItem( Localization.getMessage( "GROUP_CHAT_MODERATOR" ), 0, -1 ) );
      tab.setGObject( participantList );
      soft.rightSoft = roleItem;
    } else {
      memberList = new List();
      adminList = new List();
      ownerList = new List();
      outcastList = new List();
      tab.addTabItem( new TabItem( Localization.getMessage( "GROUP_CHAT_MEMBER" ), 0, -1 ) );
      tab.addTabItem( new TabItem( Localization.getMessage( "GROUP_CHAT_ADMIN" ), 0, -1 ) );
      tab.addTabItem( new TabItem( Localization.getMessage( "GROUP_CHAT_OWNER" ), 0, -1 ) );
      tab.addTabItem( new TabItem( Localization.getMessage( "GROUP_CHAT_OUTCAST" ), 0, -1 ) );
      tab.setGObject( memberList );
      soft.rightSoft = affiliationItem;
    }
    tab.tabEvent = new TabEvent() {

      public void stateChanged( int previousIndex, int selectedIndex, int tabsCount ) {
        if ( isRole ) {
          if ( selectedIndex == 0 ) {
            tab.setGObject( participantList );
          } else if ( selectedIndex == 1 ) {
            tab.setGObject( moderatorList );
          }
        } else {
          if ( selectedIndex == 0 ) {
            tab.setGObject( memberList );
          } else if ( selectedIndex == 1 ) {
            tab.setGObject( adminList );
          } else if ( selectedIndex == 2 ) {
            tab.setGObject( ownerList );
          } else if ( selectedIndex == 3 ) {
            tab.setGObject( outcastList );
          }
        }
      }
    };
    tab.selectedIndex = 0;
    /** Setting GObject **/
    setGObject( tab );
    /** Requesting lists **/
    try {
      requestLists();
    } catch ( IOException ex ) {
      ActionExec.showFail( Localization.getMessage( "USERS_READING_FAILED" ) );
    }
  }

  public final void requestLists() throws IOException {
    requestId = "grchus_frm_".concat( xmppAccountRoot.xmppSession.getId() );
    if ( isRole ) {
      XmppSender.requestGroupChatLists( xmppAccountRoot.xmppSession, groupChatJid, requestId.concat( "participant" ), "role", "participant" );
      XmppSender.requestGroupChatLists( xmppAccountRoot.xmppSession, groupChatJid, requestId.concat( "moderator" ), "role", "moderator" );
    } else {
      XmppSender.requestGroupChatLists( xmppAccountRoot.xmppSession, groupChatJid, requestId.concat( "member" ), "affiliation", "member" );
      XmppSender.requestGroupChatLists( xmppAccountRoot.xmppSession, groupChatJid, requestId.concat( "admin" ), "affiliation", "admin" );
      XmppSender.requestGroupChatLists( xmppAccountRoot.xmppSession, groupChatJid, requestId.concat( "owner" ), "affiliation", "owner" );
      XmppSender.requestGroupChatLists( xmppAccountRoot.xmppSession, groupChatJid, requestId.concat( "outcast" ), "affiliation", "outcast" );
    }
    MidletMain.screen.setWaitScreenState( true );
  }

  public void removeItem() throws IOException {
    if ( ( ( List ) tab.gObject ).selectedIndex != -1
            && ( ( List ) tab.gObject ).selectedIndex < ( ( List ) tab.gObject ).items.size() ) {
      GroupChatUser groupChatUser = ( GroupChatUser ) ( ( List ) tab.gObject ).items.elementAt( ( ( List ) tab.gObject ).selectedIndex );
      requestId = "grchus_rm_frm_".concat( xmppAccountRoot.xmppSession.getId() );
      MidletMain.screen.setWaitScreenState( true );
      XmppSender.affiliationAddGroupChatLists(
              xmppAccountRoot.xmppSession, groupChatJid,
              requestId, groupChatUser.jid, "none", "" );
      requestLists();
    }
  }

  public void setResult( XmppAccountRoot xmppAccountRoot, String id, Vector items ) {
    if ( this.xmppAccountRoot.equals( xmppAccountRoot ) && id.startsWith( requestId ) ) {
      if ( id.endsWith( "participant" ) ) {
        participantList.items = items;
      } else if ( id.endsWith( "moderator" ) ) {
        moderatorList.items = items;
      } else if ( id.endsWith( "member" ) ) {
        memberList.items = items;
      } else if ( id.endsWith( "admin" ) ) {
        adminList.items = items;
      } else if ( id.endsWith( "owner" ) ) {
        ownerList.items = items;
      } else if ( id.endsWith( "outcast" ) ) {
        outcastList.items = items;
      }
      MidletMain.screen.setWaitScreenState( false );
    }
  }

  public void setError( XmppAccountRoot xmppAccountRoot, String id ) {
    if ( this.xmppAccountRoot.equals( xmppAccountRoot )
            && id.startsWith( requestId ) ) {
      MidletMain.screen.setWaitScreenState( false );
      ActionExec.showFail( Localization.getMessage( "PERMISSION_DENIED" ) );
      requestId = "";
    }
  }
}
