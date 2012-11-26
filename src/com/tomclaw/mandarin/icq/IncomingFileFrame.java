package com.tomclaw.mandarin.icq;

import com.tomclaw.mandarin.main.ActionExec;
import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.StringUtil;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class IncomingFileFrame extends Window {

  private Pane pane;

  public IncomingFileFrame( final IcqAccountRoot icqAccountRoot, final int ch2msgType, final String buddyId, final int[] externalIp, final int dcTcpPort, final boolean isViaRendezvousServer,
          final long fileLength, final byte[] fileName, final byte[] cookie ) {
    super( MidletMain.screen );

    header = new Header( Localization.getMessage( "INC_FILE_FROM" ).concat( " " ).concat( buddyId ) );

    soft = new Soft( MidletMain.screen );

    soft.leftSoft = new PopupItem( Localization.getMessage( "CANCEL" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "ACCEPT" ) ) {
      public void actionPerformed() {
        /** Accepting file **/
        new Thread() {
          public void run() {
            ActionExec.performTransferAction( icqAccountRoot, ch2msgType, buddyId, externalIp, dcTcpPort, isViaRendezvousServer,
                    fileLength, fileName, cookie, true );
          }
        }.start();
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };

    pane = new Pane( null, false );

    addLabels( "BUDDYID_LABEL", buddyId );
    addLabels( "FILENAME_LABEL", StringUtil.byteArrayToString( fileName, true ) );
    addLabels( "FILESIZE_LABEL", String.valueOf( fileLength ) );

    setGObject( pane );
  }

  private Label addLabels( String title, String descr ) {
    Label label1 = new Label( Localization.getMessage( title ) );
    label1.setFocusable( false );
    label1.setTitle( true );
    pane.addItem( label1 );
    Label label2 = new Label( descr );
    label2.setFocusable( false );
    pane.addItem( label2 );
    return label2;
  }
}
