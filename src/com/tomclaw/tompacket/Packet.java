package com.tomclaw.tompacket;

import com.tomclaw.utils.ArrayUtil;
import com.tomclaw.utils.StringUtil;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class Packet {

  public ByteArrayOutputStream baos;
  public TomOutputStream tos;
  public String mapFileName;
  public String mapBlockName;
  public static String className = "";
  public static String blockName = "PACKET_UNK";
  public static boolean isUniversal = true;
  public static int infoFlags = 0x00000000;

  public Packet( String mapFileName ) {
    baos = new ByteArrayOutputStream();
    tos = new TomOutputStream( baos );

    int delimiter = mapFileName.indexOf( ":" );
    if ( delimiter != -1 ) {
      this.mapFileName = mapFileName.substring( 0, delimiter ) + ".tpt";
      this.mapBlockName = mapFileName.substring( delimiter + 1 );
    } else {
      this.mapFileName = mapFileName + ".tpt";
      this.mapBlockName = null;
    }
  }

  public byte[] loadBlock() throws Throwable {
    InputStream is = PacketBuilder.openMapStream( mapFileName );
    DataInputStream dis = new DataInputStream( is );
    /**
     * Header
     */
    byte[] header = new byte[3];
    dis.readFully( header );
    if ( ArrayUtil.equals( header, "TP#".getBytes() ) ) {
      int t_tpVersion = dis.readByte();
      if ( t_tpVersion == MapUtil.tpVersion ) {
        int blockCount = dis.readChar();
        for ( int i = 0; i < blockCount; i++ ) {
          int blockSize = dis.readChar();
          blockName = dis.readUTF();
          isUniversal = dis.readBoolean();
          if ( ( isUniversal && mapBlockName == null ) || blockName.equals( mapBlockName ) ) {
            className = dis.readUTF();
            infoFlags = dis.readInt();
            int modelCount = dis.readChar();
            /**
             * Body
             */
            for ( int c = 0; c < modelCount; c++ ) {
              byte[] caseTypeBytes = new byte[3];
              dis.readFully( caseTypeBytes );
              String caseType = StringUtil.byteArrayToString( caseTypeBytes );
              String caseName = dis.readUTF();
              byte prefixType = 0;
              boolean bigEndian = true;
              int encIndex = 0;
              boolean strView = false;
              /**
               * Постфикс первого уровня
               */
              if ( caseName.endsWith( "_STR" ) ) {
                strView = true;
                caseName = caseName.substring( 0, caseName.length() - 4 );
              }
              /**
               * Постфикс второго уровня
               */
              if ( caseName.endsWith( "_LE" ) ) {
                bigEndian = false;
                caseName = caseName.substring( 0, caseName.length() - 3 );
              } else if ( caseName.endsWith( "_BE" ) ) {
                bigEndian = true;
                caseName = caseName.substring( 0, caseName.length() - 3 );
              }
              /**
               * Префикс первого уровня
               */
              if ( caseName.startsWith( "BYT_" ) ) {
                prefixType = 8;
                caseName = caseName.substring( 4 );
              } else if ( caseName.startsWith( "INT_" ) ) {
                prefixType = 16;
                caseName = caseName.substring( 4 );
              } else if ( caseName.startsWith( "LNG_" ) ) {
                prefixType = 32;
                caseName = caseName.substring( 4 );
              }
              /**
               * Префикс второго уровня
               */
              if ( caseName.startsWith( "WIN_" ) ) {
                encIndex = 0;
                caseName = caseName.substring( 4 );
              } else if ( caseName.startsWith( "UTF_" ) ) {
                encIndex = 1;
                caseName = caseName.substring( 4 );
              } else if ( caseName.startsWith( "UCS_" ) ) {
                encIndex = 2;
                caseName = caseName.substring( 4 );
              }
              /**
               * Длина значения
               */
              int caseLength = dis.readChar();
              long packetLength = 0;
              boolean isNullLength = false;
              if ( caseLength == 0 && PacketBuilder.checkNull( caseName ) ) {
                continue;
              }
              if ( PacketBuilder.checkNULL( caseName ) ) {
                /** Если поле нужно убрать из пакета **/
                caseType = "UNK";
              } else {
                if ( caseLength == MapUtil.SIZE_16BIT - 1 ) {
                  /** Если в файле карты у нас NULL **/
                  if ( PacketBuilder.checkNull( caseName ) ) {
                    /** И ничего не меняется в Hashtable **/
                    continue;
                  }
                  /** Что-то лежит в Hashtable и нам надо это использовать **/
                  isNullLength = true;
                }
                if ( strView ) {
                  byte[] string = new byte[caseLength];
                  dis.readFully( string );
                  String caseDefValue = StringUtil.byteArrayToString( string, true );
                  caseDefValue = PacketBuilder.checkString( caseName, caseDefValue );
                  // Подгрузка файла
                  packetLength = PacketBuilder.cacheMap( caseDefValue ).getData().length;
                }
              }
              /**
               * Типизация и сборка
               */
              if ( caseType.equals( MapUtil.types[0] ) ) {
                byte caseDefValue = strView ? ( byte ) packetLength : ( isNullLength ? 0 : dis.readByte() );
                caseDefValue = strView ? caseDefValue : PacketBuilder.checkByte( caseName, caseDefValue );
                tos.write8( caseDefValue );
              } else if ( caseType.equals( MapUtil.types[1] ) ) {
                char caseDefValue = ( isNullLength ? 0 : dis.readChar() );
                caseDefValue = PacketBuilder.checkChar( caseName, caseDefValue );
                tos.write8( caseDefValue );
              } else if ( caseType.equals( MapUtil.types[2] ) ) {
                int caseDefValue = strView ? ( int ) packetLength : ( isNullLength ? 0 : ( int ) dis.readChar() );
                caseDefValue = strView ? caseDefValue : PacketBuilder.checkInt( caseName, caseDefValue );
                tos.write16( caseDefValue, bigEndian );
              } else if ( caseType.equals( MapUtil.types[3] ) ) {
                long caseDefValue = strView ? packetLength : ( isNullLength ? 0 : dis.readInt() );
                caseDefValue = strView ? caseDefValue : PacketBuilder.checkLong( caseName, caseDefValue );
                tos.write32( ( int ) caseDefValue, bigEndian );
              } else if ( caseType.equals( MapUtil.types[4] ) ) {
                byte[] string = new byte[caseLength];
                if ( !isNullLength ) {
                  dis.readFully( string );
                }
                String caseDefValue = isNullLength ? "" : StringUtil.byteArrayToString( string, true );
                caseDefValue = PacketBuilder.checkString( caseName, caseDefValue );
                byte[] out_string = null;
                if ( encIndex == 0 ) {
                  out_string = StringUtil.stringToByteArray( caseDefValue, false );
                } else if ( encIndex == 1 ) {
                  out_string = StringUtil.stringToByteArray( caseDefValue, true );
                } else if ( encIndex == 2 ) {
                  out_string = StringUtil.stringToUcs2beByteArray( caseDefValue );
                }
                if ( prefixType == 8 ) {
                  tos.write8( out_string.length );
                } else if ( prefixType == 16 ) {
                  tos.write16( out_string.length, bigEndian );
                } else if ( prefixType == 32 ) {
                  tos.write32( out_string.length, bigEndian );
                }
                tos.write( out_string );
              } else if ( caseType.equals( MapUtil.types[5] ) ) {
                byte[] array = new byte[caseLength];
                if ( !isNullLength ) {
                  dis.readFully( array );
                }
                array = PacketBuilder.checkArray( caseName, array );
                // caseDefValue = TransUtil.concatToBytes(array, ',', 16);
                tos.write( array );
              } else if ( caseType.equals( MapUtil.types[6] ) ) {
                byte[] string = new byte[caseLength];
                if ( !isNullLength ) {
                  dis.readFully( string );
                }
                String caseDefValue = isNullLength ? "" : StringUtil.byteArrayToString( string );
                caseDefValue = PacketBuilder.checkString( caseName, caseDefValue );
                // Подгрузка файла
                Packet packet = PacketBuilder.cacheMap( caseDefValue );
                tos.write( packet.getData() );
              } else {
                /**
                 * Unknown type
                 */
                dis.skipBytes( Math.abs( caseLength ) );
              }
            }
          } else {
            dis.skipBytes( blockSize );
          }
        }
        tos.flush();
        return baos.toByteArray();
      }
    }
    return null;
  }

  public byte[] getData() {
    return baos.toByteArray();
  }
}
