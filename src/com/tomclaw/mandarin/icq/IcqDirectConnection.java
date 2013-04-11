package com.tomclaw.mandarin.icq;

import com.tomclaw.mandarin.core.Handler;
import com.tomclaw.mandarin.dc.DirectConnection;
import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.mandarin.net.NetConnection;
import com.tomclaw.utils.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class IcqDirectConnection implements DirectConnection {

  public String proxyIp = null;
  public byte[] proxyIpBytes = null;
  public int proxyPort = -1;
  public byte[] fileName = null;
  public long fileByteSize = -1;
  public String buddyId;
  public byte[] icbmCookie = new byte[ 8 ];
  public NetConnection localProxyConnection = null;
  public boolean isReceivingFile = false;
  public byte seqNumber = 1;
  public boolean remoteProxyConnectionSentFlag = false;
  public boolean isComplete = false;
  public boolean isError = false;
  public String statusString = "NO_STATUS";
  public int percentValue = 0;
  public int speed = 0;
  public int bufferSize = 512;
  public String fileLocation = null;
  public boolean isStop = false;
  public IcqAccountRoot icqAccountRoot = null;

  public IcqDirectConnection( IcqAccountRoot icqAccountRoot ) {
    this.icqAccountRoot = icqAccountRoot;
  }

  public String getBuddyId() {
    return buddyId;
  }

  public byte[] getFileName() {
    return fileName;
  }

  public long getFileByteSize() {
    return fileByteSize;
  }

  public String getProxyIp() {
    return proxyIp;
  }

  public int getProxyPort() {
    return proxyPort;
  }

  public String getStatusString() {
    return statusString;
  }

  public int getPercentValue() {
    return percentValue;
  }

  public int getSpeed() {
    return speed;
  }

  public void sendStop() throws IOException {
    sendMessageType( ( byte ) 0x01 );
  }

  public boolean isErrorFlag() {
    return isError;
  }

  public boolean isReceivingFileFlag() {
    return isReceivingFile;
  }

  public boolean isCompleteFlag() {
    return isComplete;
  }

  public boolean isStopFlag() {
    return isStop;
  }

  public void setIsReceivingFile( boolean isReceivingFile ) {
    this.isReceivingFile = isReceivingFile;
  }

  public boolean equals( DirectConnection directConnection ) {
    if ( directConnection instanceof IcqDirectConnection ) {
      if ( ArrayUtil.equals( ( ( IcqDirectConnection ) directConnection ).icbmCookie, icbmCookie ) ) {
        return true;
      }
    }
    return false;
  }

  public byte[] getSessCookie() {
    return icbmCookie;
  }

  public void sendFile() throws IOException, InterruptedException {
    sendFileViaProxy();
    waitForProxyReady();
  }

  public void setTransactionInfo( byte[] fileName, String fileLocation, long fileByteSize, String buddyId ) {
    this.fileName = fileName;
    this.fileLocation = fileLocation;
    this.fileByteSize = fileByteSize;
    this.buddyId = buddyId;
  }

  public void generateCookie() {
    DataUtil.nextBytes( icbmCookie );
  }

  public void sendMessageType( byte msgType ) throws IOException {
    statusString = "SEND_FILE_ACCEPT";
    Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );
    LogUtil.outMessage( "sendFileAccept" );
    LogUtil.outMessage( "buddyId=" + buddyId );
    LogUtil.outMessage( "icbmCookie=" + HexUtil.bytesToString( icbmCookie ) );

    Snac p = new Snac( 0x0004, 0x0006, 0, 0, 7 );
    p.addByteArray( icbmCookie );// ICBM Cookie: D73E594C98250000
    p.addWord( 0x0002 ); // Message Channel ID: 0x0002
    // Uin
    p.addByte( buddyId.length() );
    p.addByteArray( buddyId.getBytes() );
    // TLV: Rendez Vous Data
    p.addWord( 0x0005 );

    ArrayUtil buffer = new ArrayUtil();
    buffer.append( new byte[]{ 0x00, msgType } ); // Message Type: Accept (0x0002)
    buffer.append( icbmCookie ); // ICBM Cookie: D73E594C98250000
    buffer.append( new byte[]{ // Send File {09461343-4c7f-11d1-8222-444553540000}
              ( byte ) 0x09, ( byte ) 0x46, ( byte ) 0x13, ( byte ) 0x43,
              ( byte ) 0x4c, ( byte ) 0x7f, ( byte ) 0x11, ( byte ) 0xd1,
              ( byte ) 0x82, ( byte ) 0x22, ( byte ) 0x44, ( byte ) 0x45,
              ( byte ) 0x53, ( byte ) 0x54, ( byte ) 0x00, ( byte ) 0x00
            } );

    p.addWord( buffer.length() );
    p.addByteArray( buffer.byteString );

    try {
      p.send( icqAccountRoot.session.getNetworkConnection().outputStream, icqAccountRoot.session.getNextSeq() );
      if ( msgType == 0x02 ) {
        statusString = "FILE_ACCEPT_SENT";
      } else if ( msgType == 0x01 ) {
        statusString = "STOPPED";
        isStop = true;
      }
      Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );
    } catch ( IOException ex1 ) {
      statusString = "IO_EXCEPTION";
      isError = true;
      Handler.updateTransactions( icqAccountRoot );
      Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );
    }
  }

  public void requestProxyIpPort() throws IOException, InterruptedException {
    statusString = "REQUEST_PROXY";
    Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );

    localProxyConnection = new NetConnection();
    localProxyConnection.connectAddress( "ars.icq.com", 5190 ); //dcTcpPort
    LogUtil.outMessage( "Connected to ars.icq.com" );

    // Request proxy server packet
    Snac snac = new Snac( 0, 0, 0, 0 );
    snac.addWord( 0x044a );
    snac.addWord( 0x0002 ); // Command type
    snac.addWord( 0x0000 );
    snac.addWord( 0x0000 );
    snac.addWord( 0x0000 ); // Flags
    snac.addByte( icqAccountRoot.userId.length() ); // SnLen
    snac.addByteArray( icqAccountRoot.userId.getBytes() ); //Sn
    snac.addByteArray( icbmCookie ); // Cookie
    snac.addWord( 0x0001 );
    snac.addWord( 0x0010 );
    snac.addByteArray( new byte[]{ // Send File {09461343-4c7f-11d1-8222-444553540000}
              ( byte ) 0x09, ( byte ) 0x46, ( byte ) 0x13, ( byte ) 0x43,
              ( byte ) 0x4c, ( byte ) 0x7f, ( byte ) 0x11, ( byte ) 0xd1,
              ( byte ) 0x82, ( byte ) 0x22, ( byte ) 0x44, ( byte ) 0x45,
              ( byte ) 0x53, ( byte ) 0x54, ( byte ) 0x00, ( byte ) 0x00
            } );
    ArrayUtil proxyPacket = new ArrayUtil();
    byte[] packetSize = new byte[ 2 ];
    DataUtil.put16( packetSize, 0, snac.getByteArray().toByteArray().length - 10 );
    proxyPacket.append( packetSize );
    proxyPacket.append( new ArrayUtil( snac.getByteArray().toByteArray() ).subarray( 10, snac.getByteArray().toByteArray().length ) );
    localProxyConnection.write( proxyPacket.byteString );
    localProxyConnection.flush();

    byte[] proxyServerPacket;
    proxyServerPacket = localProxyConnection.read( 2 );
    int length = DataUtil.get16( proxyServerPacket, 0 );
    proxyServerPacket = localProxyConnection.read( length );
    if ( MidletMain.logLevel == 1 ) {
      HexUtil.dump_( proxyServerPacket, "proxyServerPacket " );
    }

    int offset = 0;
    int protVer = DataUtil.get16( proxyServerPacket, offset );
    int cmdType = DataUtil.get16( proxyServerPacket, offset += 2 );
    int unk1 = DataUtil.get16( proxyServerPacket, offset += 2 );
    int unk2 = DataUtil.get16( proxyServerPacket, offset += 2 );
    int flags = DataUtil.get16( proxyServerPacket, offset += 2 );
    if ( cmdType == 0x0003 ) {
      proxyPort = DataUtil.get16( proxyServerPacket, offset += 2 );
      proxyIpBytes = DataUtil.getByteArray( proxyServerPacket, offset += 2, 4 );
      proxyIp = DataUtil.get8int( proxyIpBytes, 0 ) + "." + DataUtil.get8int( proxyIpBytes, 1 ) + "." + DataUtil.get8int( proxyIpBytes, 2 ) + "." + DataUtil.get8int( proxyIpBytes, 3 );
      statusString = "REQUEST_RETREIVED"; // Needs to be tested
      Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );
    } else {
      // ERROR
      statusString = "PROXY_ERROR";
      isError = true;
      Handler.updateTransactions( icqAccountRoot );
      Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );
    }
    // dcConnection.disconnect();
  }

  public void sendFileViaProxy() throws IOException, InterruptedException {
    statusString = "SENDING_REQUEST";
    Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );

    Snac p = new Snac( 0x0004, 0x0006, 0, 0, 7 );
    requestProxyIpPort();
    p.addByteArray( icbmCookie );// ICBM Cookie: D73E594C98250000
    p.addWord( 0x0002 ); // Message Channel ID: 0x0002
    // Uin
    p.addByte( buddyId.length() );
    p.addByteArray( buddyId.getBytes() );
    // TLV: Rendez Vous Data
    p.addWord( 0x0005 );

    ArrayUtil buffer = new ArrayUtil();
    buffer.append( new byte[]{ 0x00, 0x00 } ); // Message Type: Request (0x0000)
    buffer.append( icbmCookie ); // ICBM Cookie: D73E594C98250000
    buffer.append( new byte[]{ // Send File {09461343-4c7f-11d1-8222-444553540000}
              ( byte ) 0x09, ( byte ) 0x46, ( byte ) 0x13, ( byte ) 0x43,
              ( byte ) 0x4c, ( byte ) 0x7f, ( byte ) 0x11, ( byte ) 0xd1,
              ( byte ) 0x82, ( byte ) 0x22, ( byte ) 0x44, ( byte ) 0x45,
              ( byte ) 0x53, ( byte ) 0x54, ( byte ) 0x00, ( byte ) 0x00
            } );
    buffer.append( new byte[]{ // TLV: Sequence Number
              0x00, 0x0a,
              0x00, 0x02,
              0x00, seqNumber } );
    seqNumber++;

    buffer.append( new byte[]{ 0x00, 0x0f, 0x00, 0x00 } ); // TLV: Request Host Check

    buffer.append( new byte[]{ 0x00, 0x0d, 0x00, 0x05, } ); // TLV: Data MIME Type
    buffer.append( "utf-8".getBytes() ); // Value: utf-8

    // TLV: Invitation Text
    String invTextValue = "&lt;ICQ_COOL_FT&gt;&lt;FS&gt;" + new String( fileName ) + "&lt;/FS&gt;&lt;S&gt;" + fileByteSize + "&lt;/S&gt;&lt;SID&gt;1&lt;/SID&gt;&lt;DESC&gt;&lt;/DESC&gt;&lt;/ICQ_COOL_FT&gt;";
    byte[] invTextSize = new byte[ 2 ];
    DataUtil.put16( invTextSize, 0, invTextValue.length() );
    buffer.append( new byte[]{ 0x00, 0x0c } );
    buffer.append( invTextSize );
    buffer.append( invTextValue.getBytes() );

    // TLV: Rendezvous IP
    LogUtil.outMessage( "socket.getAddress()=" + proxyIp );
    byte[] rendezvouzIp = new byte[ 4 ];

    DataUtil.put8( rendezvouzIp, 0, proxyIpBytes[0] );
    DataUtil.put8( rendezvouzIp, 1, proxyIpBytes[1] );
    DataUtil.put8( rendezvouzIp, 2, proxyIpBytes[2] );
    DataUtil.put8( rendezvouzIp, 3, proxyIpBytes[3] );
    buffer.append( new byte[]{ 0x00, 0x02, 0x00, 0x04 } );
    buffer.append( rendezvouzIp );

    // TLV: External Port
    byte[] port = new byte[ 2 ];
    DataUtil.put16( port, 0, proxyPort );
    buffer.append( new byte[]{ 0x00, 0x05, 0x00, 0x02 } );
    buffer.append( port );

    // TLV: Data MIME Type
    byte[] mime; // = new byte[ 4 ];
    mime = new byte[]{ 0x00, 0x15, 0x00, 0x02 };
    byte[] mimeData = new byte[ 2 ];
    DataUtil.put16( mimeData, 0, 0xf668 );
    buffer.append( mime );
    buffer.append( mimeData );

    // TLV: Extended Data
    byte[] ext; // = new byte[ 4 ];
    ext = new byte[]{ 0x00, 0x01, 0x00, 0x01 };
    byte[] size = new byte[ 4 ];
    DataUtil.put32( size, 0, fileByteSize );
    buffer.append( new byte[]{ 0x27, 0x11 } );
    byte[] tlvSize = new byte[ 2 ];
    DataUtil.put16( tlvSize, 0, ext.length + size.length + fileName.length + 1 );

    buffer.append( tlvSize );
    buffer.append( ext );
    buffer.append( size );
    buffer.append( fileName );
    buffer.append( new byte[]{ 0x00 } );

    // TLV: File name encoding
    buffer.append( new byte[]{ 0x27, 0x12, 0x00, 0x05 } );
    buffer.append( "utf-8".getBytes() );

    // TLV: Request Data via Rendezvous Server
    buffer.append( new byte[]{ 0x00, 0x10, 0x00, 0x00 } );

    // TLV: Size
    buffer.append( new byte[]{ 0x27, 0x13, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00 } );
    buffer.append( size );

    p.addWord( buffer.length() );
    p.addByteArray( buffer.byteString );

    try {
      p.send( icqAccountRoot.session.getNetworkConnection().outputStream, 
              icqAccountRoot.session.getNextSeq() );
    } catch ( IOException ex1 ) {
      statusString = "IO_EXCEPTION";
      isError = true;
      Handler.updateTransactions( icqAccountRoot );
      Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );
    }
  }

  public void sendToCreatedProxy() {
    try {
      LogUtil.outMessage( "sendToCreatedProxy " + proxyIp + " and buddy " + buddyId );
      LogUtil.outMessage( proxyIp + ":" + proxyPort );
      LogUtil.outMessage( "No errors" );

      // Ready packet to proxy server
      statusString = "PROCESSING_FILEXFER";
      Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );

      processCoolFileXfer( localProxyConnection );
      return;
      //}
    } catch ( IOException ex ) {
      // ex.printStackTrace();
      LogUtil.outMessage( "IO Exception: " + ex.getMessage(), true );
    } catch ( InterruptedException ex ) {
      // ex.printStackTrace();
      LogUtil.outMessage( "InterruptedException: " + ex.getMessage(), true );
    }
    statusString = "IO_EXCEPTION";
    isError = true;
    Handler.updateTransactions( icqAccountRoot );
    Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );
  }

  public void sendToRemoteProxy( int[] externalIp, int dcTcpPort ) {
    DataUtil.put8( proxyIpBytes, 0, externalIp[0] );
    DataUtil.put8( proxyIpBytes, 1, externalIp[1] );
    DataUtil.put8( proxyIpBytes, 2, externalIp[2] );
    DataUtil.put8( proxyIpBytes, 3, externalIp[3] );
    this.proxyIp = externalIp[0] + "." + externalIp[1] + "." + externalIp[2] + "." + externalIp[3];
    this.proxyPort = dcTcpPort;
    sendToRemoteProxy( false );
  }

  public void sendToRemoteProxy( boolean isUseLocalConnection ) {
    // Updating proxy data
    statusString = "CONNECTING_PROXY";
    Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );

    LogUtil.outMessage( "sendToRemoteProxy " + proxyIp + " and buddy " + buddyId );
    remoteProxyConnectionSentFlag = true;
    try {
      NetConnection dcConnection;
      if ( !isUseLocalConnection ) {
        dcConnection = new NetConnection();
        LogUtil.outMessage( proxyIp + ":" + proxyPort );
        dcConnection.connectAddress( proxyIp, 5190 );//dcTcpPort 5190
      } else {
        dcConnection = localProxyConnection;
      }
      LogUtil.outMessage( "Connected to proxy" );
      if ( !isUseLocalConnection ) {// !isReceivingFile
        ArrayUtil buffer = new ArrayUtil();
        // Packet to proxy server
        buffer.append( new byte[]{ 0x04, 0x4a } );
        buffer.append( new byte[]{ 0x00, 0x04 } );// 2
        buffer.append( new byte[]{ 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 } );
        buffer.append( new byte[]{ ( byte ) icqAccountRoot.userId.length() } );
        buffer.append( icqAccountRoot.userId.getBytes() );
        byte[] portBytes = new byte[ 2 ];
        DataUtil.put16( portBytes, 0, proxyPort );
        buffer.append( portBytes );
        buffer.append( icbmCookie );
        buffer.append( new byte[]{ 0x00, 0x01, 0x00, 0x10 } );
        buffer.append( new byte[]{
                  ( byte ) 0x09, ( byte ) 0x46, ( byte ) 0x13, ( byte ) 0x43, //Send.f
                  ( byte ) 0x4C, ( byte ) 0x7F, ( byte ) 0x11, ( byte ) 0xD1,
                  ( byte ) 0x82, ( byte ) 0x22, ( byte ) 0x44, ( byte ) 0x45,
                  ( byte ) 0x53, ( byte ) 0x54, ( byte ) 0x00, ( byte ) 0x00 } );

        ArrayUtil wholePacket = new ArrayUtil();
        wholePacket.append( new byte[]{ 0x00, 0x00 } );
        DataUtil.put16( wholePacket.byteString, 0, buffer.length() );
        wholePacket.append( buffer.byteString );

        dcConnection.write( wholePacket.byteString );
        dcConnection.flush();
      }
      try {
        byte[] lengthData = dcConnection.read( 2 );
        int offset = 0;
        int length = DataUtil.get16( lengthData, offset );
        byte[] proxyData = dcConnection.read( length );
        int packVer = DataUtil.get16( proxyData, offset );
        int cmdType = DataUtil.get16( proxyData, offset += 2 );
        int unk1 = DataUtil.get16( proxyData, offset += 2 );
        int unk2 = DataUtil.get16( proxyData, offset += 2 );
        int flags = DataUtil.get16( proxyData, offset += 2 );
        if ( MidletMain.logLevel == 1 ) {
          HexUtil.dump_( proxyData, "proxyData " );
          LogUtil.outMessage( "packVer=" + packVer );
          LogUtil.outMessage( "cmdType=" + cmdType );
          LogUtil.outMessage( "unk1=" + unk1 );
          LogUtil.outMessage( "unk2=" + unk2 );
          LogUtil.outMessage( "flags=" + flags );
        }
        if ( cmdType == 0x0001 ) {
          int errCode = DataUtil.get16( proxyData, offset += 2 );
          LogUtil.outMessage( "errCode=" + cmdType );
          statusString = "PROXY_ERROR";
          isError = true;
          Handler.updateTransactions( icqAccountRoot );
          Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );
          return;
        } else {
          sendMessageType( ( byte ) 0x02 );
          // Ready packet to proxy server
          if ( isReceivingFile ) {
            processFileReceive( dcConnection );
          } else {
            processCoolFileXfer( dcConnection );
          }
          return;
        }
      } catch ( IOException ex ) {
        // ex.printStackTrace();
        LogUtil.outMessage( "Ltl IOException: " + ex.getMessage() );
      } catch ( InterruptedException ex ) {
        // ex.printStackTrace();
        LogUtil.outMessage( "InterruptedException: " + ex.getMessage() );
      }
    } catch ( IOException ex ) {
      // ex.printStackTrace();
      LogUtil.outMessage( "Ttl IOException: " + ex.getMessage() );
    }
    statusString = "IO_EXCEPTION";
    isError = true;
    Handler.updateTransactions( icqAccountRoot );
    Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );
  }

  public void processFileReceive( NetConnection dcConnection ) throws IOException, InterruptedException {
    statusString = "RECEIVING_FILE";
    Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );

    LogUtil.outMessage( "Ready to receive" );
    byte[] proxyServerPacket = dcConnection.read( 256 );
    if ( MidletMain.logLevel == 1 ) {
      HexUtil.dump_( proxyServerPacket, "proxyServerPacket " );
    }
    DataUtil.put16( proxyServerPacket, 0x06, 0x0202 );
    dcConnection.write( proxyServerPacket );
    dcConnection.flush();
    LogUtil.outMessage( "Acknowledge sent" );
    // proxyServerPacket = dcConnection.read(256);

    LogUtil.outMessage( "Receiving file: " + ( int ) fileByteSize );

    FileConnection fileConnection;
    OutputStream outputStream;
    try {
      fileConnection = ( FileConnection ) Connector.open( "file://" + MidletMain.incomingFilesFolder + StringUtil.byteArrayToString( fileName, true ), Connector.READ_WRITE );
      if ( fileConnection.exists() ) {
        fileConnection.delete();
      }
      fileConnection.create();
      outputStream = fileConnection.openOutputStream();
    } catch ( Throwable ex1 ) {
      LogUtil.outMessage( "Local file error: " + ex1.getMessage(), true );
      statusString = "LOCAL_ERROR";
      this.isError = true;
      Handler.updateTransactions( icqAccountRoot );
      Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );
      return;
    }

    long dataBytesRead;
    if ( fileByteSize > 8 ) {
      dataBytesRead = 8;
    } else {
      dataBytesRead = fileByteSize;
    }
    LogUtil.outMessage( "dataBytesRead=" + dataBytesRead );
    byte[] dataStart = dcConnection.read( ( int ) dataBytesRead );
    if ( ArrayUtil.equals( dataStart, new byte[]{ 0x4f, 0x46, 0x54, 0x32, 0x01, 0x00, 0x01, 0x01 }, ( int ) dataBytesRead ) ) {
      // OFT2 header from Miranda (unknown)
      dcConnection.read( 256 - ( int ) dataBytesRead );
    } else {
      // dataStart is a half of file in length: dataOffset
      outputStream.write( dataStart );
      outputStream.flush();
    }

    int receivedBytes;
    byte[] buffer = new byte[ bufferSize ];
    long startTime = System.currentTimeMillis();
    while ( dataBytesRead < fileByteSize ) {
      receivedBytes = dcConnection.inputStream.read( buffer );
      outputStream.write( buffer, 0, receivedBytes );
      outputStream.flush();
      MidletMain.incrementDataCount( receivedBytes );
      dataBytesRead += receivedBytes;
      percentValue = ( int ) ( dataBytesRead * 100 / fileByteSize );
      if ( System.currentTimeMillis() - startTime > 1000 ) {
        speed = ( int ) ( 8 * 1000 * dataBytesRead / ( System.currentTimeMillis() - startTime ) ) / 1024;
      }
      Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );
      if ( isStop ) {
        outputStream.close();
        fileConnection.close();
        dcConnection.disconnect();
        isError = true;
        Handler.updateTransactions( icqAccountRoot );
        //ActionExec.updateTransactions();
        return;
      }
    }
    outputStream.close();
    fileConnection.close();

    // Output to console: HexUtil.dump_(fileContain, "receivedData ");
    DataUtil.put16( proxyServerPacket, 0x06, 0x0204 );
    try {
      dcConnection.write( proxyServerPacket );
      dcConnection.flush();
    } catch ( IOException ex1 ) {
      /**
       * QIP is closing conection without listening
       * for receipment receive whole file data
       */
      // ...do nothing...
    }
    LogUtil.outMessage( "Receiving complete" );
    statusString = "RECEIVING_COMPLETE";
    isComplete = true;
    Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );
    Handler.updateTransactions( icqAccountRoot );
    dcConnection.disconnect();
  }

  public void waitForProxyReady() {
    statusString = "WAITING_FOR_PROXY_READY";
    Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );
    try {
      byte[] lengthData = localProxyConnection.read( 2 );
      int offset = 0;
      int length = DataUtil.get16( lengthData, offset );
      byte[] proxyData = localProxyConnection.read( length );
      int packVer = DataUtil.get16( proxyData, offset );
      int cmdType = DataUtil.get16( proxyData, offset += 2 );
      int unk1 = DataUtil.get16( proxyData, offset += 2 );
      int unk2 = DataUtil.get16( proxyData, offset += 2 );
      int flags = DataUtil.get16( proxyData, offset += 2 );
      if ( MidletMain.logLevel == 1 ) {
        HexUtil.dump_( proxyData, "proxyData " );
        LogUtil.outMessage( "packVer=" + packVer );
        LogUtil.outMessage( "cmdType=" + cmdType );
        LogUtil.outMessage( "unk1=" + unk1 );
        LogUtil.outMessage( "unk2=" + unk2 );
        LogUtil.outMessage( "flags=" + flags );
      }
      if ( cmdType == 0x0001 ) {
        int errCode = DataUtil.get16( proxyData, offset += 2 );
        LogUtil.outMessage( "errCode=" + cmdType );
        // ERROR
        statusString = "PROXY_ERROR";
        isError = true;
        Handler.updateTransactions( icqAccountRoot );
        Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );
      } else if ( cmdType == 0x0005 ) {
        sendToCreatedProxy();
      }
      return;
    } catch ( IOException ex ) {
      // ex.printStackTrace();
      LogUtil.outMessage( "IOException: " + ex.getMessage(), true );
    } catch ( InterruptedException ex ) {
      // ex.printStackTrace();
      LogUtil.outMessage( "InterruptedException: " + ex.getMessage(), true );
    } catch ( IndexOutOfBoundsException ex ) {
      // ex.printStackTrace();
      LogUtil.outMessage( "IndexOutOfBoundsException: " + ex.getMessage(), true );
    }
    statusString = "IO_EXCEPTION";
    isError = true;
    Handler.updateTransactions( icqAccountRoot );
    Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );
  }

  public void processCoolFileXfer( NetConnection dcConnection ) throws IOException, InterruptedException {
    statusString = "SENDING_HEADER";
    Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );

    Snac snac = new Snac( 0, 0, 0, 0 );
    snac.addDWord( 0x4f465432 ); // Protocol version
    snac.addWord( 0x0100 ); // Length
    snac.addWord( 0x0101 ); // Type: Prompt
    snac.addByteArray( icbmCookie ); // ICBM cookie

    snac.addWord( 0x0000 ); // Encrypt
    snac.addWord( 0x0000 ); // Comp
    snac.addWord( 0x0001 ); // Total files
    snac.addWord( 0x0001 ); // Files left
    snac.addWord( 0x0001 ); // Total parts
    snac.addWord( 0x0001 ); // Parts left
    snac.addDWord( fileByteSize ); // Total size

    snac.addDWord( fileByteSize ); // Size
    snac.addDWord( 0x42d937c4 ); // Mod time
    snac.addDWord( 0xeeaf0000 ); // Checksum
    snac.addDWord( 0xffff0000 ); // RfrcSum

    snac.addDWord( 0x00000000 ); // RfSize
    snac.addDWord( 0x00000000 ); // CreTime
    snac.addDWord( 0xffff0000 ); // RfcSum
    snac.addDWord( 0x00000000 ); // nRecvd

    snac.addDWord( 0xffff0000 ); // RecvCsum
    snac.addByteArray( "Cool FileXfer".getBytes() ); // IDString
    snac.addByteArray( new byte[]{ // IDString c'td
              0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
              0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            } );

    snac.addDWord( 0x00000000 ); // IDString c'td
    snac.addByte( 0x20 ); // Flags
    snac.addByte( 0x1c ); // NameOff
    snac.addByte( 0x11 ); // SizeOff
    snac.addByteArray( new byte[]{ // Dummy
              0x00, 0x00, 0x00, 0x00,
              0x00, 0x00, 0x00, 0x00,
              0x00
            } );

    snac.addByteArray( new byte[]{ // Dummy (c'td)
              0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
              0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            } );

    snac.addByteArray( new byte[]{ // Dummy (c'td)
              0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
              0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            } );

    snac.addByteArray( new byte[]{ // Dummy (c'td)
              0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
              0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            } );

    snac.addByteArray( new byte[]{ // Dummy (c'td)
              0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
              0x00, 0x00, 0x00, 0x00, } );
    snac.addByteArray( new byte[]{ //MacFileInfo
              0x00, 0x00
            } );

    snac.addByteArray( new byte[]{ //MacFileInfo
              0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
              0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            } );
    snac.addWord( 0x0000 ); // Encoding
    snac.addWord( 0x0000 ); // Subtype

    snac.addByteArray( fileName ); // Filename
    snac.addByteArray( new byte[ 64 - fileName.length ] );

    dcConnection.write( new ArrayUtil( snac.getByteArray().toByteArray() ).subarray( 10, snac.getByteArray().toByteArray().length ) );
    dcConnection.flush();

    byte[] proxyServerPacket = dcConnection.read( 256 );
    
    if ( MidletMain.logLevel == 1 ) {
      HexUtil.dump_( proxyServerPacket, "proxyServerPacket " );
    }

    statusString = "TRANSFERING_FILE";
    Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );

    FileConnection fileConnection;
    InputStream inputStream;
    try {
      LogUtil.outMessage( "Full file path: ".concat( "file://" + fileLocation + StringUtil.byteArrayToString( fileName, true ) ) );
      fileConnection = ( FileConnection ) Connector.open( "file://" + fileLocation + StringUtil.byteArrayToString( fileName, true ), Connector.READ );
      if ( !fileConnection.exists() ) {
        LogUtil.outMessage( "File not exist", true );
        statusString = "FILE_NOT_EXIST";
        this.isError = true;
        Handler.updateTransactions( icqAccountRoot );
        Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );
        return;
      }
      inputStream = fileConnection.openInputStream();
    } catch ( Throwable ex1 ) {
      LogUtil.outMessage( "Local file error: " + ex1.getMessage(), true );
      statusString = "LOCAL_ERROR";
      this.isError = true;
      Handler.updateTransactions( icqAccountRoot );
      Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );
      return;
    }

    long fileSentBytes = 0;
    byte[] buffer = new byte[ bufferSize ];
    int bufferRead;
    long startTime = System.currentTimeMillis();
    while ( fileSentBytes < fileByteSize ) {
      bufferRead = inputStream.read( buffer );
      dcConnection.write( buffer, 0, bufferRead );
      dcConnection.flush();
      MidletMain.incrementDataCount( bufferRead );
      fileSentBytes += bufferRead;
      percentValue = ( int ) ( fileSentBytes * 100 / fileByteSize );
      if ( System.currentTimeMillis() - startTime > 1000 ) {
        speed = ( int ) ( 8 * 1000 * fileSentBytes / ( System.currentTimeMillis() - startTime ) ) / 1024;
      }
      Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );
      if ( isStop ) {
        inputStream.close();
        fileConnection.close();
        dcConnection.disconnect();
        isError = true;
        Handler.updateTransactions( icqAccountRoot );
        //ActionExec.updateTransactions(icqAccountRoot);
        return;
      }
    }

    LogUtil.outMessage( "Transfering complete" );
    statusString = "TRANSFERING_COMPLETE";
    isComplete = true;
    Handler.updateTransactionInfo( icqAccountRoot, icbmCookie );
    Handler.updateTransactions( icqAccountRoot );

    proxyServerPacket = dcConnection.read( 256 );
    if ( MidletMain.logLevel == 1 ) {
      HexUtil.dump_( proxyServerPacket, "proxyServerPacket " );
    }
    dcConnection.disconnect();
  }
}
