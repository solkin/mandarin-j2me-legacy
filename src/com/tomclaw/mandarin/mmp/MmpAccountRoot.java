package com.tomclaw.mandarin.mmp;

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
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class MmpAccountRoot extends AccountRoot {

  /** Runtime **/
  public Stack queueActionStack;
  public MmpGroup phoneGroup = null;
  /** Objects **/
  public MmpSession session;

  public MmpAccountRoot( String userId ) {
    super( userId );
  }

  public void construct() {
    host = "mrim.mail.ru";
    port = "2042";
  }

  public void initSpecialData() {
    /** New session instance **/
    session = new MmpSession( this );
  }

  public void saveSpecialSettings() throws Throwable {
  }

  public void show() {
  }

  public String getAccType() {
    return "mmp";
  }

  public void sendTypingStatus( String userId, boolean b ) {
  }

  public byte[] sendMessage( BuddyItem buddyItem, String string,
          String resource ) throws IOException {
    byte[] cookie = new byte[ 8 ];
    if ( buddyItem.isPhone() ) {
      String userPhone = buddyItem.getUserId();
      if ( !userPhone.startsWith( "+" ) ) {
        userPhone += "+";
      }
      MmpPacketSender.MRIM_CS_SMS_MESSAGE( this, userPhone, string );
    } else {
      LogUtil.outMessage( ">>> contactItem.getUserId()="
              + buddyItem.getUserId() );
      cookie = MmpPacketSender.MRIM_CS_MESSAGE( this, buddyItem.getUserId(),
              string, 0x00000028, " " );
    }
    return cookie;
  }

  public void sendWakeup( BuddyItem buddyItem ) throws IOException {
    LogUtil.outMessage( ">>> contactItem.getUserId()="
            + buddyItem.getUserId() );
    MmpPacketSender.MRIM_CS_MESSAGE( this, buddyItem.getUserId(),
            Localization.getMessage( "WAKE_UP_NOT_SUPPORTED" ),
            0x80400000,
            "eNptUsFugkAQJaYnE/9h0nOji6Kx+g29ND3uBXFBUgSDa3sw/lv7Cf7BalklWFCg"
            + "aZtoSheMSY297Jt9M/PezmRLkiQ1SpI0xS7VZazaY7M4tJEh15sy7hNdR/lpqbYh"
            + "I+V2inXHprRniQBh3TYtrGsD1R0TWkcKPKgDZ6h2Z7NKeYo1x3JcUdrFLukjbLiE"
            + "2Aj3rAlBBVVvNo9kHuS0wPPE32p0XosuVU/t4t0FKzcb/7mdaIFFIr+fJU46LeXI"
            + "C8zplnJpKdfbXTErfjLJ86Np9xU80WQ8Ut1+sZ6x3MZar4E1sdsewiPXoUSjlfI8"
            + "WvCYe6EfwDba7hO2iTNYshh2bL1IPT853EDkhSyIIC34NE5YGC03PGRFvOPLbyZa"
            + "+C72M7hTTat6P4GXNx4mDBZ8LWQ9j+9WgQ8faRh8+j5cvwrhzUF4XldhHrAvlhxA"
            + "aKY/RxkBe3iPVjzhsPZ4fOjAgNJRp1ZTDWLT6jA3cSe1fDqolGdX4utkWSb9Alob"
            + "C7Q=" );
  }

  public MmpItem setBuddyStatus( String buddyId, long buddyStatus ) {
    GroupHeader groupItem;
    for ( int c = 0; c < buddyItems.size(); c++ ) {
      groupItem = ( GroupHeader ) buddyItems.elementAt( c );
      for ( int i = 0; i < groupItem.getChildsCount(); i++ ) {
        if ( ( ( MmpItem ) groupItem.getChilds().elementAt( i ) ).userId
                .equals( buddyId ) ) {
          ( ( MmpItem ) groupItem.getChilds().elementAt( i ) ).setStatusIndex(
                  MmpStatusUtil.getStatusIndex( buddyStatus ), null );
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
        if ( ( ( MmpItem ) groupItem.getChilds().elementAt( i ) ).userId
                .equals( buddyId ) ) {
          return MmpStatusUtil.getStatus( ( ( MmpItem ) groupItem.getChilds()
                  .elementAt( i ) ).getStatusIndex() );
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
      for ( int i = 0; i < ( ( GroupHeader ) buddyItems.elementAt( c ) )
              .getChildsCount(); i++ ) {
        ( ( MmpItem ) ( ( GroupHeader ) buddyItems.elementAt( c ) )
                .getChilds().elementAt( i ) ).updateUiData();
      }
    }
    updateMainFrameUI();
  }

  public void connectAction( final int statusIndex ) {
    if ( isConnecting || this.statusIndex != 0 ) {
      return;
    }
    isConnecting = true;

    new Thread() {
      public void run() {
        try {
          do {
            if ( MidletMain.httpHiddenPing > 0 ) {
              try {
                NetConnection.httpPing( "http://www.mail.ru" );
              } catch ( IOException ex ) {
                LogUtil.outMessage( "HTTP hidden connection failed" );
              }
            }
            try {
              if ( session.login_stage( host + ":" + port, userId, userPassword,
                      MmpStatusUtil.getStatus( statusIndex ), "" ) ) {
                MmpAccountRoot.this.statusIndex = statusIndex;
                LogUtil.outMessage( "Updating status in AccountStatus" );
                MidletMain.mainFrame.updateAccountsStatus();
                ActionExec.setConnectionStage( MmpAccountRoot.this, 10 );
              }
              isConnecting = false;
              return;
            } catch ( IOException ex ) {
              LogUtil.outMessage( "IO Exception" );
              ActionExec.showError( Localization.getMessage( "IO_EXCEPTION" ) );
            } catch ( IncorrectAddressException ex ) {
              LogUtil.outMessage( "Incorrect address" );
              ActionExec.showError( Localization.getMessage( "INCORRECT_ADDRESS" ) );
            } catch ( Throwable ex ) {
              LogUtil.outMessage( "Throwable" );
              ActionExec.showError( Localization.getMessage( "THROWABLE" ) );
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
    this.buddyItems = buddyList;
  }

  public String getStatusImages() {
    return "/res/groups/img_mmpstatus.png";
  }

  public void setPrivateItems( Vector privateList ) {
  }

  public void setStatusText( String statusText, boolean isStatusReadable ) {
    super.setStatusText( statusText, isStatusReadable );
    try {
      MmpPacketSender.MRIM_CS_CHANGE_STATUS( this,
              MmpStatusUtil.getStatus( statusIndex ), statusText );
    } catch ( IOException ex ) {
      LogUtil.outMessage( "Error while sending status in MMP" );
    }
  }

  public BuddyGroup getGroupInstance() {
    return new MmpGroup();
  }

  public BuddyItem getBuddyInstance() {
    return new MmpItem();
  }

  public Cookie addGroup( BuddyGroup buddyGroup ) throws IOException {
    MmpGroup mmpGroup = ( ( MmpGroup ) buddyGroup );
    mmpGroup.contactId = getGroupContactId();
    mmpGroup.flags = getNextGroupId();
    Cookie cookie = MmpPacketSender.MRIM_CS_ADD_CONTACT( this, mmpGroup.flags,
            0x00000000, new byte[ 0 ],
            StringUtil.string1251ToByteArray( mmpGroup.getUserId() ), new byte[ 0 ] );
    return cookie;
  }

  public Cookie addBuddy( BuddyItem buddyItem, BuddyGroup buddyGroup ) throws IOException {
    Cookie cookie;
    if ( buddyItem.isPhone() ) {
      buddyItem.setUserPhone( buddyItem.getUserId() );
      ( ( MmpItem ) buddyItem ).flags =
              PacketType.CONTACT_FLAG_PHONE;
      cookie = MmpPacketSender.MRIM_CS_ADD_CONTACT( this, 0x00100000,
              0x67000000, "phone".getBytes(),
              StringUtil.string1251ToByteArray( buddyItem.getUserNick() ),
              buddyItem.getUserId().getBytes() );
    } else {
      cookie = MmpPacketSender.MRIM_CS_ADD_CONTACT( this, 0x00000000,
              ( ( MmpGroup ) buddyGroup ).getId()/*& 0x0000ffff*/,
              buddyItem.getUserId().getBytes(),
              StringUtil.string1251ToByteArray( buddyItem.getUserNick() ),
              new byte[ 0 ] );
    }
    return cookie;
  }

  public Cookie renameBuddy( final String itemName, final BuddyItem buddyItem,
          String phones ) throws IOException {
    String buddyId;
    if ( buddyItem.isPhone() ) {
      buddyId = "phone";
    } else {
      buddyId = ( ( MmpItem ) buddyItem ).userId;
    }
    return MmpPacketSender.MRIM_CS_MODIFY_CONTACT( this,
            ( ( MmpItem ) buddyItem ).contactId,
            ( ( MmpItem ) buddyItem ).flags/* >> 24*/,
            ( ( MmpItem ) buddyItem ).groupId, buddyId.getBytes(),
            StringUtil.string1251ToByteArray( itemName ),
            phones == null ? "" : phones );
  }

  public Cookie renameGroup( final String itemName,
          final BuddyGroup buddyGroup ) throws IOException {
    return MmpPacketSender.MRIM_CS_MODIFY_CONTACT( this,
            ( ( MmpGroup ) buddyGroup ).contactId,
            ( ( MmpGroup ) buddyGroup ).flags, 0,
            StringUtil.string1251ToByteArray(
            ( ( MmpGroup ) buddyGroup ).userId ),
            StringUtil.string1251ToByteArray( itemName ), "" );
  }

  public void requestAuth( String requestText, BuddyItem buddyItem )
          throws IOException {
    byte[] data = StringUtil.string1251ToByteArray( requestText );
    String reasonText = "AgAAAAAAAAAGAAAA".concat(
            Base64.encode( data, 0, data.length ) );
    LogUtil.outMessage( reasonText );
    MmpPacketSender.MRIM_CS_MESSAGE( this, buddyItem.getUserId(), reasonText,
            DataUtil.reverseLong( PacketType.MESSAGE_FLAG_AUTHORIZE ), "" );
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
    return MmpPacketSender.MRIM_CS_MODIFY_CONTACT( this,
            ( ( MmpItem ) buddyItem ).contactId,
            ( ( MmpItem ) buddyItem ).flags | PacketType.CONTACT_FLAG_REMOVED,
            ( ( MmpItem ) buddyItem ).groupId, buddyId.getBytes(),
            StringUtil.string1251ToByteArray(
            ( ( MmpItem ) buddyItem ).userNick ), phones == null ? "" : phones );
  }

  public Cookie removeGroup( final BuddyGroup groupHeader ) throws IOException {
    return MmpPacketSender.MRIM_CS_MODIFY_CONTACT( this,
            ( ( MmpGroup ) groupHeader ).contactId,
            ( ( MmpGroup ) groupHeader ).flags
            | PacketType.CONTACT_FLAG_REMOVED/*0x03020001*/, 0,
            StringUtil.string1251ToByteArray(
            ( ( MmpGroup ) groupHeader ).userId ),
            StringUtil.string1251ToByteArray(
            ( ( MmpGroup ) groupHeader ).userId ), "" );
  }

  public DirectConnection getDirectConnectionInstance() {
    return null;
  }

  public int getNextBuddyId() {
    return ( int ) ( 0x00000002 | ( ( new Random(
            System.currentTimeMillis() ).nextLong() ) & 0xffff0000 ) );
  }

  public int getNextGroupId() {
    return ( int ) ( ( buddyItems.size() << 24 ) | PacketType.CONTACT_FLAG_GROUP );
  }

  public int getGroupContactId() {
    final int groupsCount = buddyItems.size();
    for ( int contactId = 0; contactId < PacketType.NORM_GROUPS_MAX; contactId++ ) {
      for ( int c = 0; c <= groupsCount; c++ ) {
        if ( c == groupsCount ) {
          return contactId;
        } else if ( ( ( MmpGroup ) buddyItems.elementAt( c ) ).contactId == contactId ) {
          break;
        }
      }
    }
    return 0;
  }
}
