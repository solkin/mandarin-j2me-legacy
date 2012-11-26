package com.tomclaw.mandarin.main;

import com.tomclaw.mandarin.dc.DirectConnection;
import java.io.IOException;
import java.util.Vector;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public interface AccountRoot {

  public AccountRoot init( boolean isStart );

  public int getUnrMsgs();

  public void setUnrMsgs( int unrMsgs );

  public long getStatusId();

  public String getUserId();

  public String getUserPassword();

  public void setUserId( String userId );

  public void setUserPassword( String userPassword );

  public String getUserNick();

  public void setUserNick( String userNick );

  public String getAccType();

  public String getHost();

  public String getPort();

  public int getStatusIndex();

  public void sendTypingStatus( String userId, boolean b );

  public Vector getBuddyItems();

  public void setBuddyItems( Vector buddyItems );

  public void setYOffset( int yOffset );

  public void setSelectedIndex( int selectedColumn, int selectedRow );

  public void saveAllSettings();

  public byte[] sendMessage( BuddyItem buddyItem, String string, String resource ) throws IOException;

  public void updateMainFrameBuddyList();

  public ServiceMessages getServiceMessages();

  public String getStatusImages();

  public void offlineAllBuddyes();

  public void offlineAccount();

  public void setTreeItems( Vector buddyList );

  public void setPrivateItems( Vector privateList );

  public void sortBuddyes();

  public void updateOfflineBuddylist();

  public void setShowGroups( boolean isShowGroups );

  public void setShowOffline( boolean isShowOffline );

  public boolean getShowGroups();

  public boolean getShowOffline();

  public Cookie addGroup( String groupName, long groupId ) throws IOException;

  public Cookie addBuddy( String buddyId, BuddyGroup buddyGroup, String nickName, int type, long itemId ) throws IOException;

  public Cookie renameBuddy( String itemName, BuddyItem buddyItem, String phones ) throws IOException;

  public Cookie renameGroup( String text, BuddyGroup buddyGroup ) throws IOException;

  public void requestAuth( String text, BuddyItem buddyItem ) throws IOException;

  public void acceptAuthorization( BuddyItem buddyItem ) throws IOException;

  public void requestInfo( String userId, int reqSeqNum ) throws IOException;

  public Cookie removeBuddy( BuddyItem buddyItem ) throws IOException;

  public Cookie removeGroup( BuddyGroup buddyGroup ) throws IOException;

  public BuddyItem getItemInstance();

  public boolean getUseSsl();

  public void setUseSsl( boolean isUseSsl );

  public void setTransactionManager( TransactionManager transactionManager );

  public TransactionManager getTransactionManager();

  public TransactionsFrame getTransactionsFrame();

  public void setTransactionsFrame( TransactionsFrame transactionsFrame );

  public DirectConnection getDirectConnectionInstance();

  public long getNextItemId();
}
