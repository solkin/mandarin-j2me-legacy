package com.tomclaw.mandarin.icq;

import com.tomclaw.mandarin.core.Handler;
import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.mandarin.net.IncorrectAddressException;
import com.tomclaw.mandarin.net.NetConnection;
import com.tomclaw.utils.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class IcqSession implements Runnable {

  public NetConnection netConnection = null;
  public int seq = 0;
  private boolean isAlive = true;
  public final IcqAccountRoot icqAccountRoot;
  public boolean isError = true;
  public boolean isRequestSsi = false;

  public IcqSession( IcqAccountRoot icqAccountRoot ) {
    this.icqAccountRoot = icqAccountRoot;
  }

  /**
   * Settings network connection
   * @param netConnection
   */
  public void setNetConnection( NetConnection netConnection ) {
    this.netConnection = netConnection;
  }

  public int getNextSeq() {
    return ++seq;
  }

  /**
   * Login section by user UIN and password only using NetConnection,
   * setted up by setNetConnection.
   * @param userUin
   * @param password
   * @throws IOException
   * @throws InterruptedException
   * @throws UnknownHostException
   * @throws IncorrectAddressException
   */
  public void loginMd5( int initStatus ) throws IOException, InterruptedException, IncorrectAddressException, LegacyProtocolException, LoginFailedException {
    boolean authSuccess = false;
    LogUtil.outMessage( "MD5 method" );
    /** Reading protocol version from server **/
    byte[] packetData = receivePacket();
    LogUtil.outMessage( "Protocol received" );
    /** Sending protocol version to server **/
    byte[] flapHeader = Snac.createFlapHeader( 0x01, getNextSeq(), 0x04 );
    ByteArrayOutputStream bas = new ByteArrayOutputStream( flapHeader.length + 4 );
    bas.write( flapHeader );
    bas.write( packetData );
    bas.flush();
    netConnection.write( bas.toByteArray() );
    LogUtil.outMessage( "Protocol sent" );
    /** Sending auth key request **/
    Snac snac = new Snac( 0x0017, 0x0006, 0, 0, 0 );

    snac.addWord( 0x0001 );
    snac.addWordLString( icqAccountRoot.userId );

    snac.addWord( 0x004b );
    snac.addWord( 0x0000 );

    snac.addWord( 0x005a );
    snac.addWord( 0x0000 );

    LogUtil.outMessage( "sending auth key request" );
    snac.send( getNetworkConnection().outputStream, getNextSeq() );
    /** Reading auth key from server **/
    packetData = receivePacket();
    int snacFamily = DataUtil.get16( packetData, 0 );
    int snacSubtype = DataUtil.get16( packetData, 2 );

    LogUtil.outMessage( "snacFamily: " + snacFamily );
    LogUtil.outMessage( "snacSubtype: " + snacSubtype );
    if ( MidletMain.logLevel == 1 ) {
      LogUtil.outMessage( "Data: " + HexUtil.bytesToString( packetData ) );
    }

    if ( snacFamily == 0x0017 && snacSubtype == 0x0007 ) {
      int keyLength = DataUtil.get16( packetData, 10 );
      String key = DataUtil.byteArray2string( packetData, 12, keyLength );
      LogUtil.outMessage( "Key: " + key );
      Handler.setConnectionStage( icqAccountRoot, 2 );
      /** Generating MD5 hash **/
      String aol = "AOL Instant Messenger (SM)";
      MD5 md5 = new MD5( key.concat( icqAccountRoot.userPassword ).concat( aol ).getBytes() );
      byte[] md5hash = md5.doFinal();
      LogUtil.outMessage( "MD5 Hash: " + HexUtil.bytesToString( md5hash ) );
      /** Sending auth key request **/
      snac = new Snac( 0x0017, 0x0002, 0, 0, 0 );
      snac.addWord( 0x0001 );
      snac.addWordLString( icqAccountRoot.userId );
      snac.addWord( 0x0025 );
      snac.addWord( md5hash.length );
      snac.addByteArray( md5hash );
      LogUtil.outMessage( "sending MD5 hash" );
      snac.send( getNetworkConnection().outputStream, getNextSeq() );
      /** Reading and parsing server data **/
      String bosHostPort = new String();
      byte[] cookie = new byte[ 0 ];
      byte[] authResponse; // = new byte[flap.data_field_length];

      authResponse = receivePacket();

      LogUtil.outMessage( "Auth: " + HexUtil.bytesToString( authResponse ) );

      // AimTlvList resp = new AimTlvList(authResponse, 10, authResponse.length - 10);

      int offset = 10;

      LogUtil.outMessage( "authResponse.length = " + authResponse.length );
      while ( offset < authResponse.length ) {
        int respType = DataUtil.get16( authResponse, offset );
        offset += 2;
        int respLength = DataUtil.get16( authResponse, offset );
        offset += 2;
        byte[] respValue = DataUtil.getByteArray( authResponse, offset, respLength );
        offset += respLength;
        LogUtil.outMessage( "Type: " + respType );

        switch ( respType ) {
          case 0x0001: {
            /**
             * TLV.Type(0x01) - screen name (uin)
             */
            LogUtil.outMessage( "Screen name (UIN)" );
            break;
          }
          case 0x0004: {
            /**
             * TLV.Type(0x04) - error page description url
             */
            LogUtil.outMessage( "Error page description url" );
            LogUtil.outMessage( new String( respValue ) );
            break;
          }
          case 0x0008: {
            /**
             * TLV.Type(0x08) - authorization error
             */
            LogUtil.outMessage( "Authorization error" );
            break;
          }
          case 0x000C: {
            /**
             * TLV.Type(0x0C) - unknown
             */
            LogUtil.outMessage( "Unknown error" );
            break;
          }
          case 0x0005: {
            /**
             * TLV.Type(0x05) - BOS server address
             */
            LogUtil.outMessage( "BOS server address" );
            bosHostPort = new String( respValue );
            authSuccess = true;
            break;
          }
          case 0x0006: {
            /**
             * TLV.Type(0x06) - authorization cookie
             */
            LogUtil.outMessage( "Authorization cookie" );
            cookie = respValue;
            authSuccess &= true;
            break;
          }
        }
      }
      if ( authSuccess ) {
        /**
         * Closing auth connection & connecting to BOS
         */
        netConnection.disconnect();
        LogUtil.outMessage( "Auth disconnected. Connecting to BOS: " + bosHostPort );
        Handler.setConnectionStage( icqAccountRoot, 4 );
        netConnection.connectAddress( bosHostPort );
        LogUtil.outMessage( "Connected to BOS" );

        //Thread thread = new Thread(this);
        //thread.start();
        protocolNegotation( cookie, initStatus );
        return;
      }
    }

    LogUtil.outMessage( "Login failed" );
    throw new LoginFailedException();

  }

  /**
   * Login section by user UIN and password only using NetConnection,
   * setted up by setNetConnection.
   * @param userUin
   * @param password
   * @throws IOException
   * @throws InterruptedException
   * @throws UnknownHostException
   * @throws IncorrectAddressException
   */
  public void login( int initStatus ) throws IOException, InterruptedException,
          IncorrectAddressException, LegacyProtocolException, LoginFailedException {
    // icqAccountRoot.userId = "610334831";
    isAlive = true;
    if ( icqAccountRoot.userId.indexOf( '@' ) == -1 ) {
      loginMd5( initStatus );
      return;
    }
    boolean authSuccess = false;
    /** Reading protocol version from server **/
    receivePacket();
    /** XORing password **/
    byte[] password_b = DataUtil.string2byteArray( icqAccountRoot.userPassword );
    final byte[] xor_table = { ( byte ) 0xf3, ( byte ) 0x26, ( byte ) 0x81, ( byte ) 0xc4, ( byte ) 0x39, ( byte ) 0x86, ( byte ) 0xdb, ( byte ) 0x92 };
    int xorindex = 0;
    for ( int i = 0; i < password_b.length; i++ ) {
      password_b[i] ^= xor_table[xorindex++];
      if ( xorindex >= xor_table.length ) {
        xorindex = 0;
      }
    }
    /** Sending auth key request **/
    Snac snac = new Snac();

    snac.addDWord( 0x00000001 );

    snac.addWord( 0x0056 );
    snac.addWord( 0x0000 );

    snac.addWord( 0x0001 );
    snac.addWordLString( icqAccountRoot.userId );

    snac.addWord( 0x0002 );
    snac.addWord( password_b.length );
    snac.addByteArray( password_b );

    byte[] flapHeader = Snac.createFlapHeader( 0x01, getNextSeq(), snac.getByteArray().size() );
    ByteArrayOutputStream bas = new ByteArrayOutputStream( flapHeader.length + snac.getByteArray().size() );
    bas.write( flapHeader );
    bas.write( snac.getByteArray().toByteArray() );
    bas.flush();
    netConnection.write( bas.toByteArray() );

    /** Reading and parsing server data **/
    String bosHostPort = new String();
    byte[] cookie = new byte[ 0 ];
    byte[] authResponse;

    authResponse = receivePacket();

    LogUtil.outMessage( "Auth: " + HexUtil.bytesToString( authResponse ) );

    int offset = 0;

    LogUtil.outMessage( "authResponse.length = " + authResponse.length );
    while ( offset < authResponse.length ) {
      int respType = DataUtil.get16( authResponse, offset );
      offset += 2;
      int respLength = DataUtil.get16( authResponse, offset );
      offset += 2;
      byte[] respValue = DataUtil.getByteArray( authResponse, offset, respLength );
      offset += respLength;
      LogUtil.outMessage( "Type: " + respType );

      switch ( respType ) {
        case 0x0001: {
          /** TLV.Type(0x01) - screen name (uin) **/
          LogUtil.outMessage( "Screen name (UIN)" );
          MidletMain.mainFrame.getAccountTab( icqAccountRoot.getUserId() ).accountUserId = StringUtil.byteArrayToString( respValue );
          icqAccountRoot.userId = StringUtil.byteArrayToString( respValue );
          LogUtil.outMessage( "value = " + icqAccountRoot.userId );
          break;
        }
        case 0x0004: {
          /** TLV.Type(0x04) - error page description URL **/
          LogUtil.outMessage( "Error page description url" );
          LogUtil.outMessage( new String( respValue ) );
          break;
        }
        case 0x0008: {
          /** TLV.Type(0x08) - authorization error **/
          LogUtil.outMessage( "Authorization error" );
          break;
        }
        case 0x000C: {
          /** TLV.Type(0x0C) - unknown **/
          LogUtil.outMessage( "Unknown error" );
          break;
        }
        case 0x0005: {
          /** TLV.Type(0x05) - BOS server address **/
          LogUtil.outMessage( "BOS server address" );
          bosHostPort = new String( respValue );
          authSuccess = true;
          break;
        }
        case 0x0006: {
          /** TLV.Type(0x06) - authorization cookie **/
          LogUtil.outMessage( "Authorization cookie" );
          cookie = respValue;
          authSuccess &= true;
          break;
        }
      }
    }
    if ( authSuccess ) {
      /** Closing auth connection & connecting to BOS **/
      netConnection.disconnect();
      LogUtil.outMessage( "Auth disconnected. Connecting to BOS: " + bosHostPort );
      Handler.setConnectionStage( icqAccountRoot, 4 );
      netConnection.connectAddress( bosHostPort );
      LogUtil.outMessage( "Connected to BOS" );
      protocolNegotation( cookie, initStatus );
      return;
    }

    LogUtil.outMessage( "Login failed" );
    throw new LoginFailedException();
  }

  public void protocolNegotation( byte[] cookie, int initStatus )
          throws IOException, InterruptedException, LegacyProtocolException {
    /** Protocol negotiation **/
    Handler.setConnectionStage( icqAccountRoot, 5 );
    /** Reading hello packet **/
    receiveAllPackets();
    /** Sending auth data **/
    byte[] chipsa_flapHeader = Snac.createFlapHeader( 0x01, ++seq, 4 + 4 + cookie.length );
    byte[] xxx = new byte[ 8 ];
    int offset = 0;
    offset += DataUtil.put16( xxx, offset, 0x0000 );
    offset += DataUtil.put16( xxx, offset, 0x0001 );
    offset += DataUtil.put16( xxx, offset, 0x0006 );
    offset += DataUtil.put16( xxx, offset, cookie.length );

    byte cookie_reply[] = new byte[ chipsa_flapHeader.length + xxx.length + cookie.length ];

    System.arraycopy( chipsa_flapHeader, 0, cookie_reply, 0, chipsa_flapHeader.length );
    System.arraycopy( xxx, 0, cookie_reply, chipsa_flapHeader.length, xxx.length );
    System.arraycopy( cookie, 0, cookie_reply, chipsa_flapHeader.length + xxx.length, cookie.length );

    netConnection.write( cookie_reply );
    netConnection.flush();

    /**
     * Login to BOS is complete
     */
    LogUtil.outMessage( "Login to BOS is complete" );
    Handler.setConnectionStage( icqAccountRoot, 6 );

    /**
     * Receiving supported services list
     */
    receiveAllPackets();
    /**
     * Client ask for services version numbers
     */
    Snac snac = new Snac( 0x0001, 0x0017, 0, 0, 0x17 );

    snac.addByteArray( new byte[]{
              0x00, 0x01, 0x00, 0x04,
              0x00, 0x02, 0x00, 0x01,
              0x00, 0x03, 0x00, 0x01,
              0x00, 0x04, 0x00, 0x01,
              0x00, 0x08, 0x00, 0x01,
              0x00, 0x09, 0x00, 0x01,
              0x00, 0x0a, 0x00, 0x01,
              0x00, 0x0b, 0x00, 0x01,
              0x00, 0x0c, 0x00, 0x01,
              0x00, 0x13, 0x00, 0x04,
              0x00, 0x15, 0x00, 0x01,
              0x00, 0x22, 0x00, 0x01,
              0x00, 0x24, 0x00, 0x01,
              0x00, 0x25, 0x00, 0x01
            } );

    snac.send( netConnection.outputStream, ++seq );

    /**
     * Server sends its services version numbers
     */
    receiveAllPackets();
    /**
     * Setup current connection
     */
    /**
     * Client ask server for rate limits info
     */
    snac = new Snac( 0x0001, 0x0006, 0, 0, 7 );
    snac.send( netConnection.outputStream, ++seq );

    /**
     * Server sends rate limits information
     */
    receiveAllPackets();

    /**
     * Client ack connection rate limits
     */
    snac = new Snac( 0x0001, 0x0008, 0, 0, 8 );
    snac.addWord( 0x0001 );
    snac.addWord( 0x0002 );
    snac.addWord( 0x0003 );
    snac.addWord( 0x0004 );
    snac.addWord( 0x0005 );
    snac.send( netConnection.outputStream, ++seq );

    Handler.setConnectionStage( icqAccountRoot, 7 );
    /**
     * Services setup
     */
    /**
     * Client ask server location service limitations
     */
    snac = new Snac( 0x0002, 0x0002, 0, 0, 9 );
    snac.send( netConnection.outputStream, ++seq );
    /**
     * Server replies via location service limitations
     */
    receiveAllPackets();
    /**
     * Client sends its capabilities / profile to server
     */
    IcqPacketSender.sendCapabilities( icqAccountRoot.session, icqAccountRoot.xStatusId, initStatus );
    /**
     * Client ask server BLM service limitations
     */
    snac = new Snac( 0x0003, 0x0002, 0, 0, 10 );
    snac.send( netConnection.outputStream, ++seq );
    Handler.setConnectionStage( icqAccountRoot, 8 );
    /**
     * Server replies via BLM service limitations
     */
    // REMOVED: receiveAllPackets();
    /**
     * Client ask server ICBM service parameters
     */
    snac = new Snac( 0x0004, 0x0004, 0, 0, 11 );
    snac.send( netConnection.outputStream, ++seq );
    /**
     * Server sends ICBM service parameters to client
     */
    // REMOVED: receiveAllPackets();
    /**
     * ICBM parameters could be changed here by SNAC (0x04, 0x02)
     */
    snac = new Snac( 0x0004, 0x0002, 0, 0, 12 );
    // Channel to set up
    snac.addWord( 0x0000 );
    // Message flags
    snac.addDWord( 0x0000000b );
    // Max message snac size
    snac.addWord( 0x1f40 );// 1F40
    // Max sender warning level
    snac.addWord( 0x03e7 );
    // Max receiver warning level
    snac.addWord( 0x03e7 );
    // Minimum message interval (sec)
    snac.addWord( 0x0000 );
    // Unknown parameter 0x0000
    snac.addWord( 0x0000 );
    snac.send( netConnection.outputStream, ++seq );
    /** Server sends ICBM service parameters to client **/
    // receiveAllPackets();
    /** Client ask server PRM service limitations **/
    snac = new Snac( 0x0009, 0x0002, 0, 0, 13 );
    snac.send( netConnection.outputStream, ++seq );
    /** Server sends PRM service limitations to client **/
    // REMOVED: receiveAllPackets();
    /** Client ask server for SSI service limitations **/
    snac = new Snac( 0x0013, 0x0002, 0, 0, 14 );
    snac.send( netConnection.outputStream, ++seq );
    /** Server sends SSI service limitations to client **/
    // REMOVED: receiveAllPackets();
    Handler.setConnectionStage( icqAccountRoot, 9 );
    /** Client requests SSI **/
    snac = new Snac( 0x0013, 0x0004, 0, 0, 15 );
    snac.send( netConnection.outputStream, ++seq );
    /** Server sends client SSI **/
    receiveAllPackets();
    /** Client activates server SSI data **/
    snac = new Snac( 0x0013, 0x0007, 0, 0, 16 );
    snac.send( netConnection.outputStream, ++seq );
    /** Final actions **/
    finalActions( initStatus );
  }

  public void finalActions( int initStatus )
          throws IOException, IOException, InterruptedException {
    /** Final actions **/
    /** Client sends its DC info and status to server **/
    Snac snac = new Snac( 0x0001, 0x001e, 0, 0, 6 );
    snac.addByteArray( new byte[]{
              ( byte ) 0x00, ( byte ) 0x06, ( byte ) 0x00, ( byte ) 0x04 } );
    snac.addWord( 0x0001 );
    if ( initStatus < 0x1000 ) {
      snac.addWord( initStatus );
    } else {
      snac.addWord( 0x0000 );
    }
    snac.addByteArray( new byte[]{
              /**(byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x04,
               (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00,*/
              ( byte ) 0x00, ( byte ) 0x08,
              ( byte ) 0x00, ( byte ) 0x02, ( byte ) 0x00, ( byte ) 0x00,
              ( byte ) 0x00, ( byte ) 0x0C, ( byte ) 0x00, ( byte ) 0x25, // dc info (optional)
              ( byte ) 0xC0, ( byte ) 0xA8, ( byte ) 0x04, ( byte ) 0x6B, // DC Internal ip address
              ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x73, ( byte ) 0x74, // DC tcp port
              ( byte ) 0x04, // DC type
              ( byte ) 0x00, ( byte ) 0x0B, // DC Protocol version
              ( byte ) 0x00, ( byte ) 0xAE, ( byte ) 0xF0, ( byte ) 0x9E, // Auth cookie
              ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x50, // Web front port
              ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x03, // Client features
              ( byte ) 0x77, ( byte ) 0x65, ( byte ) 0x78, ( byte ) 0x68 } ); // Mandarin = 77657868
    try {
      byte major = ( byte ) Integer.parseInt( String.valueOf( MidletMain.build.charAt( 0 ) ) );
      byte main = ( byte ) Integer.parseInt( String.valueOf( MidletMain.build.charAt( 1 ) ) );
      byte submain = ( byte ) Integer.parseInt( String.valueOf( MidletMain.build.charAt( 2 ) ) );
      byte minor = ( byte ) Integer.parseInt( String.valueOf( MidletMain.build.charAt( 3 ) ) );
      LogUtil.outMessage( "Version: " + major + "." + main + "." + submain + "." + minor );
      snac.addByte( major );
      snac.addByte( main );
      snac.addByte( submain );
      snac.addByte( minor );
    } catch ( Throwable ex ) {
      snac.addWord( 0x0000 );
    }
    snac.addByteArray( new byte[]{
              ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
              ( byte ) 0x00, ( byte ) 0x00
            } );
    snac.send( netConnection.outputStream, ++seq );
    /** Setting/Update private status **/
    IcqPacketSender.setUpdatePrivacy( icqAccountRoot.session, icqAccountRoot.privateBuddyId, icqAccountRoot.pStatusId );
    /** Client READY command **/
    snac = new Snac( 0x0001, 0x0002, 0, 0, 0x0002 );

    snac.addWord( 0x0001 );
    snac.addWord( 0x0003 );
    snac.addDWord( 69636 );

    snac.addWord( 0x0002 );
    snac.addWord( 0x0001 );
    snac.addDWord( 65796 );

    snac.addWord( 0x0003 );
    snac.addWord( 0x0001 );
    snac.addDWord( 69636 );

    snac.addWord( 0x0004 );
    snac.addWord( 0x0001 );
    snac.addDWord( 69636 );

    snac.addWord( 0x0006 );
    snac.addWord( 0x0001 );
    snac.addDWord( 69636 );

    snac.addWord( 0x0008 );
    snac.addWord( 0x0001 );
    snac.addDWord( 69636 );

    snac.addWord( 0x0009 );
    snac.addWord( 0x0001 );
    snac.addDWord( 69636 );

    snac.addWord( 0x000a );
    snac.addWord( 0x0001 );
    snac.addDWord( 69636 );

    snac.addWord( 0x0013 );
    snac.addWord( 0x0002 );
    snac.addDWord( 69636 );

    snac.send( netConnection.outputStream, ++seq );
    /** Requesting offline messages **/
    IcqPacketSender.requestOfflineMessages( icqAccountRoot.session, icqAccountRoot.userId );
    /** Looking for some stuff on server **/
    new Thread( this ).start();
    Handler.setConnectionStage( icqAccountRoot, 10 );
  }

  public void receiveAllPackets() throws IOException, InterruptedException, LegacyProtocolException {
    while ( isAlive ) {
      receivePacket();
      if ( netConnection.getAvailable() < 6 ) {
        break;
      }
    }
  }

  public byte[] receivePacket()
          throws IOException, InterruptedException, LegacyProtocolException {
    byte[] flapData = netConnection.read( 6 );
    if ( flapData == null || !isAlive ) {
      LogUtil.outMessage( "Disconnected. Flap reading aborted." );
      disconnect();
      return null;
    }
    /** Reading FLAP header **/
    // int channel = DataUtil.get16( flapData, 0 );
    // int seqnum = DataUtil.get16( flapData, 2 );
    int dataFieldLength = DataUtil.get16( flapData, 4 );
    /** Reading body **/
    byte[] data = netConnection.read( dataFieldLength );
    if ( data != null && isAlive ) {
      IcqPacketParser.parsePacket( icqAccountRoot, data );
    } else {
      LogUtil.outMessage( "Disconnected. Flap body reading aborted." );
      disconnect();
      return null;
    }
    return data;
  }

  public NetConnection getNetworkConnection() {
    return netConnection;
  }

  public void disconnect() {
    isAlive = false;
  }

  public boolean getAlive() {
    return isAlive;
  }

  /**
   * Incoming stuff thread
   */
  public void run() {
    Thread keepAlive = new Thread() {

      public void run() {
        while ( isAlive && IcqSession.this.netConnection != null ) {
          try {
            IcqPacketSender.sendKeepAlive( icqAccountRoot.session );
            sleep( MidletMain.keepAliveDelay * 1000 );
          } catch ( InterruptedException ex ) {
          }
        }
      }
    };
    keepAlive.setPriority( Thread.MIN_PRIORITY );
    keepAlive.start();
    Thread httpPing = new Thread() {

      public void run() {
        while ( isAlive && MidletMain.httpHiddenPing > 0 ) {
          try {
            NetConnection.httpPing( "http://www.icq.com" );
            sleep( MidletMain.httpHiddenPing * 1000 );
          } catch ( Throwable ex ) {
            LogUtil.outMessage( "HTTP hidden connection failed" );
          }
        }
      }
    };
    httpPing.setPriority( Thread.MIN_PRIORITY );
    httpPing.start();
    isError = false;
    while ( isAlive ) {
      try {
        receivePacket();
        if ( MidletMain.pack_count > MidletMain.pack_count_invoke_gc ) {
          System.gc();
          MidletMain.pack_count = 0;
        }
        MidletMain.pack_count++;
      } catch ( IOException ex ) {
        if ( isAlive ) {
          isError = true;
          disconnect();
          LogUtil.outMessage( "Failed: " + ex.getMessage(), true );
        }
      } catch ( Throwable ex1 ) {
        LogUtil.outMessage( "Error in listener: " + ex1.getMessage(), true );
      }
    }
    isAlive = true;
    try {
      netConnection.disconnect();
      netConnection = null;
    } catch ( IOException ex ) {
      LogUtil.outMessage( "Disconnect failed: " + this.toString(), true );
    }
    int prevStatus = icqAccountRoot.statusIndex;
    icqAccountRoot.statusIndex = 0;
    Handler.disconnectEvent( icqAccountRoot );
    if ( MidletMain.autoReconnect && isError ) {
      try {
        Thread.sleep( MidletMain.reconnectTime );
      } catch ( InterruptedException ex ) {
      }
      icqAccountRoot.connectAction( prevStatus );
    }
  }
}
