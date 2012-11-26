package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class InfoFrame extends Window {

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
    Pane pane = new Pane( null, false );
    for ( int c = 0; c < param.length; c++ ) {
      Label paramLabel = new Label( Localization.getMessage( param[c] ).concat( ":" ) );
      paramLabel.setTitle(true);
      pane.addItem( paramLabel );
      Label valueLabel = new Label( value[c] );
      valueLabel.setTitle(false);
      pane.addItem( valueLabel );
    }
    setGObject( pane );
  }
}
