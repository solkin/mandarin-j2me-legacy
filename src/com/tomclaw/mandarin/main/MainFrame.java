package com.tomclaw.mandarin.main;

import com.tomclaw.bingear.BinGear;
import com.tomclaw.bingear.GroupNotFoundException;
import com.tomclaw.bingear.IncorrectValueException;
import com.tomclaw.images.Splitter;
import com.tomclaw.mandarin.icq.*;
import com.tomclaw.mandarin.mmp.MmpAccountRoot;
import com.tomclaw.mandarin.mmp.MmpPacketSender;
import com.tomclaw.mandarin.mmp.MmpSmsSendFrame;
import com.tomclaw.mandarin.mmp.MmpStatusUtil;
import com.tomclaw.mandarin.xmpp.*;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.RecordUtil;
import com.tomclaw.utils.StringUtil;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Display;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-201
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class MainFrame extends Window {

  public Soft icqSoft;
  /** ICQ right buddy popups **/
  public PopupItem icqBuddyRightPopupItem;
  /** ICQ right group popups **/
  public PopupItem icqGroupRightPopupItem;
  public Soft mmpSoft;
  /** MMP right buddy popups **/
  public PopupItem mmpBuddyRightPopupItem;
  /** MMP right phone popups **/
  public PopupItem mmpPhoneRightPopupItem;
  /** MMP right group popups **/
  public PopupItem mmpGroupRightPopupItem;
  public Soft xmppSoft;
  /** Xmpp right buddy popups **/
  public PopupItem xmppBuddyRightPopupItem;
  /** Xmpp right group popups **/
  public PopupItem xmppGroupRightPopupItem;
  /** Xmpp right conference popups **/
  public PopupItem xmppConfrRightPopupItem;
  public Group buddyList;
  public Tab accountTabs;
  /** Account popups **/
  public PopupItem accountPopupItem;
  /** Settings popups **/
  public PopupItem settingsPopupItem;
  /** Service messages popups **/
  public PopupItem servicePopupItem;
  /** Services popups **/
  public PopupItem servicesPopupItem;
  /** Donate popup **/
  public PopupItem donatePopupItem;
  /** Info popups **/
  public PopupItem infoPopupItem;
  /** Minimize & Exit popups **/
  public PopupItem lockPopupItem;
  public PopupItem minimizePopupItem;
  public PopupItem exitPopupItem;
  public Pane pane;
  /** Custom protocol dialogs **/
  public BuddyInfoFrame buddyInfoFrame;
  public StatusReaderFrame statusReaderFrame;

  /** Hotkeys Events **/
  public MainFrame( Screen screen ) {
    super( screen );
    /** Hotkeys **/
    this.addKeyEvent( new KeyEvent( 0, "KEY_CLIENTINFO", true ) {
      public void actionPerformed() {
        AccountRoot accountRoot = getActiveAccountRoot();
        if ( accountRoot instanceof IcqAccountRoot ) {
          BuddyItem buddyItem = getSelectedBuddyItem();
          if ( buddyItem != null && ( ( IcqItem ) buddyItem ).clientInfo != null ) {
            ClientInfoFrame clientInfoFrame = new ClientInfoFrame( ( IcqAccountRoot ) accountRoot, ( IcqItem ) buddyItem );
            clientInfoFrame.s_prevWindow = MainFrame.this;
            MidletMain.screen.setActiveWindow( clientInfoFrame );
          }
        }
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_BUDDYINFO", true ) {
      public void actionPerformed() {
        AccountRoot accountRoot = getActiveAccountRoot();
        BuddyItem buddyItem = getSelectedBuddyItem();
        if ( buddyItem != null ) {
          buddyInfoFrame = new BuddyInfoFrame( accountRoot, buddyItem );
          buddyInfoFrame.s_prevWindow = MainFrame.this;
          MidletMain.screen.setActiveWindow( buddyInfoFrame );
        }
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_DIALOG", true ) {
      public void actionPerformed() {
        BuddyItem buddyItem = getSelectedBuddyItem();
        if ( buddyItem != null ) {
          Resource resource = null;
          String resourceTitle = "";
          if ( buddyItem instanceof XmppItem ) {
            if ( ( ( XmppItem ) buddyItem ).getUnreadCount() > 0 ) {
              resource = ( ( XmppItem ) buddyItem ).getUnreadResource();
              resourceTitle = resource.resource;
            } else {
              resource = ( ( XmppItem ) buddyItem ).getDefaultResource();
              resourceTitle = resource.resource;
            }
          }
          ChatTab chatTab = MidletMain.chatFrame.getChatTab( getActiveAccountRoot(), ( ( BuddyItem ) buddyItem ).getUserId(), resourceTitle, true );
          if ( chatTab == null ) {
            /** There is no opened chat tab **/
            chatTab = new ChatTab( getActiveAccountRoot(),
                    ( ( BuddyItem ) buddyItem ),
                    resource,
                    getActiveAccountRoot().getStatusImages().hashCode(), "/res/groups/img_chat.png".hashCode() );
            MidletMain.chatFrame.addChatTab( chatTab, true );
          }
        }
        MidletMain.screen.setActiveWindow( MidletMain.chatFrame );
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_STATUSES", true ) {
      public void actionPerformed() {
        AccountRoot accountRoot = getActiveAccountRoot();
        if ( accountRoot instanceof IcqAccountRoot ) {
          BuddyItem buddyItem = getSelectedBuddyItem();
          if ( buddyItem != null ) {
            statusReaderFrame = new StatusReaderFrame( ( IcqAccountRoot ) getActiveAccountRoot(), ( IcqItem ) buddyItem );
            statusReaderFrame.s_prevWindow = MainFrame.this;
            MidletMain.screen.setActiveWindow( statusReaderFrame );
          }
        }
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_UNIQUE", true ) {
      public void actionPerformed() {
        AccountRoot accountRoot = getActiveAccountRoot();
        if ( accountRoot instanceof IcqAccountRoot ) {
          BuddyItem buddyItem = getSelectedBuddyItem();
          if ( buddyItem != null ) {
            UniqueFrame uniqueFrame;
            try {
              uniqueFrame = new UniqueFrame( getActiveAccountRoot(), buddyItem );
              uniqueFrame.s_prevWindow = MainFrame.this;
              MidletMain.screen.setActiveWindow( uniqueFrame );
            } catch ( Throwable ex ) {
              LogUtil.outMessage( "Error loading UniqueFrame: " + ex.getMessage() );
            }
          }
        }
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_REMOVE", true ) {
      public void actionPerformed() {
        try {
          AccountRoot accountRoot = getActiveAccountRoot();
          BuddyItem buddyItem = getSelectedBuddyItem();
          if ( buddyItem != null ) {
            Cookie cookie = accountRoot.removeBuddy( buddyItem );
            QueueAction queueAction = new QueueAction( accountRoot, buddyItem, cookie ) {
              public void actionPerformed( Hashtable params ) {
                // Removing buddyItem
                LogUtil.outMessage( "Action Performed" );
                Vector buddyItems = this.accountRoot.getBuddyItems();
                if ( buddyItems != null && !buddyItems.isEmpty() ) {
                  LogUtil.outMessage( "BuddyItems present" );
                  for ( int c = 0; c < buddyItems.size(); c++ ) {
                    GroupHeader groupHeader = ( GroupHeader ) buddyItems.elementAt( c );
                    if ( groupHeader == null || groupHeader.getChildsCount() == 0 ) {
                      continue;
                    }
                    if ( groupHeader.getChilds().indexOf( this.buddyItem ) != -1 ) {
                      groupHeader.getChilds().removeElement( this.buddyItem );
                      LogUtil.outMessage( "Removed." );
                      break;
                    }
                  }
                  this.accountRoot.updateOfflineBuddylist();
                  LogUtil.outMessage( "Updated." );
                }
              }
            };
            LogUtil.outMessage( "QueueAction created" );
            Queue.pushQueueAction( queueAction );
          } else {
            BuddyGroup buddyGroup = getSelectedGroupItem();
            if ( buddyGroup != null ) {
              Cookie cookie = accountRoot.removeGroup( buddyGroup );

              QueueAction queueAction = new QueueAction( accountRoot, buddyGroup, cookie ) {
                public void actionPerformed( Hashtable params ) {
                  // Removing buddyGroup
                  this.accountRoot.getBuddyItems().removeElement( this.buddyGroup );
                  LogUtil.outMessage( "Action Performed" );
                  this.accountRoot.updateOfflineBuddylist();
                }
              };
              LogUtil.outMessage( "QueueAction created" );
              Queue.pushQueueAction( queueAction );
            }
          }
        } catch ( Throwable ex1 ) {
          LogUtil.outMessage( ex1 );
        }
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_SENDFILE", true ) {
      public void actionPerformed() {
        AccountRoot accountRoot = getActiveAccountRoot();
        BuddyItem buddyItem = getSelectedBuddyItem();
        if ( buddyItem != null ) {
          FileBrowserFrame fileBrowserFrame = new FileBrowserFrame( 0, accountRoot, buddyItem.getUserId() );
          fileBrowserFrame.s_prevWindow = MainFrame.this;
          MidletMain.screen.setActiveWindow( fileBrowserFrame );
        }
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_SENDPHOTO", true ) {
      public void actionPerformed() {
        if ( System.getProperty( "supports.video.capture" ).equals( "true" ) ) {
          AccountRoot accountRoot = getActiveAccountRoot();
          BuddyItem buddyItem = getSelectedBuddyItem();
          if ( buddyItem != null ) {
            if ( MidletMain.photoPreparingFrame == null ) {
              MidletMain.photoPreparingFrame = new PhotoPreparingFrame( accountRoot, buddyItem.getUserId() );
            } else {
              MidletMain.photoPreparingFrame.accountRoot = accountRoot;
              MidletMain.photoPreparingFrame.buddyId = buddyItem.getUserId();
              MidletMain.photoPreparingFrame.createPlayer();
            }
            MidletMain.photoPreparingFrame.s_prevWindow = MainFrame.this;
            MidletMain.screen.setActiveWindow( MidletMain.photoPreparingFrame );
          }
        }
      }
    } );

    this.addKeyEvent( new KeyEvent( 0, "KEY_HISTORY", true ) {
      public void actionPerformed() {
        AccountRoot accountRoot = getActiveAccountRoot();
        BuddyItem buddyItem = getSelectedBuddyItem();
        if ( buddyItem != null ) {
          HistoryFrame historyFrame = new HistoryFrame( accountRoot.getAccType(), buddyItem.getUserId() );
          historyFrame.s_prevWindow = MainFrame.this;
          MidletMain.screen.setActiveWindow( historyFrame );
        }
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_RENAME", true ) {
      public void actionPerformed() {
        AccountRoot accountRoot = getActiveAccountRoot();
        BuddyItem buddyItem = getSelectedBuddyItem();
        if ( buddyItem != null ) {
          RenameItemFrame renameItemFrame = new RenameItemFrame( accountRoot, buddyItem );
          renameItemFrame.s_prevWindow = MainFrame.this;
          MidletMain.screen.setActiveWindow( renameItemFrame );
        } else {
          BuddyGroup buddyGroup = getSelectedGroupItem();
          if ( buddyGroup != null ) {
            RenameItemFrame renameItemFrame = new RenameItemFrame( accountRoot, buddyGroup );
            renameItemFrame.s_prevWindow = MainFrame.this;
            MidletMain.screen.setActiveWindow( renameItemFrame );
          }
        }
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_SOUNDS", true ) {
      public void actionPerformed() {
        MidletMain.isSound = !MidletMain.isSound;
        MidletMain.settingsFrame.soundEnabled.setState( MidletMain.isSound );
        try {
          MidletMain.settings.setValue( "Alarm", "soundEnabled", MidletMain.isSound ? "true" : "false" );
          MidletMain.saveRmsData( false, true, false );
        } catch ( GroupNotFoundException ex ) {
        } catch ( IncorrectValueException ex ) {
        }
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_FILTERGROUPS", true ) {
      public void actionPerformed() {
        AccountRoot accountRoot = getActiveAccountRoot();
        if ( accountRoot instanceof IcqAccountRoot ) {
          buddyList.isShowGroups = ( ( ( IcqAccountRoot ) accountRoot ).isShowGroups = !( ( IcqAccountRoot ) accountRoot ).isShowGroups );
          ( ( IcqAccountRoot ) accountRoot ).saveAllSettings();
        } else {
          if ( accountRoot instanceof MmpAccountRoot ) {
            buddyList.isShowGroups = ( ( ( MmpAccountRoot ) accountRoot ).isShowGroups = !( ( MmpAccountRoot ) accountRoot ).isShowGroups );
            ( ( MmpAccountRoot ) accountRoot ).saveAllSettings();
          } else {
            if ( accountRoot instanceof XmppAccountRoot ) {
              buddyList.isShowGroups = ( ( ( XmppAccountRoot ) accountRoot ).isShowGroups = !( ( XmppAccountRoot ) accountRoot ).isShowGroups );
              ( ( XmppAccountRoot ) accountRoot ).saveAllSettings();
            }
          }
        }
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_FILTEROFFLINE", true ) {
      public void actionPerformed() {
        AccountRoot accountRoot = getActiveAccountRoot();
        if ( accountRoot instanceof IcqAccountRoot ) {
          buddyList.maxWeight = ( ( ( IcqAccountRoot ) getActiveAccountRoot() ).isShowOffline = !( ( IcqAccountRoot ) getActiveAccountRoot() ).isShowOffline ) ? 0 : -1;
          ( ( IcqAccountRoot ) getActiveAccountRoot() ).saveAllSettings();
        } else {
          if ( accountRoot instanceof MmpAccountRoot ) {
            buddyList.maxWeight = ( ( ( MmpAccountRoot ) getActiveAccountRoot() ).isShowOffline = !( ( MmpAccountRoot ) getActiveAccountRoot() ).isShowOffline ) ? 0 : -1;
            ( ( MmpAccountRoot ) getActiveAccountRoot() ).saveAllSettings();
          } else {
            if ( accountRoot instanceof XmppAccountRoot ) {
              buddyList.maxWeight = ( ( ( XmppAccountRoot ) getActiveAccountRoot() ).isShowOffline = !( ( XmppAccountRoot ) getActiveAccountRoot() ).isShowOffline ) ? 0 : -1;
              ( ( XmppAccountRoot ) getActiveAccountRoot() ).saveAllSettings();
            }
          }
        }
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_SERVMESSAGES", true ) {
      public void actionPerformed() {
        AccountRoot accountRoot = getActiveAccountRoot();
        if ( accountRoot != null ) {
          MidletMain.serviceMessagesFrame.setItems( accountRoot.getServiceMessages() );
          MidletMain.screen.setActiveWindow( MidletMain.serviceMessagesFrame );
        }
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_MINIMIZE", true ) {
      public void actionPerformed() {
        Display.getDisplay( MidletMain.midletMain ).setCurrent( null );
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_REQUESTAUTH", true ) {
      public void actionPerformed() {
        BuddyItem buddyItem = getSelectedBuddyItem();
        if ( buddyItem != null ) {
          AuthRequestFrame authRequestFrame = new AuthRequestFrame( getActiveAccountRoot(), buddyItem );
          authRequestFrame.s_prevWindow = MainFrame.this;
          MidletMain.screen.setActiveWindow( authRequestFrame );
        }
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_ACCEPTAUTH", true ) {
      public void actionPerformed() {
        BuddyItem buddyItem = getSelectedBuddyItem();
        if ( buddyItem != null ) {
          try {
            getActiveAccountRoot().acceptAuthorization( buddyItem );
          } catch ( IOException ex ) {
            LogUtil.outMessage( ex );
          }
        }
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_DIALOGS", true ) {
      public void actionPerformed() {
        if ( s_nextWindow != null ) {
          MidletMain.screen.setActiveWindow( MidletMain.chatFrame );
          /** Reset unread messages count **/
          ChatTab chatTab = MidletMain.chatFrame.getSelectedChatTab(); // MidletMain.chatFrame.getChatTab((IcqAccountRoot) getActiveAccountRoot(), buddyItem.getUserId(), true);
          if ( chatTab != null ) {
            String resource = null;
            if ( chatTab.resource != null ) {
              resource = chatTab.resource.resource;
            }
            AccountRoot accountRoot = getActiveAccountRoot();
            if ( accountRoot != null ) {
              accountRoot.setUnrMsgs( accountRoot.getUnrMsgs() - chatTab.buddyItem.getUnreadCount( resource ) );
              MidletMain.mainFrame.updateAccountsStatus();
            }
            chatTab.buddyItem.setUnreadCount( 0, resource );
          }
          MidletMain.chatFrame.chatTabs.tabEvent.stateChanged( MidletMain.chatFrame.chatTabs.selectedIndex, MidletMain.chatFrame.chatTabs.selectedIndex, MidletMain.chatFrame.chatTabs.items.size() - 1 );
          MidletMain.chatFrame.prepareGraphics();
        } else {
          ActionExec.showNotify( Localization.getMessage( "NO_DIALOGS_OPEN" ) );
        }
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_BUDDYLIST_TOP", true ) {
      public void actionPerformed() {
        buddyList.yOffset = 0;
        buddyList.selectedColumn = 0;
        buddyList.selectedRow = 0;
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_BUDDYLIST_BOTTOM", true ) {
      public void actionPerformed() {
        int maxOffset = buddyList.totalItemsCount * buddyList.itemHeight - buddyList.height;
        if ( maxOffset < 0 ) {
          maxOffset = 0;
        }
        buddyList.yOffset = maxOffset;
        buddyList.selectedColumn = 0;
        buddyList.selectedRow = buddyList.totalItemsCount - 1;
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_BUDDYLIST_SCREEN_HIGHER", true ) {
      public void actionPerformed() {
        int maxOffset = buddyList.yOffset - buddyList.height;
        int selRow = buddyList.selectedRow - buddyList.height / buddyList.itemHeight - 1;
        if ( maxOffset < 0 ) {
          maxOffset = 0;
        }
        if ( selRow < 0 ) {
          selRow = 0;
        }
        buddyList.yOffset = maxOffset;
        buddyList.selectedColumn = 0;
        buddyList.selectedRow = selRow;
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_BUDDYLIST_SCREEN_LOWER", true ) {
      public void actionPerformed() {
        int selOffset = buddyList.yOffset + buddyList.height;
        int maxOffset = buddyList.totalItemsCount * buddyList.itemHeight - buddyList.height;
        int selRow = buddyList.selectedRow + buddyList.height / buddyList.itemHeight + 1;
        if ( maxOffset < 0 ) {
          // maxOffset = 0;
          selOffset = 0;
          selRow = buddyList.totalItemsCount - 1;
        } else {
          if ( selOffset > maxOffset ) {
            selOffset = maxOffset;
          }
          if ( selRow >= buddyList.totalItemsCount ) {
            selRow = buddyList.totalItemsCount - 1;
          }
        }
        buddyList.yOffset = selOffset;
        buddyList.selectedColumn = 0;
        buddyList.selectedRow = selRow;
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_LOCKSCREEN", true ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( new LockFrame() );
      }
    } );
    /** Soft **/
    soft = new Soft( screen );
    /** Tabs **/
    accountTabs = new Tab( screen );
    accountTabs.tabEvent = new TabEvent() {
      public void stateChanged( int previousIndex, int selectedIndex, int tabsCount ) {
        AccountRoot accountRoot = checkAccountRoot( previousIndex );
        if ( accountRoot != null ) {
          accountRoot.setYOffset( buddyList.yOffset );
          accountRoot.setSelectedIndex( buddyList.selectedColumn, buddyList.selectedRow );
        }
        accountRoot = checkAccountRoot( selectedIndex );
        if ( accountRoot != null ) {
          switchAccountRoot( accountRoot );
        }
      }
    };
    /** Applying GObject **/
    setGObject( accountTabs );
    /** Static popup **/
    accountPopupItem = new PopupItem( Localization.getMessage( "ACCOUNT" ), IconsType.HASH_MAIN, 7 );
    accountPopupItem.addSubItem( new PopupItem( Localization.getMessage( "CREATE" ), IconsType.HASH_MAIN, 8 ) {
      public void actionPerformed() {
        AccountEditorFrame accountEditorFrame = new AccountEditorFrame( null, null, null, null, null, true, null, false );
        MidletMain.screen.setActiveWindow( accountEditorFrame );
      }
    } );
    accountPopupItem.addSubItem( new PopupItem( Localization.getMessage( "EDIT" ), IconsType.HASH_MAIN, 9 ) {
      public void actionPerformed() {
        AccountRoot tempIcqAccountRoot = ( AccountRoot ) getActiveAccountRoot();
        AccountEditorFrame accountEditorFrame = new AccountEditorFrame(
                tempIcqAccountRoot.getUserId(),
                tempIcqAccountRoot.getUserNick(),
                tempIcqAccountRoot.getUserPassword(),
                tempIcqAccountRoot.getHost(), tempIcqAccountRoot.getPort(), tempIcqAccountRoot.getUseSsl(), tempIcqAccountRoot.getAccType(), true );
        MidletMain.screen.setActiveWindow( accountEditorFrame );
      }
    } );
    accountPopupItem.addSubItem( new PopupItem( Localization.getMessage( "REMOVE" ), IconsType.HASH_MAIN, 10 ) {
      public void actionPerformed() {
        AccountRoot tempIcqAccountRoot = ( AccountRoot ) getActiveAccountRoot();
        for ( int c = 0; c < accountTabs.items.size(); c++ ) {
          if ( ( ( AccountTab ) accountTabs.items.elementAt( c ) ).accountRoot.equals( tempIcqAccountRoot ) ) {
            accountTabs.items.removeElementAt( c );
            MidletMain.accounts.removeGroup( tempIcqAccountRoot.getUserId() );
            MidletMain.saveRmsData( true, false, false );
            switchAccountRoot( getActiveAccountRoot() );
            return;
          }
        }
      }
    } );
    /** Settings **/
    settingsPopupItem = new PopupItem( Localization.getMessage( "SETTINGS" ), IconsType.HASH_MAIN, 12 ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( MidletMain.settingsFrame );
      }
    };
    /** Service messages **/
    servicePopupItem = new PopupItem( Localization.getMessage( "SERVICE_MESSAGES" ), IconsType.HASH_MAIN, 11 ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_SERVMESSAGES" ).actionPerformed();
      }
    };
    /** Services popup **/
    servicesPopupItem = new PopupItem( Localization.getMessage( "SERVICES" ), IconsType.HASH_MAIN, 13 );
    servicesPopupItem.addSubItem( new PopupItem( Localization.getMessage( "UPDATE_CHECK" ), IconsType.HASH_MAIN, 14 ) {
      public void actionPerformed() {
        UpdateCheckFrame updateCheckFrame = new UpdateCheckFrame( false );
        updateCheckFrame.s_prevWindow = MainFrame.this;
        MidletMain.screen.setActiveWindow( updateCheckFrame );
      }
    } );
    servicesPopupItem.addSubItem( new PopupItem( Localization.getMessage( "SEND_OPINION" ), IconsType.HASH_MAIN, 15 ) {
      public void actionPerformed() {
        OpinionSendFrame opinionSendFrame = new OpinionSendFrame();
        opinionSendFrame.s_prevWindow = MainFrame.this;
        MidletMain.screen.setActiveWindow( opinionSendFrame );
      }
    } );
    servicesPopupItem.addSubItem( new PopupItem( Localization.getMessage( "CONNECTION_TEST" ), IconsType.HASH_MAIN, 16 ) {
      public void actionPerformed() {
        NetTestFrame netTestFrame = new NetTestFrame();
        netTestFrame.s_prevWindow = MainFrame.this;
        MidletMain.screen.setActiveWindow( netTestFrame );
      }
    } );
    donatePopupItem = new PopupItem( Localization.getMessage( "DONATE" ), IconsType.HASH_MAIN, 17 ) {
      public void actionPerformed() {
        DonateFrame donateFrame = new DonateFrame();
        donateFrame.s_prevWindow = MainFrame.this;
        MidletMain.screen.setActiveWindow( donateFrame );
      }
    };
    /** Info popup **/
    infoPopupItem = new PopupItem( Localization.getMessage( "INFO" ), IconsType.HASH_MAIN, 18 );
    infoPopupItem.addSubItem( new PopupItem( Localization.getMessage( "ABOUT" ), IconsType.HASH_MAIN, 19 ) {
      public void actionPerformed() {
        AboutFrame aboutFrame = new AboutFrame();
        aboutFrame.s_prevWindow = MainFrame.this;
        MidletMain.screen.setActiveWindow( aboutFrame );
      }
    } );
    infoPopupItem.addSubItem( new PopupItem( Localization.getMessage( "TRAFFIC" ), IconsType.HASH_MAIN, 20 ) {
      public void actionPerformed() {
        TrafficInfoFrame trafficInfoFrame = new TrafficInfoFrame();
        trafficInfoFrame.s_prevWindow = MainFrame.this;
        MidletMain.screen.setActiveWindow( trafficInfoFrame );
      }
    } );
    infoPopupItem.addSubItem( donatePopupItem );
    /** Minimize & Exit popups **/
    lockPopupItem = new PopupItem( Localization.getMessage( "LOCK_SCREEN" ), IconsType.HASH_MAIN, 28 ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_LOCKSCREEN" ).actionPerformed();
      }
    };
    minimizePopupItem = new PopupItem( Localization.getMessage( "MINIMIZE" ), IconsType.HASH_MAIN, 21 ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_MINIMIZE" ).actionPerformed();
      }
    };
    exitPopupItem = new PopupItem( Localization.getMessage( "EXIT" ), IconsType.HASH_MAIN, 22 ) {
      public void actionPerformed() {
        MidletMain.midletMain.notifyDestroyed();
      }
    };
    /** List **/
    buddyList = new Group();
    buddyList.columnCount = 1;
    buddyList.setTouchOrientation( screen.isPointerEvents );
    buddyList.actionPerformedEvent = new GroupEvent() {
      public void actionPerformed( GroupChild buddyItem ) {
        MainFrame.this.getKeyEvent( "KEY_DIALOG" ).actionPerformed();
      }
    };
    accountTabs.gObject = buddyList;

    /** Initialize accounts list **/
    updateAccountsList();
    /** Load offline buddy list **/
    if ( !accountTabs.items.isEmpty() ) {
      checkAccountRoot( 0 );
      switchAccountRoot( ( ( AccountTab ) accountTabs.items.firstElement() ).accountRoot );
    } else {
      if ( pane == null ) {
        initPane();
      }
      setGObject( pane );
      initEmptySoft();
    }
  }

  public void updateActiveAccountRoot() {
    if ( accountTabs.items.isEmpty() ) {
      return;
    }
    AccountRoot accountRoot = getActiveAccountRoot();
    if ( accountRoot != null ) {
      buddyList.columnCount = MidletMain.columnCount;
      accountRoot.updateMainFrameBuddyList();
      prepareGraphics();
      MidletMain.screen.repaint();
    }
  }

  public final void switchAccountRoot( AccountRoot accountRoot ) {
    MidletMain.screen.setWaitScreenState( true );
    if ( accountRoot == null ) {
      /** Showing pane **/
      if ( getGObject().equals( accountTabs ) ) {
        if ( pane == null ) {
          initPane();
        }
        setGObject( pane );
        initEmptySoft();
      }
    } else {
      accountTabs.validateSelection();
      if ( !getGObject().equals( accountTabs ) ) {
        setGObject( accountTabs );
      }
      if ( accountRoot.getBuddyItems() == null ) {
        loadOfflineBuddyList( accountRoot );
      }
      /** Applying Group yOffset **/
      /** Switching soft items **/
      if ( accountRoot instanceof IcqAccountRoot ) {
        /** Showing buddy items **/
        LogUtil.outMessage( "Installing images" );
        buddyList.imageLeftFileHash = new int[] { "/res/groups/img_chat.png".hashCode(), "/res/groups/img_icqstatus.png".hashCode(), "/res/groups/img_xstatus.png".hashCode() };
        buddyList.imageRightFileHash = new int[] { IconsType.HASH_PLIST, IconsType.HASH_PLIST, IconsType.HASH_PLIST, IconsType.HASH_CLIENTS, IconsType.HASH_MAIN };
        LogUtil.outMessage( "Preparing items" );
        buddyList.items = ( ( IcqAccountRoot ) accountRoot ).buddyItems;
        /** Loading ICQ account data **/
        LogUtil.outMessage( "Checkong caps" );
        if ( CapUtil.dataCaps == null ) {
          LogUtil.outMessage( "--- Loading caps..." );
          CapUtil.loadCaps();
        }
        /** Loading ICQ images **/
        LogUtil.outMessage( "Checking and loading images" );
        if ( Splitter.getImageGroup( "/res/groups/img_xstatus.png".hashCode() ) == null ) {
          LogUtil.outMessage( "Loading 1" );
          Splitter.splitImage( "/res/groups/img_xstatus.png" );
        }
        if ( Splitter.getImageGroup( "/res/groups/img_pstatus.png".hashCode() ) == null ) {
          LogUtil.outMessage( "Loading 2" );
          Splitter.splitImage( "/res/groups/img_pstatus.png" );
        }
        if ( Splitter.getImageGroup( "/res/groups/img_clients.png".hashCode() ) == null ) {
          LogUtil.outMessage( "Loading 3" );
          Splitter.splitImage( "/res/groups/img_clients.png" );
        }
        if ( icqSoft == null ) {
          LogUtil.outMessage( "Init icqSoft" );
          initIcqSoft();
        }
        soft = icqSoft;
        /** Runtime settings **/
        LogUtil.outMessage( "Runtime settings" );
        buddyList.yOffset = ( ( IcqAccountRoot ) accountRoot ).yOffset;
        buddyList.selectedColumn = ( ( IcqAccountRoot ) accountRoot ).selectedColumn;
        buddyList.selectedRow = ( ( IcqAccountRoot ) accountRoot ).selectedRow;
        LogUtil.outMessage( "Complete." );
      } else if ( accountRoot instanceof MmpAccountRoot ) {
        buddyList.imageLeftFileHash = new int[] { "/res/groups/img_chat.png".hashCode(), "/res/groups/img_mmpstatus.png".hashCode() };
        buddyList.items = ( ( MmpAccountRoot ) accountRoot ).buddyItems;
        if ( mmpSoft == null ) {
          initMmpSoft();
        }
        soft = mmpSoft;
        /** Runtime settings **/
        buddyList.yOffset = ( ( MmpAccountRoot ) accountRoot ).yOffset;
        buddyList.selectedColumn = ( ( MmpAccountRoot ) accountRoot ).selectedColumn;
        buddyList.selectedRow = ( ( MmpAccountRoot ) accountRoot ).selectedRow;
      } else if ( accountRoot instanceof XmppAccountRoot ) {
        buddyList.imageLeftFileHash = new int[] { "/res/groups/img_chat.png".hashCode(), "/res/groups/img_xmppstatus.png".hashCode() };
        buddyList.items = ( ( XmppAccountRoot ) accountRoot ).buddyItems;
        if ( xmppSoft == null ) {
          initXmppSoft();
        }
        soft = xmppSoft;
        /** Runtime settings **/
        buddyList.yOffset = ( ( XmppAccountRoot ) accountRoot ).yOffset;
        buddyList.selectedColumn = ( ( XmppAccountRoot ) accountRoot ).selectedColumn;
        buddyList.selectedRow = ( ( XmppAccountRoot ) accountRoot ).selectedRow;
      }
      buddyList.maxWeight = ( accountRoot.getShowOffline() ? 0 : -1 );
      buddyList.isShowGroups = accountRoot.getShowGroups();
      buddyList.columnCount = MidletMain.columnCount;
    }
    MidletMain.screen.setWaitScreenState( false );
  }

  public final void loadOfflineBuddyList( AccountRoot accountRoot ) {
    BinGear dataGear = null;
    try {
      RecordUtil.readFile( "/icq/".concat(
              String.valueOf( accountRoot.getUserId().hashCode() ) )
              .concat( "/buddylist.dat" ), dataGear );
      Vector buddyItems = new Vector();
      /** Loading buddyItems from dataGear **/
      accountRoot.setBuddyItems( buddyItems );
    } catch ( Throwable ex ) {
      accountRoot.setBuddyItems( new Vector() );
    }
    accountRoot.updateMainFrameBuddyList();
  }

  public final void updateAccountsList() {
    AccountTab tempTabItem;
    final String[] accounts = MidletMain.accounts.listGroups();
    for ( int c = 0; c < accounts.length; c++ ) {
      try {
        final String accountUserId = accounts[c];
        tempTabItem = new AccountTab( accountUserId,
                MidletMain.accounts.getValue( accounts[c], "nick" ),
                "/res/groups/img_".concat( MidletMain.accounts.getValue( accounts[c], "type" ) ).concat( "status.png" ).hashCode(), 0 );
        accountTabs.addTabItem( tempTabItem );
      } catch ( Throwable ex ) {
      }
    }
  }

  public final void updateAccountsStatus() {
    for ( int c = 0; c < accountTabs.items.size(); c++ ) {
      try {
        ( ( AccountTab ) accountTabs.items.elementAt( c ) ).updateAccountStatus();
      } catch ( Throwable ex ) {
      }
    }
  }

  public final void initIcqSoft() {
    /** ICQ Soft **/
    icqSoft = new Soft( Screen.screen );

    final PopupItem statusItem = new PopupItem( Localization.getMessage( "MSTATUS" ) );
    final PopupItem xStatusItem = new PopupItem( Localization.getMessage( "XSTATUS" ) );
    final PopupItem pStatusItem = new PopupItem( Localization.getMessage( "PSTATUS" ) );

    final PopupItem groupsPopup = new PopupItem( Localization.getMessage( "GROUPS" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_FILTERGROUPS" ).actionPerformed();
      }
    };
    final PopupItem offlinePopup = new PopupItem( Localization.getMessage( "OFFLINE_BUDDYES" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_FILTEROFFLINE" ).actionPerformed();
      }
    };

    icqSoft.leftSoft = new PopupItem( Localization.getMessage( "MENU" ) ) {
      public void actionPerformed() {
        IcqAccountRoot icqAccountRoot = ( ( IcqAccountRoot ) getActiveAccountRoot() );
        /** Checking statusIndex icons **/
        statusItem.imageFileHash = "/res/groups/img_icqstatus.png".hashCode();
        statusItem.imageIndex = icqAccountRoot.getStatusIndex();
        xStatusItem.imageFileHash = "/res/groups/img_xstatus.png".hashCode();
        if ( icqAccountRoot.xStatusId != -1 ) {
          xStatusItem.imageIndex = icqAccountRoot.xStatusId;
        } else {
          xStatusItem.imageIndex = 36;
        }
        pStatusItem.imageFileHash = "/res/groups/img_pstatus.png".hashCode();
        pStatusItem.imageIndex = icqAccountRoot.pStatusId - 1;
        /** Filters **/
        groupsPopup.title = ( icqAccountRoot.isShowGroups ? Localization.getMessage( "HIDE" ) : Localization.getMessage( "SHOW" ) ).concat( " " ).concat( Localization.getMessage( "GROUPS" ) );
        offlinePopup.title = ( icqAccountRoot.isShowOffline ? Localization.getMessage( "HIDE" ) : Localization.getMessage( "SHOW" ) ).concat( " " ).concat( Localization.getMessage( "OFFLINE_BUDDYES" ) );
        /** Addings dialog popup items **/
      }
    };
    icqSoft.leftSoft.addSubItem( statusItem );
    PopupItem tempPopupItem;
    for ( int c = 0; c < IcqStatusUtil.getStatusCount(); c++ ) {
      final int statusId = IcqStatusUtil.getStatus( c );
      final int statusIndex = c;
      tempPopupItem = new PopupItem( Localization.getMessage( IcqStatusUtil.getStatusDescr( c ) ) ) {
        public void actionPerformed() {
          final IcqAccountRoot icqAccountRoot = ( IcqAccountRoot ) ( ( AccountTab ) accountTabs.items.elementAt( accountTabs.selectedIndex ) ).accountRoot;
          /** Status is selected **/
          if ( icqAccountRoot.statusIndex == 0 && statusIndex != -1 ) {
            /** Need to connect **/
            icqAccountRoot.loadStatus( statusIndex );
            icqAccountRoot.connectAction( statusIndex );
          } else {
            if ( icqAccountRoot.statusIndex != 0 && statusIndex == 0 ) {
              /** Need go offline **/
              ActionExec.disconnectEvent( icqAccountRoot );
              icqAccountRoot.session.disconnect();
            } else if ( icqAccountRoot.statusIndex != 0 ) {
              try {
                /** Plain statusIndex changing **/
                icqAccountRoot.statusIndex = statusIndex;
                icqAccountRoot.loadStatus( statusIndex );
                IcqPacketSender.setStatus( icqAccountRoot.session, ( statusId < 0x1000 ) ? statusId : 0x0000 );
                IcqPacketSender.sendCapabilities( icqAccountRoot.session, icqAccountRoot.xStatusId, statusId );
                updateAccountsStatus();
              } catch ( IOException ex ) {
                LogUtil.outMessage( "Can't set status", true );
              }
              SetStatusTextFrame setStatusTextFrame = new SetStatusTextFrame( icqAccountRoot, statusIndex );
              setStatusTextFrame.s_prevWindow = MainFrame.this;
              MidletMain.screen.setActiveWindow( setStatusTextFrame );
            }
          }
        }
      };
      tempPopupItem.imageFileHash = "/res/groups/img_icqstatus.png".hashCode();
      tempPopupItem.imageIndex = c;
      statusItem.addSubItem( tempPopupItem );
    }

    icqSoft.leftSoft.addSubItem( xStatusItem );
    /** X-Status None **/
    tempPopupItem = new PopupItem( Localization.getMessage( "xstatus36" ) ) {
      public void actionPerformed() {
        final IcqAccountRoot icqAccountRoot = ( IcqAccountRoot ) ( ( AccountTab ) accountTabs.items.elementAt( accountTabs.selectedIndex ) ).accountRoot;
        icqAccountRoot.xStatusId = -1;
        icqAccountRoot.xTitle = "";
        icqAccountRoot.xText = "";
        icqAccountRoot.isXStatusReadable = false;
        icqAccountRoot.saveAllSettings();
        if ( icqAccountRoot.statusIndex != 0 ) {
          try {
            IcqPacketSender.sendCapabilities( icqAccountRoot.session, icqAccountRoot.xStatusId, IcqStatusUtil.getStatus( icqAccountRoot.statusIndex ) );
            //! MidletMain.saveStatusSettings(accountRoot, accountRoot.xStatusId, accountRoot.pStatusId, accountRoot.privateBuddyId);
          } catch ( IOException ex ) {
            LogUtil.outMessage( "Can't set xstatus", true );
          }
        }
      }
    };
    tempPopupItem.imageFileHash = "/res/groups/img_xstatus.png".hashCode();
    tempPopupItem.imageIndex = 36;
    xStatusItem.addSubItem( tempPopupItem );
    /** X-Status from caps.dat file **/
    String[] capGroups = CapUtil.dataCaps.listGroups();
    String tempString;
    for ( int c = 0; c < capGroups.length; c++ ) {
      try {
        tempString = CapUtil.dataCaps.getValue( capGroups[c], "type" );
        if ( tempString != null && Integer.parseInt( tempString ) == Capability.CAP_XSTATUS ) {
          final int iconIndex = Integer.parseInt( CapUtil.dataCaps.getValue( capGroups[c], "icon" ).substring( 7 ) );
          tempPopupItem = new PopupItem( Localization.getMessage( CapUtil.dataCaps.getValue( capGroups[c], "icon" ) ) ) {
            public void actionPerformed() {
              final IcqAccountRoot icqAccountRoot = ( IcqAccountRoot ) ( ( AccountTab ) accountTabs.items.elementAt( accountTabs.selectedIndex ) ).accountRoot;
              icqAccountRoot.xStatusId = iconIndex;
              icqAccountRoot.xTitle = "";
              icqAccountRoot.xText = "";
              icqAccountRoot.isXStatusReadable = false;
              icqAccountRoot.saveAllSettings();
              if ( icqAccountRoot.statusIndex != 0 ) {
                try {
                  IcqPacketSender.sendCapabilities( icqAccountRoot.session,
                          icqAccountRoot.xStatusId,
                          IcqStatusUtil.getStatus( icqAccountRoot.statusIndex ) );
                } catch ( IOException ex ) {
                  LogUtil.outMessage( "Can't set xstatus", true );
                }
              }
              SetExtStatusFrame setExtStatusFrame = new SetExtStatusFrame( icqAccountRoot, icqAccountRoot.xStatusId );
              setExtStatusFrame.s_prevWindow = MainFrame.this;
              MidletMain.screen.setActiveWindow( setExtStatusFrame );
            }
          };
          tempPopupItem.imageFileHash = "/res/groups/img_xstatus.png".hashCode();
          tempPopupItem.imageIndex = iconIndex;
          xStatusItem.addSubItem( tempPopupItem );
        }
      } catch ( GroupNotFoundException ex ) {
      } catch ( IncorrectValueException ex ) {
      }
    }

    icqSoft.leftSoft.addSubItem( pStatusItem );
    String popupTitle = "";
    for ( int c = 1; c <= 5; c++ ) {
      final int pStatusIndex = c;
      switch ( pStatusIndex ) {
        case 1: {
          popupTitle = Localization.getMessage( "PSTATUS_ALLOW_ALL" );
          break;
        }
        case 2: {
          popupTitle = Localization.getMessage( "PSTATUS_BLOCK_ALL" );
          break;
        }
        case 3: {
          popupTitle = Localization.getMessage( "PSTATUS_PERMIT_LIST_ONLY" );
          break;
        }
        case 4: {
          popupTitle = Localization.getMessage( "PSTATUS_BLOCK_DENY_LIST" );
          break;
        }
        case 5: {
          popupTitle = Localization.getMessage( "PSTATUS_ALLOW_BUDDY_LIST" );
          break;
        }
      }
      PopupItem _privateStatusItem = new PopupItem( popupTitle ) {
        public void actionPerformed() {
          ( ( IcqAccountRoot ) ( ( AccountTab ) accountTabs.items.elementAt( accountTabs.selectedIndex ) ).accountRoot ).setUpdatePrivacy( pStatusIndex );
        }
      };
      _privateStatusItem.imageFileHash = "/res/groups/img_pstatus.png".hashCode();
      _privateStatusItem.imageIndex = pStatusIndex - 1;
      pStatusItem.addSubItem( _privateStatusItem );
    }

    PopupItem filterPopup = new PopupItem( Localization.getMessage( "FILTER" ), IconsType.HASH_MAIN, 0 );
    filterPopup.addSubItem( groupsPopup );
    filterPopup.addSubItem( offlinePopup );

    icqSoft.leftSoft.addSubItem( filterPopup );

    icqSoft.leftSoft.addSubItem( new PopupItem( Localization.getMessage( "DIALOGS" ), IconsType.HASH_MAIN, 1 ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_DIALOGS" ).actionPerformed();
      }
    } );
    icqSoft.leftSoft.addSubItem( new PopupItem( Localization.getMessage( "FILETRANFSER" ), IconsType.HASH_MAIN, 2 ) {
      public void actionPerformed() {
        AccountRoot accountRoot = getActiveAccountRoot();
        accountRoot.getTransactionsFrame().s_prevWindow = MainFrame.this;
        MidletMain.screen.setActiveWindow( accountRoot.getTransactionsFrame() );
      }
    } );
    PopupItem buddyListItem = ( new PopupItem( Localization.getMessage( "BUDDYLIST" ), IconsType.HASH_MAIN, 3 ) );
    buddyListItem.addSubItem( new PopupItem( Localization.getMessage( "ADD_GROUP" ), IconsType.HASH_MAIN, 4 ) {
      public void actionPerformed() {
        AddingGroupFrame addingGroupFrame = new AddingGroupFrame( ( IcqAccountRoot ) getActiveAccountRoot() );
        addingGroupFrame.s_prevWindow = MainFrame.this;
        MidletMain.screen.setActiveWindow( addingGroupFrame );
      }
    } );
    buddyListItem.addSubItem( new PopupItem( Localization.getMessage( "ADD_BUDDY" ), IconsType.HASH_MAIN, 5 ) {
      public void actionPerformed() {
        AddingBuddyFrame addingBuddyFrame = new AddingBuddyFrame( ( IcqAccountRoot ) getActiveAccountRoot(), 0 );
        addingBuddyFrame.s_prevWindow = MainFrame.this;
        MidletMain.screen.setActiveWindow( addingBuddyFrame );
      }
    } );
    /*buddyListItem.addSubItem(new PopupItem(Localization.getMessage("SEARCH_BUDDY"), IconsType.HASH_MAIN, 6) {
        
     public void actionPerformed() {
     }
     });*/
    icqSoft.leftSoft.addSubItem( buddyListItem );
    icqSoft.leftSoft.addSubItem( accountPopupItem );

    icqSoft.leftSoft.addSubItem( settingsPopupItem );
    icqSoft.leftSoft.addSubItem( servicePopupItem );
    icqSoft.leftSoft.addSubItem( servicesPopupItem );
    icqSoft.leftSoft.addSubItem( infoPopupItem );
    icqSoft.leftSoft.addSubItem( lockPopupItem );
    icqSoft.leftSoft.addSubItem( minimizePopupItem );
    icqSoft.leftSoft.addSubItem( exitPopupItem );

    PopupItem privacyPopupItem = new PopupItem( Localization.getMessage( "PRIVACY" ) );
    final PopupItem visiblePopupItem = new PopupItem( Localization.getMessage( "PRI_VISIBLE_ADD" ) ) {
      public void actionPerformed() {
        BuddyItem buddyItem = getSelectedBuddyItem();
        if ( buddyItem != null ) {
          try {
            if ( ( ( IcqItem ) buddyItem ).isInPermitList ) {
              LogUtil.outMessage( buddyItem.getUserId() + " is in visible list: permitBuddyId = " + ( ( IcqItem ) buddyItem ).permitBuddyId );
              Cookie cookie = IcqPacketSender.deletePrivacy( ( ( IcqAccountRoot ) getActiveAccountRoot() ).session, ( ( IcqItem ) buddyItem ).userId, ( ( IcqItem ) buddyItem ).groupId, ( ( IcqItem ) buddyItem ).permitBuddyId, 0x0002 );
              QueueAction queueAction = new QueueAction( getActiveAccountRoot(), buddyItem, cookie ) {
                public void actionPerformed( Hashtable params ) {
                  // Removing buddyItem
                  LogUtil.outMessage( "Action Performed" );
                  ( ( IcqItem ) this.buddyItem ).isInPermitList = false;
                  this.buddyItem.updateUiData();
                  this.accountRoot.updateOfflineBuddylist();
                }
              };
              LogUtil.outMessage( "QueueAction created" );
              Queue.pushQueueAction( queueAction );
            } else {
              LogUtil.outMessage( buddyItem.getUserId() + " is not yet in visible list" );
              final int permitBuddyId = ( int ) ( ( IcqAccountRoot ) getActiveAccountRoot() ).getNextBuddyId();
              Cookie cookie = IcqPacketSender.addPrivacy( ( ( IcqAccountRoot ) getActiveAccountRoot() ).session, ( ( IcqItem ) buddyItem ).userId, ( ( IcqItem ) buddyItem ).groupId, permitBuddyId, 0x0002 );
              QueueAction queueAction = new QueueAction( getActiveAccountRoot(), buddyItem, cookie ) {
                public void actionPerformed( Hashtable params ) {
                  // Removing buddyItem
                  LogUtil.outMessage( "Action Performed" );
                  ( ( IcqItem ) this.buddyItem ).isInPermitList = true;
                  ( ( IcqItem ) this.buddyItem ).permitBuddyId = permitBuddyId;
                  this.buddyItem.updateUiData();
                  this.accountRoot.updateOfflineBuddylist();
                }
              };
              LogUtil.outMessage( "QueueAction created" );
              Queue.pushQueueAction( queueAction );
            }
          } catch ( IOException ex ) {
            LogUtil.outMessage( "Can not change private status" );
          }
        }
      }
    };
    final PopupItem invisiblePopupItem = new PopupItem( Localization.getMessage( "PRI_INVISIBLE_ADD" ) ) {
      public void actionPerformed() {
        BuddyItem buddyItem = getSelectedBuddyItem();
        if ( buddyItem != null ) {
          try {
            if ( ( ( IcqItem ) buddyItem ).isInDenyList ) {
              Cookie cookie = IcqPacketSender.deletePrivacy( ( ( IcqAccountRoot ) getActiveAccountRoot() ).session, ( ( IcqItem ) buddyItem ).userId, ( ( IcqItem ) buddyItem ).groupId, ( ( IcqItem ) buddyItem ).denyBuddyId, 0x0003 );
              QueueAction queueAction = new QueueAction( getActiveAccountRoot(), buddyItem, cookie ) {
                public void actionPerformed( Hashtable params ) {
                  // Removing buddyItem
                  LogUtil.outMessage( "Action Performed" );
                  ( ( IcqItem ) this.buddyItem ).isInDenyList = false;
                  this.buddyItem.updateUiData();
                  this.accountRoot.updateOfflineBuddylist();
                }
              };
              LogUtil.outMessage( "QueueAction created" );
              Queue.pushQueueAction( queueAction );
            } else {
              final int denyBuddyId = ( int ) ( ( IcqAccountRoot ) getActiveAccountRoot() ).getNextBuddyId();
              Cookie cookie = IcqPacketSender.addPrivacy( ( ( IcqAccountRoot ) getActiveAccountRoot() ).session, ( ( IcqItem ) buddyItem ).userId, ( ( IcqItem ) buddyItem ).groupId, denyBuddyId, 0x0003 );
              QueueAction queueAction = new QueueAction( getActiveAccountRoot(), buddyItem, cookie ) {
                public void actionPerformed( Hashtable params ) {
                  // Removing buddyItem
                  LogUtil.outMessage( "Action Performed" );
                  ( ( IcqItem ) this.buddyItem ).isInDenyList = true;
                  ( ( IcqItem ) this.buddyItem ).denyBuddyId = denyBuddyId;
                  this.buddyItem.updateUiData();
                  this.accountRoot.updateOfflineBuddylist();
                }
              };
              LogUtil.outMessage( "QueueAction created" );
              Queue.pushQueueAction( queueAction );
            }
          } catch ( IOException ex ) {
            LogUtil.outMessage( "Can not change private status" );
          }
        }
      }
    };
    final PopupItem ignorePopupItem = new PopupItem( Localization.getMessage( "PRI_IGNORE_ADD" ) ) {
      public void actionPerformed() {
        BuddyItem buddyItem = getSelectedBuddyItem();
        if ( buddyItem != null ) {
          try {
            if ( ( ( IcqItem ) buddyItem ).isInIgnoreList ) {
              Cookie cookie = IcqPacketSender.deletePrivacy( ( ( IcqAccountRoot ) getActiveAccountRoot() ).session, ( ( IcqItem ) buddyItem ).userId, ( ( IcqItem ) buddyItem ).groupId, ( ( IcqItem ) buddyItem ).ignoreBuddyId, 0x000e );
              QueueAction queueAction = new QueueAction( getActiveAccountRoot(), buddyItem, cookie ) {
                public void actionPerformed( Hashtable params ) {
                  // Removing buddyItem
                  LogUtil.outMessage( "Action Performed" );
                  ( ( IcqItem ) this.buddyItem ).isInIgnoreList = false;
                  this.buddyItem.updateUiData();
                  this.accountRoot.updateOfflineBuddylist();
                }
              };
              LogUtil.outMessage( "QueueAction created" );
              Queue.pushQueueAction( queueAction );
            } else {
              final int ignoreBuddyId = ( int ) ( ( IcqAccountRoot ) getActiveAccountRoot() ).getNextBuddyId();
              Cookie cookie = IcqPacketSender.addPrivacy( ( ( IcqAccountRoot ) getActiveAccountRoot() ).session, ( ( IcqItem ) buddyItem ).userId, ( ( IcqItem ) buddyItem ).groupId, ignoreBuddyId, 0x000e );
              QueueAction queueAction = new QueueAction( getActiveAccountRoot(), buddyItem, cookie ) {
                public void actionPerformed( Hashtable params ) {
                  // Removing buddyItem
                  LogUtil.outMessage( "Action Performed" );
                  ( ( IcqItem ) this.buddyItem ).isInIgnoreList = true;
                  ( ( IcqItem ) this.buddyItem ).ignoreBuddyId = ignoreBuddyId;
                  this.buddyItem.updateUiData();
                  this.accountRoot.updateOfflineBuddylist();
                }
              };
              LogUtil.outMessage( "QueueAction created" );
              Queue.pushQueueAction( queueAction );
            }
          } catch ( IOException ex ) {
            LogUtil.outMessage( "Can not change private status" );
          }
        }
      }
    };

    Thread rightAction = new Thread() {
      public void run() {
        BuddyItem buddyItem = getSelectedBuddyItem();
        if ( buddyItem != null ) {
          icqSoft.rightSoft = icqBuddyRightPopupItem;
          if ( ( ( IcqItem ) buddyItem ).isInPermitList ) {
            visiblePopupItem.title = Localization.getMessage( "PRI_VISIBLE_DELETE" );
          } else {
            visiblePopupItem.title = Localization.getMessage( "PRI_VISIBLE_ADD" );
          }
          if ( ( ( IcqItem ) buddyItem ).isInDenyList ) {
            invisiblePopupItem.title = Localization.getMessage( "PRI_INVISIBLE_DELETE" );
          } else {
            invisiblePopupItem.title = Localization.getMessage( "PRI_INVISIBLE_ADD" );
          }
          if ( ( ( IcqItem ) buddyItem ).isInIgnoreList ) {
            ignorePopupItem.title = Localization.getMessage( "PRI_IGNORE_DELETE" );
          } else {
            ignorePopupItem.title = Localization.getMessage( "PRI_IGNORE_ADD" );
          }
        } else {
          icqSoft.rightSoft = icqGroupRightPopupItem;
        }
      }
    };

    icqBuddyRightPopupItem = new PopupItem( Localization.getMessage( "BUDDY" ) );
    icqGroupRightPopupItem = new PopupItem( Localization.getMessage( "BUDDY" ) );

    icqBuddyRightPopupItem.setActionPerformed( rightAction );
    icqGroupRightPopupItem.setActionPerformed( rightAction );

    icqSoft.rightSoft = icqBuddyRightPopupItem;

    icqBuddyRightPopupItem.addSubItem( new PopupItem( Localization.getMessage( "DIALOG" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_DIALOG" ).actionPerformed();
      }
    } );
    icqBuddyRightPopupItem.addSubItem( new PopupItem( Localization.getMessage( "INFO" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_BUDDYINFO" ).actionPerformed();
      }
    } );
    icqBuddyRightPopupItem.addSubItem( new PopupItem( Localization.getMessage( "CLIENT" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_CLIENTINFO" ).actionPerformed();
      }
    } );
    icqBuddyRightPopupItem.addSubItem( new PopupItem( Localization.getMessage( "STATUSES" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_STATUSES" ).actionPerformed();
      }
    } );
    icqBuddyRightPopupItem.addSubItem( new PopupItem( Localization.getMessage( "UNIQUE" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_UNIQUE" ).actionPerformed();
      }
    } );
    icqBuddyRightPopupItem.addSubItem( new PopupItem( Localization.getMessage( "SENDFILE" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_SENDFILE" ).actionPerformed();
      }
    } );
    if ( System.getProperty( "supports.video.capture" ) != null && System.getProperty( "supports.video.capture" ).equals( "true" ) ) {
      icqBuddyRightPopupItem.addSubItem( new PopupItem( Localization.getMessage( "SENDPHOTO" ) ) {
        public void actionPerformed() {
          MainFrame.this.getKeyEvent( "KEY_SENDPHOTO" ).actionPerformed();
        }
      } );
    }
    icqBuddyRightPopupItem.addSubItem( new PopupItem( Localization.getMessage( "HISTORY" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_HISTORY" ).actionPerformed();
      }
    } );
    PopupItem renameItem = new PopupItem( Localization.getMessage( "RENAME" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_RENAME" ).actionPerformed();
      }
    };
    icqBuddyRightPopupItem.addSubItem( renameItem );
    icqGroupRightPopupItem.addSubItem( renameItem );
    PopupItem removeItem = new PopupItem( Localization.getMessage( "REMOVE" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_REMOVE" ).actionPerformed();
      }
    };
    icqBuddyRightPopupItem.addSubItem( removeItem );
    icqGroupRightPopupItem.addSubItem( removeItem );
    PopupItem authPopupItem = new PopupItem( Localization.getMessage( "AUTH" ) );
    authPopupItem.addSubItem( new PopupItem( Localization.getMessage( "AUTH_REQUEST" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_REQUESTAUTH" ).actionPerformed();
      }
    } );
    authPopupItem.addSubItem( new PopupItem( Localization.getMessage( "AUTH_ACCEPT" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_ACCEPTAUTH" ).actionPerformed();
      }
    } );
    authPopupItem.addSubItem( new PopupItem( Localization.getMessage( "AUTH_DENY" ) ) {
      public void actionPerformed() {
        BuddyItem buddyItem = getSelectedBuddyItem();
        if ( buddyItem != null ) {
          try {
            IcqPacketSender.authReply( ( ( IcqAccountRoot ) getActiveAccountRoot() ).session, buddyItem.getUserId(), false, StringUtil.stringToByteArray( Localization.getMessage( "DEFAULT_REJECT" ), true ) );
          } catch ( IOException ex ) {
          }
        }
      }
    } );
    icqBuddyRightPopupItem.addSubItem( authPopupItem );

    privacyPopupItem.addSubItem( visiblePopupItem );
    privacyPopupItem.addSubItem( invisiblePopupItem );
    privacyPopupItem.addSubItem( ignorePopupItem );
    icqBuddyRightPopupItem.addSubItem( privacyPopupItem );
  }

  public BuddyItem getSelectedBuddyItem() {
    try {
      if ( buddyList.selectedRealGroup >= 0 && buddyList.selectedRealGroup < buddyList.items.size() ) {
        if ( buddyList.selectedRealIndex >= 0 && buddyList.selectedRealIndex < ( ( GroupHeader ) buddyList.items.elementAt( buddyList.selectedRealGroup ) ).getChildsCount() ) {
          return ( BuddyItem ) ( ( GroupHeader ) buddyList.items.elementAt( buddyList.selectedRealGroup ) ).getChilds().elementAt( buddyList.selectedRealIndex );
        }
      }
    } catch ( java.lang.ClassCastException ex1 ) {
    }
    return null;
  }

  public BuddyGroup getSelectedGroupItem() {
    try {
      if ( buddyList.selectedRealGroup >= 0 && buddyList.selectedRealGroup < buddyList.items.size() ) {
        if ( buddyList.selectedRealIndex == -1 ) {
          return ( BuddyGroup ) ( ( GroupHeader ) buddyList.items.elementAt( buddyList.selectedRealGroup ) );
        }
      }
    } catch ( java.lang.ClassCastException ex1 ) {
    }
    return null;
  }

  public final void initMmpSoft() {
    /** MMP Soft **/
    mmpSoft = new Soft( Screen.screen );

    final PopupItem statusItem = new PopupItem( Localization.getMessage( "MSTATUS" ) );

    final PopupItem groupsPopup = new PopupItem( Localization.getMessage( "GROUPS" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_FILTERGROUPS" ).actionPerformed();
      }
    };
    final PopupItem offlinePopup = new PopupItem( Localization.getMessage( "OFFLINE_BUDDYES" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_FILTEROFFLINE" ).actionPerformed();
      }
    };

    mmpSoft.leftSoft = new PopupItem( Localization.getMessage( "MENU" ) ) {
      public void actionPerformed() {
        MmpAccountRoot mmpAccountRoot = ( ( MmpAccountRoot ) getActiveAccountRoot() );
        /** Checking statusIndex icons **/
        statusItem.imageFileHash = "/res/groups/img_mmpstatus.png".hashCode();
        statusItem.imageIndex = mmpAccountRoot.getStatusIndex();
        /** Addings dialog popup items **/
        /** Filters **/
        groupsPopup.title = ( mmpAccountRoot.isShowGroups ? Localization.getMessage( "HIDE" ) : Localization.getMessage( "SHOW" ) ).concat( " " ).concat( Localization.getMessage( "GROUPS" ) );
        offlinePopup.title = ( mmpAccountRoot.isShowOffline ? Localization.getMessage( "HIDE" ) : Localization.getMessage( "SHOW" ) ).concat( " " ).concat( Localization.getMessage( "OFFLINE_BUDDYES" ) );

      }
    };

    mmpSoft.leftSoft.addSubItem( statusItem );

    PopupItem filterPopup = new PopupItem( Localization.getMessage( "FILTER" ), IconsType.HASH_MAIN, 0 );
    filterPopup.addSubItem( groupsPopup );
    filterPopup.addSubItem( offlinePopup );
    mmpSoft.leftSoft.addSubItem( filterPopup );

    mmpSoft.leftSoft.addSubItem( new PopupItem( Localization.getMessage( "DIALOGS" ), IconsType.HASH_MAIN, 1 ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_DIALOGS" ).actionPerformed();
      }
    } );

    PopupItem buddyListItem = ( new PopupItem( Localization.getMessage( "BUDDYLIST" ), IconsType.HASH_MAIN, 3 ) );
    buddyListItem.addSubItem( new PopupItem( Localization.getMessage( "ADD_GROUP" ), IconsType.HASH_MAIN, 4 ) {
      public void actionPerformed() {
        AddingGroupFrame addingGroupFrame = new AddingGroupFrame( getActiveAccountRoot() );
        addingGroupFrame.s_prevWindow = MainFrame.this;
        MidletMain.screen.setActiveWindow( addingGroupFrame );
      }
    } );
    buddyListItem.addSubItem( new PopupItem( Localization.getMessage( "ADD_BUDDY" ), IconsType.HASH_MAIN, 5 ) {
      public void actionPerformed() {
        AddingBuddyFrame addingBuddyFrame = new AddingBuddyFrame( getActiveAccountRoot(), 1 );
        addingBuddyFrame.s_prevWindow = MainFrame.this;
        MidletMain.screen.setActiveWindow( addingBuddyFrame );
      }
    } );
    buddyListItem.addSubItem( new PopupItem( Localization.getMessage( "ADD_PHONE" ), IconsType.HASH_MAIN, 5 ) {
      public void actionPerformed() {
        AddingBuddyFrame addingBuddyFrame = new AddingBuddyFrame( getActiveAccountRoot(), 2 );
        addingBuddyFrame.s_prevWindow = MainFrame.this;
        MidletMain.screen.setActiveWindow( addingBuddyFrame );
      }
    } );
    mmpSoft.leftSoft.addSubItem( buddyListItem );
    mmpSoft.leftSoft.addSubItem( new PopupItem( Localization.getMessage( "SEND_FREE_SMS" ), IconsType.HASH_CHAT, 7 ) {
      public void actionPerformed() {
        MmpSmsSendFrame mmpSmsSendFrame = new MmpSmsSendFrame( ( MmpAccountRoot ) getActiveAccountRoot() );
        mmpSmsSendFrame.s_prevWindow = MainFrame.this;
        MidletMain.screen.setActiveWindow( mmpSmsSendFrame );
      }
    } );

    PopupItem extPopupItem = new PopupItem( Localization.getMessage( "MMP_EXT_STATUS" ) );
    PopupItem tempPopupItem;
    PopupItem parentPopupItem = statusItem;
    int statusImageHashCode = "/res/groups/img_mmpstatus.png".hashCode();
    for ( int c = 0; c < MmpStatusUtil.getStatusCount(); c++ ) {
      final long statusId = MmpStatusUtil.getStatus( c );
      final int statusIndex = c;
      if ( Localization.getMessage( MmpStatusUtil.getStatusDescr( c ) ).equals( Localization._DEFAULT_STRING ) ) {
        continue;
      }
      tempPopupItem = new PopupItem( Localization.getMessage( MmpStatusUtil.getStatusDescr( c ) ) ) {
        public void actionPerformed() {
          final MmpAccountRoot mmpAccountRoot = ( MmpAccountRoot ) ( ( AccountTab ) accountTabs.items.elementAt( accountTabs.selectedIndex ) ).accountRoot;
          /** Status is selected **/
          if ( mmpAccountRoot.statusIndex == 0 && statusIndex != 0 ) {
            /** Need to connect **/
            mmpAccountRoot.loadStatus( statusIndex );
            mmpAccountRoot.connectAction( statusIndex );
          } else {
            if ( mmpAccountRoot.statusIndex != 0 && statusIndex == 0 ) {
              /** Need go offline **/
              ActionExec.disconnectEvent( mmpAccountRoot );
              mmpAccountRoot.session.disconnect();
            } else {
              if ( mmpAccountRoot.statusIndex != 0 ) {
                try {
                  mmpAccountRoot.statusIndex = statusIndex;
                  mmpAccountRoot.loadStatus( statusIndex );
                  /** Plain statusIndex changing **/
                  MmpPacketSender.MRIM_CS_CHANGE_STATUS( mmpAccountRoot,
                          statusId, mmpAccountRoot.statusText );
                  updateAccountsStatus();
                } catch ( IOException ex ) {
                  LogUtil.outMessage( "Can't set status", true );
                }
                SetStatusTextFrame setStatusTextFrame = new SetStatusTextFrame( mmpAccountRoot, statusIndex );
                setStatusTextFrame.s_prevWindow = MainFrame.this;
                MidletMain.screen.setActiveWindow( setStatusTextFrame );
              }
            }
          }
        }
      };
      tempPopupItem.imageFileHash = statusImageHashCode;
      tempPopupItem.imageIndex = c;
      parentPopupItem.addSubItem( tempPopupItem );
      if ( c == MmpStatusUtil.baseStatusCount ) {
        statusItem.addSubItem( extPopupItem );
        parentPopupItem = extPopupItem;
      }
    }

    mmpSoft.leftSoft.addSubItem( accountPopupItem );

    mmpSoft.leftSoft.addSubItem( settingsPopupItem );
    mmpSoft.leftSoft.addSubItem( servicePopupItem );
    mmpSoft.leftSoft.addSubItem( servicesPopupItem );
    mmpSoft.leftSoft.addSubItem( infoPopupItem );
    mmpSoft.leftSoft.addSubItem( lockPopupItem );
    mmpSoft.leftSoft.addSubItem( minimizePopupItem );
    mmpSoft.leftSoft.addSubItem( exitPopupItem );

    Thread rightAction = new Thread() {
      public void run() {
        BuddyItem buddyItem = getSelectedBuddyItem();
        if ( buddyItem != null ) {
          if ( buddyItem.isPhone() ) {
            mmpSoft.rightSoft = mmpPhoneRightPopupItem;
          } else {
            mmpSoft.rightSoft = mmpBuddyRightPopupItem;
          }
        } else {
          mmpSoft.rightSoft = mmpGroupRightPopupItem;
        }
      }
    };

    mmpBuddyRightPopupItem = new PopupItem( Localization.getMessage( "BUDDY" ) );
    mmpPhoneRightPopupItem = new PopupItem( Localization.getMessage( "BUDDY" ) );
    mmpGroupRightPopupItem = new PopupItem( Localization.getMessage( "BUDDY" ) );

    mmpBuddyRightPopupItem.setActionPerformed( rightAction );
    mmpPhoneRightPopupItem.setActionPerformed( rightAction );
    mmpGroupRightPopupItem.setActionPerformed( rightAction );

    mmpSoft.rightSoft = mmpBuddyRightPopupItem;

    PopupItem dialogItem = new PopupItem( Localization.getMessage( "DIALOG" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_DIALOG" ).actionPerformed();
      }
    };
    mmpBuddyRightPopupItem.addSubItem( dialogItem );
    mmpPhoneRightPopupItem.addSubItem( dialogItem );

    PopupItem infoItem = new PopupItem( Localization.getMessage( "INFO" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_BUDDYINFO" ).actionPerformed();
      }
    };

    mmpBuddyRightPopupItem.addSubItem( infoItem );
    mmpPhoneRightPopupItem.addSubItem( infoItem );

    PopupItem wakeupItem = new PopupItem( Localization.getMessage( "WAKEUP" ) ) {
      public void actionPerformed() {
        AccountRoot accountRoot = getActiveAccountRoot();
        if ( accountRoot instanceof MmpAccountRoot ) {
          BuddyItem buddyItem = getSelectedBuddyItem();
          if ( buddyItem != null ) {
            try {
              ( ( MmpAccountRoot ) accountRoot ).sendWakeup( buddyItem );
            } catch ( IOException ex ) {
            }
          }
        }
      }
    };

    mmpBuddyRightPopupItem.addSubItem( wakeupItem );

    PopupItem historyItem = new PopupItem( Localization.getMessage( "HISTORY" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_HISTORY" ).actionPerformed();
      }
    };

    mmpBuddyRightPopupItem.addSubItem( historyItem );
    mmpPhoneRightPopupItem.addSubItem( historyItem );

    PopupItem renameItem = new PopupItem( Localization.getMessage( "RENAME" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_RENAME" ).actionPerformed();
      }
    };

    mmpBuddyRightPopupItem.addSubItem( renameItem );
    mmpPhoneRightPopupItem.addSubItem( renameItem );
    mmpGroupRightPopupItem.addSubItem( renameItem );

    PopupItem removeItem = new PopupItem( Localization.getMessage( "REMOVE" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_REMOVE" ).actionPerformed();
      }
    };

    mmpBuddyRightPopupItem.addSubItem( removeItem );
    mmpPhoneRightPopupItem.addSubItem( removeItem );
    mmpGroupRightPopupItem.addSubItem( removeItem );

    PopupItem authPopupItem = new PopupItem( Localization.getMessage( "AUTH" ) );
    authPopupItem.addSubItem( new PopupItem( Localization.getMessage( "AUTH_REQUEST" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_REQUESTAUTH" ).actionPerformed();
      }
    } );
    authPopupItem.addSubItem( new PopupItem( Localization.getMessage( "AUTH_ACCEPT" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_ACCEPTAUTH" ).actionPerformed();
      }
    } );
    mmpBuddyRightPopupItem.addSubItem( authPopupItem );
  }

  public void initXmppSoft() {
    /** MMP Soft **/
    xmppSoft = new Soft( Screen.screen );

    final PopupItem statusItem = new PopupItem( Localization.getMessage( "MSTATUS" ) );

    final PopupItem groupsPopup = new PopupItem( Localization.getMessage( "GROUPS" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_FILTERGROUPS" ).actionPerformed();
      }
    };
    final PopupItem offlinePopup = new PopupItem( Localization.getMessage( "OFFLINE_BUDDYES" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_FILTEROFFLINE" ).actionPerformed();
      }
    };

    xmppSoft.leftSoft = new PopupItem( Localization.getMessage( "MENU" ) ) {
      public void actionPerformed() {
        XmppAccountRoot xmppAccountRoot = ( ( XmppAccountRoot ) getActiveAccountRoot() );
        /** Checking statusIndex icons **/
        statusItem.imageFileHash = "/res/groups/img_xmppstatus.png".hashCode();
        statusItem.imageIndex = xmppAccountRoot.getStatusIndex();
        /** Addings dialog popup items **/
        /** Filters **/
        groupsPopup.title = ( xmppAccountRoot.isShowGroups ? Localization.getMessage( "HIDE" ) : Localization.getMessage( "SHOW" ) ).concat( " " ).concat( Localization.getMessage( "GROUPS" ) );
        offlinePopup.title = ( xmppAccountRoot.isShowOffline ? Localization.getMessage( "HIDE" ) : Localization.getMessage( "SHOW" ) ).concat( " " ).concat( Localization.getMessage( "OFFLINE_BUDDYES" ) );
      }
    };

    xmppSoft.leftSoft.addSubItem( statusItem );

    PopupItem filterPopup = new PopupItem( Localization.getMessage( "FILTER" ), IconsType.HASH_MAIN, 0 );
    filterPopup.addSubItem( groupsPopup );
    filterPopup.addSubItem( offlinePopup );
    xmppSoft.leftSoft.addSubItem( filterPopup );

    xmppSoft.leftSoft.addSubItem( new PopupItem( Localization.getMessage( "DIALOGS" ), IconsType.HASH_MAIN, 1 ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_DIALOGS" ).actionPerformed();
      }
    } );

    xmppSoft.leftSoft.addSubItem( new PopupItem( Localization.getMessage( "SERVICES" ), IconsType.HASH_MAIN, 27 ) {
      public void actionPerformed() {
        MidletMain.servicesFrame = new ServicesFrame( ( XmppAccountRoot ) getActiveAccountRoot() );
        MidletMain.servicesFrame.s_prevWindow = MainFrame.this;
        MidletMain.screen.setActiveWindow( MidletMain.servicesFrame );
      }
    } );

    xmppSoft.leftSoft.addSubItem( new PopupItem( Localization.getMessage( "BOOKMARKS" ), IconsType.HASH_MAIN, 26 ) {
      public void actionPerformed() {
        MidletMain.bookmarksFrame = new BookmarksFrame( ( XmppAccountRoot ) getActiveAccountRoot() );
        MidletMain.bookmarksFrame.s_prevWindow = MainFrame.this;
        MidletMain.screen.setActiveWindow( MidletMain.bookmarksFrame );
      }
    } );

    xmppSoft.leftSoft.addSubItem( new PopupItem( Localization.getMessage( "FILETRANFSER" ), IconsType.HASH_MAIN, 2 ) {
      public void actionPerformed() {
        AccountRoot accountRoot = getActiveAccountRoot();
        accountRoot.getTransactionsFrame().s_prevWindow = MainFrame.this;
        MidletMain.screen.setActiveWindow( accountRoot.getTransactionsFrame() );
      }
    } );

    PopupItem buddyListItem = ( new PopupItem( Localization.getMessage( "BUDDYLIST" ), IconsType.HASH_MAIN, 3 ) );
    buddyListItem.addSubItem( new PopupItem( Localization.getMessage( "ADD_BUDDY" ), IconsType.HASH_MAIN, 5 ) {
      public void actionPerformed() {
        AddingBuddyFrame addingBuddyFrame = new AddingBuddyFrame( getActiveAccountRoot(), 1 );
        addingBuddyFrame.s_prevWindow = MainFrame.this;
        MidletMain.screen.setActiveWindow( addingBuddyFrame );
      }
    } );

    PopupItem tempPopupItem;
    for ( int c = 0; c < XmppStatusUtil.getStatusCount(); c++ ) {
      final int statusIndex = c;
      tempPopupItem = new PopupItem( Localization.getMessage( XmppStatusUtil.getStatusDescr( c ) ) ) {
        public void actionPerformed() {
          final XmppAccountRoot xmppAccountRoot = ( XmppAccountRoot ) ( ( AccountTab ) accountTabs.items.elementAt( accountTabs.selectedIndex ) ).accountRoot;
          /** Status is selected **/
          if ( xmppAccountRoot.statusIndex == 0 && statusIndex != 0 ) {
            xmppAccountRoot.connectAction( statusIndex );
          } else {
            if ( xmppAccountRoot.statusIndex != 0 && statusIndex == 0 ) {
              /** Need go offline **/
              try {
                xmppAccountRoot.xmppSession.disconnect();
                xmppAccountRoot.statusIndex = statusIndex;
                ActionExec.disconnectEvent( xmppAccountRoot );
              } catch ( IOException ex ) {
                LogUtil.outMessage( "Can't disconnect", true );
              }
            } else {
              if ( xmppAccountRoot.statusIndex != 0 ) {
                /** Plain statusIndex changing **/
                try {
                  XmppSender.setStatus( xmppAccountRoot.xmppSession.xmlWriter, xmppAccountRoot.userId.concat( "/" ).concat( xmppAccountRoot.resource ),
                          XmppStatusUtil.statuses[statusIndex], "", xmppAccountRoot.priority );
                  xmppAccountRoot.statusIndex = statusIndex;
                  if ( xmppAccountRoot.conferenceGroup != null && xmppAccountRoot.conferenceGroup.getChildsCount() > 0 ) {
                    for ( int c = 0; c < xmppAccountRoot.conferenceGroup.getChilds().size(); c++ ) {
                      XmppItem groupChatItem = ( XmppItem ) xmppAccountRoot.conferenceGroup.getChilds().elementAt( c );
                      if ( groupChatItem.getStatusIndex() != XmppStatusUtil.offlineIndex ) {
                        XmppSender.sendPresence( xmppAccountRoot.xmppSession.xmlWriter, null, groupChatItem.userId,
                                null, XmppStatusUtil.statuses[statusIndex], "", xmppAccountRoot.priority, false, null, null );
                      }
                    }
                  }
                  updateAccountsStatus();
                } catch ( IOException ex ) {
                  LogUtil.outMessage( "Can't set status", true );
                }
              }
            }
          }
        }
      };
      tempPopupItem.imageFileHash = "/res/groups/img_xmppstatus.png".hashCode();
      tempPopupItem.imageIndex = c;
      statusItem.addSubItem( tempPopupItem );
    }

    xmppSoft.leftSoft.addSubItem( accountPopupItem );

    xmppSoft.leftSoft.addSubItem( settingsPopupItem );
    xmppSoft.leftSoft.addSubItem( servicePopupItem );
    xmppSoft.leftSoft.addSubItem( servicesPopupItem );
    xmppSoft.leftSoft.addSubItem( infoPopupItem );
    xmppSoft.leftSoft.addSubItem( lockPopupItem );
    xmppSoft.leftSoft.addSubItem( minimizePopupItem );
    xmppSoft.leftSoft.addSubItem( exitPopupItem );

    final PopupItem dialogsPopup = new PopupItem( Localization.getMessage( "DIALOG" ) );
    final PopupItem sendFilePopup = new PopupItem( Localization.getMessage( "SENDFILE" ) );
    Thread xmppRightPopupAction = new Thread() {
      public void run() {
        if ( !dialogsPopup.isEmpty() ) {
          dialogsPopup.subPopup.items.removeAllElements();
          dialogsPopup.subPopup.yOffset = 0;
          dialogsPopup.subPopup.selectedIndex = 0;
        }
        if ( !sendFilePopup.isEmpty() ) {
          sendFilePopup.subPopup.items.removeAllElements();
          sendFilePopup.subPopup.yOffset = 0;
          sendFilePopup.subPopup.selectedIndex = 0;
        }
        final XmppItem xmppItem = ( XmppItem ) getSelectedBuddyItem();
        if ( xmppItem != null ) {
          Enumeration resources = xmppItem.resources.elements();
          if ( xmppItem.resources.size() <= 1 ) {
            final Resource resource = xmppItem.getDefaultResource();
            dialogsPopup.setActionPerformed( new Thread() {
              public void run() {
                LogUtil.outMessage( "One resource: " + resource.resource );
                XmppAccountRoot xmppAccountRoot = ( XmppAccountRoot ) getActiveAccountRoot();
                ChatTab chatTab = MidletMain.chatFrame.getChatTab( xmppAccountRoot, xmppItem.getUserId(), resource.resource, true );
                if ( chatTab == null ) {
                  /** There is no opened chat tab **/
                  chatTab = new ChatTab( xmppAccountRoot, xmppItem, resource, xmppAccountRoot.getStatusImages().hashCode(), "/res/groups/img_chat.png".hashCode() );
                  MidletMain.chatFrame.addChatTab( chatTab, true );
                }
                MidletMain.screen.setActiveWindow( MidletMain.chatFrame );
              }
            } );
            sendFilePopup.setActionPerformed( new Thread() {
              public void run() {
                LogUtil.outMessage( "One resource: " + resource.resource );
                XmppAccountRoot xmppAccountRoot = ( XmppAccountRoot ) getActiveAccountRoot();
                FileBrowserFrame fileBrowserFrame = new FileBrowserFrame( 0, xmppAccountRoot, xmppItem.getUserId().concat( "/" ).concat( resource.resource ) );
                fileBrowserFrame.s_prevWindow = MainFrame.this;
                MidletMain.screen.setActiveWindow( fileBrowserFrame );
              }
            } );
          } else {
            while ( resources.hasMoreElements() ) {
              final Resource resource = ( Resource ) resources.nextElement();
              dialogsPopup.addSubItem( new PopupItem(
                      ( ( resource.resource.length() == 0 && xmppItem.isGroupChat ) ? Localization.getMessage( "XMPP_ROOM" ) : ( resource.resource.length() == 0 ? Localization.getMessage( "XMPP_ALL_RESOURCES" ) : resource.resource ) ),
                      "/res/groups/img_xmppstatus.png".hashCode(),
                      ( ( resource.resource.length() == 0 && xmppItem.isGroupChat ) ? XmppStatusUtil.groupChatIndex : ( ( resource.resource.length() == 0 ) ? XmppStatusUtil.onlineIndex : resource.statusIndex ) ) ) {
                public void actionPerformed() {
                  LogUtil.outMessage( "Of multiple resource: " + resource.resource );
                  XmppAccountRoot xmppAccountRoot = ( XmppAccountRoot ) getActiveAccountRoot();
                  ChatTab chatTab = MidletMain.chatFrame.getChatTab( xmppAccountRoot, xmppItem.getUserId(), resource.resource, true );
                  if ( chatTab == null ) {
                    /** There is no opened chat tab **/
                    chatTab = new ChatTab( xmppAccountRoot, xmppItem, resource, xmppAccountRoot.getStatusImages().hashCode(), "/res/groups/img_chat.png".hashCode() );
                    MidletMain.chatFrame.addChatTab( chatTab, true );
                  }
                  MidletMain.screen.setActiveWindow( MidletMain.chatFrame );
                }
              } );
              sendFilePopup.addSubItem( new PopupItem(
                      ( ( resource.resource.length() == 0 && xmppItem.isGroupChat ) ? Localization.getMessage( "XMPP_ROOM" ) : ( resource.resource.length() == 0 ? Localization.getMessage( "XMPP_ALL_RESOURCES" ) : resource.resource ) ),
                      "/res/groups/img_xmppstatus.png".hashCode(),
                      ( ( resource.resource.length() == 0 && xmppItem.isGroupChat ) ? XmppStatusUtil.groupChatIndex : ( ( resource.resource.length() == 0 ) ? XmppStatusUtil.onlineIndex : resource.statusIndex ) ) ) {
                public void actionPerformed() {
                  LogUtil.outMessage( "Of multiple resource: " + resource.resource );
                  XmppAccountRoot xmppAccountRoot = ( XmppAccountRoot ) getActiveAccountRoot();
                  FileBrowserFrame fileBrowserFrame = new FileBrowserFrame( 0, xmppAccountRoot, xmppItem.getUserId().concat( "/" ).concat( resource.resource ) );
                  fileBrowserFrame.s_prevWindow = MainFrame.this;
                  MidletMain.screen.setActiveWindow( fileBrowserFrame );
                }
              } );
            }
          }
        }
        if ( xmppItem != null && xmppItem.isGroupChat ) {
          xmppSoft.rightSoft = xmppConfrRightPopupItem;
        } else {
          if ( xmppItem != null ) {
            xmppSoft.rightSoft = xmppBuddyRightPopupItem;
          } else {
            /** Group popup menu **/
            xmppSoft.rightSoft = xmppGroupRightPopupItem;
          }
        }
      }
    };

    xmppBuddyRightPopupItem = new PopupItem( Localization.getMessage( "BUDDY" ) );
    xmppGroupRightPopupItem = new PopupItem( Localization.getMessage( "BUDDY" ) );
    xmppConfrRightPopupItem = new PopupItem( Localization.getMessage( "BUDDY" ) );
    xmppBuddyRightPopupItem.setActionPerformed( xmppRightPopupAction );
    xmppGroupRightPopupItem.setActionPerformed( xmppRightPopupAction );
    xmppConfrRightPopupItem.setActionPerformed( xmppRightPopupAction );

    xmppSoft.rightSoft = xmppBuddyRightPopupItem;

    xmppBuddyRightPopupItem.addSubItem( dialogsPopup );

    xmppBuddyRightPopupItem.addSubItem( new PopupItem( Localization.getMessage( "HISTORY" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_HISTORY" ).actionPerformed();
      }
    } );

    xmppBuddyRightPopupItem.addSubItem( sendFilePopup );
    /** Conference right popup */
    xmppConfrRightPopupItem.addSubItem( dialogsPopup );

    xmppConfrRightPopupItem.addSubItem( new PopupItem( Localization.getMessage( "HISTORY" ) ) {
      public void actionPerformed() {
        MainFrame.this.getKeyEvent( "KEY_HISTORY" ).actionPerformed();
      }
    } );
    xmppConfrRightPopupItem.addSubItem( new PopupItem( Localization.getMessage( "EDIT_TOPIC" ) ) {
      public void actionPerformed() {
        final XmppAccountRoot xmppAccountRoot = ( XmppAccountRoot ) ( ( AccountTab ) accountTabs.items.elementAt( accountTabs.selectedIndex ) ).accountRoot;
        final XmppItem xmppItem = ( XmppItem ) getSelectedBuddyItem();
        if ( xmppItem != null ) {
          LogUtil.outMessage( "JID: " + xmppItem.userId );
          LogUtil.outMessage( "Subject: " + xmppItem.groupChatSubject );
          TopicEditFrame topicEditFrame = new TopicEditFrame( xmppAccountRoot, xmppItem.userId, xmppItem.groupChatSubject );
          topicEditFrame.s_prevWindow = MidletMain.mainFrame;
          MidletMain.screen.setActiveWindow( topicEditFrame );
        }
      }
    } );
    xmppConfrRightPopupItem.addSubItem( new PopupItem( Localization.getMessage( "EDIT_NICK" ) ) {
      public void actionPerformed() {
        final XmppAccountRoot xmppAccountRoot = ( XmppAccountRoot ) ( ( AccountTab ) accountTabs.items.elementAt( accountTabs.selectedIndex ) ).accountRoot;
        final XmppItem xmppItem = ( XmppItem ) getSelectedBuddyItem();
        if ( xmppItem != null ) {
          GroupChatNickEditFrame groupChatNickEditFrame = new GroupChatNickEditFrame( xmppAccountRoot, xmppItem );
          groupChatNickEditFrame.s_prevWindow = MidletMain.mainFrame;
          MidletMain.screen.setActiveWindow( groupChatNickEditFrame );
        }
      }
    } );
    xmppConfrRightPopupItem.addSubItem( new PopupItem( Localization.getMessage( "CONFIGURE_CONFR" ) ) {
      public void actionPerformed() {
        final XmppAccountRoot xmppAccountRoot = ( XmppAccountRoot ) ( ( AccountTab ) accountTabs.items.elementAt( accountTabs.selectedIndex ) ).accountRoot;
        final XmppItem xmppItem = ( XmppItem ) getSelectedBuddyItem();
        if ( xmppItem != null ) {
          MidletMain.groupChatConfFrame = new GroupChatConfFrame( xmppAccountRoot, xmppItem );
          MidletMain.groupChatConfFrame.s_prevWindow = MidletMain.mainFrame;
          MidletMain.screen.setActiveWindow( MidletMain.groupChatConfFrame );
        }
      }
    } );
    xmppConfrRightPopupItem.addSubItem( new PopupItem( Localization.getMessage( "ROLE_LIST" ) ) {
      public void actionPerformed() {
        final XmppAccountRoot xmppAccountRoot = ( XmppAccountRoot ) ( ( AccountTab ) accountTabs.items.elementAt( accountTabs.selectedIndex ) ).accountRoot;
        final XmppItem xmppItem = ( XmppItem ) getSelectedBuddyItem();
        if ( xmppItem != null ) {
          MidletMain.groupChatUsersFrame = new GroupChatUsersFrame( xmppAccountRoot, xmppItem.userId, true );
          MidletMain.groupChatUsersFrame.s_prevWindow = MidletMain.mainFrame;
          MidletMain.screen.setActiveWindow( MidletMain.groupChatUsersFrame );
        }
      }
    } );
    xmppConfrRightPopupItem.addSubItem( new PopupItem( Localization.getMessage( "AFFILIATION_LIST" ) ) {
      public void actionPerformed() {
        final XmppAccountRoot xmppAccountRoot = ( XmppAccountRoot ) ( ( AccountTab ) accountTabs.items.elementAt( accountTabs.selectedIndex ) ).accountRoot;
        final XmppItem xmppItem = ( XmppItem ) getSelectedBuddyItem();
        if ( xmppItem != null ) {
          MidletMain.groupChatUsersFrame = new GroupChatUsersFrame( xmppAccountRoot, xmppItem.userId, false );
          MidletMain.groupChatUsersFrame.s_prevWindow = MidletMain.mainFrame;
          MidletMain.screen.setActiveWindow( MidletMain.groupChatUsersFrame );
        }
      }
    } );
    xmppConfrRightPopupItem.addSubItem( new PopupItem( Localization.getMessage( "EXIT_CONFR" ) ) {
      public void actionPerformed() {
        final XmppAccountRoot xmppAccountRoot = ( XmppAccountRoot ) ( ( AccountTab ) accountTabs.items.elementAt( accountTabs.selectedIndex ) ).accountRoot;
        final XmppItem xmppItem = ( XmppItem ) getSelectedBuddyItem();
        if ( xmppItem != null ) {
          xmppItem.offlineResources();
          XmppSender.exitConfrence( xmppAccountRoot.xmppSession, xmppItem.userId, xmppItem.groupChatNick );
          xmppAccountRoot.buddyItems.removeElement( xmppItem );
          xmppAccountRoot.conferenceGroup.getChilds().removeElement( xmppItem );
        }
      }
    } );
  }

  public AccountRoot getActiveAccountRoot() {
    accountTabs.validateSelection();
    if ( accountTabs.items.isEmpty() ) {
      return null;
    }
    if ( accountTabs.selectedIndex == -1 ) {
      /** Pane on Main Frame **/
      return null;
    }
    return checkAccountRoot( accountTabs.selectedIndex );
  }

  public final AccountRoot checkAccountRoot( int index ) {
    AccountTab tempAccountTab = ( ( AccountTab ) accountTabs.items.elementAt( index ) );
    try {
      if ( tempAccountTab.accountRoot == null ) {
        /** Load account root **/
        if ( MidletMain.accounts.getValue( tempAccountTab.accountUserId, "type" ).equals( "icq" ) ) {
          tempAccountTab.accountRoot = new IcqAccountRoot( tempAccountTab.accountUserId );
        } else if ( MidletMain.accounts.getValue( tempAccountTab.accountUserId, "type" ).equals( "mmp" ) ) {
          tempAccountTab.accountRoot = new MmpAccountRoot( tempAccountTab.accountUserId );
        } else if ( MidletMain.accounts.getValue( tempAccountTab.accountUserId, "type" ).equals( "xmpp" ) ) {
          tempAccountTab.accountRoot = new XmppAccountRoot( tempAccountTab.accountUserId );
        }
        tempAccountTab.accountRoot.init( true );
      }
      return tempAccountTab.accountRoot;
    } catch ( GroupNotFoundException ex ) {
    } catch ( IncorrectValueException ex ) {
    }
    return null;
  }

  public final void initPane() {
    pane = new Pane( null, false );
    pane.addItem( new Label( Localization.getMessage( "INTRO_MSG" ) ) );
    Button button = new Button( Localization.getMessage( "ADD_ACCOUNT" ) ) {
      public void actionPerformed() {
        AccountEditorFrame accountEditorFrame = new AccountEditorFrame( null, null, null, null, null, true, null, false );
        MidletMain.screen.setActiveWindow( accountEditorFrame );
      }
    };
    button.setFocusable( true );
    button.setFocused( true );
    pane.addItem( button );
  }

  public final void initEmptySoft() {
    soft = new Soft( MidletMain.screen );
    soft.leftSoft = new PopupItem( Localization.getMessage( "EXIT" ) ) {
      public void actionPerformed() {
        MidletMain.midletMain.notifyDestroyed();
      }
    };
    soft.rightSoft = new PopupItem( "" );
  }

  public AccountTab getAccountTab( String loginId ) {
    AccountTab tempAccountTab;
    for ( int c = 0; c < accountTabs.items.size(); c++ ) {
      tempAccountTab = ( ( AccountTab ) accountTabs.items.elementAt( c ) );
      if ( tempAccountTab.accountUserId.equals( loginId ) ) {
        return tempAccountTab;
      }
    }
    return null;
  }

  public BuddyItem getBuddyItem( AccountRoot accountRoot, String buddyId ) {
    if ( accountRoot == null || buddyId == null ) {
      return null;
    }
    Vector buddyItems = accountRoot.getBuddyItems();
    GroupHeader buddyGroup;
    BuddyItem buddyItem;
    for ( int i = 0; i < buddyItems.size(); i++ ) {
      buddyGroup = ( GroupHeader ) buddyItems.elementAt( i );
      if ( buddyGroup.getChilds() == null || buddyGroup.getChildsCount() == 0 ) {
        continue;
      }
      for ( int j = 0; j < buddyGroup.getChilds().size(); j++ ) {
        buddyItem = ( BuddyItem ) buddyGroup.getChilds().elementAt( j );
        if ( buddyItem != null && buddyItem.getUserId() != null && buddyItem.getUserId().equals( buddyId ) ) {
          return buddyItem;
        }
      }
    }
    return null;
  }

  public void typingNotify( AccountRoot accountRoot, String buddyId, boolean b ) {
    BuddyItem buddyItem = getBuddyItem( accountRoot, buddyId );
    if ( buddyItem != null ) {
      buddyItem.setTypingStatus( b );
      buddyItem.updateUiData();
    }
  }

  public boolean typingNotify( AccountRoot accountRoot, String buddyId ) {
    boolean isTyping;
    BuddyItem buddyItem = getBuddyItem( accountRoot, buddyId );
    if ( buddyItem != null ) {
      buddyItem.setTypingStatus( true );
      buddyItem.updateUiData();
    }
    isTyping = buddyItem.getTypingStatus();
    return isTyping;
  }
}
