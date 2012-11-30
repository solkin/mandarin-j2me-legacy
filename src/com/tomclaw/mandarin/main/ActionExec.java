package com.tomclaw.mandarin.main;

import com.tomclaw.mandarin.icq.*;
import com.tomclaw.mandarin.mmp.MmpAccountRoot;
import com.tomclaw.mandarin.mmp.MmpItem;
import com.tomclaw.mandarin.mmp.MmpStatusUtil;
import com.tomclaw.mandarin.xmpp.XmppAccountRoot;
import com.tomclaw.mandarin.xmpp.XmppItem;
import com.tomclaw.mandarin.xmpp.XmppSession;
import com.tomclaw.mandarin.xmpp.XmppStatusUtil;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Display;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class ActionExec {

  public static void setConnectionStage( AccountRoot accountRoot, int i ) {
    LogUtil.outMessage( "setConnectionStage(".concat( accountRoot.getUserId() ).concat( ": " ).concat( String.valueOf( i ) ).concat( ")" ) );
    MidletMain.mainFrame.getAccountTab( accountRoot.getUserId() ).fillPercent = 100 * i / 10;
    MidletMain.screen.repaint();
  }

  public static void disconnectEvent( AccountRoot accountRoot ) {
    /** Switch all buddyes to offline status **/
    accountRoot.offlineAllBuddyes();
    /** Update tab icons **/
    accountRoot.offlineAccount();
    accountRoot.updateMainFrameBuddyList();
    MidletMain.mainFrame.updateAccountsStatus();
  }

  public static void setBuddyStatus( IcqAccountRoot icqAccountRoot, String buddyId, int buddyStatus, Capability[] caps, ClientInfo clientInfo ) {
    if ( caps != null ) {
      for ( int c = 0; c < caps.length; c++ ) {
        caps[c] = CapUtil.fillCapFields( caps[c] );
      }
    }
    int prevBuddyStatus = icqAccountRoot.getBuddyStatus( buddyId );
    IcqItem icqItem = icqAccountRoot.setBuddyStatus( buddyId, buddyStatus, caps, clientInfo );
    if ( icqItem != null ) {
      ChatTab chatTab = MidletMain.chatFrame.getChatTab( icqAccountRoot, buddyId, null );
      if ( chatTab != null ) {
        // chatTab.buddyItem = icqItem;
        chatTab.updateChatCaption();
      }
    }
    if ( ( prevBuddyStatus != -1 && buddyStatus == -1 ) || ( prevBuddyStatus == -1 && buddyStatus != -1 ) ) {
      icqAccountRoot.getServiceMessages().addMessage( buddyId, icqItem.getUserNick(),
              Localization.getMessage( "CHANGED_STATUS" ).concat( " \"" ).
              concat( Localization.getMessage( IcqStatusUtil.getStatusDescr( IcqStatusUtil.getStatusIndex( prevBuddyStatus ) ) ) ).
              concat( "\" " ).concat( Localization.getMessage( "TO" ) ).concat( " \"" ).
              concat( Localization.getMessage( IcqStatusUtil.getStatusDescr( IcqStatusUtil.getStatusIndex( buddyStatus ) ) ) ).concat( "\"" ),
              ServiceMessages.TYPE_STATUS_CHANGE );
    }
    if ( MidletMain.isSound || MidletMain.getBoolean( MidletMain.uniquest, "icq" + buddyId.hashCode(), "NOTIF_STAT_CHANGE" ) ) {
      LogUtil.outMessage( "Playing sound" );
      if ( prevBuddyStatus != -1 && buddyStatus == -1 ) {
        playSound( 0x04 );
      } else if ( prevBuddyStatus == -1 && buddyStatus != -1 ) {
        playSound( 0x03 );
      }
    }
  }

  public static void setStatusMessage( IcqAccountRoot icqAccountRoot, byte[] msgCookie, byte[] msgText ) {
    LogUtil.outMessage( "Status msg cookie == " + HexUtil.bytesToString( msgCookie ) );
    LogUtil.outMessage( "Window msg cookie == " + HexUtil.bytesToString( MidletMain.mainFrame.statusReaderFrame.plainCookie ) );
    if ( MidletMain.mainFrame.statusReaderFrame != null && ArrayUtil.equals( MidletMain.mainFrame.statusReaderFrame.plainCookie, msgCookie ) ) {
      MidletMain.mainFrame.statusReaderFrame.setPlainStatusText( StringUtil.byteArrayToString( msgText, true ) );
    }
  }

  public static void setXStatusMessage( IcqAccountRoot icqAccountRoot, byte[] msgCookie, byte[] bytes, byte[] bytes0 ) {
    LogUtil.outMessage( "Status x-msg cookie == " + HexUtil.bytesToString( msgCookie ) );
    LogUtil.outMessage( "Window x-msg cookie == " + HexUtil.bytesToString( MidletMain.mainFrame.statusReaderFrame.xStatCookie ) );
    if ( MidletMain.mainFrame.statusReaderFrame != null && ArrayUtil.equals( MidletMain.mainFrame.statusReaderFrame.xStatCookie, msgCookie ) ) {
      MidletMain.mainFrame.statusReaderFrame.setXStatusText( StringUtil.byteArrayToString( bytes, true ), StringUtil.byteArrayToString( bytes0, true ) );
    }
  }

  public static void recMess( AccountRoot accountRoot, String screenName, String resourceTitle, String groupChatNick, String decodedString, byte[] msgCookie, int type ) {
    LogUtil.outMessage( "Received message from: ".concat( screenName ) );
    LogUtil.outMessage( "Message text:          ".concat( decodedString ) );
    if ( MidletMain.getBoolean( MidletMain.uniquest, accountRoot.getAccType() + screenName.hashCode(), "LOCK_INCOMING" ) ) {
      return;
    }
    if ( screenName.equals( "AOL Instant Messenger" ) ) {
      return;
    }
    BuddyItem buddyItem;
    try {
      buddyItem = MidletMain.mainFrame.getBuddyItem( accountRoot, screenName );
      ChatTab chatTab = MidletMain.chatFrame.getChatTab( accountRoot, screenName, resourceTitle, false );
      if ( chatTab == null ) {
        LogUtil.outMessage( "Chat tab not exist" );
        com.tomclaw.mandarin.xmpp.Resource resource = null;
        if ( buddyItem == null ) {
          LogUtil.outMessage( "BuddyItem is not exist. Creating..." );
          buddyItem = accountRoot.getItemInstance();
          buddyItem.setUserId( screenName );
          buddyItem.updateUiData();
          /*if (accountRoot instanceof XmppAccountRoot) {
           ((XmppAccountRoot)accountRoot).addTempItem(buddyItem);
           }*/
          /** Request info and add buddy to server **/
          /** Append to buddy list **/
          if ( accountRoot.getBuddyItems().isEmpty() ) {
            accountRoot.getBuddyItems().addElement( new GroupHeader( "Temp group" ) );
          }
          LogUtil.outMessage( "Group checking complete" );
          ( ( GroupHeader ) accountRoot.getBuddyItems().elementAt( 0 ) ).addChild( ( GroupChild ) buddyItem );
        }
        LogUtil.outMessage( "Done. Creating ChatTab instance" );
        if ( accountRoot instanceof XmppAccountRoot ) {
          resource = ( ( XmppItem ) buddyItem ).getResource( resourceTitle );
        }
        chatTab = new ChatTab( accountRoot, buddyItem, resource, accountRoot.getStatusImages().hashCode(), "/res/groups/img_chat.png".hashCode() );
        LogUtil.outMessage( "ChatTab appending..." );
        MidletMain.chatFrame.addChatTab( chatTab, false );
        LogUtil.outMessage( "Almost done." );
      } else {
        LogUtil.outMessage( "Chat tab exist.        ".concat( chatTab.title ) );
      }

      boolean isDisplayed = MidletMain.chatFrame.addChatItem( chatTab, groupChatNick, decodedString, msgCookie, type, true );
      LogUtil.outMessage( "isDisplayed = " + isDisplayed );
      if ( MidletMain.screen.activeWindow.equals( MidletMain.chatFrame ) && isDisplayed ) {
        /** Chat frame is visible **/
        /** Message read **/
        /** No unrMsgs increment needed **/
      } else {
        buddyItem.setUnreadCount( buddyItem.getUnreadCount( resourceTitle ) + 1, resourceTitle );
        accountRoot.setUnrMsgs( accountRoot.getUnrMsgs() + 1 );
        MidletMain.mainFrame.updateAccountsStatus();
        if ( buddyItem.getUnreadCount() == 1 ) {
          buddyItem.updateUiData();
          chatTab.updateChatCaption();
        }
      }

      boolean isAlarm = true;
      if ( groupChatNick != null && accountRoot instanceof XmppAccountRoot ) {
        LogUtil.outMessage( "GroupChat detected" );
        String groupChatRealNick = ( ( XmppItem ) buddyItem ).groupChatNick;
        LogUtil.outMessage( "groupChatNick = " + groupChatNick );
        LogUtil.outMessage( "groupChatRealNick = " + groupChatRealNick );
        LogUtil.outMessage( "decodedString = " + decodedString );
        LogUtil.outMessage( "decodedString = " + decodedString );
        if ( groupChatRealNick.equals( groupChatNick ) ) {
          isAlarm = false;
          LogUtil.outMessage( "Alarm switched off" );
        } else if ( MidletMain.alarmRepliesOnly && decodedString.indexOf( groupChatRealNick ) == -1 ) {
          isAlarm = false;
          LogUtil.outMessage( "Alarm switched off cause alarmRepliesOnly = " + MidletMain.alarmRepliesOnly );
        }
        if ( ( ( XmppItem ) buddyItem ).groupChatSubject == null ) {
          isAlarm = false;
          LogUtil.outMessage( "Alarm switched off cause groupChatSubject = null" );
        }
      }
      /**
       * Alarm
       */
      boolean isExpandedFlag;
      if ( !MidletMain.isPhotoActive && MidletMain.isExpand && ( !( Display.getDisplay( MidletMain.midletMain ).getCurrent() instanceof javax.microedition.lcdui.TextBox ) || Display.getDisplay( MidletMain.midletMain ).getCurrent() == null ) ) {
        Display.getDisplay( MidletMain.midletMain ).setCurrent( MidletMain.screen );
        isExpandedFlag = true;
      } else {
        isExpandedFlag = false;
      }
      boolean isUniqueNotify = MidletMain.getBoolean( MidletMain.uniquest, accountRoot.getAccType() + screenName.hashCode(), "NOTIF_MESSAGES" );
      if ( ( MidletMain.vibrateDelay > 0 || isUniqueNotify ) && isAlarm ) {
        if ( isExpandedFlag ) {
          try {
            Thread.sleep( 400 );
          } catch ( InterruptedException ex ) {
            // ex.printStackTrace();
          }
        }
        Display.getDisplay( MidletMain.midletMain ).vibrate( isUniqueNotify ? 500 : MidletMain.vibrateDelay );
      }
      MidletMain.screen.repaint();
      if ( ( MidletMain.isSound || isUniqueNotify ) && isAlarm ) {
        playSound( 0x01 );
      }
    } catch ( Throwable ex1 ) {
    }
  }

  public static void sendXStatusMessage( IcqAccountRoot icqAccountRoot, byte[] msgCookie, String screenName ) {
    IcqItem icqItem = icqAccountRoot.getBuddy( screenName );
    icqAccountRoot.getServiceMessages().addMessage( screenName, icqItem != null ? icqItem.getUserNick() : screenName,
            Localization.getMessage( "REQUEST_XSTATUS" ) + " " + ( icqAccountRoot.isXStatusReadable ? Localization.getMessage( "XSTATUS_SENT" ) : Localization.getMessage( "XSTATUS_NOT_SENT" ) ),
            ServiceMessages.TYPE_XSTATUS_READ );
    if ( icqAccountRoot.isXStatusReadable && ( !MidletMain.getBoolean( MidletMain.uniquest, icqAccountRoot.getAccType() + screenName.hashCode(), "DISABLE_XSTATUS_READING" ) ) || ( MidletMain.getBoolean( MidletMain.uniquest, icqAccountRoot.getAccType() + screenName.hashCode(), "SEND_SPECIAL_XSTATUS" ) && ( !MidletMain.getBoolean( MidletMain.uniquest, icqAccountRoot.getAccType() + screenName.hashCode(), "DISABLE_XSTATUS_READING" ) ) ) ) {
      try {
        String xTitle;
        String xDescr;
        if ( MidletMain.getBoolean( MidletMain.uniquest, icqAccountRoot.getAccType() + screenName.hashCode(), "SEND_SPECIAL_XSTATUS" ) ) {
          xTitle = MidletMain.getString( MidletMain.uniquest, icqAccountRoot.getAccType() + screenName.hashCode(), "FLD_SPECIAL_XTITLE" );
          xDescr = MidletMain.getString( MidletMain.uniquest, icqAccountRoot.getAccType() + screenName.hashCode(), "FLD_SPECIAL_XDESCR" );
        } else {
          xTitle = icqAccountRoot.xTitle;
          xDescr = icqAccountRoot.xText;
        }
        LogUtil.outMessage( "xTitle == " + xTitle );
        LogUtil.outMessage( "xDescr == " + xDescr );
        IcqPacketSender.sendXStatusText( icqAccountRoot.session, icqAccountRoot.userId, msgCookie, screenName, StringUtil.stringToByteArray( xTitle, true ), StringUtil.stringToByteArray( xDescr, true ) );
      } catch ( IOException ex ) {
        LogUtil.outMessage( "Cannot send status message", true );
      }
    }
  }

  public static void sendStatusMessage( IcqAccountRoot icqAccountRoot, byte[] msgCookie, String screenName ) {
    IcqItem icqItem = icqAccountRoot.getBuddy( screenName );
    icqAccountRoot.getServiceMessages().addMessage( screenName, icqItem != null ? icqItem.getUserNick() : screenName,
            Localization.getMessage( "REQUEST_STATUS" ) + " " + ( icqAccountRoot.isPStatusReadable ? Localization.getMessage( "STATUS_SENT" ) : Localization.getMessage( "STATUS_NOT_SENT" ) ),
            ServiceMessages.TYPE_MSTATUS_READ );
    if ( icqAccountRoot.isPStatusReadable && !MidletMain.getBoolean( MidletMain.uniquest, icqAccountRoot.getAccType() + screenName.hashCode(), "DISABLE_PSTATUS_READING" )
            || ( MidletMain.getBoolean( MidletMain.uniquest, icqAccountRoot.getAccType() + screenName.hashCode(), "SEND_SPECIAL_PSTATUS" ) && ( !MidletMain.getBoolean( MidletMain.uniquest, icqAccountRoot.getAccType() + screenName.hashCode(), "DISABLE_PSTATUS_READING" ) ) ) ) {
      try {
        String pStatus;
        if ( MidletMain.getBoolean( MidletMain.uniquest, icqAccountRoot.getAccType() + screenName.hashCode(), "SEND_SPECIAL_PSTATUS" ) ) {
          pStatus = MidletMain.getString( MidletMain.uniquest, icqAccountRoot.getAccType() + screenName.hashCode(), "FLD_SPECIAL_PSTATUS" );
        } else {
          pStatus = icqAccountRoot.statusText;
        }
        LogUtil.outMessage( "pStatus == " + pStatus );
        IcqPacketSender.sendStatusMessage( icqAccountRoot.session, msgCookie, screenName, pStatus );
      } catch ( IOException ex ) {
        LogUtil.outMessage( "Cannot send status message", true );
      }
    }
  }

  public static void msgAck( AccountRoot accountRoot, String buddyId, String resource, byte[] msgCookie ) {
    if ( buddyId == null ) {
      MidletMain.chatFrame.msgAck( accountRoot, msgCookie, ChatItem.DLV_STATUS_DELIVERED );
    } else {
      MidletMain.chatFrame.msgAck( accountRoot, buddyId, resource, msgCookie, ChatItem.DLV_STATUS_DELIVERED );
    }
    MidletMain.screen.repaint();
    if ( MidletMain.isSound ) {
      playSound( 0x02 );
    }
  }

  public static void setBuddyTypingStatus( final AccountRoot accountRoot, final String buddyId, String resource, boolean isTyping, boolean isInvertTyping ) {
    /** Updating notify in buddylist **/
    if ( isInvertTyping ) {
      // isTyping = MidletMain.mainFrame.typingNotify( accountRoot, buddyId );
    } else {
      MidletMain.mainFrame.typingNotify( accountRoot, buddyId, isTyping );
    }
    /** Updating notify in ChatFrame **/
    ChatTab chatTab = MidletMain.chatFrame.getChatTab( accountRoot, buddyId, resource );
    if ( chatTab != null ) {
      /** Dialog is opened **/
      chatTab.updateChatCaption();
    }
    MidletMain.screen.repaint();
  }

  public static void showUserShortInfo( AccountRoot accountRoot, BuddyInfo buddyInfo ) {
    LogUtil.outMessage( "User info response. \nreqSeqNum = " + buddyInfo.reqSeqNum );
    LogUtil.outMessage( "buddyId = " + buddyInfo.buddyId );
    LogUtil.outMessage( "nickName = " + buddyInfo.nickName );
    if ( MidletMain.mainFrame.buddyInfoFrame != null && ( MidletMain.mainFrame.buddyInfoFrame.reqSeqNum == buddyInfo.reqSeqNum || ( buddyInfo.buddyId != null && MidletMain.mainFrame.buddyInfoFrame.buddyItem.getUserId().equals( buddyInfo.buddyId ) ) ) ) {
      MidletMain.mainFrame.buddyInfoFrame.placeInfo( buddyInfo );
    }
  }

  /*public static void showUserShortInfo(IcqAccountRoot accountRoot, int reqSeqNum, String nickName, String firstName, String lastName, String eMail, byte authFlag) {
   LogUtil.outMessage("User info response. reqSeqNum = " + reqSeqNum);
   if (MidletMain.mainFrame.buddyInfoFrame != null && MidletMain.mainFrame.buddyInfoFrame.reqSeqNum == reqSeqNum) {
   MidletMain.mainFrame.buddyInfoFrame.placeInfo(nickName, firstName, lastName, eMail, authFlag);
   }
   }*/
  /*public static void ssiComplete(IcqAccountRoot icqAccountRoot, int resultCode) {
   if (icqAccountRoot.session.isRequestSsi) {
   switch (resultCode) {
   case IcqPacketParser.SSI_NO_ERRORS: {
   showNotify(MidletMain.mainFrame, Localization.getMessage("NO_ERROR"), Localization.getMessage("SSI_NO_ERRORS"));
   try {
   IcqPacketSender.requestBuddyList(icqAccountRoot.session);
   } catch (IOException ex) {
   showNotify(MidletMain.mainFrame, Localization.getMessage("ERROR"), Localization.getMessage("ERR_IO_EXCEPTION"));
   LogUtil.outMessage("Can't request buddy list", true);
   ex.printStackTrace();
   }
   break;
   }
   case IcqPacketParser.SSI_NOT_FOUND: {
   showNotify(MidletMain.mainFrame, Localization.getMessage("ERROR"), Localization.getMessage("SSI_NOT_FOUND"));
   break;
   }
   case IcqPacketParser.SSI_ALR_EXIST: {
   showNotify(MidletMain.mainFrame, Localization.getMessage("ERROR"), Localization.getMessage("SSI_ALR_EXIST"));
   break;
   }
   case IcqPacketParser.SSI_ADD_ERROR: {
   showNotify(MidletMain.mainFrame, Localization.getMessage("ERROR"), Localization.getMessage("SSI_ADD_ERROR"));
   break;
   }
   case IcqPacketParser.SSI_LIMIT_EXC: {
   showNotify(MidletMain.mainFrame, Localization.getMessage("ERROR"), Localization.getMessage("SSI_LIMIT_EXC"));
   break;
   }
   case IcqPacketParser.SSI_TICQTOAIM: {
   showNotify(MidletMain.mainFrame, Localization.getMessage("ERROR"), Localization.getMessage("SSI_TICQTOAIM"));
   break;
   }
   case IcqPacketParser.SSI_AUTH_REQD: {
   showNotify(MidletMain.mainFrame, Localization.getMessage("ERROR"), Localization.getMessage("SSI_AUTH_REQD"));
   break;
   }
   }
   }
   icqAccountRoot.session.isRequestSsi = true;
   }*/
  public static void setBuddyList( AccountRoot accountRoot, Vector buddyList, Vector privateList, int privateBuddyId, int snacFlags, byte[] snacRequestId ) {
    LogUtil.outMessage( "setBuddyList(".concat( accountRoot.getUserId() ).concat( ")" ) );
    if ( accountRoot instanceof IcqAccountRoot && privateBuddyId != -1 ) {
      ( ( IcqAccountRoot ) accountRoot ).privateBuddyId = privateBuddyId;
      LogUtil.outMessage( "icqAccountRoot.privateBuddyId = " + privateBuddyId );
    }
    // LogUtil.outMessage("isReset = " +((IcqAccountRoot) accountRoot).isReset);
    // LogUtil.outMessage("snacFlags = " +snacFlags);
    accountRoot.setTreeItems( buddyList );
    if ( snacFlags == 0x00 ) {
      accountRoot.setPrivateItems( privateList );
      accountRoot.sortBuddyes();
      accountRoot.updateMainFrameBuddyList();
      MidletMain.screen.repaint();
      /** Reset flag **/
      LogUtil.outMessage( "Reset flag accepted" );
      if ( accountRoot instanceof IcqAccountRoot ) {
        ( ( IcqAccountRoot ) accountRoot ).isReset = true;
      }
      accountRoot.updateOfflineBuddylist();
      MidletMain.chatFrame.updateChatTabBuddyes();
    } else {
      if ( accountRoot instanceof IcqAccountRoot ) {
        ( ( IcqAccountRoot ) accountRoot ).isReset = false;
      }
    }
  }

  private static void playSound( int eventType ) {
    eventType--;
    if ( MidletMain.eventSound[eventType].length() == 0 ) {
      return;
    }
    try {
      LogUtil.outMessage( "Playing: " + MidletMain.defSoundLocation + MidletMain.eventSound[eventType] );
      InputStream is = Class.forName( "com.tomclaw.mandarin.main.MidletMain" ).getResourceAsStream( MidletMain.defSoundLocation + MidletMain.eventSound[eventType] );

      if ( is != null ) {
        final Player p;
        try {
          p = Manager.createPlayer( is, "audio/mpeg" );

          p.realize();

          VolumeControl volumeControl = ( VolumeControl ) p.getControl( "VolumeControl" );
          volumeControl.setLevel( MidletMain.volumeLevel );

          p.start();
          final long mediaTime = 1500; // This is default time to deallocate player
          Thread thread = new Thread() {
            public void run() {
              try {
                Thread.sleep( mediaTime );
                try {
                  // LogUtil.outMessage("Stop");
                  p.stop();
                } catch ( MediaException ex ) {
                  LogUtil.outMessage( ex.getMessage() );
                }
                p.close();
              } catch ( InterruptedException ex ) {
                LogUtil.outMessage( ex.getMessage() );
              }
            }
          };
          thread.start();
        } catch ( IOException ex ) {
          LogUtil.outMessage( ex.getMessage() );
        } catch ( MediaException ex ) {
          LogUtil.outMessage( ex.getMessage() );
        }
      }
    } catch ( Throwable ex1 ) {
      LogUtil.outMessage( ex1.getMessage() );
    }
  }

  private static void showDialog( final Window window, final String title,
          final String message, final boolean isError ) {
    Soft soft = new Soft( MidletMain.screen );
    soft.leftSoft = new PopupItem( Localization.getMessage( "CLOSE" ) ) {
      public void actionPerformed() {
        window.closeDialog();
        if ( isError ) {
          MidletMain.screen.setActiveWindow( MidletMain.mainFrame );
        }
      }
    };
    window.showDialog( new Dialog( MidletMain.screen, soft, title, message ) );
    MidletMain.screen.repaint();
  }
  
  public static void showDialog( String title, String message ) {
    showDialog( MidletMain.screen.activeWindow, title, message, false );
  }

  public static void showInfo( String message ) {
    showDialog( MidletMain.screen.activeWindow, 
            Localization.getMessage( "INFO_ITEM" ), message, false );
  }

  public static void showNotify( String message ) {
    showDialog( MidletMain.screen.activeWindow, 
            Localization.getMessage( "WARNING" ), message, false );
  }

  public static void showError( String message ) {
    showDialog( MidletMain.screen.activeWindow, 
            Localization.getMessage( "ERROR" ), message, false );
  }

  public static void showFail( String message ) {
    showDialog( MidletMain.screen.activeWindow, 
            Localization.getMessage( "ERROR" ), message, true );
  }

  public static void performTransferAction( final IcqAccountRoot icqAccountRoot, int ch2msgType, String buddyId, final int[] externalIp, final int dcTcpPort, boolean isViaRendezvousServer,
          long fileLength, byte[] fileName, final byte[] cookie, boolean isFromMsgDialog ) {

    IcqDirectConnection directConnection = ( IcqDirectConnection ) icqAccountRoot.getTransactionManager().getTransaction( cookie );
    if ( directConnection == null || directConnection.isReceivingFile ) {
      if ( directConnection == null ) {
        if ( MidletMain.isAutoAcceptFiles || isFromMsgDialog ) {
          directConnection = new IcqDirectConnection( icqAccountRoot );
          icqAccountRoot.getTransactionManager().addTransaction( directConnection );
        } else {
          showFileRequestMessage( icqAccountRoot, ch2msgType, buddyId, externalIp, dcTcpPort, isViaRendezvousServer,
                  fileLength, fileName, cookie );
          return;
        }
      }
      LogUtil.outMessage( "Receiving" );
      if ( ch2msgType == 0x0000 ) {
        directConnection.isReceivingFile = true;
        directConnection.buddyId = buddyId;
        directConnection.icbmCookie = cookie;
        if ( fileLength != -1 ) {
          directConnection.fileByteSize = fileLength;
          LogUtil.outMessage( "Accepted fileLength=" + fileLength );
        }
        if ( fileName != null ) {
          directConnection.fileName = fileName;
          LogUtil.outMessage( "Accepted fileName=".concat( StringUtil.byteArrayToString( directConnection.fileName, true ) ) );
        }
        updateTransactions( icqAccountRoot );
        if ( !ArrayUtil.equals( externalIp, new int[] { 0x00, 0x00, 0x00, 0x00 } ) && isViaRendezvousServer ) {
          // Proxy received
          LogUtil.outMessage( "Proxy received" );
          directConnection.proxyIp = externalIp[0] + "." + externalIp[1] + "." + externalIp[2] + "." + externalIp[3];
          directConnection.proxyIpBytes = new byte[ 4 ];
          DataUtil.put8( directConnection.proxyIpBytes, 0, externalIp[0] );
          DataUtil.put8( directConnection.proxyIpBytes, 1, externalIp[1] );
          DataUtil.put8( directConnection.proxyIpBytes, 2, externalIp[2] );
          DataUtil.put8( directConnection.proxyIpBytes, 3, externalIp[3] );
          directConnection.proxyPort = dcTcpPort;
          LogUtil.outMessage( "Data accepted" );
          if ( directConnection.remoteProxyConnectionSentFlag ) {
            try {
              directConnection.localProxyConnection.disconnect();
              directConnection.localProxyConnection = null;
            } catch ( IOException ex ) {
              ex.printStackTrace();
            }
          }
          Thread thread = new Thread() {
            public void run() {
              ( ( IcqDirectConnection ) icqAccountRoot.getTransactionManager().getTransaction( cookie ) ).sendToRemoteProxy( false );
            }
          };
          thread.start();
        } else {
          // Change connection to proxy
          LogUtil.outMessage( "Change connection to proxy" );
          try {
            directConnection.seqNumber++;
            directConnection.sendFileViaProxy();
            Thread thread = new Thread() {
              public void run() {
                ( ( IcqDirectConnection ) icqAccountRoot.getTransactionManager().getTransaction( cookie ) ).sendToRemoteProxy( true );
              }
            };
            thread.start();
            /** TEST **/
          } catch ( IOException ex ) {
            ex.printStackTrace();
          } catch ( InterruptedException ex ) {
            ex.printStackTrace();
          }
        }
      } else if ( ch2msgType == 0x0002 ) {
        // Ack
        LogUtil.outMessage( "Ack" );
        if ( !directConnection.remoteProxyConnectionSentFlag ) {
          Thread thread = new Thread() {
            public void run() {
              ( ( IcqDirectConnection ) icqAccountRoot.getTransactionManager().getTransaction( cookie ) ).sendToRemoteProxy( true );
            }
          };
          thread.start();
        }
      }
    } else {
      // Sending file
      if ( !ArrayUtil.equals( externalIp, new int[] { 0x00, 0x00, 0x00, 0x00 } ) ) {
        Thread thread = new Thread() {
          public void run() {
            ( ( IcqDirectConnection ) icqAccountRoot.getTransactionManager().getTransaction( cookie ) ).sendToRemoteProxy( externalIp, dcTcpPort );
          }
        };
        thread.start();
      } else {
        if ( ch2msgType == 0x0000 ) {
        } else if ( ch2msgType == 0x0002 ) {
          LogUtil.outMessage( "Incoming accept" );
          // Continuing
          // ...
          // directConnection.sendToCreatedProxy();
        }
      }
    }
  }

  public static void updateTransactionInfo( AccountRoot accountRoot, byte[] cookie ) {
    if ( accountRoot.getTransactionsFrame() != null && accountRoot.getTransactionsFrame().transactionItemFrame != null ) {
      if ( ArrayUtil.equals( accountRoot.getTransactionsFrame().transactionItemFrame.directConnection.getSessCookie(), cookie ) && MidletMain.screen.activeWindow.equals( accountRoot.getTransactionsFrame().transactionItemFrame ) ) {
        // Active frame is equals
        accountRoot.getTransactionsFrame().transactionItemFrame.updateData();
      }
    }
  }

  public static void updateTransactions( AccountRoot accountRoot ) {
    if ( accountRoot.getTransactionsFrame() != null ) {
      accountRoot.getTransactionsFrame().updateTransactions();
    } else if ( MidletMain.mainFrame.equals( MidletMain.screen.activeWindow ) ) {
      MidletMain.screen.repaint();
    }
  }

  public static void showFileRequestMessage( final IcqAccountRoot icqAccountRoot, final int ch2msgType, final String buddyId, final int[] externalIp, final int dcTcpPort, final boolean isViaRendezvousServer,
          final long fileLength, final byte[] fileName, final byte[] cookie ) {
    IncomingFileFrame incomingFileFrame = new IncomingFileFrame( icqAccountRoot, ch2msgType, buddyId, externalIp, dcTcpPort, isViaRendezvousServer, fileLength, fileName, cookie );

    icqAccountRoot.getServiceMessages().addMessage( buddyId, buddyId,
            Localization.getMessage( "FILE_NAME" ).concat( " \"" ).
            concat( StringUtil.byteArrayToString( fileName, true ) ).
            concat( "\" " ), ServiceMessages.TYPE_FILETRANSFER );

    incomingFileFrame.s_prevWindow = MidletMain.screen.activeWindow;
    MidletMain.screen.setActiveWindow( incomingFileFrame );
  }

  //public static void releaseMailInfo(MmpAccountRoot mmpAccountRoot, MailInfo mailInfo) {
  //}

  /*public static void setConnectionStage(MmpAccountRoot mmpAccountRoot, int i) {
   LogUtil.outMessage("setConnectionStage(".concat(mmpAccountRoot.userId).concat(": ").concat(String.valueOf(i)).concat(")"));
   MidletMain.mainFrame.getAccountTab(mmpAccountRoot.userId).fillPercent = 100 * i / 10;
   }*/

  /*public static void disconnectEvent(MmpAccountRoot mmpAccountRoot) {
   mmpAccountRoot.offlineAllBuddyes();
   mmpAccountRoot.updateMainFrameBuddyList();
   mmpAccountRoot.statusId = 0;
   MidletMain.mainFrame.updateAccountsStatus();
   }*/
  /*public static void setMailItems(MmpAccountRoot mmpAccountRoot, Vector buddyList) {
   LogUtil.outMessage("setBuddyList(".concat(mmpAccountRoot.userId).concat(")"));
   mmpAccountRoot.setTreeItems(buddyList);
   mmpAccountRoot.updateMainFrameBuddyList();
   mmpAccountRoot.sortBuddyes();
   MidletMain.screen.repaint();
   mmpAccountRoot.updateOfflineBuddylist();
   MidletMain.chatFrame.updateChatTabBuddyes();
   setConnectionStage(mmpAccountRoot, 10);
   }*/
  public static void setMailStatus( MmpAccountRoot mmpAccountRoot, String userMail, long userStatus ) {
    long prevBuddyStatus = mmpAccountRoot.getBuddyStatus( userMail );

    MmpItem mmpItem = mmpAccountRoot.setBuddyStatus( userMail, userStatus );
    if ( mmpItem != null ) {
      ChatTab chatTab = MidletMain.chatFrame.getChatTab( mmpAccountRoot, userMail, null, false );
      if ( chatTab != null ) {
        // chatTab.buddyItem = icqItem;
        chatTab.updateChatCaption();
      }
    }
    if ( ( prevBuddyStatus != 0 && userStatus == 0 ) || ( prevBuddyStatus == 0 && userStatus != 0 ) ) {
      LogUtil.outMessage( "Changing status in service messages" );
      mmpAccountRoot.getServiceMessages().addMessage( userMail, mmpItem != null ? mmpItem.getUserNick() : userMail,
              Localization.getMessage( "CHANGED_STATUS" ).concat( " \"" ).concat( Localization.getMessage( MmpStatusUtil.getStatusDescr( MmpStatusUtil.getStatusIndex( prevBuddyStatus ) ) ) ).
              concat( "\" " ).concat( Localization.getMessage( "TO" ) ).concat( " \"" ).concat( Localization.getMessage( MmpStatusUtil.getStatusDescr( MmpStatusUtil.getStatusIndex( userStatus ) ) ) ).
              concat( "\"" ), ServiceMessages.TYPE_STATUS_CHANGE );
    }
    if ( MidletMain.isSound ) {
      if ( prevBuddyStatus != 0 && userStatus == 0 ) {
        playSound( 0x04 );
      } else if ( prevBuddyStatus == 0 && userStatus != 0 ) {
        playSound( 0x03 );
      }
    }
  }

  /*public static void setBuddyTypingStatus(MmpAccountRoot mmpAccountRoot, String userMail) {
   MidletMain.mainFrame.typingNotify(mmpAccountRoot, userMail);
   ChatTab chatTab = MidletMain.chatFrame.getChatTab(mmpAccountRoot, userMail);
   if (chatTab != null) {
   chatTab.updateChatCaption();
   }
   MidletMain.screen.repaint();
   }*/

  /*public static void recMess(MmpAccountRoot mmpAccountRoot, String userMail, String messageText, byte[] cookie, int msgType) {
   LogUtil.outMessage("Received message from: ".concat(userMail));
   LogUtil.outMessage("Message text:          ".concat(messageText));
   ChatTab chatTab = MidletMain.chatFrame.getChatTab(mmpAccountRoot, userMail, false);
   BuddyItem buddyItem = MidletMain.mainFrame.getBuddyItem(mmpAccountRoot, userMail);
   if (chatTab == null) {
   LogUtil.outMessage("Chat tab not exist");
   if (buddyItem == null) {
   LogUtil.outMessage("BuddyItem is not exist. Creating...");
   buddyItem = new MmpItem(userMail);
   buddyItem.updateUiData();
    
   if (mmpAccountRoot.buddyItems.isEmpty()) {
   mmpAccountRoot.buddyItems.addElement(new IcqGroup("Temp group"));
   }
   LogUtil.outMessage("Group checking complete");
   ((GroupHeader) mmpAccountRoot.buddyItems.elementAt(0)).addChild((GroupChild) buddyItem);
    
   }
   LogUtil.outMessage("Done. Creating ChatTab instance");
   chatTab = new ChatTab(mmpAccountRoot, buddyItem, "/res/groups/img_mmpstatus.png".hashCode(), "/res/groups/img_chat.png".hashCode());
   LogUtil.outMessage("ChatTab appending...");
   MidletMain.chatFrame.addChatTab(chatTab, false);
   LogUtil.outMessage("Almost done.");
   } else {
   LogUtil.outMessage("Chat tab exist.        ".concat(chatTab.title));
   }
   boolean isDisplayed = MidletMain.chatFrame.addChatItem(chatTab, messageText, cookie, msgType, true);
   LogUtil.outMessage("isDisplayed = " + isDisplayed);
   if (MidletMain.screen.activeWindow.equals(MidletMain.chatFrame) && isDisplayed) {
    
   } else {
   buddyItem.setUnreadCount(buddyItem.getUnreadCount() + 1);
   mmpAccountRoot.unrMsgs++;
   MidletMain.mainFrame.updateAccountsStatus();
   if (buddyItem.getUnreadCount() == 1) {
   buddyItem.updateUiData();
   chatTab.updateChatCaption();
   }
   }
   boolean isExpandedFlag;
   if (MidletMain.isExpand
   && (!(Display.getDisplay(MidletMain.midletMain).getCurrent() instanceof javax.microedition.lcdui.TextBox)
   || Display.getDisplay(MidletMain.midletMain).getCurrent() == null)) {
   Display.getDisplay(MidletMain.midletMain).setCurrent(MidletMain.screen);
   isExpandedFlag = true;
   } else {
   isExpandedFlag = false;
   }
   if (MidletMain.vibrateDelay > 0) {
   if (isExpandedFlag) {
   try {
   Thread.sleep(400);
   } catch (InterruptedException ex) {
   // ex.printStackTrace();
   }
   }
   Display.getDisplay(MidletMain.midletMain).vibrate(MidletMain.vibrateDelay);
   }
   MidletMain.screen.repaint();
   if (MidletMain.isSound) {
   playSound(0x01);
   }
   }*/

  /*public static void msgAck(MmpAccountRoot mmpAccountRoot, byte[] temp) {
   LogUtil.outMessage("Message ack");
   MidletMain.chatFrame.msgAck(mmpAccountRoot, temp, ChatItem.DLV_STATUS_DELIVERED);
   MidletMain.screen.repaint();
   if (MidletMain.isSound) {
   playSound(0x02);
   }
   }*/
  /*public static void releaseQueue(MmpAccountRoot mmpAccountRoot, long contactId) {
   QueueAction queueAction = mmpAccountRoot.unregisterAction();
   if (queueAction != null && contactId != -1) {
   if (queueAction.getBuddyItem() != null) {
   ((MmpItem) queueAction.getBuddyItem()).contactId = contactId;
   }
   if (queueAction.getBuddyGroup() != null) {
   ((MmpGroup) queueAction.getBuddyGroup()).contactId = contactId;
   }
   queueAction.actionPerformed();
   }
   }*/
  public static void setMainFrameAction( MmpAccountRoot mmpAccountRoot, String message ) {
  }

  public static void showMainFrameMessage( MmpAccountRoot mmpAccountRoot, String message ) {
  }

  public static void setXmppStatus( XmppAccountRoot xmppAccountRoot, String from, String t_show, String t_status, String capsNode, String capsVer ) {
    XmppItem rosterItem = xmppAccountRoot.getBuddyItem( from );
    int prevBuddyStatus = XmppStatusUtil.offlineIndex;
    int statusIndex = XmppStatusUtil.getStatusIndex( t_show );

    if ( rosterItem == null ) {
      LogUtil.outMessage( "... not found." );
      rosterItem = new XmppItem( XmppSession.getClearJid( from ) );
      xmppAccountRoot.xmppSession.roster.put( XmppSession.getClearJid( from ), rosterItem );
      xmppAccountRoot.addTempItem( rosterItem );
      // add(rosterItem);
      // Main.mainFrame.updateRoster();
    } else {
      prevBuddyStatus = rosterItem.getResource( XmppSession.getJidResource( from ) ).status;
    }
    LogUtil.outMessage( "... present!" );
    // boolean isExistResource = rosterItem.isExistResource(getJidResource(from));
    rosterItem.getResource( XmppSession.getJidResource( from ) ).status = statusIndex;
    rosterItem.getResource( XmppSession.getJidResource( from ) ).statusText = t_status;
    rosterItem.getResource( XmppSession.getJidResource( from ) ).caps = capsNode;
    rosterItem.getResource( XmppSession.getJidResource( from ) ).ver = capsVer;
    rosterItem.updateUiData();

    ChatTab chatTab = MidletMain.chatFrame.getChatTab( xmppAccountRoot, XmppSession.getClearJid( from ), XmppSession.getJidResource( from ), false );
    if ( chatTab != null ) {
      LogUtil.outMessage( "Chat tab exist." );
      chatTab.buddyItem = rosterItem;
      if ( chatTab.resource != null ) {
        chatTab.resource = rosterItem.getResource( chatTab.resource.resource );
      }
      chatTab.updateChatCaption();
      LogUtil.outMessage( "Chat tab buddyItem updated." );
    } else {
      if ( statusIndex == XmppStatusUtil.offlineIndex
              && !XmppSession.getJidResource( from ).equals( "" )
              && MidletMain.isRemoveResources ) {
        rosterItem.removeResource( XmppSession.getJidResource( from ) );
        rosterItem.updateUiData();
      }
    }
    if ( ( prevBuddyStatus != XmppStatusUtil.offlineIndex && statusIndex == XmppStatusUtil.offlineIndex )
            || ( prevBuddyStatus == XmppStatusUtil.offlineIndex && statusIndex != XmppStatusUtil.offlineIndex ) ) {
      LogUtil.outMessage( "Changing status in service messages from: " + prevBuddyStatus + " to " + statusIndex );
      xmppAccountRoot.getServiceMessages().addMessage( from, rosterItem.isGroupChat ? XmppSession.getJidResource( from ) : rosterItem.getUserNick(),
              Localization.getMessage( "CHANGED_STATUS" ).concat( " \"" ).
              concat( Localization.getMessage( XmppStatusUtil.getStatusDescr( prevBuddyStatus ) ) ).
              concat( "\" " ).concat( Localization.getMessage( "TO" ) ).concat( " \"" ).
              concat( Localization.getMessage( XmppStatusUtil.getStatusDescr( statusIndex ) ) ).concat( "\"" ),
              ServiceMessages.TYPE_STATUS_CHANGE );
    }
    if ( MidletMain.isSound ) {
      if ( prevBuddyStatus != XmppStatusUtil.offlineIndex && statusIndex == XmppStatusUtil.offlineIndex ) {
        playSound( 0x04 );
      } else if ( prevBuddyStatus == XmppStatusUtil.offlineIndex && statusIndex != XmppStatusUtil.offlineIndex ) {
        playSound( 0x03 );
      }
    }
    MidletMain.screen.repaint();
  }

  public static void setServicesList( XmppAccountRoot xmppAccountRoot, Vector servicesList, String id ) {
    MidletMain.servicesFrame.setServicesList( xmppAccountRoot, servicesList, id );
  }

  public static void setServiceInfo( XmppAccountRoot xmppAccountRoot, String from, String id, Vector identityes, Vector features ) {
    MidletMain.servicesFrame.setServiceInfo( xmppAccountRoot, from, id, identityes, features );
  }

  public static void setHostInfo( XmppAccountRoot xmppAccountRoot, String id, Vector identityes, Vector features ) {
    MidletMain.servicesFrame.setHostInfo( xmppAccountRoot, id, identityes, features );
  }

  public static void setBookmarks( XmppAccountRoot xmppAccountRoot, String id, Vector bookmarks ) {
    MidletMain.bookmarksFrame.setBookmarks( xmppAccountRoot, id, bookmarks );
  }

  public static void setGroupChatSettings( XmppAccountRoot xmppAccountRoot, String id, Vector objects, String FORM_TYPE ) {
    MidletMain.groupChatConfFrame.setObjects( xmppAccountRoot, id, objects, FORM_TYPE );
  }

  public static void setGroupChatResult( XmppAccountRoot xmppAccountRoot, String id ) {
    MidletMain.groupChatConfFrame.setResult( xmppAccountRoot, id );
  }

  public static void setGroupChatResult( XmppAccountRoot xmppAccountRoot, String id, Vector items ) {
    MidletMain.groupChatUsersFrame.setResult( xmppAccountRoot, id, items );
  }

  public static void setBookmarkError( XmppAccountRoot xmppAccountRoot, String id, String errorText, int errorId ) {
    MidletMain.bookmarksFrame.setBookmarkError( xmppAccountRoot, id, errorText, errorId );
  }

  public static void setAffiliationAddResult( XmppAccountRoot xmppAccountRoot, String id ) {
    MidletMain.affiliationAddFrame.setRequestResult( xmppAccountRoot, id );
  }

  public static void processQueueAction( AccountRoot accountRoot, Cookie cookie, Hashtable params ) {
    LogUtil.outMessage( "processQueueAction for: " + cookie.cookieString );
    Queue.runQueueAction( cookie, params );
    MidletMain.screen.repaint();
  }

  public static void cancelQueueAction( AccountRoot accountRoot, Cookie cookie, String errorStringCode ) {
    LogUtil.outMessage( "cancelQueueAction for: " + cookie.cookieString );
    Queue.popQueueAction( cookie );
    showNotify( Localization.getMessage( errorStringCode ) );
  }
}
