package com.tomclaw.mandarin.main;

import com.tomclaw.mandarin.icq.IcqAccountRoot;
import com.tomclaw.mandarin.icq.IcqPacketParser;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.HexUtil;
import com.tomclaw.utils.LogUtil;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class BuddyInfoFrame extends Window {

  public Pane pane;
  public int reqSeqNum;
  public BuddyItem buddyItem;
  public AccountRoot accountRoot;
  public String clientBuffer = "";
  private Button updateNickButton;

  public BuddyInfoFrame( final AccountRoot accountRoot,
          final BuddyItem buddyItem ) {
    super( MidletMain.screen );
    this.accountRoot = accountRoot;
    this.buddyItem = buddyItem;
    /** Info request sequence number **/
    reqSeqNum = MidletMain.reqSeqNum++;
    /** Header **/
    header = new Header( Localization.getMessage( "INFO_ABOUT" )
            .concat( " " ).concat( buddyItem.getUserId() ) );
    /** Creating soft **/
    soft = new Soft( MidletMain.screen );
    /** Left soft items **/
    soft.leftSoft = new PopupItem( Localization.getMessage( "CLOSE" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    soft.rightSoft = new PopupItem( "" );
    /** Initializing pane **/
    pane = new Pane( null, false );
    if ( false && (accountRoot.getStatusIndex() == 0 || buddyItem.isPhone()) ) {
      Label idLabel = new Label( Localization.getMessage( "BUDDY_ID_LABEL" ) );
      idLabel.setTitle( true );
      pane.addItem( idLabel );
      Label idLabel_a = new Label( buddyItem.getUserId() );
      pane.addItem( idLabel_a );
      Label nickLabel = new Label(
              Localization.getMessage( "NICK_NAME_LABEL" ) );
      nickLabel.setTitle( true );
      pane.addItem( nickLabel );
      Label nickLabel_a = new Label( buddyItem.getUserNick() );
      pane.addItem( nickLabel_a );
    } else {
      String waitText;
      LogUtil.outMessage( "Dialog with reqSeqNum = " + reqSeqNum );
      waitText = "WAIT_LABEL";
      try {
        accountRoot.requestInfo( buddyItem.getUserId(), reqSeqNum );
      } catch ( IOException ex ) {
        waitText = "IO_EXCEPTION";
      }
    /*byte[] data = HexUtil.stringToBytes( "002500030000656441610000000001fd5b4261646f674d64697250726f66696c65476574436d642869643d3138333733323837322c4e616d653d343735353736363939293a20205b4c6f6f6b75704279456d61696c45762869643d3138333733323837332c656d61696c3d692e736f6c6b696e40636f72702e6d61696c2e72752c41494d3d5945532c4943513d5945532c736563733d302e303034333839293a2053554343455353205b4d6f7266476574446174612869643d3138333733323837342c6562756464793d692e736f6c6b696e40636f72702e6d61696c2e7275293a204d4f5246435f5354415455535f494e56414c49445f4f5045524154494f4e20284641494c454429205d5b53656e64456d61696c546f55494e45762869643d"
            + "3138333733323837352c46616b6555494e3d3636303232352c456d61696c3d692e736f6c6b696e40636f72702e6d61696c2e72752c736563733d302e303034333035293a2020594150505f7374617475733d31302075696e3d36313033333438333120285355434345535329205d5d5b53656e64494d444765744d756c7469496e666f45762869643d3138333733323837382c4e616d653d3437353537363639392c736563733d302e31343935293a20494d442072657475726e20636f64653d31206e4d6174636865733d32206e50616765733d31206e526573756c74733d3220285355434345535329205d5d00000002000000000000000200093631303333343833314e83421700000000002907d0001000000000000000000000000000000000080b0017d098d0b3d0bed18"
            + "0d18c20d0a1d0bed0bbd0bad0b8d0bd07d6000007d700000066000ad098d0b3d0bed180d18c0067000cd0a1d0bed0bbd0bad0b8d0bd006a0017d098d0b3d0bed180d18c20d0a1d0bed0bbd0bad0b8d0bd0068000400000001080c0002000000690016000500640000006e00000078000000820000008c000007d10016000500640000006e00000078000000820000008c0000006d000007d3000007d4000007d900020000006b000007da000400000000006e0046000d00640000006e000000780000007d00000082000400000000008c000400000000009600048000000000a000048000000000aa000000b4000000be000000c8000000d20000006c00040000000007ea00048000000007eb00040000000007ec00040000000007ed00040000000007ee00040000000007ef00"
            + "040000000007f000040000000007f10002007f006f000007f300040000000008090004000000010070000423591be007f800040000000007f9000007fb00040000000007fd00040000000007ff000400000000080200040000000108030004000000000805000400000000080700040000000008080000000000000015692e736f6c6b696e40636f72702e6d61696c2e72758000000000000000002807d0001000000000000000000000000000000000080b0018d098d0b3d0bed180d18c2020d0a1d0bed0bbd0bad0b8d0bd07d6000007d700000066000ad098d0b3d0bed180d18c0067000cd0a1d0bed0bbd0bad0b8d0bd006a0018d098d0b3d0bed180d18c2020d0a1d0bed0bbd0bad0b8d0bd0068000400000001080c0002000007d8002b0003006e0004000000000078000"
            + "40000000100640015692e736f6c6b696e40636f72702e6d61696c2e727500690022000500640000006e000cd09cd0bed181d0bad0b2d0b00078000000820000008c0000006d000007d3000007d4000007d900020000006b000007da000400000000006c00040000000007ea00048000000007eb00040000000007ec00040000000007ed00040000000007ee00040000000007ef00040000000007f000040000000007f10002007f006f000007f30004000000010809000400000001007000042359380007f800040000000007f9000007fb00040000000007fd00040000000007ff00040000000108020004000000000803000400000000080500040000000008070004000000000808000000000000" );
    
      IcqPacketParser.ICQBuddyUserInfo( (IcqAccountRoot)accountRoot, data, 0, 0, null);*/
      Label waitLabel = new Label( Localization.getMessage( waitText ) );
      pane.addItem( waitLabel );
    }
    /** Applying pane **/
    setGObject( pane );
  }

  public void updateNickAction( final BuddyInfo buddyInfo ) {
    try {
      /** This is buddy, not group **/
      Cookie cookie = accountRoot.renameBuddy( buddyInfo.nickName, buddyItem,
              buddyItem.getUserPhone() );
      LogUtil.outMessage( "Request queued, cookie received" );
      QueueAction queueAction = new QueueAction(
              accountRoot, buddyItem, cookie ) {
        public void actionPerformed( Hashtable params ) {
          LogUtil.outMessage( "Action Performed" );
          buddyItem.setUserNick( buddyInfo.nickName );
          buddyItem.updateUiData();
          accountRoot.updateOfflineBuddylist();
        }
      };
      LogUtil.outMessage( "QueueAction created" );
      Queue.pushQueueAction( queueAction );
      LogUtil.outMessage( "queueAction: "
              + queueAction.getCookie().cookieString );

      BuddyInfoFrame.this.pane.items.removeElement( updateNickButton );
      MidletMain.screen.repaint();
    } catch ( IOException ex ) {
    }
  }

  public void placeInfo( final BuddyInfo buddyInfo ) {
    pane.items.removeAllElements();
    if ( buddyInfo.avatar != null ) {
      Label avatarLabel = new Label( buddyInfo.nickName );
      avatarLabel.setBold( true );
      avatarLabel.setHeader( true );
      avatarLabel.image = buddyInfo.avatar;
      pane.addItem( avatarLabel );
    }
    String labelMessage;
    String labelDescription;
    Label descriptionLabel;
    Enumeration keys = buddyInfo.buddyHash.keys();
    for ( int c = 0; c < buddyInfo.buddyHash.size() + 2; c++ ) {
      Label onlineLabel = new Label( "" );
      onlineLabel.setTitle( true );
      descriptionLabel = new Label( "" );
      switch ( c ) {
        case 0x00: {
          labelMessage = "BUDDY_ID_LABEL";
          labelDescription = buddyItem.getUserId();
          break;
        }
        case 0x01: {
          labelMessage = "NICK_NAME_LABEL";
          labelDescription = buddyInfo.nickName;
          if ( !buddyInfo.nickName.equals( buddyItem.getUserNick() ) ) {
            updateNickButton = new Button(
                    Localization.getMessage( "UPDATE_NICKNAME" ) ) {
              public void actionPerformed() {
                updateNickAction( buddyInfo );
              }
            };
            updateNickButton.setFocusable( true );
            updateNickButton.setFocused( true );
            pane.addItem( updateNickButton );
          }
          break;
        }
        default: {
          labelMessage = ( String ) keys.nextElement();
          labelDescription = ( String ) buddyInfo.buddyHash.get( labelMessage );
          if ( labelDescription.equals( "" ) ) {
            continue;
          }
          break;
        }
      }
      onlineLabel.setCaption( Localization.getMessage( labelMessage ) + ": " );
      descriptionLabel.setCaption( labelDescription );

      clientBuffer += onlineLabel.caption + "\n";
      clientBuffer += labelDescription + "\n";

      pane.addItem( onlineLabel );
      pane.addItem( descriptionLabel );
    }
    soft.rightSoft = new PopupItem( Localization.getMessage( "COPY" ) ) {
      public void actionPerformed() {
        MidletMain.buffer = clientBuffer;
      }
    };
    MidletMain.screen.repaint();
    /** Collecting garbage **/
    Runtime.getRuntime().gc();
  }
}
