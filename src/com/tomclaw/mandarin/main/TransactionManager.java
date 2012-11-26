package com.tomclaw.mandarin.main;

import com.tomclaw.mandarin.dc.DirectConnection;
import com.tomclaw.utils.ArrayUtil;
import java.util.Vector;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class TransactionManager {

  public Vector transactions = new Vector();

  public DirectConnection addTransaction( DirectConnection directConnection ) {
    DirectConnection tempDirectConnection;
    for ( int c = 0; c < transactions.size(); c++ ) {
      tempDirectConnection = ( DirectConnection ) transactions.elementAt( c );
      if ( tempDirectConnection.isCompare( directConnection ) ) {
        // Transaction already exist
        return tempDirectConnection;
      }
    }
    transactions.addElement( directConnection );
    return directConnection;
  }

  public DirectConnection getTransaction( byte[] cookie ) {
    DirectConnection tempDirectConnection;
    for ( int c = 0; c < transactions.size(); c++ ) {
      tempDirectConnection = ( DirectConnection ) transactions.elementAt( c );
      if ( ArrayUtil.equals( tempDirectConnection.getSessCookie(), cookie ) ) {
        // Transaction exist
        return tempDirectConnection;
      }
    }
    return null;
  }

  public int getActiveTransactions() {
    DirectConnection tempDirectConnection;
    int actTrsCnt = 0;
    for ( int c = 0; c < transactions.size(); c++ ) {
      tempDirectConnection = ( DirectConnection ) transactions.elementAt( c );
      if ( !tempDirectConnection.isStopFlag() && !tempDirectConnection.isErrorFlag() && !tempDirectConnection.isCompleteFlag() ) {
        actTrsCnt++;
      }
    }
    return actTrsCnt;
  }

  public boolean removeTransaction( byte[] cookie ) {
    DirectConnection tempDirectConnection;
    for ( int c = 0; c < transactions.size(); c++ ) {
      tempDirectConnection = ( DirectConnection ) transactions.elementAt( c );
      if ( ArrayUtil.equals( tempDirectConnection.getSessCookie(), cookie ) ) {
        // Transaction exist
        transactions.removeElementAt( c );
        return true;
      }
    }
    return false;
  }

  public void removeCompleted() {
    DirectConnection tempDirectConnection;
    for ( int c = transactions.size() - 1; c >= 0; c-- ) {
      tempDirectConnection = ( DirectConnection ) transactions.elementAt( c );
      if ( tempDirectConnection.isCompleteFlag() || tempDirectConnection.isErrorFlag() || tempDirectConnection.isStopFlag() ) {
        transactions.removeElementAt( c );
      }
    }
  }
}
