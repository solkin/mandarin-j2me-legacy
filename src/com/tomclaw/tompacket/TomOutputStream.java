package com.tomclaw.tompacket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class TomOutputStream extends DataOutputStream {

  public TomOutputStream( OutputStream os ) {
    super( os );
  }

  public void write8( int value ) throws IOException {
    writeByte( ( byte ) ( value & 0xff ) );
  }

  public void write16( int value, boolean bigEndian ) throws IOException {
    if ( bigEndian ) {
      writeByte( ( byte ) ( ( value >> 8 ) & 0xff ) );
      writeByte( ( byte ) ( value & 0xff ) );
    } else {
      writeByte( ( byte ) ( value & 0xff ) );
      writeByte( ( byte ) ( ( value >> 8 ) & 0xff ) );
    }
  }

  public void write32( long value, boolean bigEndian ) throws IOException {
    if ( bigEndian ) {
      writeByte( ( byte ) ( ( value >> 24 ) & 0xff ) );
      writeByte( ( byte ) ( ( value >> 16 ) & 0xff ) );
      writeByte( ( byte ) ( ( value >> 8 ) & 0xff ) );
      writeByte( ( byte ) ( value & 0xff ) );
    } else {
      writeByte( ( byte ) ( value & 0xff ) );
      writeByte( ( byte ) ( ( value >> 8 ) & 0xff ) );
      writeByte( ( byte ) ( ( value >> 16 ) & 0xff ) );
      writeByte( ( byte ) ( ( value >> 24 ) & 0xff ) );
    }
  }
}
