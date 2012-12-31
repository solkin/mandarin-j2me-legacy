package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.ChatItem;
import com.tomclaw.utils.TimeUtil;
import java.util.Stack;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class ServiceMessages {

  public static final int TYPE_STATUS_CHANGE = 0x01;
  public static final int TYPE_XSTATUS_READ = 0x02;
  public static final int TYPE_MSTATUS_READ = 0x03;
  public static final int TYPE_FILETRANSFER = 0x04;
  public Stack messages = new Stack();
  public int messagesCount = 30;

  public void addMessage( String buddyId, String buddyNick, String text, int type ) {
    /** Adapting for BB viewing **/
    text = "[p]".concat( text ).concat( "[/p]" );
    /** Checking for type in settings **/
    switch ( type ) {
      case TYPE_STATUS_CHANGE: {
        if ( !MidletMain.statusChange ) {
          return;
        }
        break;
      }
      case TYPE_XSTATUS_READ: {
        if ( !MidletMain.xStatusRead ) {
          return;
        }
        break;
      }
      case TYPE_MSTATUS_READ: {
        if ( !MidletMain.mStatusRead ) {
          return;
        }
        break;
      }
      case TYPE_FILETRANSFER: {
        if ( !MidletMain.fileTransfer ) {
          return;
        }
        break;
      }
    }
    /** Appending message **/
    ChatItem item = new ChatItem( MidletMain.serviceMessagesFrame.pane,
            buddyId, buddyNick, TimeUtil.getTimeString( TimeUtil.getCurrentTimeGMT(), true ),
            type == TYPE_FILETRANSFER ? ChatItem.TYPE_FILE_TRANSFER : ChatItem.TYPE_INFO_MSG, text );
    messages.push( item );
    if ( messages.size() > messagesCount ) {
      messages.removeElementAt( 0 );
    }
    /** Checking for screen existance **/
    if ( MidletMain.serviceMessagesFrame.pane.items.equals( messages )
            && MidletMain.screen.activeWindow.equals( MidletMain.serviceMessagesFrame ) ) {
      MidletMain.serviceMessagesFrame.updateItemsLocation( true );
    }
  }

  public void clearMessages() {
    messages.removeAllElements();
  }
}
