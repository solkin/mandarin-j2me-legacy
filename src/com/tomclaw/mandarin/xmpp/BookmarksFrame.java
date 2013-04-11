package com.tomclaw.mandarin.xmpp;

import com.tomclaw.mandarin.core.Handler;
import com.tomclaw.mandarin.main.ChatTab;
import com.tomclaw.mandarin.main.InfoFrame;
import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import java.io.IOException;
import java.util.Vector;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class BookmarksFrame extends Window {

  private XmppAccountRoot xmppAccountRoot;
  private List bookmarksList;
  private String requestId = "";
  private Bookmark bookmark;

  public BookmarksFrame( final XmppAccountRoot xmppAccountRoot ) {
    super( MidletMain.screen );
    this.xmppAccountRoot = xmppAccountRoot;
    /** Header **/
    header = new Header( Localization.getMessage( "BOOKMARKS_FRAME" ) );
    /** Creating soft **/
    soft = new Soft( MidletMain.screen );
    /** Left soft items **/
    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {

      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    /** Right soft items **/
    soft.rightSoft = new PopupItem( Localization.getMessage( "MENU" ) );
    soft.rightSoft.addSubItem( new PopupItem( Localization.getMessage( "ADD_BOOKMARK" ) ) {

      public void actionPerformed() {
        BookmarkEditorFrame bookmarkEditorFrame = new BookmarkEditorFrame( null );
        bookmarkEditorFrame.s_prevWindow = BookmarksFrame.this;
        MidletMain.screen.setActiveWindow( bookmarkEditorFrame );
      }
    } );
    soft.rightSoft.addSubItem( new PopupItem( Localization.getMessage( "EDIT_BOOKMARK" ) ) {

      public void actionPerformed() {
        if ( bookmarksList.selectedIndex != -1 && bookmarksList.selectedIndex < bookmarksList.items.size() ) {
          Bookmark bookmark = ( Bookmark ) bookmarksList.getElement( bookmarksList.selectedIndex );
          BookmarkEditorFrame bookmarkEditorFrame = new BookmarkEditorFrame( bookmark );
          bookmarkEditorFrame.s_prevWindow = BookmarksFrame.this;
          MidletMain.screen.setActiveWindow( bookmarkEditorFrame );
        }
      }
    } );
    soft.rightSoft.addSubItem( new PopupItem( Localization.getMessage( "REMOVE_BOOKMARK" ) ) {

      public void actionPerformed() {
        if ( bookmarksList.selectedIndex != -1 && bookmarksList.selectedIndex < bookmarksList.items.size() ) {
          bookmarksList.items.removeElementAt( bookmarksList.selectedIndex );
          saveBookmarks();
        }
      }
    } );
    soft.rightSoft.addSubItem( new PopupItem( Localization.getMessage( "INFO_BOOKMARK" ) ) {

      public void actionPerformed() {
        if ( bookmarksList.selectedIndex != -1 && bookmarksList.selectedIndex < bookmarksList.items.size() ) {
          Bookmark bookmark = ( Bookmark ) bookmarksList.getElement( bookmarksList.selectedIndex );
          String[] param;
          String[] value;
          if ( bookmark.password != null ) {
            param = new String[]{ "BKMRK_JID", "BKMRK_NAME", "BKMRK_NICK", "BKMRK_PASSWORD", "BKMRK_MINIMIZE", "BKMRK_AUTOJOIN" };
            value = new String[]{ bookmark.jid, bookmark.name,
              bookmark.nick, bookmark.password,
              bookmark.minimize ? Localization.getMessage( "TRUE" ) : Localization.getMessage( "FALSE" ),
              bookmark.autojoin ? Localization.getMessage( "TRUE" ) : Localization.getMessage( "FALSE" ) };
          } else {
            param = new String[]{ "BKMRK_JID", "BKMRK_NAME", "BKMRK_NICK", "BKMRK_MINIMIZE", "BKMRK_AUTOJOIN" };
            value = new String[]{ bookmark.jid, bookmark.name, bookmark.nick,
              bookmark.minimize ? Localization.getMessage( "TRUE" ) : Localization.getMessage( "FALSE" ),
              bookmark.autojoin ? Localization.getMessage( "TRUE" ) : Localization.getMessage( "FALSE" ) };
          }
          InfoFrame infoFrame = new InfoFrame( param, value );
          infoFrame.s_prevWindow = BookmarksFrame.this;
          MidletMain.screen.setActiveWindow( infoFrame );
        }
      }
    } );
    soft.rightSoft.addSubItem( new PopupItem( Localization.getMessage( "JOIN_BOOKMARK" ) ) {

      public void actionPerformed() {
        joinBookmark();
      }
    } );
    /** List **/
    bookmarksList = new List();
    bookmarksList.listEvent = new ListEvent() {

      public void actionPerformed( ListItem li ) {
        joinBookmark();
      }
    };
    setGObject( bookmarksList );

    requestBookmarks();
  }

  public void joinBookmark() {
    if ( bookmarksList.selectedIndex != -1 && bookmarksList.selectedIndex < bookmarksList.items.size() ) {
      bookmark = ( Bookmark ) bookmarksList.getElement( bookmarksList.selectedIndex );

      MidletMain.screen.setWaitScreenState( true );

      requestId = "groupchat_join_".concat( xmppAccountRoot.xmppSession.getId() );
      try {
        XmppSender.joinConfrence( xmppAccountRoot.xmppSession, requestId, bookmark.jid, bookmark.nick, bookmark.password );
      } catch ( IOException ex ) {
        LogUtil.outMessage( "Error while conference join: " + ex.getMessage() );
        Handler.showError( Localization.getMessage( "IO_EXCEPTION" ) );
      }
    }
  }

  public final void requestBookmarks() {
    MidletMain.screen.setWaitScreenState( true );
    new Thread() {

      public void run() {
        String id = requestId;
        try {
          Thread.sleep( 20000 );
        } catch ( InterruptedException ex ) {
        }
        MidletMain.screen.setWaitScreenState( false );
        if ( id.equals( requestId ) ) {
          Handler.showError( Localization.getMessage( "NO_RESPONSE" ) );
        }
      }
    }.start();
    requestId = "bookmrksfrm_get".concat( xmppAccountRoot.xmppSession.getId() );
    XmppSender.requesBookmarks( xmppAccountRoot.xmppSession, requestId );
    bookmark = null;
  }

  public void setBookmarkStatus( XmppAccountRoot xmppAccountRoot, String jid ) {
    if ( bookmark != null && xmppAccountRoot.equals( xmppAccountRoot ) && jid.equals( bookmark.jid ) ) {
      if ( xmppAccountRoot.conferenceGroup == null
              || !xmppAccountRoot.getBuddyItems().contains( xmppAccountRoot.conferenceGroup ) ) {
        xmppAccountRoot.conferenceGroup = new XmppGroup( Localization.getMessage( "CONFERENCE_GROUP" ) );
        xmppAccountRoot.getBuddyItems().addElement( xmppAccountRoot.conferenceGroup );
      }
      XmppItem conferenceItem = new XmppItem( bookmark.jid, bookmark.name );
      conferenceItem.isGroupChat = true;
      conferenceItem.getResource( "" );
      conferenceItem.updateUiData();
      conferenceItem.groupChatNick = bookmark.nick;
      ChatTab chatTab = MidletMain.chatFrame.getChatTab( xmppAccountRoot, bookmark.jid, "" );
      if ( chatTab != null ) {
        /** Dialog is opened **/
        chatTab.buddyItem = conferenceItem;
        chatTab.resource = conferenceItem.getResource( "" );
        chatTab.updateChatCaption();
      }
      xmppAccountRoot.removeBuddyItem( conferenceItem.userId );
      xmppAccountRoot.conferenceGroup.addChild( conferenceItem );
      xmppAccountRoot.xmppSession.roster.put( conferenceItem.userId,
              conferenceItem );
      xmppAccountRoot.updateMainFrameUI();
      MidletMain.screen.setWaitScreenState( false );
      bookmark = null;
      MidletMain.screen.setActiveWindow( s_prevWindow );
    }
  }

  public void setBookmarkError( XmppAccountRoot xmppAccountRoot, String id, String errorText, int errorId ) {
    if ( xmppAccountRoot.equals( this.xmppAccountRoot ) && id.equals( this.requestId ) ) {
      MidletMain.screen.setWaitScreenState( false );
      switch ( errorId ) {
        case 0x01: {
          Handler.showError( Localization.getMessage( errorText ) );
          bookmark = null;
        }
      }
    }
  }

  public void setBookmarks( XmppAccountRoot xmppAccountRoot, String id, Vector bookmarks ) {
    LogUtil.outMessage( "setBookmarks" );
    if ( this.xmppAccountRoot.equals( xmppAccountRoot ) && requestId.equals( id ) ) {
      if ( bookmarks != null ) {
        bookmarksList.items = bookmarks;
        bookmarksList.yOffset = 0;
        bookmarksList.selectedIndex = 0;
      }
      requestId = "";
      MidletMain.screen.setWaitScreenState( false );
    }
  }

  public void removeBookmark( Bookmark bookmark ) {
    bookmarksList.items.removeElement( bookmark );
    MidletMain.screen.repaint();
  }

  public void setBookmark( Bookmark bookmark ) {
    if ( !bookmarksList.items.contains( bookmark ) ) {
      bookmarksList.addItem( bookmark );
    }
    MidletMain.screen.repaint();
  }

  public void saveBookmarks() {
    MidletMain.screen.setWaitScreenState( true );
    requestId = "bookmrksfrm_set".concat( xmppAccountRoot.xmppSession.getId() );
    XmppSender.sendBookmarks( xmppAccountRoot.xmppSession, requestId, bookmarksList.items );
  }
}
