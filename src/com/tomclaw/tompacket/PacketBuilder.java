package com.tomclaw.tompacket;

import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.mandarin.net.NetConnection;
import com.tomclaw.utils.HexUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class PacketBuilder {

  public static String rootFolder = "/res/maps/";
  public static Hashtable packet_cache = new Hashtable();
  public static Hashtable data;

  public static void setup( Hashtable data ) {
    PacketBuilder.data = data;
    init();
  }

  private static void init() {
    packet_cache.clear();
  }

  public static InputStream openMapStream( String mapFileName ) {
    InputStream inputStream = MidletMain.clazz.getResourceAsStream( rootFolder + mapFileName );
    return inputStream;
  }

  public static Packet cacheMap( String fileName ) throws Throwable {
    Packet packet;
    if ( packet_cache.containsKey( fileName ) ) {
      packet = ( Packet ) packet_cache.get( fileName );
    } else {
      packet = new Packet( fileName );
      packet.loadBlock();
      packet_cache.put( fileName, packet );
    }
    return packet;
  }

  public static void sendPacket( NetConnection netConnection, String fileName, Hashtable data ) throws IOException {
    PacketBuilder.setup( data );
    Packet packet = null;
    try {
      packet = PacketBuilder.cacheMap( fileName );
      HexUtil.dump_( packet.getData(), "OUT: " );
    } catch ( Throwable ex ) {
    }
    if ( packet != null ) {
      netConnection.write( packet.getData() );
      netConnection.flush();
    }

  }

  public static boolean checkNull( String caseName ) {
    if ( data.containsKey( caseName ) ) {
      return ( data.get( caseName ) == null );
    }
    return true;
  }

  static boolean checkNULL( String caseName ) {
    if ( data.containsKey( caseName ) ) {
      if ( data.get( caseName ) instanceof String ) {
        return ( ( String ) data.get( caseName ) ).equals( "NULL" );
      }
    }
    return false;
  }

  public static byte checkByte( String caseName, byte caseDefValue ) {
    if ( data.containsKey( caseName ) ) {
      return ( ( Byte ) data.get( caseName ) ).byteValue();
    }
    return caseDefValue;
  }

  static String checkString( String caseName, String caseDefValue ) {
    if ( data.containsKey( caseName ) ) {
      return ( String ) data.get( caseName );
    }
    return caseDefValue;
  }

  static char checkChar( String caseName, char caseDefValue ) {
    if ( data.containsKey( caseName ) ) {
      return ( ( Character ) data.get( caseName ) ).charValue();
    }
    return caseDefValue;
  }

  static int checkInt( String caseName, int caseDefValue ) {
    if ( data.containsKey( caseName ) ) {
      return ( ( Integer ) data.get( caseName ) ).intValue();
    }
    return caseDefValue;
  }

  static long checkLong( String caseName, long caseDefValue ) {
    if ( data.containsKey( caseName ) ) {
      return ( ( Long ) data.get( caseName ) ).longValue();
    }
    return caseDefValue;
  }

  static byte[] checkArray( String caseName, byte[] array ) {
    if ( data.containsKey( caseName ) ) {
      return ( byte[] ) data.get( caseName );
    }
    return array;
  }
}
