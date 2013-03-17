package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class ServiceMessagesFrame extends Window {

  public Pane pane;
  public ServiceMessages serviceMessages;

  public ServiceMessagesFrame() {
    super( MidletMain.screen );

    header = new Header( Localization.getMessage( "SERVICE_MESSAGES" ) );

    soft = new Soft( MidletMain.screen );

    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "MENU" ) );

    soft.rightSoft.addSubItem( new PopupItem( Localization.getMessage( "APPEND" ) ) {
      public void actionPerformed() {
        ChatItem chatItem = ( ( ChatItem ) pane.getFocusedPaneObject() );
        if ( chatItem == null ) {
          return;
        }
        MidletMain.buffer += "\n[".concat( chatItem.buddyNick ).concat( "]\n " ).concat( chatItem.itemDateTime ).concat( " \n" ).concat( chatItem.text );
      }
    } );
    soft.rightSoft.addSubItem( new PopupItem( Localization.getMessage( "COPY" ) ) {
      public void actionPerformed() {
        ChatItem chatItem = ( ( ChatItem ) pane.getFocusedPaneObject() );
        if ( chatItem == null ) {
          return;
        }
        MidletMain.buffer = "[".concat( chatItem.buddyNick ).concat( "]\n " ).concat( chatItem.itemDateTime ).concat( " \n" ).concat( chatItem.text );

      }
    } );
    soft.rightSoft.addSubItem( new PopupItem( Localization.getMessage( "CLEAR" ) ) {
      public void actionPerformed() {
        serviceMessages.clearMessages();
      }
    } );
    pane = new Pane( null, false );
    setGObject( pane );
  }

  public void setItems( ServiceMessages serviceMessages ) {
    this.serviceMessages = serviceMessages;
    updateItemsLocation( false );
  }

  public void updateItemsLocation( boolean isPrepareGraphics ) {
    pane.items = serviceMessages.messages;
    pane.yOffset = 0;
    if ( isPrepareGraphics ) {
      ServiceMessagesFrame.this.prepareGraphics();
    }
    if ( !pane.items.isEmpty() ) {
      pane.setFocused( pane.items.size() - 1 );
      if ( pane.getTotalHeight() > pane.getHeight() ) {
        pane.yOffset = pane.getTotalHeight() - pane.getHeight();
      }
    }
    if ( isPrepareGraphics ) {
      ServiceMessagesFrame.this.prepareGraphics();
    }
    MidletMain.screen.repaint();
  }
}
