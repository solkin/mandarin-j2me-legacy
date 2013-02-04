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
  private Label ___proxyLabel;
  private Label ___speedLabel;
  private Gauge gauge;
  private String proxyString;

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

    addLabels( "BUDDYID_LABEL", directConnection.getBuddyId() );
    addLabels( "FILENAME_LABEL", StringUtil.byteArrayToString( directConnection.getFileName(), true ) );
    addLabels( "FILESIZE_LABEL", String.valueOf( directConnection.getFileByteSize() ) );
    ___proxyLabel = addLabels( "PROXY_LABEL", Localization.getMessage( "NO_PROXY" ) );
    ___speedLabel = addLabels( "SPEED_LABEL", Localization.getMessage( "CALCULATING" ) );
    gauge = new Gauge( Localization.getMessage( "STATUS_LABEL" ) );
    pane.addItem( gauge );

    updateData();

    setGObject( pane );
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

  public final void updateData() {
    if ( directConnection.getProxyIp() != null
            && directConnection.getProxyPort() != -1 ) {
      proxyString = directConnection.getProxyIp() + ":" + directConnection.getProxyPort();
    } else {
      proxyString = Localization.getMessage( "NO_PROXY" );
    }
    updateLabels( ___proxyLabel, "PROXY_LABEL", proxyString );
    updateLabels( ___speedLabel, "SPEED_LABEL", directConnection.getSpeed() + " " + Localization.getMessage( "KBIT_PER_SEC" ) );
    gauge.caption = ( Localization.getMessage( directConnection.getStatusString() ) );
    gauge.setValue( directConnection.getPercentValue() );
    MidletMain.screen.repaint();
  }
}
