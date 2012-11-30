package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.GroupChild;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public abstract class BuddyItem extends GroupChild {

  /**
   * Variables
   */
  public String userId;
  public String userNick;
  public String userPhone;
  public boolean isPhone;
  public boolean typingStatus;
  public int buddyType;
  /**
   * Typing thread
   */
  private Timer typingTimer;
  private TimerTask timerTask;

  public BuddyItem( String userId ) {
    super( userId );
    this.userId = userId;
    this.userNick = userId;
  }

  public void setUserNick( String userNick ) {
    this.userNick = userNick;
  }

  public void setUserId( String userId ) {
    this.userId = userId;
  }

  public void setLeftImages( int[] leftImages ) {
    this.imageLeftIndex = leftImages;
  }
  
  public abstract void setUnreadCount( int unreadCount, String resource );

  public void setUserPhone( String userPhone ) {
    this.userPhone = userPhone;
    userId = userPhone;
  }

  public void setTypingStatus( boolean isTyping ) {
    this.typingStatus = isTyping;
    if ( typingTimer != null ) {
      typingTimer.cancel();
      typingTimer = null;
    }
    if ( isTyping ) {
      timerTask = new TimerTask() {
        public void run() {
          BuddyItem.this.typingStatus = false;
          BuddyItem.this.updateUiData();
          MidletMain.screen.repaint();
          BuddyItem.this.typingTimer = null;
          BuddyItem.this.timerTask = null;
        }
      };
      typingTimer = new java.util.Timer();
      typingTimer.schedule( timerTask, 25000 );
    }
  }

  public void setIsPhone( boolean isPhone ) {
    this.isPhone = isPhone;
  }

  public void setBuddyType( int buddyType ) {
    this.buddyType = buddyType;
  }

  public String getUserId() {
    return userId;
  }

  public int[] getLeftImages() {
    return this.imageLeftIndex;
  }

  public abstract int getUnreadCount();

  public abstract int getUnreadCount( String resource );

  public abstract void updateUiData();

  public String getUserPhone() {
    if ( userPhone == null ) {
      return "";
    }
    return userPhone;
  }

  public boolean getTypingStatus() {
    return typingStatus;
  }

  public String getUserNick() {
    return userNick;
  }
  
  public int getBuddyType() {
    return buddyType;
  }

  public boolean isPhone() {
    return isPhone;
  }
}
