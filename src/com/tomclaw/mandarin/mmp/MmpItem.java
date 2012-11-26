package com.tomclaw.mandarin.mmp;

import com.tomclaw.mandarin.main.BuddyItem;
import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.tcuilite.ChatItem;
import com.tomclaw.tcuilite.GroupChild;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class MmpItem extends GroupChild implements BuddyItem {

  public String userId;
  public long groupId;
  public int buddyId;
  public String userNick;
  public boolean isAvaitingAuth;
  public int buddyType;
  public long buddyStatus;
  public int unreadCount = 0;
  public boolean typingStatus = false;
  public long servFlags;
  public String userPhone;
  public boolean isPhone;
  public long flags;
  public long contactId;
  /**
   * Privacy
   */
  public boolean isInPermitList = false;
  public boolean isInDenyList = false;
  public boolean isInIgnoreList = false;
  public int permitBuddyId = 0x00;
  public int denyBuddyId = 0x00;
  public int ignoreBuddyId = 0x00;
  /** 
   * Typing thread
   */
  private Timer typingTimer;
  private TimerTask timerTask;

  public MmpItem() {
    super( "" );
  }

  public MmpItem( String userId ) {
    super( userId );
    /**
     * Default values
     */
    this.userId = userId;
    userNick = userId;
    buddyId = 0;
    groupId = 0;
    buddyType = 0;
    isAvaitingAuth = false;
    buddyStatus = 0;
    this.imageLeftIndex = new int[1];
  }

  public MmpItem( String userId, String userNick ) {
    super( userNick );
    this.userId = userId;
    this.userNick = userNick;
    /**
     * Default values
     */
    buddyId = 0;
    groupId = 0;
    buddyType = 0;
    isAvaitingAuth = false;
    buddyStatus = 0;
    this.imageLeftIndex = new int[1];
  }

  public MmpItem( String userId, String userNick, int buddyType ) {
    this( userId, userNick );
    this.buddyType = buddyType;
    this.imageLeftIndex = new int[1];
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

  public int getUnreadCount() {
    return unreadCount;
  }

  public int getUnreadCount( String resource ) {
    return unreadCount;
  }

  public void setUnreadCount( int unreadCount, String resource ) {
    this.unreadCount = unreadCount;
  }

  public String getUserPhone() {
    if ( userPhone == null ) {
      return "";
    }
    return userPhone;
  }

  public boolean isPhone() {
    return isPhone;
  }

  public void updateUiData() {
    this.title = userNick;
    int chatImage = -1;
    weight = 0;
    if ( !MmpStatusUtil.expectIsStatus( buddyStatus ) ) {
      buddyStatus = MmpStatusUtil.getStatus( 1 );
    }
    if ( buddyStatus != 0 && MidletMain.isSortOnline ) {
      weight = -2;
    }
    if ( unreadCount > 0 ) {
      chatImage = ChatItem.TYPE_PLAIN_MSG;
      if ( MidletMain.isRaiseUnread ) {
        weight = -3;
      }
    }
    if ( typingStatus ) {
      chatImage = 11;
    }
    this.imageLeftIndex = new int[]{chatImage, isPhone ? MmpStatusUtil.phoneStatus : MmpStatusUtil.getStatusIndex( buddyStatus ), -1};
    // Logger.outMessage(userNick);
    this.imageRightIndex = new int[]{-1, -1, -1, -1};
    if ( MidletMain.chatFrame != null ) {
      isBold = MidletMain.chatFrame.getChatTab( userId ) != null;
      weight = isBold ? -3 : weight;
    }
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
          MmpItem.this.typingStatus = false;
          MmpItem.this.updateUiData();
          MidletMain.screen.repaint();
          MmpItem.this.typingTimer = null;
          MmpItem.this.timerTask = null;
        }
      };
      typingTimer = new java.util.Timer();
      typingTimer.schedule( timerTask, 10000 );
    }
  }

  public boolean getTypingStatus() {
    return typingStatus;
  }

  public int getBuddyType() {
    return buddyType;
  }

  public void setUserPhone( String userPhone ) {
    this.userPhone = userPhone;
    userId = userPhone;
  }

  public void setBuddyType( int buddyType ) {
    this.buddyType = buddyType;
  }

  public void setIsPhone( boolean isPhone ) {
    this.isPhone = isPhone;
  }
}
