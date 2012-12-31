package com.tomclaw.mandarin.mmp;

import com.tomclaw.mandarin.main.ActionExec;
import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.mandarin.net.IncorrectAddressException;
import com.tomclaw.mandarin.net.NetConnection;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.DataUtil;
import com.tomclaw.utils.HexUtil;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;
import java.io.IOException;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class MmpSession implements Runnable {

  public NetConnection netConnection;
  public int seqNum = 1;
  public boolean isAlive = false;
  public long pingDelay = 60000;
  public String clientId = null;
  public String mraVer = null;
  public Thread listener = null;
  public MmpAccountRoot mmpAccountRoot;
  public boolean isError = false;

  public MmpSession( MmpAccountRoot mmpAccountRoot ) {
    this.mmpAccountRoot = mmpAccountRoot;

    int major = Integer.parseInt( MidletMain.version.substring( 0, MidletMain.version.indexOf( "." ) ) );
    int minor = Integer.parseInt( MidletMain.version.substring( MidletMain.version.indexOf( "." ) + 1 ) );

    byte major1 = ( byte ) Integer.parseInt( String.valueOf( MidletMain.build.charAt( 0 ) ) );
    byte main1 = ( byte ) Integer.parseInt( String.valueOf( MidletMain.build.charAt( 1 ) ) );
    byte submain1 = ( byte ) Integer.parseInt( String.valueOf( MidletMain.build.charAt( 2 ) ) );
    byte minor1 = ( byte ) Integer.parseInt( String.valueOf( MidletMain.build.charAt( 3 ) ) );
    clientId = "client=\"Mandarin IM\" name=\"mandarin_im\" version=\"" + major + "." + minor + "\" build=\""
            + major1 + main1 + submain1 + minor1 + "\"";
    mraVer = "Mandarin IM " + major + "." + minor;
  }

  public boolean login_stage( String hostPort, String userId, String passwrd, 
          long statusId, String statusString, String descrString ) 
          throws IncorrectAddressException, IOException, InterruptedException, 
          IncorrectAddressException {
    isAlive = true;
    netConnection = new NetConnection();
    LogUtil.outMessage( "Connecting..." );
    netConnection.connectAddress( hostPort );
    ActionExec.setConnectionStage( mmpAccountRoot, 1 );
    LogUtil.outMessage( "Connected to: " + hostPort );
    byte[] header = netConnection.readTo( ( byte ) 0x0a );
    ActionExec.setConnectionStage( mmpAccountRoot, 2 );
    HexUtil.dump_( header, "MRIM_CS_HELLO_ACK: " );
    hostPort = new String( header, 0, header.length - 1 );
    LogUtil.outMessage( "Closing connection..." );
    netConnection.disconnect();
    ActionExec.setConnectionStage( mmpAccountRoot, 3 );
    LogUtil.outMessage( "Connecting to RCS: " + hostPort );
    netConnection.connectAddress( hostPort );
    ActionExec.setConnectionStage( mmpAccountRoot, 4 );
    LogUtil.outMessage( "Conneced" );
    /**
     * MRIM_CS_HELLO
     */
    Packet packet = new Packet();
    packet.seq = seqNum++;
    packet.msg = PacketType.MRIM_CS_HELLO;
    packet.send( netConnection );
    ActionExec.setConnectionStage( mmpAccountRoot, 5 );
    LogUtil.outMessage( "Hello packet sent" );
    /**
     * MRIM_CS_HELLO_ACK
     */
    packet = receivePacket();
    pingDelay = DataUtil.get32_reversed( packet.data.byteString, 0, true ) * 1000;
    LogUtil.outMessage( "Hello ack received" );
    LogUtil.outMessage( ">> pingDelay = " + pingDelay );
    /**
     * Starting ping
     * MRIM_CS_PING
     */
    Thread thread = new Thread() {

      public void run() {
        while ( isAlive ) {
          try {
            sleep( MmpSession.this.pingDelay );
            Packet packet = new Packet();
            packet.seq = seqNum++;
            packet.msg = PacketType.MRIM_CS_PING;
            packet.send( netConnection );
          } catch ( Throwable ex ) {
            LogUtil.outMessage( ex.getMessage() );
          }
        }
      }
    };
    thread.setPriority( Thread.MIN_PRIORITY );
    thread.start();
    ActionExec.setConnectionStage( mmpAccountRoot, 6 );
    Thread httpPing = new Thread() {

      public void run() {
        while ( isAlive && MidletMain.httpHiddenPing > 0 ) {
          try {
            sleep( MidletMain.httpHiddenPing * 1000 );
            /** HTTP ping **/
            NetConnection.httpPing( "http://www.mail.ru" );
          } catch ( Throwable ex ) {
            LogUtil.outMessage( ex.getMessage() );
          }
        }
      }
    };
    httpPing.setPriority( Thread.MIN_PRIORITY );
    httpPing.start();
    ActionExec.setConnectionStage( mmpAccountRoot, 7 );
    /**
     * MRIM_CS_LOGIN2
     */
    packet = new Packet();
    packet.seq = seqNum++;
    packet.msg = PacketType.MRIM_CS_LOGIN2;
    packet.proto = 0x0001000e; // 0x0001000e
    packet.data.append( DataUtil.mmputil_prepareByteStringWthLength( userId ) );
    packet.data.append( DataUtil.mmputil_prepareByteStringWthLength( passwrd ) );
//    byte[] temp = new byte[4];
//    DataUtil.put32_reversed( temp, 0, statusId );
//    packet.data.append( temp );
//    packet.data.append( DataUtil.mmputil_prepareByteStringWthLength( "STATUS_ONLINE" ) );
//    packet.data.append( DataUtil.mmputil_prepareByteStringWthLength( statusString ) );
//    packet.data.append( DataUtil.mmputil_prepareByteStringWthLength( descrString ) );
//    //packet.data.append(HexUtil.stringToBytes("3600000028000000636C69656E743D226A6167656E7422206E616D653D224D5241222076657273696F6E3D22312E34220A0000004D524120312E342E3336"));
//    temp = new byte[4];
//    DataUtil.put32_reversed( temp, 0, clientId.length() + mraVer.length() );
//    packet.data.append( temp );
//    packet.data.append( DataUtil.mmputil_prepareByteStringWthLength( clientId ) );
//    packet.data.append( DataUtil.mmputil_prepareByteStringWthLength( mraVer ) );
    
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
    // DataUtil.put32_reversed( temp, 0, -1 );
    temp = new byte[4];
    DataUtil.put32_reversed( temp, 0, mmpAccountRoot.session.clientId.length() 
            + mmpAccountRoot.session.mraVer.length() );
    packet.data.append( temp );
    packet.data.append( DataUtil.mmputil_prepareByteStringWthLength( 
            mmpAccountRoot.session.clientId ) );
    packet.data.append( DataUtil.mmputil_prepareByteStringWthLength( 
            mmpAccountRoot.session.mraVer ) );
    packet.data.append( temp );
    
    packet.send( netConnection );
    LogUtil.outMessage( "Login sent" );
    ActionExec.setConnectionStage( mmpAccountRoot, 8 );
    /**
     * MRIM_CS_LOGIN_ACK
     */
    packet = receivePacket();
    if ( packet.msg == PacketType.MRIM_CS_LOGIN_ACK ) {
      LogUtil.outMessage( "Login ack received" );
      listener = new Thread( this );
      listener.start();
      return true;
    } else if ( packet.msg == PacketType.MRIM_CS_LOGIN_REJ ) {
      LogUtil.outMessage( "Login rejected" );
    }
    ActionExec.setConnectionStage( mmpAccountRoot, 9 );
    return false;
  }

  public Packet receivePacket() throws IOException, InterruptedException, java.lang.IndexOutOfBoundsException {
    Packet packet = new Packet();
    byte[] data = netConnection.read( 44 );
    if ( data.length >= 44 ) {
      packet.parseHeader( data ); // Reading header
      // LogUtil.outMessage(">> Header received");
      // LogUtil.outMessage("   recv. head: " + HexUtil.bytesToString(data));
      packet.data.byteString = netConnection.read( ( int ) packet.dlen );
      // LogUtil.outMessage("   recv. data: " + HexUtil.bytesToString(packet.data.byteString));
      return packet;
    } else {
      LogUtil.outMessage( ">> Header size is less than 44" );
      // LogUtil.outMessage("   " + HexUtil.bytesToString(data));
      return null;
    }
  }

  public void disconnect() {
    isAlive = false;
  }

  public void run() {
    isError = false;
    while ( isAlive ) {
      try {
        MmpPacketParser.parsePacket( mmpAccountRoot, receivePacket() );
        if ( MidletMain.pack_count > MidletMain.pack_count_invoke_gc ) {
          System.gc();
          MidletMain.pack_count = 0;
        }
        MidletMain.pack_count++;
      } catch ( Throwable ex ) {
        if ( isAlive ) {
          isError = true;
          isAlive = false;
          LogUtil.outMessage( ex.getMessage(), true );
        }
      }
    }
    try {
      netConnection.disconnect();
    } catch ( Throwable ex ) {
      LogUtil.outMessage( "Disconnect failed: " + this.toString(), true );
    }
    long prevStatus = mmpAccountRoot.statusId;
    ActionExec.disconnectEvent( mmpAccountRoot );
    if ( MidletMain.autoReconnect && isError ) {
      try {
        Thread.sleep( MidletMain.reconnectTime );
      } catch ( InterruptedException ex ) {
      }
      mmpAccountRoot.connectAction( prevStatus );
    }
  }

  void setNetConnection( NetConnection netConnection ) {
    this.netConnection = netConnection;
  }
}
