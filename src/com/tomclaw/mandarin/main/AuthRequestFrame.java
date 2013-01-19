package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.StringUtil;
import java.io.IOException;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class AuthRequestFrame extends Window {

  public Field requestTextField;

  public AuthRequestFrame( final AccountRoot accountRoot, final BuddyItem buddyItem ) {
    super( MidletMain.screen );
    /** Header **/
    header = new Header( Localization.getMessage( "AUTH_REQUEST" ) );
    /** Creating soft **/
    soft = new Soft( MidletMain.screen );
    /** Left soft items **/
    soft.leftSoft = new PopupItem( Localization.getMessage( "CANCEL" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "REQUEST" ) ) {
      public void actionPerformed() {
        if ( !StringUtil.isFill( requestTextField.getText() ) ) {
          ActionExec.showNotify( Localization.getMessage( "EMPTY_FIELD" ) );
        } else {
          try {
            accountRoot.requestAuth( requestTextField.getText(), buddyItem );
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
    pane.addItem( new Label( Localization.getMessage( "ENTER_AUTHREQ_HERE" ) ) );
    requestTextField = new Field( Localization.getMessage( "DEFAULT_REQUEST_TEXT" ) );
    requestTextField.setFocusable( true );
    requestTextField.setFocused( true );
    requestTextField.title = Localization.getMessage( "REQUEST_TEXT" );
    pane.addItem( requestTextField );
    pane.addItem( new Label( Localization.getMessage( "AUTH_TEXT_COMMENT" ) ) );
    /** Applying pane **/
    setGObject( pane );
  }
}
