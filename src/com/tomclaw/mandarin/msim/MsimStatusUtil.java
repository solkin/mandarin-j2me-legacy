/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tomclaw.mandarin.msim;

/**
 *
 * @author solkin
 */
public class MsimStatusUtil {
  
  public static final String[] statusesDescr = new String[]{ "STATUS_OFFLINE", "STATUS_ONLINE" };
  public static final int[] statusIds = new int[]{ 0x0000, 0x0001 };
  
  public static long getStatus( int index ) {
    return statusIds[index];
  }

  public static int getStatusIndex( long statusId ) {
    for ( int c = 0; c < statusIds.length; c++ ) {
      if ( statusId == statusIds[c] ) {
        return c;
      }
    }
    return -1;
  }
}
