package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.DataUtil;
import javax.microedition.lcdui.TextField;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class OpinionSendFrame extends Window {

  public Field emailField;
  public Field opinionField;
  private Pane pane;

  public OpinionSendFrame() {
    super( MidletMain.screen );
    /** Header **/
    header = new Header( Localization.getMessage( "OPINION_FRAME" ) );
    /** Soft **/
    soft = new Soft( MidletMain.screen );
    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {

      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "SEND" ) ) {

      public boolean isEmpty( String textData ) {
        boolean isEmpty = false;
        for ( int c = 0; c < textData.length(); c++ ) {
          if ( textData.charAt( c ) != ' ' ) {
            isEmpty = true;
            break;
          }
        }
        return isEmpty;
      }

      public void actionPerformed() {
        pane = new Pane( null, false );
        soft.rightSoft = new PopupItem( "" );

        Label sendLabel = new Label( Localization.getMessage( "OPINION_SENDING_STATUS" ) );
        sendLabel.setTitle(true);
        pane.addItem( sendLabel );

        Label infoLabel = new Label( Localization.getMessage( "INFO_LABEL" ) );
        pane.addItem( infoLabel );

        setGObject( pane );

        MidletMain.screen.repaint();
        /** Checking form **/
        if ( !isEmpty( emailField.getText() ) || emailField.getText().indexOf( ( char ) '@' ) == -1 || emailField.getText().indexOf( ( char ) '.' ) == -1 || !isEmpty( opinionField.getText() ) ) {
          infoLabel.setCaption( Localization.getMessage( "FORM_INVALID" ) );
          MidletMain.screen.repaint();
          return;
        }
        /** Coding output data **/
        String emailData = DataUtil.codeString( emailField.getText() );
        String textData = DataUtil.codeString( opinionField.getText() );
        String linkAdd = "http://www.tomclaw.com/services/"
                + "mandarin/scripts/opinion.php?e=" + emailData + "&m=" + textData;
        infoLabel.setCaption( Localization.getMessage( "SENDING_LABEL" ) );
        MidletMain.screen.repaint();
        String retreivedData;
        try {
          /** Sending data **/
          // retreivedData = NetConnection.retreiveData(linkAdd);
          retreivedData = Localization.getMessage( "OPINION_SENT" );
        } catch ( Throwable ex1 ) {
          retreivedData = Localization.getMessage( "SENDING_FAILED" );
        }
        infoLabel.setCaption( retreivedData );
        MidletMain.screen.repaint();
      }
    };
    /** GObject **/
    pane = new Pane( null, false );

    pane.addItem( new Label( Localization.getMessage( "OPINION_SERVICE_INFO" ) ) );
    pane.addItem( new Label( Localization.getMessage( "EMAIL_FIELD" ) ) );
    emailField = new Field( "" );
    emailField.constraints = TextField.EMAILADDR;
    emailField.maxSize = 400;
    emailField.isFocusable = true;
    emailField.isFocused = true;
    pane.addItem( emailField );
    pane.addItem( new Label( Localization.getMessage( "OPINION_FIELD" ) ) );
    opinionField = new Field( "" );
    opinionField.isFocusable = true;
    pane.addItem( opinionField );
    Label notifyLabel = new Label( Localization.getMessage( "OPINION_NOTIFY_LABEL" ) );
    notifyLabel.setTitle( true );
    pane.addItem( notifyLabel );

    setGObject( pane );
  }
}
