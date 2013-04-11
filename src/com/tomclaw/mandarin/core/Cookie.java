package com.tomclaw.mandarin.core;

import com.tomclaw.utils.DataUtil;
import com.tomclaw.utils.StringUtil;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class Cookie {

  public byte[] cookie;
  public long cookieValue;
  public String cookieString;

  public Cookie() {
    cookieString = StringUtil.generateString( 4 );
    cookie = cookieString.getBytes();
    cookieValue = DataUtil.get32( cookie, 0, false );
  }

  public Cookie( byte[] cookie ) {
    this.cookie = cookie;
    cookieValue = DataUtil.get32( cookie, 0, false );
    cookieString = new String( cookie );
  }

  public Cookie( long cookieValue ) {
    cookie = new byte[ 4 ];
    this.cookieValue = cookieValue;
    DataUtil.put32_reversed( cookie, 0, cookieValue );
    cookieString = new String( cookie );
  }

  public Cookie( String cookieString ) {
    cookie = new byte[ 4 ];
    this.cookieString = cookieString;
    cookie = cookieString.getBytes();
    cookieValue = DataUtil.get32( cookie, 0, false );
  }
}
