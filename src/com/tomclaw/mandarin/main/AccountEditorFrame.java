package com.tomclaw.mandarin.main;

import com.tomclaw.bingear.GroupNotFoundException;
import com.tomclaw.bingear.IncorrectValueException;
import com.tomclaw.mandarin.icq.IcqAccountRoot;
import com.tomclaw.mandarin.mmp.MmpAccountRoot;
import com.tomclaw.mandarin.xmpp.XmppAccountRoot;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import javax.microedition.lcdui.TextField;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class AccountEditorFrame extends Window {

  public Pane pane;
  public RadioGroup protocolSwitcher;
  public Label protName;
  public Field fldLogin;
  public Field fldPassw;
  public Field fldNick;
  public Field fldHost;
  public Field fldPort;
  public Check isUseSsl;

  public void radioAction() {
    switch ( AccountEditorFrame.this.protocolSwitcher.getCombed() ) {
      case 0x00: {
        AccountEditorFrame.this.protName.setCaption(
                Localization.getMessage( "UIN" ).concat( ":" ) );
        fldLogin.setText( "" );
        fldLogin.updateCaption();
        fldLogin.constraints = TextField.EMAILADDR; // NUMERIC;
        fldHost.setText( "login.icq.com" );
        fldHost.updateCaption();
        fldPort.setText( "5190" );
        fldPort.updateCaption();
        isUseSsl.state = false;
        isUseSsl.setFocusable( false );
        break;
      }
      case 0x01: {
        AccountEditorFrame.this.protName.setCaption(
                Localization.getMessage( "MAIL_RU_EMAIL" ).concat( ":" ) );
        fldLogin.setText( "" );
        fldLogin.updateCaption();
        fldLogin.constraints = TextField.EMAILADDR;
        fldHost.setText( "mrim.mail.ru" );
        fldHost.updateCaption();
        fldPort.setText( "2042" );
        fldPort.updateCaption();
        isUseSsl.state = false;
        isUseSsl.setFocusable( false );
        break;
      }
      case 0x02: {
        AccountEditorFrame.this.protName.setCaption(
                Localization.getMessage( "JID" ).concat( ":" ) );
        fldLogin.setText( "" );
        fldLogin.updateCaption();
        fldLogin.constraints = TextField.EMAILADDR;
        fldHost.setText( "" );
        fldHost.updateCaption();
        fldPort.setText( "5222" );
        fldPort.updateCaption();
        isUseSsl.state = true;
        isUseSsl.setFocusable( true );
        break;
      }
    }
  }

  public AccountEditorFrame( String loginId, String nick, String passwd,
          String host, String port, boolean ussl, String type,
          final boolean isEdit ) {
    super( MidletMain.screen );

    LogUtil.outMessage( "ussl = " + ussl );

    soft = new Soft( MidletMain.screen );
    soft.leftSoft = new PopupItem( Localization.getMessage( "CANCEL" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( MidletMain.mainFrame );
      }
    };
    soft.rightSoft = new PopupItem( isEdit ? Localization.getMessage( "SAVE" )
            : Localization.getMessage( "CREATE" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setWaitScreenState( true );
        new Thread() {
          public void run() {
            String accType = "";
            fldNick.setText( fldNick.getText().length() > 0 ? fldNick.getText()
                    : fldLogin.getText() );
            AccountTab tempTabItem =
                    MidletMain.mainFrame.getAccountTab( fldLogin.getText() );
            if ( tempTabItem == null ) {
              tempTabItem =
                      new AccountTab( fldLogin.getText(), fldNick.getText(), 0, 0 );
            }
            try {
              MidletMain.accounts.addGroup( fldLogin.getText() );
              MidletMain.accounts.addItem( fldLogin.getText(), "nick", fldNick.getText() );
              MidletMain.accounts.addItem( fldLogin.getText(), "pass", fldPassw.getText() );
              MidletMain.accounts.addItem( fldLogin.getText(), "host", fldHost.getText() );
              MidletMain.accounts.addItem( fldLogin.getText(), "port", fldPort.getText() );
              MidletMain.accounts.addItem( fldLogin.getText(), "ussl", isUseSsl.state ? "true" : "false" );
              switch ( AccountEditorFrame.this.protocolSwitcher.getCombed() ) {
                case 0x00: {
                  accType = "icq";
                  if ( !isEdit ) {
                    tempTabItem.accountRoot = new IcqAccountRoot( fldLogin.getText() );
                  }
                  break;
                }
                case 0x01: {
                  accType = "mmp";
                  if ( !isEdit ) {
                    tempTabItem.accountRoot = new MmpAccountRoot( fldLogin.getText() );
                  }
                  break;
                }
                case 0x02: {
                  accType = "xmpp";
                  if ( !isEdit ) {
                    tempTabItem.accountRoot = new XmppAccountRoot( fldLogin.getText() );
                  }
                  break;
                }
              }
              MidletMain.accounts.addItem( fldLogin.getText(), "type", accType );
            } catch ( IncorrectValueException ex ) {
            } catch ( GroupNotFoundException ex1 ) {
            }
            tempTabItem.imageFileHash = "/res/groups/img_".concat( accType ).concat( "status.png" ).hashCode();
            tempTabItem.protocolFileHash = tempTabItem.imageFileHash;
            /**
             * Saving accounts *
             */
            tempTabItem.accountRoot.saveAllSettings();
            /**
             * Applying changes on UI *
             */
            tempTabItem.accountRoot.init( !isEdit );
            if ( isEdit ) {
              tempTabItem.title = fldNick.getText();
            } else {
              MidletMain.mainFrame.accountTabs.addTabItem( tempTabItem );
            }
            MidletMain.mainFrame.switchAccountRoot( MidletMain.mainFrame.getActiveAccountRoot() );
            MidletMain.screen.setActiveWindow( MidletMain.mainFrame );
            MidletMain.screen.setWaitScreenState( false );
          }
        }.start();
      }
    };

    protocolSwitcher = new RadioGroup();

    pane = new Pane( this, false );
    pane.addItem( new Label( Localization.getMessage( "SEL_PROTOCOL" ).concat( ":" ) ) );

    Radio radio = new Radio( "ICQ", isEdit ? ( type.equals( "icq" ) ? true : false ) : true ) {
      public void actionPerformed() {
        radioAction();
      }
    };
    radio.setFocusable( !isEdit );
    protocolSwitcher.addRadio( radio );
    pane.addItem( radio );
    radio = new Radio( "Mail.Ru", isEdit ? ( type.equals( "mmp" ) ? true : false ) : false ) {
      public void actionPerformed() {
        radioAction();
      }
    };
    radio.setFocusable( !isEdit );
    protocolSwitcher.addRadio( radio );
    pane.addItem( radio );
    radio = new Radio( "XMPP (Jabber)", isEdit ? ( type.equals( "xmpp" ) ? true : false ) : false ) {
      public void actionPerformed() {
        radioAction();
      }
    };
    radio.setFocusable( !isEdit );
    protocolSwitcher.addRadio( radio );
    pane.addItem( radio );

    protName = new Label( Localization.getMessage( "UIN" ).concat( ":" ) );
    pane.addItem( protName );
    fldLogin = new Field( isEdit ? loginId : "" );
    fldLogin.setFocusable( !isEdit );
    fldLogin.setFocused( !isEdit );
    fldLogin.constraints = TextField.EMAILADDR;
    pane.addItem( fldLogin );
    pane.addItem( new Label( Localization.getMessage( "PASSWORD" ).concat( ":" ) ) );
    fldPassw = new Field( isEdit ? passwd : "" );
    fldPassw.setFocusable( true );
    fldPassw.setFocused( isEdit );
    fldPassw.constraints = TextField.PASSWORD;
    pane.addItem( fldPassw );
    pane.addItem( new Label( Localization.getMessage( "NICK" ).concat( ":" ) ) );
    fldNick = new Field( isEdit ? nick : "" );
    fldNick.setFocusable( true );
    pane.addItem( fldNick );
    pane.addItem( new Label( Localization.getMessage( "HOST" ).concat( ":" ) ) );
    fldHost = new Field( isEdit ? host : "login.icq.com" );
    fldHost.setFocusable( true );
    pane.addItem( fldHost );
    pane.addItem( new Label( Localization.getMessage( "PORT" ).concat( ":" ) ) );
    fldPort = new Field( isEdit ? port : "5190" );
    fldPort.setFocusable( true );
    pane.addItem( fldPort );
    isUseSsl = new Check( Localization.getMessage( "USE_SSL" ).concat( ":" ), isEdit ? ussl : true );
    isUseSsl.setFocusable( true );
    pane.addItem( isUseSsl );

    if ( isEdit ) {
      switch ( AccountEditorFrame.this.protocolSwitcher.getCombed() ) {
        case 0x00: {
          AccountEditorFrame.this.protName.setCaption( Localization.getMessage( "UIN" ).concat( ":" ) );
          fldLogin.constraints = TextField.EMAILADDR;
          isUseSsl.setFocusable( false );
          break;
        }
        case 0x01: {
          AccountEditorFrame.this.protName.setCaption( Localization.getMessage( "MAIL_RU_EMAIL" ).concat( ":" ) );
          fldLogin.constraints = TextField.EMAILADDR;
          isUseSsl.setFocusable( false );
          break;
        }
        case 0x02: {
          AccountEditorFrame.this.protName.setCaption( Localization.getMessage( "JID" ).concat( ":" ) );
          fldLogin.constraints = TextField.EMAILADDR;
          isUseSsl.setFocusable( true );
          break;
        }
      }
    }
    setGObject( pane );
  }
}
