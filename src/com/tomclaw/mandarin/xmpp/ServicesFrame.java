package com.tomclaw.mandarin.xmpp;

import com.tomclaw.mandarin.main.InfoFrame;
import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import java.util.Vector;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class ServicesFrame extends Window {

  public Pane hostChangePane;
  public Field hostField;
  public Group servicesGroup;
  public PopupItem servicesPopup;
  public PopupItem hostChangePopup;
  public XmppAccountRoot xmppAccountRoot;
  public String requestId = "";
  // public String from = "";
  public Vector services;
  public ServiceItem parentService;
  private ServiceGroup tempItemsHeader;

  public ServicesFrame( final XmppAccountRoot accountRoot ) {
    super( MidletMain.screen );
    this.xmppAccountRoot = accountRoot;
    /** Header **/
    header = new Header( Localization.getMessage( "SERVICES_FRAME" ) );
    /** Creating soft **/
    soft = new Soft( MidletMain.screen );
    /** Left soft items **/
    soft.leftSoft = new PopupItem( Localization.getMessage( "CANCEL" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    /** Right soft items **/
    servicesPopup = new PopupItem( Localization.getMessage( "MENU" ) );
    servicesPopup.addSubItem( new PopupItem( Localization.getMessage( "CHANGE_HOST" ) ) {
      public void actionPerformed() {
        soft.rightSoft = hostChangePopup;
        setGObject( hostChangePane );
        MidletMain.screen.repaint();
      }
    } );
    servicesPopup.addSubItem( new PopupItem( Localization.getMessage( "SERVICES_ROOT" ) ) {
      public void actionPerformed() {
        hostChangePopup.actionPerformed();
      }
    } );
    servicesPopup.addSubItem( new PopupItem( Localization.getMessage( "REFRESH" ) ) {
      public void actionPerformed() {
        requestItems();
      }
    } );
    servicesPopup.addSubItem( new PopupItem( Localization.getMessage( "SERVICE_INFO" ) ) {
      public void actionPerformed() {
        ServiceItem serviceItem = getSelectedItem();
        String[] param = new String[]{ "SERV_JID", "SERV_NAME", "SERV_NODE", "SERV_IDENT", "SERV_FEAT" };
        String[] value = new String[ 5 ];
        value[0] = serviceItem.jid == null ? "" : serviceItem.jid;
        value[1] = serviceItem.name == null ? "" : serviceItem.name;
        value[2] = serviceItem.node == null ? "" : serviceItem.node;
        value[3] = "";
        value[4] = "";
        if ( serviceItem.identityes != null ) {
          for ( int c = 0; c < serviceItem.identityes.size(); c++ ) {
            Identity identity = ( Identity ) serviceItem.identityes.elementAt( c );
            if ( c > 0 ) {
              value[3] += "\n";
            }
            value[3] += identity.name;
          }
        }
        if ( serviceItem.features != null ) {
          for ( int c = 0; c < serviceItem.features.size(); c++ ) {
            String feature = ( String ) serviceItem.features.elementAt( c );
            if ( c > 0 ) {
              value[4] += "\n";
            }
            value[4] += feature;
          }
        }
        InfoFrame infoFrame = new InfoFrame( param, value );
        infoFrame.s_prevWindow = ServicesFrame.this;
        MidletMain.screen.setActiveWindow( infoFrame );
      }
    } );
    /** Right soft items **/
    hostChangePopup = new PopupItem( Localization.getMessage( "SEARCH" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setWaitScreenState( true );
        soft.rightSoft = servicesPopup;
        setGObject( servicesGroup );
        MidletMain.screen.repaint();
        parentService = new ServiceItem( hostField.getText(), null, hostField.getText() );
        requestId = "srvfrm_host".concat( accountRoot.xmppSession.getId() );
        XmppSender.queryingForInformation( accountRoot.xmppSession, parentService.jid, requestId );
      }
    };
    soft.rightSoft = hostChangePopup;
    /** Creating pane **/
    hostChangePane = new Pane( null, false );
    /** Creating hostChangePane objects **/
    Label notifyLabel = new Label( Localization.getMessage( "ACCOUNT_HOST" ).concat( ":" ) );
    notifyLabel.setTitle( true );
    hostChangePane.addItem( notifyLabel );
    hostChangePane.addItem( new Label( accountRoot.host ) );
    notifyLabel = new Label( Localization.getMessage( "SERVICES_HOST" ).concat( ":" ) );
    notifyLabel.setTitle( true );
    hostChangePane.addItem( notifyLabel );
    hostField = new Field( accountRoot.domain );
    hostField.setFocusable( true );
    hostField.setFocused( true );
    hostField.title = Localization.getMessage( "SERVICES_FRAME" );
    hostChangePane.addItem( hostField );
    /** Creating list **/
    servicesGroup = new Group();
    servicesGroup.actionPerformedEvent = new GroupEvent() {
      public void actionPerformed( GroupChild gc ) {
        if ( ( ( ServiceItem ) gc ).containsFeature( "http://jabber.org/protocol/disco#items" ) ) {
          parentService = ( ServiceItem ) gc;
          requestItems();
        } else {
          showNotify( Localization.getMessage( "WARNING" ),
                  Localization.getMessage( "ITEM_EMPTY" ), false );
        }
      }
    };
    /** Applying pane **/
    setGObject( hostChangePane );
    if ( accountRoot.statusId == XmppStatusUtil.offlineIndex ) {
      showNotify( Localization.getMessage( "ERROR" ),
              Localization.getMessage( "NO_CONNECTION" ), true );
    }
  }

  public void requestItems() {
    LogUtil.outMessage( "Info support: " + parentService.containsFeature( "http://jabber.org/protocol/disco#info" ) );
    if ( parentService.containsFeature( "http://jabber.org/protocol/disco#items" ) ) {
      MidletMain.screen.setWaitScreenState( true );
      requestId = "srvfrm_items".concat( xmppAccountRoot.xmppSession.getId() );
      XmppSender.requesingAllItems( xmppAccountRoot.xmppSession, parentService.jid, parentService.node, requestId );
      new Thread() {
        public void run() {
          String id = requestId;
          try {
            Thread.sleep( 30000 );
          } catch ( InterruptedException ex ) {
          }
          MidletMain.screen.setWaitScreenState( false );
          if ( id.equals( requestId ) ) {
            showNotify( Localization.getMessage( "WARNING" ),
                    Localization.getMessage( "SOME_SERVICES_UNAVAILABLE" ), false );
          }
        }
      }.start();
    } else {
      showNotify( Localization.getMessage( "WARNING" ),
              Localization.getMessage( "ITEM_EMPTY" ), false );
    }
  }

  public void setServicesList( XmppAccountRoot xmppAccountRoot, Vector services, String id ) {
    if ( xmppAccountRoot.equals( this.xmppAccountRoot ) && id.equals( requestId ) ) {
      this.services = services;
      servicesGroup.items.removeAllElements();
      servicesGroup.yOffset = 0;
      // servicesGroup.setSelectedIndex( 0 );

      tempItemsHeader = new ServiceGroup( "unclassified", Localization.getMessage( "UNCLASSIFIED" ) );
      tempItemsHeader.setChilds( services );
      servicesGroup.addHeader( tempItemsHeader );

      if ( parentService.containsFeature( "http://jabber.org/protocol/disco#info" ) ) {
        LogUtil.outMessage( "http://jabber.org/protocol/disco#info supported" );
        for ( int c = 0; c < services.size(); c++ ) {
          String qfiId = "srvfrm_info".concat( xmppAccountRoot.xmppSession.getId() );
          XmppSender.queryingForInformation( this.xmppAccountRoot.xmppSession, ( ( ServiceItem ) services.elementAt( c ) ).jid, qfiId );
        }
      } else {
        LogUtil.outMessage( "No http://jabber.org/protocol/disco#info supported" );
        requestId = "";
        MidletMain.screen.setWaitScreenState( false );
      }
    }
  }

  public void setServiceInfo( XmppAccountRoot xmppAccountRoot, String from, String id, Vector identityes, Vector features ) {
    if ( xmppAccountRoot.equals( this.xmppAccountRoot ) /*&& id.equals(requestId)*/ ) {
      LogUtil.outMessage( "Received service info" );
      for ( int c = 0; c < services.size(); c++ ) {
        ServiceItem t_Service = ( ServiceItem ) services.elementAt( c );
        t_Service.identityes = identityes;
        t_Service.features = features;
        Identity identity;
        if ( identityes.isEmpty() ) {
          identity = new Identity( "unclassified", "unknown", "none" );
        } else {
          identity = ( Identity ) t_Service.identityes.firstElement();
        }
        // t_Service.title = identity.name;
        if ( t_Service.jid.equals( from ) ) {
          boolean isGroupFoundFlag = false;
          ServiceGroup serviceGroup = null;
          for ( int i = 0; i < servicesGroup.items.size(); i++ ) {
            serviceGroup = ( ServiceGroup ) servicesGroup.items.elementAt( i );
            if ( serviceGroup.category.equals( identity.category ) ) {
              isGroupFoundFlag = true;
              serviceGroup.addChild( t_Service );
              break;
            }
          }
          if ( !isGroupFoundFlag ) {
            serviceGroup = new ServiceGroup( identity.category, identity.category );
            serviceGroup.addChild( t_Service );
            servicesGroup.addHeader( serviceGroup );
          }
          services.removeElementAt( c );
          break;
        }
      }
      if ( services.isEmpty() ) {
        requestId = "";
        MidletMain.screen.setWaitScreenState( false );
      }
    }
  }

  public void setNoServiceInfo( XmppAccountRoot xmppAccountRoot, String from, String id ) {
    if ( xmppAccountRoot.equals( this.xmppAccountRoot ) /*&& id.equals(requestId)*/ ) {
      LogUtil.outMessage( "Received NO service info" );
      for ( int c = 0; c < services.size(); c++ ) {
        ServiceItem t_Service = ( ServiceItem ) services.elementAt( c );
        if ( t_Service.jid.equals( from ) ) {
          tempItemsHeader.addChild( t_Service );
          services.removeElementAt( c );
          break;
        }
      }
      if ( services.isEmpty() ) {
        requestId = "";
        MidletMain.screen.setWaitScreenState( false );
      }
    }
  }

  public void setHostInfo( XmppAccountRoot xmppAccountRoot, String id, Vector identityes, Vector features ) {
    if ( this.xmppAccountRoot.equals( xmppAccountRoot ) && id.equals( requestId ) ) {
      LogUtil.outMessage( "Received host info, features: " + features.size() + " identityes: " + identityes.size() );
      parentService.features = features;
      parentService.identityes = identityes;
      requestItems();
    }
  }

  public ServiceItem getSelectedItem() {
    try {
      if ( servicesGroup.selectedRealGroup >= 0 && servicesGroup.selectedRealGroup < servicesGroup.items.size() ) {
        if ( servicesGroup.selectedRealIndex >= 0 && servicesGroup.selectedRealIndex < ( ( ServiceGroup ) servicesGroup.items.elementAt( servicesGroup.selectedRealGroup ) ).getChildsCount() ) {
          return ( ServiceItem ) ( ( ServiceGroup ) servicesGroup.items.elementAt( servicesGroup.selectedRealGroup ) ).getChilds().elementAt( servicesGroup.selectedRealIndex );
        }
      }
    } catch ( java.lang.ClassCastException ex1 ) {
    }
    return null;
  }

  public final void showNotify( final String title, final String message, final boolean isFail ) {
    Soft notifySoft = new Soft( MidletMain.screen );
    notifySoft.leftSoft = new PopupItem( Localization.getMessage( "CLOSE" ) ) {
      public void actionPerformed() {
        closeDialog();
      }
    };
    ServicesFrame.this.showDialog( new Dialog( MidletMain.screen, notifySoft, title, message ) );
    MidletMain.screen.repaint();
    try {
      Thread.sleep( 5000 );
    } catch ( InterruptedException ex ) {
    }
    ServicesFrame.this.closeDialog();
    if ( isFail ) {
      MidletMain.screen.setActiveWindow( s_prevWindow );
    } else {
      MidletMain.screen.repaint();
    }
  }
}
