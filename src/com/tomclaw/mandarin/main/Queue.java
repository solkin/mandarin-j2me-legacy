package com.tomclaw.mandarin.main;

import com.tomclaw.utils.LogUtil;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class Queue {

  private static Hashtable actions = new Hashtable();

  public static void pushQueueAction( QueueAction action ) {
    actions.put( action.getCookie().cookieString, action );
  }

  public static QueueAction popQueueAction( Cookie cookie ) {
    LogUtil.outMessage( "Actions count: " + actions.size() );

    Enumeration e = actions.elements();
    while ( e.hasMoreElements() ) {
      LogUtil.outMessage( ( ( QueueAction ) e.nextElement() ).getCookie().cookieString );
    }

    QueueAction queueAction = ( QueueAction ) actions.get( cookie.cookieString );
    if ( queueAction != null ) {
      LogUtil.outMessage( "QueueAction found!" );
      actions.remove( cookie.cookieString );
    }
    return queueAction;
  }

  public static void runQueueAction( Cookie cookie, Hashtable params ) {
    QueueAction queueAction = ( QueueAction ) actions.get( cookie.cookieString );
    if ( queueAction != null ) {
      LogUtil.outMessage( "QueueAction not null" );
      try {
        queueAction.actionPerformed( params );
      } catch ( Throwable ex ) {
        LogUtil.outMessage( ex );
      }
      actions.remove( cookie.cookieString );
    }
  }
}
