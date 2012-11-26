package com.tomclaw.mandarin.main;

import com.tomclaw.utils.ArrayUtil;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class UpdateChecker {

  public static final String fileName = "Mandarin";
  public static final String fileExt = "jar";
  public static String latestVersion = "";
  public static int downloadCount = 0;
  public static int updateCount = 0;
  public static String versionURL = "";
  public static String changeLog = "";
  public static String updateURL = "http://www.tomclaw.com/dload/update.php?f="
          + fileName + "&e=" + fileExt + "&v=" + MidletMain.version + "&l=" + MidletMain.locale;
  /** Coming version **/
  public static String comingVersion = "";
  public static String comingDate = "";
  public static String comingImageUrl = "";
  public static String comingText = "";

  public static boolean isUpdatePresent() throws Throwable {
    HttpConnection c = null;
    InputStream s = null;
    String updateInfo;
    ArrayUtil data = new ArrayUtil();
    try {
      LogUtil.outMessage( "URL: " + updateURL );
      c = ( HttpConnection ) Connector.open( updateURL );
      s = c.openInputStream();
      byte[] buffer = new byte[128];
      int read;
      while ( ( read = s.read( buffer ) ) != -1 ) {
        data.append( buffer, 0, read );
      }
      updateInfo = StringUtil.byteArrayToString( data.byteString, true );
    } catch ( Throwable ex1 ) {
      throw new Throwable();
    } finally {
      if ( s != null ) {
        s.close();
      }
      if ( c != null ) {
        c.close();
      }
    }
    // updateInfo = "4.0.1;1000;500;http://www.tomclaw.com/dload/dload.php?f=Mandarin&e=jar;Список изменений будет позднее;";
    // LogUtil.outMessage(updateInfo);
    int offs = updateInfo.indexOf( ";" );
    System.out.println( updateInfo );

    latestVersion = ( updateInfo.substring( 0, offs ) );
    if ( latestVersion.equals( MidletMain.version ) ) {
      comingVersion = updateInfo.substring( ++offs,
              ( offs = updateInfo.indexOf( ";", offs ) ) );
      comingDate = updateInfo.substring( ++offs,
              ( offs = updateInfo.indexOf( ";", offs ) ) );
      comingText = updateInfo.substring( ++offs,
              ( offs = updateInfo.indexOf( ";", offs ) ) );
      comingImageUrl = updateInfo.substring( ++offs,
              ( updateInfo.indexOf( ";", offs ) ) );
      LogUtil.outMessage( "comingVersion=" + comingVersion + "\n"
              + "comingDate=" + comingDate + "\n"
              + "comingText=" + comingText + "\n"
              + "comingImageUrl=" + comingImageUrl );
      return false;
    } else {
      downloadCount = Integer.parseInt( updateInfo.substring( ++offs,
              ( offs = updateInfo.indexOf( ";", offs ) ) ) );
      updateCount = Integer.parseInt( updateInfo.substring( ++offs,
              ( offs = updateInfo.indexOf( ";", offs ) ) ) );
      versionURL = ( updateInfo.substring( ++offs,
              ( offs = updateInfo.indexOf( ";", offs ) ) ) );
      changeLog = ( updateInfo.substring( ++offs,
              ( updateInfo.indexOf( ";", offs ) ) ) );

      LogUtil.outMessage( "latestVersion=" + latestVersion + "\n"
              + "downloadCount=" + downloadCount + "\n"
              + "updateCount=" + updateCount + "\n"
              + "versionURL=" + versionURL + "\n"
              + "changeLog=" + changeLog );
      return true;
    }
  }
}
