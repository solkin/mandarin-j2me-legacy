package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import javax.microedition.io.ConnectionNotFoundException;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class DonateFrame extends Window {

  private Pane pane;

  public DonateFrame() {
    super( MidletMain.screen );
    /** Header **/
    header = new Header( Localization.getMessage( "DONATE_FRAME" ) );
    /** Soft **/
    soft = new Soft( MidletMain.screen );
    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {

      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "SMS_WALLET" ) ) {

      public void actionPerformed() {
        try {
          MidletMain.midletMain.platformRequest( "http://smskopilka.ru/?info&id=59978" );
        } catch ( ConnectionNotFoundException ex ) {
        }
      }
    };
    /** Pane **/
    pane = new Pane( null, false );

    Label titleLabel = new Label( Localization.getMessage( "DONATE_NOTE" ) );
    pane.addItem( titleLabel );
    addLabelPair( "WHY_DONATE_TITLE", Localization.getMessage( "WHY_DONATE" ) );
    addLabelPair( "SMS_WALLET", Localization.getMessage( "SMS_WALLET_DESCR" ) );
    addLabelPair( "PAY_PAL", "inbox@tomclaw.com" );
    addLabelPair( "WEB_MONEY", "WMR: R948790084724, WMZ: Z111853743320" );
    addLabelPair( "YANDEX_MONEY", "41001664768952" );

    /** Set GObject **/
    setGObject( pane );
  }

  public final void addLabelPair( String title, String caption ) {
    Label label1 = new Label( Localization.getMessage( title ) );
    label1.setTitle(true);
    pane.addItem( label1 );

    Label label2 = new Label( caption );
    pane.addItem( label2 );
  }
}
