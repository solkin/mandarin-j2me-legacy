package com.tomclaw.mandarin.mmp;

import com.tomclaw.bingear.GroupNotFoundException;
import com.tomclaw.bingear.IncorrectValueException;
import com.tomclaw.mandarin.dc.DirectConnection;
import com.tomclaw.mandarin.main.*;
import com.tomclaw.mandarin.net.IncorrectAddressException;
import com.tomclaw.mandarin.net.NetConnection;
import com.tomclaw.tcuilite.GroupHeader;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.Base64;
import com.tomclaw.utils.DataUtil;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;
import java.io.IOException;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class MmpAccountRoot extends AccountRoot {

  /**
   * Data
   */
  public String statusText = "Mandarin";
  public String statusDscr = MidletMain.version.concat( " [" ).concat( MidletMain.build.concat( "]" ) );
  /**
   * Runtime
   */
  public Stack queueActionStack;
  public MmpGroup phoneGroup = null;
  /**
   * Objects
   */
  public MmpSession session;

  public MmpAccountRoot( String userId ) {
    this.userId = userId;
    host = "mrim.mail.ru";
    port = "2042";
  }

  public void initSpecialData() {
    /** New session instance **/
    session = new MmpSession( this );
    serviceMessages = new ServiceMessages();
    this.statusId = MmpStatusUtil.getStatus( 0 );
  }

  public void saveAllSettings() {
    try {
      MidletMain.accounts.addItem( userId, "isShowGroups", String.valueOf( isShowGroups ) );
      MidletMain.accounts.addItem( userId, "isShowOffline", String.valueOf( isShowOffline ) );
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

  public void show() {
  }

  public String getAccType() {
    return "mmp";
  }

  public int getStatusIndex() {
    return MmpStatusUtil.getStatusIndex( statusId );
  }

  public void sendTypingStatus( String userId, boolean b ) {
  }

  public void offlineAllBuddyes() {
    GroupHeader tempGroupItem;
    for ( int i = 0; i < buddyItems.size(); i++ ) {
      tempGroupItem = ( ( GroupHeader ) buddyItems.elementAt( i ) );
      for ( int j = 0; j < ( ( GroupHeader ) tempGroupItem ).getChildsCount(); j++ ) {
        MmpItem tempIcqItem = ( ( MmpItem ) tempGroupItem.getChilds().elementAt( j ) );
        tempIcqItem.buddyStatus = 0;
      }
    }
  }

  public byte[] sendMessage( BuddyItem buddyItem, String string, String resource ) throws IOException {
    byte[] cookie = new byte[ 8 ];
    if ( buddyItem.isPhone() ) {
      String userPhone = buddyItem.getUserId();
      if ( !userPhone.startsWith( "+" ) ) {
        userPhone += "+";
      }
      MmpPacketSender.MRIM_CS_SMS_MESSAGE( this, userPhone, string );
    } else {
      LogUtil.outMessage( ">>> contactItem.getUserId()=" + buddyItem.getUserId() );
      cookie = MmpPacketSender.MRIM_CS_MESSAGE( this, buddyItem.getUserId(), string, 0x00000028, " " );
    }
    return cookie;
  }

  public MmpItem setBuddyStatus( String buddyId, long buddyStatus ) {
    GroupHeader groupItem;
    for ( int c = 0; c < buddyItems.size(); c++ ) {
      groupItem = ( GroupHeader ) buddyItems.elementAt( c );
      for ( int i = 0; i < groupItem.getChildsCount(); i++ ) {
        if ( ( ( MmpItem ) groupItem.getChilds().elementAt( i ) ).userId.equals( buddyId ) ) {
          ( ( MmpItem ) groupItem.getChilds().elementAt( i ) ).buddyStatus = buddyStatus;
          ( ( MmpItem ) groupItem.getChilds().elementAt( i ) ).updateUiData();
          updateMainFrameBuddyList();
          return ( ( MmpItem ) groupItem.getChilds().elementAt( i ) );
        }
      }
    }
    return null;
  }

  public long getBuddyStatus( String buddyId ) {
    GroupHeader groupItem;
    for ( int c = 0; c < buddyItems.size(); c++ ) {
      groupItem = ( GroupHeader ) buddyItems.elementAt( c );
      for ( int i = 0; i < groupItem.getChildsCount(); i++ ) {
        if ( ( ( MmpItem ) groupItem.getChilds().elementAt( i ) ).userId.equals( buddyId ) ) {
          return ( ( MmpItem ) groupItem.getChilds().elementAt( i ) ).buddyStatus;
        }
      }
    }
    return 0;
  }

  public void updateMainFrameBuddyList() {
    for ( int c = 0; c < buddyItems.size(); c++ ) {
      if ( buddyItems.elementAt( c ) instanceof MmpGroup ) {
        ( ( MmpGroup ) buddyItems.elementAt( c ) ).updateUiData();
      }
      for ( int i = 0; i < ( ( GroupHeader ) buddyItems.elementAt( c ) ).getChildsCount(); i++ ) {
        ( ( MmpItem ) ( ( GroupHeader ) buddyItems.elementAt( c ) ).getChilds().elementAt( i ) ).updateUiData();
      }
    }
    if ( MidletMain.mainFrame.getActiveAccountRoot().equals( this ) ) {
      MidletMain.mainFrame.buddyList.items = this.buddyItems;
      MidletMain.screen.repaint();
    }
  }

  public void connectAction( long statusId ) {
    do {
      if ( MidletMain.httpHiddenPing > 0 ) {
        try {
          NetConnection.httpPing( "http://www.mail.ru" );
        } catch ( IOException ex ) {
          LogUtil.outMessage( "HTTP hidden connection failed" );
        }
      }
      try {
        if ( session.login_stage( host + ":" + port, userId, userPassword, statusId, statusText, statusDscr ) ) {
          this.statusId = statusId;
          LogUtil.outMessage( "Updating status in AccountStatus" );
          MidletMain.mainFrame.updateAccountsStatus();
          ActionExec.setConnectionStage( this, 10 );
        }
        return;
      } catch ( IOException ex ) {
        //! currentAction = com.tomclaw.tcui.localization.Localization.getMessage("ERR_IO_EXCEPTION");
        LogUtil.outMessage( "IO Exception" );
        ActionExec.showNotify( Localization.getMessage( "ERROR" ), Localization.getMessage( "IO_EXCEPTION" ) );
      } catch ( IncorrectAddressException ex ) {
        //! currentAction = com.tomclaw.tcui.localization.Localization.getMessage("ERR_INCORRECT_ADDR");
        LogUtil.outMessage( "Incorrect address" );
        ActionExec.showNotify( Localization.getMessage( "ERROR" ), Localization.getMessage( "INCORRECT_ADDRESS" ) );
      } catch ( Throwable ex ) {
        //! currentAction = com.tomclaw.tcui.localization.Localization.getMessage("ERR_THROWABLE");
        LogUtil.outMessage( "Throwable" );
        ActionExec.showNotify( Localization.getMessage( "ERROR" ), Localization.getMessage( "THROWABLE" ) );
        // ex.printStackTrace();
      }
      try {
        Thread.sleep( MidletMain.reconnectTime );
      } catch ( InterruptedException ex ) {
        // ex.printStackTrace();
      }
    } while ( MidletMain.autoReconnect );
  }

  public void setTreeItems( Vector buddyList ) {
    this.buddyItems = buddyList;
  }

  public void sortBuddyes() {
  }

  public String getStatusImages() {
    return "/res/groups/img_mmpstatus.png";
  }

  public void offlineAccount() {
    this.statusId = MmpStatusUtil.getStatus( 0 );
  }

  public void setPrivateItems( Vector privateList ) {
  }

  public BuddyItem getItemInstance() {
    return new MmpItem();
  }

  public Cookie addGroup( String groupName, long groupId ) throws IOException {
    Cookie cookie = MmpPacketSender.MRIM_CS_ADD_CONTACT( this, groupId, 0x00000000, new byte[ 0 ], StringUtil.string1251ToByteArray( groupName ), new byte[ 0 ] );
    return cookie;
  }

  public Cookie addBuddy( String buddyId, final BuddyGroup buddyGroup, String nickName, int type, long itemId ) throws IOException {
    final boolean isTelephone = ( type == 0x02 );
    Cookie cookie;
    if ( isTelephone ) {
      cookie = MmpPacketSender.MRIM_CS_ADD_CONTACT( this, 0x00100000, 0x67000000, "phone".getBytes(), StringUtil.string1251ToByteArray( nickName ), buddyId.getBytes() );
    } else {
      cookie = MmpPacketSender.MRIM_CS_ADD_CONTACT( this, 0x00000000, ( ( MmpGroup ) buddyGroup ).getId()/*& 0x0000ffff*/, buddyId.getBytes(), StringUtil.string1251ToByteArray( nickName ), new byte[ 0 ] );
    }
    return cookie;
  }

  public Cookie renameBuddy( final String itemName, final BuddyItem buddyItem, String phones ) throws IOException {
    String buddyId;
    if ( buddyItem.isPhone() ) {
      buddyId = "phone";
    } else {
      buddyId = ( ( MmpItem ) buddyItem ).userId;
    }
    return MmpPacketSender.MRIM_CS_MODIFY_CONTACT( this, ( ( MmpItem ) buddyItem ).contactId, ( ( MmpItem ) buddyItem ).flags/* >> 24*/, ( ( MmpItem ) buddyItem ).groupId, buddyId.getBytes(), StringUtil.string1251ToByteArray( itemName ), phones == null ? "" : phones );
  }

  public Cookie renameGroup( final String itemName, final BuddyGroup buddyGroup ) throws IOException {
    return MmpPacketSender.MRIM_CS_MODIFY_CONTACT( this, ( ( MmpGroup ) buddyGroup ).contactId, ( ( MmpGroup ) buddyGroup ).flags, 0, StringUtil.string1251ToByteArray( ( ( MmpGroup ) buddyGroup ).userId ), StringUtil.string1251ToByteArray( itemName ), "" );
  }

  public void requestAuth( String requestText, BuddyItem buddyItem ) throws IOException {
    byte[] data = StringUtil.string1251ToByteArray( requestText );
    String reasonText = "AgAAAAAAAAAGAAAA".concat( Base64.encode( data, 0, data.length ) );
    LogUtil.outMessage( reasonText );
    MmpPacketSender.MRIM_CS_MESSAGE( this, buddyItem.getUserId(), reasonText, DataUtil.reverseLong( PacketType.MESSAGE_FLAG_AUTHORIZE ), "" );
  }

  public void acceptAuthorization( BuddyItem buddyItem ) throws IOException {
    MmpPacketSender.MRIM_CS_AUTHORIZE( this, buddyItem.getUserId() );
  }

  public void requestInfo( String userId, int reqSeqNum ) throws IOException {
    MmpPacketSender.MRIM_CS_WP_REQUEST( this, userId );
  }

  public Cookie removeBuddy( final BuddyItem buddyItem ) throws IOException {
    String buddyId;
    String phones = buddyItem.isPhone() ? buddyItem.getUserPhone() : "";
    if ( buddyItem.isPhone() ) {
      buddyId = "phone";
    } else {
      buddyId = ( ( MmpItem ) buddyItem ).userId;
    }
    return MmpPacketSender.MRIM_CS_MODIFY_CONTACT( this, ( ( MmpItem ) buddyItem ).contactId, ( ( MmpItem ) buddyItem ).flags | PacketType.CONTACT_FLAG_REMOVED, ( ( MmpItem ) buddyItem ).groupId, buddyId.getBytes(), StringUtil.string1251ToByteArray( ( ( MmpItem ) buddyItem ).userNick ), phones == null ? "" : phones );
  }

  public Cookie removeGroup( final BuddyGroup groupHeader ) throws IOException {
    return MmpPacketSender.MRIM_CS_MODIFY_CONTACT( this, ( ( MmpGroup ) groupHeader ).contactId, ( ( MmpGroup ) groupHeader ).flags | PacketType.CONTACT_FLAG_REMOVED/*0x03020001*/, 0, StringUtil.string1251ToByteArray( ( ( MmpGroup ) groupHeader ).userId ), StringUtil.string1251ToByteArray( ( ( MmpGroup ) groupHeader ).userId ), "" );
  }

  public DirectConnection getDirectConnectionInstance() {
    return null;
  }

  public long getNextItemId() {
    return 0x00000002 | ( ( new Random( System.currentTimeMillis() ).nextLong() ) & 0xffff0000 );
  }
}
