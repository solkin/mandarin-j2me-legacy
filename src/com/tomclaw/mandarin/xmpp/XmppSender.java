package com.tomclaw.mandarin.xmpp;

import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.tcuilite.Check;
import com.tomclaw.tcuilite.Field;
import com.tomclaw.tcuilite.PaneObject;
import com.tomclaw.tcuilite.Radio;
import com.tomclaw.utils.Base64;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.TimeUtil;
import java.io.IOException;
import java.util.Vector;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class XmppSender {

  public static void setStatus( XmlWriter xmlWriter, String from, String statusId, String status, int priority ) throws IOException {
    if ( statusId.equals( "invisible" ) ) {
      sendPresence( xmlWriter, from, null, "invisible", null, null, priority, false, null, null );
    } else {
      sendPresence( xmlWriter, from, null, null, statusId, status, priority, false, null, null );
    }
  }

  public static void joinConfrence( XmppSession xmppSession, String id, String jid, String nick, String password ) throws IOException {
    sendPresence( xmppSession.xmlWriter, null, jid.concat( "/" ).concat( nick ), null, XmppStatusUtil.statuses[xmppSession.xmppAccountRoot.getStatusIndex()], "", 5, true, id, password );
  }

  public static void sendPresence( XmlWriter xmlWriter, String from, String to, String type, String statusId, String status, int priority, boolean isConference, String id, String password ) throws IOException {
    xmlWriter.startTag( "presence" );
    if ( from != null && !isConference ) {
      xmlWriter.attribute( "from", from );
    }
    if ( type != null && !isConference ) {
      xmlWriter.attribute( "type", type );
    }
    if ( to != null ) {
      xmlWriter.attribute( "to", to );
    }
    if ( isConference ) {
      xmlWriter.attribute( "xml:lang", "ru" );
      xmlWriter.attribute( "xmlns", "jabber:client" );
      xmlWriter.attribute( "id", id );
    }
    if ( statusId != null ) {
      xmlWriter.startTag( "show" );
      xmlWriter.text( statusId );
      xmlWriter.endTag();
    }
    if ( status != null ) {
      xmlWriter.startTag( "status" );
      xmlWriter.text( status );
      xmlWriter.endTag();
    }
    if ( priority != 0 ) {
      xmlWriter.startTag( "priority" );
      xmlWriter.text( Integer.toString( priority ) );
      xmlWriter.endTag();
    }
    if ( isConference ) {
      xmlWriter.startTag( "x" );
      xmlWriter.attribute( "xmlns", "http://jabber.org/protocol/muc" );
      if ( password != null ) {
        xmlWriter.startTag( "password" );
        xmlWriter.text( password );
        xmlWriter.endTag();
      }
      xmlWriter.endTag();
    }
    String version = MidletMain.version + " "
            + MidletMain.type + "-build " + MidletMain.build;
    xmlWriter.startTag( "c" );
    xmlWriter.attribute( "xmlns", "http://jabber.org/protocol/caps" );
    xmlWriter.attribute( "node", "http://tomclaw.com/mandarin_im/caps" );
    xmlWriter.attribute( "ver", version );
    xmlWriter.endTag();
    xmlWriter.endTag();
    xmlWriter.flush();
  }

  public static void sendRosterRequest( XmppSession xmppSession ) throws IOException {

    xmppSession.xmlWriter.startTag( "iq" );
    xmppSession.xmlWriter.attribute( "from", xmppSession.xmppAccountRoot.userId.concat( "/" ).concat( xmppSession.xmppAccountRoot.resource ) );
    xmppSession.xmlWriter.attribute( "type", "get" );

    xmppSession.xmlWriter.startTag( "query" );
    xmppSession.xmlWriter.attribute( "xmlns", "jabber:iq:roster" );
    xmppSession.xmlWriter.endTag();

    xmppSession.xmlWriter.endTag();
    xmppSession.xmlWriter.flush();
  }

  public static String sendMessage( XmppSession xmppSession, String to, String body, String type, boolean isSubject ) {
    String id = xmppSession.getId();
    try {
      LogUtil.outMessage( "Message start" );
      xmppSession.xmlWriter.startTag( "message" );
      if ( isSubject ) {
        xmppSession.xmlWriter.attribute( "xmlns", "jabber:client" );
        xmppSession.xmlWriter.attribute( "id", id );
      }
      xmppSession.xmlWriter.attribute( "type", type );
      xmppSession.xmlWriter.attribute( "to", to );
      xmppSession.xmlWriter.startTag( isSubject ? "subject" : "body" );
      LogUtil.outMessage( "Coding body" );
      xmppSession.xmlWriter.text( body );
      LogUtil.outMessage( "Body coded" );
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.flush();
      LogUtil.outMessage( "Message sent" );
    } catch ( final Exception e ) {
      // e.printStackTrace();
      LogUtil.outMessage( "ERR: " + e.getMessage() );
    }
    return id;
  }

  public static void queryingForInformation( XmppSession xmppSession, String to, String id ) {
    try {
      LogUtil.outMessage( xmppSession.xmppAccountRoot.username.concat( "@" ).concat( xmppSession.xmppAccountRoot.domain ).concat( "/" ).concat( xmppSession.xmppAccountRoot.resource ) );
      xmppSession.xmlWriter.startTag( "iq" );
      // xmppSession.xmlWriter.attribute("xmlns", "jabber:client");
      xmppSession.xmlWriter.attribute( "type", "get" );
      xmppSession.xmlWriter.attribute( "from", xmppSession.xmppAccountRoot.username.concat( "@" ).concat( xmppSession.xmppAccountRoot.domain ).concat( "/" ).concat( xmppSession.xmppAccountRoot.resource ) );
      xmppSession.xmlWriter.attribute( "to", to );
      if ( id != null ) {
        xmppSession.xmlWriter.attribute( "id", id );
      }
      xmppSession.xmlWriter.startTag( "query" );
      xmppSession.xmlWriter.attribute( "xmlns", "http://jabber.org/protocol/disco#info" );
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.flush();
    } catch ( final Exception e ) {
      // e.printStackTrace();
      LogUtil.outMessage( "ERR: " + e.getMessage() );
    }
  }

  public static void requesingAllItems( XmppSession xmppSession, String to, String node, String id ) {
    try {
      LogUtil.outMessage( xmppSession.xmppAccountRoot.username.concat( "@" ).concat( xmppSession.xmppAccountRoot.domain ).concat( "/" ).concat( xmppSession.xmppAccountRoot.resource ) );
      xmppSession.xmlWriter.startTag( "iq" );
      // xmppSession.xmlWriter.attribute("xmlns", "jabber:client");
      xmppSession.xmlWriter.attribute( "type", "get" );
      xmppSession.xmlWriter.attribute( "from", xmppSession.xmppAccountRoot.username.concat( "@" ).concat( xmppSession.xmppAccountRoot.domain ).concat( "/" ).concat( xmppSession.xmppAccountRoot.resource ) );
      xmppSession.xmlWriter.attribute( "to", to );
      if ( id != null ) {
        xmppSession.xmlWriter.attribute( "id", id );
      }
      xmppSession.xmlWriter.startTag( "query" );
      xmppSession.xmlWriter.attribute( "xmlns", "http://jabber.org/protocol/disco#items" );
      if ( node != null ) {
        xmppSession.xmlWriter.attribute( "node", node );
      }
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.flush();
    } catch ( final Exception e ) {
      // e.printStackTrace();
      LogUtil.outMessage( "ERR: " + e.getMessage() );
    }
  }

  public static void requesBookmarks( XmppSession xmppSession, String id ) {
    /*
     <!-- Out -->
     <iq xmlns="jabber:client" type="get" int_id="86">
     <query xmlns="jabber:iq:private">
     <storage xmlns="storage:bookmarks"/>
     </query>
     </iq>
     */
    try {
      LogUtil.outMessage( xmppSession.xmppAccountRoot.username.concat( "@" ).concat( xmppSession.xmppAccountRoot.domain ).concat( "/" ).concat( xmppSession.xmppAccountRoot.resource ) );
      xmppSession.xmlWriter.startTag( "iq" );
      xmppSession.xmlWriter.attribute( "xmlns", "jabber:client" );
      xmppSession.xmlWriter.attribute( "type", "get" );
      if ( id != null ) {
        xmppSession.xmlWriter.attribute( "id", id );
      }
      xmppSession.xmlWriter.startTag( "query" );
      xmppSession.xmlWriter.attribute( "xmlns", "jabber:iq:private" );
      xmppSession.xmlWriter.startTag( "storage" );
      xmppSession.xmlWriter.attribute( "xmlns", "storage:bookmarks" );
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.flush();
    } catch ( final Exception e ) {
      // e.printStackTrace();
      LogUtil.outMessage( "ERR: " + e.getMessage() );
    }
  }

  public static void exitConfrence( XmppSession xmppSession, String jid, String nick ) {
    /*
     <presence to="mandarin@conference.xmpp.ru/solkin_" type="unavailable">
     <status>Я использую Miranda Me (http://miranda-me.ru/)</status>
     </presence>
     */
    try {
      LogUtil.outMessage( xmppSession.xmppAccountRoot.username.concat( "@" ).concat( xmppSession.xmppAccountRoot.domain ).concat( "/" ).concat( xmppSession.xmppAccountRoot.resource ) );
      xmppSession.xmlWriter.startTag( "presence" );
      xmppSession.xmlWriter.attribute( "to", jid.concat( "/" ).concat( nick ) );
      xmppSession.xmlWriter.attribute( "type", "unavailable" );
      xmppSession.xmlWriter.startTag( "status" );
      xmppSession.xmlWriter.text( "Mandarin IM was here" );
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.flush();
    } catch ( final Exception e ) {
      // e.printStackTrace();
      LogUtil.outMessage( "ERR: " + e.getMessage() );
    }
  }

  public static void sendBookmarks( XmppSession xmppSession, String id, Vector items ) {
    /**
     <iq xmlns="jabber:client" type="set" int_id="380">
     <query xmlns="jabber:iq:private">
     <storage xmlns="storage:bookmarks">
     <conference minimize="0" jid="mandarin@conference.xmpp.ru" autojoin="0" name="mandarin">
     <nick>solkin</nick>
     </conference>
     </storage>
     </query>
     </iq>
     */
    try {
      LogUtil.outMessage( xmppSession.xmppAccountRoot.username.concat( "@" ).concat( xmppSession.xmppAccountRoot.domain ).concat( "/" ).concat( xmppSession.xmppAccountRoot.resource ) );
      xmppSession.xmlWriter.startTag( "iq" );
      xmppSession.xmlWriter.attribute( "xmlns", "jabber:client" );
      xmppSession.xmlWriter.attribute( "type", "set" );
      if ( id != null ) {
        xmppSession.xmlWriter.attribute( "id", id );
      }
      xmppSession.xmlWriter.startTag( "query" );
      xmppSession.xmlWriter.attribute( "xmlns", "jabber:iq:private" );
      xmppSession.xmlWriter.startTag( "storage" );
      xmppSession.xmlWriter.attribute( "xmlns", "storage:bookmarks" );

      Bookmark bookmark;
      for ( int c = 0; c < items.size(); c++ ) {
        bookmark = ( Bookmark ) items.elementAt( c );
        xmppSession.xmlWriter.startTag( "conference" );
        xmppSession.xmlWriter.attribute( "jid", bookmark.jid );
        xmppSession.xmlWriter.attribute( "name", bookmark.name );
        xmppSession.xmlWriter.attribute( "minimize", bookmark.minimize ? "1" : "0" );
        xmppSession.xmlWriter.attribute( "autojoin", bookmark.autojoin ? "1" : "0" );
        xmppSession.xmlWriter.startTag( "nick" );
        xmppSession.xmlWriter.text( bookmark.nick );
        xmppSession.xmlWriter.endTag();
        if ( bookmark.password != null && bookmark.password.length() > 0 ) {
          xmppSession.xmlWriter.startTag( "password" );
          xmppSession.xmlWriter.text( bookmark.password );
          xmppSession.xmlWriter.endTag();
        }
        xmppSession.xmlWriter.endTag();
      }

      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.flush();
      LogUtil.outMessage( "Bookmarks sent" );
    } catch ( final Exception e ) {
      // e.printStackTrace();
      LogUtil.outMessage( "ERR: " + e.getMessage() );
    }
  }

  public static void sendClientVersion( XmppSession xmppSession, String jid, String id ) {
    try {
      String version = MidletMain.version + " "
              + MidletMain.type + "-build " + MidletMain.build;
      String device = null;
      try {
        device = System.getProperty( "microedition.platform" ) + " [" + System.getProperty( "microedition.configuration" ) + "]";
      } catch ( Throwable ex1 ) {
      }
      if ( device == null ) {
        device = "J2ME";
      }
      LogUtil.outMessage( "version=".concat( version ) );
      LogUtil.outMessage( "os=".concat( device ) );
      LogUtil.outMessage( "sendClientVersion to ".concat( jid ) );
      xmppSession.xmlWriter.startTag( "iq" );
      xmppSession.xmlWriter.attribute( "to", jid );
      xmppSession.xmlWriter.attribute( "xml:lang", "ru" );
      xmppSession.xmlWriter.attribute( "id", id );
      xmppSession.xmlWriter.attribute( "type", "result" );
      xmppSession.xmlWriter.startTag( "query" );
      xmppSession.xmlWriter.attribute( "xmlns", "jabber:iq:version" );
      xmppSession.xmlWriter.startTag( "name" );
      xmppSession.xmlWriter.text( "Mandarin IM" );
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.startTag( "version" );
      xmppSession.xmlWriter.text( version );
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.startTag( "os" );
      xmppSession.xmlWriter.text( device );
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.flush();
      LogUtil.outMessage( "Client info sent" );
    } catch ( final Exception e ) {
      // e.printStackTrace();
      LogUtil.outMessage( "ERR: " + e.getMessage() );
    }
  }

  public static void requestGroupChatSettings( XmppSession xmppSession, String groupChatJid, String id ) throws IOException {
    /* 
     <iq type="get" to="mandarin@conference.xmpp.ru" int_id="mir_35">
     <query xmlns="http://jabber.org/protocol/muc#owner" />
     </iq>
     */
    xmppSession.xmlWriter.startTag( "iq" );
    xmppSession.xmlWriter.attribute( "type", "get" );
    xmppSession.xmlWriter.attribute( "to", groupChatJid );
    xmppSession.xmlWriter.attribute( "id", id );

    xmppSession.xmlWriter.startTag( "query" );
    xmppSession.xmlWriter.attribute( "xmlns", "http://jabber.org/protocol/muc#owner" );
    xmppSession.xmlWriter.endTag();

    xmppSession.xmlWriter.endTag();
    xmppSession.xmlWriter.flush();
  }

  public static void sendGroupChatSettings( XmppSession xmppSession, String groupChatJid, Vector items, String FORM_TYPE, String id ) throws IOException {
    /*
     <iq type="set" to="lager@conference.xmpp.ru" int_id="mir_86">
     <query xmlns="http://jabber.org/protocol/muc#owner">
     <x xmlns="jabber:x:data" type="submit">
     <field var="FORM_TYPE">
     <value>http://jabber.org/protocol/muc#roomconfig</value>
     </field>
     <field var="muc#roomconfig_roomname">
     <value></value>
     </field>
     </x>
     </query>
     </iq>
     */
    xmppSession.xmlWriter.startTag( "iq" );
    xmppSession.xmlWriter.attribute( "type", "set" );
    xmppSession.xmlWriter.attribute( "to", groupChatJid );
    xmppSession.xmlWriter.attribute( "id", id );

    xmppSession.xmlWriter.startTag( "query" );
    xmppSession.xmlWriter.attribute( "xmlns", "http://jabber.org/protocol/muc#owner" );

    xmppSession.xmlWriter.startTag( "x" );
    xmppSession.xmlWriter.attribute( "xmlns", "jabber:x:data" );
    xmppSession.xmlWriter.attribute( "type", "submit" );

    if ( FORM_TYPE != null ) {
      xmppSession.xmlWriter.startTag( "field" );
      xmppSession.xmlWriter.attribute( "var", "FORM_TYPE" );
      xmppSession.xmlWriter.startTag( "value" );
      xmppSession.xmlWriter.text( FORM_TYPE );
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.endTag();
    }

    for ( int c = 0; c < items.size(); c++ ) {
      PaneObject paneObject = ( PaneObject ) items.elementAt( c );
      if ( paneObject.getName() != null ) {
        if ( paneObject instanceof Field ) {
          xmppSession.xmlWriter.startTag( "field" );
          xmppSession.xmlWriter.attribute( "var", paneObject.getName() );
          xmppSession.xmlWriter.startTag( "value" );
          xmppSession.xmlWriter.text( ( ( Field ) paneObject ).getText() );
          xmppSession.xmlWriter.endTag();
          xmppSession.xmlWriter.endTag();
        }
        if ( paneObject instanceof Check ) {
          xmppSession.xmlWriter.startTag( "field" );
          xmppSession.xmlWriter.attribute( "var", paneObject.getName() );
          xmppSession.xmlWriter.startTag( "value" );
          xmppSession.xmlWriter.text( ( ( Check ) paneObject ).state ? "1" : "0" );
          xmppSession.xmlWriter.endTag();
          xmppSession.xmlWriter.endTag();
        }
        if ( paneObject instanceof Radio ) {
          if ( ( ( Radio ) paneObject ).radioState ) {
            xmppSession.xmlWriter.startTag( "field" );
            xmppSession.xmlWriter.attribute( "var", ( ( Radio ) paneObject ).radioGroup.getName() );
            xmppSession.xmlWriter.startTag( "value" );
            xmppSession.xmlWriter.text( ( ( Radio ) paneObject ).getName() );
            xmppSession.xmlWriter.endTag();
            xmppSession.xmlWriter.endTag();
          }
        }
      }
    }
    xmppSession.xmlWriter.endTag();
    xmppSession.xmlWriter.endTag();
    xmppSession.xmlWriter.endTag();
    xmppSession.xmlWriter.flush();
  }

  public static void requestGroupChatLists( XmppSession xmppSession, String groupChatJid, String id, String itemType, String itemValue ) throws IOException {
    /*
     <iq type="get" to="mandarin@conference.xmpp.ru" int_id="mir_10">
     <query xmlns="http://jabber.org/protocol/muc#admin">
     <item role="participant" />
     </query>
     </iq>
     */
    xmppSession.xmlWriter.startTag( "iq" );
    xmppSession.xmlWriter.attribute( "type", "get" );
    xmppSession.xmlWriter.attribute( "to", groupChatJid );
    xmppSession.xmlWriter.attribute( "id", id );

    xmppSession.xmlWriter.startTag( "query" );
    xmppSession.xmlWriter.attribute( "xmlns", "http://jabber.org/protocol/muc#admin" );

    xmppSession.xmlWriter.startTag( "item" );
    xmppSession.xmlWriter.attribute( itemType, itemValue );
    xmppSession.xmlWriter.endTag();

    xmppSession.xmlWriter.endTag();

    xmppSession.xmlWriter.endTag();
    xmppSession.xmlWriter.flush();
  }

  public static void affiliationAddGroupChatLists( XmppSession xmppSession, String groupChatJid, String id, String itemJid, String itemAffl, String reason ) throws IOException {
    /*
     <iq type="set" to="mandarin@conference.xmpp.ru" int_id="mir_34">
     <query xmlns="http://jabber.org/protocol/muc#admin">
     <item jid="solkin@limun.org" affiliation="none" />
     </query>
     </iq>
     */
    xmppSession.xmlWriter.startTag( "iq" );
    xmppSession.xmlWriter.attribute( "type", "set" );
    xmppSession.xmlWriter.attribute( "to", groupChatJid );
    xmppSession.xmlWriter.attribute( "id", id );

    xmppSession.xmlWriter.startTag( "query" );
    xmppSession.xmlWriter.attribute( "xmlns", "http://jabber.org/protocol/muc#admin" );

    xmppSession.xmlWriter.startTag( "item" );
    xmppSession.xmlWriter.attribute( "jid", itemJid );
    xmppSession.xmlWriter.attribute( "affiliation", itemAffl );
    xmppSession.xmlWriter.startTag( "reason" );
    xmppSession.xmlWriter.text( reason );
    xmppSession.xmlWriter.endTag();
    xmppSession.xmlWriter.endTag();

    xmppSession.xmlWriter.endTag();

    xmppSession.xmlWriter.endTag();
    xmppSession.xmlWriter.flush();
  }

  public static void ibbOpen( XmppSession xmppSession, String jid, String id, String sid, int blockSize, String stanza ) throws IOException {
    /*
     <iq type="set" to="solkin@xmpp.ru/Miranda" int_id="mir_59">
     <open xmlns="http://jabber.org/protocol/ibb" sid="47241386" block-size="2048" stanza="message" />
     </iq>
     */
    xmppSession.xmlWriter.startTag( "iq" );
    xmppSession.xmlWriter.attribute( "type", "set" );
    xmppSession.xmlWriter.attribute( "to", jid );
    xmppSession.xmlWriter.attribute( "id", id );

    xmppSession.xmlWriter.startTag( "open" );
    xmppSession.xmlWriter.attribute( "xmlns", "http://jabber.org/protocol/ibb" );
    xmppSession.xmlWriter.attribute( "sid", sid );
    xmppSession.xmlWriter.attribute( "block-size", String.valueOf( blockSize ) );
    xmppSession.xmlWriter.attribute( "stanza", stanza );
    xmppSession.xmlWriter.endTag();

    xmppSession.xmlWriter.endTag();
    xmppSession.xmlWriter.flush();
  }

  public static void sendIBBFileBlockMessage( XmppSession xmppSession, String jid, String id, String sid, int seq, byte[] data, int offset, int length ) throws IOException {
    /*
     <message to="ezdovoi_kot@jabber.ru/BombusQD" int_id="mir_60">
     <data xmlns="http://jabber.org/protocol/ibb" sid="47241386" seq="0">/726vf+tqq3/5+Pn/9bT1v/W09b/ra6t/0JBQv/e397/tbLn/6Wezv/Gvvf/3tv3/7Wq7/+lnu//rabv/73H//+Mmv//tarv/97b9/9VVVX/QkFC//////97htr/vcf//4ya//9VVVX//////7CL8P/T0f//vcf//4ya///e397//////woKCv8pLCn/AAAA/zk4Of8AAID/AAAQ/1VVVf//////1MXt//X1//+9x///jJr////3/////v//vbq9/62qrf/n4+f/1tPW/9bT1v+trq3/QkFC/97f3v+1suf/pZ7O/8a+9//e2/f/tarv/6We7/+tpu//vcf//4ya////////3d3d///////d3d3/qqqq/6qqqv+IiIj/3d3d/1VVVf//////3d3d/93d//+7qu7/zMzu/6qqzP//////9/P3///////d3d3/qqqq/6qqqv+IiIj/3d3d/5ndK/+GvQP/tehe/3jPF/8AAAD/lqN7/1VVVf//////sIvw/9PR//+9x///jJr//1VVVf//////3d3d/93d//+7qu7/zMzu/6qqzP//////9/P3///////d3d3/qqqq/6qqqv+IiIj/3d3d/1VVVf//////3d3d/93d//+7qu7/zMzu/6qqzP//////9/P3///////d3d3/qqqq/6qqqv+IiIj/3d3d//////8AAAD/7+/v/87Pzv/d3f//u6ru/8zM7v+qqsz///////fz9///////3d3d/6qqqv+qqqr/iIiI/93d3f8AAID/AAAQ/woKCv8pLCn/AAAA/wAAAP85ODn/VVVV//////+wi/D/09H//73H//+Mmv//vcf//4ya///XcwP/98Ji/+6WJ//qYwP//////3V7dP9VVVX//////93d3f/d3f//u6ru/8zM7v+qqsz/3d3d/6qqqg==</data>
     <amp xmlns="http://jabber.org/protocol/amp">
     <rule condition="deliver-at" value="stored" action="error" />
     <rule condition="match-resource" value="exact" action="error" />
     </amp>
     </message>
     */
    xmppSession.xmlWriter.startTag( "message" );
    xmppSession.xmlWriter.attribute( "to", jid );
    xmppSession.xmlWriter.attribute( "id", id );

    xmppSession.xmlWriter.startTag( "data" );
    xmppSession.xmlWriter.attribute( "xmlns", "http://jabber.org/protocol/ibb" );
    xmppSession.xmlWriter.attribute( "sid", sid );
    xmppSession.xmlWriter.attribute( "seq", String.valueOf( seq ) );
    xmppSession.xmlWriter.text( Base64.encode( data, offset, length ) );
    xmppSession.xmlWriter.endTag();

    xmppSession.xmlWriter.startTag( "amp" );
    xmppSession.xmlWriter.attribute( "xmlns", "http://jabber.org/protocol/amp" );
    xmppSession.xmlWriter.endTag();

    xmppSession.xmlWriter.startTag( "rule" );
    xmppSession.xmlWriter.attribute( "condition", "deliver-at" );
    xmppSession.xmlWriter.attribute( "value", "stored" );
    xmppSession.xmlWriter.attribute( "action", "error" );
    xmppSession.xmlWriter.endTag();

    xmppSession.xmlWriter.startTag( "rule" );
    xmppSession.xmlWriter.attribute( "condition", "match-resource" );
    xmppSession.xmlWriter.attribute( "value", "exact" );
    xmppSession.xmlWriter.attribute( "action", "error" );
    xmppSession.xmlWriter.endTag();

    xmppSession.xmlWriter.endTag();
    xmppSession.xmlWriter.flush();
  }

  public static void ibbClose( XmppSession xmppSession, String jid, String id, String sid ) throws IOException {
    /*
     <iq type="set" to="ezdovoi_kot@jabber.ru/BombusQD" int_id="mir_61">
     <close xmlns="http://jabber.org/protocol/ibb" sid="47241386" />
     </iq>
     */
    xmppSession.xmlWriter.startTag( "iq" );
    xmppSession.xmlWriter.attribute( "type", "set" );
    xmppSession.xmlWriter.attribute( "to", jid );
    xmppSession.xmlWriter.attribute( "id", id );

    xmppSession.xmlWriter.startTag( "close" );
    xmppSession.xmlWriter.attribute( "xmlns", "http://jabber.org/protocol/ibb" );
    xmppSession.xmlWriter.attribute( "sid", sid );
    xmppSession.xmlWriter.endTag();

    xmppSession.xmlWriter.endTag();
    xmppSession.xmlWriter.flush();
  }

  public static void sendDiscoInfo( XmppSession xmppSession, String jid, String id, String node ) throws IOException {
    /*
     <iq type="result" to="solkin@xmpp.ru/Miranda" int_id="mir_85">
     <query xmlns="http://jabber.org/protocol/disco#info">
     <identity category="client" type="pc" name="Miranda" />
     <feature var="http://jabber.org/protocol/disco#info" />
     <feature var="http://jabber.org/protocol/disco#items" />
     <feature var="http://jabber.org/protocol/caps" />
     <feature var="http://jabber.org/protocol/si" />
     <feature var="http://jabber.org/protocol/si/profile/file-transfer" />
     <feature var="http://jabber.org/protocol/bytestreams" />
     <feature var="http://jabber.org/protocol/ibb" />
     <feature var="jabber:iq:oob" />
     <feature var="http://jabber.org/protocol/commands" />
     <feature var="http://jabber.org/protocol/muc" />
     <feature var="http://jabber.org/protocol/chatstates" />
     <feature var="jabber:iq:last" />
     <feature var="jabber:iq:version" />
     <feature var="urn:xmpp:time" />
     <feature var="urn:xmpp:ping" />
     <feature var="jabber:x:data" />
     <feature var="jabber:x:event" />
     <feature var="vcard-temp" />
     <feature var="jabber:iq:agents" />
     <feature var="jabber:iq:browse" />
     <feature var="http://jabber.org/protocol/mood+notify" />
     <feature var="http://miranda-im.org/caps/secureim" />
     <feature var="jabber:iq:privacy" />
     <feature var="urn:xmpp:receipts" />
     <feature var="http://jabber.org/protocol/tune+notify" />
     <feature var="jabber:iq:private" />
     <feature var="urn:xmpp:attention:0" />
     <feature var="http://jabber.org/protocol/activity+notify" />
     <feature var="urn:xmpp:jingle:1" />
     <x xmlns="jabber:x:data" type="result">
     <field var="FORM_TYPE" type="hidden">
     <value>urn:xmpp:dataforms:softwareinfo</value>
     </field>
     <field var="os">
     <value>Microsoft Windows</value>
     </field>
     <field var="os_version">
     <value>7  (build 7600)</value>
     </field>
     <field var="software">
     <value>Miranda IM Jabber Protocol (Unicode)</value>
     </field>
     <field var="software_version">
     <value>0.9.6.0</value>
     </field>
     <field var="x-miranda-core-version">
     <value>0.9.6.0</value>
     </field>
     <field var="x-miranda-core-is-unicode">
     <value>1</value>
     </field>
     <field var="x-miranda-core-is-alpha">
     <value>0</value>
     </field>
     </x>
     </query>
     </iq>
     */

    String[] features = new String[]{
      "http://jabber.org/protocol/disco#info",
      "http://jabber.org/protocol/caps",
      "http://jabber.org/protocol/si",
      "http://jabber.org/protocol/si/profile/file-transfer",
      "http://jabber.org/protocol/ibb",
      "http://jabber.org/protocol/muc",
      "jabber:iq:version",
      "jabber:x:data",
      "jabber:iq:time"
    /*"jabber:iq:last","jabber:iq:oob","urn:xmpp:time","jabber:iq:version","jabber:x:conference","http://jabber.org/protocol/bytestreams","http://jabber.org/protocol/caps","http://jabber.org/protocol/chatstates","http://jabber.org/protocol/disco#info","http://jabber.org/protocol/disco#items","http://jabber.org/protocol/muc","http://jabber.org/protocol/muc#user","http://jabber.org/protocol/si","http://jabber.org/protocol/si/profile/file-transfer","http://jabber.org/protocol/xhtml-im","urn:xmpp:ping","urn:xmpp:attention:0","urn:xmpp:bob","urn:xmpp:jingle:1","http://www.google.com/xmpp/protocol/session","http://www.google.com/xmpp/protocol/voice/v1","http://www.google.com/xmpp/protocol/video/v1","http://www.google.com/xmpp/protocol/camera/v1","urn:xmpp:jingle:apps:rtp:1","urn:xmpp:jingle:apps:rtp:audio","urn:xmpp:jingle:apps:rtp:video","urn:xmpp:jingle:transports:raw-udp:1","urn:xmpp:jingle:transports:ice-udp:1","urn:xmpp:avatar:metadata+notify","http://jabber.org/protocol/mood+notify","http://jabber.org/protocol/tune+notify","http://jabber.org/protocol/nick+notify","http://jabber.org/protocol/ibb"*/
    };

    String platform = null;
    try {
      platform = System.getProperty( "microedition.platform" );
    } catch ( Throwable ex1 ) {
    }
    if ( platform == null ) {
      platform = "J2ME";
    }

    String configuration = null;
    try {
      configuration = System.getProperty( "microedition.configuration" );
    } catch ( Throwable ex1 ) {
    }
    if ( configuration == null ) {
      configuration = "J2ME";
    }

    String version = MidletMain.version + " "
            + MidletMain.type + "-build " + MidletMain.build;

    String[][] fields = new String[][]{{
        "FORM_TYPE",
        "os",
        "os_version",
        "software",
        "software_version"}, {
        "urn:xmpp:dataforms:softwareinfo",
        platform,
        configuration,
        "Mandarin IM",
        version}};
    xmppSession.xmlWriter.startTag( "iq" );
    xmppSession.xmlWriter.attribute( "type", "result" );
    xmppSession.xmlWriter.attribute( "to", jid );
    xmppSession.xmlWriter.attribute( "from", xmppSession.xmppAccountRoot.username.concat( "@" ).concat( xmppSession.xmppAccountRoot.domain ).concat( "/" ).concat( xmppSession.xmppAccountRoot.resource ) );
    xmppSession.xmlWriter.attribute( "id", id );

    xmppSession.xmlWriter.startTag( "query" );
    xmppSession.xmlWriter.attribute( "xmlns", "http://jabber.org/protocol/disco#info" );
    if ( node != null ) {
      xmppSession.xmlWriter.attribute( "node", node );
    }

    xmppSession.xmlWriter.startTag( "identity" );
    xmppSession.xmlWriter.attribute( "category", "client" );
    xmppSession.xmlWriter.attribute( "type", "pc" );
    xmppSession.xmlWriter.attribute( "name", "Mandarin" );
    xmppSession.xmlWriter.endTag();

    for ( int c = 0; c < features.length; c++ ) {
      xmppSession.xmlWriter.startTag( "feature" );
      xmppSession.xmlWriter.attribute( "var", features[c] );
      xmppSession.xmlWriter.endTag();
    }

    xmppSession.xmlWriter.startTag( "x" );
    xmppSession.xmlWriter.attribute( "xmlns", "jabber:x:data" );
    xmppSession.xmlWriter.attribute( "type", "result" );
    for ( int c = 0; c < fields[0].length; c++ ) {
      xmppSession.xmlWriter.startTag( "field" );
      xmppSession.xmlWriter.attribute( "var", fields[0][c] );
      if ( fields[0][c].equals( "FORM_TYPE" ) ) {
        xmppSession.xmlWriter.attribute( "type", "hidden" );
      }
      xmppSession.xmlWriter.startTag( "value" );
      xmppSession.xmlWriter.text( fields[1][c] );
      xmppSession.xmlWriter.endTag();
      xmppSession.xmlWriter.endTag();
    }
    xmppSession.xmlWriter.endTag();

    xmppSession.xmlWriter.endTag();

    xmppSession.xmlWriter.endTag();
    xmppSession.xmlWriter.flush();
  }

  public static void sendTime( XmppSession xmppSession, String jid, String id ) throws IOException {
    String utcTime = TimeUtil.getUtcTimeString( TimeUtil.getCurrentTime() );
    String tzoTime = ( ( TimeUtil.getGmtOffset() / 3600 ) > 0 ? "+" : "-" ).concat( ( TimeUtil.getGmtOffset() / 3600 ) >= 10 ? "" : "0" ).concat( String.valueOf( TimeUtil.getGmtOffset() / 3600 ) ).concat( ":00" );

    xmppSession.xmlWriter.startTag( "iq" );
    xmppSession.xmlWriter.attribute( "type", "result" );
    xmppSession.xmlWriter.attribute( "xmlns", "jabber:client" );
    xmppSession.xmlWriter.attribute( "to", jid );
    xmppSession.xmlWriter.attribute( "id", id );


    xmppSession.xmlWriter.startTag( "time" );
    xmppSession.xmlWriter.attribute( "xmlns", "urn:xmpp:time" );

    xmppSession.xmlWriter.startTag( "utc" );
    xmppSession.xmlWriter.text( utcTime );
    xmppSession.xmlWriter.endTag();

    xmppSession.xmlWriter.startTag( "tzo" );
    xmppSession.xmlWriter.text( tzoTime );
    xmppSession.xmlWriter.endTag();

    xmppSession.xmlWriter.endTag();

    xmppSession.xmlWriter.endTag();
    xmppSession.xmlWriter.flush();
  }

  public static void sendLast( XmppSession xmppSession, String jid, String id ) throws IOException {
    xmppSession.xmlWriter.startTag( "iq" );
    xmppSession.xmlWriter.attribute( "type", "result" );
    xmppSession.xmlWriter.attribute( "xmlns", "jabber:client" );
    xmppSession.xmlWriter.attribute( "to", jid );
    xmppSession.xmlWriter.attribute( "id", id );

    xmppSession.xmlWriter.startTag( "query" );
    xmppSession.xmlWriter.attribute( "xmlns", "jabber:iq:last" );
    xmppSession.xmlWriter.attribute( "seconds", "1" );
    xmppSession.xmlWriter.endTag();

    xmppSession.xmlWriter.endTag();
    xmppSession.xmlWriter.flush();
  }
}
