package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import java.io.IOException;
import javax.microedition.lcdui.Image;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class AboutFrame extends Window {

  public AboutFrame() {
    super( MidletMain.screen );
    /** Header **/
    header = new Header( Localization.getMessage( "ABOUT_FRAME" ) );
    /** Soft **/
    soft = new Soft( MidletMain.screen );
    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "DONATE" ) ) {
      public void actionPerformed() {
        DonateFrame donateFrame = new DonateFrame();
        donateFrame.s_prevWindow = AboutFrame.this;
        MidletMain.screen.setActiveWindow( donateFrame );
      }
    };
    /** Pane **/
    Pane pane = new Pane( null, false );

    Label titleLabel = new Label( "Mandarin " + MidletMain.version + " "
            + MidletMain.type + "-build " + MidletMain.build );
    try {
      ( ( PlainContent ) titleLabel.getContent() ).image = Image.createImage( "/res/huge/logo.png" );
    } catch ( IOException ex ) {
    }
    pane.addItem( titleLabel );
    pane.addItem( new Label( Localization.getMessage( "ABOUT_AUTHOR" ) ) );
    pane.addItem( new Label( Localization.getMessage( "ABOUT_MANDARIN" ) ) );
    pane.addItem( new Label( Localization.getMessage( "ABOUT_OWNERS" ) ) );
    pane.addItem( new Label( Localization.getMessage( "ABOUT_LICENSE" ) ) );
    pane.addItem( new Label( Localization.getMessage( "ABOUT_TESTERS" ) ) );

    /** Set GObject **/
    setGObject( pane );
  }
}
