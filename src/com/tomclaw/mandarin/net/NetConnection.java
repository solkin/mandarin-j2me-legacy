package com.tomclaw.mandarin.net;

import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.utils.ArrayUtil;
import com.tomclaw.utils.LogUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.SecureConnection;
import javax.microedition.io.SocketConnection;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class NetConnection {

  private SocketConnection socket = null;
  public OutputStream outputStream = null;
  public InputStream inputStream = null;

  public void connectAddress( String host, int port ) throws IOException {
    connectAddress( host, port, false );
  }

  public void connectAddress( String host, int port, boolean isUseSsl )
          throws IOException {
    if ( isUseSsl ) {
      socket = ( SecureConnection ) Connector.open( "ssl://" + host + ":"
              + port, Connector.READ_WRITE );
    } else {
      socket = ( SocketConnection ) Connector.open( "socket://" + host + ":"
              + port, Connector.READ_WRITE );
    }
    outputStream = socket.openOutputStream();
    inputStream = socket.openInputStream();
    LogUtil.outMessage( "Connected successfull" );
  }

  public void connectAddress( String hostPort )
          throws IncorrectAddressException, IOException {
    String host;
    int port;
    int dividor = hostPort.lastIndexOf( ':' );
    if ( dividor != -1 ) {
      host = hostPort.substring( 0, dividor );
      try {
        port = Integer.parseInt(
                hostPort.substring( dividor + 1, hostPort.length() ) );
      } catch ( java.lang.NumberFormatException ex1 ) {
        throw new IncorrectAddressException();
      }
      connectAddress( host, port );
    } else {
      throw new IncorrectAddressException();
    }
  }

  public void disconnect() throws IOException {
    outputStream.close();
    inputStream.close();
    socket.close();
  }

  public void write( byte[] data ) throws IOException {
    outputStream.write( data );
    MidletMain.incrementDataCount( data.length );
  }

  public void write( byte[] data, int from, int size ) throws IOException {
    outputStream.write( data, from, size );
    MidletMain.incrementDataCount( data.length );
  }

  public void flush() throws IOException {
    outputStream.flush();
  }

  public byte[] read( int length ) throws IOException, InterruptedException,
          java.io.InterruptedIOException, java.lang.IndexOutOfBoundsException {
    byte[] data = new byte[ length ];
    int dataReadSum = 0;
    int dataRead;
    do {
      dataRead = inputStream.read( data, dataReadSum,
              data.length - dataReadSum );
      if ( dataRead == -1 ) {
        throw new IOException();
      }
      dataReadSum += dataRead;
    } while ( dataReadSum < data.length );
    MidletMain.incrementDataCount( length );
    return data;
  }

  public byte[] readTo( byte stopByte ) throws IOException,
          InterruptedException, java.io.InterruptedIOException {
    ArrayUtil data = new ArrayUtil();
    byte[] b = new byte[ 1 ];
    do {
      inputStream.read( b, 0, 1 );
      data.append( b );
    } while ( b[0] != stopByte );
    MidletMain.incrementDataCount( data.byteString.length );
    return data.byteString;
  }

  public int getAvailable() throws IOException {
    return inputStream.available();
  }

  /**
   * Creating hidden HTTP connection for devices, that could not
   * keep Socket connection while protocol negotiation
   */
  public static void httpPing( String url ) throws IOException {

    HttpConnection c;
    c = ( HttpConnection ) Connector.open( url );
    c.openInputStream();
  }

  public static String retreiveData( String url ) throws IOException {
    String data = new String();
    HttpConnection httpConnection = ( HttpConnection ) Connector.open( url );
    InputStream is = httpConnection.openInputStream();
    int read;
    byte[] buffer = new byte[ 128 ];
    while ( ( read = is.read( buffer ) ) != -1 ) {
      data += new String( buffer, 0, read );
    }
    return data;
  }
}
