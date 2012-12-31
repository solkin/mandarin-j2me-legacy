package com.tomclaw.mandarin.main;

import com.tomclaw.mandarin.dc.DirectConnection;
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

  /**
   * Static settings
   */
  /**
   * Data
   */
  public String userId;
  public String userNick;
  public String userPassword;
  public String host = "";
  public String port = "";
  public Vector buddyItems = new Vector();
  public long statusId = -1; // -1
  private String buddyListFile = null;
  public boolean isUseSsl = false;
  /**
   * Threads and states
   */
  public TransactionManager transactionManager = null;
  public TransactionsFrame transactionsFrame = null;
  public ServiceMessages serviceMessages = null;
  public boolean isConnecting = false;
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
  public boolean isReset;

  public AccountRoot( String userId ) {
    this.userId = userId;
    AccountRoot.this.construct();
  }

  public abstract void construct();

  public abstract void connectAction( final long statusId );

  public AccountRoot init( boolean isStart ) {
    /** Loading user nick, user password **/
    userNick = MidletMain.getString( MidletMain.accounts, userId, "nick" );
    userPassword = MidletMain.getString( MidletMain.accounts, userId, "pass" );
    host = MidletMain.getString( MidletMain.accounts, userId, "host" );
    port = MidletMain.getString( MidletMain.accounts, userId, "port" );
    if ( isStart ) {
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

  public long getStatusId() {
    return statusId;
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

  public abstract int getStatusIndex();

  public abstract void sendTypingStatus( String userId, boolean b );

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

  public abstract byte[] sendMessage( BuddyItem buddyItem, String string, String resource ) throws IOException;

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
    return serviceMessages;
  }

  public abstract String getStatusImages();

  public void offlineAllBuddyes() {
    BuddyGroup tempGroupItem;
    for ( int i = 0; i < buddyItems.size(); i++ ) {
      tempGroupItem = ( ( BuddyGroup ) buddyItems.elementAt( i ) );
      for ( int j = 0; j < ( ( GroupHeader ) tempGroupItem ).getChildsCount(); j++ ) {
        BuddyItem tempIcqItem = ( ( BuddyItem ) tempGroupItem.getChilds().elementAt( j ) );
        tempIcqItem.setStatusIndex( 0, null );
      }
    }
  }

  public abstract void offlineAccount();

  public abstract void setTreeItems( Vector buddyList );

  public abstract void setPrivateItems( Vector privateList );

  public abstract void sortBuddyes();

  public void updateOfflineBuddylist() {
    MidletMain.updateOfflineBuddylist( buddyListFile, buddyItems );
  }

  public void loadOfflineBuddyList() {
    MidletMain.loadOfflineBuddyList( this, buddyListFile, buddyItems );
    isReset = true;
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

  public abstract Cookie addGroup( String groupName, long groupId ) throws IOException;

  public abstract Cookie addBuddy( String buddyId, BuddyGroup buddyGroup, String nickName, int type, long itemId ) throws IOException;

  public abstract Cookie renameBuddy( String itemName, BuddyItem buddyItem, String phones ) throws IOException;

  public abstract Cookie renameGroup( String text, BuddyGroup buddyGroup ) throws IOException;

  public abstract void requestAuth( String text, BuddyItem buddyItem ) throws IOException;

  public abstract void acceptAuthorization( BuddyItem buddyItem ) throws IOException;

  public abstract void requestInfo( String userId, int reqSeqNum ) throws IOException;

  public abstract Cookie removeBuddy( BuddyItem buddyItem ) throws IOException;

  public abstract Cookie removeGroup( BuddyGroup buddyGroup ) throws IOException;

  public abstract BuddyItem getItemInstance();

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

  public abstract DirectConnection getDirectConnectionInstance();

  public abstract long getNextItemId();
}
