package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class InfoFrame extends Window {

  private Pane pane;

  public InfoFrame( String[] param, String[] value ) {
    super( MidletMain.screen );
    /** Header **/
    header = new Header( Localization.getMessage( "INFO_FRAME" ) );
    /** Creating soft **/
    soft = new Soft( MidletMain.screen );
    /** Left soft items **/
    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {

      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    /** Pane **/
    pane = new Pane( null, false );
    for ( int c = 0; c < param.length; c++ ) {
      addLabels( param[c], value[c] );
    }
    setGObject( pane );
  }

  private void addLabels( String title, String descr ) {
    pane.addItem( new Label( new RichContent( "[p][b]" + Localization.getMessage( title ) + ": [/b]" + descr + "[/p]" ) ) );
  }
}
