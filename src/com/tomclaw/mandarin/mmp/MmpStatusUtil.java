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
  public static int baseStatusCount = 5;
  public static int extStatusCount = 50;

  public static boolean expectIsStatus( long status ) {
    /** Base status **/
    for ( int c = 0; c < statusIds.length; c++ ) {
      if ( statusIds[c] == status ) {
        return true;
      }
    }
    /** Ext status **/
    if ( ( ( status + 0x0002 ) >> 8 ) < extStatusCount + baseStatusCount ) {
      return true;
    }
    return false;
  }

  public static String getStatusName( long statusId ) {
    System.out.println("getStatusName("+statusId+")");
    int index = getStatusIndex( statusId );
    System.out.println("index = "+index);
    if ( index >= statusesNames.length ) {
      return statusX.concat( String.valueOf( index-2 ) );
    }
    return statusesNames[index];
  }

  public static String getStatusDescr( int index ) {
    if ( index >= statusIds.length ) {
      return "MMP_EXT_" + index;
    }
    return statusesDescr[index];
  }

  public static int getStatusCount() {
    return baseStatusCount + extStatusCount;
  }

  public static long getStatus( int index ) {
    if ( index >= statusIds.length ) {
      // 0x404 - 0x3504
      index -= 2;
      return ( index << 8 ) | 0x0004;
    }
    return statusIds[index];
  }

  public static int getStatusIndex( long statusId ) {
    for ( int c = 0; c < statusIds.length; c++ ) {
      if ( statusId == statusIds[c] ) {
        return c;
      }
    }
    return ( int ) ( ( statusId - 0x0004 ) >> 8 )+2;
  }
}
