package com.tomclaw.mandarin.main;

import com.tomclaw.mandarin.core.AccountRoot;
import com.tomclaw.mandarin.icq.IcqStatusUtil;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class SetStatusTextFrame extends Window {

  private Field textField;
  private Check readableCheck;

  public SetStatusTextFrame( final AccountRoot accountRoot, final int statusIndex ) {
    super( MidletMain.screen );
    boolean isReadableEnabled = !accountRoot.getAccType().toUpperCase().equals( "MMP" );
    final String groupHeader = "PStatus_".concat( accountRoot.getAccType().toUpperCase() );
    /** Header **/
    header = new Header( Localization.getMessage( "STATUS_TEXT" ) );
    /** Initializing soft **/
    soft = new Soft( MidletMain.screen );
    /** Left soft items **/
    soft.leftSoft = new PopupItem( Localization.getMessage( "CANCEL" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    /** Right soft item **/
    soft.rightSoft = new PopupItem( Localization.getMessage( "APPLY" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setWaitScreenState( true );
        try {
          /** Checking for group present **/
          if ( MidletMain.statuses.getGroup( groupHeader ) == null ) {
            /** Earlier versions have no such group, only PStatus **/
            MidletMain.statuses.addGroup( groupHeader );
          }
          MidletMain.statuses.addItem( groupHeader, String.valueOf( statusIndex ),
                  textField.getText().concat( "&rdb" )
                  .concat( readableCheck.getState() ? "true" : "false" ) );
          MidletMain.saveRmsData( false, false, true );
          /** Sending status to the network **/
          accountRoot.setStatusText( textField.getText(), readableCheck.getState() );
          /** Closing window **/
          MidletMain.screen.setWaitScreenState( false );
          MidletMain.screen.setActiveWindow( s_prevWindow );
          return;
        } catch ( Throwable ex ) {
          LogUtil.outMessage( ex );
        }
        MidletMain.screen.setWaitScreenState( false );
      }
    };
    /** Initializing pane **/
    Pane pane = new Pane( null, false );
    Label statusHeader = new Label( Localization.getMessage( "PLAIN_STATUS_TEXT" ) );
    statusHeader.setHeader( true );
    pane.addItem( statusHeader );
    pane.addItem( new Label( Localization.getMessage( accountRoot.getStatusDescr( statusIndex ) ).concat( ":" ) ) );
    /** Initializing textField with default status text **/
    String statusText = MidletMain.getString( MidletMain.statuses, groupHeader, String.valueOf( statusIndex ) );
    if ( statusText == null ) {
      statusText = "";
    }
    textField = new Field( statusText.substring( 0, ( statusText.indexOf( "&rdb" ) == -1 ) ? statusText.length() : statusText.indexOf( "&rdb" ) ) );
    textField.setFocusable( true );
    textField.setFocused( true );
    pane.addItem( textField );
    readableCheck = new Check( Localization.getMessage( "STATUS_READABLE" ),
            ( statusText.indexOf( "&rdb" ) == -1 ) ? false : statusText.substring( statusText.indexOf( "&rdb" ) + 4 ).equals( "true" ) );
    readableCheck.setFocusable( true );
    readableCheck.setState( true );
    if ( isReadableEnabled ) {
      pane.addItem( readableCheck );
    }
    /** Applying pane **/
    setGObject( pane );
  }
}
