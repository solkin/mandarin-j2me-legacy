package com.tomclaw.mandarin.icq;

import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.mandarin.net.NetConnection;
import com.tomclaw.utils.DataUtil;
import com.tomclaw.utils.HexUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
   * SNAC constructor comment.
   */
  public Snac( int family, int subtype ) throws IOException {
    this( family, subtype, 0, 0 );
  }

  /**
   * SNAC constructor comment.
   */
  public Snac( int family, int subtype, int flag1, int flag2 ) throws IOException {
    this( family, subtype, 0, 0, 0 );
  }

  /**
   * SNAC constructor comment.
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

  public Snac() {
    this.family = 0;
    this.subtype = 0;
    this.flag1 = 0;
    this.flag2 = 0;
    this.requestId = 0;
  }

  /**
   * SNAC constructor comment.
   */
  public final void addByte( int byt ) throws IOException {
    byteArray.write( ( byte ) ( ( byt ) & 0xff ) );
  }

  /**
   * SNAC constructor comment.
   */
  public final void addByteArray( byte[] b ) throws IOException {
    if ( b.length > 0 ) {
      byteArray.write( b );
    }
  }

  /**
   * SNAC constructor comment.
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
   * SNAC constructor comment.
   */
  public final void addStringPrependedWithByteLength( String s ) throws IOException {
    byte[] ba = DataUtil.string2byteArray( s );
    addByte( ba.length );
    addByteArray( ba );
  }

  /**
   * SNAC constructor comment.
   */
  public final void addStringRaw( String s ) throws IOException {
    byte[] ba = DataUtil.string2byteArray( s );
    addByteArray( ba );
  }

  /**
   * SNAC constructor comment.
   */
  public final void addTlv( int type, byte[] value ) throws IOException {
    addWord( type );
    addWord( value.length );
    addByteArray( value );
  }

  /**
   * SNAC constructor comment.
   */
  public final void addTlv( int type, String value ) throws IOException {
    addTlv( type, DataUtil.string2byteArray( value ) );
  }

  /**
   * SNAC constructor comment.
   */
  public final void addTlvByte( int type, int byt ) throws IOException {
    addWord( type );
    addWord( 1 );
    addWord( byt );
  }

  /**
   * SNAC constructor comment.
   */
  public final void addTlvDWord( int type, long dword ) throws IOException {
    addWord( type );
    addWord( 4 );
    addDWord( dword );
  }

  /**
   * SNAC constructor comment.
   */
  public final void addTlvWord( int type, int word ) throws IOException {
    addWord( type );
    addWord( 2 );
    addWord( word );
  }

  /**
   * SNAC constructor comment.
   */
  public final void addWord( int x ) throws IOException {
    byteArray.write( ( byte ) ( ( x >> 8 ) & 0xff ) );
    byteArray.write( ( byte ) ( ( x ) & 0xff ) );
  }

  public final void addWordReversed( int x ) throws IOException {
    byteArray.write( ( byte ) ( ( x ) & 0xff ) );
    byteArray.write( ( byte ) ( ( x >> 8 ) & 0xff ) );
  }

  public final void addIcqUin( long uin ) throws IOException {
    addByteArray( new byte[]{
              ( byte ) ( uin & 0xff ), //
              ( byte ) ( ( uin >> 8 ) & 0xff ), //
              ( byte ) ( ( uin >> 16 ) & 0xff ), //
              ( byte ) ( ( uin >> 24 ) & 0xff )
            } );
  }

  /**
   Creates a channel 2 FLAP packet with this SNAC packet inside,
   and sends it immediately using a given connection conn.
   */
  public void send( NetConnection netConnection, int seq ) throws IOException {
    if ( netConnection == null ) {
      throw new IOException();
    }
    byteArray.flush();
    byte[] flapData = byteArray.toByteArray();
    byteArray.close();
    byte[] flapHeader = new FlapHeader( 2, seq,
            flapData.length ).byteArray;
    ByteArrayOutputStream bas = new ByteArrayOutputStream( flapData.length
            + flapHeader.length );
    bas.write( flapHeader );
    bas.write( flapData );
    bas.flush();

    netConnection.write( bas.toByteArray() );

    // Logger.outMessage("<< SNAC (" + HexUtil.toHexString(family) + ", " + HexUtil.toHexString(subtype) + ")");
    if ( MidletMain.logLevel == 1 ) {
      HexUtil.dump_( System.out, bas.toByteArray(), "<< SNAC (" + HexUtil.toHexString( family ) + ", " + HexUtil.toHexString( subtype ) + "): " );
    }

    bas.close();
    netConnection.flush();
  }

  public ByteArrayOutputStream getByteArray() {
    return byteArray;
  }
}
