package com.tomclaw.mandarin.main;

import com.tomclaw.mandarin.dc.DirectConnection;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class TransactionsFrame extends Window {

  private List list;
  private AccountRoot accountRoot;
  public TransactionItemFrame transactionItemFrame;

  public TransactionsFrame( final AccountRoot accountRoot ) {
    super( MidletMain.screen );

    header = new Header( Localization.getMessage( "TRANSACTIONS_FRAME" ) );

    this.accountRoot = accountRoot;

    soft = new Soft( MidletMain.screen );

    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };

    soft.rightSoft = new PopupItem( Localization.getMessage( "MENU" ) );

    soft.rightSoft.addSubItem( new PopupItem( Localization.getMessage( "SELECT" ) ) {
      public void actionPerformed() {
        if ( list.selectedIndex >= 0 && list.selectedIndex < list.items.size() ) {
          ( ( ListItem ) list.items.elementAt( list.selectedIndex ) ).actionPerformed();
        }
      }
    } );

    soft.rightSoft.addSubItem( new PopupItem( Localization.getMessage( "REMOVE_COMPLETED" ) ) {
      public void actionPerformed() {
        accountRoot.getTransactionManager().removeCompleted();
      }
    } );

    list = new List();

    updateTransactions();

    setGObject( list );
  }

  public final void updateTransactions() {
    list.items.removeAllElements();
    if ( accountRoot.getTransactionManager().transactions.size() > 0 ) {
      for ( int c = 0; c < accountRoot.getTransactionManager().transactions.size(); c++ ) {
        final DirectConnection directConnection = ( DirectConnection ) accountRoot.getTransactionManager().transactions.elementAt( c );
        ListItem item = new ListItem( directConnection.getBuddyId() + " - " + StringUtil.byteArrayToString( directConnection.getFileName(), true ) ) {
          public void actionPerformed() {
            LogUtil.outMessage( "Transaction showed" );
            transactionItemFrame = new TransactionItemFrame( directConnection );
            transactionItemFrame.s_prevWindow = TransactionsFrame.this;
            MidletMain.screen.setActiveWindow( transactionItemFrame );
          }
        };
        item.imageFileHash = IconsType.HASH_FILES;
        item.imageIndex = ( directConnection.isErrorFlag() ? 4 : ( ( directConnection.isReceivingFileFlag() ? 0 : 2 ) + ( directConnection.isCompleteFlag() ? 1 : 0 ) ) );

        list.addItem( item );
      }
    }
    MidletMain.screen.repaint();
  }
}
