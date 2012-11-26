package com.tomclaw.mandarin.mmp;

import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import java.io.IOException;
import javax.microedition.lcdui.TextField;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class MmpSmsSendFrame extends Window {

  public Field phoneField;
  public Field textField;

  public MmpSmsSendFrame( final MmpAccountRoot mmpAccountRoot ) {
    super( MidletMain.screen );
    /** Header **/
    header = new Header( Localization.getMessage( "SEND_FREE_SMS" ) );
    /** Soft **/
    soft = new Soft( MidletMain.screen );

    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {

      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "SEND" ) ) {

      public void actionPerformed() {
        String phoneNumber = phoneField.getText();
        if ( phoneNumber.startsWith( "8" ) && phoneNumber.length() == 11 ) {
          phoneNumber = "+7" + phoneNumber.substring( 1 );
        }
        if ( phoneNumber.length() == 10 ) {
          phoneNumber = "+7" + phoneNumber;
        }
        if ( !phoneNumber.startsWith( "+" ) ) {
          phoneNumber = "+" + phoneNumber;
        }
        try {
          LogUtil.outMessage( "Sending SMS for " + phoneNumber );
          MmpPacketSender.MRIM_CS_SMS_MESSAGE( mmpAccountRoot, phoneNumber, textField.getText() );
        } catch ( IOException ex ) {
          // ex.printStackTrace();
        }
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    /** Pane **/
    Pane pane = new Pane( this, false );

    Label titleLabel = new Label( Localization.getMessage( "ABOUT_MMP_SMS_SERVICE" ) );
    titleLabel.setTitle( true );
    pane.addItem( titleLabel );

    pane.addItem( new Label( Localization.getMessage( "PHONE_NUMBER" ) ) );
    phoneField = new Field( "+7" );
    phoneField.setFocusable( true );
    phoneField.setFocused( true );
    phoneField.constraints = TextField.PHONENUMBER;
    pane.addItem( phoneField );

    pane.addItem( new Label( Localization.getMessage( "SMS_TEXT" ) ) );
    textField = new Field( "" );
    textField.setFocusable( true );
    textField.constraints = TextField.ANY;
    textField.maxSize = 140;
    pane.addItem( textField );

    /** Set GObject **/
    setGObject( pane );
  }
}
