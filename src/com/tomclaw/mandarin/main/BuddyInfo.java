package com.tomclaw.mandarin.main;

import java.util.Vector;
import javax.microedition.lcdui.Image;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class BuddyInfo {

  public Vector extInfo;
  public String buddyId;
  public String nickName;
  public Image avatar;

  public BuddyInfo() {
    this( null, null );
  }

  public BuddyInfo( String buddyId, String nickName ) {
    extInfo = new Vector();
    this.buddyId = buddyId;
    this.nickName = nickName;
  }

  public void addKeyValue( String key, String value ) {
    extInfo.addElement( new KeyValue( key, value ) );
  }

  public int getKeyValueSize() {
    return extInfo.size();
  }

  public KeyValue getKeyValue( int i ) {
    return ( KeyValue ) extInfo.elementAt( i );
  }

  class KeyValue {

    public String key;
    public String value;

    public KeyValue( String key, String value ) {
      this.key = key;
      this.value = value;
    }
  }
}
