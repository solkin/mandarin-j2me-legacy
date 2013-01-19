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
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013 
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class IcqAccountRoot extends AccountRoot {

  /** Data **/
  public Vector t_OnlineItems = new Vector();
  public int xStatusId = -1;
  public int pStatusId = 1;
  public int privateBuddyId = -1;
  public String statusText = "Mandarin ".concat( MidletMain.version ).concat( " [" ).concat( MidletMain.build.concat( "]" ) );
  public String xTitle = "";
  public String xText = "";
  public boolean isPStatusReadable = true;
  public boolean isXStatusReadable = true;
  /** Threads and states **/
  public IcqSession session;

  public IcqAccountRoot( String userId ) {
    super( userId );
  }

  /**
   * Constructing account root
   */
  public void construct() {
    host = "login.icq.com";
    port = "5190";
  }

  public void initSpecialData() {
    /** New session instance **/
    session = new IcqSession( this );
    /** Loading XStatus, PStatus **/
    xStatusId = MidletMain.getInteger( MidletMain.accounts, userId, "xStatusId" );
    pStatusId = MidletMain.getInteger( MidletMain.accounts, userId, "pStatusId" );
    privateBuddyId = MidletMain.getInteger( MidletMain.accounts, userId, "privateBuddyId" );
    /** Status **/
    String statusData = MidletMain.getString( MidletMain.statuses, "PStatus", String.valueOf( statusIndex ) );
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

  public void saveSpecialSettings() throws Throwable {
    MidletMain.accounts.addItem( userId, "xStatusId", String.valueOf( xStatusId ) );
    MidletMain.accounts.addItem( userId, "pStatusId", String.valueOf( pStatusId ) );
    MidletMain.accounts.addItem( userId, "privateBuddyId", String.valueOf( privateBuddyId ) );
  }

  public void show() {
  }

  public String getAccType() {
    return "icq";
  }

  public void sendTypingStatus( String userId, boolean b ) {
    try {
      IcqPacketSender.sendTypingStatus( session, userId, b );
    } catch ( IOException ex ) {
      LogUtil.outMessage( "Couldn't send typing notify" );
    }
  }

  public void connectAction( final int statusIndex ) {
    if ( isConnecting || this.statusIndex != 0 ) {
      return;
    }
    new Thread() {
      public void run() {
        isConnecting = true;
        try {
          do {
            if ( MidletMain.httpHiddenPing > 0 ) {
              try {
                NetConnection.httpPing( "http://www.icq.com" );
              } catch ( IOException ex ) {
                LogUtil.outMessage( "HTTP hidden connection failed" );
              }
            }
            NetConnection netConnection = new NetConnection();
            String errorCause;
            boolean isFail = false;
            try {
              ActionExec.setConnectionStage( IcqAccountRoot.this, 0 );
              netConnection.connectAddress( host + ":" + port );
              ActionExec.setConnectionStage( IcqAccountRoot.this, 1 );
              session.setNetConnection( netConnection );
              session.isRequestSsi = false;
              session.login( IcqStatusUtil.getStatus( statusIndex ) );
              IcqAccountRoot.this.statusIndex = statusIndex;
              MidletMain.mainFrame.updateAccountsStatus();
              isConnecting = false;
              return;
            } catch ( LoginFailed ex ) {
              LogUtil.outMessage( "Failed" );
              errorCause = Localization.getMessage( "FAILED" );
              isFail = true;
            } catch ( ProtocolSupportBecameOld ex ) {
              LogUtil.outMessage( "Protocol support became old" );
              errorCause = Localization.getMessage( "PROTOCOL_SUPPORT_BECAME_OLD" );
              isFail = true;
            } catch ( InterruptedException ex ) {
              LogUtil.outMessage( "Failed: " + ex.getMessage() );
              errorCause = Localization.getMessage( "INTERRUPTED" );
            } catch ( IOException ex ) {
              LogUtil.outMessage( "IO Exception" );
              errorCause = Localization.getMessage( "IO_EXCEPTION" );
            } catch ( IncorrectAddressException ex ) {
              LogUtil.outMessage( "Incorrect address" );
              errorCause = Localization.getMessage( "INCORRECT_ADDRESS" );
              isFail = true;
            } catch ( Throwable ex ) {
              LogUtil.outMessage( "Throwable" );
              errorCause = Localization.getMessage( "INCORRECT_ADDRESS" );
              isFail = true;
            }
            /** Checking for error **/
            if ( errorCause != null ) {
              ActionExec.showError( errorCause );
              /** Failing on crytical errors **/
              if ( isFail ) {
                isConnecting = false;
                return;
              }
            }
            Thread.sleep( MidletMain.reconnectTime );
          } while ( MidletMain.autoReconnect );
        } catch ( InterruptedException ex ) {
        }
        isConnecting = false;
      }
    }.start();
  }

  public void setTreeItems( Vector buddyList ) {
    LogUtil.outMessage( "setTreeItems for " + buddyList.size() );
    if ( isReset ) {
      LogUtil.outMessage( "reset node" );
      t_OnlineItems.removeAllElements();
      /** Save online buddyes **/
      IcqItem tempIcqItem;
      for ( int c = 0; c < buddyItems.size(); c++ ) {
        for ( int j = 0; j < ( ( GroupHeader ) buddyItems.elementAt( c ) ).getChildsCount(); j++ ) {
          tempIcqItem = ( ( IcqItem ) ( ( GroupHeader ) buddyItems.elementAt( c ) ).getChilds().elementAt( j ) );
          if ( tempIcqItem.getStatusIndex() != 0 ) {
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
            tempIcqItem.setStatusIndex( ( ( IcqItem ) t_OnlineItems.elementAt( q ) ).getStatusIndex(), null );
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

  public IcqItem setBuddyStatus( String buddyId, int buddyStatus,
          Capability[] caps, ClientInfo clientInfo ) {
    IcqGroup groupItem;
    IcqItem icqItem;
    for ( int c = 0; c < buddyItems.size(); c++ ) {
      groupItem = ( IcqGroup ) buddyItems.elementAt( c );
      for ( int i = 0; i < groupItem.getChildsCount(); i++ ) {
        icqItem = ( IcqItem ) groupItem.getChilds().elementAt( i );
        if ( icqItem.userId.equals( buddyId ) ) {
          icqItem.setStatusIndex( IcqStatusUtil.getStatusIndex( buddyStatus ),
                  null );
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
      return IcqStatusUtil.getStatus( icqItem.getStatusIndex() );
    }
    return -1;
  }

  public IcqItem getBuddy( String buddyId ) {
    IcqGroup groupItem;
    for ( int c = 0; c < buddyItems.size(); c++ ) {
      groupItem = ( IcqGroup ) buddyItems.elementAt( c );
      for ( int i = 0; i < groupItem.getChildsCount(); i++ ) {
        if ( ( ( IcqItem ) groupItem.getChilds().elementAt( i ) ).userId
                .equals( buddyId ) ) {
          return ( ( IcqItem ) groupItem.getChilds().elementAt( i ) );
        }
      }
    }
    return null;
  }

  public void updateMainFrameBuddyList() {
    for ( int c = 0; c < buddyItems.size(); c++ ) {
      for ( int i = 0; i < ( ( GroupHeader ) buddyItems.elementAt( c ) )
              .getChildsCount(); i++ ) {
        ( ( BuddyItem ) ( ( GroupHeader ) buddyItems.elementAt( c ) )
                .getChilds().elementAt( i ) ).updateUiData();
      }
    }
    updateMainFrameUI();
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

  public byte[] sendMessage( BuddyItem buddyItem, String string,
          String resource ) throws IOException {
    return IcqPacketSender.sendMessage( session, buddyItem.getUserId(),
            string );
  }

  public String getStatusImages() {
    return "/res/groups/img_icqstatus.png";
  }

  public BuddyGroup getGroupInstance() {
    return new IcqGroup();
  }

  public BuddyItem getBuddyInstance() {
    return new IcqItem();
  }

  public Cookie addGroup( BuddyGroup buddyGroup ) throws IOException {
    IcqGroup icqGroup = ( IcqGroup ) buddyGroup;
    icqGroup.groupId = getNextGroupId();
    return IcqPacketSender.addBuddy( session, StringUtil.stringToByteArray(
            icqGroup.getUserId(), true ), icqGroup.groupId, 0x00, 0x0001, false, null );
  }

  public Cookie addBuddy( BuddyItem buddyItem, BuddyGroup buddyGroup )
          throws IOException {
    ( ( IcqItem ) buddyItem ).buddyId = getNextBuddyId();
    ( ( IcqItem ) buddyItem ).groupId = ( ( IcqGroup ) buddyGroup ).groupId;
    ( ( IcqItem ) buddyItem ).isAvaitingAuth = true;
    return IcqPacketSender.addBuddy( session, buddyItem.getUserId().getBytes(),
            ( ( IcqGroup ) buddyGroup ).groupId,
            ( ( IcqItem ) buddyItem ).buddyId, 0x00, true,
            StringUtil.stringToByteArray( buddyItem.getUserNick(), true ) );
  }

  public Cookie renameBuddy( String itemName, BuddyItem buddyItem,
          String phones ) throws IOException {
    return IcqPacketSender.updateBuddy( session,
            buddyItem.getUserId().getBytes(),
            ( ( IcqItem ) buddyItem ).groupId,
            ( ( IcqItem ) buddyItem ).buddyId, 0x0000,
            ( ( IcqItem ) buddyItem ).isAvaitingAuth,
            StringUtil.stringToByteArray( itemName, true ) );
  }

  public Cookie renameGroup( String itemName, BuddyGroup buddyGroup )
          throws IOException {
    return IcqPacketSender.updateBuddy( session,
            StringUtil.stringToByteArray( itemName, true ),
            ( ( IcqGroup ) buddyGroup ).groupId, 0x0000, 0x0001, false, null );
  }

  public void requestAuth( String requestText, BuddyItem buddyItem )
          throws IOException {
    IcqPacketSender.authRequest( session, buddyItem.getUserId(),
            StringUtil.stringToByteArray( requestText, true ) );
  }

  public void acceptAuthorization( BuddyItem buddyItem ) throws IOException {
    IcqPacketSender.authReply( session, buddyItem.getUserId(), true,
            StringUtil.stringToByteArray(
            Localization.getMessage( "DEFAULT_ACCEPT" ), true ) );
  }

  public void requestInfo( String userId, int reqSeqNum ) throws IOException {
    IcqPacketSender.shortInfoRequest( session, this.userId, userId, reqSeqNum );
  }

  public Cookie removeBuddy( BuddyItem buddyItem ) throws IOException {
    return IcqPacketSender.removeBuddy( session, buddyItem.getUserId(),
            ( ( IcqItem ) buddyItem ).groupId,
            ( ( IcqItem ) buddyItem ).buddyId,
            ( ( IcqItem ) buddyItem ).buddyType );
  }

  public Cookie removeGroup( BuddyGroup buddyGroup ) throws IOException {
    return IcqPacketSender.removeBuddy( session, buddyGroup.getUserId(),
            ( ( IcqGroup ) buddyGroup ).groupId,
            ( ( IcqGroup ) buddyGroup ).buddyId,
            ( ( IcqGroup ) buddyGroup ).buddyType );
  }

  public DirectConnection getDirectConnectionInstance() {
    return new IcqDirectConnection( this );
  }

  public int getNextBuddyId() {
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

  public int getNextGroupId() {
    return getNextBuddyId();
  }
}
