package com.tomclaw.mandarin.xmpp;

import com.tomclaw.mandarin.dc.DirectConnection;
import com.tomclaw.mandarin.core.Handler;
import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.utils.ArrayUtil;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;
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
public class XmppIBBytestream implements DirectConnection {

  public String proxyIp = null;
  public byte[] proxyIpBytes = null;
  public int proxyPort = -1;
  public byte[] fileName = null;
  public long fileByteSize = -1;
  public String buddyId;
  public byte[] icbmCookie = new byte[8];
  public boolean isReceivingFile = false;
  public boolean isComplete = false;
  public boolean isError = false;
  public String statusString = "NO_STATUS";
  public int percentValue = 0;
  public int speed = 0;
  public int blockSize = 4096;
  public String fileLocation = null;
  public boolean isStop = false;
  public XmppAccountRoot xmppAccountRoot = null;
  public OutputStream outputStream = null;
  public FileConnection fileConnection = null;
  public long startTime = 0;
  public long fileRecvBytes = 0;

  public XmppIBBytestream( XmppAccountRoot xmppAccountRoot ) {
    this.xmppAccountRoot = xmppAccountRoot;
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
    XmppSender.ibbClose( xmppAccountRoot.xmppSession, buddyId, "tansactionabort_".concat( xmppAccountRoot.xmppSession.getId() ), StringUtil.byteArrayToString( icbmCookie ) );
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
    if ( directConnection instanceof XmppIBBytestream ) {
      if ( ArrayUtil.equals( ( ( XmppIBBytestream ) directConnection ).icbmCookie, icbmCookie ) ) {
        return true;
      }
    }
    return false;
  }

  public byte[] getSessCookie() {
    return icbmCookie;
  }

  public void sendFile() throws IOException, InterruptedException {
    statusString = "SENDING_REQUEST";
    Handler.updateTransactions( xmppAccountRoot );
    Handler.updateTransactionInfo( xmppAccountRoot, icbmCookie );
    LogUtil.outMessage( "Sending file thru XMPP" );
    XmlWriter xmlWriter = xmppAccountRoot.xmppSession.xmlWriter;
    String sid = StringUtil.byteArrayToString( icbmCookie );
    String toJid = buddyId;
    String desc = "file:///".concat( fileLocation ).concat( StringUtil.byteArrayToString( fileName, true ) );

    xmlWriter.startTag( "iq" );
    xmlWriter.attribute( "type", "set" );
    xmlWriter.attribute( "to", toJid );
    xmlWriter.attribute( "id", sid );

    xmlWriter.startTag( "si" );
    xmlWriter.attribute( "xmlns", "http://jabber.org/protocol/si" );
    xmlWriter.attribute( "id", sid );
    xmlWriter.attribute( "mime-type", "binary/octet-stream" );
    xmlWriter.attribute( "profile", "http://jabber.org/protocol/si/profile/file-transfer" );

    xmlWriter.startTag( "file" );
    xmlWriter.attribute( "xmlns", "http://jabber.org/protocol/si/profile/file-transfer" );
    xmlWriter.attribute( "name", StringUtil.byteArrayToString( fileName, true ) );
    xmlWriter.attribute( "size", String.valueOf( fileByteSize ) );

    xmlWriter.startTag( "desc" );
    xmlWriter.text( desc );
    xmlWriter.endTag();

    xmlWriter.endTag();

    xmlWriter.startTag( "feature" );
    xmlWriter.attribute( "xmlns", "http://jabber.org/protocol/feature-neg" );
    xmlWriter.startTag( "x" );
    xmlWriter.attribute( "xmlns", "jabber:x:data" );
    xmlWriter.attribute( "type", "form" );
    xmlWriter.startTag( "field" );
    xmlWriter.attribute( "var", "stream-method" );
    xmlWriter.attribute( "type", "list-single" );
    xmlWriter.startTag( "option" );
    xmlWriter.startTag( "value" );
    xmlWriter.text( "http://jabber.org/protocol/ibb" );
    xmlWriter.endTag();
    xmlWriter.endTag();
    xmlWriter.endTag();
    xmlWriter.endTag();
    xmlWriter.endTag();
    xmlWriter.endTag();
    xmlWriter.endTag();

    xmlWriter.flush();

    statusString = "ACK_WAITING";
    Handler.updateTransactions( xmppAccountRoot );
    Handler.updateTransactionInfo( xmppAccountRoot, icbmCookie );
  }

  public void setTransactionInfo( byte[] fileName, String fileLocation, long fileByteSize, String buddyId ) {
    this.fileName = fileName;
    this.fileLocation = fileLocation;
    this.fileByteSize = fileByteSize;
    this.buddyId = buddyId;
  }

  public void generateCookie() {
    String fullTime = String.valueOf( System.currentTimeMillis() / 100 );
    icbmCookie = StringUtil.stringToByteArray( fullTime.substring( fullTime.length() - 8 ), true );
  }

  public void sendStreamOpen() {
    try {
      statusString = "STREAM_OPEN";
      LogUtil.outMessage( "Opening inbound bytestream" );
      XmppSender.ibbOpen( xmppAccountRoot.xmppSession, buddyId, "transaction_".concat( StringUtil.byteArrayToString( icbmCookie ) ), StringUtil.byteArrayToString( icbmCookie ), blockSize, "message" );
    } catch ( IOException ex ) {
      isError = true;
      statusString = "IO_EXCEPTION";
      this.isError = true;
    }
    Handler.updateTransactions( xmppAccountRoot );
    Handler.updateTransactionInfo( xmppAccountRoot, icbmCookie );
  }

  public void startTransfer() {
    LogUtil.outMessage( "Starting transfer" );
    String sid = StringUtil.byteArrayToString( icbmCookie );
    fileConnection = null;
    InputStream inputStream;
    try {
      LogUtil.outMessage( "Full file path: ".concat( "file://" + fileLocation + StringUtil.byteArrayToString( fileName, true ) ) );
      fileConnection = ( FileConnection ) Connector.open( "file://" + fileLocation + StringUtil.byteArrayToString( fileName, true ), Connector.READ );
      if ( !fileConnection.exists() ) {
        LogUtil.outMessage( "File not exist", true );
        statusString = "FILE_NOT_EXIST";
        this.isError = true;
        Handler.updateTransactions( xmppAccountRoot );
        Handler.updateTransactionInfo( xmppAccountRoot, icbmCookie );
        return;
      }
      inputStream = fileConnection.openInputStream();

      long fileSentBytes = 0;
      byte[] buffer = new byte[blockSize];
      int bufferRead;
      startTime = System.currentTimeMillis();
      int seq = 0;
      statusString = "TRANSFERING_FILE";
      Handler.updateTransactions( xmppAccountRoot );
      Handler.updateTransactionInfo( xmppAccountRoot, icbmCookie );
      while ( fileSentBytes < fileByteSize ) {
        bufferRead = inputStream.read( buffer );
        if ( seq > 65535 ) {
          seq = 0;
        }
        XmppSender.sendIBBFileBlockMessage( xmppAccountRoot.xmppSession, buddyId, xmppAccountRoot.xmppSession.getId(), sid, seq, buffer, 0, bufferRead );
        seq++;
        MidletMain.incrementDataCount( bufferRead );
        fileSentBytes += bufferRead;
        percentValue = ( int ) ( fileSentBytes * 100 / fileByteSize );
        if ( System.currentTimeMillis() - startTime > 1000 ) {
          speed = ( int ) ( 8 * 1000 * fileSentBytes / ( System.currentTimeMillis() - startTime ) ) / 1024;
        }
        Handler.updateTransactionInfo( xmppAccountRoot, icbmCookie );
        if ( isStop ) {
          inputStream.close();
          fileConnection.close();
          isError = true;
          statusString = "STOPPED";
          Handler.updateTransactions( xmppAccountRoot );
          Handler.updateTransactionInfo( xmppAccountRoot, icbmCookie );
          return;
        }
      }

    } catch ( Throwable ex1 ) {
      LogUtil.outMessage( "Local file error: " + ex1.getMessage(), true );
      statusString = "LOCAL_ERROR";
      this.isError = true;
      Handler.updateTransactions( xmppAccountRoot );
      Handler.updateTransactionInfo( xmppAccountRoot, icbmCookie );
      return;
    }
    LogUtil.outMessage( "Transfering complete" );
    statusString = "TRANSFERING_COMPLETE";
    isComplete = true;
    Handler.updateTransactionInfo( xmppAccountRoot, icbmCookie );
    Handler.updateTransactions( xmppAccountRoot );
    try {
      XmppSender.ibbClose( xmppAccountRoot.xmppSession, buddyId, "tansactioncomplete_".concat( xmppAccountRoot.xmppSession.getId() ), sid );
    } catch ( IOException ex ) {
      LogUtil.outMessage( "Couldn't complete transaction by final packet" );
      statusString = "IO_EXCEPTION";
      this.isError = true;
      Handler.updateTransactions( xmppAccountRoot );
      Handler.updateTransactionInfo( xmppAccountRoot, icbmCookie );
    }
  }

  public void receiveFile( String id ) throws IOException {
    statusString = "SENDING_PARAMS";
    Handler.updateTransactions( xmppAccountRoot );
    Handler.updateTransactionInfo( xmppAccountRoot, icbmCookie );
    isReceivingFile = true;
    XmlWriter xmlWriter = xmppAccountRoot.xmppSession.xmlWriter;

    xmlWriter.startTag( "iq" );
    xmlWriter.attribute( "type", "result" );
    xmlWriter.attribute( "to", buddyId );
    xmlWriter.attribute( "id", id );

    xmlWriter.startTag( "si" );
    xmlWriter.attribute( "xmlns", "http://jabber.org/protocol/si" );

    xmlWriter.startTag( "feature" );
    xmlWriter.attribute( "xmlns", "http://jabber.org/protocol/feature-neg" );
    xmlWriter.startTag( "x" );
    xmlWriter.attribute( "xmlns", "jabber:x:data" );
    xmlWriter.attribute( "type", "submit" );
    xmlWriter.startTag( "field" );
    xmlWriter.attribute( "var", "stream-method" );
    xmlWriter.startTag( "value" );
    xmlWriter.text( "http://jabber.org/protocol/ibb" );
    xmlWriter.endTag();
    xmlWriter.endTag();
    xmlWriter.endTag();
    xmlWriter.endTag();
    xmlWriter.endTag();
    xmlWriter.endTag();

    xmlWriter.flush();
    statusString = "RECEIVE_READY";
    Handler.updateTransactions( xmppAccountRoot );
    Handler.updateTransactionInfo( xmppAccountRoot, icbmCookie );
  }

  public void setParamsAndAck( String blockSize, String id ) throws IOException {
    this.blockSize = Integer.parseInt( blockSize );
    XmlWriter xmlWriter = xmppAccountRoot.xmppSession.xmlWriter;

    xmlWriter.startTag( "iq" );
    xmlWriter.attribute( "type", "result" );
    xmlWriter.attribute( "to", buddyId );
    xmlWriter.attribute( "id", id );
    xmlWriter.endTag();
    xmlWriter.flush();

    fileConnection = null;
    try {
      fileConnection = ( FileConnection ) Connector.open( "file://" + MidletMain.incomingFilesFolder + StringUtil.byteArrayToString( fileName, true ), Connector.READ_WRITE );
      if ( fileConnection.exists() ) {
        fileConnection.delete();
      }
      fileConnection.create();
      outputStream = fileConnection.openOutputStream();
      startTime = System.currentTimeMillis();
      fileRecvBytes = 0;
      statusString = "RECEIVE_READY";
    } catch ( Throwable ex1 ) {
      LogUtil.outMessage( "Local file error: " + ex1.getMessage(), true );
      statusString = "LOCAL_ERROR";
      this.isError = true;
    }
    Handler.updateTransactions( xmppAccountRoot );
    Handler.updateTransactionInfo( xmppAccountRoot, icbmCookie );
  }

  public void storeData( String seq, byte[] decode ) {
    try {
      fileRecvBytes += decode.length;
      outputStream.write( decode );
      outputStream.flush();
      percentValue = ( int ) ( fileRecvBytes * 100 / fileByteSize );
      if ( System.currentTimeMillis() - startTime > 1000 ) {
        speed = ( int ) ( 8 * 1000 * fileRecvBytes / ( System.currentTimeMillis() - startTime ) ) / 1024;
      }
      Handler.updateTransactionInfo( xmppAccountRoot, icbmCookie );
      if ( isStop ) {
        outputStream.close();
        fileConnection.close();
        isError = true;
        statusString = "STOPPED";
        Handler.updateTransactions( xmppAccountRoot );
      } else {
        statusString = "STORING_BLOCK";
      }
    } catch ( IOException ex ) {
      statusString = "LOCAL_ERROR";
      this.isError = true;
    }
    Handler.updateTransactions( xmppAccountRoot );
    Handler.updateTransactionInfo( xmppAccountRoot, icbmCookie );
  }

  public void closeFileAndAck( String id ) throws IOException {
    outputStream.flush();
    outputStream.close();
    fileConnection.close();

    LogUtil.outMessage( "Transfering complete" );
    statusString = "TRANSFERING_COMPLETE";
    isComplete = true;
    Handler.updateTransactionInfo( xmppAccountRoot, icbmCookie );
    Handler.updateTransactions( xmppAccountRoot );

    XmlWriter xmlWriter = xmppAccountRoot.xmppSession.xmlWriter;

    xmlWriter.startTag( "iq" );
    xmlWriter.attribute( "type", "result" );
    xmlWriter.attribute( "to", buddyId );
    xmlWriter.attribute( "id", id );
    xmlWriter.endTag();
    xmlWriter.flush();
  }
}
