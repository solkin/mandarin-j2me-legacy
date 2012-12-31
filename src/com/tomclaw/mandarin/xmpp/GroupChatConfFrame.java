package com.tomclaw.mandarin.xmpp;

import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import java.io.IOException;
import java.util.Vector;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class GroupChatConfFrame extends Window {

  public Pane pane;
  public XmppAccountRoot xmppAccountRoot;
  public XmppItem buddyItem;
  public String requestId;
  public Label notifyLabel;
  public String FORM_TYPE;

  public GroupChatConfFrame( final XmppAccountRoot xmppAccountRoot, final XmppItem buddyItem ) {
    super( MidletMain.screen );
    this.xmppAccountRoot = xmppAccountRoot;
    this.buddyItem = buddyItem;
    /** Header **/
    header = new Header( Localization.getMessage( "GROUPCHAT_CONFIGURE_FRAME" ) );
    /** Creating soft **/
    soft = new Soft( MidletMain.screen );
    /** Left soft items **/
    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {

      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "SAVE" ) ) {

      public void actionPerformed() {
        MidletMain.screen.setWaitScreenState( true );
        requestId = "grconffrm_set_".concat( xmppAccountRoot.xmppSession.getId() );
        try {
          XmppSender.sendGroupChatSettings( xmppAccountRoot.xmppSession, 
                  buddyItem.userId, pane.items, FORM_TYPE, requestId );
        } catch ( IOException ex ) {
          notifyLabel.setCaption( Localization.getMessage( "SETTINGS_READING_FAILED" ) );
          pane.items.removeAllElements();
          pane.yOffset = 0;
          pane.addItem( notifyLabel );
          MidletMain.screen.setWaitScreenState( false );
        }
      }
    };
    /** Pane **/
    pane = new Pane( null, false );
    /** Pane objects **/
    notifyLabel = new Label( Localization.getMessage( "READING_SETTINGS" ) );
    pane.addItem( notifyLabel );
    /** Setting GObject **/
    setGObject( pane );
    /** Requesting settings **/
    requestId = "grconffrm_get_".concat( xmppAccountRoot.xmppSession.getId() );
    try {
      XmppSender.requestGroupChatSettings( xmppAccountRoot.xmppSession, 
              buddyItem.userId, requestId );
    } catch ( IOException ex ) {
      notifyLabel.setCaption( Localization.getMessage( "SETTINGS_READING_FAILED" ) );
      MidletMain.screen.repaint();
    }
  }

  public void setObjects( XmppAccountRoot xmppAccountRoot, String id, Vector objects, String FORM_TYPE ) {
    if ( this.xmppAccountRoot.equals( xmppAccountRoot ) && requestId.equals( id ) ) {
      pane.items = objects;
      this.FORM_TYPE = FORM_TYPE;
      prepareGraphics();
      MidletMain.screen.repaint();
    }
  }

  public void setResult( XmppAccountRoot xmppAccountRoot, String id ) {
    if ( this.xmppAccountRoot.equals( xmppAccountRoot ) && requestId.equals( id ) ) {
      MidletMain.screen.setWaitScreenState( false );
    }
  }
}
