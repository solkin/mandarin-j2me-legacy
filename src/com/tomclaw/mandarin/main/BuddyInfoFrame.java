package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class BuddyInfoFrame extends Window {

  public Pane pane;
  public int reqSeqNum;
  public BuddyItem buddyItem;
  public AccountRoot accountRoot;
  public String clientBuffer = "";
  private Button updateNickButton;

  public BuddyInfoFrame( final AccountRoot accountRoot,
          final BuddyItem buddyItem ) {
    super( MidletMain.screen );
    this.accountRoot = accountRoot;
    this.buddyItem = buddyItem;
    /** Info request sequence number **/
    reqSeqNum = MidletMain.reqSeqNum++;
    /** Header **/
    header = new Header( Localization.getMessage( "USER_SUMMARY" ) );
    /** Creating soft **/
    soft = new Soft( MidletMain.screen );
    /** Left soft items **/
    soft.leftSoft = new PopupItem( Localization.getMessage( "CLOSE" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    soft.rightSoft = new PopupItem( "" );
    /** Initializing pane **/
    pane = new Pane( null, false );
    if ( accountRoot.getStatusIndex() == 0 || buddyItem.isPhone() ) {
      pane.addItem( new Label( new RichContent( "[p][b]"
              + Localization.getMessage( "BUDDY_ID_LABEL" ) + ": [/b]"
              + buddyItem.getUserId() + "[/p]" ) ) );
      pane.addItem( new Label( new RichContent( "[p][b]"
              + Localization.getMessage( "NICK_NAME_LABEL" ) + ": [/b]"
              + buddyItem.getUserNick() + "[/p]" ) ) );
    } else {
      String waitText;
      LogUtil.outMessage( "Dialog with reqSeqNum = " + reqSeqNum );
      waitText = "WAIT_LABEL";
      try {
        accountRoot.requestInfo( buddyItem.getUserId(), reqSeqNum );
      } catch ( IOException ex ) {
        waitText = "IO_EXCEPTION";
      }
      Label waitLabel = new Label( Localization.getMessage( waitText ) );
      pane.addItem( waitLabel );
    }
    /** Applying pane **/
    setGObject( pane );
  }

  public void updateNickAction( final BuddyInfo buddyInfo ) {
    try {
      /** This is buddy, not group **/
      Cookie cookie = accountRoot.renameBuddy( buddyInfo.nickName, buddyItem,
              buddyItem.getUserPhone() );
      LogUtil.outMessage( "Request queued, cookie received" );
      QueueAction queueAction = new QueueAction(
              accountRoot, buddyItem, cookie ) {
        public void actionPerformed( Hashtable params ) {
          LogUtil.outMessage( "Action Performed" );
          this.buddyItem.setUserNick( buddyInfo.nickName );
          this.buddyItem.updateUiData();
          this.accountRoot.updateOfflineBuddylist();
        }
      };
      LogUtil.outMessage( "QueueAction created" );
      Queue.pushQueueAction( queueAction );
      LogUtil.outMessage( "queueAction: "
              + queueAction.getCookie().cookieString );

      BuddyInfoFrame.this.pane.items.removeElement( updateNickButton );
      MidletMain.screen.repaint();
    } catch ( IOException ex ) {
    }
  }

  public void placeInfo( final BuddyInfo buddyInfo ) {
    pane.items.removeAllElements();
    if ( buddyInfo.avatar != null ) {
      Label avatarLabel = new Label( buddyInfo.nickName );
      ( ( PlainContent ) avatarLabel.getContent() ).setBold( true );
      avatarLabel.setHeader( true );
      ( ( PlainContent ) avatarLabel.getContent() ).image = buddyInfo.avatar;
      pane.addItem( avatarLabel );
    }
    String labelMessage;
    String labelDescription;
    Label infoLabel;
    for ( int c = 0; c < buddyInfo.getKeyValueSize() + 2; c++ ) {
      RichContent content = new RichContent( "" );
      infoLabel = new Label( content );
      switch ( c ) {
        case 0x00: {
          labelMessage = "BUDDY_ID_LABEL";
          if ( buddyItem.getUserId().endsWith( "@uin.icq" ) ) {
            labelDescription = buddyItem.getUserId().substring(
                    0, buddyItem.getUserId().indexOf( "@uin.icq" ) );
          } else {
            labelDescription = buddyItem.getUserId();
          }
          break;
        }
        case 0x01: {
          labelMessage = "NICK_NAME_LABEL";
          labelDescription = buddyInfo.nickName;
          if ( !buddyInfo.nickName.equals( buddyItem.getUserNick() ) ) {
            updateNickButton = new Button(
                    Localization.getMessage( "UPDATE_NICKNAME" ) ) {
              public void actionPerformed() {
                updateNickAction( buddyInfo );
              }
            };
            updateNickButton.setFocusable( true );
            updateNickButton.setFocused( true );
            pane.addItem( updateNickButton );
          }
          break;
        }
        default: {
          BuddyInfo.KeyValue keyValue = buddyInfo.getKeyValue( c - 2 );
          labelMessage = keyValue.key;
          labelDescription = keyValue.value;
          if ( StringUtil.isNullOrEmpty( labelDescription ) ) {
            continue;
          }
          break;
        }
      }
      content.setText( "[p][b]" + Localization.getMessage( labelMessage )
              + ": [/b]" + labelDescription + "[/p]" );

      clientBuffer += infoLabel.getContent().getText() + "\n";
      clientBuffer += labelDescription + "\n";

      pane.addItem( infoLabel );
    }
    soft.rightSoft = new PopupItem( Localization.getMessage( "COPY" ) ) {
      public void actionPerformed() {
        MidletMain.buffer = clientBuffer;
      }
    };
    MidletMain.screen.repaint();
    /** Collecting garbage **/
    Runtime.getRuntime().gc();
  }
}
