package com.tomclaw.mandarin.main;

import com.tomclaw.mandarin.core.*;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;
import java.util.Hashtable;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class RenameItemFrame extends Window {

  private Field itemNameField;
  private Field phoneNumberField;

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
          Handler.showNotify( Localization.getMessage( "EMPTY_FIELD" ) );
        } else {
          try {
            LogUtil.outMessage( "Rename start" );
            Cookie cookie = accountRoot.renameBuddy( itemNameField.getText(), buddyItem, isPhone ? phoneNumberField.getText() : "" );
            LogUtil.outMessage( "Request queued, cookie received" );
            QueueAction queueAction = new QueueAction( accountRoot, buddyItem, cookie ) {

              public void actionPerformed( Hashtable params ) {
                LogUtil.outMessage( "Action Performed" );
                this.buddyItem.setUserNick( itemNameField.getText() );
                if ( isPhone ) {
                  this.buddyItem.setUserPhone( phoneNumberField.getText() );
                }
                this.buddyItem.updateUiData();
                this.accountRoot.updateOfflineBuddylist();
              }
            };
            LogUtil.outMessage( "QueueAction created" );
            Queue.pushQueueAction( queueAction );
            LogUtil.outMessage( "queueAction: " + queueAction.getCookie().cookieString );
            LogUtil.outMessage( "Switching window..." );
            MidletMain.screen.setActiveWindow( s_prevWindow );
          } catch ( Throwable ex ) {
            Handler.showError( Localization.getMessage( "IO_EXCEPTION" ) );
          }
        }
      }
    };
    /** Creating pane **/
    Pane pane = new Pane( null, false );
    /** Creating objects **/
    Label notifyLabel = new Label( Localization.getMessage( "BUDDY_RENAME" ) );
    notifyLabel.setHeader( true );
    pane.addItem( notifyLabel );
    pane.addItem( new Label( Localization.getMessage( "BUDDY_RENAME_INFO" ) ) );
    itemNameField = new Field( buddyItem.getUserNick() );
    itemNameField.setFocusable( true );
    itemNameField.setFocused( true );
    itemNameField.title = Localization.getMessage( "BUDDY_NICK" );
    pane.addItem( itemNameField );
    if ( isPhone ) {
      pane.addItem( new Label( Localization.getMessage( "PHONE_EDIT" ) ) );
      phoneNumberField = new Field( buddyItem.getUserPhone() );
      phoneNumberField.setFocusable( true );
      phoneNumberField.title = Localization.getMessage( "VALIDATED_CELLULAR_LABEL" );
      pane.addItem( phoneNumberField );
    }
    /** Applying pane **/
    setGObject( pane );
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
          Handler.showNotify( Localization.getMessage( "EMPTY_FIELD" ) );
        } else {
          LogUtil.outMessage( "Rename start" );
          Cookie cookie = accountRoot.renameGroup( itemNameField.getText(), buddyGroup );
          LogUtil.outMessage( "Request queued, cookie received" );
          QueueAction queueAction = new QueueAction( accountRoot, buddyGroup, cookie ) {

            public void actionPerformed( Hashtable params ) {
              LogUtil.outMessage( "Action Performed" );
              this.buddyGroup.setUserId( itemNameField.getText() );
              this.buddyGroup.updateUiData();
              this.accountRoot.updateOfflineBuddylist();
            }
          };
          LogUtil.outMessage( "QueueAction created" );
          Queue.pushQueueAction( queueAction );
          LogUtil.outMessage( "queueAction: " + queueAction.getCookie().cookieString );
          LogUtil.outMessage( "Switching window..." );
          MidletMain.screen.setActiveWindow( s_prevWindow );
        }
      }
    };
    /** Creating pane **/
    Pane pane = new Pane( null, false );
    /** Creating objects **/
    Label notifyLabel = new Label( Localization.getMessage( "GROUP_RENAME" ) );
    notifyLabel.setHeader( true );
    pane.addItem( notifyLabel );
    pane.addItem( new Label( Localization.getMessage( "GROUP_RENAME_INFO" ) ) );
    itemNameField = new Field( buddyGroup.getUserId() == null ? "" : buddyGroup.getUserId() );
    itemNameField.setFocusable( true );
    itemNameField.setFocused( true );
    itemNameField.title = Localization.getMessage( "GROUP_NAME" );
    pane.addItem( itemNameField );
    /** Applying pane **/
    setGObject( pane );
  }
}
