package com.tomclaw.mandarin.main;

import com.tomclaw.mandarin.dc.DirectConnection;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;
import java.io.IOException;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class TransactionItemFrame extends Window {

  public final DirectConnection directConnection;
  private Pane pane;
  public Label _buddyIdLabel;
  public Label fileNameLabel;
  public Label fileSizeLabel;
  public Label ___proxyLabel;
  public Label ___speedLabel;
  public Gauge gauge;

  public TransactionItemFrame( final DirectConnection directConnection ) {
    super( MidletMain.screen );

    header = new Header( Localization.getMessage( "TRANSACTION_WITH" ).concat( " " ).concat( directConnection.getBuddyId() ) );

    this.directConnection = directConnection;

    soft = new Soft( MidletMain.screen );

    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };

    soft.rightSoft = new PopupItem( Localization.getMessage( "STOP" ) ) {
      public void actionPerformed() {
        try {
          directConnection.sendStop();
        } catch ( IOException ex ) {
          LogUtil.outMessage( "IOException: ".concat( ex.getMessage() ) );
        }
      }
    };

    pane = new Pane( null, false );

    _buddyIdLabel = addLabels( "BUDDYID_LABEL", directConnection.getBuddyId() );
    fileNameLabel = addLabels( "FILENAME_LABEL", StringUtil.byteArrayToString( directConnection.getFileName(), true ) );
    fileSizeLabel = addLabels( "FILESIZE_LABEL", String.valueOf( directConnection.getFileByteSize() ) );
    ___proxyLabel = addLabels( "PROXY_LABEL", directConnection.getProxyIp() + ":" + directConnection.getProxyPort() );
    ___speedLabel = addLabels( "SPEED_LABEL", Localization.getMessage( "CALCULATING" ) );
    gauge = new Gauge( Localization.getMessage( "STATUS_LABEL" ) );
    pane.addItem( gauge );

    updateData();

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

  public final void updateData() {
    if ( directConnection.getProxyIp() != null ) {
      ___proxyLabel.setCaption( directConnection.getProxyIp() + ":" + directConnection.getProxyPort() );
      ___proxyLabel.updateCaption();
    }
    gauge.caption = ( Localization.getMessage( directConnection.getStatusString() ) );
    gauge.setValue( directConnection.getPercentValue() );
    ___speedLabel.setCaption( directConnection.getSpeed() + " " + Localization.getMessage( "KBIT_PER_SEC" ) );
    ___speedLabel.updateCaption();
    MidletMain.screen.repaint();
  }
}
