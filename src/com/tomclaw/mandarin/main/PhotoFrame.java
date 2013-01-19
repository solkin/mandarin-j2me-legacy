package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.Header;
import com.tomclaw.tcuilite.Pane;
import com.tomclaw.tcuilite.PopupItem;
import com.tomclaw.tcuilite.Soft;
import com.tomclaw.tcuilite.Window;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

/**
 *
 * @author solkin
 */
public class PhotoFrame extends Window {

  private Player player;
  private VideoControl videoControl;

  public PhotoFrame( final AccountRoot accountRoot, final String buddyId ) {
    super( MidletMain.screen );
    header = new Header( "Take photo" );
    // cmdCamera = new Command( "Camera", Command.SCREEN, 1 );
    soft = new Soft( MidletMain.screen );
    soft.leftSoft = new PopupItem( "Camera" ) {
      public void actionPerformed() {
        showCamera();
      }
    };
    soft.rightSoft = new PopupItem( "Back" ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };
    try {
      player = Manager.createPlayer( "capture://video" );
      player.realize();
      videoControl = ( VideoControl ) player.getControl( "VideoControl" );
      videoControl.initDisplayMode( VideoControl.USE_DIRECT_VIDEO, MidletMain.screen );
      videoControl.setDisplayLocation( 0, header.getHeight() );
      videoControl.setVisible( true );
      //videoControl.setDisplaySize( width, height - header.getHeight() - soft.getHeight() );
    } catch ( Throwable ex ) {
      ex.printStackTrace();
    }
    Pane pane = new Pane( null, false );
    setGObject( pane );
  }

  public void showCamera() {
    try {
      MidletMain.screen.setFullScreenMode( true );
      MidletMain.screen.repaint();
      soft.leftSoft = new PopupItem( "Capture" ) {
        public void actionPerformed() {
          new Thread() {
            public void run() {
              takePhoto();
            }

            public void takePhoto() {
              try {
                byte[] photo = videoControl.getSnapshot( null );
                saveImage2File( photo );
                Image image = Image.createImage( photo, 0, photo.length );
                //form.append( image );
                //display.setCurrent( form );
                player.close();
              } catch ( MediaException me ) {
              }
            }

            void saveImage2File( byte[] photo ) {
              // Receive a photo as byte array
              // Save Image to file
              FileConnection fileConn;
              DataOutputStream dos;

              try {
                fileConn = ( FileConnection ) Connector.open(
                        "file:///" + MidletMain.incomingFilesFolder + "/MND".concat( String.valueOf( System.currentTimeMillis() ) ) + ".png" );
                if ( !fileConn.exists() ) {
                  fileConn.create();
                }
                dos = new DataOutputStream( fileConn.openOutputStream() );
                dos.write( photo );
                dos.flush();
                dos.close();
                fileConn.close();

              } catch ( IOException ioe ) {
                System.out.println( "Error!" + ioe );
              }
            }
          }.start();
        }
      };
      player.start();
    } catch ( Throwable ex ) {
      ex.printStackTrace();
    }
  }
} // end of VideoCaptureMidlet
