package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.tcuilite.smiles.AnimSmile;
import com.tomclaw.tcuilite.smiles.SmileLink;
import com.tomclaw.tcuilite.smiles.Smiles;
import com.tomclaw.utils.StringUtil;
import javax.microedition.lcdui.Display;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class SmilesFrame extends Window {

  public Grid grid;

  public SmilesFrame() {
    super( MidletMain.screen );
    /** Header **/
    header = new Header( Localization.getMessage( "SMILES_FRAME" ) );
    /** Soft **/
    soft = new Soft( MidletMain.screen );

    soft.leftSoft = new PopupItem( Localization.getMessage( "SELECT" ) ) {

      public void actionPerformed() {
        grid.actionObject = ( PaneObject ) grid.items.elementAt( grid.focusedColumn + grid.columns * grid.focusedRow );
        if ( grid.actionObject != null ) {
          grid.actionPerformedEvent.actionPerformed( grid.actionObject );
        }
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {

      public void actionPerformed() {
        Display.getDisplay( MidletMain.midletMain ).setCurrent( MidletMain.chatFrame.textBox );
        MidletMain.chatFrame.textBox.getCaretPosition();
      }
    };

    /** Grid **/
    grid = new Grid( this, true );
    grid.focusedColumn = 0;
    grid.focusedRow = 0;
    grid.actionPerformedEvent = new PaneEvent() {

      public void actionPerformed( PaneObject po ) {
        if ( po != null ) {
          String smileDefinition = Smiles.smiles[( ( Smile ) po ).smileLink.smileIndex].getSmileDefinitions()[0];
          smileDefinition = StringUtil.replace( smileDefinition, "\\[", "[" );
          smileDefinition = StringUtil.replace( smileDefinition, "\\]", "]" );
          MidletMain.chatFrame.textBox.setString(
                  MidletMain.chatFrame.textBox.getString() + " "
                  + smileDefinition );
          Display.getDisplay( MidletMain.midletMain ).setCurrent( MidletMain.chatFrame.textBox );
          MidletMain.chatFrame.textBox.getCaretPosition();
        }
      }
    };

    SmileLink smileLink;
    for ( int c = 0; c < Smiles.smiles.length; c++ ) {
      smileLink = new SmileLink( c );
      if ( Smiles.smiles[smileLink.smileIndex] instanceof AnimSmile ) {
        Smiles.loadSmileARGB( ( AnimSmile ) Smiles.smiles[smileLink.smileIndex] );
      }
      Smile smile = new Smile( smileLink );
      smile.setFocusable( true );
      smile.setFocused( c == 0 );
      grid.addItem( smile );
    }

    grid.itemWidth = Smiles.averageWidth;
    grid.itemHeight = Smiles.averageHeight;

    /** Set GObject **/
    setGObject( grid );
  }
}
