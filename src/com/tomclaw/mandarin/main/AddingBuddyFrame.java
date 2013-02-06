package com.tomclaw.mandarin.main;

import com.tomclaw.mandarin.mmp.MmpAccountRoot;
import com.tomclaw.mandarin.mmp.MmpItem;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;
import java.io.IOException;
import java.util.Hashtable;
import javax.microedition.lcdui.TextField;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class AddingBuddyFrame extends Window {

  /** Constants **/
  private static final int TYPE_ICQ = 0x00;
  private static final int TYPE_MMP = 0x01;
  private static final int TYPE_PHONE = 0x02;
  /** Fields **/
  public Field buddyIdField;
  public Field buddyNickField;
  public RadioGroup buddyGroup;

  public AddingBuddyFrame( final AccountRoot accountRoot, final int winType ) {
    super( MidletMain.screen );
    /** Header **/
    header = new Header( Localization.getMessage( "ADD_BUDDY" ) );
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
        if ( !StringUtil.isFill( buddyIdField.getText() )
                || !StringUtil.isFill( buddyNickField.getText() ) ) {
          ActionExec.showNotify( Localization.getMessage( "EMPTY_FIELDS" ) );
        } else {
          try {
            BuddyGroup groupItem = ( winType == TYPE_PHONE
                    ? ( ( MmpAccountRoot ) accountRoot ).phoneGroup
                    : ( BuddyGroup ) accountRoot.getBuddyItems()
                    .elementAt( buddyGroup.getCombed() ) );

            BuddyItem buddyItem = accountRoot.getBuddyInstance();
            buddyItem.setIsPhone( winType == TYPE_PHONE );
            buddyItem.setUserId( buddyIdField.getText() );
            buddyItem.setUserNick( buddyNickField.getText() );

            Cookie cookie = accountRoot.addBuddy( buddyItem, groupItem );

            QueueAction queueAction = new QueueAction( accountRoot,
                    buddyItem, cookie ) {
              public void actionPerformed( Hashtable params ) {
                switch ( winType ) {
                  case TYPE_MMP:
                  case TYPE_PHONE: {
                    ( ( MmpItem ) this.buddyItem ).contactId =
                            ( ( Long ) params.get( "contactId" ) ).longValue();
                    break;
                  }
                }
                getBuddyGroup().addChild( this.buddyItem );
                LogUtil.outMessage( "Action Performed" );
                this.buddyItem.updateUiData();
                this.accountRoot.updateOfflineBuddylist();
              }
            };
            queueAction.setBuddyGroup( groupItem );
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
    String idLabelString = "";
    int constraints = TextField.ANY;
    switch ( winType ) {
      case TYPE_ICQ: {
        idLabelString = "ENTER_ID_HERE";
        constraints = TextField.NUMERIC;
        break;
      }
      case TYPE_MMP: {
        idLabelString = "ENTER_MAIL_HERE";
        constraints = TextField.EMAILADDR;
        break;
      }
      case TYPE_PHONE: {
        idLabelString = "ENTER_PHONE_HERE";
        constraints = TextField.PHONENUMBER;
        break;
      }
    }
    Label idLabel = new Label( Localization.getMessage( "ADDING_BUDDY" ) );
    idLabel.setHeader( true );
    pane.addItem( idLabel );
    pane.addItem( new Label( Localization.getMessage( idLabelString ) ) );
    buddyIdField = new Field( "" );
    buddyIdField.setFocusable( true );
    buddyIdField.setFocused( true );
    buddyIdField.constraints = constraints;
    buddyIdField.title = Localization.getMessage( "BUDDY_ID" );
    pane.addItem( buddyIdField );
    pane.addItem( new Label( Localization.getMessage( "ENTER_NICK_HERE" ) ) );
    buddyNickField = new Field( "" );
    buddyNickField.setFocusable( true );
    buddyNickField.title = Localization.getMessage( "BUDDY_NICK" );
    pane.addItem( buddyNickField );
    if ( winType != TYPE_PHONE ) {
      pane.addItem( new Label( Localization.getMessage( "SELECT_GROUP" ) ) );
      buddyGroup = new RadioGroup();
      if ( accountRoot.getBuddyItems().isEmpty() ) {
        ActionExec.showFail( Localization.getMessage( "NO_GROUPS" ) );
      } else {
        BuddyGroup groupItem;
        for ( int c = 0; c < accountRoot.getBuddyItems().size(); c++ ) {
          groupItem = ( BuddyGroup ) accountRoot.getBuddyItems().elementAt( c );
          Radio radio = new Radio( groupItem.getUserId() == null ? ""
                  : groupItem.getUserId(), false );
          radio.setFocusable( true );
          pane.addItem( radio );
          buddyGroup.addRadio( radio );
        }
        buddyGroup.setCombed( 0 );
      }
    }
    /** Applying pane **/
    setGObject( pane );
  }
}
