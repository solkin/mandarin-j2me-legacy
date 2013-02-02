package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.DirectDraw;
import com.tomclaw.tcuilite.Gauge;
import com.tomclaw.tcuilite.Label;
import com.tomclaw.tcuilite.Pane;
import com.tomclaw.tcuilite.RichContent;
import com.tomclaw.tcuilite.Theme;
import com.tomclaw.tcuilite.Window;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.DrawUtil;
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

  private Pane pane;
  private Gauge gauge;
  private Image logo;
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
    gauge.setFocusable( true );
    gauge.setFocused( true );
    gauge.setSize( screen.getWidth() - 1,
            Theme.font.getHeight() + 2 * 2 + 4 );
    gauge.setLocation( 0, screen.getHeight() - gauge.getHeight() - 1 );

    try {
      logo = Image.createImage( "/res/huge/tangerine.png" );

      directDraw_afterAll = new DirectDraw() {
        int width, height, l_height = 4 + fontBold.getHeight() + 4 + fontPlain.getHeight() + 4;

        public void paint( Graphics g ) {
          paint( g, 0, 0 );
        }

        public void paint( Graphics g, int i, int i1 ) {
          width = MidletMain.screen.getWidth();
          height = MidletMain.screen.getHeight();

          DrawUtil.fillVerticalGradient( g, 0, 0, width, l_height, Label.headerGradFrom,
                  Label.headerGradTo );
          g.setColor( Label.headerHr );
          g.drawLine( 0, l_height - 1, width, l_height - 1 );

          g.drawImage( logo, width / 2, height / 2,
                  Graphics.VCENTER | Graphics.HCENTER );
          g.setFont( fontBold );
          g.setColor( Label.foreColor );
          g.drawString( title,
                  width / 2 - fontBold.stringWidth( title ) / 2,
                  4, Graphics.TOP | Graphics.LEFT );
          g.setFont( fontPlain );
          g.drawString( build, 
                  width / 2 - fontPlain.stringWidth( build ) / 2, 
                  4 + fontBold.getHeight() + 4, Graphics.TOP | Graphics.LEFT );
          gauge.repaint( g );
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
