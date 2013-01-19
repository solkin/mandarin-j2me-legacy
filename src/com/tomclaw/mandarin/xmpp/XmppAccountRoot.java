package com.tomclaw.mandarin.xmpp;

import com.tomclaw.mandarin.dc.DirectConnection;
import com.tomclaw.mandarin.main.*;
import com.tomclaw.tcuilite.GroupHeader;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;
import java.io.IOException;
import java.util.Vector;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class XmppAccountRoot extends AccountRoot {

  /** Data **/
  public String domain;
  public String username;
  public String resource;
  public int priority;
  /** Runtime **/
  public XmppSession xmppSession;
  public XmppGroup conferenceGroup = null;
  public XmppGroup tempGroup;

  public XmppAccountRoot( String userId ) {
    super( userId );
  }

  public void construct() {
  }

  public void initSpecialData() {
    /** New session instance **/
    xmppSession = new XmppSession( this );

    username = userId.substring( 0, userId.indexOf( '@' ) );
    domain = userId.substring( userId.indexOf( '@' ) + 1 );
    //resource = MidletMain.getString( MidletMain.accounts, jid, "resource" );

    if ( StringUtil.isNullOrEmpty( host ) ) {
      host = domain;
    }

    resource = "Mandarin IM ".concat( MidletMain.version ).concat( " [" ).concat( MidletMain.build.concat( "]" ) );

    // if ( isStart ) {
    // transactionManager = new TransactionManager();
    // serviceMessages = new ServiceMessages();
    // }
  }

  public void saveSpecialSettings() throws Throwable {
    MidletMain.accounts.addItem( userId, "resource", resource );
  }

  public String getAccType() {
    return "xmpp";
  }

  public void sendTypingStatus( String userId, boolean b ) {
  }

  public byte[] sendMessage( BuddyItem buddyItem, String string, String resource ) throws IOException {
    String fullJid = buddyItem.getUserId();
    if ( resource.length() > 0 ) {
      fullJid += "/".concat( resource );
    }
    String type;
    if ( ( ( XmppItem ) buddyItem ).isGroupChat && resource.length() == 0 ) {
      type = "groupchat";
    } else {
      type = "chat";
    }
    LogUtil.outMessage( "fullJid: " + fullJid );
    LogUtil.outMessage( "string: " + string );
    LogUtil.outMessage( "type: " + type );
    return XmppSender.sendMessage( xmppSession, fullJid, string, type, false ).getBytes();
  }

  public void updateMainFrameBuddyList() {
    for ( int c = 0; c < buddyItems.size(); c++ ) {
      LogUtil.outMessage( ( ( GroupHeader ) buddyItems.elementAt( c ) ).title );
      for ( int i = 0; i < ( ( GroupHeader ) buddyItems.elementAt( c ) ).getChildsCount(); i++ ) {
        ( ( XmppItem ) ( ( GroupHeader ) buddyItems.elementAt( c ) ).getChilds().elementAt( i ) ).updateUiData();
        LogUtil.outMessage( "\t" + ( ( XmppItem ) ( ( GroupHeader ) buddyItems.elementAt( c ) ).getChilds().elementAt( i ) ).getUserId() );
      }
    }
    updateMainFrameUI();
  }

  public XmppItem getBuddyItem( String jid ) {
    return ( XmppItem ) xmppSession.roster.get( XmppSession.getClearJid( jid ) );
  }

  public void removeBuddyItem( String jid ) {
    for ( int c = 0; c < buddyItems.size(); c++ ) {
      for ( int i = 0; i < ( ( GroupHeader ) buddyItems.elementAt( c ) ).getChildsCount(); i++ ) {
        XmppItem xmppItem = ( ( XmppItem ) ( ( GroupHeader ) buddyItems.elementAt( c ) ).getChilds().elementAt( i ) );
        if ( xmppItem.userId.equals( jid ) ) {
          this.unrMsgs -= xmppItem.getUnreadCount();
          ( ( GroupHeader ) buddyItems.elementAt( c ) ).getChilds().removeElementAt( i );
        }
      }
    }
  }

  public void addTempItem( XmppItem xmppItem ) {
    if ( tempGroup == null || !buddyItems.contains( tempGroup ) ) {
      tempGroup = new XmppGroup( Localization.getMessage( "XMPP_TEMP_GROUP" ) );
      buddyItems.addElement( tempGroup );
    }
    tempGroup.addChild( xmppItem );
    xmppSession.roster.put( xmppItem.userId, xmppItem );
    updateMainFrameUI();
  }

  public String getStatusImages() {
    return "/res/groups/img_xmppstatus.png";
  }

  public void setTreeItems( Vector buddyList ) {
    this.buddyItems = buddyList;
  }

  public void setPrivateItems( Vector privateList ) {
  }
  
  public BuddyGroup getGroupInstance() {
    return new XmppGroup();
  }

  public BuddyItem getBuddyInstance() {
    return new XmppItem();
  }

  public Cookie addGroup( BuddyGroup buddyGroup ) throws IOException {
    return null;
  }

  public Cookie addBuddy( BuddyItem buddyItem, BuddyGroup buddyGroup ) 
          throws IOException {
    return null;
  }

  public Cookie renameBuddy( String itemName, BuddyItem buddyItem, 
          String phones ) throws IOException {
    return null;
  }

  public Cookie renameGroup( String text, BuddyGroup buddyGroup ) 
          throws IOException {
    return null;
  }

  public void requestAuth( String text, BuddyItem buddyItem ) 
          throws IOException {
  }

  public void acceptAuthorization( BuddyItem buddyItem ) throws IOException {
  }

  public void requestInfo( String userId, int reqSeqNum ) throws IOException {
  }

  public Cookie removeBuddy( BuddyItem buddyItem ) throws IOException {
    return null;
  }

  public Cookie removeGroup( BuddyGroup buddyGroup ) throws IOException {
    return null;
  }

  public DirectConnection getDirectConnectionInstance() {
    return new XmppIBBytestream( this );
  }

  public int getNextBuddyId() {
    return 0;
  }
  
  public int getNextGroupId() {
    return 0;
  }

  public void connectAction( final int statusIndex ) {
    if ( isConnecting || this.statusIndex != 0 ) {
      return;
    }
    /** Need to connect **/
    new Thread() {
      public void run() {
        try {
          do {
            try {
              xmppSession.connect( statusIndex );
              XmppAccountRoot.this.statusIndex = statusIndex;
              MidletMain.mainFrame.updateAccountsStatus();
              isConnecting = false;
              return;
            } catch ( IOException ex ) {
              LogUtil.outMessage( "IO Exception" );
              ActionExec.showError( Localization.getMessage( "IO_EXCEPTION" ) );
            } catch ( Throwable ex ) {
              LogUtil.outMessage( "Throwable" );
              ActionExec.showError( Localization.getMessage( "THROWABLE" ) );
            }
            sleep( MidletMain.reconnectTime );
          } while ( MidletMain.autoReconnect );
        } catch ( InterruptedException ex ) {
        }
        isConnecting = false;
      }
    }.start();
  }
}
