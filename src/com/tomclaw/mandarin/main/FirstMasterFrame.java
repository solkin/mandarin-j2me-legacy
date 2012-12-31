package com.tomclaw.mandarin.main;

import com.tomclaw.bingear.GroupNotFoundException;
import com.tomclaw.bingear.IncorrectValueException;
import com.tomclaw.mandarin.net.NetConnection;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.DataUtil;
import com.tomclaw.utils.LogUtil;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class FirstMasterFrame extends Window {

  public Pane[] panes = null;
  private int masterStep = 0;
  public int stepsCount = 5;
  public Soft softBack;
  public String netCheckResult = "";
  public Label netCheckResultLabel1;
  public Label netCheckResultLabel2;
  public String testServer =
          "http://www.tomclaw.com/services/mandarin/scripts/register.php";

  public FirstMasterFrame() {
    super( MidletMain.screen );
    /** Header **/
    header = new Header( Localization.getMessage( "WELCOME_HEADER" ) );
    /** Soft **/
    soft = new Soft( MidletMain.screen );
    soft.leftSoft = new PopupItem( "" ) {

      public void actionPerformed() {
        if ( masterStep > 0 ) {
          masterStep--;
          checkState();
        }
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "NEXT" ) ) {

      public void actionPerformed() {
        if ( masterStep < stepsCount - 1 ) {
          masterStep++;
          checkState();
        } else {
          MidletMain.screen.setActiveWindow( MidletMain.mainFrame );
        }
      }
    };
    /** Pane **/
    panes = new Pane[stepsCount];

    /** 1 **/
    panes[0] = new Pane( null, false );
    Label titleLabel1 = new Label( Localization.getMessage( "WELCOME_TITLE" ) );
    titleLabel1.setTitle( true );
    panes[0].addItem( titleLabel1 );
    panes[0].addItem( new Label( Localization.getMessage( "WELCOME_TEXT" ) ) );

    /** 2 **/
    panes[1] = new Pane( null, false );
    Label titleLabel2 = new Label( Localization.getMessage( "LICENSE_TITLE" ) );
    titleLabel2.setTitle( true );
    panes[1].addItem( titleLabel2 );
    panes[1].addItem( new Label( Localization.getMessage( "LICENSE_TEXT_1" ) ) );
    panes[1].addItem( new Label( Localization.getMessage( "LICENSE_TEXT_2" ) ) );
    panes[1].addItem( new Label( Localization.getMessage( "LICENSE_TEXT_3" ) ) );
    panes[1].addItem( new Label( Localization.getMessage( "LICENSE_TEXT_4" ) ) );

    /** 3 **/
    panes[2] = new Pane( null, false );
    Label titleLabel3 = new Label( Localization.getMessage( "NETCHECK_TITLE" ) );
    titleLabel3.setTitle(true);
    panes[2].addItem( titleLabel3 );
    panes[2].addItem( new Label( Localization.getMessage( "NETCHECK_TEXT" ) ) );

    /** 4 **/
    panes[3] = new Pane( null, false );
    panes[3].addItem( new Label( Localization.getMessage( "NETCHECK_PROCESS" ) ) );

    /** 5 **/
    panes[4] = new Pane( null, false );
    netCheckResultLabel1 = new Label( "" );
    netCheckResultLabel2 = new Label( "" );

    /** Set GObject **/
    setGObject( panes[0] );
  }

  public void checkState() {
    if ( masterStep > 0 && masterStep < 3 ) {
      soft.leftSoft.title = ( Localization.getMessage( "BACK" ) );
    } else {
      soft.leftSoft.title = "";
    }
    if ( masterStep < stepsCount && masterStep != 3 && masterStep != 4 ) {
      soft.rightSoft.title = ( Localization.getMessage( "NEXT" ) );
    } else if ( masterStep == 4 ) {
      soft.rightSoft.title = ( Localization.getMessage( "FINISH" ) );
    } else {
      soft.rightSoft.title = "";
    }
    /** Set GObject **/
    setGObject( panes[masterStep] );
    if ( masterStep == 3 ) {
      MidletMain.screen.repaint();
      new Thread() {

        public void run() {
          String retreivedData = null;
          try {
            String devicePlatform = System.getProperty( "microedition.platform" );
            if ( devicePlatform == null || devicePlatform.length() == 0 ) {
              devicePlatform = "j2me";
            }
            testServer += "?dev=" + DataUtil.codeString( devicePlatform )
                    + "&build=" + MidletMain.build
                    + "&ver=" + MidletMain.version
                    + "&loc=" + DataUtil.codeString( MidletMain.locale );
            /** Sending data **/
            retreivedData = NetConnection.retreiveData( testServer );
            MidletMain.settings.setValue( "Master", "copyId", retreivedData );
            LogUtil.outMessage( retreivedData );
            netCheckResult = "REG_SUCCESS";
          } catch ( Throwable ex1 ) {
            //retreivedData = Localization.getMessage("REG_FAILED");
            netCheckResult = "REG_FAILED";
          }
          try {
            MidletMain.settings.setValue( "Master", "isFirstRun", "false" );
          } catch ( GroupNotFoundException ex ) {
          } catch ( IncorrectValueException ex ) {
          }
          MidletMain.saveRmsData( false, true, false );

          if ( netCheckResult.equals( "REG_SUCCESS" ) ) {
            for ( int c = retreivedData.length(); c > 0; c -= 3 ) {
              retreivedData = retreivedData.substring( 0, c ) + " " + retreivedData.substring( c );
            }
            Label titleLabel = new Label(
                    Localization.getMessage( "NUM_OF_INSTALLS_0" )
                    + " " + retreivedData
                    + Localization.getMessage( "NUM_OF_INSTALLS_1" ) );
            titleLabel.setTitle(true);
            panes[4].addItem( titleLabel );
          }
          netCheckResultLabel1.setCaption( Localization.getMessage( netCheckResult + "_0" ) );
          panes[4].addItem( netCheckResultLabel1 );
          netCheckResultLabel2.setCaption( Localization.getMessage( netCheckResult + "_1" ) );
          panes[4].addItem( netCheckResultLabel2 );
          masterStep++;
          checkState();
          /** Set GObject **/
          setGObject( panes[masterStep] );
          MidletMain.screen.repaint();
        }
      }.start();
    }
  }
}
