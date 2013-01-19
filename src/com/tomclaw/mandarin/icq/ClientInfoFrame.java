package com.tomclaw.mandarin.icq;

import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.DataUtil;
import com.tomclaw.utils.HexUtil;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.TimeUtil;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class ClientInfoFrame extends Window {

  public Pane plainInfoPane;
  public Pane dcInfoPane;
  public List capsInfoList;
  public String clientBuffer = "";
  public Tab tab;

  public ClientInfoFrame( final IcqAccountRoot icqAccountRoot, IcqItem icqItem ) {
    super( MidletMain.screen );
    /** Header **/
    header = new Header( Localization.getMessage( "CLIENT_INFO" ) );
    /** Creating soft **/
    soft = new Soft( MidletMain.screen );
    /** Left soft items **/
    soft.leftSoft = new PopupItem( Localization.getMessage( "CLOSE" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "COPY" ) ) {
      public void actionPerformed() {
        MidletMain.buffer = clientBuffer;
      }
    };
    /** Initializing panes **/
    initPanes( icqItem );
    /** Creating tab **/
    tab = new Tab( screen );
    tab.setGObject( plainInfoPane );
    /** Creating objects **/
    tab.addTabItem( new TabItem( Localization.getMessage( "PLAIN_INFO" ), 0, -1 ) );
    tab.addTabItem( new TabItem( Localization.getMessage( "DC_INFO" ), 0, -1 ) );
    tab.addTabItem( new TabItem( Localization.getMessage( "CAPS_INFO" ), 0, -1 ) );
    tab.selectedIndex = 0;
    tab.tabEvent = new TabEvent() {
      public void stateChanged( int i, int i1, int i2 ) {
        switch ( i1 ) {
          case 0: {
            tab.setGObject( plainInfoPane );
            break;
          }
          case 1: {
            tab.setGObject( dcInfoPane );
            break;
          }
          case 2: {
            tab.setGObject( capsInfoList );
            break;
          }
        }
      }
    };
    /** Applying pane **/
    setGObject( tab );
  }

  public final void initPanes( IcqItem icqItem ) {
    /** Plain info **/
    String labelMessage = "";
    String labelDescription = "";
    Label descriptionLabel;
    plainInfoPane = new Pane( null, false );
    plainInfoPane.setTouchOrientation( MidletMain.screen.isPointerEvents );
    for ( int c = 0; c < 5; c++ ) {
      Label onlineLabel = new Label( "" );
      onlineLabel.setTitle( true );
      descriptionLabel = new Label( "" );
      switch ( c ) {
        case 0x00: {
          LogUtil.outMessage( "CLIENTID_LABEL" );
          Capability capability = CapUtil.getCapabilityByType( icqItem.capabilities, Capability.CAP_CLIENTID );
          if ( capability == null || capability.capName.equals( "" ) ) {
            continue;
          }
          labelMessage = "CLIENTID_LABEL";
          labelDescription = capability.capName;
          LogUtil.outMessage( "capability.capName=" + capability.capName );
          LogUtil.outMessage( "capability.capIcon=" + capability.capIcon );
          break;
        }
        case 0x01: {
          LogUtil.outMessage( "SIGNONTIME_LABEL" );
          if ( icqItem.clientInfo.signOnTime == 0 ) {
            continue;
          }
          labelMessage = "SIGNONTIME_LABEL";
          LogUtil.outMessage( labelMessage + ": " + icqItem.clientInfo.signOnTime );
          labelDescription = TimeUtil.getDateString( TimeUtil.getMentionedTimeGMT( icqItem.clientInfo.signOnTime ), true );
          break;
        }
        case 0x02: {
          LogUtil.outMessage( "ONLINETIME_LABEL" );
          if ( System.currentTimeMillis() / 1000 - icqItem.clientInfo.onLineTime == 0 ) {
            continue;
          }
          labelMessage = "ONLINETIME_LABEL";
          LogUtil.outMessage( labelMessage + ": " + icqItem.clientInfo.onLineTime );
          labelDescription = TimeUtil.getTimeString( System.currentTimeMillis() / 1000 - icqItem.clientInfo.onLineTime, true );
          break;
        }
        case 0x03: {
          LogUtil.outMessage( "IDLE_LABEL" );
          if ( icqItem.clientInfo.idleTime == 0 ) {
            continue;
          }
          labelMessage = "IDLE_LABEL";
          labelDescription = TimeUtil.getTimeString( icqItem.clientInfo.idleTime, true );
          break;
        }
        case 0x04: {
          LogUtil.outMessage( "MEMBERSINCETIME_LABEL" );
          if ( icqItem.clientInfo.memberSinceTime == 0 ) {
            continue;
          }
          labelMessage = "MEMBERSINCETIME_LABEL";
          labelDescription = TimeUtil.getDateString( TimeUtil.getMentionedTimeGMT( icqItem.clientInfo.memberSinceTime ), true );
          break;
        }

      }
      onlineLabel.setCaption( Localization.getMessage( labelMessage ) + ": " );
      descriptionLabel.setCaption( labelDescription );

      clientBuffer += onlineLabel.caption + "\n";
      clientBuffer += labelDescription + "\n";

      plainInfoPane.addItem( onlineLabel );
      plainInfoPane.addItem( descriptionLabel );
    }
    /** DC info **/
    dcInfoPane = new Pane( null, false );
    dcInfoPane.setTouchOrientation( MidletMain.screen.isPointerEvents );
    labelMessage = "";
    labelDescription = "";
    for ( int c = 0; c < 9; c++ ) {
      Label onlineLabel = new Label( "" );
      onlineLabel.setTitle( true );
      descriptionLabel = new Label( "" );
      switch ( c ) {
        case 0x00: {
          LogUtil.outMessage( "EXTERNALIP_LABEL" );
          if ( icqItem.clientInfo.externalIp[0] == 0
                  && icqItem.clientInfo.externalIp[1] == 0
                  && icqItem.clientInfo.externalIp[2] == 0
                  && icqItem.clientInfo.externalIp[3] == 0 ) {
            continue;
          }
          labelMessage = "EXTERNALIP_LABEL";
          labelDescription = icqItem.clientInfo.externalIp[0] + "." + icqItem.clientInfo.externalIp[1] + "." + icqItem.clientInfo.externalIp[2] + "." + icqItem.clientInfo.externalIp[3];
          break;
        }
        case 0x01: {
          LogUtil.outMessage( "INTERNALIP_LABEL" );
          if ( icqItem.clientInfo.internalIp[0] == 0
                  && icqItem.clientInfo.internalIp[1] == 0
                  && icqItem.clientInfo.internalIp[2] == 0
                  && icqItem.clientInfo.internalIp[3] == 0 ) {
            continue;
          }
          labelMessage = "INTERNALIP_LABEL";
          labelDescription = icqItem.clientInfo.internalIp[0] + "." + icqItem.clientInfo.internalIp[1] + "." + icqItem.clientInfo.internalIp[2] + "." + icqItem.clientInfo.internalIp[3];
          break;
        }
        case 0x02: {
          LogUtil.outMessage( "DCTCPPORT_LABEL" );
          if ( icqItem.clientInfo.dcTcpPort == 0 ) {
            continue;
          }
          labelMessage = "DCTCPPORT_LABEL";
          labelDescription = String.valueOf( icqItem.clientInfo.dcTcpPort );
          break;
        }
        case 0x03: {
          LogUtil.outMessage( "DCTYPE_LABEL" );
          labelMessage = "DCTYPE_LABEL";
          labelDescription = Localization.getMessage( "DCTYPE_" + icqItem.clientInfo.dcType );
          break;
        }
        case 0x04: {
          LogUtil.outMessage( "DCPROTOCOLVERSION_LABEL" );
          labelMessage = "DCPROTOCOLVERSION_LABEL";
          labelDescription = String.valueOf( icqItem.clientInfo.dcProtocolVersion );
          break;
        }
        case 0x05: {
          LogUtil.outMessage( "DCAUTHCOOKIE_LABEL" );
          if ( icqItem.clientInfo.dcAuthCookie == 0 ) {
            continue;
          }
          labelMessage = "DCAUTHCOOKIE_LABEL";
          byte[] data = new byte[ 4 ];
          DataUtil.put32( data, 0, icqItem.clientInfo.dcAuthCookie );
          labelDescription = "0x".concat( HexUtil.bytesToString( data ) );
          break;
        }
        case 0x06: {
          LogUtil.outMessage( "DCINFO_LABEL" );
          labelMessage = "DCINFO_LABEL";
          byte[] data = new byte[ 4 ];
          DataUtil.put32( data, 0, icqItem.clientInfo.lastInfoUpdateTime );
          labelDescription = "0x".concat( HexUtil.bytesToString( data ).concat( "\n0x" ) );
          DataUtil.put32( data, 0, icqItem.clientInfo.lastExtInfoUpdateTime );
          labelDescription += HexUtil.bytesToString( data ).concat( "\n0x" );
          DataUtil.put32( data, 0, icqItem.clientInfo.lastExtStatusUpdateTime );
          labelDescription += HexUtil.bytesToString( data ).concat( "\n" );
          break;
        }
        case 0x07: {
          LogUtil.outMessage( "WEBFRONTPORT_LABEL" );
          if ( icqItem.clientInfo.webFrontPort == 0 ) {
            continue;
          }
          labelMessage = "WEBFRONTPORT_LABEL";
          labelDescription = String.valueOf( icqItem.clientInfo.webFrontPort );
          break;
        }
        case 0x08: {
          LogUtil.outMessage( "CLIENTFEATURES_LABEL" );
          if ( icqItem.clientInfo.clientFeatures == 0x00 ) {
            continue;
          }
          labelMessage = "CLIENTFEATURES_LABEL";
          byte[] data = new byte[ 4 ];
          DataUtil.put32( data, 0, icqItem.clientInfo.clientFeatures );
          labelDescription = "0x".concat( HexUtil.bytesToString( data ) );
          break;
        }
      }
      onlineLabel.setCaption( Localization.getMessage( labelMessage ) + ": " );
      descriptionLabel.setCaption( labelDescription );

      clientBuffer += onlineLabel.caption + "\n";
      clientBuffer += labelDescription + "\n";

      dcInfoPane.addItem( onlineLabel );
      dcInfoPane.addItem( descriptionLabel );
    }
    /** Caps info **/
    LogUtil.outMessage( "/** Caps info **/" );
    capsInfoList = new List();
    capsInfoList.setTouchOrientation( MidletMain.screen.isPointerEvents );
    for ( int c = 0; c < icqItem.capabilities.length; c++ ) {
      ListItem listItem = new ListItem( ( icqItem.capabilities[c].capName == null ) ? HexUtil.bytesToString( icqItem.capabilities[c].capBytes ) : icqItem.capabilities[c].capName );
      capsInfoList.addItem( listItem );
      clientBuffer += listItem.title + "\n";
    }
    LogUtil.outMessage( "Panes inited" );
  }
}
