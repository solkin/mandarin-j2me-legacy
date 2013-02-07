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
public class AddingGroupFrame extends Window {

  private Field groupNameField;

  public AddingGroupFrame( final AccountRoot accountRoot ) {
    super( MidletMain.screen );
    /** Header **/
    header = new Header( Localization.getMessage( "ADD_GROUP" ) );
    /** Creating soft **/
    soft = new Soft( MidletMain.screen );
    /** Left soft items **/
    soft.leftSoft = new PopupItem( Localization.getMessage( "CANCEL" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "ADD" ) ) {
      public void actionPerformed() {
        if ( !StringUtil.isFill( groupNameField.getText() ) ) {
          ActionExec.showNotify( Localization.getMessage( "EMPTY_FIELD" ) );
        } else {
          try {
            BuddyGroup buddyGroup = accountRoot.getGroupInstance();
            buddyGroup.setUserId( groupNameField.getText() );
            Cookie cookie = accountRoot.addGroup( buddyGroup );
            QueueAction queueAction = new QueueAction(
                    accountRoot, buddyGroup, cookie ) {
              public void actionPerformed( Hashtable params ) {
                this.accountRoot.getBuddyItems().addElement( this.buddyGroup );
                LogUtil.outMessage( "Action Performed" );
                this.buddyGroup.updateUiData();
                this.accountRoot.updateOfflineBuddylist();
              }
            };
            LogUtil.outMessage( "QueueAction created" );
            Queue.pushQueueAction( queueAction );
            MidletMain.screen.setActiveWindow( s_prevWindow );
          } catch ( IOException ex ) {
            ActionExec.showError( Localization.getMessage( "IO_EXCEPTION" ) );
          }
        }
      }
    };
    /** Creating pane **/
    Pane pane = new Pane( null, false );
    /** Creating objects **/
    Label notifyLabel = new Label( Localization.getMessage( "GROUP_ADDING" ) );
    notifyLabel.setHeader( true );
    pane.addItem( notifyLabel );
    pane.addItem( new Label( Localization.getMessage( "ENTER_NAME_HERE" ) ) );
    groupNameField = new Field( "" );
    groupNameField.setFocusable( true );
    groupNameField.setFocused( true );
    groupNameField.title = Localization.getMessage( "GROUP_NAME" );
    pane.addItem( groupNameField );
    /** Applying pane **/
    setGObject( pane );
  }
}
