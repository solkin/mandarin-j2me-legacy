package com.tomclaw.mandarin.icq;

import com.tomclaw.mandarin.core.Cookie;
import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.mandarin.net.BinarySpore;
import com.tomclaw.tompacket.PacketBuilder;
import com.tomclaw.utils.ArrayUtil;
import com.tomclaw.utils.DataUtil;
import com.tomclaw.utils.LogUtil;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class IcqPacketSender {

  public static final int TLV_AV_AUTH = 0x0066;
  public static final int TLV_GR_IDS = 0x00C8;
  public static final int TLV_UNK = 0x00C9;
  public static final int TLV_AIM_PR_SET = 0x00CA;
  public static final int TLV_VIS_CLASS = 0x00CB;
  public static final int TLV_ALL_OTH_SEE = 0x00CC;
  public static final int TLV_SHORTCUT = 0x00CD;
  public static final int TLV_IMPORT_TIME = 0x00D4;
  public static final int TLV_BUDDY_ICON = 0x00D5;
  public static final int TLV_NICK_NAME = 0x0131;
  public static final int TLV_MAIL_ADDR = 0x0137;
  public static final int TLV_SMS_NUMBER = 0x013A;
  public static final int TLV_BUDDY_COMMENT = 0x013C;
  public static final int TLV_PERS_ALERTS = 0x013D;
  public static final int TLV_SOUND_BUDDY = 0x013E;
  public static final int TLV_FIRST_MESS = 0x0145;

  public static byte[] sendMessage( final IcqSession icqSession,
          final String buddyId, final String msgText ) {
    final byte[] msgCookie = new byte[ 8 ];
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        DataUtil.nextBytes( msgCookie );
        Hashtable data = new Hashtable();
        data.put( "DATAGRAM_SEQ_NUM", new Integer( icqSession.getNextSeq() ) );
        data.put( "FLAP_DATA_SIZE", "SNAC" );
        data.put( "FLAP_DATA", "SNAC" );
        data.put( "SERVICE_ID", new Integer( 0x04 ) );
        data.put( "SUBTYPE_ID", new Integer( 0x06 ) );
        data.put( "SNAC_REQUEST_ID", new Long( 17 ) );
        data.put( "SNAC_DATA", "CLI_SEND_ICBM" );
        data.put( "MSG_ID_COOKIE", msgCookie );
        data.put( "SCREENNAME", buddyId );
        data.put( "MESSAGE_TEXT_STRING", msgText );
        PacketBuilder.writePacket( this, "FLAP", data );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    return msgCookie;
  }

  public static Cookie sendMsgAck( final IcqSession icqSession,
          final int chanel, final String userId, final byte[] cookies ) {
    final Cookie cookie = new Cookie();
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        Hashtable data = new Hashtable();
        data.put( "DATAGRAM_SEQ_NUM", new Integer( icqSession.getNextSeq() ) );
        data.put( "FLAP_DATA_SIZE", "SNAC" );
        data.put( "FLAP_DATA", "SNAC" );
        data.put( "SERVICE_ID", new Integer( 0x04 ) );
        data.put( "SUBTYPE_ID", new Integer( 0x0b ) );
        data.put( "SNAC_REQUEST_ID", new Long( cookie.cookieValue ) );
        data.put( "SNAC_DATA", "CLI_MSG_ACK" );
        data.put( "MSG_COOKIE", cookies );
        data.put( "MSG_CHANNEL", new Integer( chanel ) );
        data.put( "SCREENNAME", userId );
        PacketBuilder.writePacket( this, "FLAP", data );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    return cookie;
  }

  public static Cookie setStatus( final IcqSession icqSession, 
          final int statusId ) {
    /** Sending status to server **/
    final Cookie cookie = new Cookie();
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        Snac snac = new Snac( 0x0001, 0x001e, 0, 0, cookie.cookieValue );
        snac.addByteArray( new byte[]{
                  ( byte ) 0x00, ( byte ) 0x06, ( byte ) 0x00, ( byte ) 0x04 } );
        snac.addWord( 0x0001 ); // Web-aware
        snac.addWord( statusId );
        snac.send( this, icqSession.getNextSeq() );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    return cookie;
  }

  public static Cookie setUpdatePrivacy( final IcqSession icqSession, 
          final int privateBuddyId, final int privacyStatus ) {
    final Cookie cookie;
    if ( privateBuddyId != -1 ) {
      cookie = new Cookie();
      /** If buddy list contain such field, as privateBuddy **/
      BinarySpore binarySpore = new BinarySpore() {

        public void onRun() throws Throwable {
          Snac snac = new Snac( 0x0013, 0x0009, 0, 0, cookie.cookieValue );
          snac.addWord( 0x0000 );
          snac.addWord( 0x0000 );
          snac.addWord( privateBuddyId );
          snac.addWord( 0x0004 );
          snac.addWord( 0x0005 );
          snac.addWord( 0x00ca );
          snac.addWord( 0x0001 );
          snac.addByte( privacyStatus );
          snac.send( this, icqSession.getNextSeq() );
        }
      };
      icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    } else {
      cookie = null;
      LogUtil.outMessage( "No private buddy" );
    }
    return cookie;
  }

  public static Cookie addPrivacy( final IcqSession icqSession, 
          final String user_uin, int groupId, final int itemId, 
          final int itemFlag ) {
    final Cookie cookie = new Cookie();
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        Snac p = new Snac( 0x0013, 0x0008, 0, 0, cookie.cookieValue );
        p.addWordLString( user_uin );
        p.addWord( 0x0000 );
        p.addWord( itemId );
        p.addWord( itemFlag );
        p.addWord( 0x0000 );
        p.send( this, icqSession.getNextSeq() );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    return cookie;
  }

  public static Cookie deletePrivacy( final IcqSession icqSession, 
          final String user_uin, int groupId, final int itemId, 
          final int itemFlag ) {
    final Cookie cookie = new Cookie();
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        Snac p = new Snac( 0x0013, 0x000a, 0, 0, cookie.cookieValue );
        p.addWordLString( user_uin );
        p.addWord( 0x0000 );
        p.addWord( itemId );
        p.addWord( itemFlag );
        p.addWord( 0x0000 );
        p.send( this, icqSession.getNextSeq() );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    return cookie;
  }

  public static Cookie removeBuddy( final IcqSession icqSession, 
          final String buddyId, final int groupId, final int itemId, 
          final int itemFlag ) {
    final Cookie cookie = new Cookie();
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        Snac p = new Snac( 0x0013, 0x000a, 0, 0, cookie.cookieValue );
        p.addWordLString( buddyId );
        p.addWord( groupId );
        p.addWord( itemId );
        p.addWord( itemFlag );
        p.addWord( 0 );
        p.send( this, icqSession.getNextSeq() );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    return cookie;
  }

  public static Cookie addBuddy( final IcqSession icqSession, 
          final byte[] buddyId, final int groupId, final int itemId, 
          final int itemFlag, final boolean authReq, final byte[] nickName ) {
    final Cookie cookie = new Cookie();
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        Snac p = new Snac( 0x0013, 0x0008, 0, 0, cookie.cookieValue );
        p.addWord( buddyId.length );
        p.addByteArray( buddyId );
        p.addWord( groupId );
        p.addWord( itemId );
        p.addWord( itemFlag );
        Snac p1 = new Snac( 0x0013, 0x0008, 0, 0, cookie.cookieValue );
        if ( authReq ) {
          p1.addTlv( TLV_AV_AUTH, new byte[ 0 ] );
        }
        if ( nickName != null ) {
          p1.addTlv( TLV_NICK_NAME, nickName );
        }
        byte[] buffer = new byte[ p1.getByteArray().toByteArray().length - 10 ];
        System.arraycopy( p1.getByteArray().toByteArray(), 10, buffer,
                0, p1.getByteArray().toByteArray().length - 10 );
        p.addWord( buffer.length );
        p.addByteArray( buffer );
        p.send( this, icqSession.getNextSeq() );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    return cookie;
  }

  public static Cookie updateBuddy( final IcqSession icqSession, 
          final byte[] buddyId, final int groupId, final int itemId, 
          final int itemFlag, final boolean authReq, final byte[] nickName ) {
    final Cookie cookie = new Cookie();
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        Snac p = new Snac( 0x0013, 0x0009, 0, 0, cookie.cookieValue );
        p.addWord( buddyId.length );
        p.addByteArray( buddyId );
        p.addWord( groupId );
        p.addWord( itemId );
        p.addWord( itemFlag );
        Snac p1 = new Snac( 0x0013, 0x0009, 0, 0, cookie.cookieValue );
        if ( authReq ) {
          p1.addTlv( TLV_AV_AUTH, new byte[ 0 ] );
        }
        if ( nickName != null ) {
          p1.addTlv( TLV_NICK_NAME, nickName );
        }
        byte[] buffer = new byte[ p1.getByteArray().toByteArray().length - 10 ];
        System.arraycopy( p1.getByteArray().toByteArray(), 10, buffer,
                0, p1.getByteArray().toByteArray().length - 10 );
        p.addWord( buffer.length );
        p.addByteArray( buffer );
        p.send( this, icqSession.getNextSeq() );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    return cookie;
  }

  public static Cookie removeYourself( final IcqSession icqSession, 
          final String buddyId ) {
    final Cookie cookie = new Cookie();
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        Snac p = new Snac( 0x0013, 0x0016, 0, 0, cookie.cookieValue );
        p.addByteLString( buddyId );
        p.send( this, icqSession.getNextSeq() );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    return cookie;
  }

  public static Cookie authRequest( final IcqSession icqSession, 
          final String buddyId, final byte[] reasonMsg ) {
    final Cookie cookie = new Cookie();
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        Snac p = new Snac( 0x0013, 0x0018, 0, 0, cookie.cookieValue );
        // Uin
        p.addByteLString( buddyId );
        // Reason
        p.addWord( reasonMsg.length );
        p.addByteArray( reasonMsg );
        p.addWord( 0x0000 );

        p.send( this, icqSession.getNextSeq() );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    return cookie;
  }

  public static Cookie authReply( final IcqSession icqSession, 
          final String buddyId, final boolean isAccepted, 
          final byte[] reasonMsg ) {
    final Cookie cookie = new Cookie();
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        Snac p = new Snac( 0x0013, 0x001a, 0, 0, cookie.cookieValue );
        p.addByteLString( buddyId );
        p.addByte( isAccepted ? 1 : 0 );
        p.addWord( reasonMsg.length );
        p.addByteArray( reasonMsg );
        p.send( this, icqSession.getNextSeq() );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    return cookie;
  }

  public static Cookie shortInfoRequest( final IcqSession icqSession, final String userId,
          int reqSeqNum ) {
    final Cookie cookie = new Cookie();
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        Hashtable data = new Hashtable();
        data.put( "DATAGRAM_SEQ_NUM", new Integer( icqSession.getNextSeq() ) );
        data.put( "FLAP_DATA_SIZE", "SNAC" );
        data.put( "FLAP_DATA", "SNAC" );
        data.put( "SERVICE_ID", new Integer( 0x25 ) );
        data.put( "SUBTYPE_ID", new Integer( 0x02 ) );
        data.put( "SNAC_REQUEST_ID", new Long( cookie.cookieValue ) );
        data.put( "SNAC_DATA", "CLI_USER_INFO_REQUEST" );
        // data.put( "MAP_NAME", "CLI_USER_INFO_REQUEST:BLOCK_EMAIL" );
        data.put( "MAP_NAME", "CLI_USER_INFO_REQUEST:BLOCK_OSCAR" );
        data.put( "SCREENNAME", userId );
        PacketBuilder.writePacket( this, "FLAP", data );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    return cookie;
  }

  public static void sendKeepAlive( final IcqSession icqSession ) {
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        write( Snac.createFlapHeader( 0x05, icqSession.getNextSeq(), 0 ) );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
  }

  public static Cookie sendCapabilities( final IcqSession icqSession, 
          final int xStatusId, final int aStatusId ) {
    final Cookie cookie = new Cookie();
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        int major = Integer.parseInt( MidletMain.version.substring( 0, MidletMain.version.indexOf( "." ) ) );
        int minor = Integer.parseInt( MidletMain.version.substring( MidletMain.version.indexOf( "." ) + 1 ) );
        Hashtable data = new Hashtable();
        data.put( "DATAGRAM_SEQ_NUM", new Integer( icqSession.getNextSeq() ) );
        data.put( "FLAP_DATA_SIZE", "SNAC" );
        data.put( "FLAP_DATA", "SNAC" );
        data.put( "SERVICE_ID", new Integer( 0x02 ) );
        data.put( "SUBTYPE_ID", new Integer( 0x04 ) );
        data.put( "SNAC_REQUEST_ID", new Long( cookie.cookieValue ) );
        data.put( "SNAC_DATA", "CLI_SET_LOCATION_INFO" );
        if ( !MidletMain.isTest ) {
          data.put( "IS_TEST_BUILD", "NULL" );
        }
        data.put( "MANDARIN_ID", new byte[]{
                  ( byte ) 0x4D, ( byte ) 0x61, ( byte ) 0x6E, ( byte ) 0x64,
                  ( byte ) 0x61, ( byte ) 0x72, ( byte ) 0x69, ( byte ) 0x6E,
                  ( byte ) 0x20, ( byte ) 0x49, ( byte ) 0x4D, ( byte ) 0x00,
                  ( byte ) 0x01, ( byte ) major, ( byte ) minor, ( byte ) 0x00 } );
        if ( xStatusId != -1 ) {
          data.put( "CAP_XSTATUS", CapUtil.getXStatusCap( xStatusId ) );
        }
        if ( aStatusId >= 0x1000 ) {
          data.put( "CAP_EXT_STATUS", IcqStatusUtil.getAStatusCap( aStatusId ) );
        }
        PacketBuilder.writePacket( this, "FLAP", data );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    return cookie;
  }

  public static Cookie requestOfflineMessages( final IcqSession icqSession, 
          final String buddyId ) {
    final Cookie cookie = new Cookie();
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        Snac p = new Snac( 0x0015, 0x0002, 0, 0, cookie.cookieValue );
        p.addWord( 0x0001 );
        p.addWord( 0x000A );
        p.addWordReversed( 0x0008 );
        p.addDWordReversed( Long.parseLong( buddyId ) );
        p.addWordReversed( 0x003C );
        p.addWord( 0x0001 );
        p.send( this, icqSession.getNextSeq() );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    return cookie;
  }

  public static Cookie requestStatusMessage( final IcqSession icqSession, 
          final byte[] cookies, final String buddyId ) {
    final Cookie cookie = new Cookie();
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        Hashtable data = new Hashtable();
        data.put( "DATAGRAM_SEQ_NUM", new Integer( icqSession.getNextSeq() ) );
        data.put( "FLAP_DATA_SIZE", "SNAC" );
        data.put( "FLAP_DATA", "SNAC" );
        data.put( "SERVICE_ID", new Integer( 0x04 ) );
        data.put( "SUBTYPE_ID", new Integer( 0x06 ) );
        data.put( "SNAC_REQUEST_ID", new Long( cookie.cookieValue ) );
        data.put( "SNAC_DATA", "CLI_SEND_ICBM_CH2" );
        data.put( "MSG_ID_COOKIE", cookies );
        data.put( "SCREENNAME", buddyId );
        PacketBuilder.writePacket( this, "FLAP", data );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    return cookie;
  }

  public static Cookie sendStatusMessage( final IcqSession icqSession, 
          final byte[] cookies, final String buddyId, 
          final String statusText ) {
    final Cookie cookie = new Cookie();
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        Hashtable data = new Hashtable();
        data.put( "DATAGRAM_SEQ_NUM", new Integer( icqSession.getNextSeq() ) );
        data.put( "FLAP_DATA_SIZE", "SNAC" );
        data.put( "FLAP_DATA", "SNAC" );
        data.put( "SERVICE_ID", new Integer( 0x04 ) );
        data.put( "SUBTYPE_ID", new Integer( 0x0b ) );
        data.put( "SNAC_REQUEST_ID", new Long( cookie.cookieValue ) );
        data.put( "SNAC_DATA", "CLI_ICBM_SEND_STATUS_MSG" );
        data.put( "ICBM_COOKIE", cookies );
        data.put( "SCREENNAME", buddyId );
        data.put( "STATUS_TEXT", statusText );
        PacketBuilder.writePacket( this, "FLAP", data );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    return cookie;
  }

  public static Cookie sendXStatusText( final IcqSession icqSession, 
          final String userId, final byte[] cookies, final String buddyId, 
          final byte[] xTitle, final byte[] xText ) {
    final Cookie cookie = new Cookie();
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        Snac p = new Snac( 0x0004, 0x000b, 0, 0, cookie.cookieValue );
        //Cookie
        //byte[] cookie = new byte[8];
        //new Random().nextBytes(cookie);
        p.addByteArray( cookies );
        //Channel
        p.addWord( 0x0002 );
        //Screen name
        p.addByteLString( buddyId );
        //Reason: channel specefic
        p.addWord( 0x0003 );

        //Ext data
        ArrayUtil extendedData = new ArrayUtil( new byte[]{
                  ( byte ) 0x1b, ( byte ) 0x00 } );
        //extendedData += new String(new byte[]{(byte) 0x27, (byte) 0x11});

        //Version
        extendedData.append( new byte[]{ ( byte ) 0x08, ( byte ) 0x00 } );

        //Plugin: None
        extendedData.append( new byte[]{
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00 } );

        //Unknown
        extendedData.append( new byte[]{
                  ( byte ) 0x00, ( byte ) 0x00 } );

        //Client capabilities
        extendedData.append( new byte[]{
                  ( byte ) 0x03, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00 } );

        //Unknown
        extendedData.append( new byte[]{ ( byte ) 0x04 } );

        //Downcounter?
        extendedData.append( new byte[]{
                  ( byte ) 0xfe, ( byte ) 0xff } );

        //Length - may change!
        extendedData.append( new byte[]{
                  ( byte ) 0x0e, ( byte ) 0x00 } );

        //Downcounter?
        extendedData.append( new byte[]{
                  ( byte ) 0xfe, ( byte ) 0xff } );

        //Unknown
        extendedData.append( new byte[]{
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00 } );

        //Message type: plugin message described by text string
        extendedData.append( new byte[]{ ( byte ) 0x1a } );

        //Message flags
        extendedData.append( new byte[]{ ( byte ) 0x00 } );

        //Status code
        extendedData.append( new byte[]{ ( byte ) 0x00, ( byte ) 0x00 } );

        //Priority code
        extendedData.append( new byte[]{ ( byte ) 0x00, ( byte ) 0x00 } );

        //Text length
        extendedData.append( new byte[]{ ( byte ) 0x01, ( byte ) 0x00 } );

        //Text
        extendedData.append( new byte[]{ ( byte ) 0x00 } );

        //Request
        extendedData.append( new byte[]{
                  ( byte ) 0x4F, ( byte ) 0x00, ( byte ) 0x3B, ( byte ) 0x60,
                  ( byte ) 0xB3, ( byte ) 0xEF, ( byte ) 0xD8, ( byte ) 0x2A,
                  ( byte ) 0x6C, ( byte ) 0x45, ( byte ) 0xA4, ( byte ) 0xE0,
                  ( byte ) 0x9C, ( byte ) 0x5A, ( byte ) 0x5E, ( byte ) 0x67,
                  ( byte ) 0xE8, ( byte ) 0x65, ( byte ) 0x08, ( byte ) 0x00,
                  ( byte ) 0x2A, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00 } );

        extendedData.append( "Script Plug-in: Remote Notification Arrive".getBytes() );

        extendedData.append( new byte[]{
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x01, ( byte ) 0x00,
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x9D,
                  ( byte ) 0x01, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x99,
                  ( byte ) 0x01, ( byte ) 0x00, ( byte ) 0x00 } );

        String xTextResp = "<NR><RES>&lt;ret event='OnRemoteNotification'"
                + "&gt;&lt;srv&gt;&lt;id&gt;cAwaySrv&lt;/id&gt;&lt;val "
                + "srv_id='cAwaySrv'&gt;&lt;Root&gt;&lt;CASXtraSetAwayMessage"
                + "&gt;&lt;/CASXtraSetAwayMessage&gt;&lt;uin&gt;" + userId + "&lt;"
                + "/uin&gt;&lt;index&gt;4&lt;/index&gt;&lt;title&gt;" + new String( xTitle )
                + "&lt;/title&gt;&lt;desc&gt;" + new String( xText )
                + "&lt;/desc&gt;&lt;/Root&gt;&lt;/val&gt;&lt;/srv&gt;&lt;"
                + "/ret&gt;</RES></NR>";
        extendedData.append( xTextResp.getBytes() );

        ////TLV length
        //p.addWord(extendedData.length());
        p.addByteArray( extendedData.byteString );

        p.send( this, icqSession.getNextSeq() );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    return cookie;
  }

  public static Cookie requestXStatusText( final IcqSession icqSession, 
          final String userId, final byte[] cookies, final String buddyId ) {
    final Cookie cookie = new Cookie();
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        Snac p = new Snac( 0x0004, 0x0006, 0, 0, cookie.cookieValue );
        //Cookie
        p.addByteArray( cookies );
        //Channel
        p.addWord( 0x0002 );
        //Screen name length
        p.addByteLString( buddyId );
        //Rendezvous message data
        p.addWord( 0x0005 );

        ArrayUtil radezvousData = new ArrayUtil();
        radezvousData.append( new byte[]{
                  ( byte ) 0x00, ( byte ) 0x00 } );
        radezvousData.append( cookies );
        //Server relaying
        radezvousData.append( new byte[]{ ( byte ) 0x09, ( byte ) 0x46, ( byte ) 0x13,
                  ( byte ) 0x49, ( byte ) 0x4c, ( byte ) 0x7f, ( byte ) 0x11, ( byte ) 0xd1,
                  ( byte ) 0x82, ( byte ) 0x22, ( byte ) 0x44, ( byte ) 0x45, ( byte ) 0x53,
                  ( byte ) 0x54, ( byte ) 0x00, ( byte ) 0x00 } );
        //TLV unk
        radezvousData.append( new byte[]{ ( byte ) 0x00, ( byte ) 0x0a, ( byte ) 0x00,
                  ( byte ) 0x02, ( byte ) 0x00, ( byte ) 0x01 } );
        //TLV unk
        radezvousData.append( new byte[]{ ( byte ) 0x00, ( byte ) 0x0f, ( byte ) 0x00,
                  ( byte ) 0x00 } );

        //Ext data
        radezvousData.append( new byte[]{ ( byte ) 0x27, ( byte ) 0x11 } );

        ArrayUtil extendedData = new ArrayUtil( new byte[]{
                  ( byte ) 0x1b, ( byte ) 0x00, ( byte ) 0x08, ( byte ) 0x00, ( byte ) 0x00,
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x03, ( byte ) 0x00, ( byte ) 0x00,
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0xfe, ( byte ) 0xff, ( byte ) 0x0e,
                  ( byte ) 0x00, ( byte ) 0xfe, ( byte ) 0xff, ( byte ) 0x00, ( byte ) 0x00,
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
                  ( byte ) 0x1a, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x01,
                  ( byte ) 0x00, ( byte ) 0x01, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x4f,
                  ( byte ) 0x00, ( byte ) 0x3b, ( byte ) 0x60, ( byte ) 0xb3, ( byte ) 0xef,
                  ( byte ) 0xd8, ( byte ) 0x2a, ( byte ) 0x6c, ( byte ) 0x45, ( byte ) 0xa4,
                  ( byte ) 0xe0, ( byte ) 0x9c, ( byte ) 0x5a, ( byte ) 0x5e, ( byte ) 0x67,
                  ( byte ) 0xe8, ( byte ) 0x65, ( byte ) 0x08, ( byte ) 0x00, ( byte ) 0x2a,
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x53, ( byte ) 0x63,
                  ( byte ) 0x72, ( byte ) 0x69, ( byte ) 0x70, ( byte ) 0x74, ( byte ) 0x20,
                  ( byte ) 0x50, ( byte ) 0x6c, ( byte ) 0x75, ( byte ) 0x67, ( byte ) 0x2d,
                  ( byte ) 0x69, ( byte ) 0x6e, ( byte ) 0x3a, ( byte ) 0x20, ( byte ) 0x52,
                  ( byte ) 0x65, ( byte ) 0x6d, ( byte ) 0x6f, ( byte ) 0x74, ( byte ) 0x65,
                  ( byte ) 0x20, ( byte ) 0x4e, ( byte ) 0x6f, ( byte ) 0x74, ( byte ) 0x69,
                  ( byte ) 0x66, ( byte ) 0x69, ( byte ) 0x63, ( byte ) 0x61, ( byte ) 0x74,
                  ( byte ) 0x69, ( byte ) 0x6f, ( byte ) 0x6e, ( byte ) 0x20, ( byte ) 0x41,
                  ( byte ) 0x72, ( byte ) 0x72, ( byte ) 0x69, ( byte ) 0x76, ( byte ) 0x65,
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x01, ( byte ) 0x00, ( byte ) 0x00,
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
                  ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
                  ( byte ) 0x13, ( byte ) 0x01, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x0f,
                  ( byte ) 0x01, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x3c, ( byte ) 0x4e,
                  ( byte ) 0x3e, ( byte ) 0x3c, ( byte ) 0x51, ( byte ) 0x55, ( byte ) 0x45,
                  ( byte ) 0x52, ( byte ) 0x59, ( byte ) 0x3e, ( byte ) 0x26, ( byte ) 0x6c,
                  ( byte ) 0x74, ( byte ) 0x3b, ( byte ) 0x51, ( byte ) 0x26, ( byte ) 0x67,
                  ( byte ) 0x74, ( byte ) 0x3b, ( byte ) 0x26, ( byte ) 0x6c, ( byte ) 0x74,
                  ( byte ) 0x3b, ( byte ) 0x50, ( byte ) 0x6c, ( byte ) 0x75, ( byte ) 0x67,
                  ( byte ) 0x69, ( byte ) 0x6e, ( byte ) 0x49, ( byte ) 0x44, ( byte ) 0x26,
                  ( byte ) 0x67, ( byte ) 0x74, ( byte ) 0x3b, ( byte ) 0x73, ( byte ) 0x72,
                  ( byte ) 0x76, ( byte ) 0x4d, ( byte ) 0x6e, ( byte ) 0x67, ( byte ) 0x26,
                  ( byte ) 0x6c, ( byte ) 0x74, ( byte ) 0x3b, ( byte ) 0x2f, ( byte ) 0x50,
                  ( byte ) 0x6c, ( byte ) 0x75, ( byte ) 0x67, ( byte ) 0x69, ( byte ) 0x6e,
                  ( byte ) 0x49, ( byte ) 0x44, ( byte ) 0x26, ( byte ) 0x67, ( byte ) 0x74,
                  ( byte ) 0x3b, ( byte ) 0x26, ( byte ) 0x6c, ( byte ) 0x74, ( byte ) 0x3b,
                  ( byte ) 0x2f, ( byte ) 0x51, ( byte ) 0x26, ( byte ) 0x67, ( byte ) 0x74,
                  ( byte ) 0x3b, ( byte ) 0x3c, ( byte ) 0x2f, ( byte ) 0x51, ( byte ) 0x55,
                  ( byte ) 0x45, ( byte ) 0x52, ( byte ) 0x59, ( byte ) 0x3e, ( byte ) 0x3c,
                  ( byte ) 0x4e, ( byte ) 0x4f, ( byte ) 0x54, ( byte ) 0x49, ( byte ) 0x46,
                  ( byte ) 0x59, ( byte ) 0x3e, ( byte ) 0x26, ( byte ) 0x6c, ( byte ) 0x74,
                  ( byte ) 0x3b, ( byte ) 0x73, ( byte ) 0x72, ( byte ) 0x76, ( byte ) 0x26,
                  ( byte ) 0x67, ( byte ) 0x74, ( byte ) 0x3b, ( byte ) 0x26, ( byte ) 0x6c,
                  ( byte ) 0x74, ( byte ) 0x3b, ( byte ) 0x69, ( byte ) 0x64, ( byte ) 0x26,
                  ( byte ) 0x67, ( byte ) 0x74, ( byte ) 0x3b, ( byte ) 0x63, ( byte ) 0x41,
                  ( byte ) 0x77, ( byte ) 0x61, ( byte ) 0x79, ( byte ) 0x53, ( byte ) 0x72,
                  ( byte ) 0x76, ( byte ) 0x26, ( byte ) 0x6c, ( byte ) 0x74, ( byte ) 0x3b,
                  ( byte ) 0x2f, ( byte ) 0x69, ( byte ) 0x64, ( byte ) 0x26, ( byte ) 0x67,
                  ( byte ) 0x74, ( byte ) 0x3b, ( byte ) 0x26, ( byte ) 0x6c, ( byte ) 0x74,
                  ( byte ) 0x3b, ( byte ) 0x72, ( byte ) 0x65, ( byte ) 0x71, ( byte ) 0x26,
                  ( byte ) 0x67, ( byte ) 0x74, ( byte ) 0x3b, ( byte ) 0x26, ( byte ) 0x6c,
                  ( byte ) 0x74, ( byte ) 0x3b, ( byte ) 0x69, ( byte ) 0x64, ( byte ) 0x26,
                  ( byte ) 0x67, ( byte ) 0x74, ( byte ) 0x3b, ( byte ) 0x41, ( byte ) 0x77,
                  ( byte ) 0x61, ( byte ) 0x79, ( byte ) 0x53, ( byte ) 0x74, ( byte ) 0x61,
                  ( byte ) 0x74, ( byte ) 0x26, ( byte ) 0x6c, ( byte ) 0x74, ( byte ) 0x3b,
                  ( byte ) 0x2f, ( byte ) 0x69, ( byte ) 0x64, ( byte ) 0x26, ( byte ) 0x67,
                  ( byte ) 0x74, ( byte ) 0x3b, ( byte ) 0x26, ( byte ) 0x6c, ( byte ) 0x74,
                  ( byte ) 0x3b, ( byte ) 0x74, ( byte ) 0x72, ( byte ) 0x61, ( byte ) 0x6e,
                  ( byte ) 0x73, ( byte ) 0x26, ( byte ) 0x67, ( byte ) 0x74, ( byte ) 0x3b,
                  ( byte ) 0x31, ( byte ) 0x26, ( byte ) 0x6c, ( byte ) 0x74, ( byte ) 0x3b,
                  ( byte ) 0x2f, ( byte ) 0x74, ( byte ) 0x72, ( byte ) 0x61, ( byte ) 0x6e,
                  ( byte ) 0x73, ( byte ) 0x26, ( byte ) 0x67, ( byte ) 0x74, ( byte ) 0x3b,
                  ( byte ) 0x26, ( byte ) 0x6c, ( byte ) 0x74, ( byte ) 0x3b, ( byte ) 0x73,
                  ( byte ) 0x65, ( byte ) 0x6e, ( byte ) 0x64, ( byte ) 0x65, ( byte ) 0x72,
                  ( byte ) 0x49, ( byte ) 0x64, ( byte ) 0x26, ( byte ) 0x67, ( byte ) 0x74,
                  ( byte ) 0x3b } );
        extendedData.append( userId.getBytes() );
        extendedData.append( new byte[]{
                  ( byte ) 0x26, ( byte ) 0x6c, ( byte ) 0x74, ( byte ) 0x3b, ( byte ) 0x2f,
                  ( byte ) 0x73, ( byte ) 0x65, ( byte ) 0x6e, ( byte ) 0x64, ( byte ) 0x65,
                  ( byte ) 0x72, ( byte ) 0x49, ( byte ) 0x64, ( byte ) 0x26, ( byte ) 0x67,
                  ( byte ) 0x74, ( byte ) 0x3b, ( byte ) 0x26, ( byte ) 0x6c, ( byte ) 0x74,
                  ( byte ) 0x3b, ( byte ) 0x2f, ( byte ) 0x72, ( byte ) 0x65, ( byte ) 0x71,
                  ( byte ) 0x26, ( byte ) 0x67, ( byte ) 0x74, ( byte ) 0x3b, ( byte ) 0x26,
                  ( byte ) 0x6c, ( byte ) 0x74, ( byte ) 0x3b, ( byte ) 0x2f, ( byte ) 0x73,
                  ( byte ) 0x72, ( byte ) 0x76, ( byte ) 0x26, ( byte ) 0x67, ( byte ) 0x74,
                  ( byte ) 0x3b, ( byte ) 0x3c, ( byte ) 0x2f, ( byte ) 0x4e, ( byte ) 0x4f,
                  ( byte ) 0x54, ( byte ) 0x49, ( byte ) 0x46, ( byte ) 0x59, ( byte ) 0x3e,
                  ( byte ) 0x3c, ( byte ) 0x2f, ( byte ) 0x4e, ( byte ) 0x3e } );

        //TLV length
        p.addWord( radezvousData.byteString.length + 2 + extendedData.byteString.length );
        p.addByteArray( radezvousData.byteString );
        p.addWord( extendedData.byteString.length );
        p.addByteArray( extendedData.byteString );
        // p.addDWord(0x00030000);

        p.send( this, icqSession.getNextSeq() );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    return cookie;
  }

  public static Cookie sendTypingStatus( final IcqSession icqSession, 
          final String buddyId, final boolean typingStatus ) {
    final Cookie cookie = new Cookie();
    BinarySpore binarySpore = new BinarySpore() {

      public void onRun() throws Throwable {
        Snac p = new Snac( 0x0004, 0x0014, 0, 0, cookie.cookieValue );
        p.addByteArray( new byte[]{ // Notification Cookie: 0000000000000000
                  0x00, 0x00, 0x00, 0x00,
                  0x00, 0x00, 0x00, 0x00
                } );
        p.addWord( 0x0001 ); // Notification Channel: 256
        // Uin
        p.addByteLString( buddyId );
        // Flag
        p.addWord( typingStatus ? 0x0002 : 0x0000 );

        p.send( this, icqSession.getNextSeq() );
      }
    };
    icqSession.getNetworkConnection().outputStream.releaseSpore( binarySpore );
    return cookie;
  }
}
