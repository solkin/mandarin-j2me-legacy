package com.tomclaw.mandarin.icq;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class ClientInfo {

  public long signOnTime = 0;
  public int idleTime = 0;
  public long memberSinceTime = 0;
  public byte[] externalIp = new byte[4];
  public byte[] internalIp = new byte[4];
  public long dcTcpPort = 0;
  public byte dcType = 0;
  public int dcProtocolVersion = 0;
  public long dcAuthCookie = 0;
  public long webFrontPort = 0;
  public long clientFeatures = 0;
  /**
   * Flags
   */
  public long lastInfoUpdateTime = 0;
  public long lastExtInfoUpdateTime = 0;
  public long lastExtStatusUpdateTime = 0;
  /**
   * Unknown
   */
  public int unk = 0;
  /**
   * Other
   */
  public long onLineTime = 0;
}
