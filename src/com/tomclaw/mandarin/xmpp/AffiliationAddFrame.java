package com.tomclaw.mandarin.xmpp;

import com.tomclaw.mandarin.main.ActionExec;
import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import java.io.IOException;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class AffiliationAddFrame extends Window {

  public XmppAccountRoot xmppAccountRoot;
  public String groupChatJid;
  public Field jidField;
  public RadioGroup afflTypeGroup;
  public Field reasonField;
  String requestId = "";

  public AffiliationAddFrame( XmppAccountRoot xmppAccountRoot, String groupChatJid ) {
    super( MidletMain.screen );
    this.xmppAccountRoot = xmppAccountRoot;
    this.groupChatJid = groupChatJid;
    /** Header **/
    this.header = new Header( Localization.getMessage( "AFFILIATION_ADD_FRAME" ) );
    /** Creating soft **/
    soft = new Soft( MidletMain.screen );
    /** Left soft items **/
    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "AFFILIATION_ADD" ) ) {
      public void actionPerformed() {
        try {
          AffiliationAddFrame.this.sendAffiliationAdd();
        } catch ( IOException ex ) {
        }
      }
    };
    /** Crating pane **/
    Pane pane = new Pane( null, false );

    pane.addItem( new Label( Localization.getMessage( "AFFILIATION_JID" ).concat( ":" ) ) );
    jidField = new Field( "" );
    jidField.setFocusable( true );
    jidField.setFocused( true );
    pane.addItem( jidField );

    afflTypeGroup = new RadioGroup();
    Radio memberRadio = new Radio( Localization.getMessage( "GROUP_CHAT_MEMBER" ), true );
    Radio adminRadio = new Radio( Localization.getMessage( "GROUP_CHAT_ADMIN" ), false );
    Radio ownerRadio = new Radio( Localization.getMessage( "GROUP_CHAT_OWNER" ), false );
    Radio outcastRadio = new Radio( Localization.getMessage( "GROUP_CHAT_OUTCAST" ), false );

    memberRadio.setFocusable( true );
    adminRadio.setFocusable( true );
    ownerRadio.setFocusable( true );
    outcastRadio.setFocusable( true );

    afflTypeGroup.addRadio( memberRadio );
    afflTypeGroup.addRadio( adminRadio );
    afflTypeGroup.addRadio( ownerRadio );
    afflTypeGroup.addRadio( outcastRadio );

    pane.addItem( new Label( Localization.getMessage( "AFFILIATION_TYPE" ).concat( ":" ) ) );
    pane.addItem( memberRadio );
    pane.addItem( adminRadio );
    pane.addItem( ownerRadio );
    pane.addItem( outcastRadio );

    pane.addItem( new Label( Localization.getMessage( "AFFILIATION_REASON" ) ) );
    reasonField = new Field( "" );
    reasonField.setFocusable( true );
    pane.addItem( reasonField );
    /** Settings GObject **/
    setGObject( pane );
  }

  public void sendAffiliationAdd() throws IOException {
    String afflType = "none";
    switch ( afflTypeGroup.getCombed() ) {
      case 0x00: {
        afflType = "member";
        break;
      }
      case 0x01: {
        afflType = "admin";
        break;
      }
      case 0x02: {
        afflType = "owner";
        break;
      }
      case 0x03: {
        afflType = "outcast";
        break;
      }
    }
    requestId = "afladd_frm_".concat( xmppAccountRoot.xmppSession.getId() );
    XmppSender.affiliationAddGroupChatLists(
            xmppAccountRoot.xmppSession, groupChatJid,
            requestId, jidField.getText(), afflType, reasonField.getText() );
    MidletMain.screen.setWaitScreenState( true );
  }

  public void setRequestResult( XmppAccountRoot xmppAccountRoot, String requestId ) {
    if ( xmppAccountRoot.equals( this.xmppAccountRoot ) && requestId.equals( this.requestId ) ) {
      MidletMain.screen.setWaitScreenState( false );
      MidletMain.screen.setActiveWindow( s_prevWindow );
      try {
        MidletMain.groupChatUsersFrame.requestLists();
      } catch ( IOException ex ) {
        ActionExec.showFail( Localization.getMessage( "USERS_READING_FAILED" ) );
      }
    }
  }
}
