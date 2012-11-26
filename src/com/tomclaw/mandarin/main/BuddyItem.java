package com.tomclaw.mandarin.main;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public interface BuddyItem {

  public String getUserNick();

  public void setUserNick( String userNick );

  public String getUserId();

  public void setUserId( String userId );

  public int[] getLeftImages();

  public void setLeftImages( int[] leftImages );

  public int getUnreadCount();

  public int getUnreadCount( String resource );

  public void setUnreadCount( int unreadCount, String resource );

  public void updateUiData();

  public String getUserPhone();

  public boolean isPhone();

  public void setTypingStatus( boolean isTyping );

  public boolean getTypingStatus();

  public int getBuddyType();

  public void setUserPhone( String userPhone );

  public void setBuddyType( int buddyType );

  public void setIsPhone( boolean isPhone );
}
