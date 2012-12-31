package com.tomclaw.mandarin.mmp;

import com.tomclaw.mandarin.main.ActionExec;
import com.tomclaw.mandarin.main.BuddyInfo;
import com.tomclaw.mandarin.main.Cookie;
import com.tomclaw.tcuilite.ChatItem;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.*;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

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
        String groupMask = new String( DataUtil.getByteArray( packet.data.byteString, offset, maskLength ) );
        offset += maskLength;
        maskLength = ( int ) DataUtil.get32_reversed( packet.data.byteString, offset, true );
        offset += 4;
        String buddyMask = new String( DataUtil.getByteArray( packet.data.byteString, offset, maskLength ) );
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
          LogUtil.outMessage( "Group (" + mmpGroup.userId + ") >>" + mmpGroup.flags );

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
                mmpItem.userId = new String( DataUtil.getByteArray( packet.data.byteString,
                        offset += 4, nameLength ) );
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
                System.out.println( "mmpItem.buddyStatus "
                        + "= " + mmpItem.getStatusIndex() );
                offset += 4;
                break;
              }
              case 6: { // s
                // Phone
                int nameLength = ( int ) DataUtil.get32( packet.data.byteString, offset, false );
                mmpItem.userPhone = new String( DataUtil.getByteArray( packet.data.byteString,
                        offset += 4, nameLength ) );
                offset += nameLength;
                break;
              }
              case 7: { // s
                // Extended status info
                int length = ( int ) DataUtil.get32( packet.data.byteString, offset, false );
                offset += 4;
                if ( length > 0 ) {
                  String statusIdString = new String( DataUtil.getByteArray( packet.data.byteString,
                          offset, length ) );
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
                  System.out.println( i + ": " + StringUtil.byteArrayToString(
                          DataUtil.getByteArray( packet.data.byteString,
                          offset += 4, length ) ) );
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
              mmpItem.isPhone = true;
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
        /**
         * Showing all the list
         */
        ActionExec.setBuddyList( mmpAccountRoot, buddyList, null, -1, 0, null );
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
      String statusIdString = new String( DataUtil.getByteArray( packet.data.byteString, offset, nameLength ) );
      offset += nameLength;
      nameLength = ( int ) DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      String statusString = new String( DataUtil.getByteArray( packet.data.byteString, offset, nameLength ) );
      offset += nameLength;
      nameLength = ( int ) DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      String statusDescr = new String( DataUtil.getByteArray( packet.data.byteString, offset, nameLength ) );
      offset += nameLength;
      nameLength = ( int ) DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      String userMail = new String( DataUtil.getByteArray( packet.data.byteString, offset, nameLength ) );
      offset += nameLength;
      long clientFlags = DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      nameLength = ( int ) DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      String clientIdString = new String( DataUtil.getByteArray( packet.data.byteString, offset, nameLength ) );
      offset += nameLength;
      LogUtil.outMessage( "userMail = " + userMail );
      LogUtil.outMessage( "userStatus = " + userStatus );
      LogUtil.outMessage( "statusIdString = " + statusIdString );
      LogUtil.outMessage( "statusString = " + statusString );
      LogUtil.outMessage( "statusDescr = " + statusDescr );
      LogUtil.outMessage( "clientFlags = " + clientFlags );
      LogUtil.outMessage( "clientIdString = " + clientIdString );

      System.out.println( "userMail = " + userMail );
      System.out.println( "userStatus = " + userStatus );
      System.out.println( "statusIdString = " + statusIdString );
      System.out.println( "statusString = " + statusString );
      System.out.println( "statusDescr = " + statusDescr );
      System.out.println( "clientFlags = " + clientFlags );
      System.out.println( "clientIdString = " + clientIdString );
      try {
        ActionExec.setMailStatus( mmpAccountRoot, userMail, MmpStatusUtil
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
      String userMail = new String( DataUtil.getByteArray( packet.data.byteString, offset, stringLength ) );
      offset += stringLength;
      stringLength = ( int ) DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      String messageText = null;
      byte[] bArray = DataUtil.getByteArray( packet.data.byteString, offset, stringLength );
      LogUtil.outMessage( "flags = " + flags );
      LogUtil.outMessage( "encoding = " + ( int ) ( ( flags & 0xffff0000 ) >> 16 ) );
      switch ( StringUtil.determEncoding( bArray ) ) { // (int) ((flags & 0xffff0000) >> 16)
        case 0x01: {
          messageText = StringUtil.getWin1251( bArray );
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
              messageText = StringUtil.getWin1251( data );
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
        ActionExec.setBuddyTypingStatus( mmpAccountRoot, userMail, null, false, true );
        return;
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
      ActionExec.recMess( mmpAccountRoot, userMail, null, null, messageText, cookie, msgType );
    } else if ( packet.msg == PacketType.MRIM_CS_MESSAGE_STATUS ) {
      long status = DataUtil.get32_reversed( packet.data.byteString, offset, true );
      offset += 4;
      if ( status == PacketType.MESSAGE_DELIVERED ) {
        LogUtil.outMessage( "Message delivered" );
        byte[] temp = new byte[ 8 ];
        DataUtil.put32( temp, 0, packet.seq );
        ActionExec.msgAck( mmpAccountRoot, null, null, temp );
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
        ActionExec.setMainFrameAction( mmpAccountRoot, Localization.getMessage( "CONTACT_OPER_SUCCESS" ) );
        if ( packet.data.byteString.length >= offset + 4 ) {
          long contactId = DataUtil.get32_reversed( packet.data.byteString, offset, true );
          offset += 4;
          Hashtable params = new Hashtable();
          params.put( "contactId", new Long( contactId ) );
          ActionExec.processQueueAction( mmpAccountRoot, cookie, params );
          return;
        }
        if ( packet.msg == PacketType.MRIM_CS_MODIFY_CONTACT_ACK ) {
          Hashtable params = new Hashtable();
          params.put( "contactId", new Long( 0 ) );
          ActionExec.processQueueAction( mmpAccountRoot, cookie, params );
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
      ActionExec.cancelQueueAction( mmpAccountRoot, cookie, errorString );
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
          fields[c] = new String( DataUtil.getByteArray( packet.data.byteString, offset, ( int ) fieldLength ) );
          offset += fieldLength;
        }
        BuddyInfo buddyInfo = new BuddyInfo( null, null, -1 );
        for ( int c = 0; c < fieldNum; c++ ) {
          long fieldLength = DataUtil.get32_reversed( packet.data.byteString, offset, true );
          offset += 4;
          byte[] value = ( DataUtil.getByteArray( packet.data.byteString, offset, ( int ) fieldLength ) );
          offset += fieldLength;
          if ( fields[c].equals( "Username" ) ) {
            buddyInfo.buddyId = new String( value );
          } else if ( fields[c].equals( "Domain" ) ) {
            buddyInfo.buddyId += "@" + new String( value );
            buddyInfo.buddyHash.put( "DOMAIN", new String( value ) );
          } else if ( fields[c].equals( "Nickname" ) ) {
            buddyInfo.nickName = StringUtil.getWin1251( value );
          } else if ( fields[c].equals( "FirstName" ) ) {
            buddyInfo.buddyHash.put( "FIRST_NAME_LABEL", StringUtil.getWin1251( value ) );
          } else if ( fields[c].equals( "LastName" ) ) {
            buddyInfo.buddyHash.put( "LAST_NAME_LABEL", StringUtil.getWin1251( value ) );
          } else if ( fields[c].equals( "Sex" ) ) {
            int sexValue = value.length > 0 ? value[0] : 0;
            String sexString;
            if ( sexValue == 49 ) {
              sexString = Localization.getMessage(
                      "MALE" );
            } else if ( sexValue == 50 ) {
              sexString = Localization.getMessage(
                      "FEMALE" );
            } else {
              sexString = Localization.getMessage(
                      "UNK" );
            }
            buddyInfo.buddyHash.put( "SEX", sexString );
            // mailInfo.sex = 
          } else if ( fields[c].equals( "Birthday" ) ) {
            buddyInfo.buddyHash.put( "BIRTHDAY", new String( value ) );
            // mailInfo.birthday = new String(value);
          } else if ( fields[c].equals( "City_id" ) ) {
            // mailInfo.cityId = Integer.parseInt(new String(value));
          } else if ( fields[c].equals( "Location" ) ) {
            buddyInfo.buddyHash.put( "LOCATION", StringUtil.getWin1251( value ) );
            // mailInfo.location = StringUtil.getWin1251(value);
          } else if ( fields[c].equals( "Zodiac" ) ) {
            // mailInfo.zodiac = Integer.parseInt(new String(value));
          } else if ( fields[c].equals( "BMonth" ) ) {
            // mailInfo.bMonth = Integer.parseInt(new String(value));
          } else if ( fields[c].equals( "BDay" ) ) {
            // mailInfo.bDay = Integer.parseInt(new String(value));
          } else if ( fields[c].equals( "Country_id" ) ) {
            // mailInfo.countryId = Integer.parseInt(new String(value));
          } else if ( fields[c].equals( "Phone" ) ) {
            buddyInfo.buddyHash.put( "PHONE", new String( value ) );
            // mailInfo.phone = new String(value);
          } else if ( fields[c].equals( "mrim_status" ) ) {
            // mailInfo.mrimStatus = value.length >= 4 ? DataUtil.get32_reversed(value, 0, true) : 0;
          } else if ( fields[c].equals( "status_uri" ) ) {
            // mailInfo.statusUri = new String(value);
          } else if ( fields[c].equals( "status_title" ) ) {
            buddyInfo.buddyHash.put( "STATUSTITLE", StringUtil.getWin1251( value ) );
            // mailInfo.statusTitle = StringUtil.getWin1251(value);
          } else if ( fields[c].equals( "status_desc" ) ) {
            buddyInfo.buddyHash.put( "STATUSDESC", StringUtil.getWin1251( value ) );
            // mailInfo.statusDesc = StringUtil.getWin1251(value);
          } else if ( fields[c].equals( "ua_features" ) ) {
            // mailInfo.uaFeatures = value.length >= 4 ? DataUtil.get32_reversed(value, 0, true) : 0;
          }
        }
        ActionExec.showUserShortInfo( mmpAccountRoot, buddyInfo );
      } else if ( status == PacketType.MRIM_ANKETA_INFO_STATUS_NOUSER ) {
      } else if ( status == PacketType.MRIM_ANKETA_INFO_STATUS_RATELIMERR ) {
      }
    }
  }
}