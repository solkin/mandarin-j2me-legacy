package com.tomclaw.mandarin.icq;

import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.mandarin.net.NetConnection;
import com.tomclaw.utils.DataUtil;
import com.tomclaw.utils.HexUtil;
import com.tomclaw.utils.StringUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class Snac {

  public final int family;
  public final int subtype;
  public long requestId;
  public final byte flag1;
  public final byte flag2;
  private final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();

  /**
   * SNAC
   * @param family
   * @param subtype
   * @param flag1
   * @param flag2
   * @throws IOException 
   */
  public Snac( int family, int subtype, int flag1, int flag2 ) throws IOException {
    this( family, subtype, 0, 0, 0 );
  }

  /**
   * SNAC
   * @param family
   * @param subtype
   * @param flag1
   * @param flag2
   * @param requestId
   * @throws IOException 
   */
  public Snac( int family, int subtype, int flag1, int flag2, long requestId ) throws
          IOException {
    this.family = family;
    this.subtype = subtype;
    this.flag1 = ( byte ) ( flag1 & 0xff );
    this.flag2 = ( byte ) ( flag2 & 0xff );
    this.requestId = requestId;
    addWord( family );
    addWord( subtype );
    addByte( flag1 );
    addByte( flag2 );
    addDWord( requestId );
  }

  /**
   * SNAC
   */
  public Snac() {
    this.family = 0;
    this.subtype = 0;
    this.flag1 = 0;
    this.flag2 = 0;
    this.requestId = 0;
  }

  /**
   * Adding one byte
   * @param byt
   * @throws IOException 
   */
  public final void addByte( int byt ) throws IOException {
    byteArray.write( ( byte ) ( ( byt ) & 0xff ) );
  }

  /**
   * Adding byte array
   * @param b
   * @throws IOException 
   */
  public final void addByteArray( byte[] b ) throws IOException {
    if ( b.length > 0 ) {
      byteArray.write( b );
    }
  }

  public final void addByteLString( String s ) throws IOException {
    byte[] array = StringUtil.stringToByteArray( s, true );
    addByte( array.length );
    if ( array.length > 0 ) {
      byteArray.write( array );
    }
  }

  public final void addWordLString( String s ) throws IOException {
    byte[] array = StringUtil.stringToByteArray( s, true );
    addWord( array.length );
    if ( array.length > 0 ) {
      byteArray.write( array );
    }
  }

  /**
   * Adding double word (4 byte)
   * @param x
   * @throws IOException 
   */
  public final void addDWord( long x ) throws IOException {
    addWord( ( ( int ) ( ( x >> 16 ) & 0xffff ) ) );
    addWord( ( ( int ) ( ( x ) & 0xffff ) ) );
  }

  public final void addDWordReversed( long x ) throws IOException {
    addWordReversed( ( ( int ) ( ( x ) & 0xffff ) ) );
    addWordReversed( ( ( int ) ( ( x >> 16 ) & 0xffff ) ) );
  }

  /**
   * Adding type, length, value
   * @param type
   * @param value
   * @throws IOException 
   */
  public final void addTlv( int type, byte[] value ) throws IOException {
    addWord( type );
    addWord( value.length );
    addByteArray( value );
  }

  /**
   * Adding type, length, value
   * @param x
   * @throws IOException 
   */
  public final void addWord( int x ) throws IOException {
    byteArray.write( ( byte ) ( ( x >> 8 ) & 0xff ) );
    byteArray.write( ( byte ) ( ( x ) & 0xff ) );
  }

  public final void addWordReversed( int x ) throws IOException {
    byteArray.write( ( byte ) ( ( x ) & 0xff ) );
    byteArray.write( ( byte ) ( ( x >> 8 ) & 0xff ) );
  }

  /**
   * Creates a channel 2 FLAP packet with this SNAC packet inside,
   * and sends it immediately using a given connection conn.
   */
  public void send( OutputStream outputStream, int seq ) throws IOException {
    byteArray.flush();
    byte[] flapData = byteArray.toByteArray();
    byteArray.close();
    byte[] flapHeader = Snac.createFlapHeader( 2, seq, flapData.length );
    ByteArrayOutputStream bas = new ByteArrayOutputStream( flapData.length
            + flapHeader.length );
    bas.write( flapHeader );
    bas.write( flapData );
    bas.flush();

    outputStream.write( bas.toByteArray() );
    outputStream.flush();

    if ( MidletMain.logLevel == 1 ) {
      HexUtil.dump_( System.out, bas.toByteArray(), "<< SNAC (" + HexUtil.toHexString( family ) + ", " + HexUtil.toHexString( subtype ) + "): " );
    }

    bas.close();
  }

  public ByteArrayOutputStream getByteArray() {
    return byteArray;
  }
  
  public static byte[] createFlapHeader(int channel, int seqNum, 
          int dataFieldLength) {
    return new byte[]{
      ( byte ) 0x2a, ( byte ) channel,
      ( byte ) ( ( seqNum >> 8 ) & 0xff ),
      ( byte ) ( ( seqNum ) & 0xff ),
      ( byte ) ( ( dataFieldLength >> 8 ) & 0xff ),
      ( byte ) ( ( dataFieldLength ) & 0xff )
    };
  }
}
