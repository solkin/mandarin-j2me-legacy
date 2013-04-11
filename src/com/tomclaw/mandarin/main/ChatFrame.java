package com.tomclaw.mandarin.main;

import com.tomclaw.mandarin.core.AccountRoot;
import com.tomclaw.mandarin.core.BuddyItem;
import com.tomclaw.mandarin.xmpp.XmppAccountRoot;
import com.tomclaw.mandarin.xmpp.XmppItem;
import com.tomclaw.mandarin.xmpp.XmppStatusUtil;
import com.tomclaw.tcuilite.Screen;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.tcuilite.smiles.Smiles;
import com.tomclaw.utils.ArrayUtil;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;
import com.tomclaw.utils.TimeUtil;
import java.util.Vector;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class ChatFrame extends Window {

  public Tab chatTabs;
  public Pane chatPane;
  public TextBox textBox;
  public int maxSize = 2048;
  public PopupItem hyperlinkPopupItem;
  public PopupItem authAcceptPopupItem;
  public PopupItem replyItem;
  public char[] http_den_sym = new char[]{ ' ', '\n', '\r', '\t', '!', '"',
    '#', '\'', '*', ',', ';', '<', '>', '[', ']', '^', '`', '{', '|', '}' };

  public ChatFrame() {
    super( MidletMain.screen );

    this.addKeyEvent( new KeyEvent( 0, "KEY_WRITE", true ) {

      public void actionPerformed() {
        ChatTab chatTab = getSelectedChatTab();
        if ( chatTab == null ) {
          return;
        }
        textBox.setTitle( chatTab.title );
        chatTab.accountRoot.sendTypingStatus( chatTab.buddyItem.getUserId(), true );
        Display.getDisplay( MidletMain.midletMain ).setCurrent( textBox );

      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_COPY", true ) {

      public void actionPerformed() {
        /**
         * Message deoptimization
         */
        if ( !chatPane.items.isEmpty() && chatPane.psvLstFocusedIndex >= 0 && chatPane.psvLstFocusedIndex < chatPane.items.size() ) {
          ChatItem chatItem = ( ( ChatItem ) chatPane.items.elementAt( chatPane.psvLstFocusedIndex ) );
          MidletMain.buffer = "[".concat( chatItem.buddyNick ).concat( "]\n " ).concat( chatItem.itemDateTime ).concat( " \n" ).concat( getMessageText( chatItem ) );
        }
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_PASTE", true ) {

      public void actionPerformed() {
        ChatTab chatTab = getSelectedChatTab();
        if ( chatTab == null ) {
          return;
        }
        textBox.setTitle( chatTab.title );
        chatTab.accountRoot.sendTypingStatus( chatTab.buddyItem.getUserId(), true );
        /**
         * Pasting text
         */
        textBox.setString( MidletMain.buffer );
        Display.getDisplay( MidletMain.midletMain ).setCurrent( textBox );
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_APPEND", true ) {

      public void actionPerformed() {
        if ( !chatPane.items.isEmpty() && chatPane.psvLstFocusedIndex >= 0 && chatPane.psvLstFocusedIndex < chatPane.items.size() ) {
          ChatItem chatItem = ( ( ChatItem ) chatPane.items.elementAt( chatPane.psvLstFocusedIndex ) );
          MidletMain.buffer += "\n[".concat( chatItem.buddyNick ).concat( "]\n " ).concat( chatItem.itemDateTime ).concat( " \n" ).concat( getMessageText( chatItem ) );
        }
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_CLEARCHAT", true ) {

      public void actionPerformed() {
        ( ( ChatTab ) ChatFrame.this.chatTabs.items.elementAt( ChatFrame.this.chatTabs.selectedIndex ) ).chatItems.removeAllElements();
        System.gc();
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_CLOSECHAT", true ) {

      public void actionPerformed() {
        ChatTab chatTab = ( ( ChatTab ) ChatFrame.this.chatTabs.items.elementAt( ChatFrame.this.chatTabs.selectedIndex ) );
        if ( chatTab.buddyItem instanceof XmppItem && !chatTab.resource.resource.equals( "" ) && MidletMain.isRemoveResources ) {
          if ( chatTab.resource.statusIndex == XmppStatusUtil.offlineIndex ) {
            ( ( XmppItem ) chatTab.buddyItem ).removeResource( chatTab.resource.resource );
            ( ( XmppItem ) chatTab.buddyItem ).updateUiData();
          }
        }
        chatTab.chatItems = null;
        ChatFrame.this.removeChatTab( ChatFrame.this.chatTabs.selectedIndex );
        /**
         * Checking
         */
        if ( chatTabs.items.isEmpty() ) {
          MidletMain.screen.setActiveWindow( s_prevWindow );
          s_prevWindow.s_nextWindow = null;
          return;
        }
        /**
         * Switch to exist dialog
         */
        if ( chatTabs.selectedIndex >= chatTabs.items.size() - 1 ) {
          chatTabs.tabEvent.stateChanged( chatTabs.selectedIndex, chatTabs.items.size() - 1, chatTabs.items.size() );
          chatTabs.selectedIndex = chatTabs.items.size() - 1;
        } else {
          chatTabs.tabEvent.stateChanged( chatTabs.selectedIndex, chatTabs.selectedIndex, chatTabs.items.size() );
        }
        if ( chatTabs.totalWidth > MidletMain.screen.getWidth() ) {
          chatTabs.xOffset = chatTabs.totalWidth - MidletMain.screen.getWidth();
        } else {
          chatTabs.xOffset = 0;
        }
        System.gc();
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_REPLY", true ) {

      public void actionPerformed() {
        if ( !chatPane.items.isEmpty() && chatPane.psvLstFocusedIndex >= 0 && chatPane.psvLstFocusedIndex < chatPane.items.size() ) {
          ChatItem chatItem = ( ( ChatItem ) chatPane.items.elementAt( chatPane.psvLstFocusedIndex ) );
          ChatTab chatTab = getSelectedChatTab();
          if ( chatTab == null ) {
            return;
          }
          textBox.setTitle( chatTab.title );
          /**
           * Pasting text
           */
          textBox.setString( chatItem.buddyNick.concat( ": " ) );
          Display.getDisplay( MidletMain.midletMain ).setCurrent( textBox );
        }
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_CHAT_SCREEN_TOP", true ) {

      public void actionPerformed() {
        chatPane.yOffset = 0;
        if ( !chatPane.items.isEmpty() ) {
          chatPane.setFocused( 0 );
        }
      }
    } );
    this.addKeyEvent( new KeyEvent( 0, "KEY_CHAT_SCREEN_BOTTOM", true ) {

      public void actionPerformed() {
        int maxOffset = chatPane.getTotalHeight() - chatPane.height;
        if ( maxOffset < 0 ) {
          maxOffset = 0;
        }
        chatPane.yOffset = maxOffset;
        if ( !chatPane.items.isEmpty() ) {
          chatPane.setFocused( chatPane.items.size() - 1 );
        }
      }
    } );

    textBox = new TextBox( "", "", maxSize, TextField.ANY );
    textBox.addCommand( new Command( Localization.getMessage( "SEND" ), Command.OK, 4 ) );
    textBox.addCommand( new Command( Localization.getMessage( "BACK" ), Command.BACK, 3 ) );
    textBox.addCommand( new Command( Localization.getMessage( "SMILES" ), Command.HELP, 2 ) );
    textBox.addCommand( new Command( Localization.getMessage( "CLEAR" ), Command.EXIT, 1 ) );
    textBox.setCommandListener( new CommandListener() {

      public void commandAction( Command c, Displayable d ) {
        switch ( c.getCommandType() ) {
          case Command.OK: {
            ChatTab chatTab = getSelectedChatTab();
            if ( chatTab == null ) {
              return;
            }
            if ( !StringUtil.isEmptyOrNull( textBox.getString() ) ) {
              if ( chatTab.accountRoot.getStatusIndex() != -1 ) {
                /**
                 * Account is online
                 */
                byte[] msgCookie;
                msgCookie = chatTab.accountRoot.sendMessage( chatTab.buddyItem, textBox.getString(), chatTab.resource == null ? "" : chatTab.resource.resource );
                if ( chatTab.buddyItem instanceof XmppItem
                        && ( ( XmppItem ) chatTab.buddyItem ).isGroupChat
                        && ( chatTab.resource == null || chatTab.resource.resource.length() == 0 ) ) {
                } else {
                  ChatFrame.this.addChatItem( chatTab, null, textBox.getString(), msgCookie, ChatItem.TYPE_PLAIN_MSG, false );
                }
                textBox.setString( "" );
              }
            }
            ChatFrame.this.prepareGraphics();
            MidletMain.screen.activeWindow = MidletMain.chatFrame;
            Display.getDisplay( MidletMain.midletMain ).setCurrent( MidletMain.screen );
            MidletMain.screen.setFullScreenMode( true );
            chatTab.accountRoot.sendTypingStatus( chatTab.buddyItem.getUserId(), false );
            break;
          }
          case Command.EXIT: {
            textBox.setString( "" );
            break;
          }
          case Command.BACK: {
            MidletMain.screen.activeWindow = MidletMain.chatFrame;
            ChatTab chatTab = getSelectedChatTab();
            if ( chatTab == null ) {
              return;
            }
            ChatFrame.this.prepareGraphics();
            Display.getDisplay( MidletMain.midletMain ).setCurrent( MidletMain.screen );
            MidletMain.screen.setFullScreenMode( true );
            chatTab.accountRoot.sendTypingStatus( chatTab.buddyItem.getUserId(), false );
            break;
          }
          case Command.HELP: {
            if ( MidletMain.smilesFrame == null ) {
              MidletMain.smilesFrame = new SmilesFrame();
            }
            Display.getDisplay( MidletMain.midletMain ).setCurrent( MidletMain.screen );
            MidletMain.smilesFrame.prepareGraphics();
            MidletMain.screen.setFullScreenMode( true );
            MidletMain.screen.activeWindow = MidletMain.smilesFrame;
            MidletMain.screen.repaint( Screen.REPAINT_STATE_PLAIN );
            break;
          }
        }
      }
    } );
    soft = new Soft( MidletMain.screen );
    /**
     * Left soft items
     */
    hyperlinkPopupItem = new PopupItem( Localization.getMessage( "GO_TO_HYPERLINK" ) );
    replyItem = new PopupItem( Localization.getMessage( "REPLY" ) ) {

      public void actionPerformed() {
        ChatFrame.this.getKeyEvent( "KEY_REPLY" ).actionPerformed();
      }
    };
    authAcceptPopupItem = new PopupItem( Localization.getMessage( "AUTH_ACCEPT" ) ) {

      public void actionPerformed() {
        if ( !chatPane.items.isEmpty() && chatPane.psvLstFocusedIndex >= 0 && chatPane.psvLstFocusedIndex < chatPane.items.size() ) {
          ChatTab chatTab = getSelectedChatTab();
          if ( chatTab != null ) {
            chatTab.accountRoot.acceptAuthorization( chatTab.buddyItem );
          }
        }
      }
    };
    replyItem = new PopupItem( Localization.getMessage( "REPLY" ) ) {

      public void actionPerformed() {
        ChatFrame.this.getKeyEvent( "KEY_REPLY" ).actionPerformed();
      }
    };
    soft.leftSoft = new PopupItem( Localization.getMessage( "MENU" ) ) {

      public void actionPerformed() {
        /**
         * Links
         */
        boolean isLinkPresent = false;
        if ( !hyperlinkPopupItem.isEmpty() ) {
          hyperlinkPopupItem.subPopup.items.removeAllElements();
          hyperlinkPopupItem.subPopup.selectedIndex = 0;
        }
        ChatItem chatItem = null;
        if ( !chatPane.items.isEmpty() && chatPane.psvLstFocusedIndex >= 0
                && chatPane.psvLstFocusedIndex < chatPane.items.size() ) {
          chatItem = ( ( ChatItem ) chatPane.items.elementAt(
                  chatPane.psvLstFocusedIndex ) );
          /**
           * Links recognition
           */
          int linkPoint;
          int offset = 0;
          String msgText = chatItem.bbResult.originalString;
          while ( ( linkPoint = msgText.indexOf( "http://", offset ) ) != -1 ) {
            int linkEnd = msgText.length();
            int t_linkEnd;
            for ( int c = 0; c < http_den_sym.length; c++ ) {
              t_linkEnd = Math.min( linkEnd, msgText.indexOf( http_den_sym[c], linkPoint ) );
              if ( t_linkEnd > 0 ) {
                linkEnd = t_linkEnd;
              }
            }
            if ( linkPoint < 0 ) {
              linkPoint = 0;
            }
            final String hyperlink = msgText.substring( linkPoint, linkEnd );
            offset = linkEnd;
            if ( hyperlink.length() < 256 ) {
              isLinkPresent = true;
              hyperlinkPopupItem.addSubItem( new PopupItem( hyperlink ) {

                public void actionPerformed() {
                  try {
                    MidletMain.midletMain.platformRequest( hyperlink );
                  } catch ( ConnectionNotFoundException ex ) {
                    LogUtil.outMessage( "Unable to go to " + hyperlink + " cause " + ex.getMessage() );
                  }
                }
              } );
              LogUtil.outMessage( hyperlink );
            }
          }
        }
        /**
         * Adding / removing shortcut links depending on the type of message
         */
        // Link jump
        if ( soft.leftSoft.subPopup.items.contains( hyperlinkPopupItem ) ) {
          if ( !isLinkPresent ) {
            soft.leftSoft.subPopup.items.removeElement( hyperlinkPopupItem );
          }
        } else {
          if ( isLinkPresent ) {
            soft.leftSoft.subPopup.items.insertElementAt( hyperlinkPopupItem, 1 );
          }
        }
        // Authorization accept
        if ( soft.leftSoft.subPopup.items.contains( authAcceptPopupItem ) ) {
          if ( chatItem == null
                  || chatItem.itemType != ChatItem.TYPE_AUTH_REQ_MSG ) {
            soft.leftSoft.subPopup.items.removeElement( authAcceptPopupItem );
          }
        } else {
          if ( chatItem != null
                  && chatItem.itemType == ChatItem.TYPE_AUTH_REQ_MSG ) {
            soft.leftSoft.subPopup.items.insertElementAt( authAcceptPopupItem, 1 );
          }
        }
        /**
         * Answer item
         */
        ChatTab chatTab = getSelectedChatTab();
        if ( chatTab != null ) {
          if ( chatTab.buddyItem instanceof XmppItem
                  && ( ( XmppItem ) chatTab.buddyItem ).isGroupChat ) {
            if ( !soft.leftSoft.subPopup.items.contains( replyItem ) ) {
              soft.leftSoft.addSubItem( replyItem );
            }
          } else {
            if ( !soft.leftSoft.isEmpty() ) {
              soft.leftSoft.subPopup.items.removeElement( replyItem );
            }
          }
        }
      }
    };
    soft.leftSoft.addSubItem( new PopupItem( Localization.getMessage( "WRITE" ) ) {

      public void actionPerformed() {
        ChatFrame.this.getKeyEvent( "KEY_WRITE" ).actionPerformed();
      }
    } );
    soft.leftSoft.addSubItem( new PopupItem( Localization.getMessage( "CLEAR" ) ) {

      public void actionPerformed() {
        ChatFrame.this.getKeyEvent( "KEY_CLEARCHAT" ).actionPerformed();
      }
    } );
    soft.leftSoft.addSubItem( new PopupItem( Localization.getMessage( "COPY" ) ) {

      public void actionPerformed() {
        ChatFrame.this.getKeyEvent( "KEY_COPY" ).actionPerformed();
      }
    } );
    soft.leftSoft.addSubItem( new PopupItem( Localization.getMessage( "APPEND" ) ) {

      public void actionPerformed() {
        ChatFrame.this.getKeyEvent( "KEY_APPEND" ).actionPerformed();
      }
    } );
    soft.leftSoft.addSubItem( new PopupItem( Localization.getMessage( "PASTE" ) ) {

      public void actionPerformed() {
        ChatFrame.this.getKeyEvent( "KEY_PASTE" ).actionPerformed();
      }
    } );
    soft.leftSoft.addSubItem( new PopupItem( Localization.getMessage( "CLOSE" ) ) {

      public void actionPerformed() {
        ChatFrame.this.getKeyEvent( "KEY_CLOSECHAT" ).actionPerformed();
      }
    } );
    /**
     * Right soft items
     */
    soft.rightSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {

      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( MidletMain.mainFrame );
      }
    };

    chatTabs = new Tab( screen );
    chatTabs.tabEvent = new TabEvent() {

      public void stateChanged( int prevIndex, int currIndex, int totlItems ) {
        try {
          /**
           * Saving previous yOffset
           */
          LogUtil.outMessage( "totlItems = " + totlItems );
          LogUtil.outMessage( "prevIndex = " + prevIndex );
          /**
           * Checking for null-type of chat
           */
          ChatTab t_ChatTab = ( ( ChatTab ) chatTabs.items.elementAt( currIndex ) );
          if ( t_ChatTab.chatItems == null ) {
            t_ChatTab.chatItems = new Vector();
          }
          /**
           * Applying items
           */
          chatPane.items = t_ChatTab.chatItems;
          /**
           * Applying scroll flag
           */
          ChatFrame.this.prepareGraphics();
          if ( chatPane.getTotalHeight() > chatPane.getHeight() ) {
            chatPane.yOffset = chatPane.getTotalHeight() - chatPane.getHeight();
          } else {
            chatPane.yOffset = 0;
          }
          /**
           * Focusing 
           */
          if ( !chatPane.items.isEmpty() ) {
            chatPane.setFocused( chatPane.items.size() - 1 );
          }
          LogUtil.outMessage( "Focused index: " + chatPane.getFocused() );
          /**
           * Reset unread messages count
           */
          String resource = null;
          if ( t_ChatTab.resource != null ) {
            resource = t_ChatTab.resource.resource;
          }
          t_ChatTab.accountRoot.setUnrMsgs(
                  t_ChatTab.accountRoot.getUnrMsgs() - t_ChatTab.buddyItem.getUnreadCount( resource ) );
          MidletMain.mainFrame.updateAccountsStatus();
          t_ChatTab.buddyItem.setUnreadCount( 0, resource );
          t_ChatTab.buddyItem.updateUiData();
          t_ChatTab.updateChatCaption();
          screen.repaint();
        } catch ( Throwable ex1 ) {
          LogUtil.outMessage( "chatTabs.tabEvent: " + ex1.getMessage() );
        }
      }
    };
    chatPane = new Pane( this, true );
    chatPane.setTouchOrientation( MidletMain.screen.isPointerEvents );
    chatPane.moveStep = Theme.font.getHeight() * 3;
    chatPane.actionPerformedEvent = new PaneEvent() {

      public void actionPerformed( PaneObject po ) {
        ChatTab chatTab = getSelectedChatTab();
        if ( chatTab == null ) {
          return;
        }
        textBox.setTitle( chatTab.title );
        chatTab.accountRoot.sendTypingStatus( chatTab.buddyItem.getUserId(), true );
        Display.getDisplay( MidletMain.midletMain ).setCurrent( textBox );
        textBox.getCaretPosition();
      }
    };

    chatTabs.setGObject( chatPane );

    setGObject( chatTabs );
  }

  public void addChatTab( ChatTab chatTab, boolean isSwitchTo ) {
    chatTabs.addTabItem( chatTab );
    chatTab.buddyItem.updateUiData();
    if ( isSwitchTo ) {
      chatTabs.tabEvent.stateChanged( chatTabs.selectedIndex, chatTabs.items.size() - 1, chatTabs.items.size() );
      chatTabs.selectedIndex = chatTabs.items.size() - 1;
      if ( chatTabs.totalWidth > MidletMain.screen.getWidth() ) {
        /**
         * Tab invisible, must be scrolled to
         */
        chatTabs.xOffset = chatTabs.totalWidth - MidletMain.screen.getWidth();
      }
    }
    /**
     * Settings next window
     */
    MidletMain.mainFrame.s_nextWindow = this;
  }

  public void removeChatTab( ChatTab chatTab ) {
    chatTab.buddyItem.updateUiData();
    chatTabs.items.removeElement( chatTab );
  }

  public void removeChatTab( int index ) {
    try {
      ChatTab chatTab = ( ( ChatTab ) chatTabs.items.elementAt( index ) );
      chatTabs.items.removeElementAt( index );
      chatTab.buddyItem.updateUiData();
    } catch ( Throwable ex1 ) {
    }
  }

  public ChatTab getChatTab( AccountRoot accountRoot, String buddyId, String resource, boolean isSwitchTo ) {
    ChatTab chatTab;
    for ( int c = 0; c < chatTabs.items.size(); c++ ) {
      chatTab = ( ChatTab ) chatTabs.items.elementAt( c );
      if ( chatTab.accountRoot.equals( accountRoot ) && chatTab.buddyItem.getUserId().equals( buddyId )
              && ( ( resource != null && ( ( chatTab.resource != null && chatTab.resource.resource.equals( resource ) ) || ( chatTab.resource == null && resource.equals( "" ) ) ) ) || resource == null ) ) {
        if ( isSwitchTo ) {
          chatTabs.tabEvent.stateChanged( chatTabs.selectedIndex, c, chatTabs.items.size() );
          chatTabs.selectedIndex = c;
          if ( chatTabs.totalWidth > MidletMain.screen.getWidth() ) {
            /**
             * Right corner
             */
            if ( chatTab.x + chatTab.width > chatTabs.xOffset + MidletMain.screen.getWidth() ) {
              chatTabs.xOffset = chatTab.x + chatTab.width - MidletMain.screen.getWidth();
            }
            /**
             * Left corner
             */
            if ( chatTab.x < chatTabs.xOffset ) {
              chatTabs.xOffset = chatTab.x;
            }
          }
        }
        return chatTab;
      }
    }
    return null;
  }

  public boolean addChatItem( ChatTab chatTab, String groupChatNick, String decMsg, byte[] msgCookie, int type, boolean isIncoming ) {
    decMsg = StringUtil.replace( decMsg, "[", "\\[" );
    decMsg = StringUtil.replace( decMsg, "]", "\\]" );
    decMsg = Smiles.replaceSmilesForCodes( decMsg );
    decMsg = StringUtil.replace( decMsg, "\n", "[br/]" );
    if ( groupChatNick != null && chatTab.accountRoot instanceof XmppAccountRoot ) {
      /**
       * Special addition for XMPP chat-rooms
       */
      if ( decMsg.startsWith( "/me" ) ) {
        decMsg = "[c=purple][i]".concat( groupChatNick ).concat( decMsg.substring( "/me".length(), decMsg.length() ) ).concat( "[/i][/c]" );
      } else {
        String groupChatRealNick = ( ( XmppItem ) chatTab.buddyItem ).groupChatNick;
        if ( decMsg.indexOf( groupChatRealNick ) != -1 ) {
          decMsg = "[c=red][b]".concat( decMsg ).concat( "[/b][/c]" );
        }
      }
    }
    decMsg = "[p]".concat( decMsg ).concat( "[/p]" );
    LogUtil.outMessage( "Message: " + decMsg );
    ChatItem chatItem = new com.tomclaw.tcuilite.ChatItem( chatPane, decMsg );
    chatItem.dlvStatus = isIncoming ? ChatItem.DLV_STATUS_INCOMING : ChatItem.DLV_STATUS_NOT_SENT;
    chatItem.cookie = msgCookie;
    chatItem.itemType = type;
    if ( groupChatNick == null ) {
      if ( chatTab.buddyItem instanceof XmppItem && ( ( XmppItem ) ( chatTab.buddyItem ) ).isGroupChat && chatTab.resource != null ) {
        chatItem.buddyNick = isIncoming ? chatTab.resource.resource : ( ( XmppItem ) ( chatTab.buddyItem ) ).groupChatNick;
        chatItem.buddyId = isIncoming ? chatTab.resource.resource : ( ( XmppItem ) ( chatTab.buddyItem ) ).groupChatNick;
      } else {
        chatItem.buddyNick = isIncoming ? chatTab.buddyItem.getUserNick() : chatTab.accountRoot.getUserNick();
        chatItem.buddyId = isIncoming ? chatTab.buddyItem.getUserId() : chatTab.accountRoot.getUserId();
      }
    } else {
      chatItem.buddyNick = groupChatNick;
      chatItem.buddyId = groupChatNick;
    }
    // To history must be saved whole date 
    chatItem.itemDateTime = TimeUtil.getTimeString( TimeUtil.getCurrentTimeGMT(), false );

    /**
     * History *
     */
    if ( MidletMain.isStoreHistory && !MidletMain.getBoolean( MidletMain.uniquest,
            chatTab.accountRoot.getAccType() + chatItem.buddyId.hashCode(),
            "DISABLE_HISTORY" ) ) {
      LogUtil.outMessage( "History storing..." );
      saveToHistory( chatTab.accountRoot.getAccType(), chatTab.buddyItem.getUserId()/*
               * +
               * (chatTab.resource == null ? "" :
               * String.valueOf(chatTab.resource.resource.hashCode()))
               */, chatItem );
    } else {
      LogUtil.outMessage( "History storing disabled" );
    }

    /**
     * Checking chatTab for null-type
     */
    if ( chatTab.chatItems == null ) {
      chatTab.chatItems = new Vector();
    }
    /** Focus chatItem **/
    chatTab.chatItems.addElement( chatItem );
    MidletMain.chatFrame.prepareGraphics();
    /** Scrolling to the last message **/
    ChatTab tempChatTab = getSelectedChatTab();

    LogUtil.outMessage( "Focused index: " + chatPane.psvLstFocusedIndex );
    LogUtil.outMessage( "Items total:   " + chatPane.items.size() );
    if ( tempChatTab != null && tempChatTab.equals( chatTab ) && chatPane.psvLstFocusedIndex == chatPane.items.size() - 2 || chatPane.items.size() == 1 ) {
      /** This tab is active **/
      if ( chatPane.getTotalHeight() > chatPane.getHeight() ) {
        chatPane.yOffset = chatPane.getTotalHeight() - chatPane.getHeight();
      } else {
        chatPane.yOffset = 0;
      }
      if ( !chatPane.items.isEmpty() ) {
        chatPane.setFocused( chatPane.items.size() - 1 );
      } else {
        chatItem.setFocused( true );
      }
    }
    MidletMain.screen.repaint();
    try {
      if ( tempChatTab != null && tempChatTab.equals( chatTab ) ) {
        return true;
      }
    } catch ( Throwable ex1 ) {
    }
    return false;
  }

  public void msgAck( AccountRoot accountRoot, String buddyId, String resource, byte[] msgCookie, byte dlvStatus ) {
    ChatTab chatTab;
    chatTab = getChatTab( accountRoot, buddyId, resource );
    if ( chatTab != null ) {
      for ( int i = 0; i < chatTab.chatItems.size(); i++ ) {
        if ( ArrayUtil.equals( ( ( ChatItem ) chatTab.chatItems.elementAt( i ) ).cookie, msgCookie ) ) {
          ( ( ChatItem ) chatTab.chatItems.elementAt( i ) ).dlvStatus = dlvStatus;
          return;
        }
      }
    }
  }

  public void msgAck( AccountRoot accountRoot, byte[] msgCookie, byte dlvStatus ) {
    ChatTab chatTab;
    for ( int c = 0; c < chatTabs.items.size(); c++ ) {
      chatTab = ( ( ChatTab ) chatTabs.items.elementAt( c ) );
      if ( chatTab.accountRoot.equals( accountRoot ) ) {
        for ( int i = 0; i < chatTab.chatItems.size(); i++ ) {
          if ( ArrayUtil.equals( ( ( ChatItem ) chatTab.chatItems.elementAt( i ) ).cookie, msgCookie ) ) {
            ( ( ChatItem ) chatTab.chatItems.elementAt( i ) ).dlvStatus = dlvStatus;
            return;
          }
        }
      }
    }
  }

  public ChatTab getChatTab( AccountRoot accountRoot, String buddyId, String resource ) {
    return getChatTab( accountRoot, buddyId, resource, false );
  }

  public ChatTab getChatTab( String buddyId ) {
    /**
     * Warning! Tabs from any AccountRoot could compare
     */
    ChatTab chatTab;
    for ( int c = 0; c < chatTabs.items.size(); c++ ) {
      chatTab = ( ( ChatTab ) chatTabs.items.elementAt( c ) );
      if ( chatTab.buddyItem.getUserId().equals( buddyId ) ) {
        return chatTab;
      }
    }
    return null;
  }

  public ChatTab getSelectedChatTab() {
    try {
      return ( ( ChatTab ) chatTabs.items.elementAt( chatTabs.selectedIndex ) );
    } catch ( Throwable ex1 ) {
      return null;
    }
  }

  public void updateChatTabBuddyes() {
    ChatTab chatTab;
    Vector buddyItems;
    GroupHeader groupHeader;
    boolean nextTab;
    for ( int c = 0; c < chatTabs.items.size(); c++ ) {
      nextTab = false;
      chatTab = ( ( ChatTab ) chatTabs.items.elementAt( c ) );
      buddyItems = chatTab.accountRoot.getBuddyItems();
      for ( int i = 0; i < buddyItems.size(); i++ ) {
        groupHeader = ( GroupHeader ) buddyItems.elementAt( i );
        for ( int j = 0; j < groupHeader.getChildsCount(); j++ ) {
          if ( ( ( BuddyItem ) groupHeader.getChilds().elementAt( j ) ).getUserId().equals( chatTab.buddyItem.getUserId() ) ) {
            chatTab.buddyItem = ( BuddyItem ) groupHeader.getChilds().elementAt( j );
            LogUtil.outMessage( chatTab.buddyItem.getUserNick() + " (" + chatTab.buddyItem.getUserId() + ") updated in chat frame" );
            nextTab = true;
            break;
          }
        }
        if ( nextTab ) {
          break;
        }
      }
    }
  }

  private void saveToHistory( String accType, String userId, ChatItem chatItem ) {
    try {
      RecordStore recordStore = RecordStore.openRecordStore( accType + userId.hashCode() + ".his", true );
      byte[] data = MidletMain.historyRmsRenderer.getRmsData( chatItem );
      recordStore.addRecord( data, 0, data.length );
      recordStore.closeRecordStore();
    } catch ( RecordStoreException ex ) {
      LogUtil.outMessage( ChatFrame.this.getClass(), ex );
    }
  }

  public static String getMessageText( ChatItem chatItem ) {
    return chatItem.bbResult.originalString;
  }
}
