package com.tomclaw.mandarin.mmp;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class MmpStatusUtil {

  public static int phoneStatus = 4;
  public static final String[] statusesDescr = new String[]{"STATUS_OFFLINE", "STATUS_ONLINE", "STATUS_AWAY", "STATUS_INVISIBLE"};
  public static final long[] statusIds = new long[]{0x00000000, 0x00000001, 0x00000002, 0x80000000};

  public static boolean expectIsStatus( long status ) {
    for ( int c = 0; c < statusIds.length; c++ ) {
      if ( statusIds[c] == status ) {
        return true;
      }
    }
    return false;
  }

  /*public static String[] getStatusesDescr() {
   return (String[]) statusesDescr.clone();
   }*/
  public static String getStatusDescr( int index ) {
    return statusesDescr[index];
  }

  public static int getStatusCount() {
    return statusesDescr.length;
  }

  public static long getStatus( int index ) {
    return statusIds[index];
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
