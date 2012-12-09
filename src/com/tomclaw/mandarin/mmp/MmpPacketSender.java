package com.tomclaw.mandarin.mmp;

import com.tomclaw.mandarin.main.Cookie;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.DataUtil;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;
import java.io.IOException;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class MmpPacketSender {

  public static void MRIM_CS_CHANGE_STATUS( MmpAccountRoot mmpAccountRoot, long statusId, String statusString, String descrString ) throws IOException {
    Packet packet = new Packet();
    packet.seq = mmpAccountRoot.session.seqNum++;
    packet.msg = PacketType.MRIM_CS_CHANGE_STATUS;
    packet.proto = 0x00010016;
    byte[] temp = new byte[ 4 ];
    DataUtil.put32_reversed( temp, 0, statusId & 7 );
    packet.data.append( temp );
    packet.data.append( DataUtil.mmputil_prepareByteStringWthLength(
            MmpStatusUtil.getStatusName( statusId ) ) );
    packet.data.append( DataUtil.mmputil_prepareBytesWthLength(
            StringUtil.string1251ToByteArray(
            Localization.getMessage( MmpStatusUtil.getStatusDescr(
            MmpStatusUtil.getStatusIndex( statusId ) ) ) ) ) );
    DataUtil.put32_reversed( temp, 0, 0x00 );
    packet.data.append( temp );
    DataUtil.put32_reversed( temp, 0, -1 );
    packet.data.append( temp );
    packet.send( mmpAccountRoot.session.netConnection );
  }

  public static byte[] MRIM_CS_MESSAGE( MmpAccountRoot mmpAccountRoot, String destMail, String messageText, long flags, String addon ) throws IOException {
    LogUtil.outMessage( ">>> MRIM_CS_MESSAGE to " + destMail );
    Packet packet = new Packet();
    packet.seq = mmpAccountRoot.session.seqNum++;
    packet.msg = PacketType.MRIM_CS_MESSAGE;
    packet.proto = 0x00010015; // 15 -> 0e
    packet.data.append( DataUtil.mmputil_prepareBytesFromLong( flags ) );
    packet.data.append( DataUtil.mmputil_prepareByteStringWthLength( destMail ) );
    packet.data.append( DataUtil.mmputil_prepareBytesWthLength( StringUtil.string1251ToByteArray( messageText ) ) );
    packet.data.append( DataUtil.mmputil_prepareByteStringWthLength( addon ) );
    packet.send( mmpAccountRoot.session.netConnection );
    packet.dumpPacketData();
    byte[] msgCookie = new byte[ 8 ];
    DataUtil.put32( msgCookie, 0, packet.seq );
    return msgCookie;
  }

  public static void MRIM_CS_MESSAGE_RECV( MmpAccountRoot mmpAccountRoot, String destMail, byte[] cookie ) throws IOException {
    Packet packet = new Packet();
    packet.seq = mmpAccountRoot.session.seqNum++;
    packet.msg = PacketType.MRIM_CS_MESSAGE_RECV;
    packet.proto = 0x0001000e;
    packet.data.append( DataUtil.mmputil_prepareByteStringWthLength( destMail ) );
    packet.data.append( cookie );
    packet.send( mmpAccountRoot.session.netConnection );
    packet.dumpPacketData();
  }

  public static Cookie MRIM_CS_ADD_CONTACT( MmpAccountRoot mmpAccountRoot, long flags, long groupId, byte[] contact, byte[] name, byte[] unused ) throws IOException {
    Cookie cookie = new Cookie();
    Packet packet = new Packet();
    packet.seq = cookie.cookieValue; // mmpAccountRoot.session.seqNum++;
    packet.msg = PacketType.MRIM_CS_ADD_CONTACT;
    packet.proto = 0x00010015;
    /*byte[] temp = new byte[4];
     DataUtil.put32_reversed(temp, 0, flags);
     packet.data.append(temp);
     byte[] temp1 = new byte[4];
     DataUtil.put32_reversed(temp1, 0, groupId);
     packet.data.append(temp1);*/
    packet.data.append( DataUtil.mmputil_prepareBytesFromLongReversed( flags ) );
    packet.data.append( DataUtil.mmputil_prepareBytesFromLongReversed( groupId ) );

    packet.data.append( DataUtil.mmputil_prepareBytesWthLength( contact ) );
    packet.data.append( DataUtil.mmputil_prepareBytesWthLength( name ) );
    packet.data.append( DataUtil.mmputil_prepareBytesWthLength( unused ) );
    /*temp1 = new byte[]{0x00, 0x00, 0x00, 0x00};
     packet.data.append(temp1);
     packet.data.append(temp1);*/
    packet.data.append( DataUtil.mmputil_prepareBytesFromLongReversed( 0x00000000 ) );
    packet.data.append( DataUtil.mmputil_prepareBytesFromLongReversed( 0x00000000 ) );

    packet.send( mmpAccountRoot.session.netConnection );
    packet.dumpPacketData();
    return cookie;
  }

  public static Cookie MRIM_CS_MODIFY_CONTACT( MmpAccountRoot mmpAccountRoot, long id, long flags, long groupId, byte[] contact, byte[] name, String phones ) throws IOException {
    Cookie cookie = new Cookie();
    Packet packet = new Packet();
    packet.seq = cookie.cookieValue; // mmpAccountRoot.session.seqNum++;
    packet.msg = PacketType.MRIM_CS_MODIFY_CONTACT;
    packet.proto = 0x00010015;
    packet.data.append( DataUtil.mmputil_prepareBytesFromLongReversed( id ) );
    packet.data.append( DataUtil.mmputil_prepareBytesFromLongReversed( flags ) );
    packet.data.append( DataUtil.mmputil_prepareBytesFromLongReversed( groupId ) );
    packet.data.append( DataUtil.mmputil_prepareBytesWthLength( contact ) );
    packet.data.append( DataUtil.mmputil_prepareBytesWthLength( name ) );
    packet.data.append( DataUtil.mmputil_prepareBytesWthLength( phones.getBytes() ) );
    packet.send( mmpAccountRoot.session.netConnection );
    packet.dumpPacketData();
    return cookie;
  }

  public static void MRIM_CS_AUTHORIZE( MmpAccountRoot mmpAccountRoot, String destMail ) throws IOException {
    Packet packet = new Packet();
    packet.seq = mmpAccountRoot.session.seqNum++;
    packet.msg = PacketType.MRIM_CS_AUTHORIZE;
    packet.proto = 0x0001000e;
    packet.data.append( DataUtil.mmputil_prepareByteStringWthLength( destMail ) );
    packet.send( mmpAccountRoot.session.netConnection );
    packet.dumpPacketData();
  }

  public static void MRIM_CS_WP_REQUEST( MmpAccountRoot mmpAccountRoot, String destMail ) throws IOException {
    Packet packet = new Packet();
    packet.seq = mmpAccountRoot.session.seqNum++;
    packet.msg = PacketType.MRIM_CS_WP_REQUEST;
    packet.proto = 0x00010015;
    packet.data.append( DataUtil.mmputil_prepareBytesFromLongReversed( 0x00000000 ) );
    packet.data.append( DataUtil.mmputil_prepareByteStringWthLength( destMail.substring( 0, destMail.indexOf( '@' ) ) ) );
    packet.data.append( DataUtil.mmputil_prepareBytesFromLongReversed( 0x00000001 ) );
    packet.data.append( DataUtil.mmputil_prepareByteStringWthLength( destMail.substring( destMail.indexOf( '@' ) + 1 ) ) );
    packet.send( mmpAccountRoot.session.netConnection );
    packet.dumpPacketData();
  }

  public static void MRIM_CS_SMS_MESSAGE( MmpAccountRoot mmpAccountRoot, String destMail, String messText ) throws IOException {
    Packet packet = new Packet();
    packet.seq = mmpAccountRoot.session.seqNum++;
    packet.msg = PacketType.MRIM_CS_SMS_SEND;
    packet.proto = 0x00010015;
    packet.data.append( DataUtil.mmputil_prepareBytesFromLongReversed( 0x00000000 ) );
    packet.data.append( DataUtil.mmputil_prepareByteStringWthLength( destMail ) );
    packet.data.append( DataUtil.mmputil_prepareBytesWthLength( ( StringUtil.string1251ToByteArray( messText ) ) ) );
    packet.send( mmpAccountRoot.session.netConnection );
    packet.dumpPacketData();
  }
}
