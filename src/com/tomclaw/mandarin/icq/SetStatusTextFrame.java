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
public class SetStatusTextFrame extends Window {

  public Field textField;
  public Check readableCheck;

  public SetStatusTextFrame( final IcqAccountRoot icqAccountRoot, final int statusId ) {
    super( MidletMain.screen );
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
          String[] groups = MidletMain.statuses.listGroups();
          for ( int c = 0; c < groups.length; c++ ) {
            if ( groups[c].equals( "PStatus" ) ) {
              groups = null;
              break;
            }
          }
          if ( groups != null ) {
            MidletMain.statuses.addGroup( "PStatus" );
          }
          MidletMain.statuses.addItem( "PStatus", String.valueOf( statusId ),
                  textField.getText().concat( "&rdb" ).concat( readableCheck.state ? "true" : "false" ) );
          MidletMain.saveRmsData( false, false, true );
          icqAccountRoot.statusText = textField.getText();
          icqAccountRoot.isPStatusReadable = readableCheck.state;
          icqAccountRoot.saveAllSettings();
          MidletMain.screen.setWaitScreenState( false );
          MidletMain.screen.setActiveWindow( s_prevWindow );
        } catch ( GroupNotFoundException ex ) {
        } catch ( IncorrectValueException ex ) {
        }
        MidletMain.screen.setWaitScreenState( false );
      }
    };
    /** Initializing pane **/
    Pane pane = new Pane( null, false );
    Label statusHeader = new Label( Localization.getMessage( "PLAIN_STATUS_TEXT" ) );
    statusHeader.setTitle( true );
    pane.addItem( statusHeader );
    pane.addItem( new Label( Localization.getMessage( IcqStatusUtil.getStatusDescr( IcqStatusUtil.getStatusIndex( statusId ) ) ).concat( ":" ) ) );
    /** Initializing textField with presetted status text **/
    String statusText = MidletMain.getString( MidletMain.statuses, "PStatus", String.valueOf( statusId ) );
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
    pane.addItem( readableCheck );
    /** Applying pane **/
    setGObject( pane );
  }
}
