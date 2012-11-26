package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.StringUtil;
import java.io.IOException;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
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
          showNotify( Localization.getMessage( "ERROR" ),
                  Localization.getMessage( "EMPTY_FIELD" ), false );
        } else {
          try {
            accountRoot.requestAuth( requestTextField.getText(), buddyItem );
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
    Label notifyLabel = new Label( Localization.getMessage( "ENTER_AUTHREQ_HERE" ) );
    notifyLabel.setTitle( true );
    pane.addItem( notifyLabel );
    requestTextField = new Field( Localization.getMessage( "DEFAULT_REQUEST_TEXT" ) );
    requestTextField.setFocusable( true );
    requestTextField.setFocused( true );
    requestTextField.title = Localization.getMessage( "REQUEST_TEXT" );
    pane.addItem( requestTextField );
    /** Applying pane **/
    setGObject( pane );
    if ( accountRoot.getStatusIndex() == 0 ) {
      showNotify( Localization.getMessage( "ERROR" ),
              Localization.getMessage( "NO_CONNECTION" ), true );
    }
  }

  public final void showNotify( final String title, final String message, final boolean isFail ) {
    Soft notifySoft = new Soft( MidletMain.screen );
    notifySoft.leftSoft = new PopupItem( Localization.getMessage( "CLOSE" ) ) {
      public void actionPerformed() {
        AuthRequestFrame.this.closeDialog();
        if ( isFail ) {
          MidletMain.screen.setActiveWindow( s_prevWindow );
        } else {
          MidletMain.screen.repaint();
        }
      }
    };
    AuthRequestFrame.this.showDialog( new Dialog( MidletMain.screen, notifySoft, title, message ) );
    MidletMain.screen.repaint();
    try {
      Thread.currentThread().sleep( 5000 );
    } catch ( InterruptedException ex ) {
    }
    AuthRequestFrame.this.closeDialog();
    if ( isFail ) {
      MidletMain.screen.setActiveWindow( s_prevWindow );
    } else {
      MidletMain.screen.repaint();
    }
  }
}
