package com.tomclaw.mandarin.icq;

import com.tomclaw.bingear.GroupNotFoundException;
import com.tomclaw.bingear.IncorrectValueException;
import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class SetExtStatusFrame extends Window {

  public Field titleField;
  public Field descrField;
  public Check readableCheck;

  public SetExtStatusFrame( final IcqAccountRoot icqAccountRoot, final int xStatusId ) {
    super( MidletMain.screen );
    /** Header **/
    header = new Header( Localization.getMessage( "EXT_STATUS_TEXT" ) );
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
        try {
          String[] groups = MidletMain.statuses.listGroups();
          for ( int c = 0; c < groups.length; c++ ) {
            if ( groups[c].equals( "XStatus" ) ) {
              groups = null;
              break;
            }
          }
          if ( groups != null ) {
            MidletMain.statuses.addGroup( "XStatus" );
          }
          MidletMain.statuses.addItem( "XStatus", String.valueOf( xStatusId ), titleField.getText().concat( "&dsc" ).concat( descrField.getText() ).concat( "&rdb" ).concat( readableCheck.state ? "true" : "false" ) );
          MidletMain.saveRmsData( false, false, true );
          icqAccountRoot.xTitle = titleField.getText();
          icqAccountRoot.xText = descrField.getText();
          icqAccountRoot.isXStatusReadable = readableCheck.state;
          icqAccountRoot.saveAllSettings();
          MidletMain.screen.setActiveWindow( s_prevWindow );
        } catch ( Throwable ex ) {
        }
      }
    };
    /** Initializing pane **/
    Pane pane = new Pane( null, false );
    Label statusHeader = new Label( Localization.getMessage( "EXT_STATUS_FIELDS" ) );
    statusHeader.setTitle( true );
    pane.addItem( statusHeader );
    pane.addItem( new Label( Localization.getMessage( "xstatus" + xStatusId ).concat( ":" ) ) );
    pane.addItem( new Label( Localization.getMessage( "EXT_STATUS_TITLE" ) ) );
    /** Initializing textField with presetted status text **/
    String statusText = MidletMain.getString( MidletMain.statuses, "XStatus", String.valueOf( xStatusId ) );
    if ( statusText == null || statusText.length() == 0 ) {
      statusText = "&dsc";
    }
    titleField = new Field( statusText.substring( 0, statusText.indexOf( "&dsc" ) ) );
    titleField.setFocusable( true );
    titleField.setFocused( true );
    pane.addItem( titleField );
    pane.addItem( new Label( Localization.getMessage( "EXT_STATUS_DESCR" ) ) );
    descrField = new Field( statusText.substring( statusText.indexOf( "&dsc" ) + 4,
            ( statusText.indexOf( "&rdb" ) == -1 ) ? statusText.length() : statusText.indexOf( "&rdb" ) ) );
    descrField.setFocusable( true );
    pane.addItem( descrField );
    readableCheck = new Check( Localization.getMessage( "EXT_STATUS_READABLE" ),
            ( statusText.indexOf( "&rdb" ) == -1 )
            ? false : statusText.substring( statusText.indexOf( "&rdb" ) + 4 ).equals( "true" ) );
    readableCheck.setFocusable( true );
    pane.addItem( readableCheck );
    /** Applying pane **/
    setGObject( pane );
  }
}
