package com.tomclaw.mandarin.xmpp;

import com.tomclaw.mandarin.core.Handler;
import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.mandarin.main.TransactionItemFrame;
import com.tomclaw.mandarin.net.NetConnection;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.Base64;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.TextField;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class XmppSession {

  public XmppAccountRoot xmppAccountRoot;
  public NetConnection netConnection = new NetConnection();
  public XmlReader xmlReader;
  public XmlWriter xmlWriter;
  public int int_id;
  public Hashtable roster;
  private boolean isAlive = true;

  public XmppSession( XmppAccountRoot xmppAccountRoot ) {
    this.xmppAccountRoot = xmppAccountRoot;
    roster = new Hashtable();
  }

  public void connect( int status ) throws IOException, Throwable {
    Handler.setConnectionStage( xmppAccountRoot, 0 );
    netConnection.connectAddress( xmppAccountRoot.host, Integer.parseInt( xmppAccountRoot.port ) );
    Handler.setConnectionStage( xmppAccountRoot, 2 );
    xmlReader = new XmlReader(/*new VirtualInputStream*/ ( netConnection.inputStream ) );
    xmlWriter = new XmlWriter(/*new VirtualOutputStream*/ ( netConnection.outputStream ) );
    Handler.setConnectionStage( xmppAccountRoot, 4 );

    LogUtil.outMessage( "host=" + xmppAccountRoot.host );
    LogUtil.outMessage( "username=" + xmppAccountRoot.username );
    LogUtil.outMessage( "password=" + xmppAccountRoot.getUserPassword() );
    LogUtil.outMessage( "resource=" + xmppAccountRoot.resource );
    if ( xmppAccountRoot.isUseSsl ) {
      String msg = "<stream:stream xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' to='" + xmppAccountRoot.host + "' version='1.0'>";
      xmlWriter.writeDirect( msg.getBytes() );

      do {
        xmlReader.nextTag();
      } while ( ( xmlReader.tagType != XmlReader.TAG_CLOSING ) || ( !xmlReader.tagName.equals( "stream:features" ) ) );
      Handler.setConnectionStage( xmppAccountRoot, 6 );
      LogUtil.outMessage( "SASL phase1" );

      msg = "<auth xmlns='urn:ietf:params:xml:ns:xmpp-sasl' mechanism='PLAIN'>";
      byte[] auth_msg = ( xmppAccountRoot.username + "@" + xmppAccountRoot.host + "\0" + xmppAccountRoot.username + "\0" + xmppAccountRoot.getUserPassword() ).getBytes();
      msg = msg + Base64.encode( auth_msg, 0, auth_msg.length ) + "</auth>";
      xmlWriter.writeDirect( msg.getBytes() );

      xmlReader.nextTag();
      if ( xmlReader.tagName.equals( "success" ) ) {
        while ( true ) {
          if ( ( xmlReader.tagType == XmlReader.TAG_CLOSING || xmlReader.tagType == XmlReader.TAG_SELFCLOSING ) && xmlReader.tagName.equals( "success" ) ) {
            break;
          }
          xmlReader.nextTag();
        }
      } else {
        LogUtil.outMessage( "SASL failed" );
        return;
      }
      LogUtil.outMessage( "SASL phase2" );
      Handler.setConnectionStage( xmppAccountRoot, 7 );
      msg = "<stream:stream xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' to='" + xmppAccountRoot.host + "' version='1.0'>";
      xmlWriter.writeDirect( msg.getBytes() );

      xmlReader.nextTag();
      while ( true ) {
        if ( ( xmlReader.tagType == XmlReader.TAG_CLOSING ) && xmlReader.tagName.equals( "stream:features" ) ) {
          break;
        }
        xmlReader.nextTag();
      }
      LogUtil.outMessage( "SASL done" );
      if ( xmppAccountRoot.resource == null ) {
        msg = "<iq type='set' id='res_binding'><bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'/></iq>";
      } else {
        msg = "<iq type='set' id='res_binding'><bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'><resource>" + xmppAccountRoot.resource + "</resource></bind></iq>";
      }
      xmlWriter.writeDirect( msg.getBytes() );

      xmlWriter.writeDirect(
              ( "<iq type=\"set\" id=\"mir_17\">"
              + "<session xmlns=\"urn:ietf:params:xml:ns:xmpp-session\" />"
              + "</iq>" ).getBytes() );

    } else {
      netConnection.write( "<?xml version=\"1.0\"?>".getBytes() );

      Handler.setConnectionStage( xmppAccountRoot, 6 );
      // start stream
      xmlWriter.startTag( "stream:stream" );
      xmlWriter.attribute( "from", xmppAccountRoot.userId.concat( "/" ).concat( xmppAccountRoot.resource ) );
      xmlWriter.attribute( "to", xmppAccountRoot.domain );
      xmlWriter.attribute( "xmlns", "jabber:client" );
      xmlWriter.attribute( "xmlns:stream", "http://etherx.jabber.org/streams" );
      xmlWriter.flush();

      Handler.setConnectionStage( xmppAccountRoot, 7 );

      // log in
      xmlWriter.startTag( "iq" );
      xmlWriter.attribute( "type", "set" );
      xmlWriter.attribute( "id", "auth" );
      xmlWriter.startTag( "query" );
      xmlWriter.attribute( "xmlns", "jabber:iq:auth" );

      xmlWriter.startTag( "username" );
      xmlWriter.text( xmppAccountRoot.username );
      xmlWriter.endTag();
      xmlWriter.startTag( "password" );
      xmlWriter.text( xmppAccountRoot.getUserPassword() );
      xmlWriter.endTag();
      xmlWriter.startTag( "resource" );
      xmlWriter.text( xmppAccountRoot.resource );
      xmlWriter.endTag();

      xmlWriter.endTag(); // query
      xmlWriter.endTag(); // iq
      xmlWriter.flush();
    }

    Handler.setConnectionStage( xmppAccountRoot, 8 );

    XmppSender.sendRosterRequest( this );

    Handler.setConnectionStage( xmppAccountRoot, 9 );

    XmppSender.setStatus( xmlWriter, xmppAccountRoot.userId.concat( "/" ).concat( xmppAccountRoot.resource ),
            XmppStatusUtil.statuses[status], "", 5 );

    xmppAccountRoot.statusIndex = status;

    Handler.setConnectionStage( xmppAccountRoot, 10 );
    isAlive = true;

    new Thread() {
      public void run() {
        try {
          while ( isAlive ) {
            XmppSession.this.netConnection.write( " ".getBytes() );
            sleep( 20000 );
          }
        } catch ( Throwable ex ) {
          LogUtil.outMessage( "XmppSession: " + ex.getMessage() );
        }
      }
    }.start();

    new Thread() {
      public void run() {
        try {
          parse();
        } catch ( Throwable ex ) {
          LogUtil.outMessage( "Throwable while XMPP parsing" );
          LogUtil.outMessage( "XmppSession: " + ex.getMessage() );
          System.err.println( ex.getMessage() );
        }
        LogUtil.outMessage( "XMPP parsing completed" );
        xmppAccountRoot.statusIndex = XmppStatusUtil.offlineIndex;
        Handler.disconnectEvent( xmppAccountRoot );
        if ( isAlive ) {
          int prevStatus = xmppAccountRoot.getStatusIndex();

          while ( MidletMain.autoReconnect && xmppAccountRoot.statusIndex
                  == XmppStatusUtil.offlineIndex ) {
            try {
              xmppAccountRoot.xmppSession.disconnect();
            } catch ( IOException ex1 ) {
            }
            try {
              sleep( MidletMain.reconnectTime );
              connect( prevStatus );
            } catch ( Throwable ex1 ) {
              LogUtil.outMessage( "XmppSession: " + ex1.getMessage() );
            }
          }
        }
      }
    }.start();
  }

  public void parse() throws Throwable {
    boolean isSelfClose;
    while ( xmlReader.nextTag() ) {
      LogUtil.outMessage( "Tag start" );
      if ( MidletMain.logLevel == 1 ) {
        LogUtil.outMessage( "0x" + Integer.toString( xmlReader.tagType, 16 ) + ": '" + xmlReader.tagName + "' " + xmlReader.attributes.size() );
        LogUtil.outMessage( "[" + xmlReader.body + "]" );
      }
      if ( xmlReader.tagName == null ) {
        continue;
      }
      isSelfClose = xmlReader.tagType == XmlReader.TAG_SELFCLOSING;
      if ( xmlReader.tagName.equals( "message" ) ) {
        LogUtil.outMessage( "Message tag" );
        parseMessage( isSelfClose,
                xmlReader.getAttrValue( "from", true ),
                xmlReader.getAttrValue( "to", true ),
                xmlReader.getAttrValue( "type", true ) );
        LogUtil.outMessage( "Message tag closed" );
      } else if ( xmlReader.tagName.equals( "presence" ) ) {
        LogUtil.outMessage( "Presence tag" );
        parsePresence( isSelfClose,
                xmlReader.getAttrValue( "from", true ),
                xmlReader.getAttrValue( "to", true ),
                xmlReader.getAttrValue( "type", false ) );
        LogUtil.outMessage( "Presence tag closed" );
      } else if ( xmlReader.tagName.equals( "iq" ) ) {
        LogUtil.outMessage( "Iq tag" );
        parseIq( isSelfClose, xmlReader.getAttrValue( "type", true ) );
        LogUtil.outMessage( "Iq tag closed" );
      } else {
        // Passing tag
      }
      if ( MidletMain.pack_count > MidletMain.pack_count_invoke_gc ) {
        System.gc();
        MidletMain.pack_count = 0;
      }
      MidletMain.pack_count++;
    }
  }

  public void disconnect() throws IOException {
    XmppSender.sendPresence( xmppAccountRoot.xmppSession.xmlWriter, null,
            null, "unavailable", null, null, 0, false, null, null );
    isAlive = false;
    netConnection.disconnect();
  }

  public void parsePresence( boolean isSelfClose, String from, String to, String type ) throws IOException {
    try {
      String t_show = XmppStatusUtil.statuses[XmppStatusUtil.onlineIndex];
      String t_status = "";
      String t_priority = "";
      String t_id = xmlReader.getAttrValue( "id", true );
      String errorText = "";
      int errorId = 0x00;
      String capsNode = null;
      String capsVer = null;

      while ( !isSelfClose && ( xmlReader.nextTag() && !( xmlReader.tagName != null && xmlReader.tagName.equals( "presence" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) ) ) {
        if ( xmlReader.tagName == null ) {
          continue;
        }
        if ( type.equals( "error" ) ) {
          if ( xmlReader.tagName.equals( "error" ) ) {
            String errorCode = xmlReader.getAttrValue( "code", false );
            String errorType = xmlReader.getAttrValue( "type", false );
            if ( errorType.equals( "cancel" ) ) {
              if ( errorCode.equals( "409" ) ) {
                if ( t_id.startsWith( "groupchat_join_" ) ) {
                  errorText = "NICK_IS_USED";
                  errorId = 0x01;
                }
              }
            }
          }
        } else {
          if ( xmlReader.tagName.equals( "show" ) ) {
            if ( xmlReader.tagType == XmlReader.TAG_PLAIN ) {
              continue;
            } else if ( xmlReader.tagType == XmlReader.TAG_CLOSING ) {
              t_show = xmlReader.body;
            } else if ( xmlReader.tagType == XmlReader.TAG_SELFCLOSING ) {
              t_show = XmppStatusUtil.statuses[XmppStatusUtil.onlineIndex];
            }
          } else if ( xmlReader.tagName.equals( "status" ) ) {
            if ( xmlReader.tagType == XmlReader.TAG_PLAIN ) {
              continue;
            } else if ( xmlReader.tagType == XmlReader.TAG_CLOSING ) {
              t_status = xmlReader.body;
            } else if ( xmlReader.tagType == XmlReader.TAG_SELFCLOSING ) {
              t_status = "";
            }
          } else if ( xmlReader.tagName.equals( "priority" ) ) {
            if ( xmlReader.tagType == XmlReader.TAG_PLAIN ) {
              continue;
            } else if ( xmlReader.tagType == XmlReader.TAG_CLOSING ) {
              t_priority = xmlReader.body;
            }
          } else if ( xmlReader.tagName.equals( "c" ) ) {
            if ( xmlReader.getAttrValue( "xmlns", false ).
                    equals( "http://jabber.org/protocol/caps" ) ) {
              capsNode = xmlReader.getAttrValue( "node", true );
              capsVer = xmlReader.getAttrValue( "ver", true );
            }
          }
        }
      }
      LogUtil.outMessage( "Presence from: " + from + " to " + to + " type = [" + type + "]: show = \"" + t_show + "\" status = \"" + t_status + "\" priority = \"" + t_priority + "\"" );
      if ( type.equals( "unavailable" ) ) {
        t_show = XmppStatusUtil.statuses[XmppStatusUtil.offlineIndex];
      }
      if ( from == null ) {
        return;
      }
      LogUtil.outMessage( "Searching for... " + getClearJid( from ) );
      if ( t_id != null && t_id.startsWith( "groupchat_join_" ) && type.equals( "error" ) ) {
        Handler.setBookmarkError( xmppAccountRoot, t_id, errorText, errorId );
      } else if ( !type.equals( "error" ) ) {
        if ( MidletMain.bookmarksFrame != null ) {
          MidletMain.bookmarksFrame.setBookmarkStatus( xmppAccountRoot, getClearJid( from ) );
        }
        Handler.setXmppStatus( xmppAccountRoot, from, t_show, t_status, capsNode, capsVer );
      }
    } catch ( Throwable ex1 ) {
      LogUtil.outMessage( "XmppSession: " + ex1.getMessage() );
    }
  }

  public static String getClearJid( String fullJid ) {
    if ( fullJid != null && fullJid.indexOf( "/" ) != -1 ) {
      return fullJid.substring( 0, fullJid.indexOf( "/" ) );
    }
    return fullJid;
  }

  public static String getJidResource( String fullJid ) {
    if ( fullJid != null && fullJid.indexOf( "/" ) != -1 ) {
      return fullJid.substring( fullJid.indexOf( "/" ) + 1 );
    }
    return "";
  }

  public void parseMessage( boolean isSelfClose, String from, String to, String type ) throws IOException {
    try {
      String t_id = xmlReader.getAttrValue( "id", false );
      boolean isStoreData = false;
      XmppIBBytestream directConnection = null;
      String seq = "";
      while ( !isSelfClose && ( xmlReader.nextTag() && !( xmlReader.tagName != null && xmlReader.tagName.equals( "message" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) ) ) {
        if ( xmlReader.tagName == null ) {
          continue;
        }
        if ( type != null && ( type.equals( "chat" ) || type.equals( "groupchat" ) ) ) {
          if ( xmlReader.tagName.equals( "body" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) {
            LogUtil.outMessage( "Message or subject from " + from + ": " + xmlReader.body );
            if ( type.equals( "chat" ) ) {
              Handler.recMess( xmppAccountRoot, getClearJid( from ), getJidResource( from ), null, xmlReader.body, t_id.getBytes(), com.tomclaw.tcuilite.ChatItem.TYPE_PLAIN_MSG );
            } else if ( type.equals( "groupchat" ) ) {
              if ( MidletMain.bookmarksFrame != null ) {
                MidletMain.bookmarksFrame.setBookmarkStatus( xmppAccountRoot, getClearJid( from ) );
              }
              String messageText = xmlReader.body;
              String nick = ( ( getJidResource( from ).length() == 0 ) ? Localization.getMessage( "SYSTEM_MSG" ) : getJidResource( from ) );
              Handler.recMess(
                      xmppAccountRoot, getClearJid( from ), "",
                      nick,
                      messageText, t_id.getBytes(),
                      com.tomclaw.tcuilite.ChatItem.TYPE_PLAIN_MSG );
            }
          } else if ( xmlReader.tagName.equals( "subject" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) {
            if ( MidletMain.bookmarksFrame != null ) {
              MidletMain.bookmarksFrame.setBookmarkStatus( xmppAccountRoot, getClearJid( from ) );
            }
            LogUtil.outMessage( "Subject to: " + from );
            LogUtil.outMessage( "Subject: " + xmlReader.body );
            XmppItem xmppItem = ( XmppItem ) ( roster.get( getClearJid( from ) ) );
            xmppItem.groupChatSubject = xmlReader.body;
          }
        }
        if ( xmlReader.tagName.equals( "data" ) ) {
          if ( xmlReader.getAttrValue( "xmlns", false ).equals( "http://jabber.org/protocol/ibb" ) ) {
            String sid = xmlReader.getAttrValue( "sid", false );
            seq = xmlReader.getAttrValue( "seq", false );
            directConnection = ( XmppIBBytestream ) xmppAccountRoot.getTransactionManager().getTransaction( StringUtil.stringToByteArray( sid, true ) );
            isStoreData = true;
          }
          if ( xmlReader.tagType == XmlReader.TAG_CLOSING ) {
            if ( directConnection != null ) {
              directConnection.storeData( seq, Base64.decode( xmlReader.body ) );
              isStoreData = false;
            }
          }
        }
      }
    } catch ( Throwable ex1 ) {
      LogUtil.outMessage( "XmppSession: " + ex1.getMessage() );
    }
  }

  public void parseIq( boolean isSelfClose, String type ) throws IOException {
    try {
      String from = xmlReader.getAttrValue( "from", true );
      final String id = xmlReader.getAttrValue( "id", true );
      while ( isSelfClose || ( xmlReader.nextTag() && !( xmlReader.tagName != null && xmlReader.tagName.equals( "iq" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) ) ) {
        if ( xmlReader.tagName == null ) {
          continue;
        }
        LogUtil.outMessage( "TAG: ".concat( xmlReader.tagName ) );
        LogUtil.outMessage( "type: ".concat( type ) );
        if ( type != null && type.equals( "result" ) ) {
          if ( xmlReader.tagName.equals( "query" )/* && xmlReader.tagType == XmlReader.TAG_PLAIN*/ ) {
            if ( xmlReader.getAttrValue( "xmlns", false ).equals( "jabber:iq:roster" )
                    && xmlReader.tagType == XmlReader.TAG_PLAIN ) {
              LogUtil.outMessage( "Roster parsing..." );
              String t_jid = "";
              String t_name = "";
              Vector groups = new Vector();
              roster.clear();
              Vector buddyItems = new Vector();
              XmppGroup unclassified = new XmppGroup( Localization.getMessage( "XMPP_UNCLASSIFIED" ) );
              while ( xmlReader.nextTag() && !( xmlReader.tagName.equals( "query" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) ) {
                LogUtil.outMessage( xmlReader.tagName );
                if ( xmlReader.tagName.equals( "item" ) && ( xmlReader.tagType == XmlReader.TAG_PLAIN || xmlReader.tagType == XmlReader.TAG_SELFCLOSING ) ) {
                  t_jid = xmlReader.getAttrValue( "jid", false );
                  t_name = xmlReader.getAttrValue( "name", true );
                  LogUtil.outMessage( t_jid + ": " + t_name );
                }
                if ( xmlReader.tagName.equals( "group" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) {
                  boolean isExist = false;
                  XmppGroup rosterItem = null;
                  for ( int c = 0; c < buddyItems.size(); c++ ) {
                    if ( ( ( XmppGroup ) buddyItems.elementAt( c ) ).userId.equals( localizeString( xmlReader.body ) ) ) {
                      isExist = true;
                      rosterItem = ( ( XmppGroup ) buddyItems.elementAt( c ) );
                      break;
                    }
                  }
                  if ( !isExist ) {
                    rosterItem = new XmppGroup( localizeString( xmlReader.body ) );
                    buddyItems.addElement( rosterItem );
                  }
                  groups.addElement( rosterItem );
                }
                if ( xmlReader.tagName.equals( "item" ) && ( xmlReader.tagType == XmlReader.TAG_CLOSING || xmlReader.tagType == XmlReader.TAG_SELFCLOSING ) ) {
                  XmppItem rosterItem = new XmppItem(
                          localizeString( t_jid ),
                          localizeString( t_name ) );
                  roster.put( t_jid, rosterItem );
                  if ( groups.isEmpty() ) {
                    unclassified.addChild( rosterItem );
                    LogUtil.outMessage( "Not in group" );
                  } else {
                    for ( int c = 0; c < groups.size(); c++ ) {
                      ( ( XmppGroup ) groups.elementAt( c ) ).addChild( rosterItem );
                      LogUtil.outMessage( "Group: " + ( ( XmppGroup ) groups.elementAt( c ) ).userId );
                    }
                    groups.removeAllElements();
                  }
                }
              }
              if ( unclassified.getChildsCount() > 0 ) {
                buddyItems.addElement( unclassified );
                LogUtil.outMessage( "unclassified appended" );
              }
              LogUtil.outMessage( "Roster parsed." );
              Handler.setBuddyList( xmppAccountRoot, buddyItems, null, 0, 0, new byte[]{} );
              // Main.mainFrame.updateRoster();
            } else if ( xmlReader.getAttrValue( "xmlns", false ).equals( "http://jabber.org/protocol/disco#items" )
                    && xmlReader.tagType == XmlReader.TAG_PLAIN ) {
              Vector discoItems = new Vector();
              if ( !( xmlReader.tagName.equals( "query" ) && xmlReader.tagType == XmlReader.TAG_SELFCLOSING ) ) {
                ServiceItem t_Service;
                while ( xmlReader.nextTag() && !( xmlReader.tagName.equals( "query" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) ) {
                  LogUtil.outMessage( "Service: " + xmlReader.getAttrValue( "jid", false ) );
                  t_Service = new ServiceItem( xmlReader.getAttrValue( "jid", false ), xmlReader.getAttrValue( "node", true ), xmlReader.getAttrValue( "name", true ) );
                  discoItems.addElement( t_Service );
                }
              }
              if ( id.startsWith( "srvfrm_items" ) ) {
                LogUtil.outMessage( "Services list" );
                Handler.setServicesList( this.xmppAccountRoot, discoItems, id );
              }
            } else if ( xmlReader.getAttrValue( "xmlns", false ).equals( "http://jabber.org/protocol/disco#info" )
                    && xmlReader.tagType == XmlReader.TAG_PLAIN ) {
              Vector features = new Vector();
              Vector identityes = new Vector();
              while ( xmlReader.nextTag() && !( xmlReader.tagName.equals( "query" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) ) {
                if ( xmlReader.tagName.equals( "identity" ) ) {
                  identityes.addElement( new Identity(
                          xmlReader.getAttrValue( "category", false ),
                          xmlReader.getAttrValue( "type", false ),
                          xmlReader.getAttrValue( "name", true ) ) );
                } else if ( xmlReader.tagName.equals( "feature" ) ) {
                  features.addElement( xmlReader.getAttrValue( "var", true ) );
                  LogUtil.outMessage( "Feature: " + xmlReader.getAttrValue( "var", true ) );
                }
              }
              if ( id.startsWith( "srvfrm_host" ) ) {
                LogUtil.outMessage( "Host info" );
                Handler.setHostInfo( this.xmppAccountRoot, id, identityes, features );
              } else if ( id.startsWith( "srvfrm_info" ) ) {
                LogUtil.outMessage( "Service info" );
                Handler.setServiceInfo( this.xmppAccountRoot, from, id, identityes, features );
              }
            } else if ( xmlReader.getAttrValue( "xmlns", false ).equals( "jabber:iq:private" ) ) {
              Vector bookmarks = null;
              LogUtil.outMessage( "jabber:iq:private" );
              if ( xmlReader.tagType != XmlReader.TAG_SELFCLOSING ) {
                while ( xmlReader.nextTag() && !( xmlReader.tagName.equals( "query" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) ) {
                  LogUtil.outMessage( "TAG: ".concat( xmlReader.tagName ) );
                  if ( xmlReader.tagName.equals( "storage" ) && xmlReader.getAttrValue( "xmlns", false ).equals( "storage:bookmarks" ) ) {
                    bookmarks = new Vector();
                    continue;
                  }
                  if ( bookmarks != null ) {
                    if ( xmlReader.tagName.equals( "conference" ) ) {
                      Bookmark bookmark = ( new Bookmark(
                              xmlReader.getAttrValue( "jid", false ),
                              xmlReader.getAttrValue( "name", false ),
                              xmlReader.getAttrValue( "minimize", true ),
                              xmlReader.getAttrValue( "autojoin", true ) ) );
                      if ( xmlReader.tagType != XmlReader.TAG_SELFCLOSING ) {
                        while ( xmlReader.nextTag() && !( xmlReader.tagName.equals( "conference" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) ) {
                          if ( xmlReader.tagName.equals( "nick" ) ) {
                            bookmark.nick = xmlReader.body;
                          } else if ( xmlReader.tagName.equals( "password" ) ) {
                            bookmark.password = xmlReader.body;
                          }
                        }
                      }
                      bookmarks.addElement( bookmark );
                    }
                  }
                }
              }
              LogUtil.outMessage( "id: " + id );
              if ( id.startsWith( "bookmrksfrm_get" ) ) {
                LogUtil.outMessage( "Host info" );
                Handler.setBookmarks( this.xmppAccountRoot, id, bookmarks );
              } else if ( id.startsWith( "bookmrksfrm_set" ) ) {
                Handler.setBookmarks( this.xmppAccountRoot, id, null );
              }
            } else if ( xmlReader.getAttrValue( "xmlns", false ).equals( "http://jabber.org/protocol/muc#owner" ) ) {
              Vector objects = new Vector();
              String FORM_TYPE = null;
              boolean isFocused = true;
              LogUtil.outMessage( "http://jabber.org/protocol/muc#owner" );
              if ( xmlReader.tagType != XmlReader.TAG_SELFCLOSING ) {
                while ( xmlReader.nextTag() && !( xmlReader.tagName.equals( "x" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) ) {
                  LogUtil.outMessage( "TAG: ".concat( xmlReader.tagName ) );
                  if ( xmlReader.tagName.equals( "title" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) {
                    Label label = new Label( xmlReader.body );
                    label.setTitle( true );
                    objects.addElement( label );
                    continue;
                  }
                  if ( xmlReader.tagName.equals( "field" ) ) {
                    String fieldType = xmlReader.getAttrValue( "type", false );
                    String fieldVar = xmlReader.getAttrValue( "var", false );
                    String fieldLabel = xmlReader.getAttrValue( "label", true );
                    LogUtil.outMessage( "fieldType = ".concat( fieldType ) );
                    LogUtil.outMessage( "fieldVar = ".concat( fieldVar ) );
                    LogUtil.outMessage( "fieldLabel = " + ( fieldLabel ) );
                    Field field = null;
                    Check check = null;
                    RadioGroup radioGroup = null;
                    String radioValue = null;
                    if ( fieldType.equals( "text-single" ) ) {
                      if ( fieldLabel != null ) {
                        objects.addElement( new Label( fieldLabel ) );
                      }
                      field = new Field( "" );
                      field.setFocusable( true );
                      field.setFocused( isFocused );
                      isFocused = false;
                      field.setName( fieldVar );
                      field.setTitle( fieldLabel );
                      objects.addElement( field );
                    }
                    if ( fieldType.equals( "text-private" ) ) {
                      if ( fieldLabel != null ) {
                        objects.addElement( new Label( fieldLabel ) );
                      }
                      field = new Field( "" );
                      field.setFocusable( true );
                      field.setFocused( isFocused );
                      isFocused = false;
                      field.setName( fieldVar );
                      field.setTitle( fieldLabel );
                      field.constraints = TextField.PASSWORD;
                      objects.addElement( field );
                    }
                    if ( fieldType.equals( "boolean" ) ) {
                      check = new Check( fieldLabel, false );
                      check.setFocusable( true );
                      check.setFocused( isFocused );
                      isFocused = false;
                      check.setName( fieldVar );
                      objects.addElement( check );
                    }
                    if ( fieldType.equals( "list-single" ) ) {
                      if ( fieldLabel != null ) {
                        objects.addElement( new Label( fieldLabel ) );
                      }
                      radioGroup = new RadioGroup();
                      radioGroup.setName( fieldVar );
                      radioValue = null;
                    }
                    if ( fieldType.equals( "jid-multi" ) ) {
                      if ( fieldLabel != null ) {
                        objects.addElement( new Label( fieldLabel ) );
                      }
                      field = new Field( "" );
                      field.setFocusable( true );
                      field.setFocused( isFocused );
                      isFocused = false;
                      field.setName( fieldVar );
                      field.title = fieldLabel;
                      objects.addElement( field );
                    }
                    if ( xmlReader.tagType != XmlReader.TAG_SELFCLOSING ) {
                      while ( xmlReader.nextTag() && !( xmlReader.tagName.equals( "field" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) ) {
                        LogUtil.outMessage( "SUBTAG: ".concat( xmlReader.tagName ) );
                        if ( fieldType.equals( "hidden" ) && fieldVar.equals( "FORM_TYPE" ) ) {
                          if ( xmlReader.tagName.equals( "value" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) {
                            FORM_TYPE = xmlReader.getBody();
                          }
                        }
                        if ( fieldType.equals( "text-single" ) ) {
                          if ( xmlReader.tagName.equals( "value" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) {
                            field.setText( xmlReader.getBody() );
                          }
                        }
                        if ( fieldType.equals( "text-private" ) ) {
                          if ( xmlReader.tagName.equals( "value" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) {
                            field.setText( xmlReader.getBody() );
                          }
                        }
                        if ( fieldType.equals( "boolean" ) ) {
                          if ( xmlReader.tagName.equals( "value" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) {
                            check.setState( xmlReader.getBody().equals( "1" ) || xmlReader.getBody().equals( "true" ) );
                          }
                        }
                        if ( fieldType.equals( "list-single" ) ) {
                          if ( xmlReader.tagName.equals( "option" ) ) {
                            Radio radio = new Radio( xmlReader.getAttrValue( "label", false ), false );
                            radio.setFocusable( true );
                            radio.setFocused( isFocused );
                            isFocused = false;
                            while ( xmlReader.nextTag() && !( xmlReader.tagName.equals( "option" ) && ( xmlReader.tagType == XmlReader.TAG_CLOSING || xmlReader.tagType == XmlReader.TAG_SELFCLOSING ) ) ) {
                              if ( xmlReader.tagName.equals( "value" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) {
                                radio.setName( xmlReader.getBody() );
                              }
                            }
                            objects.addElement( radio );
                            radioGroup.addRadio( radio );
                            if ( radioValue != null ) {
                              if ( radio.getName().equals( radioValue ) ) {
                                radioGroup.setCombed( radio );
                              }
                            }
                          }
                          if ( xmlReader.tagName.equals( "value" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) {
                            radioValue = xmlReader.getBody();
                          }
                        }
                      }
                    }
                  }
                }
              }
              LogUtil.outMessage( "id: " + id );
              if ( id.startsWith( "grconffrm_get" ) ) {
                LogUtil.outMessage( "Settings info" );
                Handler.setGroupChatSettings( this.xmppAccountRoot, id, objects, FORM_TYPE );
              } else if ( id.startsWith( "grconffrm_set" ) ) {
                LogUtil.outMessage( "Settings info set" );
                Handler.setGroupChatResult( this.xmppAccountRoot, id );
              }
            } else if ( xmlReader.getAttrValue( "xmlns", false ).equals( "http://jabber.org/protocol/muc#admin" ) ) {
              LogUtil.outMessage( "http://jabber.org/protocol/muc#admin" );
              Vector items = new Vector();
              if ( xmlReader.tagType != XmlReader.TAG_SELFCLOSING ) {
                while ( xmlReader.nextTag() && !( xmlReader.tagName.equals( "query" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) ) {
                  LogUtil.outMessage( "TAG: ".concat( xmlReader.tagName ) );
                  if ( xmlReader.tagName.equals( ( "item" ) ) ) {
                    GroupChatUser groupChatUser = new GroupChatUser();
                    groupChatUser.role = xmlReader.getAttrValue( "role", false );
                    groupChatUser.affiliation = xmlReader.getAttrValue( "affiliation", false );
                    groupChatUser.nick = xmlReader.getAttrValue( "nick", false );
                    groupChatUser.jid = xmlReader.getAttrValue( "jid", false );
                    groupChatUser.title = groupChatUser.jid.concat( groupChatUser.nick.length() > 0 ? " (".concat( groupChatUser.nick ).concat( ")" ) : "" );
                    if ( xmlReader.tagType == XmlReader.TAG_PLAIN ) {
                      while ( xmlReader.nextTag() && !( xmlReader.tagName.equals( "item" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) ) {
                        if ( xmlReader.tagName.equals( "reason" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) {
                          groupChatUser.reason = xmlReader.body;
                        }
                      }
                    }
                    items.addElement( groupChatUser );
                  }
                }
              }
              if ( id.startsWith( "grchus_frm_" ) ) {
                LogUtil.outMessage( "Setting users list" );
                Handler.setGroupChatResult( this.xmppAccountRoot, id, items );
              } else if ( id.startsWith( "afladd_frm_" ) ) {
                LogUtil.outMessage( "Affiliation add result" );
                Handler.setAffiliationAddResult( xmppAccountRoot, id );
              }
            }
          } else if ( xmlReader.tagName.equals( "si" )/* && xmlReader.tagType == XmlReader.TAG_PLAIN*/ ) {
            if ( xmlReader.getAttrValue( "xmlns", false ).equals( "http://jabber.org/protocol/si" ) ) {
              boolean isOk = false;
              while ( xmlReader.nextTag() && !( xmlReader.tagName.equals( "feature" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) ) {
                if ( xmlReader.tagName.equals( "feature" ) && xmlReader.tagType != XmlReader.TAG_SELFCLOSING ) {
                  if ( xmlReader.getAttrValue( "xmlns", false ).equals( "http://jabber.org/protocol/feature-neg" ) ) {
                    xmlReader.nextTag();
                    if ( xmlReader.getAttrValue( "x", false ).equals( "jabber:x:data" ) && xmlReader.getAttrValue( "type", false ).equals( "submit" ) ) {
                      while ( xmlReader.nextTag() && !( xmlReader.tagName.equals( "field" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) ) {
                        if ( xmlReader.tagName.equals( "value" ) && xmlReader.tagType != XmlReader.TAG_CLOSING ) {
                          if ( xmlReader.body.equals( "http://jabber.org/protocol/ibb" ) ) {
                            isOk = true;
                          }
                        }
                      }
                    }
                  }
                }
              }
              if ( isOk = true ) {
                LogUtil.outMessage( "Receipment ".concat( from )
                        .concat( " is ok to receive file for id "
                        .concat( id ) ) );
                XmppIBBytestream xmppIBBytestream =
                        ( XmppIBBytestream ) xmppAccountRoot
                        .getTransactionManager()
                        .getTransaction( StringUtil
                        .stringToByteArray( id, true ) );
                if ( xmppIBBytestream != null ) {
                  xmppIBBytestream.sendStreamOpen();
                }
              }
            }
          } else {
            if ( id.startsWith( "transaction_" ) ) {
              /** Searching for int_id in transaction **/
              LogUtil.outMessage( "Searching for id in transaction" );
              final XmppIBBytestream xmppIBBytestream =
                      ( XmppIBBytestream ) xmppAccountRoot
                      .getTransactionManager().getTransaction(
                      StringUtil.stringToByteArray(
                      id.substring( "transaction_".length() ), true ) );
              if ( xmppIBBytestream != null ) {
                LogUtil.outMessage( "Transaction fround" );
                new Thread() {
                  public void run() {
                    /** Sending file **/
                    LogUtil.outMessage( "File receiving ack" );
                    xmppIBBytestream.startTransfer();
                  }
                }.start();
              }
            } else if ( id.startsWith( "transactionabort_" ) ) {
              LogUtil.outMessage( "Transaction abort received" );
            } else if ( id.startsWith( "transactioncomplete_" ) ) {
              LogUtil.outMessage( "Transaction complete received" );
            }
          }
        } else if ( type != null && type.equals( "error" ) ) {
          if ( xmlReader.getAttrValue( "xmlns", false ).equals( "http://jabber.org/protocol/disco#info" ) ) {
            if ( id.startsWith( "srvfrm_host" ) ) {
              LogUtil.outMessage( "No host info" );
            } else if ( id.startsWith( "srvfrm_info" ) ) {
              LogUtil.outMessage( "No service info" );
            }
          } else if ( xmlReader.getAttrValue( "xmlns", false ).equals( "http://jabber.org/protocol/muc#admin" ) ) {
            if ( id.startsWith( "grchus_frm_" ) ) {
              LogUtil.outMessage( "Permission denied" );
              MidletMain.groupChatUsersFrame.setError( xmppAccountRoot, id );
            }
          } else if ( xmlReader.getAttrValue( "xmlns", false ).equals( "http://jabber.org/protocol/muc#owner" ) ) {
            if ( id.startsWith( "grchus_frm_" ) ) {
              LogUtil.outMessage( "Permission denied" );
              MidletMain.groupChatUsersFrame.setError( xmppAccountRoot, id );
            }
          }
        } else if ( type != null && type.equals( "get" ) ) {
          if ( xmlReader.tagName.equals( "query" ) ) {
            if ( xmlReader.getAttrValue( "xmlns", false ).equals( "http://jabber.org/protocol/disco#info" ) /*&& xmlReader.getAttrValue("node", false).startsWith("http://tomclaw.com/mandarin_im/caps")*/ ) {
              XmppSender.sendDiscoInfo( this, from, id, xmlReader.getAttrValue( "node", true ) );
            } else if ( xmlReader.getAttrValue( "xmlns", false ).equals( "jabber:iq:last" ) ) {
              XmppSender.sendLast( this, from, id );
            }
          }
          if ( xmlReader.getAttrValue( "xmlns", false ).equals( "jabber:iq:version" ) ) {
            if ( type.equals( "get" ) && xmlReader.tagType == XmlReader.TAG_SELFCLOSING ) {
              /** Client info requesting **/
              XmppSender.sendClientVersion( this, from, id );
            }
          }
          if ( xmlReader.tagName.equals( "time" ) && xmlReader.getAttrValue( "xmlns", false ).equals( "urn:xmpp:time" ) ) {
            XmppSender.sendTime( this, from, id );
          }
        } else if ( type != null && type.equals( "set" ) ) {
          if ( xmlReader.tagName.equals( "si" ) ) {
            if ( xmlReader.getAttrValue( "xmlns", false ).equals( "http://jabber.org/protocol/si" ) ) {
              String sid = xmlReader.getAttrValue( "id", false );
              String mimeType = xmlReader.getAttrValue( "mime-type", false );
              String profile = xmlReader.getAttrValue( "profile", false );
              String name = "";
              String size = "";
              String desc = "";

              boolean isOk = false;
              boolean isFileBlock = false;
              while ( xmlReader.nextTag() && !( xmlReader.tagName.equals( "feature" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) ) {
                if ( xmlReader.tagName.equals( "feature" ) && xmlReader.tagType != XmlReader.TAG_SELFCLOSING ) {
                  if ( xmlReader.getAttrValue( "xmlns", false ).equals( "http://jabber.org/protocol/feature-neg" ) ) {
                    xmlReader.nextTag();
                    if ( xmlReader.getAttrValue( "x", false ).equals( "jabber:x:data" ) && xmlReader.getAttrValue( "type", false ).equals( "submit" ) ) {
                      while ( xmlReader.nextTag() && !( xmlReader.tagName.equals( "field" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) ) {
                        if ( xmlReader.tagName.equals( "value" ) && xmlReader.tagType != XmlReader.TAG_CLOSING ) {
                          if ( xmlReader.body.equals( "http://jabber.org/protocol/ibb" ) ) {
                            isOk = true;
                          }
                        }
                      }
                    }
                  }
                } else if ( xmlReader.tagName.equals( "file" ) ) {
                  if ( xmlReader.tagType == XmlReader.TAG_CLOSING ) {
                    isFileBlock = false;
                  }
                  if ( xmlReader.getAttrValue( "xmlns", false ).equals( "http://jabber.org/protocol/si/profile/file-transfer" ) ) {
                    name = xmlReader.getAttrValue( "name", false );
                    size = xmlReader.getAttrValue( "size", false );
                    isFileBlock = xmlReader.tagType == XmlReader.TAG_PLAIN;
                  }
                } else if ( xmlReader.tagName.equals( "desc" ) && xmlReader.tagType == XmlReader.TAG_CLOSING ) {
                  desc = xmlReader.body;
                }
              }
              if ( isOk = true ) {
                LogUtil.outMessage( "Receiving file from ".concat( from ).concat( " name: ".concat( name ).concat( " size: " ).concat( size ).concat( " desc: " ).concat( desc ) ) );
                final XmppIBBytestream directConnection = ( XmppIBBytestream ) xmppAccountRoot.getDirectConnectionInstance();
                directConnection.setIsReceivingFile( false );
                directConnection.setTransactionInfo( StringUtil.stringToByteArray( name, true ), MidletMain.incomingFilesFolder, Long.parseLong( size ), from );
                directConnection.generateCookie();
                directConnection.icbmCookie = StringUtil.stringToByteArray( sid, true );
                xmppAccountRoot.getTransactionManager().addTransaction( directConnection );

                new Thread() {
                  public void run() {
                    try {
                      LogUtil.outMessage( "Sending file receive" );
                      directConnection.receiveFile( id );
                    } catch ( IOException ex ) {
                      LogUtil.outMessage( "IOException: " + ex.getMessage(), true );
                    }
                  }
                }.start();
                xmppAccountRoot.getTransactionsFrame().transactionItemFrame = new TransactionItemFrame( directConnection );
                xmppAccountRoot.getTransactionsFrame().transactionItemFrame.s_prevWindow = xmppAccountRoot.getTransactionsFrame();
              }
            }
          } else if ( xmlReader.tagName.equals( "open" ) && xmlReader.getAttrValue( "xmlns", false ).equals( "http://jabber.org/protocol/ibb" ) ) {
            LogUtil.outMessage( "Sending file ack" );
            String sid = xmlReader.getAttrValue( "sid", false );
            String blockSize = xmlReader.getAttrValue( "block-size", false );
            String stanza = xmlReader.getAttrValue( "stanza", false );
            final XmppIBBytestream directConnection = ( XmppIBBytestream ) xmppAccountRoot.getTransactionManager().getTransaction( StringUtil.stringToByteArray( sid, true ) );
            directConnection.setParamsAndAck( blockSize, id );
          } else if ( xmlReader.tagName.equals( "close" ) && xmlReader.getAttrValue( "xmlns", false ).equals( "http://jabber.org/protocol/ibb" ) ) {
            LogUtil.outMessage( "Received file finish" );
            String sid = xmlReader.getAttrValue( "sid", false );
            final XmppIBBytestream directConnection = ( XmppIBBytestream ) xmppAccountRoot.getTransactionManager().getTransaction( StringUtil.stringToByteArray( sid, true ) );
            directConnection.closeFileAndAck( id );
          }
        }
        if ( isSelfClose ) {
          break;
        }
      }
    } catch ( Throwable ex1 ) {
      LogUtil.outMessage( "XmppSession: " + ex1.getMessage() );
    }
  }

  public String localizeString( String string ) {
    if ( string == null ) {
      return null;
    } else {
      return string;
    }
  }

  public String getId() {
    int_id++;
    return String.valueOf( int_id );
  }
}
