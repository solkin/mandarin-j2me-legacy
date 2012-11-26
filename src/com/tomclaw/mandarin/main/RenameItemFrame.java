package com.tomclaw.mandarin.main;

import com.tomclaw.mandarin.xmpp.GroupChatUsersFrame;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class RenameItemFrame extends Window {

  public Field itemNameField;
  public Field phoneNumberField;

  public RenameItemFrame( final AccountRoot accountRoot, final BuddyItem buddyItem ) {
    super( MidletMain.screen );
    final boolean isPhone = buddyItem.isPhone();
    /** Header **/
    header = new Header( Localization.getMessage( "RENAME_BUDDY" ) );
    /** Creating soft **/
    soft = new Soft( MidletMain.screen );
    /** Left soft items **/
    soft.leftSoft = new PopupItem( Localization.getMessage( "CANCEL" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "RENAME" ) ) {
      public void actionPerformed() {
        LogUtil.outMessage( "Rename pressed" );
        if ( !StringUtil.isFill( itemNameField.getText() ) ) {
          LogUtil.outMessage( "Empty field" );
          showNotify( Localization.getMessage( "ERROR" ),
                  Localization.getMessage( "EMPTY_FIELD" ), false );
        } else {
          try {
            LogUtil.outMessage( "Rename start" );
            Cookie cookie = accountRoot.renameBuddy( itemNameField.getText(), buddyItem, isPhone ? phoneNumberField.getText() : "" );
            LogUtil.outMessage( "Request queued, cookie received" );
            QueueAction queueAction = new QueueAction( accountRoot, buddyItem, cookie ) {
              public void actionPerformed( Hashtable params ) {
                LogUtil.outMessage( "Action Performed" );
                buddyItem.setUserNick( itemNameField.getText() );
                if ( isPhone ) {
                  buddyItem.setUserPhone( phoneNumberField.getText() );
                }
                buddyItem.updateUiData();
                accountRoot.updateOfflineBuddylist();
              }
            };
            LogUtil.outMessage( "QueueAction created" );
            Queue.pushQueueAction( queueAction );
            LogUtil.outMessage( "queueAction: " + queueAction.getCookie().cookieString );
            LogUtil.outMessage( "Switching window..." );
            MidletMain.screen.setActiveWindow( s_prevWindow );
          } catch ( Throwable ex ) {
            // ex.printStackTrace();
            showNotify( Localization.getMessage( "ERROR" ),
                    Localization.getMessage( "IO_EXCEPTION" ), false );
          }
        }
      }
    };
    /** Creating pane **/
    Pane pane = new Pane( null, false );
    /** Creating objects **/
    Label notifyLabel = new Label( Localization.getMessage( "BUDDY_NICK" ) );
    notifyLabel.setTitle( true );
    pane.addItem( notifyLabel );
    itemNameField = new Field( buddyItem.getUserNick() );
    itemNameField.setFocusable( true );
    itemNameField.setFocused( true );
    itemNameField.title = Localization.getMessage( "BUDDY_NICK" );
    pane.addItem( itemNameField );
    if ( isPhone ) {
      Label phoneLabel = new Label( Localization.getMessage( "ENTER_PHONE_HERE" ) );
      pane.addItem( phoneLabel );
      phoneNumberField = new Field( buddyItem.getUserPhone() );
      phoneNumberField.setFocusable( true );
      phoneNumberField.title = Localization.getMessage( "PHONE" );
      pane.addItem( phoneNumberField );
    }
    /** Applying pane **/
    setGObject( pane );
    if ( accountRoot.getStatusIndex() == 0 ) {
      showNotify( Localization.getMessage( "ERROR" ),
              Localization.getMessage( "NO_CONNECTION" ), true );
    }
  }

  public RenameItemFrame( final AccountRoot accountRoot, final BuddyGroup buddyGroup ) {
    super( MidletMain.screen );
    /** Header **/
    header = new Header( Localization.getMessage( "RENAME_GROUP" ) );
    /** Creating soft **/
    soft = new Soft( MidletMain.screen );
    /** Left soft items **/
    soft.leftSoft = new PopupItem( Localization.getMessage( "CANCEL" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "RENAME" ) ) {
      public void actionPerformed() {
        if ( !StringUtil.isFill( itemNameField.getText() ) ) {
          showNotify( Localization.getMessage( "ERROR" ),
                  Localization.getMessage( "EMPTY_FIELD" ), false );
        } else {
          try {
            // IcqPacketSender.addBuddy(icqAccountRoot.session, StringUtil.stringToByteArray(itemNameField.getText(), true), DataUtil.getNextId(icqAccountRoot), 0x00, 0x0001, false, null);
            // accountRoot.renameGroup(itemNameField.getText(), buddyGroup);
            // IcqPacketSender.requestBuddyList(icqAccountRoot.session);
            // MidletMain.screen.setActiveWindow(s_prevWindow);


            LogUtil.outMessage( "Rename start" );
            Cookie cookie = accountRoot.renameGroup( itemNameField.getText(), buddyGroup );
            LogUtil.outMessage( "Request queued, cookie received" );
            QueueAction queueAction = new QueueAction( accountRoot, buddyGroup, cookie ) {
              public void actionPerformed( Hashtable params ) {
                LogUtil.outMessage( "Action Performed" );
                buddyGroup.setUserId( itemNameField.getText() );
                buddyGroup.updateUiData();
                accountRoot.updateOfflineBuddylist();
              }
            };
            LogUtil.outMessage( "QueueAction created" );
            Queue.pushQueueAction( queueAction );
            LogUtil.outMessage( "queueAction: " + queueAction.getCookie().cookieString );
            LogUtil.outMessage( "Switching window..." );
            MidletMain.screen.setActiveWindow( s_prevWindow );

          } catch ( IOException ex ) {
            showNotify( Localization.getMessage( "ERROR" ),
                    Localization.getMessage( "IO_EXCEPTION" ), false );
          }
        }
      }
    };
    /** Creating pane **/
    Pane pane = new Pane( null, false );
    /** Creating objects **/
    Label notifyLabel = new Label( Localization.getMessage( "GROUP_NAME" ) );
    notifyLabel.setTitle( true );
    pane.addItem( notifyLabel );
    itemNameField = new Field( buddyGroup.getUserId() == null ? "" : buddyGroup.getUserId() );
    itemNameField.setFocusable( true );
    itemNameField.setFocused( true );
    itemNameField.title = Localization.getMessage( "GROUP_NAME" );
    pane.addItem( itemNameField );
    /** Applying pane **/
    setGObject( pane );
    if ( accountRoot.getStatusIndex() == 0 ) {
      showNotify( Localization.getMessage( "ERROR" ),
              Localization.getMessage( "NO_CONNECTION" ), true );
    }
  }

  public final void showNotify( final String title, final String message, final boolean isFail ) {
    Soft notifySoft = new Soft( MidletMain.screen );
    notifySoft.leftSoft = new PopupItem( Localization.getMessage( "CLOSE" ) ) {
      public void actionPerformed() {
        RenameItemFrame.this.closeDialog();
        if ( isFail ) {
          MidletMain.screen.setActiveWindow( s_prevWindow );
        } else {
          MidletMain.screen.repaint();
        }
      }
    };
    RenameItemFrame.this.showDialog( new Dialog( MidletMain.screen, notifySoft, title, message ) );
    MidletMain.screen.repaint();
  }
}
