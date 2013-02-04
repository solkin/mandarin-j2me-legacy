package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.tcuilite.smiles.Smiles;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class TrafficInfoFrame extends Window {

  private Pane pane;
  private Label dataCount;
  private Label moneyCount;

  public TrafficInfoFrame() {
    super( MidletMain.screen );
    /** Header **/
    header = new Header( Localization.getMessage( "TRAFFIC_INFO_FRAME" ) );
    /** Soft **/
    soft = new Soft( MidletMain.screen );
    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "MENU" ) );
    soft.rightSoft.addSubItem( new PopupItem( Localization.getMessage( "SETUP" ) ) {
      public void actionPerformed() {
        MidletMain.settingsFrame.settingsTab.selectedIndex = 4;
        MidletMain.settingsFrame.settingsTab.setGObject( MidletMain.settingsFrame.panes[MidletMain.settingsFrame.settingsTab.selectedIndex] );
        MidletMain.settingsFrame.settingsTab.xOffset =
                ( ( TabItem ) MidletMain.settingsFrame.settingsTab.items.elementAt( MidletMain.settingsFrame.settingsTab.selectedIndex ) ).x;
        MidletMain.screen.setActiveWindow( MidletMain.settingsFrame );
      }
    } );
    soft.rightSoft.addSubItem( new PopupItem( Localization.getMessage( "RESET" ) ) {
      public void actionPerformed() {
        MidletMain.dataCount = 0;
        updateDataCount( true );
      }
    } );
    soft.rightSoft.addSubItem( new PopupItem( Localization.getMessage( "UPDATE" ) ) {
      public void actionPerformed() {
        updateDataCount( true );
      }
    } );
    /** Pane **/
    pane = new Pane( this, true );

    addLabels( "TRAFFIC_COUNTER_STATUS", Localization.getMessage( ( MidletMain.isCountData ? "ENABLED" : "DISABLED" ) ) );
    dataCount = addLabels( "TRAFFIC_SIZE", "0 [KiB]" );
    moneyCount = addLabels( "MONEY_SIZE", "0 [C.U.]" );

    updateDataCount( false );

    /** Set GObject **/
    setGObject( pane );
  }

  public final void updateDataCount( boolean isRepaint ) {
    String dataString;
    if ( MidletMain.dataCount < 1024 ) {
      dataString = String.valueOf( ( ( int ) MidletMain.dataCount ) ) + " B :(";
    } else if ( MidletMain.dataCount < 1048576 ) {
      dataString = String.valueOf( ( ( int ) MidletMain.dataCount / 1024 ) ) + " KiB :)";
    } else if ( MidletMain.dataCount < 1073741824 ) {
      dataString = String.valueOf( ( ( int ) MidletMain.dataCount / 1048576 ) ) + " MiB %)";
    } else {
      dataString = String.valueOf( ( ( int ) MidletMain.dataCount / 1073741824 ) ) + " GiB *CRAZY*";
    }
    dataString = Smiles.replaceSmilesForCodes( dataString );
    updateLabels( dataCount, "TRAFFIC_SIZE", dataString );
    updateLabels( moneyCount, "MONEY_SIZE", MidletMain.dataCost
            * MidletMain.dataCount / 1048576 + " C.U." );
    moneyCount.updateCaption();
    if ( isRepaint ) {
      TrafficInfoFrame.this.prepareGraphics();
      MidletMain.screen.repaint();
    }
  }

  private Label addLabels( String title, String descr ) {
    Label label = updateLabels( new Label( new RichContent( "" ) ), title, descr );
    pane.addItem( label );
    return label;
  }

  private Label updateLabels( Label label, String title, String descr ) {
    label.getContent().setText( "[p][b]" + Localization
            .getMessage( title ) + ": [/b]" + descr + "[/p]" );
    label.updateCaption();
    return label;
  }
}
