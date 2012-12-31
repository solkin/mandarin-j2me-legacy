package com.tomclaw.mandarin.icq;

import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.DataUtil;
import com.tomclaw.utils.LogUtil;
import java.io.IOException;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
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

  public StatusReaderFrame( final IcqAccountRoot icqAccountRoot,
          final IcqItem icqItem ) {
    super( MidletMain.screen );
    /** Header **/
    header = new Header( Localization.getMessage( "STATUS_OF" ).concat( " " )
            .concat( icqItem.getUserNick() ) );
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
        MidletMain.buffer = Localization.getMessage( "STATUS_OF" )
                .concat( " " ).concat( icqItem.getUserNick() ).concat( "\n" )
                .concat( Localization.getMessage( "PLAIN_STATUS" ) )
                .concat( "\n" ).concat( Localization.getMessage( IcqStatusUtil
                .getStatusDescr( icqItem.getStatusIndex() ) ) );
        if ( icqAccountRoot.statusIndex != 0 ) {
          MidletMain.buffer += "\n".concat( statusText.caption );
        }
        if ( capability != null ) {
          MidletMain.buffer += "\n".concat(
                  Localization.getMessage( "X_STATUS" ) ).concat( "\n" )
                  .concat( Localization.getMessage( capability.capIcon ) );
          if ( icqAccountRoot.statusIndex != 0 ) {
            MidletMain.buffer += "\n".concat( xStatusDescr.caption );
          }
        }
      }
    };
    /** Initializing pane **/
    pane = new Pane( null, false );
    Label statusHeader = new Label( Localization.getMessage( "PLAIN_STATUS" ) );
    statusHeader.setTitle( true );
    pane.addItem( statusHeader );
    Label statusLabel = new Label( Localization.getMessage( IcqStatusUtil
            .getStatusDescr( icqItem.getStatusIndex() ) ) );
    //! Setting status icon
    pane.addItem( statusLabel );
    if ( icqAccountRoot.statusIndex != 0 ) {
      statusText = new Label( Localization.getMessage( "REQUESTING" ) );
      pane.addItem( statusText );
    }
    capability = CapUtil.getCapabilityByType( icqItem.capabilities,
            Capability.CAP_XSTATUS );
    if ( capability != null ) {
      Label xStatusHeader = new Label(
              Localization.getMessage( "X_STATUS" ) );
      xStatusHeader.setTitle( true );
      pane.addItem( xStatusHeader );
      Label xStatusLabel = new Label(
              Localization.getMessage( capability.capIcon ) );
      pane.addItem( xStatusLabel );
      if ( icqAccountRoot.statusIndex != 0 ) {
        xStatusDescr = new Label( Localization.getMessage( "REQUESTING" ) );
        pane.addItem( xStatusDescr );
      }
    }
    /** Applying pane **/
    setGObject( pane );
    if ( icqAccountRoot.statusIndex != 0 ) {
      startStatusReading( icqAccountRoot, icqItem );
    }
  }

  public final void startStatusReading( final IcqAccountRoot icqAccountRoot,
          final IcqItem icqItem ) {
    new Thread() {
      public void run() {
        try {
          DataUtil.nextBytes( plainCookie );
          IcqPacketSender.requestStatusMessage( icqAccountRoot.session,
                  plainCookie, icqItem.userId );
          DataUtil.nextBytes( xStatCookie );
          IcqPacketSender.requestXStatusText( icqAccountRoot.session,
                  icqAccountRoot.userId, xStatCookie, icqItem.userId );
        } catch ( IOException ex ) {
          LogUtil.outMessage( "Cannot send status request", true );
        }
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
