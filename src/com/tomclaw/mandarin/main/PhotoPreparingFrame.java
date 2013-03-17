package com.tomclaw.mandarin.main;

import com.tomclaw.mandarin.dc.DirectConnection;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;
import java.io.IOException;
import javax.microedition.amms.control.camera.CameraControl;
import javax.microedition.amms.control.camera.FlashControl;
import javax.microedition.amms.control.camera.FocusControl;
import javax.microedition.amms.control.camera.SnapshotControl;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.*;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VideoControl;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class PhotoPreparingFrame extends Window {

  public RadioGroup resolutionsGroup;
  public RadioGroup exposureModesGroup;
  public RadioGroup flashModesGroup;
  public Check isAutoFocus;
  public Check isMacroFocus;
  public Player mPlayer;
  public AccountRoot accountRoot;
  public String buddyId;
  private CameraControl camera;
  private FlashControl flash;
  private FocusControl focus;
  private SnapshotControl snapshot;

  public PhotoPreparingFrame( final AccountRoot accountRoot, final String buddyId ) {
    super( MidletMain.screen );

    this.accountRoot = accountRoot;
    this.buddyId = buddyId;

    header = new Header( Localization.getMessage( "PHOTO_PREPARING" ) );
    soft = new Soft( MidletMain.screen );

    soft.leftSoft = new PopupItem( Localization.getMessage( "BACK" ) ) {
      public void actionPerformed() {
        MidletMain.screen.setActiveWindow( s_prevWindow );
      }
    };

    soft.rightSoft = new PopupItem( Localization.getMessage( "SNAPSHOT" ) ) {
      public void actionPerformed() {
        MidletMain.isPhotoActive = true;
        try {
          VideoControl mVideoControl = ( VideoControl ) mPlayer.getControl( "VideoControl" );
          //Display.getDisplay(this).setCurrent(new CameraCanvas(mPlayer, mVideoControl, "png"));
          final Canvas canvas = new Canvas() {
            protected void paint( Graphics g ) {
              g.setColor( 0xFFFFFF );
              g.fillRect( 0, 0, getWidth(), getHeight() );
            }
          };

          mVideoControl.initDisplayMode(
                  VideoControl.USE_DIRECT_VIDEO, canvas );
          try {
            mVideoControl.setDisplayLocation( 2, 2 );
            mVideoControl.setDisplaySize( MidletMain.screen.getWidth() - 4, MidletMain.screen.getHeight() - 4 );
          } catch ( MediaException me ) {
            try {
              mVideoControl.setDisplayFullScreen( true );
            } catch ( MediaException me2 ) {
            }
          }
          mVideoControl.setVisible( true );

          final Command okCommand = new Command( Localization.getMessage( "SNAP" ), Command.OK, 1 );
          final Command cancelCommand = new Command( Localization.getMessage( "CANCEL" ), Command.CANCEL, 1 );
          final Command sendCommand = new Command( Localization.getMessage( "SEND" ), Command.ITEM, 1 );
          final Command retryCommand = new Command( Localization.getMessage( "RETRY" ), Command.STOP, 1 );

          canvas.addCommand( okCommand );
          canvas.addCommand( cancelCommand );

          canvas.setCommandListener( new CommandListener() {
            public void commandAction( Command c, Displayable d ) {
              if ( c.getCommandType() == Command.OK ) {
                /** Snapping **/
                if ( exposureModesGroup != null ) {
                  camera.setExposureMode( camera.getSupportedExposureModes()[exposureModesGroup.getCombed()] );
                }
                camera.setStillResolution( resolutionsGroup.getCombed() * 2 );
                if ( flashModesGroup != null ) {
                  flash.setMode( flash.getSupportedModes()[flashModesGroup.getCombed()] );
                }
                if ( isAutoFocus != null && isAutoFocus.getState() ) {
                  try {
                    focus.setFocus( FocusControl.AUTO );
                  } catch ( MediaException ex ) {
                  }
                }
                if ( isMacroFocus != null ) {
                  try {
                    focus.setMacro( isMacroFocus.getState() );
                  } catch ( MediaException ex ) {
                  }
                }

                /** Snapshot **/
                snapshot = ( SnapshotControl ) mPlayer.getControl( "javax.microedition.amms.control.camera.SnapshotControl" );
                try {
                  snapshot.setDirectory( MidletMain.incomingFilesFolder );
                  snapshot.setFilePrefix( "MND".concat( String.valueOf( System.currentTimeMillis() ) ) );
                  snapshot.setFileSuffix( ".jpg" );

                  // Take one picture and allow the user to keep or discard it
                  snapshot.start( SnapshotControl.FREEZE_AND_CONFIRM );
                  // snapshot.stop();
                  // ...
                  // PlayerListener got a WAITING_UNFREEZE event and the user chose to discard the picture
                } catch ( Throwable ex1 ) {
                  Display.getDisplay( MidletMain.midletMain ).setCurrent( MidletMain.screen );
                  closePlayer();
                  ActionExec.showError( Localization.getMessage( "LOCAL_FILE_IO_EXCEPTION" ) );
                }

              } else if ( c.getCommandType() == Command.CANCEL ) {
                Display.getDisplay( MidletMain.midletMain ).setCurrent( MidletMain.screen );
                closePlayer();
                MidletMain.screen.setActiveWindow( PhotoPreparingFrame.this.s_prevWindow );
              } else if ( c.getCommandType() == Command.ITEM ) {
                snapshot.unfreeze( true );
              } else if ( c.getCommandType() == Command.STOP ) {
                snapshot.unfreeze( false );
                canvas.removeCommand( sendCommand );
                canvas.removeCommand( retryCommand );
                canvas.addCommand( okCommand );
                canvas.addCommand( cancelCommand );
              }
              System.gc();
            }
          } );

          mPlayer.start();

          mPlayer.addPlayerListener( new PlayerListener() {
            public void playerUpdate( Player player, String event, Object eventData ) {
              LogUtil.outMessage( "event = " + event );
              if ( event.equals( SnapshotControl.SHOOTING_STOPPED ) ) {
                try {
                  if ( eventData == null ) {
                    Display.getDisplay( MidletMain.midletMain ).setCurrent( MidletMain.screen );
                    closePlayer();
                    MidletMain.screen.setActiveWindow( PhotoPreparingFrame.this.s_prevWindow );
                    ActionExec.showError( Localization.getMessage( "PHONE_SUPPORT_EXCEPTION" ) );
                    return;
                  }
                  String fileName = ( String ) eventData;
                  if ( fileName.indexOf( "/" ) != -1 ) {
                    fileName = fileName.substring( fileName.lastIndexOf( '/' ) + 1 );
                  }


                  FileConnection fileConnection;

                  LogUtil.outMessage( "Connecting file: " + MidletMain.incomingFilesFolder.concat( fileName ) );
                  fileConnection = ( FileConnection ) Connector.open( "file://".concat( MidletMain.incomingFilesFolder.concat( fileName ) ), Connector.READ );
                  LogUtil.outMessage( "Connected: " + fileConnection.getPath() );

                  String fileLocation = ( fileConnection.getPath() );
                  long fileSize = fileConnection.fileSize();

                  if ( fileName != null && accountRoot.getStatusIndex() != 0 ) {
                    final DirectConnection directConnection = accountRoot.getDirectConnectionInstance();
                    directConnection.setIsReceivingFile( false );
                    directConnection.setTransactionInfo( StringUtil.stringToByteArray( fileName, true ), fileLocation, fileSize, PhotoPreparingFrame.this.buddyId );
                    directConnection.generateCookie();
                    accountRoot.getTransactionManager().addTransaction( directConnection );

                    new Thread() {
                      public void run() {
                        try {
                          directConnection.sendFile();
                        } catch ( IOException ex ) {
                          // ex.printStackTrace();
                          LogUtil.outMessage( "IOException: " + ex.getMessage(), true );
                        } catch ( InterruptedException ex ) {
                          LogUtil.outMessage( "InterruptedException: " + ex.getMessage(), true );
                        }
                      }
                    }.start();
                    accountRoot.getTransactionsFrame().transactionItemFrame = new TransactionItemFrame( directConnection );
                    accountRoot.getTransactionsFrame().transactionItemFrame.s_prevWindow = accountRoot.getTransactionsFrame();
                    MidletMain.screen.setActiveWindow( accountRoot.getTransactionsFrame().transactionItemFrame );
                  }
                  Display.getDisplay( MidletMain.midletMain ).setCurrent( MidletMain.screen );
                  closePlayer();
                } catch ( Throwable ex ) {
                  Display.getDisplay( MidletMain.midletMain ).setCurrent( MidletMain.screen );
                  closePlayer();
                  MidletMain.screen.setActiveWindow( PhotoPreparingFrame.this.s_prevWindow );
                  ActionExec.showError( Localization.getMessage( "LOCAL_FILE_IO_EXCEPTION" ).concat( " " ).concat( ex.getMessage() ) );
                }
              } else if ( event.equals( SnapshotControl.STORAGE_ERROR ) ) {
                Display.getDisplay( MidletMain.midletMain ).setCurrent( MidletMain.screen );
                closePlayer();
                MidletMain.screen.setActiveWindow( PhotoPreparingFrame.this.s_prevWindow );
                ActionExec.showError( Localization.getMessage( "LOCAL_FILE_IO_EXCEPTION" ).concat( " STORAGE_EXCEPTION" ) );
              } else if ( event.equals( SnapshotControl.WAITING_UNFREEZE ) ) {
                canvas.removeCommand( okCommand );
                canvas.removeCommand( cancelCommand );
                canvas.addCommand( sendCommand );
                canvas.addCommand( retryCommand );
              }
            }
          } );
          Display.getDisplay( MidletMain.midletMain ).setCurrent( canvas );
        } catch ( MediaException ex ) {
        }
      }
    };

    Pane pane = new Pane( null, false );

    Label label1 = new Label( Localization.getMessage( "SELECT_RESOLUTION" ) );
    label1.setFocusable( false );
    label1.setTitle( true );
    pane.addItem( label1 );

    resolutionsGroup = new RadioGroup();

    createPlayer();
    camera = ( CameraControl ) mPlayer.getControl( "javax.microedition.amms.control.camera.CameraControl" );

    int[] resolutions = camera.getSupportedStillResolutions();
    for ( int c = 0; c < resolutions.length; c += 2 ) {
      Radio radio = new Radio( resolutions[c] + " x " + resolutions[c + 1], false );
      radio.setFocusable( true );
      if ( c == 0 ) {
        radio.setFocused( true );
      }
      resolutionsGroup.addRadio( radio );
      pane.addItem( radio );
    }
    resolutionsGroup.setCombed( 0 );


    String[] exposures = camera.getSupportedExposureModes();
    if ( exposures != null && exposures.length > 0 ) {
      exposureModesGroup = new RadioGroup();
      Label label2 = new Label( Localization.getMessage( "SELECT_EXPOSURE" ) );
      label2.setFocusable( false );
      label2.setTitle( true );
      pane.addItem( label2 );

      for ( int c = 0; c < exposures.length; c++ ) {
        String title = Localization.getMessage( exposures[c].toUpperCase() );
        if ( title.equals( "???" ) ) {
          title = exposures[c];
        }
        Radio radio = new Radio( title, false );
        radio.setFocusable( true );
        exposureModesGroup.addRadio( radio );
        pane.addItem( radio );
      }
      exposureModesGroup.setCombed( 0 );
    }

    flash = ( FlashControl ) mPlayer.getControl( "javax.microedition.amms.control.camera.FlashControl" );
    if ( flash != null ) {
      int[] modes = flash.getSupportedModes();//Returns a list of flash modes 
      //supported by the camera device.
      flashModesGroup = new RadioGroup();
      Label label2 = new Label( Localization.getMessage( "SELECT_FLASH_MODE" ) );
      label2.setFocusable( false );
      label2.setTitle( true );
      pane.addItem( label2 );
      for ( int c = 0; c < modes.length; c++ ) {
        String title = "Mode-" + modes[c];
        if ( modes[c] == FlashControl.AUTO ) {
          title = Localization.getMessage( "AUTO" );
        } else if ( modes[c] == FlashControl.AUTO_WITH_REDEYEREDUCE ) {
          title = Localization.getMessage( "AUTO_WITH_REDEYEREDUCE" );
        } else if ( modes[c] == FlashControl.FILLIN ) {
          title = Localization.getMessage( "FILLIN" );
        } else if ( modes[c] == FlashControl.FORCE ) {
          title = Localization.getMessage( "FORCE" );
        } else if ( modes[c] == FlashControl.FORCE_WITH_REDEYEREDUCE ) {
          title = Localization.getMessage( "FORCE_WITH_REDEYEREDUCE" );
        } else if ( modes[c] == FlashControl.OFF ) {
          title = Localization.getMessage( "OFF" );
        }
        Radio radio = new Radio( title, false );
        radio.setFocusable( true );
        flashModesGroup.addRadio( radio );
        pane.addItem( radio );
      }
      flashModesGroup.setCombed( 0 );
    }

    /** Focus **/
    focus = ( FocusControl ) mPlayer.getControl( "javax.microedition.amms.control.camera.FocusControl" );
    if ( focus != null ) {
      Label label2 = new Label( Localization.getMessage( "SELECT_FOCUS_MODE" ) );
      label2.setFocusable( false );
      label2.setTitle( true );
      pane.addItem( label2 );
      if ( focus.isAutoFocusSupported() ) {
        isAutoFocus = new Check( Localization.getMessage( "AUTO_FOCUS" ), true );
        isAutoFocus.setFocusable( true );
        pane.addItem( isAutoFocus );
      }
      if ( focus.isMacroSupported() ) {
        isMacroFocus = new Check( Localization.getMessage( "MACRO_FOCUS" ), false );
        isMacroFocus.setFocusable( true );
        pane.addItem( isMacroFocus );
      }
    }

    setGObject( pane );
  }

  public final void createPlayer() {
    if ( mPlayer != null ) {
      closePlayer();
    }
    try {
      mPlayer = Manager.createPlayer( "capture://video" );
      mPlayer.realize();
    } catch ( Throwable ex ) {
    }
  }

  public void closePlayer() {
    try {
      try {
        mPlayer.stop();
      } catch ( MediaException ex ) {
      }
      mPlayer.deallocate();
      mPlayer.close();
    } catch ( Throwable ex ) {
    }
    System.gc();
    MidletMain.isPhotoActive = false;
  }
}
