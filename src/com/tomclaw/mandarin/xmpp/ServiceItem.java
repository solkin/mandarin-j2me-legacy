package com.tomclaw.mandarin.xmpp;

import com.tomclaw.tcuilite.GroupChild;
import com.tomclaw.utils.LogUtil;
import java.util.Vector;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class ServiceItem extends GroupChild {

  public String jid = null;
  public String node = null;
  public Vector features = null;
  /** Identity **/
  public Vector identityes = null;

  public ServiceItem( String jid, String node, String name ) {
    super( "" );
    this.jid = jid;
    this.node = node;
    this.name = name;
    if ( name != null && name.length() > 0 ) {
      this.title = name;
    } else {
      this.title = jid;
    }
  }

  public Identity getIdentityByCategory( String category ) {
    if ( identityes != null && !identityes.isEmpty() ) {
      for ( int c = 0; c < identityes.size(); c++ ) {
        if ( ( ( Identity ) identityes.elementAt( c ) ).category.equals( category ) ) {
          return ( Identity ) identityes.elementAt( c );
        }
      }
    }
    return null;
  }

  public Identity getIdentityByType( String type ) {
    if ( identityes != null && !identityes.isEmpty() ) {
      for ( int c = 0; c < identityes.size(); c++ ) {
        if ( ( ( Identity ) identityes.elementAt( c ) ).type.equals( type ) ) {
          return ( Identity ) identityes.elementAt( c );
        }
      }
    }
    return null;
  }

  public boolean containsFeature( String feature ) {
    LogUtil.outMessage( "Features present: " + ( features != null ) );
    if ( features != null ) {
      for ( int c = 0; c < features.size(); c++ ) {
        LogUtil.outMessage( ( String ) features.elementAt( c ) );
        if ( ( ( String ) features.elementAt( c ) ).equals( feature ) ) {
          return true;
        }
      }
    }
    return false;
  }
}
