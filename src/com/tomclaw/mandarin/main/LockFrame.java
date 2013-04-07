package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import java.io.IOException;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class LockFrame extends Window {

  private Pane pane;
  private Image lock;
  private Label label;
  private Label unread;
  private Soft m_soft;

  public LockFrame() {
    super( MidletMain.screen );

    header = new Header( Localization.getMessage( "LOCKED_SCREEN" ) );
    m_soft = new Soft( screen );
    soft = m_soft;

    soft.leftSoft = new PopupItem( Localization.getMessage( "UNLOCK" ) ) {

      public void actionPerformed() {
        showConfirm();
      }
    };
    /** Pane **/
    pane = new Pane( null, false );

    label = new Label( Localization.getMessage( "SCREEN_IS_LOCKED" ) );
    label.setHeader( true );
    pane.addItem( label );
    unread = new Label( getUnreadLabelText() );
    pane.addItem( unread );

    try {
      lock = Image.createImage( "/res/huge/lock.png" );

      directDraw_beforePopup = new DirectDraw() {

        int width, height;

        public void paint( Graphics grphcs ) {
          paint( grphcs, 0, 0 );
        }

        public void paint( Graphics grphcs, int i, int i1 ) {
          width = MidletMain.screen.getWidth();
          height = MidletMain.screen.getHeight();
          grphcs.drawImage( lock, i + width / 2, i1 + height / 2,
                  Graphics.VCENTER | Graphics.HCENTER );
        }
      };
    } catch ( IOException ex ) {
      lock = null;
    }
    setGObject( pane );
  }

  private void showConfirm() {
    Soft t_soft = new Soft( MidletMain.screen );
    t_soft.leftSoft = new PopupItem( Localization.getMessage( "YES" ) ) {

      public void actionPerformed() {
        soft = m_soft;
        closeDialog();
      }
    };
    t_soft.rightSoft = new PopupItem( Localization.getMessage( "NO" ) ) {

      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( MidletMain.mainFrame );
      }
    };
    soft = t_soft;
    showDialog( new Dialog( MidletMain.screen, soft,
            Localization.getMessage( "CONFIRMATION" ),
            Localization.getMessage( "REALLY_UNLOCK" ) ) );
    MidletMain.screen.repaint();
  }

  public void updateUnreadLabel() {
    ( ( PlainContent ) unread.getContent() ).setText( getUnreadLabelText() );
  }

  private String getUnreadLabelText() {
    int totalUnread = MidletMain.mainFrame.getUnreadCount();
    if ( totalUnread == 0 ) {
      return Localization.getMessage( "NO_UNREAD_MESSAGES" );
    } else {
      return totalUnread + " " + Localization.getMessage( "UNREAD_MESSAGES" );
    }
  }
}
