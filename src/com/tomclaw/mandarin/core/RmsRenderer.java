package com.tomclaw.mandarin.core;

import com.tomclaw.tcuilite.GroupChild;
import com.tomclaw.tcuilite.GroupHeader;
import com.tomclaw.utils.DataUtil;
import com.tomclaw.utils.LogUtil;
import com.tomclaw.utils.StringUtil;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class RmsRenderer {

  public static GroupHeader getRmsGroupHeader(
          byte[] data, AccountRoot accoutRoot ) {
    int offset;
    GroupHeader groupHeader = new GroupHeader( StringUtil.byteArrayToString(
            data, 2, offset = DataUtil.get16( data, 0 ), true ) );
    offset += 2;
    int childsCount = DataUtil.get16( data, offset );
    offset += 2;
    BuddyItem groupChild;
    int t_Int;
    try {
      for ( int c = 0; c < childsCount; c++ ) {
        t_Int = DataUtil.get16( data, offset );
        offset += 2;
        groupChild = accoutRoot.getBuddyInstance();
        groupChild.setUserId( StringUtil.byteArrayToString( data, offset, t_Int, true ) );
        offset += t_Int;
        t_Int = DataUtil.get16( data, offset );
        offset += 2;
        groupChild.setUserNick( StringUtil.byteArrayToString( data, offset, t_Int, true ) );
        offset += t_Int;
        t_Int = DataUtil.get16( data, offset );
        offset += 2;
        groupChild.setUserPhone( StringUtil.byteArrayToString( data, offset, t_Int, true ) );
        offset += t_Int;
        groupChild.setBuddyType( DataUtil.get16( data, offset ) );
        offset += 2;
        groupChild.setIsPhone( DataUtil.get8int( data, offset ) == 1 );
        offset++;
        groupChild.updateUiData();
        groupHeader.addChild( ( GroupChild ) groupChild );
      }
    } catch ( Throwable ex ) {
      LogUtil.outMessage( "Error while loading RMS list: " + ex.getMessage() );
    }
    return groupHeader;
  }

  public static byte[] getRmsData( GroupHeader groupHeader ) {
    byte[] data;
    byte[] titleData = StringUtil.stringToByteArray( groupHeader.title, true );
    data = new byte[ 4 + titleData.length ];
    DataUtil.put16( data, 0, titleData.length );
    DataUtil.putArray( data, 2, titleData );
    DataUtil.put16( data, 2 + titleData.length, groupHeader.getChildsCount() );
    byte[] itemData;
    byte[] t_Byte;
    int offset;
    BuddyItem groupChild;
    try {
      for ( int c = 0; c < groupHeader.getChildsCount(); c++ ) {
        // offset = 0;
        groupChild = ( BuddyItem ) groupHeader.getChilds().elementAt( c );
        titleData = StringUtil.stringToByteArray( groupChild.getUserId(), true );
        itemData = new byte[ 9 + titleData.length
                + StringUtil.stringToByteArray(
                groupChild.getUserNick(), true ).length
                + StringUtil.stringToByteArray( groupChild.getUserPhone(),
                true ).length ];
        /** Title **/
        DataUtil.put16( itemData, 0, titleData.length );
        DataUtil.putArray( itemData, 2, titleData );
        offset = 2 + titleData.length;
        /**
         * Special
         *
         * Id, nick, phone, type, isPhone
         **/
        /** userNick **/
        titleData = StringUtil.stringToByteArray( groupChild.getUserNick(), true );
        DataUtil.put16( itemData, offset, titleData.length );
        offset += 2;
        DataUtil.putArray( itemData, offset, titleData );
        offset += titleData.length;
        /** userPhone **/
        titleData = StringUtil.stringToByteArray( groupChild.getUserPhone(), true );
        DataUtil.put16( itemData, offset, titleData.length );
        offset += 2;
        DataUtil.putArray( itemData, offset, titleData );
        offset += titleData.length;
        /** itemType **/
        DataUtil.put16( itemData, offset, groupChild.getBuddyType() );
        offset += 2;
        /** isPhone **/
        DataUtil.put8( itemData, offset, groupChild.isPhone() ? 1 : 0 );
        offset++;
        /** Glueing array **/
        t_Byte = new byte[ data.length + itemData.length ];
        System.arraycopy( data, 0, t_Byte, 0, data.length );
        System.arraycopy( itemData, 0, t_Byte, data.length, itemData.length );
        data = t_Byte;
      }
    } catch ( Throwable ex1 ) {
    }
    return data;
  }
}
