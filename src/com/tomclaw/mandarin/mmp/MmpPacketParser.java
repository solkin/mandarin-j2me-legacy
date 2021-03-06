package com.tomclaw.mandarin.mmp;

import com.tomclaw.mandarin.core.Handler;
import com.tomclaw.mandarin.core.BuddyInfo;
import com.tomclaw.mandarin.core.Cookie;
import com.tomclaw.tcuilite.ChatItem;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Image;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class MmpPacketParser {

  public static void parsePacket( MmpAccountRoot mmpAccountRoot, Packet packet ) {
    packet.dumpPacketData();
    LogUtil.outMessage( "packet.msg = " + packet.msg );
    int offset = 0;
    if ( packet.msg == PacketType.MRIM_CS_CONTACT_LIST2 ) {
      long reqStatus = DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      if ( reqStatus == 0x00000000 ) {
        /** Success **/
        long groupCount = DataUtil.get32_reversed( packet.data.byteString, offset, true );
        offset += 4;
        int maskLength = ( int ) DataUtil.get32_reversed( packet.data.byteString, offset, true );
        offset += 4;
        String groupMask = StringUtil.byteArrayToString( DataUtil.getByteArray( packet.data.byteString, offset, maskLength ), false );
        offset += maskLength;
        maskLength = ( int ) DataUtil.get32_reversed( packet.data.byteString, offset, true );
        offset += 4;
        String buddyMask = StringUtil.byteArrayToString( DataUtil.getByteArray( packet.data.byteString, offset, maskLength ), false );
        offset += maskLength;
        Vector buddyList = new Vector();
        long contactIdIncrm = 0;
        MmpGroup mmpGroup;
        for ( int c = 0; c < groupCount; c++ ) {
          /** Reading group **/
          mmpGroup = new MmpGroup( "" );
          mmpGroup.contactId = contactIdIncrm++;
          for ( int i = 0; i < groupMask.length(); i++ ) {
            switch ( i ) {
              case 0: { // u
                // Flags
                mmpGroup.flags = ( int ) DataUtil.get32( packet.data.byteString, offset, false );
                offset += 4;
                break;
              }
              case 1: { // s
                int nameLength = ( int ) DataUtil.get32( packet.data.byteString, offset, false );
                mmpGroup.userId = StringUtil.byteArray1251ToString( DataUtil.getByteArray( packet.data.byteString,
                        offset += 4, nameLength ), 0, nameLength );

                offset += nameLength;
                break;
              }
              default: {
                if ( groupMask.charAt( i ) == 'u' ) {
                  offset += 4;
                } else if ( groupMask.charAt( i ) == 's' ) {
                  offset += 4 + ( int ) DataUtil.get32( packet.data.byteString, offset, false );
                }
                break;
              }
            }
          }
          LogUtil.outMessage( "Group (" + mmpGroup.userId + ") flags: " + mmpGroup.flags + ") contactId: " + mmpGroup.contactId );
          
          if ( ( mmpGroup.flags & PacketType.CONTACT_FLAG_REMOVED ) == 0 ) {
            LogUtil.outMessage( "name: " + mmpGroup.userId + " id: " + mmpGroup.contactId + " flags: " + mmpGroup.flags );
            buddyList.addElement( mmpGroup );
          }
        }
        /** Telephone contacts groups **/
        MmpGroup phonesGroup = new MmpGroup( Localization.getMessage( "TELEPHONE_CONTACTS" ) );
        buddyList.addElement( phonesGroup );
        mmpAccountRoot.phoneGroup = phonesGroup;
        /** Others contacts groups **/
        MmpGroup othersGroup = new MmpGroup( Localization.getMessage( "OTHER_CONTACTS" ) );
        buddyList.addElement( othersGroup );
        MmpItem mmpItem;
        contactIdIncrm = 20;
        while ( offset < packet.data.length() ) {
          /** Reading users **/
          mmpItem = new MmpItem( "" );
          mmpItem.contactId = contactIdIncrm++;
          for ( int i = 0; i < buddyMask.length(); i++ ) {
            switch ( i ) {
              case 0: { // u
                // Flags
                mmpItem.flags = DataUtil.get32( packet.data.byteString, offset, false );
                offset += 4;
                break;
              }
              case 1: { // u
                // Group
                mmpItem.groupId = DataUtil.get32( packet.data.byteString, offset, false );
                offset += 4;
                break;
              }
              case 2: { // s
                // Address
                int nameLength = ( int ) DataUtil.get32( packet.data.byteString, offset, false );
                mmpItem.userId = StringUtil.byteArrayToString( DataUtil.getByteArray( packet.data.byteString,
                        offset += 4, nameLength ), false );
                offset += nameLength;
                break;
              }
              case 3: { // s
                // Nick
                int nameLength = ( int ) DataUtil.get32( packet.data.byteString, offset, false );
                mmpItem.userNick = StringUtil.byteArray1251ToString( DataUtil.getByteArray( packet.data.byteString,
                        offset += 4, nameLength ), 0, nameLength );
                offset += nameLength;
                break;
              }
              case 4: { // u
                // Server flags (auth)
                mmpItem.servFlags = DataUtil.get32( packet.data.byteString, offset, false );
                offset += 4;
                break;
              }
              case 5: { // u
                // Status on-line
                mmpItem.setStatusIndex( MmpStatusUtil.getStatusIndex(
                        DataUtil.get32( packet.data.byteString, offset,
                        false ) ), null );
                offset += 4;
                break;
              }
              case 6: { // s
                // Phone
                int nameLength = ( int ) DataUtil.get32( packet.data.byteString, offset, false );
                mmpItem.userPhone = StringUtil.byteArrayToString( DataUtil.getByteArray( packet.data.byteString,
                        offset += 4, nameLength ), false );
                offset += nameLength;
                break;
              }
              case 7: { // s
                // Extended status info
                int length = ( int ) DataUtil.get32( packet.data.byteString, offset, false );
                offset += 4;
                if ( length > 0 ) {
                  String statusIdString = StringUtil.byteArrayToString( DataUtil.getByteArray( packet.data.byteString,
                          offset, length ), false );
                  offset += length;
                  if ( !StringUtil.isNullOrEmpty( statusIdString ) ) {
                    mmpItem.setStatusIndex( MmpStatusUtil.getStatusIndex( statusIdString ), null );
                  }
                }
                break;
              }
              default: {
                if ( buddyMask.charAt( i ) == 'u' ) {
                  offset += 4;
                } else if ( buddyMask.charAt( i ) == 's' ) {
                  int length = ( int ) DataUtil.get32( packet.data.byteString,
                          offset, false );
                  offset += 4;
                  offset += length;
                }
                break;
              }
            }
          }
          /** Adding user to it's group **/
          LogUtil.outMessage( mmpItem.userNick + "(" + mmpItem.userId + ") >>"
                  + mmpItem.groupId + " [" + mmpItem.flags + "] status = "
                  + mmpItem.getStatusIndex() + " id: " + mmpItem.contactId );

          if ( 0 == ( mmpItem.flags & PacketType.CONTACT_FLAG_REMOVED ) ) {
            if ( ( mmpItem.flags & PacketType.CONTACT_FLAG_PHONE ) == 0 ) {
              for ( int c = 0; c < buddyList.size(); c++ ) {
                if ( ( ( ( MmpGroup ) buddyList.elementAt( c ) ).getId() ) == mmpItem.groupId ) {
                  ( ( MmpGroup ) buddyList.elementAt( c ) ).addChild( mmpItem );
                  mmpItem = null;
                  break;
                }
              }
            } else if ( ( mmpItem.flags & PacketType.CONTACT_FLAG_PHONE ) != 0 ) {
              mmpItem.setIsPhone( true );
              mmpItem.userId = mmpItem.userPhone;
              phonesGroup.addChild( mmpItem );
              mmpItem = null;
            }
            if ( mmpItem != null ) {
              if ( DataUtil.reverseLong( mmpItem.flags ) != 1 ) {
                othersGroup.addChild( mmpItem );
              }
            }
          }
        }
        /** Showing all the list **/
        Handler.setBuddyList( mmpAccountRoot, buddyList, null, -1, 0, null );
      }
    } else if ( packet.msg == PacketType.MRIM_CS_CONNECTION_PARAMS ) {
      mmpAccountRoot.session.pingDelay = DataUtil.get32_reversed( packet.data.byteString, 0, true );
    } else if ( packet.msg == PacketType.MRIM_CS_LOGOUT ) {
      if ( DataUtil.get32_reversed( packet.data.byteString, 0, true ) == PacketType.LOGOUT_NO_RELOGIN_FLAG ) {
        // No relogin
      }
      mmpAccountRoot.session.disconnect();
    } else if ( packet.msg == PacketType.MRIM_CS_USER_STATUS ) {
      long userStatus = DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      int nameLength = ( int ) DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      String statusIdString = StringUtil.byteArrayToString( DataUtil.getByteArray( packet.data.byteString, offset, nameLength ), false );
      offset += nameLength;
      nameLength = ( int ) DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      String statusString = StringUtil.byteArrayToString( DataUtil.getByteArray( packet.data.byteString, offset, nameLength ), false );
      offset += nameLength;
      nameLength = ( int ) DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      String statusDescr = StringUtil.byteArrayToString( DataUtil.getByteArray( packet.data.byteString, offset, nameLength ), false );
      offset += nameLength;
      nameLength = ( int ) DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      String userMail = StringUtil.byteArrayToString( DataUtil.getByteArray( packet.data.byteString, offset, nameLength ), false );
      offset += nameLength;
      long clientFlags = DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      nameLength = ( int ) DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      String clientIdString = StringUtil.byteArrayToString( DataUtil.getByteArray( packet.data.byteString, offset, nameLength ), false );
      offset += nameLength;
      LogUtil.outMessage( "userMail = " + userMail );
      LogUtil.outMessage( "userStatus = " + userStatus );
      LogUtil.outMessage( "statusIdString = " + statusIdString );
      LogUtil.outMessage( "statusString = " + statusString );
      LogUtil.outMessage( "statusDescr = " + statusDescr );
      LogUtil.outMessage( "clientFlags = " + clientFlags );
      LogUtil.outMessage( "clientIdString = " + clientIdString );
      try {
        Handler.setMailStatus( mmpAccountRoot, userMail, MmpStatusUtil
                .getStatus( MmpStatusUtil.getStatusIndex( statusIdString ) ) );
      } catch ( Throwable ex1 ) {
      }
    } else if ( packet.msg == PacketType.MRIM_CS_MESSAGE_ACK ) {
      byte[] cookie = DataUtil.getByteArray( packet.data.byteString, offset, 4 );
      offset += 4;
      long flags = DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      int stringLength = ( int ) DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      String userMail = StringUtil.byteArrayToString( DataUtil.getByteArray( packet.data.byteString, offset, stringLength ), false );
      offset += stringLength;
      stringLength = ( int ) DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      String messageText = null;
      byte[] bArray = DataUtil.getByteArray( packet.data.byteString, offset, stringLength );
      LogUtil.outMessage( "flags = " + flags );
      LogUtil.outMessage( "encoding = " + ( int ) ( ( flags & 0xffff0000 ) >> 16 ) );
      switch ( StringUtil.determEncoding( bArray ) ) { // (int) ((flags & 0xffff0000) >> 16)
        case 0x01: {
          messageText = StringUtil.ucs2leByteArrayToString( bArray );
          break;
        }
        case 0x02: {
          messageText = StringUtil.byteArray1251ToString( bArray, 0, stringLength );
          break;
        }
      }
      offset += stringLength;
      int msgType;
      LogUtil.outMessage( "msgType == " + Long.toString( flags, 16 ) + " XORed: " + ( Long.toString( flags & 0x000000ff, 16 ) ) );
      if ( ( flags & 0x000000ff ) == PacketType.MESSAGE_FLAG_AUTHORIZE ) {
        msgType = ChatItem.TYPE_AUTH_REQ_MSG;

        try {
          LogUtil.outMessage( "bArray: " + StringUtil.byteArrayToString( bArray ) );
          byte[] unBase64 = Base64.decode( StringUtil.byteArrayToString( bArray ) );
          LogUtil.outMessage( HexUtil.bytesToString( unBase64 ) );
          byte[] data = ArrayUtil.copyOfRange( unBase64, 12, unBase64.length );
          switch ( StringUtil.determEncoding( data ) ) { // (int) ((flags & 0xffff0000) >> 16)
            case 0x01: {
              messageText = StringUtil.ucs2leByteArrayToString( data );
              break;
            }
            case 0x02: {
              messageText = StringUtil.byteArray1251ToString( data, 0, data.length );
              break;
            }
          }
        } catch ( IOException ex ) {
        }
        LogUtil.outMessage( "ChatItem.TYPE_AUTH_REQ_MSG" );
      } else if ( ( flags & 0x000000ff ) == PacketType.MESSAGE_FLAG_SYSTEM ) {
        msgType = ChatItem.TYPE_ERROR_MSG;
      } else if ( ( flags & 0x0000ff00 ) == PacketType.MESSAGE_FLAG_NOTIFY ) {
        Handler.setBuddyTypingStatus( mmpAccountRoot, userMail, null, false, true );
        return;
      } else if ( ( flags & 0x0000ff00 ) == PacketType.MESSAGE_FLAG_WAKEUP ) {
        msgType = ChatItem.TYPE_INFO_MSG;
        messageText = Localization.getMessage( "WAKEUP_TEXT" );
      } else {
        msgType = ChatItem.TYPE_PLAIN_MSG;
        if ( ( flags & 0x000000ff ) != PacketType.MESSAGE_FLAG_NORECV ) {
          try {
            MmpPacketSender.MRIM_CS_MESSAGE_RECV( mmpAccountRoot, userMail, cookie );
          } catch ( Throwable ex ) {
            LogUtil.outMessage( ex.getMessage(), true );
          }
        }
      }
      Handler.recMess( mmpAccountRoot, userMail, null, null, messageText, cookie, msgType );
    } else if ( packet.msg == PacketType.MRIM_CS_MESSAGE_STATUS ) {
      long status = DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      if ( status == PacketType.MESSAGE_DELIVERED ) {
        LogUtil.outMessage( "Message delivered" );
        byte[] temp = new byte[ 8 ];
        DataUtil.put32( temp, 0, packet.seq );
        Handler.msgAck( mmpAccountRoot, null, null, temp );
      } else if ( status == PacketType.MESSAGE_REJECTED_INTERR ) {
      } else if ( status == PacketType.MESSAGE_REJECTED_NOUSER ) {
      } else if ( status == PacketType.MESSAGE_REJECTED_LIMIT_EXCEEDED ) {
      } else if ( status == PacketType.MESSAGE_REJECTED_TOO_LARGE ) {
      } else if ( status == PacketType.MESSAGE_REJECTED_DENY_OFFMSG ) {
      }
    } else if ( packet.msg == PacketType.MRIM_CS_ADD_CONTACT_ACK || packet.msg == PacketType.MRIM_CS_MODIFY_CONTACT_ACK ) {
      long status = DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      String errorString = null;
      Cookie cookie = new Cookie( packet.seq );
      if ( status == PacketType.CONTACT_OPER_SUCCESS ) {
        Handler.setMainFrameAction( mmpAccountRoot, Localization.getMessage( "CONTACT_OPER_SUCCESS" ) );
        if ( packet.data.byteString.length >= offset + 4 ) {
          long contactId = DataUtil.get32_reversed( packet.data.byteString, offset, true );
          offset += 4;
          Hashtable params = new Hashtable();
          params.put( "contactId", new Long( contactId ) );
          Handler.processQueueAction( mmpAccountRoot, cookie, params );
          return;
        }
        if ( packet.msg == PacketType.MRIM_CS_MODIFY_CONTACT_ACK ) {
          Hashtable params = new Hashtable();
          params.put( "contactId", new Long( 0 ) );
          Handler.processQueueAction( mmpAccountRoot, cookie, params );
          return;
        }
      } else if ( status == PacketType.CONTACT_OPER_ERROR ) {
        errorString = "CONTACT_OPER_ERROR";
      } else if ( status == PacketType.CONTACT_OPER_INTERR ) {
        errorString = "CONTACT_OPER_INTERR";
      } else if ( status == PacketType.CONTACT_OPER_NO_SUCH_USER ) {
        errorString = "CONTACT_OPER_NO_SUCH_USER";
      } else if ( status == PacketType.CONTACT_OPER_INVALID_INFO ) {
        errorString = "CONTACT_OPER_INVALID_INFO";
      } else if ( status == PacketType.CONTACT_OPER_USER_EXISTS ) {
        errorString = "CONTACT_OPER_USER_EXISTS";
      } else if ( status == PacketType.CONTACT_OPER_GROUP_LIMIT ) {
        errorString = "CONTACT_OPER_GROUP_LIMIT";
      } else {
        errorString = "CONTACT_OPER_ERROR";
      }
      Handler.cancelQueueAction( mmpAccountRoot, cookie, errorString );
    } else if ( packet.msg == PacketType.MRIM_CS_ANKETA_INFO ) {
      long status = DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      if ( status == PacketType.MRIM_ANKETA_INFO_STATUS_OK ) {
        long fieldNum = DataUtil.get32_reversed( packet.data.byteString, offset, true );
        offset += 4;
        long maxRows = DataUtil.get32_reversed( packet.data.byteString, offset, true );
        offset += 4;
        long servTime = DataUtil.get32_reversed( packet.data.byteString, offset, true );
        offset += 4;
        String[] fields = new String[ ( int ) fieldNum ];
        for ( int c = 0; c < fieldNum; c++ ) {
          long fieldLength = DataUtil.get32_reversed( packet.data.byteString, offset, true );
          offset += 4;
          fields[c] = StringUtil.byteArrayToString( DataUtil.getByteArray( packet.data.byteString, offset, ( int ) fieldLength ) );
          offset += fieldLength;
        }
        BuddyInfo buddyInfo = new BuddyInfo();
        for ( int c = 0; c < fieldNum; c++ ) {
          long fieldLength = DataUtil.get32_reversed( packet.data.byteString, offset, true );
          offset += 4;
          byte[] value = ( DataUtil.getByteArray( packet.data.byteString, offset, ( int ) fieldLength ) );
          offset += fieldLength;
          if ( fields[c].equals( "Username" ) ) {
            buddyInfo.buddyId = StringUtil.byteArrayToString( value );
          } else if ( fields[c].equals( "Domain" ) ) {
            buddyInfo.buddyId += "@" + StringUtil.byteArrayToString( value );
          } else if ( fields[c].equals( "Nickname" ) ) {
            buddyInfo.nickName = StringUtil.ucs2leByteArrayToString( value );
          } else if ( fields[c].equals( "FirstName" ) ) {
            buddyInfo.addKeyValue( "FIRST_NAME_LABEL", StringUtil.ucs2leByteArrayToString( value ) );
          } else if ( fields[c].equals( "LastName" ) ) {
            buddyInfo.addKeyValue( "LAST_NAME_LABEL", StringUtil.ucs2leByteArrayToString( value ) );
          } else if ( fields[c].equals( "Sex" ) ) {
            int sexValue = value.length > 0 ? value[0] : 0;
            String sexString;
            if ( sexValue == 49 ) {
              sexString = Localization.getMessage(
                      "GENDER_MALE" );
            } else if ( sexValue == 50 ) {
              sexString = Localization.getMessage(
                      "GENDER_FEMALE" );
            } else {
              sexString = Localization.getMessage(
                      "GENDER_UNK" );
            }
            buddyInfo.addKeyValue( "GENDER_LABEL", sexString );
            // mailInfo.sex = 
          } else if ( fields[c].equals( "Birthday" ) ) {
            buddyInfo.addKeyValue( "BIRTH_DATE_LABEL", StringUtil.byteArrayToString( value ) );
          } else if ( fields[c].equals( "City_id" ) ) {
            // mailInfo.cityId = Integer.parseInt(StringUtil.byteArrayToString(value, false));
          } else if ( fields[c].equals( "Location" ) ) {
            buddyInfo.addKeyValue( "LOCATION", StringUtil.ucs2leByteArrayToString( value ) );
            // mailInfo.location = StringUtil.getWin1251(value);
          } else if ( fields[c].equals( "Zodiac" ) ) {
            // mailInfo.zodiac = Integer.parseInt(StringUtil.byteArrayToString(value, false));
          } else if ( fields[c].equals( "BMonth" ) ) {
            // mailInfo.bMonth = Integer.parseInt(StringUtil.byteArrayToString(value, false));
          } else if ( fields[c].equals( "BDay" ) ) {
            // mailInfo.bDay = Integer.parseInt(StringUtil.byteArrayToString(value, false));
          } else if ( fields[c].equals( "Country_id" ) ) {
            // mailInfo.countryId = Integer.parseInt(StringUtil.byteArrayToString(value, false));
          } else if ( fields[c].equals( "Phone" ) ) {
            buddyInfo.addKeyValue( "VALIDATED_CELLULAR_LABEL", StringUtil.ucs2leByteArrayToString( value ) );
            // mailInfo.phone = StringUtil.byteArrayToString(value, false);
          } else if ( fields[c].equals( "mrim_status" ) ) {
            // mailInfo.mrimStatus = value.length >= 4 ? DataUtil.get32_reversed(value, 0, true) : 0;
          } else if ( fields[c].equals( "status_uri" ) ) {
            // mailInfo.statusUri = StringUtil.byteArrayToString(value, false);
          } else if ( fields[c].equals( "status_title" ) ) {
            buddyInfo.addKeyValue( "STATUSTITLE", StringUtil.ucs2leByteArrayToString( value ) );
            // mailInfo.statusTitle = StringUtil.getWin1251(value);
          } else if ( fields[c].equals( "status_desc" ) ) {
            buddyInfo.addKeyValue( "STATUSDESC", StringUtil.ucs2leByteArrayToString( value ) );
            // mailInfo.statusDesc = StringUtil.getWin1251(value);
          } else if ( fields[c].equals( "ua_features" ) ) {
            // mailInfo.uaFeatures = value.length >= 4 ? DataUtil.get32_reversed(value, 0, true) : 0;
          }
        }
        buddyInfo.avatar = downloadAvatar( buddyInfo.buddyId );
        Handler.showUserShortInfo( mmpAccountRoot, buddyInfo );
      } else if ( status == PacketType.MRIM_ANKETA_INFO_STATUS_NOUSER ) {
      } else if ( status == PacketType.MRIM_ANKETA_INFO_STATUS_RATELIMERR ) {
      }
    }
  }

  public static Image downloadAvatar( String email ) {
    int domainIndex = email.indexOf( "@" );
    int domainDotIndex = email.indexOf( '.', domainIndex );
    if ( domainIndex != -1 && domainDotIndex != -1 ) {
      final String userName = email.substring( 0, domainIndex );
      final String domain = email.substring( domainIndex + 1, domainDotIndex );
      try {
        HttpConnection http = ( HttpConnection ) Connector.open(
                "http://buddyicon.foto.mail.ru/" + domain + "/" + userName + "/_avatar",
                Connector.READ, true );
        LogUtil.outMessage( "http://buddyicon.foto.mail.ru/" + domain + "/" + userName + "/_avatar" );
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