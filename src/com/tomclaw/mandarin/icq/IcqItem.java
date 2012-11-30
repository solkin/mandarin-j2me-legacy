package com.tomclaw.mandarin.icq;

import com.tomclaw.mandarin.main.BuddyItem;
import com.tomclaw.mandarin.main.IconsType;
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
public class IcqItem extends GroupChild implements BuddyItem {

  /**
   * Buddy types
   */
  public static final int ROOT_GROUP = 0x0000;
  public static final int NORMAL_GROUP = 0x0001;
  public static final int NORMAL_BUDDY = 0x0002;
  public static final int SYSTEM_GROUP = 0x0003;
  public static final int PHANTOM_BUDDY = 0x0004;
  public static final int PERMIT_LIST_BUDDY = 0x0005;
  public static final int DENY_LIST_BUDDY = 0x0006;
  public static final int IGNORE_LIST_BUDDY = 0x0007;
  /**
   * Another kind
   */
  public static final int ACCOUNT_ROOT = 0xffff;
  /**
   * Variables
   */
  public String userId;
  public int groupId;
  public int buddyId;
  public String userNick;
  public boolean isAvaitingAuth;
  public int buddyType;
  public int buddyStatus;
  public int unreadCount = 0;
  public Capability[] capabilities = new Capability[]{};
  public ClientInfo clientInfo = new ClientInfo();
  public boolean typingStatus = false;
  private Capability aStatusCap = null;
  private Capability aClient = null;
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

  public IcqItem() {
    super( "" );
    buddyId = 0;
    groupId = 0;
    buddyType = 0;
    isAvaitingAuth = false;
    buddyStatus = -1;
    this.imageLeftIndex = new int[ 1 ];
  }

  public IcqItem( String userId ) {
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
    buddyStatus = -1;
    this.imageLeftIndex = new int[ 1 ];
  }

  public IcqItem( String userId, String userNick ) {
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
    buddyStatus = -1;
    this.imageLeftIndex = new int[ 1 ];
  }

  public IcqItem( String userId, String userNick, int buddyType ) {
    this( userId, userNick );
    this.buddyType = buddyType;
    this.imageLeftIndex = new int[ 1 ];
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
    return "";
  }

  public boolean isPhone() {
    return false;
  }

  public void updateUiData() {
    this.title = userNick;
    int xStatus = -1;
    int chatImage = -1;
    if ( capabilities != null ) {
      Capability xStatusCap = CapUtil.getCapabilityByType( capabilities, Capability.CAP_XSTATUS );
      if ( xStatusCap != null ) {
        xStatus = Integer.parseInt( xStatusCap.capIcon.substring( 7 ) );
      }
    }
    weight = 0;
    if ( capabilities != null && buddyStatus != -1 && MidletMain.isSortOnline ) {
      aStatusCap = CapUtil.getCapabilityByType( capabilities, Capability.CAP_ASTATUS );
      weight = -2;
    } else if ( buddyStatus != -1 && MidletMain.isSortOnline ) {
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
    this.imageLeftIndex = new int[]{ chatImage, ( aStatusCap != null && buddyStatus != -1 ) ? ( Integer.parseInt( aStatusCap.capIcon.substring( 9 ) ) ) : IcqStatusUtil.getStatusIndex( buddyStatus ), xStatus };
    this.imageRightIndex = new int[]{ -1, -1, -1, -1, -1 };
    if ( this.isInPermitList ) {
      imageRightIndex[0] = IconsType.PLIST_VISIBLE;
    }
    if ( this.isInDenyList ) {
      imageRightIndex[1] = IconsType.PLIST_INVISIBLE;
    }
    if ( this.isInIgnoreList ) {
      imageRightIndex[2] = IconsType.PLIST_IGNORE;
    }
    if ( capabilities != null ) {
      aClient = CapUtil.getCapabilityByType( capabilities, Capability.CAP_CLIENTID );
      if ( aClient != null && !aClient.capName.equals( "" ) ) {
        imageRightIndex[3] = Integer.parseInt( aClient.capIcon.substring( 7 ) );
        // Logger.outMessage("imageRightIndex[3] = " + imageRightIndex[3]);
      }
    }
    if ( MidletMain.chatFrame != null ) {
      isBold = ( MidletMain.chatFrame.getChatTab( userId ) != null
              || MidletMain.getBoolean( MidletMain.uniquest, String.valueOf( "icq" + getUserId().hashCode() ), "ON_TOP" ) );
      weight = isBold ? -3 : weight;
    }
    try {
      if ( MidletMain.uniquest.getGroup( String.valueOf( "icq" + getUserId().hashCode() ) ) != null ) {
        imageRightIndex[4] = 25;
      }
    } catch ( Throwable ex ) {
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
          IcqItem.this.typingStatus = false;
          IcqItem.this.updateUiData();
          MidletMain.screen.repaint();
          IcqItem.this.typingTimer = null;
          IcqItem.this.timerTask = null;
        }
      };
      typingTimer = new java.util.Timer();
      typingTimer.schedule( timerTask, 25000 );
    }
  }

  public boolean getTypingStatus() {
    return typingStatus;
  }

  public int getBuddyType() {
    return buddyType;
  }

  public void setUserPhone( String userPhone ) {
  }

  public void setBuddyType( int buddyType ) {
    this.buddyType = buddyType;
  }

  public void setIsPhone( boolean isPhone ) {
  }
}
