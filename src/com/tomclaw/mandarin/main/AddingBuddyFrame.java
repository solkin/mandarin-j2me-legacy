package com.tomclaw.mandarin.main;

import com.tomclaw.mandarin.icq.IcqItem;
import com.tomclaw.mandarin.mmp.MmpAccountRoot;
import com.tomclaw.mandarin.mmp.MmpItem;
import com.tomclaw.mandarin.mmp.PacketType;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;
import java.io.IOException;
import java.util.Hashtable;
import javax.microedition.lcdui.TextField;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class AddingBuddyFrame extends Window {

  public Field buddyIdField;
  public Field buddyNickField;
  public RadioGroup buddyGroup;

  public AddingBuddyFrame( final AccountRoot accountRoot, final int winType ) {
    /**
     * winType:
     * 0x00 - ICQ item
     * 0x01 - Mail.Ru mail item
     * 0x02 - Mail.Ru phone item
     */
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
          showNotify( Localization.getMessage( "ERROR" ),
                  Localization.getMessage( "EMPTY_FIELDS" ), false );
        } else {
          try {
            // IcqPacketSender.addBuddy(icqAccountRoot.session, StringUtil.stringToByteArray(buddyIdField.getText(), true), DataUtil.getNextId(icqAccountRoot), 0x00, 0x0001, false, null);
            final BuddyGroup groupItem = ( winType == 0x02 ? null : ( BuddyGroup ) accountRoot.getBuddyItems().elementAt( buddyGroup.getCombed() ) );

            long userId = accountRoot.getNextItemId();

            Cookie cookie = accountRoot.addBuddy( buddyIdField.getText(), groupItem, buddyNickField.getText(), winType, userId );

            BuddyItem buddyItemNull = null;

            QueueAction queueAction = new QueueAction( accountRoot, buddyItemNull, cookie ) {
              public void actionPerformed( Hashtable params ) {
                switch ( winType ) {
                  case 0x00: {
                    buddyItem = new IcqItem( buddyIdField.getText(), buddyNickField.getText() );
                    ( ( GroupHeader ) groupItem ).addChild( ( GroupChild ) buddyItem );
                    break;
                  }
                  case 0x01: {
                    buddyItem = new MmpItem( buddyIdField.getText(), buddyNickField.getText() );
                    ( ( MmpItem ) buddyItem ).contactId = ( ( Long ) params.get( "contactId" ) ).longValue();
                    ( ( GroupHeader ) groupItem ).addChild( ( GroupChild ) buddyItem );
                    break;
                  }
                  case 0x02: {
                    buddyItem = new MmpItem( buddyIdField.getText(), buddyNickField.getText() );
                    buddyItem.setIsPhone( true );
                    buddyItem.setUserPhone( buddyIdField.getText() );
                    ( ( MmpItem ) buddyItem ).flags = PacketType.CONTACT_FLAG_PHONE;
                    ( ( MmpItem ) buddyItem ).contactId = ( ( Long ) params.get( "contactId" ) ).longValue();
                    ( ( MmpAccountRoot ) accountRoot ).phoneGroup.addChild( ( GroupChild ) buddyItem );
                    break;
                  }
                }
                // buddyItem.updateUiData();

                /*MmpItem mmpItem = (MmpItem) getBuddyItem();
                 if (!isTelephone) {
                 ((MmpGroup) buddyGroup).addChild(mmpItem);
                 } else {
                 LogUtil.outMessage("Phone item");
                 mmpItem.updateUiData();
                 phoneGroup.addChild(mmpItem);
                 }*/
                // accountRoot.updateMainFrameBuddyList();
                // accountRoot.updateOfflineBuddylist();

                LogUtil.outMessage( "Action Performed" );
                buddyItem.updateUiData();
                accountRoot.updateOfflineBuddylist();
              }
            };
            LogUtil.outMessage( "QueueAction created" );
            Queue.pushQueueAction( queueAction );

            // IcqPacketSender.requestBuddyList(icqAccountRoot.session);
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
    String idLabelString = "";
    int constraints = TextField.ANY;
    switch ( winType ) {
      case 0x00: {
        idLabelString = "ENTER_ID_HERE";
        constraints = TextField.NUMERIC;
        break;
      }
      case 0x01: {
        idLabelString = "ENTER_MAIL_HERE";
        constraints = TextField.EMAILADDR;
        break;
      }
      case 0x02: {
        idLabelString = "ENTER_PHONE_HERE";
        constraints = TextField.PHONENUMBER;
        break;
      }
    }
    Label idLabel = new Label( Localization.getMessage( idLabelString ) );
    idLabel.setTitle(true);
    pane.addItem( idLabel );
    buddyIdField = new Field( "" );
    buddyIdField.setFocusable( true );
    buddyIdField.setFocused( true );
    buddyIdField.constraints = constraints;
    buddyIdField.title = Localization.getMessage( "BUDDY_ID" );
    pane.addItem( buddyIdField );
    Label nickLabel = new Label( Localization.getMessage( "ENTER_NICK_HERE" ) );
    nickLabel.setTitle(true);
    pane.addItem( nickLabel );
    buddyNickField = new Field( "" );
    buddyNickField.setFocusable( true );
    buddyNickField.title = Localization.getMessage( "BUDDY_NICK" );
    pane.addItem( buddyNickField );
    if ( winType != 2 ) {
      Label groupLabel = new Label( Localization.getMessage( "SELECT_GROUP" ) );
      groupLabel.setTitle(true);
      pane.addItem( groupLabel );
      buddyGroup = new RadioGroup();
      if ( accountRoot.getBuddyItems().isEmpty() ) {
        showNotify( Localization.getMessage( "ERROR" ),
                Localization.getMessage( "NO_GROUPS" ), true );
      } else {
        BuddyGroup groupItem;
        for ( int c = 0; c < accountRoot.getBuddyItems().size(); c++ ) {
          groupItem = ( BuddyGroup ) accountRoot.getBuddyItems().elementAt( c );
          Radio radio = new Radio( groupItem.getUserId() == null ? "" : groupItem.getUserId(), false );
          radio.setFocusable( true );
          pane.addItem( radio );
          buddyGroup.addRadio( radio );
        }
        buddyGroup.setCombed( 0 );
      }
    }
    /** Applying pane **/
    setGObject( pane );
    if ( accountRoot.getStatusIndex() == 0 ) {
      showNotify( Localization.getMessage( "ERROR" ),
              Localization.getMessage( "NO_CONNECTION" ), true );
    }
  }

  public final void showNotify( final String title, final String message, final boolean isFail ) {
        Soft notifySoft = new Soft(MidletMain.screen);
        notifySoft.leftSoft = new PopupItem(Localization.getMessage( "CLOSE" ) ) {
          public void actionPerformed() {
            AddingBuddyFrame.this.closeDialog();
            if ( isFail ) {
              MidletMain.screen.setActiveWindow( s_prevWindow );
            } else {
              MidletMain.screen.repaint();
            }
          }
        };
        AddingBuddyFrame.this.showDialog( new Dialog( MidletMain.screen, notifySoft, title, message ) );
        MidletMain.screen.repaint();
  }
}
