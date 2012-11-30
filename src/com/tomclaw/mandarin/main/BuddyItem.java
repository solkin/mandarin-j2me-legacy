package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.GroupChild;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public abstract class BuddyItem extends GroupChild {
  
  public BuddyItem( String userId ) {
    super( userId );
  }

  public String getUserNick() {
    return userNick;
  }

  public void setUserNick( String userNick ) {
    this.userNick = userNick;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId( String userId ) {
    this.userId = userId;
  }

  public int[] getLeftImages() {
    return this.imageLeftIndex;
  }

  public void setLeftImages( int[] leftImages ) {
    this.imageLeftIndex = leftImages;
  }
  
  public abstract int getUnreadCount();

  public abstract int getUnreadCount( String resource );

  public abstract void setUnreadCount( int unreadCount, String resource );

  public abstract void updateUiData();

  public String getUserPhone() {
    if ( userPhone == null ) {
      return "";
    }
    return userPhone;
  }

  public boolean isPhone() {
    return isPhone;
  }

  public void setTypingStatus( boolean isTyping );

  public boolean getTypingStatus();

  public int getBuddyType();

  public void setUserPhone( String userPhone );

  public void setBuddyType( int buddyType );

  public void setIsPhone( boolean isPhone );
}
