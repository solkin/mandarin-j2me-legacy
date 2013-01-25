package com.tomclaw.mandarin.main;

import com.tomclaw.bingear.BinGear;
import com.tomclaw.bingear.GroupNotFoundException;
import com.tomclaw.bingear.IncorrectValueException;
import com.tomclaw.images.Splitter;
import com.tomclaw.mandarin.icq.IcqPacketParser;
import com.tomclaw.mandarin.xmpp.*;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.tcuilite.smiles.Smiles;
import com.tomclaw.utils.HexUtil;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.RecordUtil;
import com.tomclaw.utils.TimeUtil;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class MidletMain extends MIDlet {

  public static MidletMain midletMain;
  public static Screen screen;
  public static MainFrame mainFrame;
  public static ChatFrame chatFrame;
  public static SettingsFrame settingsFrame;
  public static PhotoPreparingFrame photoPreparingFrame;
  public static ServiceMessagesFrame serviceMessagesFrame;
  public static HistoryRmsRenderer historyRmsRenderer;
  public static SmilesFrame smilesFrame;
  public static ServicesFrame servicesFrame;
  public static BookmarksFrame bookmarksFrame;
  public static GroupChatConfFrame groupChatConfFrame;
  public static GroupChatUsersFrame groupChatUsersFrame;
  public static AffiliationAddFrame affiliationAddFrame;
  /** Data storers **/
  public static BinGear settings = new BinGear();
  public static BinGear accounts = new BinGear();
  public static BinGear statuses = new BinGear();
  public static BinGear uniquest = new BinGear();
  /** Paths **/
  public static String settingsResFile = "settings.ini";
  public static String accountsResFile = "accounts.ini";
  public static String statusesResFile = "statuses.ini";
  public static String uniquestResFile = "uniquest.ini";
  /** RMS paths **/
  /** Images **/
  /** Static settings **/
  /** Network **/
  public static int keepAliveDelay = 20;
  public static int httpHiddenPing = 120;
  public static boolean autoReconnect = true;
  public static int reconnectTime = 15000;
  /** Sound **/
  public static boolean isSound = true;
  public static String defSoundLocation = "/res/sounds/";
  public static String[] eventSound = new String[]{"incoming.mp3", "outgoing.mp3", "online.mp3", "offline.mp3"};
  public static int volumeLevel = 100;
  public static boolean isExpand = true;
  public static boolean alarmRepliesOnly = false;
  public static int vibrateDelay = 400;
  /** General **/
  public static int columnCount = 1;
  public static boolean isSortOnline = true;
  public static boolean isRaiseUnread = true;
  public static boolean isRemoveResources = false;
  public static boolean isStoreHistory = true;
  public static boolean isUseEffects = true;
  /** Filetransfer **/
  public static boolean isAutoAcceptFiles = false;
  public static String incomingFilesFolder = "/e:/";
  /** Tarification **/
  public static boolean isCountData = true;
  public static int dataCost = 0;
  public static long dataCount = 0;
  /** Spy **/
  public static boolean statusChange;
  public static boolean xStatusRead;
  public static boolean mStatusRead;
  public static boolean fileTransfer;
  /** Runtime **/
  public static String buffer = "";
  public static int reqSeqNum = 0;
  public static RecordStore dataCountStore;
  public static boolean isPhotoActive = false;
  public static boolean isLoggerEnabled = false;
  public static int logLevel = 0;
  public static boolean isTest = false;
  public static String locale = "";
  public static Class clazz;
  public static Runtime runtime = null;
  /** Themes **/
  public static String selectedTheme = "_0";
  public static String themeFile = "/res/themes/tcuilite_def.tt2";
  /** Other data **/
  public static String version = null;
  public static String type = null;
  public static String build = null;
  public static int pack_count_invoke_gc = 10;
  public static int pack_count = 0;
  
  public void startApp() {
    byte[] data = HexUtil.stringToBytes( "002500030000656441610000000001fd5b4261646f674d64697250726f66696c65476574436d642869643d3138333733323837322c4e616d653d343735353736363939293a20205b4c6f6f6b75704279456d61696c45762869643d3138333733323837332c656d61696c3d692e736f6c6b696e40636f72702e6d61696c2e72752c41494d3d5945532c4943513d5945532c736563733d302e303034333839293a2053554343455353205b4d6f7266476574446174612869643d3138333733323837342c6562756464793d692e736f6c6b696e40636f72702e6d61696c2e7275293a204d4f5246435f5354415455535f494e56414c49445f4f5045524154494f4e20284641494c454429205d5b53656e64456d61696c546f55494e45762869643d"
            + "3138333733323837352c46616b6555494e3d3636303232352c456d61696c3d692e736f6c6b696e40636f72702e6d61696c2e72752c736563733d302e303034333035293a2020594150505f7374617475733d31302075696e3d36313033333438333120285355434345535329205d5d5b53656e64494d444765744d756c7469496e666f45762869643d3138333733323837382c4e616d653d3437353537363639392c736563733d302e31343935293a20494d442072657475726e20636f64653d31206e4d6174636865733d32206e50616765733d31206e526573756c74733d3220285355434345535329205d5d00000002000000000000000200093631303333343833314e83421700000000002907d0001000000000000000000000000000000000080b0017d098d0b3d0bed18"
            + "0d18c20d0a1d0bed0bbd0bad0b8d0bd07d6000007d700000066000ad098d0b3d0bed180d18c0067000cd0a1d0bed0bbd0bad0b8d0bd006a0017d098d0b3d0bed180d18c20d0a1d0bed0bbd0bad0b8d0bd0068000400000001080c0002000000690016000500640000006e00000078000000820000008c000007d10016000500640000006e00000078000000820000008c0000006d000007d3000007d4000007d900020000006b000007da000400000000006e0046000d00640000006e000000780000007d00000082000400000000008c000400000000009600048000000000a000048000000000aa000000b4000000be000000c8000000d20000006c00040000000007ea00048000000007eb00040000000007ec00040000000007ed00040000000007ee00040000000007ef00"
            + "040000000007f000040000000007f10002007f006f000007f300040000000008090004000000010070000423591be007f800040000000007f9000007fb00040000000007fd00040000000007ff000400000000080200040000000108030004000000000805000400000000080700040000000008080000000000000015692e736f6c6b696e40636f72702e6d61696c2e72758000000000000000002807d0001000000000000000000000000000000000080b0018d098d0b3d0bed180d18c2020d0a1d0bed0bbd0bad0b8d0bd07d6000007d700000066000ad098d0b3d0bed180d18c0067000cd0a1d0bed0bbd0bad0b8d0bd006a0018d098d0b3d0bed180d18c2020d0a1d0bed0bbd0bad0b8d0bd0068000400000001080c0002000007d8002b0003006e0004000000000078000"
            + "40000000100640015692e736f6c6b696e40636f72702e6d61696c2e727500690022000500640000006e000cd09cd0bed181d0bad0b2d0b00078000000820000008c0000006d000007d3000007d4000007d900020000006b000007da000400000000006c00040000000007ea00048000000007eb00040000000007ec00040000000007ed00040000000007ee00040000000007ef00040000000007f000040000000007f10002007f006f000007f30004000000010809000400000001007000042359380007f800040000000007f9000007fb00040000000007fd00040000000007ff00040000000108020004000000000803000400000000080500040000000008070004000000000808000000000000" );
    IcqPacketParser.ICQBuddyUserInfo( null, data, 0, 0, null);
    //if(true)return;
    version = getAppProperty( "MIDlet-Version" );
    type = getAppProperty( "Type" );
    build = getAppProperty( "Build" );
    locale = System.getProperty( "microedition.locale" );
    if ( locale != null && locale.length() > 1 ) {
      locale = locale.substring( 0, 2 );
      System.out.println( "Locale detected: " + locale );
    } else {
      locale = "en";
      System.out.println( "Locale not detected. Default: " + locale );
    }
    runtime = Runtime.getRuntime();
    clazz = Runtime.getRuntime().getClass();
    long freeMemory = Runtime.getRuntime().freeMemory();
    System.out.println( "freeMemory = [ " + freeMemory / 1024 + " ] KiB" );
    try {
      String isTestString = getAppProperty( "Logger" );
      if ( isTestString != null && isTestString.equals( "true" ) ) {
        isLoggerEnabled = true;
      }
    } catch ( Throwable ex ) {
    }
    try {
      String logLevelString = getAppProperty( "Loglevel" );
      if ( logLevelString != null ) {
        logLevel = Integer.parseInt( logLevelString );
      }
    } catch ( Throwable ex ) {
    }
    try {
      String isTestString = getAppProperty( "Test" );
      if ( isTestString != null && isTestString.equals( "true" ) ) {
        isTest = true;
      }
    } catch ( Throwable ex ) {
    }
    /** Temporary logger **/
    LogUtil.initLogger( isLoggerEnabled, false, "127.0.0.1", 2000, false, "/root1/" );
    LogUtil.outMessage( "2 mem. eaten = [ " + ( freeMemory - runtime.freeMemory() ) / 1024 + " ] KiB" );
    /** Fixing midlet instance **/
    midletMain = this;
    /** Screen instance **/
    screen = new Screen( this );
    Theme.checkForUpSize();
    /** Loading data **/
    loadRmsData();
    updateThemesSettings();

    System.out.println( "1 mem. eaten = [ " + ( freeMemory - runtime.freeMemory() ) / 1024 + " ] KiB" );
    /** Splash **/
    SplashFrame splashFrame = new SplashFrame();
    screen.activeWindow = splashFrame;
    /** Displaying scren **/
    screen.show();
    splashFrame.updateGaugeValue( 10 );
    /** Nothing to do **/
    splashFrame.updateGaugeValue( 15 );
    updateAlarmSettings();
    splashFrame.updateGaugeValue( 20 );
    updateFiletransferSettings();
    splashFrame.updateGaugeValue( 25 );
    updateNetworkSettings();
    splashFrame.updateGaugeValue( 30 );
    updateGeneralSettings();
    splashFrame.updateGaugeValue( 35 );
    updateTarificationSettings();
    splashFrame.updateGaugeValue( 40 );
    updateSpySettings();
    splashFrame.updateGaugeValue( 43 );

    updateDataCount();
    splashFrame.updateGaugeValue( 45 );
    saveDataCountThread();
    splashFrame.updateGaugeValue( 50 );
    loadResRmsData( uniquestResFile, uniquest );
    LogUtil.outMessage( "3 mem. eaten = [ " + ( freeMemory - runtime.freeMemory() ) / 1024 + " ] KiB" );
    /** LogUtil instance **/
    if ( isLoggerEnabled ) {
      try {
        String loggerServerHost = getString( settings, "Logger", "loggerServerHost" );
        if ( loggerServerHost.length() == 0 || loggerServerHost.indexOf( ":" ) == -1 ) {
          loggerServerHost = "127.0.0.1:2000";
        }
        LogUtil.initLogger( true,
                getBoolean( settings, "Logger", "outToSocket" ), 
                loggerServerHost.substring( 0, loggerServerHost
                .indexOf( ":" ) ), Integer.parseInt( loggerServerHost
                .substring( loggerServerHost.indexOf( ":" ) + 1 ) ),
                getBoolean( settings, "Logger", "outToFile" ), 
                getString( settings, "Logger", "loggerFile" ) );
        String s_logLevel = getAppProperty( "Loglevel" );
        if ( s_logLevel != null && s_logLevel.equals( "1" ) ) {
          LogUtil.isShowMessages = false;
        }
      } catch ( Throwable ex1 ) {
      }
    }
    splashFrame.updateGaugeValue( 55 );
    try {
      Smiles.readSmileData( true );
    } catch ( Throwable ex ) {
    }
    splashFrame.updateGaugeValue( 60 );
    LogUtil.outMessage( "4 mem. eaten = [ " + ( freeMemory - runtime.freeMemory() ) / 1024 + " ] KiB" );
    /** Loading global settings, also, static IcqAccountRoot etc **/
    /** Loading localization strings **/
    Localization.initLocalizationSupport();
    splashFrame.updateGaugeValue( 64 );
    LogUtil.outMessage( "5 mem. eaten = [ " + ( freeMemory - runtime.freeMemory() ) / 1024 + " ] KiB" );
    /** Loading images **/
    Splitter.splitImage( "/res/groups/img_chat.png" );
    splashFrame.updateGaugeValue( 65 );
    Splitter.splitImage( "/res/groups/img_icqstatus.png" );
    splashFrame.updateGaugeValue( 66 );
    Splitter.splitImage( "/res/groups/img_mmpstatus.png" );
    splashFrame.updateGaugeValue( 67 );
    Splitter.splitImage( "/res/groups/img_xmppstatus.png" );
    splashFrame.updateGaugeValue( 68 );
    Splitter.splitImage( "/res/groups/img_main.png" );
    splashFrame.updateGaugeValue( 69 );
    Splitter.splitImage( "/res/groups/img_files.png" );
    splashFrame.updateGaugeValue( 70 );
    Splitter.splitImage( "/res/groups/img_plist.png" );
    splashFrame.updateGaugeValue( 71 );
    LogUtil.outMessage( "6 mem. eaten = [ " + ( freeMemory - runtime.freeMemory() ) / 1024 + " ] KiB" );
    /** Updating upSize **/
    Theme.checkForUpSize();
    /** History renderer **/
    historyRmsRenderer = new HistoryRmsRenderer();
    splashFrame.updateGaugeValue( 73 );
    /** Creating instance of MainFrame **/
    // smilesFrame = new SmilesFrame(); // May be commented
    splashFrame.updateGaugeValue( 78 );
    chatFrame = new ChatFrame();
    splashFrame.updateGaugeValue( 80 );
    mainFrame = new MainFrame( screen );
    splashFrame.updateGaugeValue( 85 );
    settingsFrame = new SettingsFrame();
    splashFrame.updateGaugeValue( 90 );
    serviceMessagesFrame = new ServiceMessagesFrame();
    splashFrame.updateGaugeValue( 95 );
    settingsFrame.s_prevWindow = mainFrame;
    settingsFrame.s_nextWindow = mainFrame;
    mainFrame.s_prevWindow = settingsFrame;
    chatFrame.s_prevWindow = mainFrame;
    serviceMessagesFrame.s_prevWindow = mainFrame;
    LogUtil.outMessage( "7 mem. eaten = [ " + ( freeMemory - runtime.freeMemory() ) / 1024 + " ] KiB" );
    /** Updating hotkeys **/
    updateHotkeysSettings();
    splashFrame.updateGaugeValue( 100 );
    /** Displaying frame on screen **/
    if ( getBoolean( settings, "Master", "isFirstRun" ) ) {
      LogUtil.outMessage( "First run master" );
      screen.activeWindow = new FirstMasterFrame();
    } else {
      if ( getLong( settings, "Master", "updateCheck" ) < System.currentTimeMillis() / 1000 ) {
        try {
          settings.setValue( "Master", "updateCheck", String.valueOf( System.currentTimeMillis() / 1000 + 60 * 60 * 24 * 30 ) );
          saveRmsData( false, true, false );
        } catch ( GroupNotFoundException ex ) {
        } catch ( IncorrectValueException ex ) {
        }
        UpdateCheckFrame updateCheckFrame = new UpdateCheckFrame( true );
        updateCheckFrame.s_prevWindow = mainFrame;
        screen.activeWindow = updateCheckFrame;
      } else {
        screen.activeWindow = mainFrame;
      }
    }
    screen.repaint();
    LogUtil.outMessage( "8 mem. eaten = [ " + ( freeMemory - runtime.freeMemory() ) / 1024 + " ] KiB" );
    freeMemory = runtime.freeMemory();
    LogUtil.outMessage( "freeMemory = [ " + freeMemory / 1024 + " ] KiB" );
  }

  public static void loadRmsData() {
    try {
      if ( RecordUtil.getRecordsCount( settingsResFile ) > 0 && RecordUtil.getRecordsCount( accountsResFile ) > 0 && RecordUtil.getRecordsCount( statusesResFile ) > 0 ) {
        RecordUtil.readFile( settingsResFile, settings );
        LogUtil.outMessage( "Settings read" );
        validateSettings();
        LogUtil.outMessage( "Settings validated" );
        RecordUtil.readFile( accountsResFile, accounts );
        LogUtil.outMessage( "Accounts read" );
        RecordUtil.readFile( statusesResFile, statuses );
        LogUtil.outMessage( "Statuses read" );
        return;
      }
    } catch ( Throwable ex ) {
      LogUtil.outMessage( "Rms data unexist: " + ex.getMessage() + ". Loading Res" );
    }
    loadResData( true, true, true );
    saveRmsData( true, true, true );
    /*loadResData();
     saveRmsData(false, true, false);*/
  }

  public static void validateSettings() {
    BinGear resSettings = new BinGear();
    try {
      resSettings.importFromIni( new DataInputStream( clazz.getResourceAsStream( "/res/".concat( settingsResFile ) ) ) );
      String[] resGroups = resSettings.listGroups();
      for ( int c = 0; c < resGroups.length; c++ ) {
        String[] resItems = resSettings.listItems( resGroups[c] );
        for ( int i = 0; i < resItems.length; i++ ) {
          String resValue = resSettings.getValue( resGroups[c], resItems[i], true );
          try {
            String rmsValue = settings.getValue( resGroups[c], resItems[i], true );
            if ( rmsValue != null ) {
              continue;
            }
          } catch ( GroupNotFoundException ex1 ) {
            settings.addGroup( resGroups[c] );
          }
          settings.addItem( resGroups[c], resItems[i], resValue );
        }
      }
    } catch ( Throwable ex ) {
      LogUtil.outMessage( "Error in vallidating: " + ex.getMessage() );
      // ex.printStackTrace();
    }
    /*System.out.println("After -------------------------------------");
     try{
     settings.exportToIni(System.out);
     } catch (IOException ex){
     ex.printStackTrace();
     }*/
    saveRmsData( false, true, false );
  }

  public static void saveRmsData( boolean accounts, boolean settings, boolean statuses ) {
    if ( accounts ) {
      saveRmsData( accountsResFile, MidletMain.accounts );
    }
    if ( settings ) {
      saveRmsData( settingsResFile, MidletMain.settings );
    }
    if ( statuses ) {
      saveRmsData( statusesResFile, MidletMain.statuses );
    }
  }

  public static void loadResRmsData( String fileName, BinGear dataGear ) {
    try {
      if ( RecordUtil.getRecordsCount( fileName ) > 0 ) {
        RecordUtil.readFile( fileName, dataGear );
        LogUtil.outMessage( fileName.concat( " read" ) );
        return;
      }
    } catch ( Throwable ex ) {
      LogUtil.outMessage( "Rms data unexist: " + ex.getMessage() + ". Loading Res" );
    }
    loadResData( fileName, dataGear );
    saveRmsData( fileName, dataGear );
  }

  public static void saveRmsData( String fileName, BinGear dataGear ) {
    LogUtil.outMessage( "saveRmsData( " + fileName + " )" );
    try {
      RecordUtil.removeFile( fileName );
    } catch ( Throwable ex ) {
      LogUtil.outMessage( "RMS IOException: \"" + ex.getMessage() + "\" on write. File: [" + fileName + "]" );
    }
    try {
      LogUtil.outMessage( "RMS index: " + RecordUtil.saveFile( fileName, dataGear, false ) );
    } catch ( IOException ex ) {
      LogUtil.outMessage( "RMS IOException: \"" + ex.getMessage() + "\" on write. File: [" + fileName + "]" );
    }
    System.gc();
  }

  public static void loadRmsData( String fileName, BinGear dataGear ) {
    try {
      RecordUtil.readFile( fileName, dataGear );
    } catch ( Throwable ex ) {
      LogUtil.outMessage( "RMS IOException: " + ex.getMessage() + " on read. File: [" + fileName + "]" );
    }
  }

  public static void loadResData( boolean isAccounts, boolean isSettings, boolean isStatuses ) {
    try {
      if ( isSettings ) {
        settings.importFromIni( new DataInputStream( clazz.getResourceAsStream( "/res/".concat( settingsResFile ) ) ) );
      }
      if ( isAccounts ) {
        accounts.importFromIni( new DataInputStream( clazz.getResourceAsStream( "/res/".concat( accountsResFile ) ) ) );
      }
      if ( isStatuses ) {
        statuses.importFromIni( new DataInputStream( clazz.getResourceAsStream( "/res/".concat( statusesResFile ) ) ) );
      }
    } catch ( IOException ex ) {
    } catch ( IncorrectValueException ex ) {
    } catch ( GroupNotFoundException ex ) {
    } catch ( Throwable ex1 ) {
    }
  }

  public static void loadResData( String fileName, BinGear dataGear ) {
    try {
      dataGear.importFromIni( new DataInputStream( clazz.getResourceAsStream( "/res/".concat( fileName ) ) ) );
    } catch ( IOException ex ) {
    } catch ( IncorrectValueException ex ) {
    } catch ( GroupNotFoundException ex ) {
    } catch ( Throwable ex1 ) {
    }
  }

  public static void updateThemesSettings() {
    selectedTheme = getString( settings, "Themes", "selectedTheme" );
    themeFile = getString( settings, "Themes", selectedTheme );
    LogUtil.outMessage( "Selected theme: " + selectedTheme );
    LogUtil.outMessage( "Loading theme file: " + themeFile );
    try {
      Theme.applyData( Theme.loadTheme( themeFile ) );
      LogUtil.outMessage( "Loaded." );
    } catch ( Throwable ex ) {
      LogUtil.outMessage( "Failed: " + ex.getMessage() );
    }
  }

  public static void updateSpySettings() {
    statusChange = getBoolean( settings, "Spy", "statusChange" );
    xStatusRead = getBoolean( settings, "Spy", "xStatusRead" );
    mStatusRead = getBoolean( settings, "Spy", "mStatusRead" );
    fileTransfer = getBoolean( settings, "Spy", "fileTransfer" );
  }

  public static void updateAlarmSettings() {
    eventSound = new String[]{
      getBoolean( settings, "Alarm", "onIncoming" ) ? "incoming.mp3" : "",
      getBoolean( settings, "Alarm", "onOutgoing" ) ? "outgoing.mp3" : "",
      getBoolean( settings, "Alarm", "onOnline" ) ? "online.mp3" : "",
      getBoolean( settings, "Alarm", "onOffline" ) ? "offline.mp3" : ""};
    volumeLevel = getInteger( settings, "Alarm", "volumeLevel" );
    vibrateDelay = getInteger( settings, "Alarm", "vibrateDelay" );
    isExpand = getBoolean( settings, "Alarm", "expandOnIncoming" );
    isSound = getBoolean( settings, "Alarm", "soundEnabled" );
    alarmRepliesOnly = getBoolean( settings, "Alarm", "alarmRepliesOnly" );
  }

  public static void updateFiletransferSettings() {
    isAutoAcceptFiles = getBoolean( settings, "Filetransfer", "autoAcceptFiles" );
    incomingFilesFolder = getString( settings, "Filetransfer", "acceptFilesFolder" );
  }

  public static void updateNetworkSettings() {
    autoReconnect = getBoolean( settings, "Network", "autoReconnect" );
    if ( getBoolean( settings, "Network", "isHttpPing" ) ) {
      httpHiddenPing = getInteger( settings, "Network", "httpPingDelay" );
    } else {
      httpHiddenPing = -1;
    }
    keepAliveDelay = getInteger( settings, "Network", "socketPingDelay" );
    if ( keepAliveDelay < 0 ) {
      keepAliveDelay = 20;
    }
  }

  public static void updateGeneralSettings() {
    isStoreHistory = getBoolean( settings, "General", "storeHistory" );
    isSortOnline = getBoolean( settings, "General", "isSortOnline" );
    isRaiseUnread = getBoolean( settings, "General", "isRaiseUnread" );
    isRemoveResources = getBoolean( settings, "General", "isRemoveResources" );
    TimeUtil.gmtOffset = getInteger( settings, "General", "gmtOffset" );
    TimeUtil.summerTime = getBoolean( settings, "General", "switchTime" );
    columnCount = getInteger( settings, "General", "columnCount" ) + 1;
    isUseEffects = getBoolean( settings, "General", "useEffects" );
    if ( !isUseEffects ) {
      Settings.MENU_DRAW_DIRECTSHADOWS = false;
      Settings.DIALOG_DRAW_SHADOWS = false;
      Settings.DIALOG_DRAW_ALPHABACK = false;
      Settings.DIALOG_SHOW_ANIMATION = false;
      Settings.SCREEN_SHOW_ANIMATION = false;
      Settings.MENU_DRAW_ALPHABACK = false;
    } else {
      Settings.MENU_DRAW_DIRECTSHADOWS = true;
      Settings.DIALOG_DRAW_SHADOWS = true;
      Settings.DIALOG_DRAW_ALPHABACK = true;
      Settings.DIALOG_SHOW_ANIMATION = true;
      Settings.SCREEN_SHOW_ANIMATION = true;
      Settings.MENU_DRAW_ALPHABACK = false;
    }
  }

  public static void updateTarificationSettings() {
    isCountData = getBoolean( settings, "Tarification", "countData" );
    dataCost = getInteger( settings, "Tarification", "costValue" );
  }

  public static void updateHotkeysSettings() {
    /*try {
     // LogUtil.outMessage("KEY_CLIENTINFO = " + getInteger(settings, "Hotkeys", "KEY_CLIENTINFO"));
     settings.exportToIni(System.out);
     } catch (IOException ex) {
     ex.printStackTrace();
     }*/
    KeyEvent t_keyEvent;
    int keyCode;
    for ( int c = 0; c < settingsFrame.keysCaption.length; c++ ) {
      keyCode = getInteger( settings, "Hotkeys", settingsFrame.keysCaption[c] );
      /*if (keyCode == 0 || keyCode == -1 || keyCode == -2) {
       /*try {
       // Hot key not setted up
       settings.setValue("Hotkeys", settingsFrame.keysCaption[c], "0");
       } catch (GroupNotFoundException ex) {
       ex.printStackTrace();
       } catch (IncorrectValueException ex) {
       ex.printStackTrace();
       }
       continue;
       }*/
      if ( settingsFrame.keysCaption[c].equals( "KEY_LEFTACCOUNT" ) ) {
        if ( keyCode == 0 ) {
          mainFrame.accountTabs.KEY_LEFT_EVENT = Screen.LEFT;
        } else {
          mainFrame.accountTabs.KEY_LEFT_EVENT = keyCode;
        }
      } else if ( settingsFrame.keysCaption[c].equals( "KEY_RIGHTACCOUNT" ) ) {
        if ( keyCode == 0 ) {
          mainFrame.accountTabs.KEY_RIGHT_EVENT = Screen.RIGHT;
        } else {
          mainFrame.accountTabs.KEY_RIGHT_EVENT = keyCode;
        }
      } else {
        t_keyEvent = mainFrame.getKeyEvent( settingsFrame.keysCaption[c] );
        if ( t_keyEvent == null ) {
          t_keyEvent = chatFrame.getKeyEvent( settingsFrame.keysCaption[c] );
        }
        if ( t_keyEvent != null ) {
          t_keyEvent.keyCode = keyCode;
        }
      }
    }
    /*mainFrame.addKeyEvent(new KeyEvent(getInteger(settings, "Hotkeys", "KEY_CLIENTINFO"), "KEY_CLIENTINFO", true) {
        
     public void actionPerformed() {
     }
     });*/

  }

  public static void updateDataCount() {
    try {
      dataCountStore = RecordStore.openRecordStore( "dataCountStore", true );
      if ( dataCountStore.getNumRecords() == 0 ) {
        dataCountStore.addRecord( "0".getBytes(), 0, "0".getBytes().length );
      }
      dataCount = Integer.parseInt( new String( dataCountStore.getRecord( 1 ) ) );
    } catch ( Throwable ex ) {
    }
  }

  public static void incrementDataCount( int dataIncrement ) {
    if ( isCountData ) {
      dataCount += dataIncrement;
    }
  }

  public static void saveDataCountThread() {
    new Thread() {

      public void run() {
        Thread.currentThread().setPriority( Thread.MIN_PRIORITY );
        long prevDataCount = 0;
        while ( true ) {
          try {
            Thread.sleep( 2000 );
          } catch ( InterruptedException ex ) {
            continue;
          }
          try {
            if ( dataCount == prevDataCount ) {
              continue;
            }
            dataCountStore.setRecord( 1, String.valueOf( dataCount ).getBytes(), 0, String.valueOf( dataCount ).getBytes().length );
            prevDataCount = dataCount;
          } catch ( RecordStoreNotOpenException ex ) {
          } catch ( InvalidRecordIDException ex ) {
          } catch ( RecordStoreException ex ) {
          }
        }
      }
    }.start();
  }

  public void pauseApp() {
  }

  public void destroyApp( boolean unconditional ) {
  }

  public static void loadOfflineBuddyList( AccountRoot accountRoot, String buddyListFile, Vector buddyItems ) {
    RecordStore recordStore = null;
    try {
      recordStore = RecordStore.openRecordStore( buddyListFile, true );
      byte[] abyte0;
      for ( int c = 1; c <= recordStore.getNumRecords(); c++ ) {
        abyte0 = recordStore.getRecord( c );
        // Class itemClass = Class.forName(itemClassPath);
        buddyItems.addElement( RmsRenderer.getRmsGroupHeader( abyte0, accountRoot ) );
      }
    } catch ( Throwable ex ) {
      buddyItems.removeAllElements();
      LogUtil.outMessage( "Error on buddy list reading: " + ex.getMessage() );
    }
    if ( recordStore != null ) {
      try {
        recordStore.closeRecordStore();
      } catch ( Throwable ex ) {
        LogUtil.outMessage( "Error in closing: " + ex.getMessage() );
      }
    }
  }

  public static void updateOfflineBuddylist( String buddyListFile, Vector buddyItems ) {
    try {
      RecordStore.deleteRecordStore( buddyListFile );
    } catch ( RecordStoreException ex ) {
      LogUtil.outMessage( "RMS error RecordStoreException: " + ex.getMessage() );
    }
    try {
      RecordStore recordStore = RecordStore.openRecordStore( buddyListFile, true );
      GroupHeader groupHeader;
      byte[] abyte0;
      for ( int c = 0; c < buddyItems.size(); c++ ) {
        groupHeader = ( GroupHeader ) buddyItems.elementAt( c );
        abyte0 = RmsRenderer.getRmsData( groupHeader );
        recordStore.addRecord( abyte0, 0, abyte0.length );
      }
      recordStore.closeRecordStore();
    } catch ( RecordStoreException ex ) {
      LogUtil.outMessage( "RMS error on buddy list saving: " + ex.getMessage() );
    }
  }

  public static boolean getBoolean( BinGear dataGear, String groupName, String itemName ) {
    try {
      String string = dataGear.getValue( groupName, itemName );
      if ( string == null ) {
        return false;
      }
      return string.equals( "true" ) ? true : false;
    } catch ( Throwable ex ) {
      return false;
    }
  }

  public static String getString( BinGear dataGear, String groupName, String itemName ) {
    try {
      String string = dataGear.getValue( groupName, itemName );
      if ( string != null ) {
        return string;
      } else {
        return "";
      }
    } catch ( Throwable ex ) {
      return "";
    }
  }

  public static int getInteger( BinGear dataGear, String groupName, String itemName ) {
    try {
      String value = dataGear.getValue( groupName, itemName );
      if ( value == null ) {
        return 0;
      }
      return Integer.parseInt( value );
    } catch ( Throwable ex ) {
      return 0;
    }
  }

  public static long getLong( BinGear dataGear, String groupName, String itemName ) {
    try {
      String value = dataGear.getValue( groupName, itemName );
      if ( value == null ) {
        return 0;
      }
      return Long.parseLong( value );
    } catch ( Throwable ex ) {
      return 0;
    }
  }
}
