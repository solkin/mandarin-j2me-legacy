package com.tomclaw.mandarin.xmpp;

import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import javax.microedition.lcdui.TextField;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class BookmarkEditorFrame extends Window {

  private Field jidField;
  private Field nameField;
  private Field nickField;
  private Field passwordField;
  private Check autojoin;
  private Check minimize;
  private final Bookmark bookmark;

  public BookmarkEditorFrame( final Bookmark bookmark ) {
    super( MidletMain.screen );
    if ( bookmark == null ) {
      this.bookmark = new Bookmark();
    } else {
      this.bookmark = bookmark;
    }
    /** Header **/
    header = new Header( Localization.getMessage( "BOOKMARK_FRAME" ) );
    /** Creating soft **/
    soft = new Soft( MidletMain.screen );
    /** Left soft items **/
    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {

      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    /** Right soft items **/
    soft.rightSoft = new PopupItem( Localization.getMessage( "SAVE" ) ) {

      public void actionPerformed() {
        MidletMain.bookmarksFrame.removeBookmark( bookmark );
        BookmarkEditorFrame.this.bookmark.jid = jidField.getText();
        BookmarkEditorFrame.this.bookmark.name = nameField.getText();
        BookmarkEditorFrame.this.bookmark.nick = nickField.getText();
        BookmarkEditorFrame.this.bookmark.password = passwordField.getText();
        BookmarkEditorFrame.this.bookmark.autojoin = autojoin.getState();
        BookmarkEditorFrame.this.bookmark.minimize = minimize.getState();
        BookmarkEditorFrame.this.bookmark.title = BookmarkEditorFrame.this.bookmark.name;
        MidletMain.bookmarksFrame.setBookmark( BookmarkEditorFrame.this.bookmark );
        MidletMain.bookmarksFrame.saveBookmarks();
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    /** Pane **/
    Pane pane = new Pane( null, false );
    Label paramLabel = new Label( Localization.getMessage( "BKMRK_JID" ).concat( ":" ) );
    pane.addItem( paramLabel );
    jidField = new Field( this.bookmark.jid );
    jidField.setFocusable( true );
    jidField.setFocused( true );
    pane.addItem( jidField );
    paramLabel = new Label( Localization.getMessage( "BKMRK_NAME" ).concat( ":" ) );
    pane.addItem( paramLabel );
    nameField = new Field( this.bookmark.name == null ? "" : this.bookmark.name );
    nameField.setFocusable( true );
    pane.addItem( nameField );
    paramLabel = new Label( Localization.getMessage( "BKMRK_NICK" ).concat( ":" ) );
    pane.addItem( paramLabel );
    nickField = new Field( this.bookmark.nick == null ? "" : this.bookmark.nick );
    nickField.setFocusable( true );
    pane.addItem( nickField );
    paramLabel = new Label( Localization.getMessage( "BKMRK_PASSWORD" ).concat( ":" ) );
    pane.addItem( paramLabel );
    passwordField = new Field( this.bookmark.password == null ? "" : this.bookmark.password );
    passwordField.setFocusable( true );
    passwordField.constraints = TextField.PASSWORD;
    pane.addItem( passwordField );
    autojoin = new Check( Localization.getMessage( "BKMRK_AUTOJOIN" ), this.bookmark.autojoin );
    autojoin.setFocusable( true );
    pane.addItem( autojoin );
    minimize = new Check( Localization.getMessage( "BKMRK_MINIMIZE" ), this.bookmark.minimize );
    minimize.setFocusable( true );
    pane.addItem( minimize );
    setGObject( pane );
  }
}
