package com.tomclaw.mandarin.main;

import com.tomclaw.mandarin.dc.DirectConnection;
import com.tomclaw.tcuilite.*;
import com.tomclaw.tcuilite.localization.Localization;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;
import com.tomclaw.utils.TimeUtil;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class FileBrowserFrame extends Window {

  /** Local variables **/
  public List filesList = null;
  FileConnection fileConnection;
  String systemPath = "";
  public final int manType;

  public FileBrowserFrame( final int manType, final AccountRoot accountRoot, final String buddyId ) {
    super( MidletMain.screen );
    this.manType = manType;
    header = new Header( Localization.getMessage( "FILE_BROWSER_FRAME" ) );
    soft = new Soft( MidletMain.screen );
    PopupItem openItem = new PopupItem( Localization.getMessage( "OPEN_ITEM" ) ) {
      public void actionPerformed() {
        filesList.listEvent.actionPerformed( null );
      }
    };
    soft.leftSoft = openItem;
    PopupItem menuItem = new PopupItem( Localization.getMessage( "MENU_ITEM" ) );
    soft.rightSoft = menuItem;
    PopupItem backItem = new PopupItem( Localization.getMessage( "CANCEL_ITEM" ) ) {
      public void actionPerformed() {
        if ( FileBrowserFrame.this.s_prevWindow != null ) {
          MidletMain.screen.setActiveWindow( FileBrowserFrame.this.s_prevWindow );
        }
      }
    };
    soft.rightSoft.addSubItem( backItem );
    if ( manType == 0x00 ) {
      PopupItem infoPopupItem = new PopupItem( Localization.getMessage( "INFO_ITEM" ) ) {
        public void actionPerformed() {
          String __selectedString;
          __selectedString = ( ( ListItem ) filesList.items.elementAt( filesList.selectedIndex ) ).title;
          if ( __selectedString.hashCode() != "...".hashCode() ) {
            boolean isFolder = __selectedString.endsWith( "/" );
            if ( !isFolder ) {
              String filePath = ( "/".concat( systemPath ).concat( __selectedString ) );
              FileConnection fileConnection;
              try {
                fileConnection = ( FileConnection ) Connector.open( "file://".concat( filePath ), Connector.READ );
                if ( !fileConnection.exists() ) {
                  return;
                }
                final String fileInfoString = Localization.getMessage( "FILE_NAME" ).concat( ":" ).concat( __selectedString ).concat( "\n " ).concat( Localization.getMessage( "FILE_SIZE" ) ).concat( ":" ).concat( fileConnection.fileSize() + " KiB" ).concat( "\n" ).concat( Localization.getMessage( "MOD_DATETIME" ) ).concat( ":" ).concat( TimeUtil.getDateString( fileConnection.lastModified(), true ) ).concat( "\n " ).concat( Localization.getMessage( "FILE_FOLDER" ) ).concat( ":" ).concat( filePath ).concat( "\n" );
                ActionExec.showInfo( fileInfoString );
              } catch ( IOException ex ) {
                LogUtil.outMessage( "Local file error: " + ex.getMessage(), true );
              }

            }
          }
        }
      };
      soft.rightSoft.addSubItem( infoPopupItem );
    }
    if ( manType == 0x01 ) {
      PopupItem selectItem = new PopupItem( Localization.getMessage( "SELECT_ITEM" ) ) {
        public void actionPerformed() {
          String __selectedString = ( ( ListItem ) filesList.items.elementAt( filesList.selectedIndex ) ).title;
          if ( __selectedString.hashCode() != "...".hashCode() ) {
            boolean isFolder = __selectedString.endsWith( "/" );

            if ( !isFolder ) {
            } else {
              if ( manType == 0x01 ) {
                MidletMain.settingsFrame.acceptFilesFolder.setText( "/".concat( systemPath ).concat( __selectedString ) );
                MidletMain.settingsFrame.acceptFilesFolder.updateCaption();
                MidletMain.settingsFrame.prepareGraphics();
                MidletMain.screen.setActiveWindow( MidletMain.settingsFrame );
              }
            }
          } else {
            if ( manType == 0x01 ) {
              MidletMain.settingsFrame.acceptFilesFolder.setText( "/".concat( systemPath ) );
              MidletMain.settingsFrame.acceptFilesFolder.updateCaption();
              MidletMain.settingsFrame.prepareGraphics();
              MidletMain.screen.setActiveWindow( MidletMain.settingsFrame );
            }
          }
        }
      };
      soft.rightSoft.addSubItem( selectItem );
    }
    filesList = new List();
    filesList.listEvent = new ListEvent() {
      public void actionPerformed( ListItem li ) {
        /** Action thread was been **/
        String __selectedString;
        __selectedString = ( ( ListItem ) filesList.items.elementAt( filesList.selectedIndex ) ).title;
        if ( __selectedString.hashCode() != "...".hashCode() ) {
          boolean isFolder = __selectedString.endsWith( "/" );
          if ( !isFolder ) {
            if ( manType == 0x00 ) {
              String filePath = ( "/".concat( systemPath ).concat( __selectedString ) );
              FileConnection fileConnection;
              try {
                fileConnection = ( FileConnection ) Connector.open( "file://".concat( filePath ), Connector.READ );
                if ( !fileConnection.exists() ) {
                  return;
                }
                String fileName = __selectedString;
                String fileLocation = "/".concat( systemPath );
                long fileSize = fileConnection.fileSize();
                if ( fileName != null && accountRoot.getStatusIndex() != 0 ) {
                  final DirectConnection directConnection = accountRoot.getDirectConnectionInstance();
                  directConnection.setIsReceivingFile( false );
                  directConnection.setTransactionInfo( StringUtil.stringToByteArray( fileName, true ), fileLocation, fileSize, buddyId );
                  directConnection.generateCookie();
                  accountRoot.getTransactionManager().addTransaction( directConnection );
                  new Thread() {
                    public void run() {
                      try {
                        directConnection.sendFile();
                      } catch ( IOException ex ) {
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
                MidletMain.screen.setActiveWindow( FileBrowserFrame.this.s_prevWindow );
                return;
              } catch ( IOException ex ) {
                LogUtil.outMessage( "Local file error: " + ex.getMessage(), true );
              }
            }
          } else {
            systemPath += __selectedString;
            readLevel( systemPath );
          }
        } else {
          getLowerLevel();
          readLevel( systemPath );
        }
        /** Repainting **/
        MidletMain.screen.repaint( Screen.REPAINT_STATE_PLAIN );
      }
    };
    setGObject( filesList );
    readLevel( systemPath );
  }

  /**
   * FS working methods
   */
  public void getLowerLevel() {
    String lastPath = new String();
    String newPath = "";
    for ( int c = 0; c < systemPath.length(); c++ ) {
      if ( systemPath.charAt( c ) == '/' ) {
        lastPath = newPath;
        char pathGet[] = new char[ c ];
        systemPath.getChars( 0, c, pathGet, 0 );
        newPath = String.valueOf( pathGet );
      }
    }
    systemPath = lastPath + "/";
  }

  public final boolean readLevel( String levelPath ) {
    if ( levelPath.length() > 1 ) {
      return readFiles( levelPath );
    } else {
      readRoots();
      return true;
    }
  }

  public boolean readFiles( String filePath ) {
    try {
      fileConnection = ( FileConnection ) Connector.open( "file:///" + filePath, 1 );
      LogUtil.outMessage( "Establising: " + "file:///" + filePath + ". Is direcory: " + fileConnection.isDirectory() );
      if ( fileConnection.isDirectory() ) {
        outEnumeration( fileConnection.list() );
        return true;
      } else {
        return false;
      }
    } catch ( IOException ex ) {
      return false;
    }
  }

  public void readRoots() {
    outEnumeration( javax.microedition.io.file.FileSystemRegistry.listRoots() );
    systemPath = "";
  }

  public void outEnumeration( Enumeration enumeration ) {
    try {
      Vector files = new Vector();
      filesList.items.removeAllElements();
      ListItem upItem = new ListItem( "..." );
      upItem.imageFileHash = IconsType.HASH_FILES;
      upItem.imageIndex = IconsType.FILES_UP;
      filesList.addItem( upItem );
      int image;
      while ( enumeration.hasMoreElements() ) {
        String nextelement = ( String ) enumeration.nextElement();
        boolean isDirectory;
        if ( nextelement.charAt( nextelement.length() - 1 ) == '/' ) {
          isDirectory = true;
        } else {
          isDirectory = false;
        }
        if ( isDirectory && systemPath.length() > 1 ) {
          image = IconsType.FILES_FOLDER;
        } else if ( !isDirectory ) {
          image = IconsType.FILES_FILE;
        } else {
          image = IconsType.FILES_DISK;
        }
        ListItem menuItem =
                new ListItem( nextelement );
        menuItem.imageFileHash = IconsType.HASH_FILES;
        menuItem.imageIndex = image;
        if ( image != IconsType.FILES_FILE ) {
          filesList.addItem( menuItem );
        } else {
          files.addElement( menuItem );
        }
      }
      switch ( manType ) {
        case 0x00: {
          break;
        }
        case 0x01: {
          files = new Vector();
          break;
        }
        case 0x02: {
          files = new Vector();
          break;
        }
        default: {
          break;
        }
      }
      for ( int c = 0; c < files.size(); c++ ) {
        filesList.addItem( ( ListItem ) files.elementAt( c ) );
      }
      filesList.selectedIndex = 0;
      if ( filesList.items.size() >= 0 ) {
        filesList.selectedIndex = 0;
      }
    } catch ( Throwable ex1 ) {
    }
  }
}
