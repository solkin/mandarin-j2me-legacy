package com.tomclaw.mandarin.xmpp;

import com.tomclaw.mandarin.main.MidletMain;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;
import java.io.*;
import java.util.EmptyStackException;
import java.util.Stack;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class XmlWriter {

  private OutputStream writer;
  private java.io.ByteArrayOutputStream baos;
  private Stack tags;
  boolean inside_tag;

  public XmlWriter( final OutputStream out ) throws UnsupportedEncodingException {
    writer = new DataOutputStream( out/*, "UTF-8"*/ );
    baos = new ByteArrayOutputStream();
    this.tags = new Stack();
    this.inside_tag = false;
  }

  public void close() {
    try {
      writer.close();
      baos.close();
    } catch ( IOException e ) {
    }
  }

  public void flush() throws IOException {
    if ( this.inside_tag ) {
      baos.write( '>' ); // prevent Invalid XML fatal error
      this.inside_tag = false;
    }
    baos.flush();
    writer.write( baos.toByteArray() );
    MidletMain.incrementDataCount( baos.size() );
    baos.reset();
    writer.flush();
  }

  public void writeDirect( byte[] data ) throws IOException {
    writer.write( data );
    writer.flush();
    MidletMain.incrementDataCount( data.length );
  }

  public void startTag( final String tag ) throws IOException {
    if ( this.inside_tag ) {
      baos.write( '>' );
    }

    baos.write( '<' );
    baos.write( tag.getBytes() );
    this.tags.push( tag );
    this.inside_tag = true;
  }

  public void attribute( final String atr, final String value ) throws IOException {
    if ( value == null ) {
      return;
    }
    baos.write( ' ' );
    baos.write( atr.getBytes() );
    baos.write( "=\'".getBytes() );
    writeEscaped( value );
    baos.write( '\'' );
  }

  public void endTag() throws IOException {
    try {
      final String tagname = ( String ) this.tags.pop();
      if ( this.inside_tag ) {
        baos.write( "/>".getBytes() );
        this.inside_tag = false;
      } else {
        baos.write( "</".getBytes() );
        baos.write( tagname.getBytes() );
        baos.write( '>' );
      }
    } catch ( final EmptyStackException e ) {
    }
  }

  public void text( final String str ) throws IOException {
    if ( this.inside_tag ) {
      baos.write( '>' );
      this.inside_tag = false;
    }
    writeEscaped( str );
  }

  private void writeEscaped( final String str ) throws IOException {
    /*final int index = 0;
     for (int i = 0; i < str.length(); i++) {
     final char c = str.charAt(i);
     switch (c) {
     case '<':
     writer.write("&lt;");
     case '>':
     writer.write("&gt;");
     case '&':
     writer.write("&amp;");
     case '\'':
     writer.write("&apos;");
     case '"':
     writer.write("&quot;");
     default:
     writer.write(c);
     }
     }*/
    baos.write( encodeUTF( StringUtil.toXmlWellFormed( str ) ) );
  }

  public static byte[] encodeUTF( final String str ) {
    LogUtil.outMessage( "Encoding UTF" );
    byte[] encoded = StringUtil.stringToByteArray( str, true );
    LogUtil.outMessage( "UTF encoded" );
    return encoded; // new String(str.getBytes("UTF-8"));
  }
};
