package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.TagUtil;
import javax.microedition.io.ConnectionNotFoundException;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class UpdateCheckFrame extends Window {

  public Pane pane;
  public UpdateChecker versionLoader;
  private Label sendLabel;
  private Label infoLabel;

  public UpdateCheckFrame( boolean isAutoCheck ) {
    super( MidletMain.screen );
    if ( isAutoCheck ) {
      showRequestForm();
    } else {
      showPlainForm();
    }
  }

  public final void showRequestForm() {
    /** Header **/
    header = new Header( Localization.getMessage( "UPDATE_CHECK_REQUEST" ) );
    /** Soft **/
    soft = new Soft( MidletMain.screen );
    soft.leftSoft = new PopupItem( Localization.getMessage( "CANCEL" ) ) {

      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "CHECK" ) ) {

      public void actionPerformed() {
        showPlainForm();
        MidletMain.screen.repaint();
      }
    };
    /** Pane **/
    pane = new Pane( null, false );

    sendLabel = new Label( Localization.getMessage( "UPDATE_CHECK_NOTIFY" ) );
    sendLabel.setTitle( true );
    pane.addItem( sendLabel );

    infoLabel = new Label( Localization.getMessage( "CHECKING_CAUSE" ) );
    pane.addItem( infoLabel );

    /** Set GObject **/
    setGObject( pane );
  }

  public final void showPlainForm() {
    /** Header **/
    header = new Header( Localization.getMessage( "UPDATE_CHECK_FRAME" ) );
    /** Soft **/
    soft = new Soft( MidletMain.screen );
    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {

      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "HOMEPAGE" ) ) {

      public void actionPerformed() {
        try {
          MidletMain.midletMain.platformRequest( "http://www.tomclaw.com" );
        } catch ( ConnectionNotFoundException ex ) {
        }
      }
    };
    /** Pane **/
    pane = new Pane( null, false );

    sendLabel = new Label( Localization.getMessage( "UPDATE_CHECK_STATUS" ) );
    sendLabel.setTitle( true );
    pane.addItem( sendLabel );

    infoLabel = new Label( Localization.getMessage( "CHECKING_LABEL" ) );
    pane.addItem( infoLabel );

    /** Set GObject **/
    setGObject( pane );

    /** Update check **/
    requestUpdateCheck();
  }

  public final void requestUpdateCheck() {
    new Thread() {

      public void run() {
        try {
          /**
           * Requesting page
           */
          if ( UpdateChecker.isUpdatePresent() ) {
            sendLabel.setCaption( Localization.getMessage( "UPDATE_STATUS" ) );
            infoLabel.setCaption( Localization.getMessage( "UPDATE_IS_READY" ) );
            addLabelPair( "LATEST_VERSION", UpdateChecker.latestVersion );
            addLabelPair( "DOWNLOAD_COUNT", String.valueOf( UpdateChecker.downloadCount ) );
            addLabelPair( "UPDATE_COUNT", String.valueOf( UpdateChecker.updateCount ) );
            addLabelPair( "VERSION_URL", UpdateChecker.versionURL );
            Button dlButton = new Button( Localization.getMessage( "DOWNLOAD" ) ) {

              public void actionPerformed() {
                try {
                  MidletMain.midletMain.platformRequest( UpdateChecker.versionURL );
                } catch ( ConnectionNotFoundException ex ) {
                }
              }
            };
            dlButton.setFocusable( true );
            dlButton.setFocused( true );
            pane.addItem( dlButton );
            addLabelPair( "CHANGE_LOG", TagUtil.removeTags( UpdateChecker.changeLog ) );
          } else {
            sendLabel.setCaption( Localization.getMessage( "UPDATE_STATUS" ) );
            infoLabel.setCaption( Localization.getMessage( "VERSION_UP_TO_DATE" ) );
            addLabelPair( "COMING_VERSION", UpdateChecker.comingVersion );
            addLabelPair( "COMING_DATE", UpdateChecker.comingDate );
            addLabelPair( "COMING_TEXT", UpdateChecker.comingText );
          }
        } catch ( Throwable ex ) {
          infoLabel.setCaption( Localization.getMessage( "UPDATE_CHECK_FAILED" ) );
        }
        MidletMain.screen.repaint();
      }
    }.start();
  }

  public void addLabelPair( String title, String caption ) {
    Label label1 = new Label( Localization.getMessage( title ) );
    label1.setTitle( true );
    pane.addItem( label1 );

    Label label2 = new Label( caption );
    pane.addItem( label2 );
  }
}
