package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
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
    /** Pane **/
    pane = new Pane( null, false );

    Label titleLabel = new Label( Localization.getMessage( "DONATE_NOTE" ) );
    titleLabel.setHeader( true );
    pane.addItem( titleLabel );
    pane.addItem( new Label( new RichContent( "[p][b]" + Localization
            .getMessage( "WHY_DONATE_TITLE" ) + " [/b]"
            + Localization.getMessage( "WHY_DONATE" ) + "[/p]" ) ) );
    addLabels( "PAY_PAL", "inbox@tomclaw.com" );
    addLabels( "WEB_MONEY", "WMR: R948790084724, WMZ: Z111853743320" );
    addLabels( "YANDEX_MONEY", "41001664768952" );

    /** Set GObject **/
    setGObject( pane );
  }

  private void addLabels( String title, String descr ) {
    pane.addItem( new Label( new RichContent( "[p][b]" + Localization
            .getMessage( title ) + ": [/b]" + descr + "[/p]" ) ) );
  }
}
