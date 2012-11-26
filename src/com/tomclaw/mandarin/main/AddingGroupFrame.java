package com.tomclaw.mandarin.main;

import com.tomclaw.mandarin.icq.IcqAccountRoot;
import com.tomclaw.mandarin.icq.IcqGroup;
import com.tomclaw.mandarin.mmp.MmpAccountRoot;
import com.tomclaw.mandarin.mmp.MmpGroup;
import com.tomclaw.mandarin.mmp.PacketType;
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
public class AddingGroupFrame extends Window {

  public Field groupNameField;

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
          showNotify( Localization.getMessage( "ERROR" ),
                  Localization.getMessage( "EMPTY_FIELD" ), false );
        } else {
          try {
            final long groupId = accountRoot.getNextItemId();
            Cookie cookie = accountRoot.addGroup( groupNameField.getText(), groupId );

            BuddyGroup groupItemNull = null;

            QueueAction queueAction = new QueueAction( accountRoot, groupItemNull, cookie ) {

              public void actionPerformed( Hashtable params ) {
                if ( accountRoot instanceof IcqAccountRoot ) {
                  buddyGroup = new IcqGroup( groupNameField.getText() );
                  ( ( IcqGroup ) buddyGroup ).groupId = ( int ) groupId;
                } else if ( accountRoot instanceof MmpAccountRoot ) {
                  buddyGroup = new MmpGroup( groupNameField.getText() );
                  ((MmpGroup)buddyGroup).contactId = ((Long) params.get( "contactId" )).longValue();
                  // long flags = PacketType.CONTACT_FLAG_GROUP;
                  /*flags = ( ( ( long ) ( groupId & 0xff ) ) << 24 ) & 0xFF000000;
                  flags |= ( ( ( long ) ( ( groupId >> 8 ) & 0xff ) ) << 16 ) & 0x00FF0000;
                  flags |= ( ( ( long ) ( byte ) ( ( groupId >> 16 ) & 0xff ) ) << 8 ) & 0x0000FF00;
                  flags |= ( ( ( long ) ( byte ) ( ( groupId >> 24 ) & 0xff ) ) ) & 0x000000FF;*/
                  ( ( MmpGroup ) buddyGroup ).flags = (int)PacketType.CONTACT_FLAG_GROUP;
                }
                accountRoot.getBuddyItems().addElement( buddyGroup );

                LogUtil.outMessage( "Action Performed" );
                buddyGroup.updateUiData();
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
    Label notifyLabel = new Label( Localization.getMessage( "ENTER_NAME_HERE" ) );
    notifyLabel.setTitle(true);
    pane.addItem( notifyLabel );
    groupNameField = new Field( "" );
    groupNameField.setFocusable( true );
    groupNameField.setFocused( true );
    groupNameField.title = Localization.getMessage( "GROUP_NAME" );
    pane.addItem( groupNameField );
    /** Applying pane **/
    setGObject( pane );
    if ( accountRoot.getStatusIndex() == 0 ) {
      showNotify( Localization.getMessage( "ERROR" ),
              Localization.getMessage( "NO_CONNECTION" ), true );
    }
  }

  public void showNotify( final String title, final String message, final boolean isFail ) {
        Soft notifySoft = new Soft(MidletMain.screen);
        notifySoft.leftSoft = new PopupItem(Localization.getMessage( "CLOSE" ) ) {
          public void actionPerformed() {
            AddingGroupFrame.this.closeDialog();
            if ( isFail ) {
              MidletMain.screen.setActiveWindow( s_prevWindow );
            } else {
              MidletMain.screen.repaint();
            }
          }
        };
        AddingGroupFrame.this.showDialog( new Dialog( MidletMain.screen, notifySoft, title, message ) );
        MidletMain.screen.repaint();
        try {
          Thread.currentThread().sleep( 5000 );
        } catch ( InterruptedException ex ) {
        }
        AddingGroupFrame.this.closeDialog();
        if ( isFail ) {
          MidletMain.screen.setActiveWindow( s_prevWindow );
        } else {
          MidletMain.screen.repaint();
        }
      }
}
