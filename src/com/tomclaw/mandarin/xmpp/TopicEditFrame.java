package com.tomclaw.mandarin.xmpp;

import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class TopicEditFrame extends Window {

  public XmppAccountRoot xmppAccountRoot;
  public Field topicField;

  public TopicEditFrame( final XmppAccountRoot xmppAccountRoot, final String groupChatJid, String topicText ) {
    super( MidletMain.screen );
    this.xmppAccountRoot = xmppAccountRoot;
    /** Header **/
    header = new Header( Localization.getMessage( "TOPICEDIT_FRAME" ) );
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
        XmppSender.sendMessage( xmppAccountRoot.xmppSession, groupChatJid, topicField.getText(), "groupchat", true );
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    /** Pane **/
    Pane pane = new Pane( null, false );
    pane.addItem( new Label( Localization.getMessage( "TOPIC_NOTIFY" ).concat( ":" ) ) );
    topicField = new Field( topicText );
    topicField.setFocusable( true );
    topicField.setFocused( true );
    pane.addItem( topicField );
    /** Set GObject **/
    setGObject( pane );
  }
}
