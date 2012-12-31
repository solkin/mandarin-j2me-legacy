package com.tomclaw.mandarin.xmpp;

import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import java.io.IOException;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class GroupChatNickEditFrame extends Window {

  public XmppAccountRoot xmppAccountRoot;
  public XmppItem xmppItem;
  public Field nickField;

  public GroupChatNickEditFrame( final XmppAccountRoot xmppAccountRoot, final XmppItem xmppItem ) {
    super( MidletMain.screen );
    /** Accepting variables **/
    this.xmppAccountRoot = xmppAccountRoot;
    this.xmppItem = xmppItem;
    /** Header **/
    header = new Header( Localization.getMessage( "GROUP_CHAT_NICK_EDIT" ) );
    /** Soft **/
    soft = new Soft( MidletMain.screen );
    /** Left soft items **/
    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {

      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    /** Right soft items **/
    soft.rightSoft = new PopupItem( Localization.getMessage( "APPLY" ) ) {

      public void actionPerformed() {
        try {
          XmppSender.sendPresence( xmppAccountRoot.xmppSession.xmlWriter, null, 
                  xmppItem.userId.concat( "/" ).concat( nickField.getText() ), 
                  null, XmppStatusUtil.getStatusDescr( 
                  (int)xmppAccountRoot.statusId ), null, 5, false, null, null );
          xmppItem.groupChatNick = nickField.getText();
        } catch ( IOException ex ) {
        }
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    /** Pane **/
    Pane pane = new Pane( null, false );
    pane.addItem( new Label( Localization.getMessage( "NICK_NAME" ) ) );
    nickField = new Field( xmppItem.groupChatNick );
    nickField.setFocusable( true );
    nickField.setFocused( true );
    pane.addItem( nickField );
    /** Setting GObject **/
    setGObject( pane );
  }
}
