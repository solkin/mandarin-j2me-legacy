package com.tomclaw.mandarin.main;

import com.tomclaw.bingear.GroupNotFoundException;
import com.tomclaw.bingear.IncorrectValueException;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import java.io.IOException;
import java.util.Hashtable;
import javax.microedition.lcdui.TextField;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class SettingsFrame extends Window {

  public Tab settingsTab;
  public GObject[] panes;
  public String[] keysCaption = null;
  public int[] keyValues = null;
  /** Objects **/
  private Field loggerServerHost;
  private Field loggerFile;
  private Check outToSocket;
  private Check outToFile;
  /** Alarm **/
  public Check soundEnabled;
  private Check onOnline;
  private Check onOffline;
  private Check onIncoming;
  private Check onOutgoing;
  private Gauge volumeLevel;
  private Field vibrateDelay;
  private Check expandOnIncoming;
  private Check alarmRepliesOnly;
  /** File transfer **/
  private Check autoAcceptFiles;
  public Field acceptFilesFolder;
  /** Network **/
  private Check isHttpPing;
  private Field socketPingDelay;
  private Field httpPingDelay;
  private Check autoReconnect;
  /** General **/
  private Check storeHistory;
  private RadioGroup columnCount;
  private Check isSortOnline;
  private Check isRaiseUnread;
  private Check isRemoveResources;
  private Field gmtOffset;
  private Check switchTime;
  private Check useEffects;
  /** Tarification **/
  private Check countData;
  private Field costValue;
  private Label dataCount;
  /** Themes **/
  RadioGroup themesGroup;
  /** Spy **/
  private Check statusChange;
  private Check xStatusRead;
  private Check mStatusRead;
  private Check fileTransfer;
  /** Runtime **/
  private boolean isCancelFirstPression = true;

  public SettingsFrame() {
    super( MidletMain.screen );

    soft = new Soft( MidletMain.screen );
    soft.leftSoft = new PopupItem( Localization.getMessage( "CANCEL" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( MidletMain.mainFrame );
      }
    };
    soft.rightSoft = new PopupItem( Localization.getMessage( "SAVE" ) ) {
      public void actionPerformed() {
        try {
          if ( MidletMain.isTest ) {
            /** LogUtil **/
            MidletMain.settings.addGroup( "Logger" );
            MidletMain.settings.addItem( "Logger", "outToSocket", outToSocket.state ? "true" : "false" );
            MidletMain.settings.addItem( "Logger", "outToFile", outToFile.state ? "true" : "false" );
            MidletMain.settings.addItem( "Logger", "loggerServerHost", loggerServerHost.getText() );
            MidletMain.settings.addItem( "Logger", "loggerFile", loggerFile.getText() );
          }
          /** Alarm **/
          MidletMain.settings.addGroup( "Alarm" );
          MidletMain.settings.addItem( "Alarm", "soundEnabled", soundEnabled.state ? "true" : "false" );
          MidletMain.settings.addItem( "Alarm", "onOnline", onOnline.state ? "true" : "false" );
          MidletMain.settings.addItem( "Alarm", "onOffline", onOffline.state ? "true" : "false" );
          MidletMain.settings.addItem( "Alarm", "onIncoming", onIncoming.state ? "true" : "false" );
          MidletMain.settings.addItem( "Alarm", "onOutgoing", onOutgoing.state ? "true" : "false" );
          MidletMain.settings.addItem( "Alarm", "volumeLevel", String.valueOf( volumeLevel.value ) );
          MidletMain.settings.addItem( "Alarm", "vibrateDelay", vibrateDelay.getText() );
          MidletMain.settings.addItem( "Alarm", "expandOnIncoming", expandOnIncoming.state ? "true" : "false" );
          MidletMain.settings.addItem( "Alarm", "alarmRepliesOnly", alarmRepliesOnly.state ? "true" : "false" );
          /** Filetransfer **/
          MidletMain.settings.addGroup( "Filetransfer" );
          MidletMain.settings.addItem( "Filetransfer", "autoAcceptFiles", autoAcceptFiles.state ? "true" : "false" );
          MidletMain.settings.addItem( "Filetransfer", "acceptFilesFolder", acceptFilesFolder.getText() );
          /** Network **/
          MidletMain.settings.addGroup( "Network" );
          MidletMain.settings.addItem( "Network", "autoReconnect", autoReconnect.state ? "true" : "false" );
          MidletMain.settings.addItem( "Network", "isHttpPing", isHttpPing.state ? "true" : "false" );
          MidletMain.settings.addItem( "Network", "httpPingDelay", httpPingDelay.getText() );
          MidletMain.settings.addItem( "Network", "socketPingDelay", socketPingDelay.getText() );
          /** General **/
          MidletMain.settings.addGroup( "General" );
          MidletMain.settings.addItem( "General", "storeHistory", storeHistory.state ? "true" : "false" );
          MidletMain.settings.addItem( "General", "isSortOnline", isSortOnline.state ? "true" : "false" );
          MidletMain.settings.addItem( "General", "isRaiseUnread", isRaiseUnread.state ? "true" : "false" );
          MidletMain.settings.addItem( "General", "isRemoveResources", isRemoveResources.state ? "true" : "false" );
          MidletMain.settings.addItem( "General", "switchTime", switchTime.state ? "true" : "false" );
          MidletMain.settings.addItem( "General", "gmtOffset", gmtOffset.getText() );
          MidletMain.settings.addItem( "General", "columnCount", String.valueOf( columnCount.getCombed() ) );
          MidletMain.settings.addItem( "General", "useEffects", useEffects.state ? "true" : "false" );
          /** Tarification **/
          MidletMain.settings.addGroup( "Tarification" );
          MidletMain.settings.addItem( "Tarification", "countData", countData.state ? "true" : "false" );
          MidletMain.settings.addItem( "Tarification", "costValue", costValue.getText() );
          /** Themes **/
          // MidletMain.settings.addGroup("Themes");
          MidletMain.settings.addItem( "Themes", "selectedTheme", "_" + themesGroup.getCombed() );
          /** Hotkeys **/
          MidletMain.settings.addGroup( "Hotkeys" );
          for ( int c = 0; c < keysCaption.length; c++ ) {
            MidletMain.settings.addItem( "Hotkeys", keysCaption[c], Integer.toString( keyValues[c] ) );
          }
          /** Spy **/
          MidletMain.settings.addGroup( "Spy" );
          MidletMain.settings.addItem( "Spy", "statusChange", statusChange.state ? "true" : "false" );
          MidletMain.settings.addItem( "Spy", "xStatusRead", xStatusRead.state ? "true" : "false" );
          MidletMain.settings.addItem( "Spy", "mStatusRead", mStatusRead.state ? "true" : "false" );
          MidletMain.settings.addItem( "Spy", "fileTransfer", fileTransfer.state ? "true" : "false" );
        } catch ( GroupNotFoundException ex ) {
          LogUtil.outMessage( "GroupNotFoundException: " + ex.getMessage() );
        } catch ( IncorrectValueException ex ) {
          LogUtil.outMessage( "IncorrectValueException: " + ex.getMessage() );
        }
        MidletMain.saveRmsData( false, true, false );

        /* GRABBING_SETTINGS
         try {
         FileConnection fc = (FileConnection) Connector.open("file:///e:/settings.ini");
         fc.create();
         MidletMain.settings.exportToIni(fc.openOutputStream());
         fc.close();
         } catch (IOException ex) {
         ex.printStackTrace();
         }*/

        MidletMain.updateAlarmSettings();
        MidletMain.updateFiletransferSettings();
        MidletMain.updateNetworkSettings();
        MidletMain.updateGeneralSettings();
        MidletMain.updateTarificationSettings();
        MidletMain.updateHotkeysSettings();
        MidletMain.updateThemesSettings();
        MidletMain.updateSpySettings();

        MidletMain.mainFrame.updateActiveAccountRoot();

        MidletMain.screen.setActiveWindow( MidletMain.mainFrame );
      }
    };
    settingsTab = new Tab( screen );

    initPanes();
    settingsTab.setGObject( panes[0] );
    settingsTab.addTabItem( new TabItem( Localization.getMessage( "GENERAL" ), IconsType.HASH_MAIN, 12 ) );
    settingsTab.addTabItem( new TabItem( Localization.getMessage( "NETWORK" ), IconsType.HASH_MAIN, 16 ) );
    settingsTab.addTabItem( new TabItem( Localization.getMessage( "ALARM" ), IconsType.HASH_MAIN, 24 ) );
    settingsTab.addTabItem( new TabItem( Localization.getMessage( "FILE_TRANSFER" ), IconsType.HASH_MAIN, 2 ) );
    settingsTab.addTabItem( new TabItem( Localization.getMessage( "TARIFICATION" ), IconsType.HASH_MAIN, 20 ) );
    settingsTab.addTabItem( new TabItem( Localization.getMessage( "HOT_KEYS" ), IconsType.HASH_MAIN, 23 ) );
    settingsTab.addTabItem( new TabItem( Localization.getMessage( "THEMES" ), IconsType.HASH_MAIN, 12 ) );
    settingsTab.addTabItem( new TabItem( Localization.getMessage( "SERVICE_MESSAGES" ), IconsType.HASH_MAIN, 11 ) );
    if ( MidletMain.isTest ) {
      settingsTab.addTabItem( new TabItem( Localization.getMessage( "LOGGER" ), IconsType.HASH_MAIN, 11 ) );
    }

    settingsTab.tabEvent = new TabEvent() {
      public void stateChanged( int pervIndex, int currIndex, int totalItems ) {
        settingsTab.setGObject( panes[currIndex] );
      }
    };
    
    settingsTab.selectedIndex = 0;

    startUpdateDataThread();

    setGObject( settingsTab );
  }

  public final void startUpdateDataThread() {
    new Thread() {
      public void run() {
        while ( true ) {
          try {
            sleep( 2000 );
          } catch ( InterruptedException ex ) {
          }
          if ( MidletMain.screen.activeWindow.equals( SettingsFrame.this ) ) {
            updateDataCount();
          }
        }
      }
    }.start();
  }

  public final void initPanes() {
    panes = new GObject[ MidletMain.isTest ? 9 : 8 ];
    if ( MidletMain.isTest ) {
      /** LogUtil pane **/
      panes[8] = new Pane( null, false );
      panes[8].setTouchOrientation( MidletMain.screen.isPointerEvents );
      Label label1 = new Label( Localization.getMessage( "LOGGER_SERVER_HOST_PORT" ) );
      label1.setTitle( true );
      ( ( Pane ) panes[8] ).addItem( label1 );
      outToSocket = new Check( Localization.getMessage( "OUT_TO_SOCKET" ), MidletMain.getBoolean( MidletMain.settings, "Logger", "outToSocket" ) );
      outToSocket.setFocusable( true );
      outToSocket.setFocused( true );
      ( ( Pane ) panes[8] ).addItem( outToSocket );
      loggerServerHost = new Field( MidletMain.getString( MidletMain.settings, "Logger", "loggerServerHost" ) );
      loggerServerHost.setFocusable( true );
      ( ( Pane ) panes[8] ).addItem( loggerServerHost );
      Label label2 = new Label( Localization.getMessage( "LOGGER_FILE" ) );
      label2.setTitle( true );
      ( ( Pane ) panes[8] ).addItem( label2 );
      outToFile = new Check( Localization.getMessage( "OUT_TO_FILE" ), MidletMain.getBoolean( MidletMain.settings, "Logger", "outToFile" ) );
      outToFile.setFocusable( true );
      ( ( Pane ) panes[8] ).addItem( outToFile );
      loggerFile = new Field( MidletMain.getString( MidletMain.settings, "Logger", "loggerFile" ) );
      loggerFile.setFocusable( true );
      ( ( Pane ) panes[8] ).addItem( loggerFile );
    }
    /** General **/
    panes[0] = new Pane( null, false );
    panes[0].setTouchOrientation( MidletMain.screen.isPointerEvents );
    storeHistory = new Check( Localization.getMessage( "STORE_HISTORY" ), MidletMain.getBoolean( MidletMain.settings, "General", "storeHistory" ) );
    storeHistory.setFocusable( true );
    storeHistory.setFocused( true );
    ( ( Pane ) panes[0] ).addItem( storeHistory );
    ( ( Pane ) panes[0] ).addItem( new Label( Localization.getMessage( "COLUMNS_NOTE" ) ) );
    columnCount = new RadioGroup();
    for ( int c = 1; c <= 5; c++ ) {
      Radio radio = new Radio( String.valueOf( c ), false );
      radio.setFocusable( true );
      columnCount.addRadio( radio );
      ( ( Pane ) panes[0] ).addItem( radio );
    }
    columnCount.setCombed( MidletMain.getInteger( MidletMain.settings, "General", "columnCount" ) );
    isSortOnline = new Check( Localization.getMessage( "SORT_ONLINE" ), MidletMain.getBoolean( MidletMain.settings, "General", "isSortOnline" ) );
    isSortOnline.setFocusable( true );
    ( ( Pane ) panes[0] ).addItem( isSortOnline );
    isRaiseUnread = new Check( Localization.getMessage( "RAISE_UNREAD" ), MidletMain.getBoolean( MidletMain.settings, "General", "isRaiseUnread" ) );
    isRaiseUnread.setFocusable( true );
    ( ( Pane ) panes[0] ).addItem( isRaiseUnread );
    isRemoveResources = new Check( Localization.getMessage( "REMOVE_OFFLINE_RESOURCES" ), MidletMain.getBoolean( MidletMain.settings, "General", "isRemoveResources" ) );
    isRemoveResources.setFocusable( true );
    ( ( Pane ) panes[0] ).addItem( isRemoveResources );
    ( ( Pane ) panes[0] ).addItem( new Label( Localization.getMessage( "GMT_TIME_OFFSET" ) ) );
    gmtOffset = new Field( MidletMain.getString( MidletMain.settings, "General", "gmtOffset" ) );
    gmtOffset.setConstraints( TextField.NUMERIC );
    gmtOffset.setFocusable( true );
    ( ( Pane ) panes[0] ).addItem( gmtOffset );
    switchTime = new Check( Localization.getMessage( "SWITCH_TIME" ), MidletMain.getBoolean( MidletMain.settings, "General", "switchTime" ) );
    switchTime.setFocusable( true );
    ( ( Pane ) panes[0] ).addItem( switchTime );
    useEffects = new Check( Localization.getMessage( "USE_EFFECTS" ), MidletMain.getBoolean( MidletMain.settings, "General", "useEffects" ) );
    useEffects.setFocusable( true );
    ( ( Pane ) panes[0] ).addItem( useEffects );
    /** Network **/
    panes[1] = new Pane( null, false );
    panes[1].setTouchOrientation( MidletMain.screen.isPointerEvents );
    Label label7 = new Label( Localization.getMessage( "PING_NOTE" ) );
    label7.setTitle( true );
    ( ( Pane ) panes[1] ).addItem( label7 );
    isHttpPing = new Check( Localization.getMessage( "HTTP_PING" ), MidletMain.getBoolean( MidletMain.settings, "Network", "isHttpPing" ) );
    isHttpPing.setFocusable( true );
    isHttpPing.setFocused( true );
    ( ( Pane ) panes[1] ).addItem( isHttpPing );
    ( ( Pane ) panes[1] ).addItem( new Label( Localization.getMessage( "HTTP_PING_DELAY" ) ) );
    httpPingDelay = new Field( MidletMain.getString( MidletMain.settings, "Network", "httpPingDelay" ) );
    httpPingDelay.setConstraints( TextField.NUMERIC );
    httpPingDelay.setFocusable( true );
    ( ( Pane ) panes[1] ).addItem( httpPingDelay );
    ( ( Pane ) panes[1] ).addItem( new Label( Localization.getMessage( "SOCKET_PING_DELAY" ) ) );
    socketPingDelay = new Field( MidletMain.getString( MidletMain.settings, "Network", "socketPingDelay" ) );
    socketPingDelay.setConstraints( TextField.NUMERIC );
    socketPingDelay.setFocusable( true );
    ( ( Pane ) panes[1] ).addItem( socketPingDelay );
    ( ( Pane ) panes[1] ).addItem( new Label( Localization.getMessage( "AUTO_RECONNECT_NOTE" ) ) );
    autoReconnect = new Check( Localization.getMessage( "AUTO_RECONNECT" ), MidletMain.getBoolean( MidletMain.settings, "Network", "autoReconnect" ) );
    autoReconnect.setFocusable( true );
    ( ( Pane ) panes[1] ).addItem( autoReconnect );
    /** Alarm **/
    panes[2] = new Pane( null, false );
    panes[2].setTouchOrientation( MidletMain.screen.isPointerEvents );
    soundEnabled = new Check( Localization.getMessage( "SOUND_ENABLED" ), MidletMain.getBoolean( MidletMain.settings, "Alarm", "soundEnabled" ) );
    soundEnabled.setFocusable( true );
    soundEnabled.setFocused( true );
    ( ( Pane ) panes[2] ).addItem( soundEnabled );
    Label label3 = new Label( Localization.getMessage( "SETTINGS_SOUNDS" ) );
    label3.setTitle( true );
    ( ( Pane ) panes[2] ).addItem( label3 );
    onOnline = new Check( Localization.getMessage( "ON_ONLINE" ), MidletMain.getBoolean( MidletMain.settings, "Alarm", "onOnline" ) );
    onOnline.setFocusable( true );
    onOnline.setFocused( true );
    ( ( Pane ) panes[2] ).addItem( onOnline );
    onOffline = new Check( Localization.getMessage( "ON_OFFLINE" ), MidletMain.getBoolean( MidletMain.settings, "Alarm", "onOffline" ) );
    onOffline.setFocusable( true );
    ( ( Pane ) panes[2] ).addItem( onOffline );
    onIncoming = new Check( Localization.getMessage( "ON_INCOMING" ), MidletMain.getBoolean( MidletMain.settings, "Alarm", "onIncoming" ) );
    onIncoming.setFocusable( true );
    ( ( Pane ) panes[2] ).addItem( onIncoming );
    onOutgoing = new Check( Localization.getMessage( "ON_OUTGOING" ), MidletMain.getBoolean( MidletMain.settings, "Alarm", "onOutgoing" ) );
    onOutgoing.setFocusable( true );
    ( ( Pane ) panes[2] ).addItem( onOutgoing );
    volumeLevel = new Gauge( Localization.getMessage( "VOLUME_LEVEL" ) );
    volumeLevel.setFocusable( true );
    volumeLevel.setValue( MidletMain.getInteger( MidletMain.settings, "Alarm", "volumeLevel" ) );
    ( ( Pane ) panes[2] ).addItem( volumeLevel );
    Label label4 = new Label( Localization.getMessage( "SETTINGS_VIBRATE" ) );
    label4.setTitle( true );
    ( ( Pane ) panes[2] ).addItem( label4 );
    ( ( Pane ) panes[2] ).addItem( new Label( Localization.getMessage( "VIBRATE_DELAY" ) ) );
    vibrateDelay = new Field( MidletMain.getString( MidletMain.settings, "Alarm", "vibrateDelay" ) );
    vibrateDelay.setConstraints( TextField.NUMERIC );
    vibrateDelay.setFocusable( true );
    ( ( Pane ) panes[2] ).addItem( vibrateDelay );
    Label label5 = new Label( Localization.getMessage( "EXPAND_NOTE" ) );
    label5.setTitle( true );
    ( ( Pane ) panes[2] ).addItem( label5 );
    expandOnIncoming = new Check( Localization.getMessage( "EXPAND_ON_INCOMING" ), MidletMain.getBoolean( MidletMain.settings, "Alarm", "expandOnIncoming" ) );
    expandOnIncoming.setFocusable( true );
    ( ( Pane ) panes[2] ).addItem( expandOnIncoming );
    alarmRepliesOnly = new Check( Localization.getMessage( "ALARM_REPLIES_ONLY" ), MidletMain.getBoolean( MidletMain.settings, "Alarm", "alarmRepliesOnly" ) );
    alarmRepliesOnly.setFocusable( true );
    ( ( Pane ) panes[2] ).addItem( alarmRepliesOnly );
    /** File transfer **/
    panes[3] = new Pane( null, false );
    panes[3].setTouchOrientation( MidletMain.screen.isPointerEvents );
    Label label6 = new Label( Localization.getMessage( "FILETRANSFER_NOTE" ) );
    label6.setTitle( true );
    ( ( Pane ) panes[3] ).addItem( label6 );
    autoAcceptFiles = new Check( Localization.getMessage( "AUTO_FILE_ACCEPT" ), MidletMain.getBoolean( MidletMain.settings, "Filetransfer", "autoAcceptFiles" ) );
    autoAcceptFiles.setFocusable( true );
    autoAcceptFiles.setFocused( true );
    ( ( Pane ) panes[3] ).addItem( autoAcceptFiles );
    ( ( Pane ) panes[3] ).addItem( new Label( Localization.getMessage( "INCOMING_FILE_FOLDER" ) ) );
    acceptFilesFolder = new Field( MidletMain.getString( MidletMain.settings, "Filetransfer", "acceptFilesFolder" ) );
    acceptFilesFolder.setFocusable( true );
    ( ( Pane ) panes[3] ).addItem( acceptFilesFolder );
    Button button = new Button( Localization.getMessage( "SELECT_FOLDER" ) ) {
      public void actionPerformed() {
        FileBrowserFrame fileBrowserFrame = new FileBrowserFrame( 0x01, null, null );
        fileBrowserFrame.s_prevWindow = SettingsFrame.this;
        MidletMain.screen.setActiveWindow( fileBrowserFrame );
      }
    };
    button.setFocusable( true );
    ( ( Pane ) panes[3] ).addItem( button );
    /** Tarification **/
    panes[4] = new Pane( null, false );
    panes[4].setTouchOrientation( MidletMain.screen.isPointerEvents );
    Label label8 = new Label( Localization.getMessage( "TARIFICATION_NOTE" ) );
    label8.setTitle( true );
    ( ( Pane ) panes[4] ).addItem( label8 );
    countData = new Check( Localization.getMessage( "COUNT_DATA" ), MidletMain.getBoolean( MidletMain.settings, "Tarification", "countData" ) );
    countData.setFocusable( true );
    countData.setFocused( true );
    ( ( Pane ) panes[4] ).addItem( countData );
    ( ( Pane ) panes[4] ).addItem( new Label( Localization.getMessage( "DATA_COST" ) ) );
    costValue = new Field( MidletMain.getString( MidletMain.settings, "Tarification", "costValue" ) );
    costValue.setConstraints( TextField.NUMERIC );
    costValue.setFocusable( true );
    ( ( Pane ) panes[4] ).addItem( costValue );
    Label label9 = new Label( Localization.getMessage( "TARIFICATION_STATUS" ) );
    label9.setTitle( true );
    ( ( Pane ) panes[4] ).addItem( label9 );
    dataCount = new Label( "0 [KiB]" );
    updateDataCount();
    ( ( Pane ) panes[4] ).addItem( dataCount );
    Button button1 = new Button( Localization.getMessage( "RESET_DATA_COUNT" ) ) {
      public void actionPerformed() {
        MidletMain.dataCount = 0;
        updateDataCount();
      }
    };
    button1.setFocusable( true );
    ( ( Pane ) panes[4] ).addItem( button1 );
    /** Hot keys **/
    panes[5] = new List();
    panes[5].setTouchOrientation( MidletMain.screen.isPointerEvents );

    keyValues = new int[ 28 ];
    keysCaption = new String[ 28 ];
    keysCaption[0] = "KEY_CLIENTINFO";
    keysCaption[1] = "KEY_BUDDYINFO";
    keysCaption[2] = "KEY_DIALOG";
    keysCaption[3] = "KEY_STATUSES";
    keysCaption[4] = "KEY_REMOVE";
    keysCaption[5] = "KEY_SENDFILE";
    keysCaption[6] = "KEY_SENDPHOTO";
    keysCaption[7] = "KEY_RENAME";
    keysCaption[8] = "KEY_SOUNDS";
    keysCaption[9] = "KEY_FILTERGROUPS";
    keysCaption[10] = "KEY_FILTEROFFLINE";
    keysCaption[11] = "KEY_SERVMESSAGES";
    keysCaption[12] = "KEY_LEFTACCOUNT";
    keysCaption[13] = "KEY_RIGHTACCOUNT";
    keysCaption[14] = "KEY_MINIMIZE";
    keysCaption[15] = "KEY_WRITE";
    keysCaption[16] = "KEY_COPY";
    keysCaption[17] = "KEY_PASTE";
    keysCaption[18] = "KEY_APPEND";
    keysCaption[19] = "KEY_CLEARCHAT";
    keysCaption[20] = "KEY_CLOSECHAT";
    keysCaption[21] = "KEY_BUDDYLIST_TOP";
    keysCaption[22] = "KEY_BUDDYLIST_BOTTOM";
    keysCaption[23] = "KEY_BUDDYLIST_SCREEN_HIGHER";
    keysCaption[24] = "KEY_BUDDYLIST_SCREEN_LOWER";
    keysCaption[25] = "KEY_REPLY";
    keysCaption[26] = "KEY_CHAT_SCREEN_TOP";
    keysCaption[27] = "KEY_CHAT_SCREEN_BOTTOM";

    ListItem tempListItem;
    for ( int c = 0; c < keysCaption.length; c++ ) {
      tempListItem = new ListItem( Localization.getMessage( keysCaption[c] ) ) {
        public void actionPerformed() {
          isCancelFirstPression = true;
          Soft notifySoft = new Soft( MidletMain.screen );
          notifySoft.leftSoft = new PopupItem( Localization.getMessage( "CLOSE" ) ) {
            public void actionPerformed() {
              closeDialog();
            }
          };
          showDialog( new Dialog( MidletMain.screen, notifySoft, Localization.getMessage( keysCaption[( ( List ) panes[5] ).selectedIndex] ),
                  Localization.getMessage( "PRESS_KEY" ) ) );
          setCapEvent();
          MidletMain.screen.repaint();
        }
      };
      ( ( List ) panes[5] ).addItem( tempListItem );
      keyValues[c] = MidletMain.getInteger( MidletMain.settings, "Hotkeys", keysCaption[c] );
      if ( keyValues[c] != 0 ) {
        try {
          tempListItem.descr = MidletMain.screen.getKeyName( keyValues[c] );
        } catch ( Throwable ex1 ) {
          tempListItem.descr = "\"" + keyValues[c] + "\"";
        }
        LogUtil.outMessage( "HotKeyCode " + keysCaption[c] + " = " + keyValues[c] );
      }
    }
    /** Themes **/
    panes[6] = new Pane( null, false );
    panes[6].setTouchOrientation( MidletMain.screen.isPointerEvents );
    Label label10 = new Label( Localization.getMessage( "SELECT_THEME_NOTE" ) );
    label10.setTitle( true );
    ( ( Pane ) panes[6] ).addItem( label10 );
    themesGroup = new RadioGroup();
    Hashtable themes;
    try {
      themes = MidletMain.settings.getGroup( "Themes" );
      if ( themes == null ) {
        // MidletMain.settings.addGroup("Themes");
        themes = new Hashtable();
        /*for (int j = 0; j < 10; j++) {
         if (j == 0) {
         themes.put("selectedTheme", "_0");
         themes.put("_0", "/res/themes/tcuilite_def.tth");
         } else {
         themes.put("_" + j, "/res/themes/tcuilite_def" + j + ".tth");
         }
         }*/
      }
      String selectedTheme = MidletMain.getString( MidletMain.settings, "Themes", "selectedTheme" );
      for ( int c = 0; c < themes.size() - 1; c++ ) {
        String themePath = ( String ) themes.get( "_" + c );
        String themeName = Theme.loadTitle( themePath );
        Radio radio = new Radio( themeName, selectedTheme.equals( "_" + c ) );
        radio.setFocusable( true );
        radio.setFocused( true );
        themesGroup.addRadio( radio );
        ( ( Pane ) panes[6] ).addItem( radio );
      }
    } catch ( IncorrectValueException ex ) {
    } catch ( GroupNotFoundException ex ) {
    }
    /** Spy **/
    panes[7] = new Pane( null, false );
    panes[7].setTouchOrientation( MidletMain.screen.isPointerEvents );
    Label label11 = new Label( Localization.getMessage( "SELECT_SPY_EVENTS_NOTE" ) );
    label11.setTitle( true );
    ( ( Pane ) panes[7] ).addItem( label11 );
    statusChange = new Check( Localization.getMessage( "TYPE_STATUS_CHANGE" ), MidletMain.getBoolean( MidletMain.settings, "Spy", "statusChange" ) );
    statusChange.setFocusable( true );
    statusChange.setFocused( true );
    ( ( Pane ) panes[7] ).addItem( statusChange );
    xStatusRead = new Check( Localization.getMessage( "TYPE_XSTATUS_READ" ), MidletMain.getBoolean( MidletMain.settings, "Spy", "xStatusRead" ) );
    xStatusRead.setFocusable( true );
    ( ( Pane ) panes[7] ).addItem( xStatusRead );
    mStatusRead = new Check( Localization.getMessage( "TYPE_MSTATUS_READ" ), MidletMain.getBoolean( MidletMain.settings, "Spy", "mStatusRead" ) );
    mStatusRead.setFocusable( true );
    ( ( Pane ) panes[7] ).addItem( mStatusRead );
    fileTransfer = new Check( Localization.getMessage( "TYPE_FILETRANSFER" ), MidletMain.getBoolean( MidletMain.settings, "Spy", "fileTransfer" ) );
    fileTransfer.setFocusable( true );
    ( ( Pane ) panes[7] ).addItem( fileTransfer );
  }

  public void setCapEvent() {
    this.capKeyEvent = new KeyEvent( 0, "", false ) {
      public void actionPerformed() {
        if ( Screen.getExtGameAct( keyCode ) != Screen.FIRE
                && /*Screen.getExtGameAct(keyCode) != Screen.UP &&
                 Screen.getExtGameAct(keyCode) != Screen.DOWN &&
                 Screen.getExtGameAct(keyCode) != Screen.LEFT &&
                 Screen.getExtGameAct(keyCode) != Screen.RIGHT &&*/ Screen.getExtGameAct( keyCode ) != Screen.KEY_CODE_LEFT_MENU
                && Screen.getExtGameAct( keyCode ) != Screen.KEY_CODE_RIGHT_MENU ) {
          keyValues[( ( List ) panes[5] ).selectedIndex] = keyCode;
          LogUtil.outMessage( "capKeyCode = " + keyCode );
          closeDialog();
          SettingsFrame.this.capKeyEvent = null;
          updateHotkeysParam();
          MidletMain.screen.repaint();
        } else if ( !isCancelFirstPression ) {
          keyValues[( ( List ) panes[5] ).selectedIndex] = 0;
          LogUtil.outMessage( "cancelled index " + ( ( List ) panes[5] ).selectedIndex );
          closeDialog();
          SettingsFrame.this.capKeyEvent = null;
          updateHotkeysParam();
          MidletMain.screen.repaint();
        } else {
          isCancelFirstPression = false;
        }
      }
    };
  }

  public void updateHotkeysParam() {
    ListItem tempListItem;
    for ( int c = 0; c < keysCaption.length; c++ ) {
      tempListItem = ( ( List ) panes[5] ).getElement( c );
      if ( keyValues[c] != 0 ) {
        try {
          tempListItem.descr = MidletMain.screen.getKeyName( keyValues[c] );
        } catch ( Throwable ex1 ) {
          tempListItem.descr = "\"" + keyValues[c] + "\"";
        }
        LogUtil.outMessage( "HotKeyCode " + keysCaption[c] + " = " + keyValues[c] );
      } else {
        tempListItem.descr = null;
      }
    }
  }

  public void updateDataCount() {
    String dataString;
    if ( MidletMain.dataCount < 1024 ) {
      dataString = String.valueOf( ( ( int ) MidletMain.dataCount ) ) + " [B]";
    } else if ( MidletMain.dataCount < 1048576 ) {
      dataString = String.valueOf( ( ( int ) MidletMain.dataCount / 1024 ) ) + " [KiB]";
    } else if ( MidletMain.dataCount < 1073741824 ) {
      dataString = String.valueOf( ( ( int ) MidletMain.dataCount / 1048576 ) ) + " [MiB]";
    } else {
      dataString = String.valueOf( ( ( int ) MidletMain.dataCount / 1073741824 ) ) + " [GiB] *CRAZY*";
    }
    dataCount.setCaption( dataString );
    dataCount.updateCaption();
    SettingsFrame.this.prepareGraphics();
    MidletMain.screen.repaint();
  }
}
