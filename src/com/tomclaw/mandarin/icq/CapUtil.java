package com.tomclaw.mandarin.icq;

import com.tomclaw.bingear.BinGear;
import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.utils.HexUtil;
import com.tomclaw.utils.LogUtil;
import java.io.DataInputStream;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class CapUtil {

  public static BinGear dataCaps = null;

  public static void loadCaps() {
    try {
      dataCaps = new BinGear();
      dataCaps.readFromDat( new DataInputStream( 
              MidletMain.clazz.getResourceAsStream( "/res/caps.dat" ) ) );
      LogUtil.outMessage( "caps.dat successfully been read" );
    } catch ( Throwable ex ) {
      LogUtil.outMessage( "caps.dat: reading error" );
    }
  }

  public static Capability fillCapFields( Capability capability ) {
    try {
      String capString = HexUtil.bytesToString( capability.capBytes );
      String[] items = dataCaps.listItems( capString, false );
      String type;
      if ( items.length == 0 ) {
        capability.capIcon = null;
        capability.capType = -1;
        capability.capName = null;
        return capability;
      } else {
        /** This is plain capability **/
        capability.capIcon = dataCaps.getValue( capString, "icon", false );
        capability.capName = dataCaps.getValue( capString, "name", false );
        type = dataCaps.getValue( capString, "type", false );
        capability.capType = ( ( type == null ) ? 0 : Integer.parseInt( type ) );
      }
    } catch ( Throwable ex ) {
    }
    return capability;

  }

  public static Capability getCapabilityByType( Capability[] caps, int capType ) {
    if ( caps != null && caps.length > 0 ) {
      for ( int c = caps.length - 1; c >= 0; c-- ) {
        if ( caps[c] != null && caps[c].capType == capType ) {
          return caps[c];
        }
      }
    }
    return null;
  }

  public static byte[] getXStatusCap( int xStatusIndex ) {
    String[] headers = dataCaps.listGroups();
    if ( headers != null ) {
      for ( int c = 0; c < headers.length; c++ ) {
        try {
          if ( dataCaps.getValue( headers[c], "type" ) == null ) {
            continue;
          }
          if ( Integer.parseInt( dataCaps.getValue( headers[c], "type" ) ) == Capability.CAP_XSTATUS
                  && String.valueOf( dataCaps.getValue( headers[c], "icon" ) ).equals( "xstatus" + xStatusIndex ) ) {
            return HexUtil.stringToBytes( headers[c] );
          }
        } catch ( Throwable ex ) {
          // Nothing to be done
        }
      }
    }
    return null;
  }
}
