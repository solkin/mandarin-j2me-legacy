package com.tomclaw.mandarin.icq;

import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.DataUtil;
import com.tomclaw.utils.LogUtil;
import java.io.IOException;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class StatusReaderFrame extends Window {

  public Label statusText;
  public Label xStatusDescr;
  public Pane pane;
  public byte[] plainCookie = new byte[ 8 ];
  public byte[] xStatCookie = new byte[ 8 ];
  private Capability capability;

  public StatusReaderFrame( final IcqAccountRoot icqAccountRoot, final IcqItem icqItem ) {
    super( MidletMain.screen );
    /** Header **/
    header = new Header( Localization.getMessage( "STATUS_OF" ).concat( " " ).concat( icqItem.getUserNick() ) );
    /** Creating soft **/
    soft = new Soft( MidletMain.screen );
    /** Left soft items **/
    soft.leftSoft = new PopupItem( Localization.getMessage( "CLOSE" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    /** Right soft item **/
    soft.rightSoft = new PopupItem( Localization.getMessage( "COPY" ) ) {
      public void actionPerformed() {
        MidletMain.buffer = Localization.getMessage( "STATUS_OF" ).concat( " " ).concat( icqItem.getUserNick() ) + "\n"
                + Localization.getMessage( "PLAIN_STATUS" ) + "\n"
                + Localization.getMessage( IcqStatusUtil.getStatusDescr( IcqStatusUtil.getStatusIndex( icqItem.buddyStatus ) ) );
        if ( icqAccountRoot.statusId != -1 ) {
          MidletMain.buffer += "\n" + statusText.caption;
        }
        if ( capability != null ) {
          MidletMain.buffer += "\n" + Localization.getMessage( "X_STATUS" ) + "\n" + Localization.getMessage( capability.capIcon );
          if ( icqAccountRoot.statusId != -1 ) {
            MidletMain.buffer += "\n" + xStatusDescr.caption;
          }
        }
      }
    };
    /** Initializing pane **/
    pane = new Pane( null, false );
    Label statusHeader = new Label( Localization.getMessage( "PLAIN_STATUS" ) );
    statusHeader.setTitle( true );
    pane.addItem( statusHeader );
    Label statusLabel = new Label( Localization.getMessage( IcqStatusUtil.getStatusDescr( IcqStatusUtil.getStatusIndex( icqItem.buddyStatus ) ) ) );
    //! Setting status icon
    pane.addItem( statusLabel );
    if ( icqAccountRoot.statusId != -1 ) {
      statusText = new Label( Localization.getMessage( "REQUESTING" ) );
      pane.addItem( statusText );
    }
    capability = CapUtil.getCapabilityByType( icqItem.capabilities, Capability.CAP_XSTATUS );
    if ( capability != null ) {
      Label xStatusHeader = new Label( Localization.getMessage( "X_STATUS" ) );
      xStatusHeader.setTitle( true );
      pane.addItem( xStatusHeader );
      Label xStatusLabel = new Label( Localization.getMessage( capability.capIcon ) );
      pane.addItem( xStatusLabel );
      if ( icqAccountRoot.statusId != -1 ) {
        xStatusDescr = new Label( Localization.getMessage( "REQUESTING" ) );
        pane.addItem( xStatusDescr );
      }
    }
    /** Applying pane **/
    setGObject( pane );
    if ( icqAccountRoot.statusId != -1 ) {
      startStatusReading( icqAccountRoot, icqItem );
    }
  }

  public final void startStatusReading( final IcqAccountRoot icqAccountRoot, final IcqItem icqItem ) {
    new Thread() {
      public void run() {
        try {
          DataUtil.nextBytes( plainCookie );
          IcqPacketSender.requestStatusMessage( icqAccountRoot.session, plainCookie, icqItem.userId );
          DataUtil.nextBytes( xStatCookie );
          IcqPacketSender.requestXStatusText( icqAccountRoot.session, icqAccountRoot.userId, xStatCookie, icqItem.userId );
        } catch ( IOException ex ) {
          LogUtil.outMessage( "Cannot send status request", true );
        }
        // Emulator.emulateStatusMessage(icqAccountRoot);
      }
    }.start();
  }

  public void setPlainStatusText( String text ) {
    statusText.setCaption( text );
    MidletMain.screen.repaint();
  }

  public void setXStatusText( String title, String description ) {
    xStatusDescr.setCaption( title.concat( " " ).concat( description ) );
    MidletMain.screen.repaint();
  }
}
