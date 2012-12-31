package com.tomclaw.mandarin.dc;

import java.io.IOException;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public interface DirectConnection {

  public String getBuddyId();

  public byte[] getFileName();

  public long getFileByteSize();

  public String getProxyIp();

  public int getProxyPort();

  public String getStatusString();

  public int getPercentValue();

  public int getSpeed();

  public void sendStop() throws IOException;

  public boolean isErrorFlag();

  public boolean isReceivingFileFlag();

  public boolean isCompleteFlag();

  public boolean isStopFlag();

  public void setTransactionInfo( byte[] fileName, String fileLocation, long fileByteSize, String buddyId );

  public void setIsReceivingFile( boolean isReceivingFile );

  public boolean isCompare( DirectConnection directConnection );

  public byte[] getSessCookie();

  public void generateCookie();

  public void sendFile() throws IOException, InterruptedException;
}
