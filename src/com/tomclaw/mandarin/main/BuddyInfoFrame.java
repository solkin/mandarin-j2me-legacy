package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import java.io.IOException;
import java.util.Enumeration;
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

  public BuddyInfoFrame( final AccountRoot accountRoot, final BuddyItem buddyItem ) {
    super( MidletMain.screen );
    this.accountRoot = accountRoot;
    this.buddyItem = buddyItem;
    /** Info request sequence number **/
    reqSeqNum = MidletMain.reqSeqNum++;
    /** Header **/
    header = new Header( Localization.getMessage( "INFO_ABOUT" ).concat( " " ).concat( buddyItem.getUserId() ) );
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
      Label idLabel = new Label( Localization.getMessage( "BUDDY_ID_LABEL" ) );
      idLabel.setTitle(true);
      pane.addItem( idLabel );
      Label idLabel_a = new Label( buddyItem.getUserId() );
      pane.addItem( idLabel_a );
      Label nickLabel = new Label( Localization.getMessage( "NICK_NAME_LABEL" ) );
      nickLabel.setTitle(true);
      pane.addItem( nickLabel );
      Label nickLabel_a = new Label( buddyItem.getUserNick() );
      pane.addItem( nickLabel_a );
    } else {
      String waitText;
      try {
        LogUtil.outMessage( "Dialog with reqSeqNum = " + reqSeqNum );
        accountRoot.requestInfo( buddyItem.getUserId(), reqSeqNum );
        waitText = "WAIT_LABEL";
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
      /**
       * This is buddy, not group
       */
      Cookie cookie = accountRoot.renameBuddy( buddyInfo.nickName, buddyItem, buddyItem.getUserPhone() );
      LogUtil.outMessage( "Request queued, cookie received" );
      QueueAction queueAction = new QueueAction( accountRoot, buddyItem, cookie ) {

        public void actionPerformed( Hashtable params ) {
          LogUtil.outMessage( "Action Performed" );
          buddyItem.setUserNick( buddyInfo.nickName );
          buddyItem.updateUiData();
          accountRoot.updateOfflineBuddylist();
        }
      };
      LogUtil.outMessage( "QueueAction created" );
      Queue.pushQueueAction( queueAction );
      LogUtil.outMessage( "queueAction: " + queueAction.getCookie().cookieString );

      BuddyInfoFrame.this.pane.items.removeElement( updateNickButton );
      MidletMain.screen.repaint();
    } catch ( IOException ex ) {
    }
  }

  public void placeInfo( final BuddyInfo buddyInfo ) {
    pane.items.removeAllElements();
    String labelMessage;
    String labelDescription;
    Label descriptionLabel;
    Enumeration keys = buddyInfo.buddyHash.keys();
    for ( int c = 0; c < buddyInfo.buddyHash.size() + 2; c++ ) {
      Label onlineLabel = new Label( "" );
      onlineLabel.setTitle(true);
      descriptionLabel = new Label( "" );
      switch ( c ) {
        case 0x00: {
          labelMessage = "BUDDY_ID_LABEL";
          labelDescription = buddyItem.getUserId();
          break;
        }
        case 0x01: {
          labelMessage = "NICK_NAME_LABEL";
          labelDescription = buddyInfo.nickName;
          if ( !buddyInfo.nickName.equals( buddyItem.getUserNick() ) ) {
            updateNickButton = new Button( Localization.getMessage( "UPDATE_NICKNAME" ) ) {

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
          labelMessage = ( String ) keys.nextElement();
          labelDescription = ( String ) buddyInfo.buddyHash.get( labelMessage );
          if ( labelDescription.equals( "" ) ) {
            continue;
          }
          break;
        }
      }
      onlineLabel.setCaption( Localization.getMessage( labelMessage ) + ": " );
      descriptionLabel.setCaption( labelDescription );

      clientBuffer += onlineLabel.caption + "\n";
      clientBuffer += labelDescription + "\n";

      pane.addItem( onlineLabel );
      pane.addItem( descriptionLabel );
    }
    soft.rightSoft = new PopupItem( Localization.getMessage( "COPY" ) ) {

      public void actionPerformed() {
        MidletMain.buffer = clientBuffer;
      }
    };
    MidletMain.screen.repaint();
  }
}
