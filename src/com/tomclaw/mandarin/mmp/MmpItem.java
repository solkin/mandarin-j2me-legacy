package com.tomclaw.mandarin.mmp;

import com.tomclaw.mandarin.main.BuddyItem;
import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.tcuilite.ChatItem;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class MmpItem extends BuddyItem {

  /** General **/
  public long groupId;
  public int buddyId;
  public long servFlags;
  public long flags;
  public long contactId;
  /** Privacy **/
  public boolean isInPermitList = false;
  public boolean isInDenyList = false;
  public boolean isInIgnoreList = false;
  public int permitBuddyId = 0x00;
  public int denyBuddyId = 0x00;
  public int ignoreBuddyId = 0x00;

  public MmpItem() {
    super();
    /** Default values **/
    buddyId = 0;
    groupId = 0;
    buddyType = 0;
    imageLeftIndex = new int[ 1 ];
  }

  public MmpItem( String userId ) {
    super( userId );
    /** Default values **/
    buddyId = 0;
    groupId = 0;
    buddyType = 0;
    imageLeftIndex = new int[ 1 ];
  }

  public MmpItem( String userId, String userNick ) {
    super( userNick );
    this.userNick = userNick;
    /** Default values **/
    buddyId = 0;
    groupId = 0;
    buddyType = 0;
    imageLeftIndex = new int[ 1 ];
  }

  public MmpItem( String userId, String userNick, int buddyType ) {
    this( userId, userNick );
    this.buddyType = buddyType;
  }

  public void updateUiData() {
    this.title = userNick;
    int chatImage = -1;
    weight = 0;
    if ( getStatusIndex() != 0 && MidletMain.isSortOnline ) {
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
    imageLeftIndex = new int[] { chatImage, isPhone() ? MmpStatusUtil.phoneStatus
      : getStatusIndex(), -1 };
    imageRightIndex = new int[] { -1, -1, -1, -1 };
    if ( MidletMain.chatFrame != null ) {
      isBold = MidletMain.chatFrame.getChatTab( userId ) != null;
      weight = isBold ? -3 : weight;
    }
  }
}
