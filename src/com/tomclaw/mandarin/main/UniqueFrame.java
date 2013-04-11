package com.tomclaw.mandarin.main;

import com.tomclaw.mandarin.core.BuddyItem;
import com.tomclaw.mandarin.core.AccountRoot;
import com.tomclaw.bingear.GroupNotFoundException;
import com.tomclaw.bingear.IncorrectValueException;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class UniqueFrame extends Window {

  private RadioGroup rdgSettingsType;
  private Check chkNotifStatChange;
  private Check chkNotifMessages;
  private Check chkOnTop;
  private Check chkLockIncoming;
  private Check chkDisableHistory;
  private Check chkDisablePStatusReading;
  private Check chkDisableXStatusReading;
  private Check chkSendSpecialPStatus;
  private Field fldSpecialPStatus;
  private Check chkSendSpecialXStatus;
  private Field fldSpecialXTitle;
  private Field fldSpecialXDescr;

  public UniqueFrame( final AccountRoot accountRoot, final BuddyItem buddyItem ) throws IncorrectValueException, GroupNotFoundException {
    super( MidletMain.screen );
    final String groupName = "icq" + buddyItem.getUserId().hashCode();
    /** Header **/
    header = new Header( Localization.getMessage( "UNIQUE_FRAME" ) + " " + buddyItem.getUserId() );
    /** Soft **/
    soft = new Soft( MidletMain.screen );
    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "SAVE" ) ) {
      public void actionPerformed() {
        if ( rdgSettingsType.getCombed() == 0 ) {
          MidletMain.uniquest.removeGroup( groupName );
        } else if ( rdgSettingsType.getCombed() == 1 ) {
          try {
            MidletMain.uniquest.addGroup( groupName );
            MidletMain.uniquest.addItem( groupName, "NOTIF_STAT_CHANGE", chkNotifStatChange.getState() ? "true" : "false" );
            MidletMain.uniquest.addItem( groupName, "NOTIF_MESSAGES", chkNotifMessages.getState() ? "true" : "false" );
            MidletMain.uniquest.addItem( groupName, "ON_TOP", chkOnTop.getState() ? "true" : "false" );
            MidletMain.uniquest.addItem( groupName, "LOCK_INCOMING", chkLockIncoming.getState() ? "true" : "false" );
            MidletMain.uniquest.addItem( groupName, "DISABLE_HISTORY", chkDisableHistory.getState() ? "true" : "false" );
            MidletMain.uniquest.addItem( groupName, "DISABLE_PSTATUS_READING", chkDisablePStatusReading.getState() ? "true" : "false" );
            MidletMain.uniquest.addItem( groupName, "DISABLE_XSTATUS_READING", chkDisableXStatusReading.getState() ? "true" : "false" );
            MidletMain.uniquest.addItem( groupName, "SEND_SPECIAL_PSTATUS", chkSendSpecialPStatus.getState() ? "true" : "false" );
            MidletMain.uniquest.addItem( groupName, "FLD_SPECIAL_PSTATUS", fldSpecialPStatus.getText() );
            MidletMain.uniquest.addItem( groupName, "SEND_SPECIAL_XSTATUS", chkSendSpecialXStatus.getState() ? "true" : "false" );
            MidletMain.uniquest.addItem( groupName, "FLD_SPECIAL_XTITLE", fldSpecialXTitle.getText() );
            MidletMain.uniquest.addItem( groupName, "FLD_SPECIAL_XDESCR", fldSpecialXDescr.getText() );
          } catch ( Throwable ex ) {
            LogUtil.outMessage( "Error in unique settings saving: " + ex.getMessage() );
          }
        }
        MidletMain.saveRmsData( MidletMain.uniquestResFile, MidletMain.uniquest );
        buddyItem.updateUiData();
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    /** Pane **/
    Pane pane = new Pane( null, false );

    Label titleLabel = new Label( Localization.getMessage( "UNIQUE_NOTE" ) );
    titleLabel.setTitle( true );
    pane.addItem( titleLabel );

    rdgSettingsType = new RadioGroup();
    Radio autoSettings = new Radio( Localization.getMessage( "AUTO_SETTINGS" ), MidletMain.uniquest.getGroup( groupName ) == null );
    rdgSettingsType.addRadio( autoSettings );
    autoSettings.setFocusable( true );
    autoSettings.setFocused( true );
    Radio manualSettings = new Radio( Localization.getMessage( "MANUAL_SETTINGS" ), !autoSettings.radioState );
    rdgSettingsType.addRadio( manualSettings );
    manualSettings.setFocusable( true );
    pane.addItem( autoSettings );
    pane.addItem( manualSettings );

    chkNotifStatChange = new Check( Localization.getMessage( "NOTIF_STAT_CHANGE" ), MidletMain.getBoolean( MidletMain.uniquest, groupName, "NOTIF_STAT_CHANGE" ) );
    chkNotifStatChange.setFocusable( true );
    pane.addItem( chkNotifStatChange );
    chkNotifMessages = new Check( Localization.getMessage( "NOTIF_MESSAGES" ), MidletMain.getBoolean( MidletMain.uniquest, groupName, "NOTIF_MESSAGES" ) );
    chkNotifMessages.setFocusable( true );
    pane.addItem( chkNotifMessages );
    chkOnTop = new Check( Localization.getMessage( "ON_TOP" ), MidletMain.getBoolean( MidletMain.uniquest, groupName, "ON_TOP" ) );
    chkOnTop.setFocusable( true );
    pane.addItem( chkOnTop );
    chkLockIncoming = new Check( Localization.getMessage( "LOCK_INCOMING" ), MidletMain.getBoolean( MidletMain.uniquest, groupName, "LOCK_INCOMING" ) );
    chkLockIncoming.setFocusable( true );
    pane.addItem( chkLockIncoming );
    chkDisableHistory = new Check( Localization.getMessage( "DISABLE_HISTORY" ), MidletMain.getBoolean( MidletMain.uniquest, groupName, "DISABLE_HISTORY" ) );
    chkDisableHistory.setFocusable( true );
    pane.addItem( chkDisableHistory );
    chkDisablePStatusReading = new Check( Localization.getMessage( "DISABLE_PSTATUS_READING" ), MidletMain.getBoolean( MidletMain.uniquest, groupName, "DISABLE_PSTATUS_READING" ) );
    chkDisablePStatusReading.setFocusable( true );
    pane.addItem( chkDisablePStatusReading );
    chkDisableXStatusReading = new Check( Localization.getMessage( "DISABLE_XSTATUS_READING" ), MidletMain.getBoolean( MidletMain.uniquest, groupName, "DISABLE_XSTATUS_READING" ) );
    chkDisableXStatusReading.setFocusable( true );
    pane.addItem( chkDisableXStatusReading );
    chkSendSpecialPStatus = new Check( Localization.getMessage( "SEND_SPECIAL_PSTATUS" ), MidletMain.getBoolean( MidletMain.uniquest, groupName, "SEND_SPECIAL_PSTATUS" ) );
    chkSendSpecialPStatus.setFocusable( true );
    pane.addItem( chkSendSpecialPStatus );
    fldSpecialPStatus = new Field( MidletMain.getString( MidletMain.uniquest, groupName, "FLD_SPECIAL_PSTATUS" ) );
    fldSpecialPStatus.setFocusable( true );
    pane.addItem( fldSpecialPStatus );
    chkSendSpecialXStatus = new Check( Localization.getMessage( "SEND_SPECIAL_XSTATUS" ), MidletMain.getBoolean( MidletMain.uniquest, groupName, "SEND_SPECIAL_XSTATUS" ) );
    chkSendSpecialXStatus.setFocusable( true );
    pane.addItem( chkSendSpecialXStatus );
    pane.addItem( new Label( Localization.getMessage( "EXT_STATUS_TITLE" ) ) );
    fldSpecialXTitle = new Field( MidletMain.getString( MidletMain.uniquest, groupName, "FLD_SPECIAL_XTITLE" ) );
    fldSpecialXTitle.setFocusable( true );
    pane.addItem( fldSpecialXTitle );
    pane.addItem( new Label( Localization.getMessage( "EXT_STATUS_DESCR" ) ) );
    fldSpecialXDescr = new Field( MidletMain.getString( MidletMain.uniquest, groupName, "FLD_SPECIAL_XDESCR" ) );
    fldSpecialXDescr.setFocusable( true );
    pane.addItem( fldSpecialXDescr );

    /** Set GObject **/
    setGObject( pane );
  }
}
