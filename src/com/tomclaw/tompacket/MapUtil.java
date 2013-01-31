package com.tomclaw.tompacket;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class MapUtil {

  public static String[] types = new String[]{ "BYT", "CHR", "INT", "LNG", "STR", "ARR", "MAP" };
  public static int[] sizes = new int[]{ 1, 1, 2, 4, 0, 0, 0 };
  public static int tpVersion = 0x02;
  public static int SIZE_16BIT = ( int ) Character.MAX_VALUE + 1;
}
