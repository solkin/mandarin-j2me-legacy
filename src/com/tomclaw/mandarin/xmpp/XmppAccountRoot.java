package com.tomclaw.mandarin.xmpp;

import com.tomclaw.bingear.GroupNotFoundException;
import com.tomclaw.bingear.IncorrectValueException;
import com.tomclaw.mandarin.dc.DirectConnection;
import com.tomclaw.mandarin.main.*;
import com.tomclaw.tcuilite.GroupHeader;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import java.io.IOException;
import java.util.Vector;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class XmppAccountRoot implements AccountRoot {

  public String jid;
  public String name;
  /** Data **/
  public String domain;
  public String host;
  public String port;
  public String username;
  public String password;
  public String resource;
  public int priority;
  public boolean isUseSsl = true;
  /** Runtime **/
  public int statusId = XmppStatusUtil.offlineIndex;
  public XmppSession xmppSession;
  private String buddyListFile = null;
  public Vector buddyItems = new Vector();
  public XmppGroup conferenceGroup = null;
  public XmppGroup tempGroup;
  /**
   * Settings
   */
  public boolean isShowGroups = true;
  public boolean isShowOffline = false;
  /**
   * Runtime
   */
  public int yOffset = 0;
  public int selectedColumn = 0;
  public int selectedRow = 0;
  public int unrMsgs = 0;
  /** Objects **/
  public ServiceMessages serviceMessages = null;
  public TransactionManager transactionManager = null;
  public TransactionsFrame transactionsFrame = null;

  public XmppAccountRoot( String jid ) {
    this.jid = jid;
  }

  public XmppAccountRoot( String jid, String name ) {
    this.jid = jid;
    this.name = name;
  }

  public AccountRoot init( boolean isStart ) {
    name = MidletMain.getString( MidletMain.accounts, jid, "nick" );
    username = jid.substring( 0, jid.indexOf( '@' ) );
    domain = jid.substring( jid.indexOf( '@' ) + 1 );
    resource = MidletMain.getString( MidletMain.accounts, jid, "resource" );
    password = MidletMain.getString( MidletMain.accounts, jid, "pass" );
    host = MidletMain.getString( MidletMain.accounts, jid, "host" );
    port = MidletMain.getString( MidletMain.accounts, jid, "port" );
    isUseSsl = MidletMain.getBoolean( MidletMain.accounts, jid, "ussl" );
    if ( host.length() == 0 ) {
      host = domain;
    }

    LogUtil.outMessage( "jid = " + jid );
    LogUtil.outMessage( "username = " + username );
    LogUtil.outMessage( "domain = " + domain );
    LogUtil.outMessage( "resource = " + resource );
    LogUtil.outMessage( "password = " + password );
    LogUtil.outMessage( "host = " + host );
    LogUtil.outMessage( "port = " + port );
    LogUtil.outMessage( "ussl = " + isUseSsl );

    resource = "Mandarin IM ".concat( MidletMain.version ).concat( " [" ).concat( MidletMain.build.concat( "]" ) );

    if ( isStart ) {
      /** New session instance **/
      xmppSession = new XmppSession( this );
      transactionManager = new TransactionManager();
      isShowGroups = MidletMain.getBoolean( MidletMain.accounts, jid, "isShowGroups" );
      isShowOffline = MidletMain.getBoolean( MidletMain.accounts, jid, "isShowOffline" );
      LogUtil.outMessage( "isShowGroups = " + String.valueOf( isShowGroups ) );
      LogUtil.outMessage( "isShowOffline = " + String.valueOf( isShowOffline ) );
      serviceMessages = new ServiceMessages();
      buddyListFile = getAccType().concat( String.valueOf( jid.hashCode() ) ).concat( ".dat" );
      loadOfflineBuddyList();
    }
    return this;
  }

  public void saveAllSettings() {
    try {
      MidletMain.accounts.addItem( jid, "resource", resource );
      MidletMain.accounts.addItem( jid, "isShowGroups", String.valueOf( isShowGroups ) );
      MidletMain.accounts.addItem( jid, "isShowOffline", String.valueOf( isShowOffline ) );
      LogUtil.outMessage( "isShowGroups = " + String.valueOf( isShowGroups ) );
      LogUtil.outMessage( "isShowOffline = " + String.valueOf( isShowOffline ) );
    } catch ( GroupNotFoundException ex ) {
      LogUtil.outMessage( "Group not found exception: " + ex.getMessage() );
    } catch ( IncorrectValueException ex ) {
      LogUtil.outMessage( "Incorrect value exception: " + ex.getMessage() );
    }
    MidletMain.saveRmsData( true, false, false );
    LogUtil.outMessage( "RMS accounts saving complete" );
  }

  public void loadOfflineBuddyList() {
    MidletMain.loadOfflineBuddyList( this, buddyListFile, buddyItems );
  }

  public int getUnrMsgs() {
    return unrMsgs;
  }

  public void setUnrMsgs( int unrMsgs ) {
    this.unrMsgs = unrMsgs;
  }

  public long getStatusId() {
    return statusId;
  }

  public String getUserId() {
    return jid;
  }

  public String getUserPassword() {
    return password;
  }

  public boolean getUseSsl() {
    return isUseSsl;
  }

  public void setUseSsl( boolean isUseSsl ) {
    this.isUseSsl = isUseSsl;
  }

  public void setUserId( String userId ) {
    this.jid = userId;
  }

  public void setUserPassword( String userPassword ) {
    this.password = userPassword;
  }

  public String getUserNick() {
    if ( this.name == null ) {
      return jid;
    }
    return name;
  }

  public void setUserNick( String userNick ) {
    this.name = userNick;
  }

  public String getAccType() {
    return "xmpp";
  }

  public String getHost() {
    return host;
  }

  public String getPort() {
    return port;
  }

  public int getStatusIndex() {
    return statusId;
  }

  public void sendTypingStatus( String userId, boolean b ) {
  }

  public Vector getBuddyItems() {
    return buddyItems;
  }

  public void setBuddyItems( Vector buddyItems ) {
    this.buddyItems = buddyItems;
  }

  public void setYOffset( int yOffset ) {
    this.yOffset = yOffset;
  }

  public void setSelectedIndex( int selectedColumn, int selectedRow ) {
    this.selectedColumn = selectedColumn;
    this.selectedRow = selectedRow;
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
        if ( xmppItem.jid.equals( jid ) ) {
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
    xmppSession.roster.put( xmppItem.jid, xmppItem );
    updateMainFrameUI();
  }

  public void updateMainFrameUI() {
    if ( MidletMain.mainFrame.getActiveAccountRoot().equals( this ) ) {
      LogUtil.outMessage( "This account root is active" );
      MidletMain.mainFrame.buddyList.items = this.buddyItems;
      MidletMain.screen.repaint();
    }
  }

  public ServiceMessages getServiceMessages() {
    return serviceMessages;
  }

  public String getStatusImages() {
    return "/res/groups/img_xmppstatus.png";
  }

  public void offlineAllBuddyes() {
    try {
      GroupHeader tempGroupItem;
      for ( int i = 0; i < buddyItems.size(); i++ ) {
        LogUtil.outMessage( buddyItems.elementAt( i ).toString() );
        tempGroupItem = ( ( GroupHeader ) buddyItems.elementAt( i ) );
        for ( int j = 0; j < tempGroupItem.getChildsCount(); j++ ) {
          XmppItem tempXmppItem = ( ( XmppItem ) tempGroupItem.getChilds().elementAt( j ) );
          tempXmppItem.offlineResources();
        }
      }
    } catch ( Throwable ex1 ) {
    }
  }

  public void offlineAccount() {
    statusId = XmppStatusUtil.offlineIndex;
  }

  public void setTreeItems( Vector buddyList ) {
    this.buddyItems = buddyList;
  }

  public void setPrivateItems( Vector privateList ) {
  }

  public void sortBuddyes() {
  }

  public void updateOfflineBuddylist() {
    MidletMain.updateOfflineBuddylist( buddyListFile, buddyItems );
  }

  public void setShowGroups( boolean isShowGroups ) {
    this.isShowGroups = isShowGroups;
  }

  public void setShowOffline( boolean isShowOffline ) {
    this.isShowOffline = isShowOffline;
  }

  public boolean getShowGroups() {
    return isShowGroups;
  }

  public boolean getShowOffline() {
    return isShowOffline;
  }

  public BuddyItem getItemInstance() {
    return new XmppItem();
  }

  public Cookie addGroup( String groupName, long itemId ) throws IOException {
    return null;
  }

  public Cookie addBuddy( String buddyId, BuddyGroup buddyGroup, String nickName, int type, long itemId ) throws IOException {
    return null;
  }

  public Cookie renameBuddy( String itemName, BuddyItem buddyItem, String phones ) throws IOException {
    return null;
  }

  public Cookie renameGroup( String text, BuddyGroup buddyGroup ) throws IOException {
    return null;
  }

  public void requestAuth( String text, BuddyItem buddyItem ) throws IOException {
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

  public TransactionManager getTransactionManager() {
    return transactionManager;
  }

  public void setTransactionManager( TransactionManager transactionManager ) {
    this.transactionManager = transactionManager;
  }

  public TransactionsFrame getTransactionsFrame() {
    if ( transactionsFrame == null ) {
      transactionsFrame = new TransactionsFrame( this );
      transactionsFrame.s_prevWindow = MidletMain.mainFrame;
    } else {
      transactionsFrame.updateTransactions();
    }
    return transactionsFrame;
  }

  public void setTransactionsFrame( TransactionsFrame transactionsFrame ) {
    this.transactionsFrame = transactionsFrame;
  }

  public DirectConnection getDirectConnectionInstance() {
    return new XmppIBBytestream( this );
  }

  public long getNextItemId() {
    return 0;
  }
}
