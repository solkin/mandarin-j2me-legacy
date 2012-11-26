package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class TrafficInfoFrame extends Window {

  public Pane pane;
  public Label dataCount;
  public Label moneyCount;

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
        // MidletMain.settingsFrame.settingsTab.tabEvent.stateChanged(0, 5, 0);
      }
    } );
    soft.rightSoft.addSubItem( new PopupItem( Localization.getMessage( "RESET" ) ) {

      public void actionPerformed() {
        MidletMain.dataCount = 0;
        updateDataCount( true );
      }
    } );
    /** Pane **/
    pane = new Pane( null, false );

    addLabelPair( "TRAFFIC_COUNTER_STATUS", Localization.getMessage( ( MidletMain.isCountData ? "ENABLED" : "DISABLED" ) ) );
    dataCount = addLabelPair( "TRAFFIC_SIZE", "0 [KiB]" );
    moneyCount = addLabelPair( "MONEY_SIZE", "0 [C.U.]" );

    updateDataCount( false );

    /** Set GObject **/
    setGObject( pane );

    new Thread() {

      public void run() {
        while ( true ) {
          try {
            Thread.currentThread().sleep( 1000 );
          } catch ( InterruptedException ex ) {
          }
          if ( MidletMain.screen.activeWindow.equals( TrafficInfoFrame.this ) ) {
            updateDataCount( true );
          }
        }
      }
    }.start();
  }

  public final void updateDataCount( boolean isRepaint ) {
    String dataString;
    if ( MidletMain.dataCount < 1024 ) {
      dataString = String.valueOf( ( ( int ) MidletMain.dataCount ) ) + " [B]";
    } else if ( MidletMain.dataCount < 1048576 ) {
      dataString = String.valueOf( ( ( int ) MidletMain.dataCount / 1024 ) ) + " [KiB]";
    } else if ( MidletMain.dataCount < 1073741824 ) {
      dataString = String.valueOf( ( ( int ) MidletMain.dataCount / 1048576 ) ) + " [MiB]";
    } else {
      dataString = String.valueOf( ( ( int ) MidletMain.dataCount / 1073741824 ) ) + " [GiB] *CRAZY*";
    }
    dataCount.setCaption( dataString );
    dataCount.updateCaption();
    moneyCount.setCaption( MidletMain.dataCost * MidletMain.dataCount / 1048576 + " [C.U.]" );
    moneyCount.updateCaption();
    if ( isRepaint ) {
      TrafficInfoFrame.this.prepareGraphics();
      MidletMain.screen.repaint();
    }
  }

  public final Label addLabelPair( String title, String caption ) {
    Label label1 = new Label( Localization.getMessage( title ) );
    label1.setTitle( true );
    pane.addItem( label1 );

    Label label2 = new Label( caption );
    pane.addItem( label2 );
    return label2;
  }
}
