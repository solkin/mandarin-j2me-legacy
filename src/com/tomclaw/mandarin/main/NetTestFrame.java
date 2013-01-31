package com.tomclaw.mandarin.main;

import com.tomclaw.mandarin.net.NetConnection;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class NetTestFrame extends Window {

  private Pane pane;
  private Label infoLabel;

  public NetTestFrame() {
    super( MidletMain.screen );
    /** Header **/
    header = new Header( Localization.getMessage( "NETWORK_TEST_FRAME" ) );
    /** Soft **/
    soft = new Soft( MidletMain.screen );
    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    /** GObject **/
    pane = new Pane( null, false );

    Label sendLabel = new Label( Localization.getMessage( "NETWORK_TEST_STATUS" ) );
    sendLabel.setTitle( true );
    pane.addItem( sendLabel );

    infoLabel = new Label( Localization.getMessage( "NET_TEST_LABEL" ) );
    pane.addItem( infoLabel );

    setGObject( pane );

    startCheck();
  }

  private void startCheck() {
    new Thread() {
      public void run() {
        String retreivedData;
        String linkTest = "http://www.tomclaw.com/services/"
                + "mandarin/scripts/nettest.php";
        try {
          /** Sending data **/
          retreivedData = NetConnection.retreiveData( linkTest );
          retreivedData = Localization.getMessage( "TEST_OK_YOUR_IP" )
                  .concat( " " ).concat( retreivedData );
        } catch ( Throwable ex1 ) {
          retreivedData = Localization.getMessage( "TEST_FAILED" );
        }
        infoLabel.setCaption( retreivedData );
        MidletMain.screen.repaint();
      }
    }.start();
  }
}
