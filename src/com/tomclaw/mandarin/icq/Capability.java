package com.tomclaw.mandarin.icq;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class Capability {

  public static final int CAP_PLAIN = 0x00;
  public static final int CAP_CLIENTID = 0x02;
  public static final int CAP_XSTATUS = 0x03;
  public static final int CAP_ASTATUS = 0x01;
  
  public byte[] capBytes = null;
  public String capName = null;
  public int capType = -1;
  public String capIcon = null;

  public Capability() {
    this.capBytes = new byte[ 16 ];
  }

  public Capability( byte[] capBytes ) {
    this.capBytes = capBytes;
  }

  public Capability( byte[] capBytes, String capName, int capType, String capIcon ) {
    this.capBytes = capBytes;
    this.capName = capName;
    this.capType = capType;
    this.capIcon = capIcon;
  }
}
