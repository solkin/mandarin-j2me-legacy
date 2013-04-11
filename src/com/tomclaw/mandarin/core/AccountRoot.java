package com.tomclaw.mandarin.core;

import com.tomclaw.mandarin.dc.DirectConnection;
import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.mandarin.main.TransactionsFrame;
import com.tomclaw.tcuilite.GroupHeader;
import com.tomclaw.utils.LogUtil;
import java.io.IOException;
import java.util.Vector;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public abstract class AccountRoot {

  /** Data **/
  public String userId;
  public String userNick;
  public String userPassword;
  public String host = "";
  public String port = "";
  public Vector buddyItems = new Vector();
  public int statusIndex = 0;
  public String statusText = "Mandarin ".concat( MidletMain.version )
          .concat( " [" ).concat( MidletMain.build.concat( "]" ) );
  public boolean isStatusReadable = true;
  private String buddyListFile = null;
  public boolean isUseSsl = false;
  /** Frames and managers **/
  private TransactionManager transactionManager = null;
  private TransactionsFrame transactionsFrame = null;
  private ServiceMessages serviceMessages = null;
  /** Threads and states **/
  public boolean isConnecting = false;
  /** Settings **/
  public boolean isShowGroups = true;
  public boolean isShowOffline = false;
  /** Runtime **/
  public int yOffset = 0;
  public int selectedColumn = 0;
  public int selectedRow = 0;
  public int unrMsgs = 0;
  public boolean isReset;

  public AccountRoot( String userId ) {
    this.userId = userId;
    AccountRoot.this.construct();
  }

  public abstract void construct();

  public abstract void connectAction( final int statusIndex );

  public AccountRoot init( boolean isStart ) {
    /** Loading user nick, user password **/
    userNick = MidletMain.getString( MidletMain.accounts, userId, "nick" );
    userPassword = MidletMain.getString( MidletMain.accounts, userId, "pass" );
    host = MidletMain.getString( MidletMain.accounts, userId, "host" );
    port = MidletMain.getString( MidletMain.accounts, userId, "port" );
    if ( isStart ) {
      loadStatus( statusIndex );
      /** Settings **/
      isShowGroups = MidletMain.getBoolean( MidletMain.accounts, userId, "isShowGroups" );
      isShowOffline = MidletMain.getBoolean( MidletMain.accounts, userId, "isShowOffline" );
      LogUtil.outMessage( "isShowGroups = " + String.valueOf( isShowGroups ) );
      LogUtil.outMessage( "isShowOffline = " + String.valueOf( isShowOffline ) );
      buddyListFile = getAccType().concat( String.valueOf( getUserId().hashCode() ) ).concat( ".dat" );
      loadOfflineBuddyList();
      /** Loading special data **/
      initSpecialData();
    }
    return this;
  }

  public abstract void initSpecialData();

  public int getUnrMsgs() {
    return unrMsgs;
  }

  public void setUnrMsgs( int unrMsgs ) {
    this.unrMsgs = unrMsgs;
  }

  public int getStatusIndex() {
    return statusIndex;
  }

  public void setUserId( String userId ) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserPassword( String userPassword ) {
    this.userPassword = userPassword;
  }

  public String getUserPassword() {
    return userPassword;
  }

  public void setUserNick( String userNick ) {
    this.userNick = userNick;
  }

  public String getUserNick() {
    return userNick;
  }

  public abstract String getAccType();

  public String getHost() {
    return host;
  }

  public String getPort() {
    return port;
  }

  public boolean getUseSsl() {
    return isUseSsl;
  }

  public void setUseSsl( boolean isUseSsl ) {
    this.isUseSsl = isUseSsl;
  }

  public abstract void sendTypingStatus( String userId, boolean b );

  public Vector getBuddyItems() {
    return buddyItems;
  }

  public void setStatusText( String statusText, boolean isStatusReadable ) {
    this.statusText = statusText;
    this.isStatusReadable = isStatusReadable;
    saveAllSettings();
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

  public void saveAllSettings() {
    try {
      MidletMain.accounts.addItem( userId, "isShowGroups", String.valueOf( isShowGroups ) );
      MidletMain.accounts.addItem( userId, "isShowOffline", String.valueOf( isShowOffline ) );
      saveSpecialSettings();
    } catch ( Throwable ex ) {
      LogUtil.outMessage( "Error while saving settings: " + ex.getMessage() );
    }
    MidletMain.saveRmsData( true, false, false );
    LogUtil.outMessage( "RMS accounts saving complete" );
  }

  public abstract void saveSpecialSettings() throws Throwable;

  public abstract byte[] sendMessage( BuddyItem buddyItem, String string, String resource );

  public void updateMainFrameBuddyList() {
    for ( int c = 0; c < buddyItems.size(); c++ ) {
      for ( int i = 0; i < ( ( GroupHeader ) buddyItems.elementAt( c ) ).getChildsCount(); i++ ) {
        ( ( BuddyItem ) ( ( GroupHeader ) buddyItems.elementAt( c ) ).getChilds().elementAt( i ) ).updateUiData();
      }
    }
    if ( MidletMain.mainFrame.getActiveAccountRoot().equals( this ) ) {
      MidletMain.mainFrame.buddyList.items = this.buddyItems;
      MidletMain.screen.repaint();
    }
  }

  public ServiceMessages getServiceMessages() {
    if ( serviceMessages == null ) {
      serviceMessages = new ServiceMessages();
    }
    return serviceMessages;
  }

  public abstract String getStatusImages();

  public void offlineAllBuddyes() {
    GroupHeader tempGroupItem;
    for ( int i = 0; i < buddyItems.size(); i++ ) {
      tempGroupItem = ( ( GroupHeader ) buddyItems.elementAt( i ) );
      for ( int j = 0; j < ( ( GroupHeader ) tempGroupItem ).getChildsCount(); j++ ) {
        BuddyItem tempIcqItem = ( ( BuddyItem ) tempGroupItem.getChilds().elementAt( j ) );
        tempIcqItem.setStatusIndex( 0, null );
      }
    }
  }

  public void offlineAccount() {
    statusIndex = 0;
  }

  public abstract void setTreeItems( Vector buddyList );

  public abstract void setPrivateItems( Vector privateList );

  public void updateOfflineBuddylist() {
    MidletMain.updateOfflineBuddylist( buddyListFile, buddyItems );
  }

  public void updateMainFrameUI() {
    if ( MidletMain.mainFrame.getActiveAccountRoot().equals( this ) ) {
      MidletMain.mainFrame.buddyList.items = this.buddyItems;
      MidletMain.screen.repaint();
    }
  }

  public void loadOfflineBuddyList() {
    MidletMain.loadOfflineBuddyList( this, buddyListFile, buddyItems );
    isReset = true;
  }

  public void loadStatus( int statusIndex ) {
    String statusData = MidletMain.getString( MidletMain.statuses,
            "PStatus_".concat( getAccType().toUpperCase() ),
            String.valueOf( statusIndex ) );
    statusText = statusData.substring( 0,
            ( statusData.indexOf( "&rdb" ) == -1 )
            ? statusData.length() : statusData.indexOf( "&rdb" ) );
    isStatusReadable = ( statusData.indexOf( "&rdb" ) == -1 ) ? false
            : statusData.substring( statusData.indexOf( "&rdb" ) + 4 )
            .equals( "true" );
    LogUtil.outMessage( "loadStatus: " + statusText 
            + " [" + isStatusReadable + "]" );
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

  public abstract Cookie addGroup( BuddyGroup buddyGroup );

  public abstract Cookie addBuddy( BuddyItem buddyItem, BuddyGroup buddyGroup );

  public abstract Cookie renameBuddy( String itemName, BuddyItem buddyItem, String phones );

  public abstract Cookie renameGroup( String text, BuddyGroup buddyGroup );

  public abstract void requestAuth( String text, BuddyItem buddyItem );

  public abstract void acceptAuthorization( BuddyItem buddyItem );

  public abstract void requestInfo( String userId, int reqSeqNum );

  public abstract Cookie removeBuddy( BuddyItem buddyItem );

  public abstract Cookie removeGroup( BuddyGroup buddyGroup );

  public abstract BuddyItem getBuddyInstance();

  public abstract BuddyGroup getGroupInstance();

  public TransactionManager getTransactionManager() {
    if ( transactionManager == null ) {
      transactionManager = new TransactionManager();
    }
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

  public abstract DirectConnection getDirectConnectionInstance();

  public abstract int getNextBuddyId();

  public abstract int getNextGroupId();

  public abstract String getStatusDescr( int statusIndex );
}
