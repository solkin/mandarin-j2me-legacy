package com.tomclaw.mandarin.xmpp;

import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.utils.StringUtil;
import java.io.InputStream;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class XmlReader {

  public static final int TAG_UNKNOWN = 0xff;
  public static final int TAG_PLAIN = 0x00;
  public static final int TAG_QUESTION = 0x01;
  public static final int TAG_SELFCLOSING = 0x02;
  public static final int TAG_CLOSING = 0x03;
  public static final int TAG_COMMENT = 0x04;
  public InputStream inputStream;
  public String tagName = null;
  public int tagType = TAG_UNKNOWN;
  public String body;
  public java.util.Hashtable attributes = new java.util.Hashtable();

  public XmlReader( InputStream inputStream ) {
    this.inputStream = inputStream;
  }

  public boolean nextTag() throws Throwable {
    tagName = null;
    tagType = TAG_UNKNOWN;
    int read;
    int p_read = 0;
    StringBuffer buffer = new StringBuffer();
    boolean isReadTagName = false;
    boolean isCommentTag = false;
    boolean isReadAttributes = false;
    boolean isReadingAttrName = false;
    boolean isReadingAttrValue = false;
    boolean isAwaitingAttrName = false;
    boolean isAwaitingAttrValue = false;
    String mb_body = "";
    body = "";
    attributes.clear();
    String attribute = null;
    int tagSize = 0;
    while ( ( read = inputStream.read() ) != -1 ) {
      tagSize++;
      /*if (read == '\n' || read == '\t') {
       continue;
       }*/
      buffer.append( ( char ) read );
      if ( isReadTagName ) {
        if ( read == '?' ) {
          tagType = TAG_QUESTION;
          isReadAttributes = true;
          buffer.delete( 0, 1 );
        } else if ( read == '!' ) {
          tagType = TAG_COMMENT;
          isCommentTag = true;
          isReadTagName = false;
        } else if ( read == '/' ) {
          tagType = TAG_CLOSING;
          buffer.setLength( 0 );
          // buffer.trimToSize();
          isReadTagName = false;
          isReadAttributes = true;
          body = mb_body;
        } else if ( read == '>' ) {
          tagType = TAG_PLAIN;
          tagName = subChars( buffer, 0, buffer.length() - 1 );
          return true;
        } else if ( read == ' ' ) {
          tagType = TAG_PLAIN;
          tagName = subChars( buffer, 0, buffer.length() - 1 );
          isReadTagName = false;
          isReadAttributes = true;
          isAwaitingAttrName = true;
        }
      } else if ( isReadAttributes ) {
        if ( read == '>' ) {
          if ( tagType == TAG_CLOSING ) {
            tagName = subChars( buffer, 0, buffer.length() - 1 );
          } else if ( buffer.length() >= 2 && buffer.charAt( buffer.length() - 2 ) == '/' ) {
            tagType = TAG_SELFCLOSING;
          } else if ( buffer.length() >= 2 && buffer.charAt( buffer.length() - 2 ) == '?' ) {
            tagType = TAG_QUESTION;
          }
          return true;
        } else {
          if ( isAwaitingAttrName && read != ' ' && read != '\t' && read != '\n' ) {
            isAwaitingAttrName = false;
            isReadingAttrName = true;
            buffer.setLength( 0 );
            // buffer.trimToSize();
            buffer.append( ( char ) read );
          }
          if ( isReadingAttrName && ( read == '=' || read == ' ' || read == '\t' || read == '\n' ) ) {
            isReadingAttrName = false;
            isAwaitingAttrValue = true;
            buffer.setLength( buffer.length() - 1 );
            // buffer.trimToSize();
            attribute = buffer.toString();
          } else if ( isAwaitingAttrValue && ( read == '\"' || read == '\'' ) ) {
            isAwaitingAttrValue = false;
            isReadingAttrValue = true;
            buffer.setLength( 0 );
            // buffer.trimToSize();
          } else if ( isReadingAttrValue && ( read == '\'' || read == '\"' ) && p_read != '\\' ) {
            isReadingAttrValue = false;
            isAwaitingAttrName = true;
            attributes.put( attribute, subChars( buffer, 0, buffer.length() - 1 ) );
          }
        }
      } else if ( isCommentTag ) {
        if ( read == '>' ) {
          return true;
        }
      } else {
        if ( read == '<' ) {
          mb_body = subChars( buffer, 0, buffer.length() - 1 );
          buffer.setLength( 0 );
          isReadTagName = true;
        }
      }
      p_read = read;
    }
    MidletMain.incrementDataCount( tagSize );
    return false;
  }

  public String getAttrValue( String attribute, boolean isNullMayBe ) {
    String value = ( String ) attributes.get( attribute );
    return isNullMayBe ? value : ( value == null ? "" : value );
  }

  public String subChars( StringBuffer sb, int start, int end ) {
    return subChars( sb, start, end, true );
  }

  public String subChars( StringBuffer sb, int start, int end, boolean utf8 ) {
    byte[] data = new byte[end - start];
    for ( int c = 0; c < data.length; c++ ) {
      data[c] = ( byte ) sb.charAt( c );
    }
    return StringUtil.toStringFromXmlWellFormed( StringUtil.byteArrayToString( data, utf8 ) );
  }

  public String getBody() {
    return body == null ? "" : body;
  }
}
