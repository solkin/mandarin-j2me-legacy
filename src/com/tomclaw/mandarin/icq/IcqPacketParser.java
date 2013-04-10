package com.tomclaw.mandarin.icq;

import com.tomclaw.mandarin.main.ActionExec;
import com.tomclaw.mandarin.main.BuddyInfo;
import com.tomclaw.mandarin.main.Cookie;
import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.mandarin.mmp.MmpPacketParser;
import com.tomclaw.tcuilite.ChatItem;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Image;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class IcqPacketParser {

  /**
   * SNAC families
   */
  public static final int GENERIC_SERVICE_CONTROLS = 0x0001;
  public static final int LOCATION_SERVICES = 0x0002;
  public static final int BUDDY_LIST_MANAGEMENT_SERVICE = 0x0003;
  public static final int ICBM_SERVICE = 0x0004;
  public static final int PRIVACY_MANAGEMENT_SERVICE = 0x0009;
  public static final int USAGE_STATS_SERVICE = 0x000b;
  public static final int SSBI_SERVICE = 0x0010;
  public static final int SSI_SERVICE = 0x0013;
  public static final int ICQ_SPECIFIC_EXTENSIONS_SERVICE = 0x0015;
  public static final int AUTHORIZATION_REGISTRATION_SERVICE = 0x0017;
  public static final int BROADCAST_SERVICE = 0x0085;
  public static final int BUDDY_USER_INFO = 0x00025;
  /**
   * Buddy list types
   */
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
  /**
   * Buddy types
   */
  public static final int UNDEFINED_TYPE = 0x00FF;
  public static final int NORMAL_UIN = 0x0000;
  public static final int NORMAL_GROUP = 0x0001;
  public static final int DELETED_UIN = 0x0019;
  public static final int SYSTEM_GROUP = 0x0020;
  public static final int UNK1_SYSTEM_TYPE = 0x001d;
  public static final int UNK2_SYSTEM_TYPE = 0x0015;
  public static final int PERMIT_LIST_RECORD = 0x0002;
  public static final int DENY_LIST_RECORD = 0x0003;
  public static final int IGNORE_LIST_RECORD = 0x000e;
  public static final int PRESENCE_INFO = 0x0005;
  public static final int OWN_ICON_AVATAR = 0x0014;
  public static final int LAST_UPDATE_DATE = 0x000f;
  public static final int PERMIT_DENY_SETTINGS = 0x0004;
  public static final int OWN_GROUP_TYPE = 0x0100;
  public static boolean isReceivePhantoms = false;
  /**
   * Message type
   */
  public static final byte MTYPE_PLAIN = 0x01;
  public static final byte MTYPE_CHAT = 0x02;
  public static final byte MTYPE_FILEREQ = 0x03;
  public static final byte MTYPE_URL = 0x04;
  public static final byte MTYPE_AUTHREQ = 0x06;
  public static final byte MTYPE_AUTHDENY = 0x07;
  public static final byte MTYPE_AUTHOK = 0x08;
  public static final byte MTYPE_SERVER = 0x09;
  public static final byte MTYPE_ADDED = 0x0c;
  public static final byte MTYPE_WWP = 0x0d;
  public static final byte MTYPE_EEXPRESS = 0x0e;
  public static final byte MTYPE_CONTACTS = 0x13;
  public static final byte MTYPE_PLUGIN = 0x1a;
  public static final byte MTYPE_AUTOAWAY = ( byte ) 0xe8;
  public static final byte MTYPE_AUTOBUSY = ( byte ) 0xe9;
  public static final byte MTYPE_AUTONA = ( byte ) 0xea;
  public static final byte MTYPE_AUTODND = ( byte ) 0xeb;
  public static final byte MTYPE_AUTOFFC = ( byte ) 0xec;
  /**
   * Message flags
   */
  public static final byte MFLAG_NORMAL = 0x01;
  public static final byte MFLAG_AUTO = 0x03;
  public static final byte MFLAG_MULTI = ( byte ) 0x80;
  /**
   * SSI ack constants
   */
  public static final int SSI_NO_ERRORS = 0x0000; // No errors (success)
  public static final int SSI_NOT_FOUND = 0x0002; // Item you want to modify not found in list
  public static final int SSI_ALR_EXIST = 0x0003; // Item you want to add allready exists
  public static final int SSI_ADD_ERROR = 0x000A; // Error adding item (invalid id, allready in list, invalid data)
  public static final int SSI_LIMIT_EXC = 0x000C; // Can't add item. Limit for this type of items exceeded
  public static final int SSI_TICQTOAIM = 0x000D; // Trying to add ICQ contact to an AIM list
  public static final int SSI_AUTH_REQD = 0x000E; // Can't add this contact because it requires authorization
  /**
   * Search
   */
  public static final int UMD_PROFILE_USER = 101; // 0x65  {SNAC_byte array}; # IDM__SN or PBC__GUID
  public static final int UMD_PROFILE_FIRST_NAME = 102; // 0x66  {SNAC_byte array};
  public static final int UMD_PROFILE_LAST_NAME = 103; // 0x67  {SNAC_byte array};
  public static final int UMD_PROFILE_GENDER = 104; // 0x68  {SNAC_u32 class=GNR}; # Note ICQ and AIM formats differ
  public static final int UMD_PROFILE_HOME_ADDRESS = 105; // 0x69  {SNAC_TLV_Block class=ADDRESS array}; # Only 1 allowed
  public static final int UMD_PROFILE_FRIENDLY_NAME = 106; // 0x6A  {SNAC_byte array}; # IMD__NICK_NAME
  public static final int UMD_PROFILE_WEBSITE_1 = 107; // 0x6B  {SNAC_byte array}; # IMD__HOME_WEBSITE
  public static final int UMD_PROFILE_RELATIONSHIP_STATUS = 108; // 0x6C  {SNAC_u32 class=RELATIONSHIP_STATUS}; # IMD__MARITAL_STATUS. Values differ
  public static final int UMD_PROFILE_LANG_1 = 109; // 0x6D  {SNAC_byte array}; # IMD__LANG_1 or PBC__PRIMARY_LANGUAGE
  public static final int UMD_PROFILE_JOBS = 110; // 0x6E  {SNAC_TLV_Block class=JOB array};     # PBC__OCCUPATION is job.title and AIM allows only 1
  public static final int UMD_PROFILE_ABOUT_ME = 111; // 0x6F  {SNAC_byte array}; # IMD__ABOUT
  public static final int UMD_PROFILE_BIRTH_DATE = 112; // 0x70  {SNAC_t70}; # 
  public static final int UMD_PROFILE_ONLINE_STATUS = 2035;
  public static final int UMD_PROFILE_WEBAWARE = 2050;
  public static final int UMD_PROFILE_STATUS_LINE = 2052;
  public static final int UMD_PROFILE_VALIDATED_CELLULAR = 2056;
  private static final int CI_GENDER_UNKNOWN = 0;
  private static final int CI_GENDER_MALE = 1;
  private static final int CI_GENDER_FEMALE = 2;

  /**
   * Packet parser method
   * @param flapHeader
   * @param packetData
   */
  public static void parsePacket( IcqAccountRoot icqAccountRoot, 
          byte[] packetData ) throws LegacyProtocolException {
    /**
     * Detecting SNAC family and subtype
     */
    int snacFamily = DataUtil.get16( packetData, 0 );
    int snacSubtype = DataUtil.get16( packetData, 2 );
    int snacFlags = 0x00;
    byte[] snacRequestId = new byte[ 0 ];
    if ( packetData.length > 4 ) {
      snacFlags = DataUtil.get16( packetData, 4 );
      snacRequestId = DataUtil.getByteArray( packetData, 6, 4 );
    }
    /** Packet dump output **/
    if ( MidletMain.logLevel == 1 ) {
      HexUtil.dump_( System.err, packetData, ">> SNAC (" + HexUtil.toHexString( snacFamily ) + ", " + HexUtil.toHexString( snacSubtype ) + "): " );
    }
    /**
     * Packet parser header method
     */
    switch ( snacFamily ) {
      case BUDDY_LIST_MANAGEMENT_SERVICE: {
        /**
         * Buddy list management service
         */
        BLMService( icqAccountRoot, packetData, snacSubtype, snacFlags, snacRequestId );
        break;
      }
      case ICBM_SERVICE: {
        /**
         * Buddy list management service
         */
        ICBMService( icqAccountRoot, packetData, snacSubtype, snacFlags, snacRequestId );
        break;
      }
      case SSI_SERVICE: {
        /**
         * Server side information service
         */
        SSIServiceSnacParser( icqAccountRoot, packetData, snacSubtype, snacFlags, snacRequestId );
        break;
      }
      case ICQ_SPECIFIC_EXTENSIONS_SERVICE: {
        /**
         * Server side information service
         */
        ICQSpecificExtService( icqAccountRoot, packetData, snacSubtype, snacFlags, snacRequestId );
        break;
      }
      case BUDDY_USER_INFO: {
        ICQBuddyUserInfo( icqAccountRoot, packetData, snacSubtype, snacFlags, snacRequestId );
        break;
      }
      default: {
        /**
         * SNAC from unsupported family
         */
        break;
      }
    }
  }

  public void parserError( int errorCode ) {
  }

  public static void BLMService( IcqAccountRoot icqAccountRoot, byte[] packetData, int snacSubtype, int snacFlags, byte[] snacRequestId ) {
    switch ( snacSubtype ) {
      case 0x000b: {
        /**
         * User online notification
         */
        Capability[] caps = null;
        ClientInfo clientInfo = new ClientInfo();
        int buddyStatus = IcqStatusUtil.getStatus( 1 );
        // SNAC offset
        int point = 10;
        int buddyNameLength = DataUtil.get8int( packetData, point );
        point += 1;
        byte[] buddyName = new byte[ buddyNameLength ];
        System.arraycopy( packetData, point, buddyName, 0, buddyNameLength );
        String buddyId = new String( buddyName );
        point += buddyNameLength;
        int warningLevel = DataUtil.get16( packetData, point );
        point += 2;
        int tlvCount = DataUtil.get16( packetData, point );
        point += 2;
        for ( int c = 0; c < tlvCount; c++ ) {
          int valueID = DataUtil.get16( packetData, point );
          point += 2;
          int length = DataUtil.get16( packetData, point );
          point += 2;
          byte[] value = new byte[ length ];
          System.arraycopy( packetData, point, value, 0, length );
          // LogUtil.outMessage("packetData: " + value);
          point += length;
          switch ( valueID ) {
            case 0x0001: {
              /*
               * TLV.Type(0x01) - user class (nick flags)
               */
              break;
            }
            case 0x0002: {
              /*
               * TLV.Type(0x02) - create time
               */
              // clientInfo.creationTime = DataUtil.get32(value, 0, true);
              // LogUtil.outMessage("Creation time: " + TimeUtil.getDateString(false, clientInfo.creationTime));
              break;
            }
            case 0x0003: {
              /*
               * TLV.Type(0x03) - signon time
               */
              clientInfo.signOnTime = DataUtil.get32( value, 0, true );
              // LogUtil.outMessage( "Signon time: " + TimeUtil.getDateString( false, clientInfo.signOnTime ) );
              break;
            }
            case 0x0004: {
              /*
               * TLV.Type(0x04) - idle time
               */
              clientInfo.idleTime = DataUtil.get16( value, 0 );
              // LogUtil.outMessage( "idle time: " + TimeUtil.getDateString( false, clientInfo.idleTime ) );
              break;
            }
            case 0x0005: {
              /*
               * TLV.Type(0x05) - account creation time
               */
              clientInfo.memberSinceTime = DataUtil.get32( value, 0, true );
              // LogUtil.outMessage( "Member since time: " + TimeUtil.getDateString( false, clientInfo.memberSinceTime ) );
              break;
            }
            case 0x0006: {
              /*
               * TLV.Type(0x06) - user status [ICQ only]
               */
              int firstPart = DataUtil.get16( value, 0 );
              int secondPart = DataUtil.get16( value, 2 );
              LogUtil.outMessage( "Status: " + "\n"
                      + "    first part: " + firstPart + "\n"
                      + "    second part:" + secondPart );
              if ( IcqStatusUtil.expectIsStatus( secondPart ) && secondPart != IcqStatusUtil.getStatus( 0 ) ) {
                buddyStatus = secondPart;
              } else {
                buddyStatus = IcqStatusUtil.getStatus( 1 );
              }
              break;
            }
            case 0x000A: {
              /*
               * TLV.Type(0x0A) - external ip addr [ICQ only]
               */
              clientInfo.externalIp = new byte[]{ ( byte ) DataUtil.get8int( value, 0 ), ( byte ) DataUtil.get8int( value, 1 ), ( byte ) DataUtil.get8int( value, 2 ), ( byte ) DataUtil.get8int( value, 3 ) };
              LogUtil.outMessage( "ip addr ext: " + clientInfo.externalIp[0] + "." + clientInfo.externalIp[1] + "." + clientInfo.externalIp[2] + "." + clientInfo.externalIp[3] );
              break;
            }
            case 0x000C: {
              /*
               * TLV.Type(0x0C) - user DC info [ICQ only]
               */
              clientInfo.internalIp = new byte[]{ 
                ( byte ) DataUtil.get8int( value, 0 ), 
                ( byte ) DataUtil.get8int( value, 1 ), 
                ( byte ) DataUtil.get8int( value, 2 ), 
                ( byte ) DataUtil.get8int( value, 3 ) };

              clientInfo.dcTcpPort = DataUtil.get32( value, 4, true );

              clientInfo.dcType = ( byte ) DataUtil.get8int( value, 8 );

              clientInfo.dcProtocolVersion = DataUtil.get16( value, 9 );

              clientInfo.dcAuthCookie = DataUtil.get32( value, 11, true );
              clientInfo.webFrontPort = DataUtil.get32( value, 15, true );

              clientInfo.clientFeatures = DataUtil.get32( value, 19, true );

              clientInfo.lastInfoUpdateTime = DataUtil.get32( value, 23, true );
              clientInfo.lastExtInfoUpdateTime = DataUtil.get32( value, 27, true );
              clientInfo.lastExtStatusUpdateTime = DataUtil.get32( value, 31, true );

              clientInfo.unk = DataUtil.get8int( value, 35 );

              LogUtil.outMessage( "ip addr int: " + clientInfo.internalIp[0] + "." + clientInfo.internalIp[1] + "." + clientInfo.internalIp[2] + "." + clientInfo.internalIp[3] );
              LogUtil.outMessage( "dc tcp port: " + clientInfo.dcTcpPort );
              LogUtil.outMessage( "dc type: " + clientInfo.dcType );
              LogUtil.outMessage( "dc protocol version: " + clientInfo.dcProtocolVersion );
              LogUtil.outMessage( "dc auth cookie: " + clientInfo.dcAuthCookie );
              LogUtil.outMessage( "web front port: " + clientInfo.webFrontPort );
              LogUtil.outMessage( "client features: " + clientInfo.clientFeatures );
              LogUtil.outMessage( "last info update time: " + clientInfo.lastInfoUpdateTime );
              LogUtil.outMessage( "last status update time: " + clientInfo.lastExtInfoUpdateTime );
              LogUtil.outMessage( "last ext status update time: " + clientInfo.lastExtStatusUpdateTime );
              LogUtil.outMessage( "unknown: " + clientInfo.unk );
              break;
            }
            case 0x000D: {
              /*
               * TLV.Type(0x0D) - client capabilities list
               */
              int capabilitiesCount = value.length / 16;
              LogUtil.outMessage( "caps_count: " + capabilitiesCount );
              caps = new Capability[ capabilitiesCount ];
              for ( int i = 0; i < capabilitiesCount; i++ ) {
                caps[i] = new Capability();
                System.arraycopy( value, i * 16, caps[i].capBytes, 0, 16 );
                if ( MidletMain.logLevel == 1 ) {
                  HexUtil.dump_( caps[i].capBytes, buddyId + ": " );
                }
              }
              break;
            }
            case 0x000F: {
              /*
               * TLV.Type(0x0F) - online time
               */
              clientInfo.onLineTime = System.currentTimeMillis() / 1000 - DataUtil.get32( value, 0, true );
              break;
            }
          }
        }
        ActionExec.setBuddyStatus( icqAccountRoot, buddyId, buddyStatus, caps, clientInfo );
        break;
      }
      case 0x000c: {
        /**
         * Buddy offline packet
         */
        int offset = 10;
        int buddyIdLength = ( DataUtil.get8int( packetData, offset ) );
        offset++;
        String buddyId = DataUtil.byteArray2string( packetData, offset,
                buddyIdLength );

        ActionExec.setBuddyStatus( icqAccountRoot, buddyId, IcqStatusUtil.getStatus( 0 ), null, null );

        break;
      }
    }
  }

  public static void ICBMService( IcqAccountRoot icqAccountRoot, 
          byte[] packetData, int snacSubtype, int snacFlags, 
          byte[] snacRequestId ) throws LegacyProtocolException {
    switch ( snacSubtype ) {
      case 0x000b: {
        /**
         * Message for client from server
         */
        int offset = 10;
        /** Offset from SNAC header */
        /** Message packet parsing... */
        byte[] msgCookie = ArrayUtil.copyOfRange( packetData, offset, offset + 8 );
        offset += 8;
        int messageChannel = DataUtil.get16( packetData, offset );
        offset += 2;
        byte screenNameLength = ( byte ) DataUtil.get8int( packetData, offset );
        offset++;
        String screenName = new String( ArrayUtil.copyOfRange( packetData, offset, offset + screenNameLength ) );
        offset += screenNameLength;
        int reason = DataUtil.get16( packetData, offset );
        offset += 2;
        switch ( reason ) {
          case 0x0003: {
            LogUtil.outMessage( "Reason 0x0003" );
            if ( messageChannel == 1 ) {
              ActionExec.msgAck( icqAccountRoot, screenName, null, msgCookie );
            } else {
              int length = DataUtil.get16_reversed( packetData, offset );
              offset += 2;
              int protocolVersion = DataUtil.get16_reversed( packetData, offset );
              offset += 2;
              byte[] plugin = DataUtil.getByteArray( packetData, offset, 16 );
              offset += 16;
              LogUtil.outMessage( "plugin: " + HexUtil.bytesToString( plugin ) );
              if ( ArrayUtil.equals( plugin,
                      new byte[]{
                        0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00 } ) ) {
                LogUtil.outMessage( "Plugin correct" );
                int unk1 = DataUtil.get16_reversed( packetData, offset );
                offset += 2;
                long capabilities = DataUtil.get32_reversed( packetData, offset, true );
                LogUtil.outMessage( "capabilities: " + capabilities );
                offset += 4;
                byte unk2 = ( byte ) DataUtil.get8int( packetData, offset );
                offset++;
                int downcounter = DataUtil.get16_reversed( packetData, offset );
                offset += 2;
                LogUtil.outMessage( "downcounter: " + downcounter );
                int chunkLength = DataUtil.get16_reversed( packetData, offset );
                offset += 2;
                LogUtil.outMessage( "chunkLength: " + chunkLength );
                int unk3 = DataUtil.get16_reversed( packetData, offset );
                offset += 2;
                // 0x0c bytes unknown
                offset += 0x0c;
                int msgType = DataUtil.get8int( packetData, offset );
                offset++;
                LogUtil.outMessage( "msgType: " + msgType );
                if ( ( msgType & 0x00ff ) == 0xe8 ) { // Message Type: Auto away message (0xe8)
                  byte msgFlags = ( byte ) DataUtil.get8int( packetData, offset );
                  offset++;
                  int statusCode = DataUtil.get16_reversed( packetData, offset );
                  offset += 2;
                  int priorityCode = DataUtil.get16_reversed( packetData, offset );
                  offset += 2;
                  int textLength = DataUtil.get16_reversed( packetData, offset );
                  offset += 2;
                  byte[] msgText = DataUtil.getByteArray( packetData, offset, textLength );
                  ActionExec.setStatusMessage( icqAccountRoot, msgCookie, msgText );
                } else {
                  if ( msgType == 0x1a ) {
                    // Here comes X-Text
                    // This must be extremely rewritten
                    LogUtil.outMessage( "msgType == 0x1a" );
                    LogUtil.outMessage( "response=" + StringUtil.byteArrayToString( packetData ) );
                    try {
                      ArrayUtil response = new ArrayUtil( packetData );
                      LogUtil.outMessage( "Parsing..." );
                      ArrayUtil xTitle = new ArrayUtil( response.subarray( response.indexOf( "title&gt;".getBytes() ) + "title&gt;".length(), response.indexOf( "/title&gt;".getBytes() ) ) );
                      xTitle.byteString = xTitle.subarray( 0, xTitle.indexOf( "&lt;".getBytes() ) );
                      LogUtil.outMessage( "xTitle = " + xTitle.getString() );
                      ArrayUtil xText = new ArrayUtil( response.subarray( response.indexOf( "desc&gt;".getBytes() ) + "desc&gt;".length(), response.indexOf( "/desc&gt;".getBytes() ) ) );
                      xText.byteString = xText.subarray( 0, xText.indexOf( "&lt;".getBytes() ) );
                      LogUtil.outMessage( "xText = " + xText.getString() );
                      LogUtil.outMessage( "XTRAZ_RESPONSE: " + xTitle + ": " + xText );
                      ActionExec.setXStatusMessage( icqAccountRoot, msgCookie, xTitle.byteString, xText.byteString );
                    } catch ( Throwable ex1 ) {
                      LogUtil.outMessage( "Error in parsing XStatus: " + ex1.getMessage() );
                      // Do nothing
                    }
                  }
                }
              }
            }
            break;
          }
        }
        break;
      }
      case 0x0007: {
        /**
         * Message for client from server
         */
        int offset = 10;
        /** Offset from SNAC header */
        /** Message packet parsing... */
        byte[] msgCookie = ArrayUtil.copyOfRange( packetData, offset, offset + 8 );
        offset += 8;
        int messageChannel = DataUtil.get16( packetData, offset );
        offset += 2;
        byte screenNameLength = ( byte ) DataUtil.get8int( packetData, offset );
        offset++;
        String screenName = new String( ArrayUtil.copyOfRange( packetData, offset, offset + screenNameLength ) );
        offset += screenNameLength;
        int senderWarningLevel = DataUtil.get16( packetData, offset );
        offset += 2;
        int numberOfTlvs = DataUtil.get16( packetData, offset );
        offset += 2;
        LogUtil.outMessage( "Mess from: " + screenName );
        LogUtil.outMessage( "There are " + numberOfTlvs + " TLV's" );
        LogUtil.outMessage( "Message channel: " + messageChannel );
        /**
         * May be setted variables
         */
        int buddyStatus;
        boolean isAutomatedMess = false;

        int messCharsetNumber = -1;
        int messCharsetSubset = -1;
        byte[] asciiString = new byte[ 0 ];
        byte messageType = MTYPE_PLAIN;
        byte messageFlags = MFLAG_NORMAL;
        int ch2msgType = -1;
        byte[] guid = new byte[ 16 ];
        String guidString = null;
        int[] proxyIp = new int[ 4 ];
        int dcTcpPort = -1;
        boolean isViaRendezvousServer = false;
        long fileLength = -1;
        byte[] fileName = null;

        for ( int c = 0; c < numberOfTlvs; c++ ) {
          int type = DataUtil.get16( packetData, offset );
          offset += 2;
          int length = DataUtil.get16( packetData, offset );
          offset += 2;
          byte[] value = ArrayUtil.copyOfRange( packetData, offset, offset + length );
          offset += length;
          if ( MidletMain.logLevel == 1 ) {
            HexUtil.dump_( value, "TLV.Type(" + HexUtil.toHexString( type ) + ") " );
          }
          /** Now, working only with type and value */
          switch ( type ) {
            case 0x0001: {
              /** User class */
              LogUtil.outMessage( "Buddy class: ignored" );
              break;
            }
            case 0x0006: {
              /** User status */
              int firstPart = DataUtil.get16( value, 0 );
              int secondPart = DataUtil.get16( value, 2 );
              LogUtil.outMessage( "Status: " + "\n"
                      + "    first part: " + firstPart + "\n"
                      + "    second part:" + secondPart );
              if ( IcqStatusUtil.expectIsStatus( secondPart ) ) {
                buddyStatus = secondPart;
              } else {
                buddyStatus = IcqStatusUtil.getStatus( 1 );
              }
              LogUtil.outMessage( "Buddy status: " + buddyStatus );

              break;
            }
            case 0x000f: {
              /** Online time */
              LogUtil.outMessage( "Online time: ignored" );
              break;
            }
            case 0x0003: {
              /** Account creation time */
              LogUtil.outMessage( "Account creation time: ignored" );
              break;
            }
            case 0x0004: {
              /** Automated response flag, like away messages */
              /** Body is empty */
              isAutomatedMess = true;
              LogUtil.outMessage( "This is automated message" );
              break;
            }
          }
        }
        while ( offset < packetData.length ) {
          int type = DataUtil.get16( packetData, offset );
          offset += 2;
          int length = DataUtil.get16( packetData, offset );
          offset += 2;
          byte[] value = ArrayUtil.copyOfRange( packetData, offset, offset + length );
          offset += length;
          // LogUtil.outMessage("TLV.Type("+HexUtil.toHexString(type)+")");
          if ( MidletMain.logLevel == 1 ) {
            HexUtil.dump_( value, "TLV.Type(" + HexUtil.toHexString( type ) + ") " );
          }
          /** Now, working only with type and value */
          switch ( type ) {
            case 0x0002: {
              /** Message data in channel 1 */
              LogUtil.outMessage( "Channel 1 message data" );
              if ( messageChannel == 1 ) {
                int point = 0;
                while ( value.length > point ) {
                  byte fragId = ( byte ) DataUtil.get8int( value, point );
                  point++;
                  byte fragVer = ( byte ) DataUtil.get8int( value, point );
                  point++;
                  int tlvLength = DataUtil.get16( value, point );
                  point += 2;
                  byte[] fragData = ArrayUtil.copyOfRange( value, point, point + tlvLength );
                  point += tlvLength;
                  /** Using fragData */
                  if ( fragVer > 0x01 ) {
                    throw new LegacyProtocolException();
                  }
                  switch ( fragId ) {
                    case 0x05: {
                      /** byte array of required capabilities */
                      break;
                    }
                    case 0x01: {
                      messCharsetNumber = DataUtil.get16( fragData, 0 );
                      messCharsetSubset = DataUtil.get16( fragData, 2 );
                      asciiString = DataUtil.getByteArray( fragData, 4, tlvLength - 4 );
                      break;
                    }

                  }
                }

              }
              break;
            }
            case 0x0005: {
              /** Rendezvous message data */
              /** Channel 2 or 4 (old-style) */
              if ( messageChannel == 2 ) {
                if ( MidletMain.logLevel == 1 ) {
                  HexUtil.dump_( value, "TLV(0x0005): " );
                }
                ch2msgType = DataUtil.get16( value, 0 );
                LogUtil.outMessage( "Channel 2 message type: " + ch2msgType );
                msgCookie = DataUtil.getByteArray( value, 2, 8 );
                if ( MidletMain.logLevel == 1 ) {
                  HexUtil.dump_( msgCookie, "cookie: " );
                }
                guid = DataUtil.getByteArray( value, 10, 16 );
                /** Inside TLV's */
                int point = 26;
                if ( MidletMain.logLevel == 1 ) {
                  HexUtil.dump_( guid, "guid: " );
                }
                while ( value.length > point ) {
                  int insType = DataUtil.get16( value, point );
                  point += 2;
                  int insLength = DataUtil.get16( value, point );
                  point += 2;
                  byte[] insValue = ArrayUtil.copyOfRange( value, point, 
                          point + insLength );
                  point += insLength;
                  switch ( insType ) {
                    case 0x0002: {
                      /** Proxy ip */
                      proxyIp = new int[]{ DataUtil.get8int( insValue, 0 ), 
                        DataUtil.get8int( insValue, 1 ), 
                        DataUtil.get8int( insValue, 2 ), 
                        DataUtil.get8int( insValue, 3 ) };
                      LogUtil.outMessage( "ip addr proxy: " + proxyIp[0] + "." 
                              + proxyIp[1] + "." + proxyIp[2] + "." + proxyIp[3] );
                      break;
                    }
                    case 0x0004: {
                      /** External ip */
                      break;
                    }
                    case 0x0005: {
                      /** Listening port */
                      dcTcpPort = DataUtil.get16( insValue, 0 );
                      break;
                    }
                    case 0x0010: {
                      /** Request Data via Rendezvous Server */
                      isViaRendezvousServer = true;
                      break;
                    }
                    case 0x000a: {
                      /** Unknown */
                      break;
                    }
                    case 0x000b: {
                      /** Unknown */
                      break;
                    }
                    case 0x000f: {
                      /** Unknown */
                      break;
                    }
                    case 0x2711: {
                      /** Extention data */
                      if ( ArrayUtil.equals( guid, new byte[]{
                                ( byte ) 0x09, ( byte ) 0x46, ( byte ) 0x13, ( byte ) 0x43, //Send.f
                                ( byte ) 0x4C, ( byte ) 0x7F, ( byte ) 0x11, ( byte ) 0xD1,
                                ( byte ) 0x82, ( byte ) 0x22, ( byte ) 0x44, ( byte ) 0x45,
                                ( byte ) 0x53, ( byte ) 0x54, ( byte ) 0x00, ( byte ) 0x00 } ) ) {
                        int fdPoint = 0;
                        DataUtil.get16( insValue, fdPoint ); // 0x0001
                        fdPoint += 2;
                        DataUtil.get16( insValue, fdPoint ); // 0x0001
                        fdPoint += 2;
                        fileLength = DataUtil.get32( insValue, fdPoint, true );
                        fdPoint += 4;
                        fileName = DataUtil.getByteArray( insValue, fdPoint, insValue.length - 8 - 1 );
                      } else {
                        if ( MidletMain.logLevel == 1 ) {
                          HexUtil.dump_( value, "ch2: " );
                        }
                        int fdPoint = 0;
                        int follDataLength = DataUtil.get16_reversed( insValue, fdPoint );
                        fdPoint += 2;
                        int protVersion = DataUtil.get16_reversed( insValue, fdPoint );
                        fdPoint += 2;
                        byte[] plugin = DataUtil.getByteArray( insValue, fdPoint, 16 );
                        fdPoint += 16;
                        int unkWord = DataUtil.get16( insValue, fdPoint );
                        fdPoint += 2;
                        long cliCapFlag = DataUtil.get16_reversed( insValue, fdPoint );
                        fdPoint += 4;
                        byte unkByte = ( byte ) DataUtil.get8int( insValue, fdPoint );
                        fdPoint++;
                        int downCounter = DataUtil.get16_reversed( insValue, fdPoint );
                        fdPoint += 2;
                        /** Following */
                        int nFollDataLength = DataUtil.get16_reversed( insValue, fdPoint );
                        fdPoint += 2;
                        int secDCounter = DataUtil.get16_reversed( insValue, fdPoint );
                        fdPoint += 2;
                        DataUtil.getByteArray( insValue, fdPoint, nFollDataLength - 2 );
                        fdPoint += nFollDataLength - 2;
                        LogUtil.outMessage( "prot. version: " + protVersion );
                        if ( MidletMain.logLevel == 1 ) {
                          HexUtil.dump_( plugin, "plugin: " );
                        }
                        LogUtil.outMessage( "Other data length: " + ( insLength - follDataLength - nFollDataLength ) );
                        if ( ArrayUtil.equals( plugin, new byte[]{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } ) ) {
                          /**
                           * Message here
                           */
                          messageType = ( byte ) DataUtil.get8int( insValue, fdPoint );
                          fdPoint++;
                          messageFlags = ( byte ) DataUtil.get8int( insValue, fdPoint );
                          fdPoint++;
                          int statusCode = DataUtil.get16_reversed( insValue, fdPoint );
                          fdPoint += 2;
                          int priorityCode = DataUtil.get16_reversed( insValue, fdPoint );
                          fdPoint += 2;
                          int messageStringLength = DataUtil.get16_reversed( insValue, fdPoint );
                          fdPoint += 2;
                          asciiString = DataUtil.getByteArray( insValue, fdPoint, messageStringLength );
                          fdPoint += messageStringLength;
                          /**
                           * This may be, but not now...
                           if (messageType == 0x01) {
                           // If message type is MTYPE_PLAIN
                           long textColor = DataUtil.get16_reversed(insValue, fdPoint);
                           fdPoint += 4;
                           long backgroundColor = DataUtil.get16_reversed(insValue, fdPoint);
                           fdPoint += 4;
                           long guidStringLength = DataUtil.get16_reversed(insValue, fdPoint);
                           fdPoint += 4;
                           guidString = DataUtil.byteArray2string(insValue, fdPoint, (int) guidStringLength);
                           LogUtil.outMessage(guidString);
                           }
                           */
                        }
                      }
                    }

                  }
                }

              } else {
                if ( messageChannel == 4 ) {
                  screenName = String.valueOf( DataUtil.get32_reversed( value, 0, true ) );
                  messageType = ( byte ) DataUtil.get8int( value, 4 );
                  messageFlags = ( byte ) DataUtil.get8int( value, 5 );
                  int messStringLength = DataUtil.get16_reversed( value, 6 );
                  asciiString = DataUtil.getByteArray( value, 8, messStringLength );
                }
              }
              break;
            }
          }
        }

        LogUtil.outMessage( "------- Data for all channels -------" );
        LogUtil.outMessage( "Sender screenname: " + screenName );
        LogUtil.outMessage( "Message charset number: " + messCharsetNumber );
        LogUtil.outMessage( "Message charset subset: " + messCharsetSubset );
        LogUtil.outMessage( "Message text string: " + new String( asciiString ) );
        LogUtil.outMessage( "--------- From Channel 2, 4 ---------" );
        LogUtil.outMessage( "Message type: " + messageType );
        LogUtil.outMessage( "Message flags: " + messageFlags );
        LogUtil.outMessage( "--------- DC ------------------------" );
        LogUtil.outMessage( "External ip: "
                + proxyIp[0] + "." + proxyIp[1] + "."
                + proxyIp[2] + "." + proxyIp[3] );
        LogUtil.outMessage( "Listening port: "
                + dcTcpPort );

        String stringCookie = new String();
        for ( int c = 0; c < 8; c++ ) {
          stringCookie += HexUtil.toHexString( ( byte ) msgCookie[c] ) + ", ";
        }
        LogUtil.outMessage( "Cookie: " + stringCookie );

        if ( ArrayUtil.equals( guid, new byte[]{
                  ( byte ) 0x09, ( byte ) 0x46, ( byte ) 0x13, ( byte ) 0x43, //Send.f
                  ( byte ) 0x4C, ( byte ) 0x7F, ( byte ) 0x11, ( byte ) 0xD1,
                  ( byte ) 0x82, ( byte ) 0x22, ( byte ) 0x44, ( byte ) 0x45,
                  ( byte ) 0x53, ( byte ) 0x54, ( byte ) 0x00, ( byte ) 0x00 } ) ) {
          /**
           * File transaction
           */
          LogUtil.outMessage( "Type: " + HexUtil.toHexString0x( ch2msgType ) );
          //if (proxyIp[0] != 0 && dcTcpPort != -1) {
          // Connecting to another proxy
          ActionExec.performTransferAction( icqAccountRoot, ch2msgType, screenName, proxyIp, dcTcpPort, isViaRendezvousServer, fileLength, fileName, msgCookie, false );
          //} else {
          // Connect to myself proxy
          //    ActionExec.sendToCreatedProxy(ch2msgType, screenName, fileLength, fileName, msgCookie);
          //}
          break;
        }
        /**
         * Decoding asciiMessage in according width:
         * messCharsetNumber && messCharsetSubset
         */
        if ( MidletMain.logLevel == 1 ) {
          HexUtil.dump_( asciiString, "ascii: " );
        }
        String decodedString = null;
        if ( asciiString != null ) {
          try {
            if ( messCharsetNumber == 0x0002 ) {
              decodedString = StringUtil.removeCr( StringUtil.ucs2beByteArrayToString( asciiString ) );
            } else {
              decodedString = StringUtil.removeCr( StringUtil.byteArrayToString( asciiString, true ) );
            }
          } catch ( Throwable ex1 ) {
          }
        }

        LogUtil.outMessage( "Detecting type of: " + messageType );

        switch ( messageType ) {
          case MTYPE_PLAIN: {
            LogUtil.outMessage( "Plain message" );
            ActionExec.recMess( icqAccountRoot, screenName, null, null, decodedString, msgCookie, ChatItem.TYPE_PLAIN_MSG );
            try {
              /**
               * Sending ack
               */
              IcqPacketSender.sendMsgAck( icqAccountRoot.session, messageChannel, screenName, msgCookie );
            } catch ( IOException ex ) {
              // ex.printStackTrace();
            }
            break;
          }
          case MTYPE_CHAT: {
            break;
          }
          case MTYPE_FILEREQ: {
            break;
          }
          case MTYPE_URL: {
            ActionExec.recMess( icqAccountRoot, screenName, null, null, decodedString, msgCookie, ChatItem.TYPE_HYPERLINK_MSG );
            break;
          }
          case MTYPE_AUTHREQ: {
            ActionExec.recMess( icqAccountRoot, screenName, null, null, decodedString, msgCookie, ChatItem.TYPE_AUTH_REQ_MSG );
            break;
          }
          case MTYPE_AUTHDENY: {
            ActionExec.recMess( icqAccountRoot, screenName, null, null, decodedString, msgCookie, ChatItem.TYPE_AUTH_DENY_MSG );
            break;
          }
          case MTYPE_AUTHOK: {
            ActionExec.recMess( icqAccountRoot, screenName, null, null, decodedString, msgCookie, ChatItem.TYPE_AUTH_OK_MSG );
            break;
          }
          case MTYPE_SERVER: {
            break;
          }
          case MTYPE_ADDED: {
            break;
          }
          case MTYPE_WWP: {
            break;
          }
          case MTYPE_EEXPRESS: {
            break;
          }
          case MTYPE_CONTACTS: {
            break;
          }
          case MTYPE_PLUGIN: {
            // Here comes requests
            // Rewrite
            ActionExec.sendXStatusMessage( icqAccountRoot, msgCookie, screenName );
            break;
          }
          case MTYPE_AUTOAWAY: {
          }
          case MTYPE_AUTOBUSY: {
          }
          case MTYPE_AUTONA: {
          }
          case MTYPE_AUTODND: {
          }
          case MTYPE_AUTOFFC: {
            ActionExec.sendStatusMessage( icqAccountRoot, msgCookie, screenName );
            break;
          }
        }

        break;
      }
      case 0x000c: {
        /**
         * Server message ack
         */
        int offset = 10;
        /** SNAC header offset */
        byte[] msgCookie = DataUtil.getByteArray( packetData, offset, 8 );
        offset += 8;
        int msgChannel = DataUtil.get16( packetData, offset );
        offset += 2;
        byte userUinStringLength = ( byte ) DataUtil.get8int( packetData, offset );
        offset++;
        String uinString = DataUtil.byteArray2string( packetData, offset, userUinStringLength );

        LogUtil.outMessage( "Message delivired ", true );
        LogUtil.outMessage( "Message channel: " + msgChannel );
        LogUtil.outMessage( "Message from: " + uinString );
        String stringCookie = new String();
        for ( int c = 0; c < 8; c++ ) {
          stringCookie += HexUtil.toHexString( ( byte ) msgCookie[c] ) + ", ";
        }
        LogUtil.outMessage( stringCookie );
        /**
         * Sending to UI
         */
        ActionExec.msgAck( icqAccountRoot, uinString, null, msgCookie );
        break;
      }
      case 0x0014: {
        /**
         * Server message ack
         */
        int offset = 10;
        /** SNAC header offset */
        offset += 8; // cookie offset
        offset += 2; // Notification channel
        byte buddyNameLength = ( byte ) DataUtil.get8int( packetData, offset );
        offset += 1;
        String buddyName = new String( DataUtil.getByteArray( packetData, offset, buddyNameLength ) );
        offset += buddyNameLength;
        int notificationType = DataUtil.get16( packetData, offset );
        LogUtil.outMessage( "buddyName: " + buddyName );
        LogUtil.outMessage( "notificationType: " + notificationType );
        ActionExec.setBuddyTypingStatus( icqAccountRoot, buddyName, null, ( notificationType == 0x0000 ? false : true ), false );
        break;
      }
    }
  }

  private static void SSIServiceSnacParser( IcqAccountRoot icqAccountRoot, 
          byte[] packetData, int snacSubtype, int snacFlags, 
          byte[] snacRequestId ) throws LegacyProtocolException {
    try {
      switch ( snacSubtype ) {
        case 0x0006: {
          /** Contact list reply **/
          int buddyCount = 0;
          int privacySettings;
          int privateBuddyId = -1;
          /** Buddy data store **/
          Vector buddyList = new Vector();
          Vector privateList = new Vector();

          IcqGroup groupItem;
          IcqItem buddyItem;
          int offset = 10;

          /** SSI Version **/
          int ssiVersion = ( DataUtil.get8int( packetData, offset ) );
          if ( ssiVersion > 1 ) {
            throw new LegacyProtocolException();
          }
          offset++;

          if ( ssiVersion == 0x00 ) {
            /** SSI objects count **/
            int ssiObjectCount = ( DataUtil.get16( packetData, offset ) );
            LogUtil.outMessage( "SSI objects count: " + ssiObjectCount );
            offset += 2;
            byte[] buddyName;
            int buddyNameLength, buddyGroupID, buddyID, buddyType, ssiTLVLen, pointBefore;
            for ( int c = 0; c < ssiObjectCount; c++ ) {
              groupItem = null;
              /** Reading roster item **/
              buddyNameLength = DataUtil.get16( packetData, offset );
              offset += 2;
              buddyName = new byte[ buddyNameLength ];
              System.arraycopy( packetData, offset, buddyName, 0, buddyNameLength );
              offset += buddyNameLength;
              buddyGroupID = DataUtil.get16( packetData, offset );
              offset += 2;
              buddyID = DataUtil.get16( packetData, offset );
              offset += 2;
              buddyType = DataUtil.get16( packetData, offset );
              offset += 2;
              ssiTLVLen = DataUtil.get16( packetData, offset );
              offset += 2;

              boolean toAddFlag = false;
              boolean toPrivateFlag = false;

              buddyItem = new IcqItem( "" );
              switch ( buddyType ) {
                case UNK1_SYSTEM_TYPE: {
                  break;
                }
                case UNK2_SYSTEM_TYPE: {
                  break;
                }
                case PERMIT_LIST_RECORD: {
                  buddyItem.buddyType = IcqItem.PERMIT_LIST_BUDDY;
                  toPrivateFlag =
                          true;
                  break;
                }
                case DENY_LIST_RECORD: {
                  buddyItem.buddyType = IcqItem.DENY_LIST_BUDDY;
                  toPrivateFlag =
                          true;
                  break;

                }
                case IGNORE_LIST_RECORD: {
                  buddyItem.buddyType = IcqItem.IGNORE_LIST_BUDDY;
                  toPrivateFlag =
                          true;
                  break;
                }
                case PRESENCE_INFO: {
                  break;
                }
                case OWN_ICON_AVATAR: {
                  break;
                }
                case LAST_UPDATE_DATE: {
                  break;
                }
                case PERMIT_DENY_SETTINGS: {
                  break;
                }
                case OWN_GROUP_TYPE: {
                  break;
                }
                case NORMAL_GROUP: {
                  groupItem = new IcqGroup( "" );
                  buddyItem = null;
                  groupItem.buddyType = IcqItem.NORMAL_GROUP;
                  toAddFlag = true;
                  break;
                }
                case SYSTEM_GROUP: {
                  groupItem = new IcqGroup( "" );
                  buddyItem = null;
                  groupItem.buddyType = IcqItem.SYSTEM_GROUP;
                  toAddFlag = isReceivePhantoms;
                  break;
                }
                case NORMAL_UIN: {
                  buddyItem.buddyType = IcqItem.NORMAL_BUDDY;
                  toAddFlag = true;
                  break;
                }
                case DELETED_UIN: {
                  buddyItem.buddyType = IcqItem.PHANTOM_BUDDY;
                  toAddFlag = isReceivePhantoms;
                  break;
                }
                default: {
                  /*
                   * Неизвестный тип
                   */
                  LogUtil.outMessage( "unk_type: " + new String( buddyName ) );
                  buddyItem.buddyType = IcqItem.NORMAL_BUDDY;
                  toAddFlag = false;
                }
              }

              if ( buddyItem != null ) {
                /*
                 * Занесение данных в экземпляр элемента
                 * списка контактов
                 */
                buddyItem.userId = StringUtil.byteArrayToString( buddyName, true );
                buddyItem.groupId = buddyGroupID;
                buddyItem.buddyId = buddyID;

                // Если UIN не имеет ника
                buddyItem.userNick = buddyItem.userId;

                LogUtil.outMessage( " - " + buddyItem.userId );
              } else {
                if ( groupItem != null ) {
                  groupItem.userId = StringUtil.byteArrayToString( buddyName, true );
                  groupItem.groupId = buddyGroupID;
                  groupItem.buddyId = buddyID;
                  // LogUtil.outMessage(groupItem.userId);
                }
              }
              /** Reading TLV **/
              pointBefore = offset;
              while ( offset < pointBefore + ssiTLVLen ) {
                int valueID = DataUtil.get16( packetData, offset );
                offset += 2;
                int length = DataUtil.get16( packetData, offset );
                offset += 2;
                byte[] value = new byte[ length ];
                System.arraycopy( packetData, offset, value, 0, length );
                offset += length;
                switch ( valueID ) {
                  case TLV_AIM_PR_SET: {
                    LogUtil.outMessage( "This is the byte that "
                            + "tells the AIM servers your privacy "
                            + "setting. If 1, then allow all users "
                            + "to see you. If 2, then block all users "
                            + "from seeing you. If 3, then allow "
                            + "only the users in the permit list. "
                            + "If 4, then block only the users in "
                            + "the deny list. If 5, then allow only "
                            + "users on your buddy list." );
                    LogUtil.outMessage( "buddyItem ? =null : " + ( buddyItem == null ) );
                    privacySettings = value[0];
                    privateBuddyId = buddyItem.buddyId;
                    LogUtil.outMessage( "private buddy id: " + privateBuddyId );
                    break;
                  }


                  case TLV_ALL_OTH_SEE: {
//                                    LogUtil.outMessage("Bitmask of flags containing " +
//                                            "\"Allow others to see...\" options. ");
                    break;
                  }

                  case TLV_AV_AUTH: {
//                                    LogUtil.outMessage("Signifies that you are " +
//                                            "awaiting authorization for this buddy. ");
                    buddyItem.isAvaitingAuth = true;
                    break;
                  }
                  case TLV_BUDDY_COMMENT: {
//                                    LogUtil.outMessage("This stores the \"buddy " +
//                                            "comment\" field. ");
                    break;
                  }

                  case TLV_BUDDY_ICON: {
//                                    LogUtil.outMessage("TLV for buddy icon info " +
//                                            "(type 0x0014). ");
                    break;
                  }

                  case TLV_GR_IDS: {
//                                    LogUtil.outMessage("If group is the master group, this contains the group ID#s of all groups in the list. ");
                    break;
                  }

                  case TLV_IMPORT_TIME: {
//                                    LogUtil.outMessage("TLV for import time item " +
//                                            "(type 0x0013). ");
                    break;
                  }

                  case TLV_MAIL_ADDR: {
//                                    LogUtil.outMessage("Your buddy locally assigned " +
//                                            "mail address.");
                    break;
                  }

                  case TLV_NICK_NAME: {
//                                    LogUtil.outMessage("This stores the name that " +
//                                            "the contact should show up as in the " +
//                                            "contact list. ");
                    if ( buddyItem != null ) {
                      buddyItem.userNick = StringUtil.byteArrayToString( value, true );
                    } else {
                      if ( groupItem != null ) {
                        groupItem.userId = StringUtil.byteArrayToString( value, true );
                      }
                    }
                    break;

                  }


                  case TLV_PERS_ALERTS: {
//                                    LogUtil.outMessage("Personal alerts for this " +
//                                            "buddy.");
                    break;
                  }

                  case TLV_SHORTCUT: {
//                                    LogUtil.outMessage("This item type (9) looks " +
//                                            "like ICQ2k shortcut list. ");
                    break;
                  }

                  case TLV_SMS_NUMBER: {
//                                    LogUtil.outMessage("Your buddy locally assigned SMS number.");
                    break;
                  }

                  case TLV_UNK: {
//                                    LogUtil.outMessage("Unknown ");
                    break;
                  }

                  case TLV_VIS_CLASS: {
//                                    LogUtil.outMessage("This is a bit mask which " +
//                                            "tells the AIM servers which class of " +
//                                            "users you want to be visible to. ");
                    break;
                  }

                  case TLV_SOUND_BUDDY: {
//                                    LogUtil.outMessage("Sound client should play " +
//                                            "as alert for this buddy. ");
                    break;
                  }

                  case TLV_FIRST_MESS: {
//                                    LogUtil.outMessage("Date/time (unix time() format) " +
//                                            "when you send message to this you first time. ");
                    break;
                  }

                }
              }

              if ( groupItem != null && groupItem.buddyId == 0 && groupItem.groupId == 0 ) {
                /** Root group **/
                groupItem.buddyType = IcqItem.ROOT_GROUP;
                continue;

              }

              if ( toPrivateFlag ) {
                buddyItem.updateUiData();
                privateList.addElement( buddyItem );
              } else {
                if ( toAddFlag ) {
                  if ( groupItem != null ) {
                    /*if ( groupItem.getChildsCount() == 0 ) {
                     groupItem.getChilds();
                     }*/
                    groupItem.updateUiData();
                    LogUtil.outMessage( "[ ".concat( groupItem.userId ).concat( " ]" ) );
                    buddyList.addElement( groupItem );
                  } else {
                    if ( buddyItem != null ) {
                      buddyItem.updateUiData();
                      LogUtil.outMessage( buddyItem.userId.concat( " = " ).concat( buddyItem.userNick ) );
                      if ( buddyList.isEmpty() ) {
                        groupItem = new IcqGroup( "" );
                        groupItem.buddyType = -1;
                        buddyList.addElement( groupItem );
                      }
                      buddyCount++;
                      ( ( IcqGroup ) buddyList.lastElement() ).addChild( buddyItem );
                    }
                  }
                }
              }
            }
          }
          /** Executing UI output **/
          LogUtil.outMessage( "Final private buddy id: " + privateBuddyId );
          if ( !buddyList.isEmpty() ) {
            LogUtil.outMessage( "snacFlags = " + snacFlags );
            ActionExec.setBuddyList( icqAccountRoot, buddyList, privateList, privateBuddyId, snacFlags, snacRequestId );
          }
          LogUtil.outMessage( "Buddy added count: " + buddyCount );
          break;
        }
        case 0x000e: {
          /** Executing UI output **/
          int offset = packetData.length - 2; // 10;

          /** Operation result **/
          int resultCode = DataUtil.get16( packetData, offset );
          /*
           0x0000	  No errors (success)
           0x0002	  Item you want to modify not found in list
           0x0003	  Item you want to add allready exists
           0x000A	  Error adding item (invalid id, allready in list, invalid data)
           0x000C	  Can't add item. Limit for this type of items exceeded
           0x000D	  Trying to add ICQ contact to an AIM list
           0x000E	  Can't add this contact because it requires authorization
           */
          // ActionExec.ssiComplete(icqAccountRoot, resultCode);
          long requestIdLong = DataUtil.get32( snacRequestId, 0, true );
          Cookie cookie = new Cookie( requestIdLong );
          LogUtil.outMessage( "cookieString = " + cookie.cookieString );
          LogUtil.outMessage( "resultCode = " + resultCode );
          String errorString = null;
          switch ( resultCode ) {
            case IcqPacketParser.SSI_NO_ERRORS: {
              ActionExec.processQueueAction( icqAccountRoot, cookie, null );
              break;
            }
            case IcqPacketParser.SSI_NOT_FOUND: {
              errorString = "SSI_NOT_FOUND";
              break;
            }
            case IcqPacketParser.SSI_ALR_EXIST: {
              errorString = "SSI_ALR_EXIST";
              break;
            }
            case IcqPacketParser.SSI_ADD_ERROR: {
              errorString = "SSI_ADD_ERROR";
              break;
            }
            case IcqPacketParser.SSI_LIMIT_EXC: {
              errorString = "SSI_LIMIT_EXC";
              break;
            }
            case IcqPacketParser.SSI_TICQTOAIM: {
              errorString = "SSI_TICQTOAIM";
              break;
            }
            case IcqPacketParser.SSI_AUTH_REQD: {
              errorString = "SSI_AUTH_REQD";
              break;
            }
            default: {
              errorString = "ERROR";
              break;
            }
          }
          if ( errorString != null ) {
            ActionExec.cancelQueueAction( icqAccountRoot, cookie, errorString );
          }
          break;
        }
        case 0x0019: {
          int offset = 18;
          int userIdLength = DataUtil.get8( packetData, offset );
          offset += 1;
          String userId = StringUtil.byteArrayToString( packetData, offset, userIdLength, true );
          offset += userIdLength;
          LogUtil.outMessage( "userId: " + userId );
          int reasonLength = DataUtil.get16( packetData, offset );
          offset += 2;
          String reason = StringUtil.byteArrayToString( packetData, offset, reasonLength, true );
          LogUtil.outMessage( "reason: " + reason );
          offset += reasonLength;
          ActionExec.recMess( icqAccountRoot, userId, null, null, reason, snacRequestId, ChatItem.TYPE_AUTH_REQ_MSG );
          break;
        }
      }
    } catch ( Throwable ex1 ) {
      LogUtil.outMessage( ex1 );
    }
  }

  public static void ICQSpecificExtService( IcqAccountRoot icqAccountRoot, byte[] packetData, int snacSubtype, int snacFlags, byte[] snacRequestId ) {
    switch ( snacSubtype ) {
      case 0x0001: {
        /** Error response **/
        int point = 10; // Ten bytes are Snac header and Snac Id
        int errorCode = DataUtil.get16( packetData, point );
        /** In later versions user must be notifyed about error **/
        LogUtil.outMessage( "Ext Service Error: " + errorCode, true );
        break;
      }
    }
  }

  public static void ICQBuddyUserInfo( IcqAccountRoot icqAccountRoot,
          byte[] packetData, int snacSubtype, int snacFlags,
          byte[] snacRequestId ) {
    if ( MidletMain.logLevel == 1 ) {
      HexUtil.dump_( packetData, "buddy info: " );
    }
    BuddyInfo buddyInfo = new BuddyInfo();
    int offset = 10;
    int result = ( int ) DataUtil.get32( packetData, offset, true );
    offset += 4;
    if ( result == 0 ) {
      LogUtil.outMessage( "Info OK" );
      /** Skip **/
      offset += DataUtil.get16( packetData, offset ) + 2 + 8;
      int count = ( int ) DataUtil.get32( packetData, offset, true );
      offset += 4;
      LogUtil.outMessage( "Count: " + count );
      for ( int n = count; --n >= 0; ) {
        int len = DataUtil.get16( packetData, offset );// setKey ( packet.readPascalUTF8 ( ) );
        offset += 2;
        buddyInfo.buddyId = StringUtil.byteArrayToString( packetData, offset, len, true );
        buddyInfo.nickName = buddyInfo.buddyId;
        offset += len;
        offset += 8;
        int prefsCount = DataUtil.get16( packetData, offset );
        offset += 2;
        for ( ; --prefsCount >= 0; ) {
          int prefType = DataUtil.get16( packetData, offset );
          offset += 2;
          switch ( prefType ) {
            /*102*/ case UMD_PROFILE_FIRST_NAME: {
              len = DataUtil.get16( packetData, offset );// setKey ( packet.readPascalUTF8 ( ) );
              offset += 2;
              buddyInfo.addKeyValue( "FIRST_NAME_LABEL",
                      StringUtil.byteArrayToString( packetData, offset, len, true ) );
              offset += len;
              continue;
            }
            /*103*/ case UMD_PROFILE_LAST_NAME: {
              len = DataUtil.get16( packetData, offset );// setKey ( packet.readPascalUTF8 ( ) );
              offset += 2;
              buddyInfo.addKeyValue( "LAST_NAME_LABEL",
                      StringUtil.byteArrayToString( packetData, offset, len, true ) );
              offset += len;
              continue;
            }
            /*104*/ case UMD_PROFILE_GENDER: {
              buddyInfo.addKeyValue( "GENDER_LABEL",
                      Math.max( CI_GENDER_MALE, DataUtil.get32( packetData,
                      offset + 2, true ) ) == CI_GENDER_MALE
                      ? Localization.getMessage( "GENDER_MALE" )
                      : Localization.getMessage( "GENDER_FEMALE" ) );
              break;
            }
            /*106*/ case UMD_PROFILE_FRIENDLY_NAME: {
              len = DataUtil.get16( packetData, offset );// setKey ( packet.readPascalUTF8 ( ) );
              offset += 2;
              buddyInfo.nickName = StringUtil.byteArrayToString( packetData, offset, len, true );
              // buddyInfo.addKeyValue( "NICK_NAME_LABEL", buddyInfo.nickName );
              offset += len;
              continue;
            }
            /*107*/ case UMD_PROFILE_WEBSITE_1: {
              len = DataUtil.get16( packetData, offset );// setKey ( packet.readPascalUTF8 ( ) );
              offset += 2;
              buddyInfo.addKeyValue( "WEBSITE_LABEL", StringUtil.byteArrayToString( packetData, offset, len, true ) );
              offset += len;
              continue;
            }
            /*111*/ case UMD_PROFILE_ABOUT_ME: {
              len = DataUtil.get16( packetData, offset );// setKey ( packet.readPascalUTF8 ( ) );
              offset += 2;
              buddyInfo.addKeyValue( "ABOUT_ME_LABEL", StringUtil.byteArrayToString( packetData, offset, len, true ) );
              offset += len;
              continue;
            }
            /*112*/ case UMD_PROFILE_BIRTH_DATE: {
              long date = DataUtil.get32( packetData, offset + 2, true );
              if(date > 0) {
                /** Plus one day - really don't know, why **/
                date += 24 * 60 * 60;
                buddyInfo.addKeyValue( "BIRTH_DATE_LABEL", TimeUtil.getDateString(
                        date, false ) );
              }
              break;
            }
            /*2035*/ /*case UMD_PROFILE_ONLINE_STATUS: {
             int status = (int)DataUtil.get32( packetData, offset + 2, true );
             System.out.println("Status: " + status);
             buddyInfo.addKeyValue( "STATUSTITLE", 
             Localization.getMessage( IcqStatusUtil.getStatusDescr( 
             IcqStatusUtil.getStatusIndex(
             status ) ) ) );
             break;
             }*/
            /*2050*/ case UMD_PROFILE_WEBAWARE: {
              // webaware = ( int ) DataUtil.get32( packetData, offset + 2, true );
              break;
            }
            /*2052*/ case UMD_PROFILE_STATUS_LINE: {
              len = DataUtil.get16( packetData, offset );// setKey ( packet.readPascalUTF8 ( ) );
              offset += 2;
              buddyInfo.addKeyValue( "STATUSDESC",
                      StringUtil.byteArrayToString( packetData, offset, len, true ) );
              offset += len;
              continue;
            }
            /*2056*/ case UMD_PROFILE_VALIDATED_CELLULAR: {
              len = DataUtil.get16( packetData, offset );// setKey ( packet.readPascalUTF8 ( ) );
              offset += 2;
              buddyInfo.addKeyValue( "VALIDATED_CELLULAR_LABEL",
                      StringUtil.byteArrayToString( packetData, offset, len, true ) );
              offset += len;
              continue;
            }
          }
          offset += DataUtil.get16( packetData, offset ) + 2;
        }
        offset += 4;
        LogUtil.outMessage( "Info completed" );
        buddyInfo.avatar = downloadAvatar( buddyInfo.buddyId );
        ActionExec.showUserShortInfo( icqAccountRoot, buddyInfo );
      }
    }
  }

  public static Image downloadAvatar( String userId ) {
    if ( !StringUtil.isNullOrEmpty( userId ) ) {
      int atIndex = userId.indexOf( '@' );
      if ( atIndex != -1 ) {
        String domain = userId.substring( atIndex + 1 );
        LogUtil.outMessage( "Domain: " + domain );
        if ( !StringUtil.isNullOrEmpty( domain )
                && ( domain.equals( "corp.mail.ru" )
                || domain.equals( "mail.ru" )
                || domain.equals( "inbox.ru" )
                || domain.equals( "bk.ru" )
                || domain.equals( "list.ru" ) ) ) {
          return MmpPacketParser.downloadAvatar( userId );
        }
      }
      try {
        HttpConnection http = ( HttpConnection ) Connector.open(
                "http://api.icq.net/expressions/get?f=native&type=buddyIcon&t="
                + userId, Connector.READ, true );
        http.setRequestMethod( "GET" );
        if ( http.getResponseCode() == HttpConnection.HTTP_OK ) {
          InputStream stream = http.openInputStream();
          ArrayUtil array = new ArrayUtil();
          byte[] buffer = new byte[ 4096 ];
          int read;
          while ( ( read = stream.read( buffer ) ) >= 0 ) {
            array.append( buffer, 0, read );
          }
          return Image.createImage( array.byteString, 0, array.length() );
        }
      } catch ( Throwable ex ) {
      }
    }
    return null;
  }
}
