package com.tomclaw.mandarin.icq;

import com.tomclaw.mandarin.main.ActionExec;
import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.StringUtil;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class IncomingFileFrame extends Window {

  private Pane pane;

  public IncomingFileFrame( final IcqAccountRoot icqAccountRoot, final int ch2msgType, final String buddyId, final int[] externalIp, final int dcTcpPort, final boolean isViaRendezvousServer,
          final long fileLength, final byte[] fileName, final byte[] cookie ) {
    super( MidletMain.screen );

    header = new Header( Localization.getMessage( "INC_FILE" ) );

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
            ActionExec.performTransferAction( icqAccountRoot, ch2msgType,
                    buddyId, externalIp, dcTcpPort, isViaRendezvousServer,
                    fileLength, fileName, cookie, true );
          }
        }.start();
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };

    pane = new Pane( null, false );

    Label label = new Label( Localization.getMessage( "INC_FILE_FROM" )
            .concat( " " ).concat( buddyId ) );
    label.setHeader( true );
    pane.addItem( label );

    addLabels( "BUDDYID_LABEL", buddyId );
    addLabels( "FILENAME_LABEL", StringUtil.byteArrayToString( fileName ) );
    /** Optimizing file size output **/
    long humanFileLength = fileLength;
    String fileSizeMetrix = "BYTES";
    /** KibiBytes **/
    if ( humanFileLength > 1024 ) {
      humanFileLength /= 1024;
      fileSizeMetrix = "KIB";
    }
    /** MibiBytes **/
    if ( humanFileLength > 1024 ) {
      humanFileLength /= 1024;
      fileSizeMetrix = "MIB";
    }
    addLabels( "FILESIZE_LABEL", String.valueOf( humanFileLength )
            + " " + Localization.getMessage( fileSizeMetrix ) );

    setGObject( pane );
  }

  private void addLabels( String title, String descr ) {
    pane.addItem( new Label( new RichContent( "[p][b]" + Localization
            .getMessage( title ) + ": [/b]" + descr + "[/p]" ) ) );
  }
}
