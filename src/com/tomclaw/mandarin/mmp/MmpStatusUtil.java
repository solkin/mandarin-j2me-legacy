package com.tomclaw.mandarin.mmp;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class MmpStatusUtil {

  public static int phoneStatus = 6;
  public static final String[] statusesDescr = new String[]{ "STATUS_OFFLINE", "STATUS_ONLINE", "STATUS_AWAY", "STATUS_FFC", "STATUS_DND", "STATUS_INVISIBLE" };
  public static final long[] statusIds = new long[]{ 0x00000000, 0x00000001, 0x00000002, 0x00000104, 0x00000204, 0x80000000 };
  public static final String[] statusesNames = new String[]{ "status_0", "status_1", "status_2", "status_chat", "status_dnd", "status_3" };
  public static final String statusX = "status_";

  public static boolean expectIsStatus( long status ) {
    for ( int c = 0; c < statusIds.length; c++ ) {
      if ( statusIds[c] == status ) {
        return true;
      }
    }
    return false;
  }

  public static String getStatusName( long statusId ) {
    int index = getStatusIndex( statusId );
    if ( index == -1 ) {
      return statusX.concat( String.valueOf( (statusId - 0x0004) >> 8 ) );
    }
    return statusesNames[index];
  }

  public static String getStatusDescr( int index ) {
    return statusesDescr[index];
  }

  public static int getStatusCount() {
    return statusesDescr.length;
  }

  public static long getStatus( int index ) {
    return statusIds[index];
  }

  public static long getExtStatus( int index ) {
    // 0x404 - 0x3504
    index += 4;
    return ( index << 8 ) | 0x0004;
  }
  
  public static int getExtStatusCount() {
    return 50;
  }

  public static String getExtStatusDescr( int index ) {
    return "MMP_EXT_" + index;
  }

  /*public static int[] getStatuses() {
   return (int[]) statusIds.clone();
   }*/
  public static int getStatusIndex( long statusId ) {
    for ( int c = 0; c < statusIds.length; c++ ) {
      if ( statusId == statusIds[c] ) {
        return c;
      }
    }
    return -1;
  }
}
