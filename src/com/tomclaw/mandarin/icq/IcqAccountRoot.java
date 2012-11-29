package com.tomclaw.mandarin.icq;

import com.tomclaw.mandarin.dc.DirectConnection;
import com.tomclaw.mandarin.main.*;
import com.tomclaw.mandarin.net.IncorrectAddressException;
import com.tomclaw.mandarin.net.NetConnection;
import com.tomclaw.tcuilite.GroupHeader;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012 
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class IcqAccountRoot extends AccountRoot {

  /**
   * Static settings
   */
  /**
   * Data
   */
  public Vector t_OnlineItems = new Vector();
  public int xStatusId = -1;
  public int pStatusId = 1;
  public int privateBuddyId = -1;
  public String statusText = "Mandarin ".concat( MidletMain.version ).concat( " [" ).concat( MidletMain.build.concat( "]" ) );
  public String xTitle = "";
  public String xText = "";
  public boolean isPStatusReadable = true;
  public boolean isXStatusReadable = true;
  private String buddyListFile = null;
  /**
   * Threads and states
   */
  public IcqSession session;

  public IcqAccountRoot(String userId) {
    super(userId);
  }
  
  /**
   * Storers
   */
  public void construct() {
    host = "login.icq.com";
    port = "5190";
  }

  public void initSpecialData() {
    /** New session instance **/
    session = new IcqSession( this );
    transactionManager = new TransactionManager();
    serviceMessages = new ServiceMessages();
    /** Loading XStatus, PStatus **/
    xStatusId = MidletMain.getInteger( MidletMain.accounts, userId, "xStatusId" );
    pStatusId = MidletMain.getInteger( MidletMain.accounts, userId, "pStatusId" );
    privateBuddyId = MidletMain.getInteger( MidletMain.accounts, userId, "privateBuddyId" );
    /** Status **/
    String statusData = MidletMain.getString( MidletMain.statuses, "PStatus", String.valueOf( statusId ) );
    statusText = statusData.substring( 0, ( statusData.indexOf( "&rdb" ) == -1 ) ? statusData.length() : statusData.indexOf( "&rdb" ) );
    isPStatusReadable = ( statusData.indexOf( "&rdb" ) == -1 )
            ? false : statusData.substring( statusData.indexOf( "&rdb" ) + 4 ).equals( "true" );
    String extStatusText = MidletMain.getString( MidletMain.statuses, "XStatus", String.valueOf( xStatusId ) );
    if ( extStatusText == null || extStatusText.length() == 0 ) {
      extStatusText = "&dsc";
    }
    xTitle = extStatusText.substring( 0, extStatusText.indexOf( "&dsc" ) );
    xText = extStatusText.substring( extStatusText.indexOf( "&dsc" ) + 4,
            ( extStatusText.indexOf( "&rdb" ) == -1 ) ? extStatusText.length() : extStatusText.indexOf( "&rdb" ) );
    isXStatusReadable = ( extStatusText.indexOf( "&rdb" ) == -1 )
            ? false : extStatusText.substring( extStatusText.indexOf( "&rdb" ) + 4 ).equals( "true" );
  }

  public void saveAllSettings() {
    try {
      MidletMain.accounts.addItem( userId, "xStatusId", String.valueOf( xStatusId ) );
      MidletMain.accounts.addItem( userId, "pStatusId", String.valueOf( pStatusId ) );
      MidletMain.accounts.addItem( userId, "privateBuddyId", String.valueOf( privateBuddyId ) );
      MidletMain.accounts.addItem( userId, "isShowGroups", String.valueOf( isShowGroups ) );
      MidletMain.accounts.addItem( userId, "isShowOffline", String.valueOf( isShowOffline ) );
      LogUtil.outMessage( "isShowGroups = " + String.valueOf( isShowGroups ) );
      LogUtil.outMessage( "isShowOffline = " + String.valueOf( isShowOffline ) );
    } catch ( Throwable ex1 ) {
      LogUtil.outMessage( ex1 );
    }
    MidletMain.saveRmsData( true, false, false );
    LogUtil.outMessage( "RMS accounts saving complete" );
  }

  public void show() {
  }

  public String getAccType() {
    return "icq";
  }

  public int getStatusIndex() {
    return IcqStatusUtil.getStatusIndex( (int)statusId );
  }

  public void sendTypingStatus( String userId, boolean b ) {
    try {
      IcqPacketSender.sendTypingStatus( session, userId, b );
    } catch ( IOException ex ) {
      LogUtil.outMessage( "Couldn't send typing notify" );
    }
  }

  public void connectAction( final int statusId ) {
    new Thread() {
      public void run() {
        do {
          if ( MidletMain.httpHiddenPing > 0 ) {
            try {
              NetConnection.httpPing( "http://www.icq.com" );
            } catch ( IOException ex ) {
              LogUtil.outMessage( "HTTP hidden connection failed" );
            }
          }
          NetConnection netConnection = new NetConnection();
          try {
            ActionExec.setConnectionStage( IcqAccountRoot.this, 0 );
            netConnection.connectAddress( host + ":" + port );
            ActionExec.setConnectionStage( IcqAccountRoot.this, 1 );
            session.setNetConnection( netConnection );
            session.isRequestSsi = false;
            session.login( statusId );
            IcqAccountRoot.this.statusId = statusId;
            MidletMain.mainFrame.updateAccountsStatus();
            return;
          } catch ( LoginFailed ex ) {
            LogUtil.outMessage( "Failed" );
            ActionExec.showNotify( Localization.getMessage( "ERROR" ), Localization.getMessage( "FAILED" ) );
            return;
          } catch ( ProtocolSupportBecameOld ex ) {
            LogUtil.outMessage( "Protocol support became old" );
            ActionExec.showNotify( Localization.getMessage( "ERROR" ), Localization.getMessage( "PROTOCOL_SUPPORT_BECAME_OLD" ) );
            return;
          } catch ( InterruptedException ex ) {
            LogUtil.outMessage( "Failed: " + ex.getMessage() );
            ActionExec.showNotify( Localization.getMessage( "ERROR" ), Localization.getMessage( "INTERRUPTED" ) );
          } catch ( IOException ex ) {
            LogUtil.outMessage( "IO Exception" );
            ActionExec.showNotify( Localization.getMessage( "ERROR" ), Localization.getMessage( "IO_EXCEPTION" ) );
          } catch ( IncorrectAddressException ex ) {
            LogUtil.outMessage( "Incorrect address" );
            ActionExec.showNotify( Localization.getMessage( "ERROR" ), Localization.getMessage( "INCORRECT_ADDRESS" ) );
            return;
          } catch ( Throwable ex ) {
            LogUtil.outMessage( "Throwable" );
            ActionExec.showNotify( Localization.getMessage( "ERROR" ), Localization.getMessage( "INCORRECT_ADDRESS" ) );
            return;
          }
          try {
            Thread.sleep( MidletMain.reconnectTime );
          } catch ( InterruptedException ex ) {
          }
        } while ( MidletMain.autoReconnect );
      }
    }.start();
  }

  public void setTreeItems( Vector buddyList ) {
    LogUtil.outMessage( "setTreeItems for " + buddyList.size() );
    if ( isReset ) {
      LogUtil.outMessage( "reset node" );
      t_OnlineItems.removeAllElements();
      /**
       * Save online buddyes
       */
      IcqItem tempIcqItem;
      for ( int c = 0; c < buddyItems.size(); c++ ) {
        for ( int j = 0; j < ( ( GroupHeader ) buddyItems.elementAt( c ) ).getChildsCount(); j++ ) {
          tempIcqItem = ( ( IcqItem ) ( ( GroupHeader ) buddyItems.elementAt( c ) ).getChilds().elementAt( j ) );
          if ( tempIcqItem.buddyStatus != -1 ) {
            t_OnlineItems.addElement( tempIcqItem );
          }
        }
      }
      this.buddyItems = buddyList;
    } else {
      LogUtil.outMessage( "append node" );
      for ( int c = 0; c < buddyList.size(); c++ ) {
        IcqGroup icqGroup = ( ( IcqGroup ) buddyList.elementAt( c ) );
        if ( c == 0 && icqGroup.buddyType == -1 ) {
          for ( int i = 0; i < icqGroup.getChildsCount(); i++ ) {
            ( ( GroupHeader ) buddyItems.lastElement() ).getChilds().addElement( icqGroup.getChilds().elementAt( i ) );
          }
          continue;
        }
        buddyItems.addElement( buddyList.elementAt( c ) );
      }
    }
  }

  public void setPrivateItems( Vector privateList ) {
    LogUtil.outMessage( "Private list size: " + privateList.size() );
    IcqGroup tempGroupItem;
    for ( int i = 0; i < buddyItems.size(); i++ ) {
      tempGroupItem = ( ( IcqGroup ) buddyItems.elementAt( i ) );
      for ( int j = 0; j < ( ( GroupHeader ) tempGroupItem ).getChildsCount(); j++ ) {
        IcqItem tempIcqItem = ( ( IcqItem ) tempGroupItem.getChilds().elementAt( j ) );
        for ( int q = 0; q < t_OnlineItems.size(); q++ ) {
          if ( tempIcqItem.userId.equals( ( ( IcqItem ) t_OnlineItems.elementAt( q ) ).userId ) ) {
            tempIcqItem.buddyStatus = ( ( IcqItem ) t_OnlineItems.elementAt( q ) ).buddyStatus;
            tempIcqItem.capabilities = ( ( IcqItem ) t_OnlineItems.elementAt( q ) ).capabilities;
            tempIcqItem.clientInfo = ( ( IcqItem ) t_OnlineItems.elementAt( q ) ).clientInfo;
          }
        }
        for ( int c = 0; c < privateList.size(); c++ ) {
          IcqItem privateItem = ( ( IcqItem ) privateList.elementAt( c ) );
          if ( tempIcqItem.userId.equals( privateItem.userId ) ) {
            /**
             * Private item exist in buddy list
             */
            if ( privateItem.buddyType == IcqItem.PERMIT_LIST_BUDDY ) {
              tempIcqItem.isInPermitList = true;
              tempIcqItem.permitBuddyId = privateItem.buddyId;
            } else {
              if ( privateItem.buddyType == IcqItem.DENY_LIST_BUDDY ) {
                tempIcqItem.isInDenyList = true;
                tempIcqItem.denyBuddyId = privateItem.buddyId;
              } else {
                if ( privateItem.buddyType == IcqItem.IGNORE_LIST_BUDDY ) {
                  tempIcqItem.isInIgnoreList = true;
                  tempIcqItem.ignoreBuddyId = privateItem.buddyId;
                }
              }
            }
            tempIcqItem.updateUiData();
            LogUtil.outMessage( "Private data set" );
          }
        }
      }
    }
  }

  public void sortBuddyes() {
  }

  public void offlineAllBuddyes() {
    IcqGroup tempGroupItem;
    for ( int i = 0; i < buddyItems.size(); i++ ) {
      tempGroupItem = ( ( IcqGroup ) buddyItems.elementAt( i ) );
      for ( int j = 0; j < ( ( GroupHeader ) tempGroupItem ).getChildsCount(); j++ ) {
        IcqItem tempIcqItem = ( ( IcqItem ) tempGroupItem.getChilds().elementAt( j ) );
        tempIcqItem.buddyStatus = -1;
      }
    }
  }

  public void loadOfflineBuddyList() {
    MidletMain.loadOfflineBuddyList( this, buddyListFile, buddyItems );
    isReset = true;
  }

  public IcqItem setBuddyStatus( String buddyId, int buddyStatus, Capability[] caps, ClientInfo clientInfo ) {
    IcqGroup groupItem;
    IcqItem icqItem;
    for ( int c = 0; c < buddyItems.size(); c++ ) {
      groupItem = ( IcqGroup ) buddyItems.elementAt( c );
      for ( int i = 0; i < groupItem.getChildsCount(); i++ ) {
        icqItem = ( IcqItem ) groupItem.getChilds().elementAt( i );
        if ( icqItem.userId.equals( buddyId ) ) {
          icqItem.buddyStatus = buddyStatus;
          icqItem.capabilities = caps;
          icqItem.clientInfo = clientInfo;
          icqItem.updateUiData();
          updateMainFrameBuddyList();
          return ( ( IcqItem ) groupItem.getChilds().elementAt( i ) );
        }
      }
    }
    return null;
  }

  public int getBuddyStatus( String buddyId ) {
    IcqItem icqItem = getBuddy( buddyId );
    if ( icqItem != null ) {
      return icqItem.buddyStatus;
    }
    return -1;
  }

  public IcqItem getBuddy( String buddyId ) {
    IcqGroup groupItem;
    for ( int c = 0; c < buddyItems.size(); c++ ) {
      groupItem = ( IcqGroup ) buddyItems.elementAt( c );
      for ( int i = 0; i < groupItem.getChildsCount(); i++ ) {
        if ( ( ( IcqItem ) groupItem.getChilds().elementAt( i ) ).userId.equals( buddyId ) ) {
          return ( ( IcqItem ) groupItem.getChilds().elementAt( i ) );
        }
      }
    }
    return null;
  }

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

  public void setUpdatePrivacy( int pStatus ) {
    try {
      /** Client update private status **/
      IcqPacketSender.setUpdatePrivacy( session, privateBuddyId, pStatus );
      this.pStatusId = pStatus;
      saveAllSettings();
    } catch ( IOException ex ) {
    }
  }

  public byte[] sendMessage( BuddyItem buddyItem, String string, String resource ) throws IOException {
    return IcqPacketSender.sendMessage( session, buddyItem.getUserId(), string );
  }

  public String getStatusImages() {
    return "/res/groups/img_icqstatus.png";
  }

  public void offlineAccount() {
    this.statusId = IcqStatusUtil.getStatus( 0 );
  }

  public BuddyItem getItemInstance() {
    return new IcqItem();
  }

  public Cookie addGroup( String groupName, long itemId ) throws IOException {
    return IcqPacketSender.addBuddy( session, StringUtil.stringToByteArray( groupName, true ), ( int ) itemId, 0x00, 0x0001, false, null );
  }

  public Cookie addBuddy( String buddyId, BuddyGroup buddyGroup, String nickName, int type, long itemId ) throws IOException {
    return IcqPacketSender.addBuddy( session, buddyId.getBytes(), ( ( IcqGroup ) buddyGroup ).groupId, ( int ) itemId, 0x00, true, StringUtil.stringToByteArray( nickName, true ) );
  }

  public Cookie renameBuddy( String itemName, BuddyItem buddyItem, String phones ) throws IOException {
    return IcqPacketSender.updateBuddy( session, buddyItem.getUserId().getBytes(), ( ( IcqItem ) buddyItem ).groupId, ( ( IcqItem ) buddyItem ).buddyId, 0x0000, ( ( IcqItem ) buddyItem ).isAvaitingAuth, StringUtil.stringToByteArray( itemName, true ) );
  }

  public Cookie renameGroup( String itemName, BuddyGroup buddyGroup ) throws IOException {
    return IcqPacketSender.updateBuddy( session, StringUtil.stringToByteArray( itemName, true ), ( ( IcqGroup ) buddyGroup ).groupId, 0x0000, 0x0001, false, null );
  }

  public void requestAuth( String requestText, BuddyItem buddyItem ) throws IOException {
    IcqPacketSender.authRequest( session, buddyItem.getUserId(), StringUtil.stringToByteArray( requestText, true ) );
  }

  public void acceptAuthorization( BuddyItem buddyItem ) throws IOException {
    IcqPacketSender.authReply( session, buddyItem.getUserId(), true, StringUtil.stringToByteArray( Localization.getMessage( "DEFAULT_ACCEPT" ), true ) );
  }

  public void requestInfo( String userId, int reqSeqNum ) throws IOException {
    IcqPacketSender.shortInfoRequest( session, this.userId, userId, reqSeqNum );
  }

  public Cookie removeBuddy( BuddyItem buddyItem ) throws IOException {
    return IcqPacketSender.removeBuddy( session, buddyItem.getUserId(), ( ( IcqItem ) buddyItem ).groupId, ( ( IcqItem ) buddyItem ).buddyId, ( ( IcqItem ) buddyItem ).buddyType );
  }

  public Cookie removeGroup( BuddyGroup buddyGroup ) throws IOException {
    return IcqPacketSender.removeBuddy( session, buddyGroup.getUserId(), ( ( IcqGroup ) buddyGroup ).groupId, ( ( IcqGroup ) buddyGroup ).buddyId, ( ( IcqGroup ) buddyGroup ).buddyType );
  }

  public DirectConnection getDirectConnectionInstance() {
    return new IcqDirectConnection( this );
  }

  public long getNextItemId() {
    int randInt = 0;
    Random rand;
    boolean isComprare = false;
    while ( isComprare == false ) {
      rand = new Random( System.currentTimeMillis() );
      randInt = rand.nextInt();
      if ( randInt < 0 ) {
        randInt = randInt * ( -1 );
      }
      randInt = randInt % 0x6FFF + 0x1000;
      isComprare = true;
      IcqGroup groupItem;
      for ( int c = 0; c < buddyItems.size(); c++ ) {
        groupItem = ( IcqGroup ) buddyItems.elementAt( c );
        for ( int i = 0; i < groupItem.getChildsCount(); i++ ) {
          if ( ( ( IcqItem ) groupItem.getChilds().elementAt( i ) ).buddyId == randInt
                  || ( ( IcqItem ) groupItem.getChilds().elementAt( i ) ).groupId == randInt ) {
            isComprare = false;
            break;
          }
        }
      }
    }
    return randInt;
  }
}
