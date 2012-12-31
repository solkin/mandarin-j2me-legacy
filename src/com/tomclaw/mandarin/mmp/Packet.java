package com.tomclaw.mandarin.mmp;

import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.mandarin.net.NetConnection;
import com.tomclaw.utils.ArrayUtil;
import com.tomclaw.utils.DataUtil;
import com.tomclaw.utils.HexUtil;
import com.tomclaw.utils.LogUtil;
import java.io.IOException;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class Packet {

  ArrayUtil data = new ArrayUtil();
  // long magic = PacketType.CS_MAGIC;	// Magic
  long proto = 0x00010016;		// Версия протокола
  long seq = 0x00000000;		// Sequence
  long msg = 0x00000000;		// Тип пакета
  long dlen = 0x00000000; 		// Длина данных
  long from = 0x00000000;		// Адрес отправителя
  long fromport = 0x00000000;         // Порт отправителя
  byte[] reserved = new byte[16];	// Зарезервировано

  public Packet() {
  }

  public boolean parseHeader( byte[] header ) {
    if ( PacketType.CS_MAGIC != DataUtil.get32_reversed( header, 0, true ) ) {
      /**
       * This is not MMP packet
       */
      return false;
    }
    proto = DataUtil.get32_reversed( header, 4, true );
    seq = DataUtil.get32_reversed( header, 8, true );
    msg = DataUtil.get32_reversed( header, 12, true );
    dlen = DataUtil.get32_reversed( header, 16, true );
    from = DataUtil.get32_reversed( header, 20, true );
    fromport = DataUtil.get32_reversed( header, 24, true );
    reserved = DataUtil.getByteArray( header, 28, 16 );
    return true;
  }

  public void send( NetConnection netConnection ) throws IOException {
    byte[] header = new byte[44];
    dlen = data.byteString.length;
    DataUtil.put32_reversed( header, 0, PacketType.CS_MAGIC );
    DataUtil.put32_reversed( header, 4, proto );
    DataUtil.put32_reversed( header, 8, seq );
    DataUtil.put32_reversed( header, 12, msg );
    DataUtil.put32_reversed( header, 16, dlen );
    DataUtil.put32_reversed( header, 20, from );
    DataUtil.put32_reversed( header, 24, fromport );
    DataUtil.putArray_reversed( header, 28, reserved );
    netConnection.write( header );
    netConnection.write( data.byteString );
    netConnection.flush();
  }

  public void dumpPacketData() {
    if ( MidletMain.logLevel == 1 ) {
      LogUtil.outMessage( ">> proto = 0x".concat( Long.toString( proto, 16 ).concat( "\n" ) ) );
      LogUtil.outMessage( "   seq = ".concat( String.valueOf( seq ) ).concat( "\n" ) );
      LogUtil.outMessage( "   msg = 0x".concat( Long.toString( msg, 16 ) ).concat( "\n" ) );
      LogUtil.outMessage( "   dlen = ".concat( String.valueOf( dlen ) ).concat( "\n" ) );
      LogUtil.outMessage( "   from = ".concat( String.valueOf( from ) ).concat( "\n" ) );
      LogUtil.outMessage( "   fromport = ".concat( String.valueOf( fromport ) ).concat( "\n" ) );
      LogUtil.outMessage( "   reserved = ".concat( HexUtil.bytesToString( reserved ) ).concat( "\n" ) );
      LogUtil.outMessage( "   data = ".concat( HexUtil.bytesToString( data.byteString ) ).concat( "\n" ) );
    }
  }
}
