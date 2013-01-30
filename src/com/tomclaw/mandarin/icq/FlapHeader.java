package com.tomclaw.mandarin.icq;

import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.mandarin.net.NetConnection;
import com.tomclaw.utils.DataUtil;
import com.tomclaw.utils.HexUtil;
import java.io.IOException;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class FlapHeader {

  public final int channel;
  public final int seqnum;
  public final int data_field_length;
  public final byte[] byteArray;

  /**
   * FLAPHeader
   * @param flap_header 
   */
  public FlapHeader( byte[] flap_header ) {
    if ( flap_header.length > 0 ) {
      channel = 0xffff & ( int ) flap_header[1];
      seqnum = DataUtil.get16( flap_header, 2 );
      data_field_length = DataUtil.get16( flap_header, 4 );
      byteArray = flap_header;
    } else {
      channel = -1;
      seqnum = -1;
      data_field_length = -1;
      byteArray = null;
    }
  }

  /**
   * FLAPHeader
   * @param channel
   * @param seqnum
   * @param data_field_length 
   */
  public FlapHeader( int channel, int seqnum, int data_field_length ) {
    this.data_field_length = data_field_length;
    this.seqnum = seqnum;
    this.channel = ( byte ) channel;
    byteArray = new byte[]{
      ( byte ) 0x2a, ( byte ) channel,
      ( byte ) ( ( seqnum >> 8 ) & 0xff ),
      ( byte ) ( ( seqnum ) & 0xff ),
      ( byte ) ( ( data_field_length >> 8 ) & 0xff ),
      ( byte ) ( ( data_field_length ) & 0xff )
    };
  }

  public void send( NetConnection netConnection ) throws IOException {
    if ( netConnection == null ) {
      throw new IOException();
    }
    netConnection.write( byteArray );
    netConnection.flush();
    if ( MidletMain.logLevel == 1 ) {
      HexUtil.dump_( System.out, byteArray, "<< FLAP: " );
    }
  }
}
