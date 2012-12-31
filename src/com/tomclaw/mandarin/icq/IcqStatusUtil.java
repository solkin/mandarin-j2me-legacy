package com.tomclaw.mandarin.icq;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class IcqStatusUtil {

  public static final String[] statusesDescr = new String[]{"STATUS_OFFLINE", "STATUS_ONLINE", "STATUS_AWAY", "STATUS_NA", "STATUS_OCCUPIED", "STATUS_DND", "STATUS_INVISIBLE", "STATUS_FFC", "STATUS_EVIL", "STATUS_DEPRESSION", "STATUS_ATHOME", "STATUS_ATWORK", "STATUS_LAUNCH", "STATUS_MOBILE"};
  public static final int[] statusIds = new int[]{-1, 0x0000, 0x0001, 0x0004, 0x0010, 0x0002, 0x0100, 0x1000, 0x1001, 0x1002, 0x1003, 0x1004, 0x1005, 0x1006};
  public static final byte[][] aStatusBytes = {
    {( byte ) 0xb7, ( byte ) 0x07, ( byte ) 0x43, ( byte ) 0x78, ( byte ) 0xf5, ( byte ) 0x0c, ( byte ) 0x77, ( byte ) 0x77, ( byte ) 0x97, ( byte ) 0x77, ( byte ) 0x57, ( byte ) 0x78, ( byte ) 0x50, ( byte ) 0x2d, ( byte ) 0x05, ( byte ) 0x75}, // QipStatus: <<Free For Chat>>
    {( byte ) 0xb7, ( byte ) 0x07, ( byte ) 0x43, ( byte ) 0x78, ( byte ) 0xf5, ( byte ) 0x0c, ( byte ) 0x77, ( byte ) 0x77, ( byte ) 0x97, ( byte ) 0x77, ( byte ) 0x57, ( byte ) 0x78, ( byte ) 0x50, ( byte ) 0x2d, ( byte ) 0x05, ( byte ) 0x79}, // QipStatus: <<Evil>>
    {( byte ) 0xb7, ( byte ) 0x07, ( byte ) 0x43, ( byte ) 0x78, ( byte ) 0xf5, ( byte ) 0x0c, ( byte ) 0x77, ( byte ) 0x77, ( byte ) 0x97, ( byte ) 0x77, ( byte ) 0x57, ( byte ) 0x78, ( byte ) 0x50, ( byte ) 0x2d, ( byte ) 0x05, ( byte ) 0x70}, // QipStatus: <<Depression>>
    {( byte ) 0xb7, ( byte ) 0x07, ( byte ) 0x43, ( byte ) 0x78, ( byte ) 0xf5, ( byte ) 0x0c, ( byte ) 0x77, ( byte ) 0x77, ( byte ) 0x97, ( byte ) 0x77, ( byte ) 0x57, ( byte ) 0x78, ( byte ) 0x50, ( byte ) 0x2d, ( byte ) 0x05, ( byte ) 0x76}, // QipStatus: <<At home>>
    {( byte ) 0xb7, ( byte ) 0x07, ( byte ) 0x43, ( byte ) 0x78, ( byte ) 0xf5, ( byte ) 0x0c, ( byte ) 0x77, ( byte ) 0x77, ( byte ) 0x97, ( byte ) 0x77, ( byte ) 0x57, ( byte ) 0x78, ( byte ) 0x50, ( byte ) 0x2d, ( byte ) 0x05, ( byte ) 0x77}, // QipStatus: <<At work>>
    {( byte ) 0xb7, ( byte ) 0x07, ( byte ) 0x43, ( byte ) 0x78, ( byte ) 0xf5, ( byte ) 0x0c, ( byte ) 0x77, ( byte ) 0x77, ( byte ) 0x97, ( byte ) 0x77, ( byte ) 0x57, ( byte ) 0x78, ( byte ) 0x50, ( byte ) 0x2d, ( byte ) 0x05, ( byte ) 0x78}, // QipStatus: <<Lunch>>
    {( byte ) 0xb7, ( byte ) 0x07, ( byte ) 0x43, ( byte ) 0x78, ( byte ) 0xf5, ( byte ) 0x0c, ( byte ) 0x77, ( byte ) 0x77, ( byte ) 0x97, ( byte ) 0x77, ( byte ) 0x57, ( byte ) 0x78, ( byte ) 0x50, ( byte ) 0x2d, ( byte ) 0x05, ( byte ) 0x74}, // QipStatus: <<From phone>>
  };

  public static boolean expectIsStatus( int status ) {
    for ( int c = 0; c < statusIds.length; c++ ) {
      if ( statusIds[c] == status ) {
        return true;
      }
    }
    return false;
  }

  public static String getStatusDescr( int index ) {
    return statusesDescr[index];
  }

  public static int getStatusCount() {
    return statusesDescr.length;
  }

  public static int getStatus( int index ) {
    return statusIds[index];
  }

  public static int getStatusIndex( int statusId ) {
    for ( int c = 0; c < statusIds.length; c++ ) {
      if ( statusId == statusIds[c] ) {
        return c;
      }
    }
    return -1;
  }

  public static byte[] getAStatusCap( int aStatusId ) {
    return aStatusBytes[aStatusId - 0x1000];
  }
}
