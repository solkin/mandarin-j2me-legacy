package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;
import java.io.DataOutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class HistoryFrame extends Window {

  public List list;
  public Pane pane;
  /** Temporary **/
  public ChatItem chatItem;
  public Soft chatSoft;
  public Soft listSoft;

  public HistoryFrame( final String accType, final String buddyId ) {
    super( MidletMain.screen );

    header = new Header( Localization.getMessage( "HISTORY_FRAME_FOR" ).concat( " " ).concat( buddyId ) );

    listSoft = new Soft( MidletMain.screen );
    soft = listSoft;

    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {
      public void actionPerformed() {
        try {
          list.recordStore.closeRecordStore();
        } catch ( Throwable ex ) {
          LogUtil.outMessage( HistoryFrame.this.getClass(), ex );
        }
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };

    soft.rightSoft = new PopupItem( Localization.getMessage( "MENU" ) );

    soft.rightSoft.addSubItem( new PopupItem( Localization.getMessage( "APPEND" ) ) {
      public void actionPerformed() {
        try {
          ChatItem chatItem = MidletMain.historyRmsRenderer.getRmsItem( list.recordStore.getRecord( list.selectedIndex + 1 ), pane );
          MidletMain.buffer += "\n[".concat( chatItem.buddyNick ).concat( "]\n " ).concat( chatItem.itemDateTime ).concat( " \n" ).concat( chatItem.text );
        } catch ( RecordStoreNotOpenException ex ) {
          LogUtil.outMessage( HistoryFrame.this.getClass(), ex );
        } catch ( InvalidRecordIDException ex ) {
          LogUtil.outMessage( HistoryFrame.this.getClass(), ex );
        } catch ( RecordStoreException ex ) {
          LogUtil.outMessage( HistoryFrame.this.getClass(), ex );
        }
      }
    } );
    soft.rightSoft.addSubItem( new PopupItem( Localization.getMessage( "COPY" ) ) {
      public void actionPerformed() {
        try {
          ChatItem chatItem = MidletMain.historyRmsRenderer.getRmsItem( list.recordStore.getRecord( list.selectedIndex + 1 ), pane );
          MidletMain.buffer = "[".concat( chatItem.buddyNick ).concat( "]\n " ).concat( chatItem.itemDateTime ).concat( " \n" ).concat( chatItem.text );
        } catch ( RecordStoreNotOpenException ex ) {
          LogUtil.outMessage( HistoryFrame.this.getClass(), ex );
        } catch ( InvalidRecordIDException ex ) {
          LogUtil.outMessage( HistoryFrame.this.getClass(), ex );
        } catch ( RecordStoreException ex ) {
          LogUtil.outMessage( HistoryFrame.this.getClass(), ex );
        }
      }
    } );
    soft.rightSoft.addSubItem( new PopupItem( Localization.getMessage( "CLEAR_HISTORY" ) ) {
      public void actionPerformed() {
        try {
          String rsName = list.recordStore.getName();
          list.recordStore.closeRecordStore();
          RecordStore.deleteRecordStore( rsName );
          list.recordStore = RecordStore.openRecordStore( rsName, true );
          list.recordStore.closeRecordStore();
          list.items.removeAllElements();
        } catch ( RecordStoreNotOpenException ex ) {
          LogUtil.outMessage( HistoryFrame.this.getClass(), ex );
          // ex.printStackTrace();
        } catch ( RecordStoreException ex ) {
          LogUtil.outMessage( HistoryFrame.this.getClass(), ex );
          // ex.printStackTrace();
        }
      }
    } );
    soft.rightSoft.addSubItem( new PopupItem( Localization.getMessage( "EXPORT_HISTORY" ) ) {
      public void actionPerformed() {
        String cmplTitle = "ERROR";
        String cmplMessage = "EXPORT_FAILED";
        String filePath = "";
        try {
          filePath = MidletMain.incomingFilesFolder + accType + buddyId.hashCode() + ".html";
          FileConnection file = ( FileConnection ) Connector.open( "file://" + filePath );

          LogUtil.outMessage( "Export to: " + "file://" + filePath + "\n" + file.getPath() );
          if ( file.exists() ) {
            file.delete();
          }
          file.create();
          DataOutputStream outputStream = file.openDataOutputStream();

          outputStream.write( "<html>\n\t<head>\n".getBytes() );
          outputStream.write( "\t\t<meta http-equiv=\"Content-Type\" content=\"text/html\"; charset=\"UTF-8\">\n".getBytes() );
          outputStream.write( "\t\t<meta name=\"Generator\" content=\"TomClaw Mandarin IM\">\n".getBytes() );
          outputStream.write( ( "\t\t<meta name=\"author\" content=\"" + buddyId + "\">\n" ).getBytes() );

          outputStream.write( ( "\t\t<style type=\"text/css\">\n" ).getBytes() );
          outputStream.write( ( "\t\t\t.o { color: blue; }\n" ).getBytes() );
          outputStream.write( ( "\t\t\t.i { color: red; }\n" ).getBytes() );
          outputStream.write( ( "\t\t\t.t { color: green; font-style: italic; }\n" ).getBytes() );
          outputStream.write( ( "\t\t\t.m { color: black; }\n" ).getBytes() );
          outputStream.write( ( "\t\t</style>\n" ).getBytes() );

          outputStream.write( ( "\t\t<title>" + buddyId + "</title>\n" ).getBytes() );
          outputStream.write( "\t</head>\n\t<body>\n".getBytes() );
          try {
            ChatItem chatItem;
            for ( int c = 0; c < list.items.size(); c++ ) {
              chatItem = MidletMain.historyRmsRenderer.getRmsItem( list.recordStore.getRecord( c + 1 ), pane );
              outputStream.write( ( "\t\t<div class=\"" + ( chatItem.buddyId.equals( buddyId ) ? "o" : "i" ) + "\"><b>" ).getBytes() );
              outputStream.write( StringUtil.stringToByteArray( chatItem.buddyNick + " [" + chatItem.buddyId + "]", true ) );
              outputStream.write( ( "</b></div>\n" ).getBytes() );
              outputStream.write( ( "\t\t<div class=\"t\">" + chatItem.itemDateTime + "</div>\n" ).getBytes() );
              outputStream.write( ( "\t\t<div class=\"m\">" ).getBytes() );
              outputStream.write( StringUtil.stringToByteArray( StringUtil.replace( chatItem.text, "\n", "<br>" ), true ) );
              outputStream.write( ( "</div>" ).getBytes() );
              outputStream.write( "<hr>\n\n".getBytes() );
            }
          } catch ( Throwable ex ) {
          }
          outputStream.write( "\n\t</body>\n</html>".getBytes() );
          outputStream.close();
          file.close();
          cmplTitle = "COMPLETE";
          cmplMessage = "EXPORT_COMPLETE";
        } catch ( Throwable ex ) {
          LogUtil.outMessage( "Export error: " + ex.getMessage() );
        }
        MidletMain.screen.repaint();

        Soft notifySoft = new Soft( MidletMain.screen );
        notifySoft.leftSoft = new PopupItem( Localization.getMessage( "CLOSE" ) ) {
          public void actionPerformed() {
            HistoryFrame.this.closeDialog();
          }
        };
        HistoryFrame.this.showDialog(
                new Dialog( MidletMain.screen, notifySoft,
                Localization.getMessage( cmplTitle ),
                Localization.getMessage( cmplMessage ) + " " + filePath ) );
      }
    } );

    list = new List( accType + buddyId.hashCode() + ".his" );
    list.listRmsRenderer = MidletMain.historyRmsRenderer;
    list.listEvent = new ListEvent() {
      public void actionPerformed( ListItem li ) {
        try {
          if ( pane == null ) {
            initPane();
          } else {
            pane.items.removeAllElements();
          }
          setGObject( pane );
          chatItem = MidletMain.historyRmsRenderer.getRmsItem( list.recordStore.getRecord( list.selectedIndex + 1 ), pane );
          pane.addItem( chatItem );
          soft = chatSoft;
        } catch ( RecordStoreNotOpenException ex ) {
          LogUtil.outMessage( HistoryFrame.this.getClass(), ex );
        } catch ( InvalidRecordIDException ex ) {
          LogUtil.outMessage( HistoryFrame.this.getClass(), ex );
        } catch ( RecordStoreException ex ) {
          LogUtil.outMessage( HistoryFrame.this.getClass(), ex );
        }
      }
    };

    setGObject( list );
  }

  public void initPane() {
    pane = new Pane( HistoryFrame.this, true );
    chatSoft = new Soft( MidletMain.screen );
    chatSoft.leftSoft = new PopupItem( Localization.getMessage( "NEXT" ) ) {
      public void actionPerformed() {
        if ( list.selectedIndex < list.items.size() - 1 ) {
          list.selectedIndex++;
          list.listEvent.actionPerformed( null );
        }
      }
    };
    chatSoft.rightSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {
      public void actionPerformed() {
        soft = listSoft;
        setGObject( list );
      }
    };
  }
}
