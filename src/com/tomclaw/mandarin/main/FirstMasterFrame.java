package com.tomclaw.mandarin.main;

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

  private Pane[] panes = null;
  private int masterStep = 0;
  private int stepsCount = 6;
  private Soft softBack;
  private String netCheckResult = "";
  private Label netCheckResultLabel1;
  private Label netCheckResultLabel2;
  private String registerServer =
          "http://www.tomclaw.com/services/mandarin/scripts/register.php";
  private Radio genderMale, genderFemale;
  private RadioGroup genderGroup;
  private Radio years0, years1, years2, years3, years4;
  private RadioGroup yearsGroup;
  private Check declineInfoSend;

  public FirstMasterFrame() {
    super( MidletMain.screen );
    /** Header **/
    header = new Header( Localization.getMessage( "WELCOME_HEADER" ) );
    /** Soft **/
    soft = new Soft( MidletMain.screen );
    soft.leftSoft = new PopupItem( "" ) {
      public void actionPerformed() {
        /** Checking for soft button is disabled **/
        if ( title.length() == 0 ) {
          return;
        }
        if ( masterStep > 0 ) {
          masterStep--;
          checkState();
        }
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "NEXT" ) ) {
      public void actionPerformed() {
        /** Checking for soft button is disabled **/
        if ( title.length() == 0 ) {
          return;
        }
        if ( masterStep < stepsCount - 1 ) {
          masterStep++;
          checkState();
        } else {
          MidletMain.screen.setActiveWindow( MidletMain.mainFrame );
        }
      }
    };
    /** Pane **/
    panes = new Pane[ stepsCount ];

    /** Welcome screen **/
    panes[0] = new Pane( null, false );
    Label titleLabel1 = new Label( Localization.getMessage( "WELCOME_TITLE" ) );
    titleLabel1.setHeader( true );
    panes[0].addItem( titleLabel1 );
    panes[0].addItem( new Label( Localization.getMessage( "WELCOME_TEXT" ) ) );

    /** License **/
    panes[1] = new Pane( null, false );
    Label titleLabel2 = new Label( Localization.getMessage( "LICENSE_TITLE" ) );
    titleLabel2.setHeader( true );
    panes[1].addItem( titleLabel2 );
    panes[1].addItem( new Label( Localization.getMessage( "LICENSE_TEXT_1" ) ) );
    panes[1].addItem( new Label( Localization.getMessage( "LICENSE_TEXT_2" ) ) );
    panes[1].addItem( new Label( Localization.getMessage( "LICENSE_TEXT_3" ) ) );
    panes[1].addItem( new Label( Localization.getMessage( "LICENSE_TEXT_4" ) ) );

    /** Small user info **/
    panes[2] = new Pane( null, false );
    Label titleLabel4 = new Label( Localization.getMessage( "SMALL_INFO" ) );
    titleLabel4.setHeader( true );
    panes[2].addItem( titleLabel4 );
    panes[2].addItem( new Label( Localization.getMessage( "INFO_MANDARIN_NEEDS" ) ) );
    genderMale = new Radio( Localization.getMessage( "GENDER_MALE" ), false );
    genderFemale = new Radio( Localization.getMessage( "GENDER_FEMALE" ), false );
    genderGroup = new RadioGroup();
    genderGroup.addRadio( genderMale );
    genderGroup.addRadio( genderFemale );
    panes[2].addItem( new Label( Localization.getMessage( "SELECT_GENDER" ) ) );
    panes[2].addItem( genderMale );
    panes[2].addItem( genderFemale );
    years0 = new Radio( Localization.getMessage( "YEARS0" ), false );
    years1 = new Radio( Localization.getMessage( "YEARS1" ), false );
    years2 = new Radio( Localization.getMessage( "YEARS2" ), false );
    years3 = new Radio( Localization.getMessage( "YEARS3" ), false );
    years4 = new Radio( Localization.getMessage( "YEARS4" ), false );
    yearsGroup = new RadioGroup();
    yearsGroup.addRadio( years0 );
    yearsGroup.addRadio( years1 );
    yearsGroup.addRadio( years2 );
    yearsGroup.addRadio( years3 );
    yearsGroup.addRadio( years4 );
    panes[2].addItem( new Label( Localization.getMessage( "SELECT_YEARS" ) ) );
    panes[2].addItem( years1 );
    panes[2].addItem( years2 );
    panes[2].addItem( years3 );
    panes[2].addItem( years4 );
    declineInfoSend = new Check( Localization.getMessage( "DECLINE_INFO_SEND" ), false );
    panes[2].addItem( declineInfoSend );

    /** Network check **/
    panes[3] = new Pane( null, false );
    Label titleLabel3 = new Label( Localization.getMessage( "NETCHECK_TITLE" ) );
    titleLabel3.setHeader( true );
    panes[3].addItem( titleLabel3 );
    panes[3].addItem( new Label( Localization.getMessage( "NETCHECK_TEXT" ) ) );

    /** Network check status **/
    panes[4] = new Pane( null, false );
    panes[4].addItem( new Label( Localization.getMessage( "NETCHECK_PROCESS" ) ) );

    /** Completed **/
    panes[5] = new Pane( null, false );
    netCheckResultLabel1 = new Label( "" );
    netCheckResultLabel2 = new Label( "" );

    /** Set GObject **/
    setGObject( panes[0] );
  }

  public void checkState() {
    if ( masterStep == 3 && !declineInfoSend.getState() ) {
      if ( genderGroup.getCombed() == -1 || yearsGroup.getCombed() == -1 ) {
        masterStep--;
        ActionExec.showError( Localization.getMessage( "FILL_SMALL_FORM" ) );
        return;
      } else {
        masterStep++;
      }
    }
    if ( masterStep > 0 && masterStep < 4 ) {
      soft.leftSoft.title = ( Localization.getMessage( "BACK" ) );
    } else {
      soft.leftSoft.title = "";
    }
    if ( masterStep < stepsCount && masterStep != 4 && masterStep != 5 ) {
      soft.rightSoft.title = ( Localization.getMessage( "NEXT" ) );
    } else if ( masterStep == 5 ) {
      soft.rightSoft.title = ( Localization.getMessage( "FINISH" ) );
    } else {
      soft.rightSoft.title = "";
    }
    /** Set GObject **/
    setGObject( panes[masterStep] );
    if ( masterStep == 4 ) {
      MidletMain.screen.repaint();
      new Thread() {
        public void run() {
          String retreivedData = null;
          try {
            String devicePlatform = System.getProperty( "microedition.platform" );
            if ( devicePlatform == null || devicePlatform.length() == 0 ) {
              devicePlatform = "j2me";
            }
            registerServer += "?dev=" + DataUtil.codeString( devicePlatform )
                    + "&build=" + MidletMain.build
                    + "&ver=" + MidletMain.version
                    + "&loc=" + DataUtil.codeString( MidletMain.locale )
                    + ( declineInfoSend.getState() ? ""
                    : ( "&gender=" + DataUtil.codeString( getGender() )
                    + "&years=" + DataUtil.codeString( getYears() ) ) );
            /** Sending data **/
            retreivedData = NetConnection.retreiveData( registerServer );
            MidletMain.settings.setValue( "Master", "copyId", retreivedData );
            LogUtil.outMessage( retreivedData );
            netCheckResult = "REG_SUCCESS";
          } catch ( Throwable ex1 ) {
            netCheckResult = "REG_FAILED";
          }
          try {
            MidletMain.settings.setValue( "Master", "isFirstRun", "false" );
          } catch ( Throwable ex ) {
          }
          MidletMain.saveRmsData( false, true, false );
          if ( netCheckResult.equals( "REG_SUCCESS" ) ) {
            for ( int c = retreivedData.length(); c > 0; c -= 3 ) {
              retreivedData = retreivedData.substring( 0, c ) + " "
                      + retreivedData.substring( c );
            }
            Label titleLabel = new Label(
                    Localization.getMessage( "NUM_OF_INSTALLS_0" )
                    + " " + retreivedData
                    + Localization.getMessage( "NUM_OF_INSTALLS_1" ) );
            titleLabel.setTitle( true );
            panes[5].addItem( titleLabel );
          }
          netCheckResultLabel1.setCaption( Localization.getMessage( netCheckResult + "_0" ) );
          panes[5].addItem( netCheckResultLabel1 );
          netCheckResultLabel2.setCaption( Localization.getMessage( netCheckResult + "_1" ) );
          panes[5].addItem( netCheckResultLabel2 );
          masterStep++;
          checkState();
          /** Set GObject **/
          setGObject( panes[masterStep] );
          MidletMain.screen.repaint();
        }
      }.start();
    }
  }

  private String getGender() {
    switch ( genderGroup.getCombed() ) {
      case 0: {
        return "male";
      }
      case 1: {
        return "female";
      }
      default: {
        return "";
      }
    }
  }

  private String getYears() {
    switch ( yearsGroup.getCombed() ) {
      case 0: {
        return "<14";
      }
      case 1: {
        return "14-18";
      }
      case 2: {
        return "18-24";
      }
      case 3: {
        return "24-32";
      }
      case 4: {
        return "32>";
      }
      default: {
        return "";
      }
    }
  }
}
