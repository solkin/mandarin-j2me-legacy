package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.DirectDraw;
import com.tomclaw.tcuilite.Gauge;
import com.tomclaw.tcuilite.Pane;
import com.tomclaw.tcuilite.Window;
import java.io.IOException;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class SplashFrame extends Window {

  Pane pane;
  Gauge gauge;
  Image logo;
  Font fontPlain = Font.getFont( Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL );
  Font fontBold = Font.getFont( Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL );
  String title, build;

  public SplashFrame() {
    super( MidletMain.screen );
    /** Pane **/
    pane = new Pane( null, false );

    title = "Mandarin " + MidletMain.version;
    build = MidletMain.type + "-build " + MidletMain.build;
    gauge = new Gauge( "Loading..." );
    pane.addItem( gauge );

    try {
      logo = Image.createImage( "/res/huge/tangerine.png" );
      
      directDraw_afterAll = new DirectDraw() {

        int width, height;

        public void paint( Graphics grphcs ) {
          paint( grphcs, 0, 0 );
        }

        public void paint( Graphics grphcs, int i, int i1 ) {
          width = MidletMain.screen.getWidth();
          height = MidletMain.screen.getHeight();
          grphcs.drawImage( logo, width / 2, height / 2, 
                  Graphics.VCENTER | Graphics.HCENTER );
          grphcs.setFont( fontBold );
          grphcs.drawString( title, width / 2 
                  - fontBold.stringWidth( title ) / 2, height 
                  - ( fontBold.getHeight() + fontPlain.getHeight() ) * 3 / 2, 
                  Graphics.TOP | Graphics.LEFT );
          grphcs.setFont( fontPlain );
          grphcs.drawString( build, width / 2 
                  - fontPlain.stringWidth( build ) / 2, height 
                  - fontPlain.getHeight() * 3 / 2, 
                  Graphics.TOP | Graphics.LEFT );
        }
      };
    } catch ( IOException ex ) {
    }

    /** Set GObject **/
    setGObject( pane );
  }

  public void updateGaugeValue( int value ) {
    gauge.setValue( value );
    MidletMain.screen.repaint();
  }
}
