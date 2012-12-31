package com.tomclaw.mandarin.main;

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

  public BuddyItem buddyItem;
  public RadioGroup rdgSettingsType;
  public Check chkNotifStatChange;
  public Check chkNotifMessages;
  public Check chkOnTop;
  public Check chkLockIncoming;
  public Check chkDisableHistory;
  public Check chkDisablePStatusReading;
  public Check chkDisableXStatusReading;
  public Check chkSendSpecialPStatus;
  public Field fldSpecialPStatus;
  public Check chkSendSpecialXStatus;
  public Field fldSpecialXTitle;
  public Field fldSpecialXDescr;

  public UniqueFrame( final AccountRoot accountRoot, final BuddyItem buddyItem ) throws IncorrectValueException, GroupNotFoundException {
    super( MidletMain.screen );
    final String groupName = "icq" + buddyItem.getUserId().hashCode();
    this.buddyItem = buddyItem;
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
            MidletMain.uniquest.addItem( groupName, "NOTIF_STAT_CHANGE", chkNotifStatChange.state ? "true" : "false" );
            MidletMain.uniquest.addItem( groupName, "NOTIF_MESSAGES", chkNotifMessages.state ? "true" : "false" );
            MidletMain.uniquest.addItem( groupName, "ON_TOP", chkOnTop.state ? "true" : "false" );
            MidletMain.uniquest.addItem( groupName, "LOCK_INCOMING", chkLockIncoming.state ? "true" : "false" );
            MidletMain.uniquest.addItem( groupName, "DISABLE_HISTORY", chkDisableHistory.state ? "true" : "false" );
            MidletMain.uniquest.addItem( groupName, "DISABLE_PSTATUS_READING", chkDisablePStatusReading.state ? "true" : "false" );
            MidletMain.uniquest.addItem( groupName, "DISABLE_XSTATUS_READING", chkDisableXStatusReading.state ? "true" : "false" );
            MidletMain.uniquest.addItem( groupName, "SEND_SPECIAL_PSTATUS", chkSendSpecialPStatus.state ? "true" : "false" );
            MidletMain.uniquest.addItem( groupName, "FLD_SPECIAL_PSTATUS", fldSpecialPStatus.getText() );
            MidletMain.uniquest.addItem( groupName, "SEND_SPECIAL_XSTATUS", chkSendSpecialXStatus.state ? "true" : "false" );
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
