package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.ChatItem;
import com.tomclaw.tcuilite.ListItem;
import com.tomclaw.tcuilite.ListRmsRenderer;
import com.tomclaw.tcuilite.Pane;
import com.tomclaw.utils.DataUtil;
import com.tomclaw.utils.StringUtil;
import com.tomclaw.utils.bb.BBResult;
import com.tomclaw.utils.bb.BBUtil;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class HistoryRmsRenderer extends ListRmsRenderer {

  public ListItem getRmsItem( byte[] data ) {
    int offset = 0;
    int length = DataUtil.get16( data, offset );
    offset += 2;
    String text = StringUtil.byteArrayToString( data, offset, length, true );
    offset += length;
    length = DataUtil.get16( data, offset );
    offset += 2;
    // String buddyId
    StringUtil.byteArrayToString( data, offset, length, true );
    offset += length;
    length = DataUtil.get16( data, offset );
    offset += 2;
    // String buddyNick
    StringUtil.byteArrayToString( data, offset, length, true );
    offset += length;
    length = DataUtil.get16( data, offset );
    offset += 2;
    // String itemDateTime
    StringUtil.byteArrayToString( data, offset, length, true );
    offset += length;
    int itemType = DataUtil.get16( data, offset );
    offset += 2;
    int dlvStatus = DataUtil.get8int( data, offset );
    offset++;
    try {
      BBResult bbResult = BBUtil.processText( text, 0, 0, 2048 );
      text = bbResult.originalString;
    } catch ( Throwable ex ) {
    }
    ListItem listItem = new ListItem( text,
            IconsType.HASH_CHAT, ( ( ( itemType == ChatItem.TYPE_PLAIN_MSG
            ? ( itemType + dlvStatus ) : itemType ) ) ) );

    return listItem;
  }

  public ChatItem getRmsItem( byte[] data, Pane pane ) {
    int offset = 0;
    int length = DataUtil.get16( data, offset );
    offset += 2;
    String text = StringUtil.byteArrayToString( data, offset, length, true );
    offset += length;
    length = DataUtil.get16( data, offset );
    offset += 2;
    String buddyId = StringUtil.byteArrayToString( data, offset, length, true );
    offset += length;
    length = DataUtil.get16( data, offset );
    offset += 2;
    String buddyNick = StringUtil.byteArrayToString( data, offset, length, true );
    offset += length;
    length = DataUtil.get16( data, offset );
    offset += 2;
    String itemDateTime = StringUtil.byteArrayToString( data, offset, length, true );
    offset += length;
    int itemType = DataUtil.get16( data, offset );
    offset += 2;
    int dlvStatus = DataUtil.get8int( data, offset );
    offset++;
    ChatItem chatItem = new ChatItem( pane, buddyId, buddyNick, itemDateTime, itemType, text );
    chatItem.dlvStatus = ( byte ) dlvStatus;
    return chatItem;
  }

  public byte[] getRmsData( ChatItem chatItem ) {
    byte[] text = StringUtil.stringToByteArray( chatItem.text, true );
    byte[] buddyId = StringUtil.stringToByteArray( chatItem.buddyId, true );
    byte[] buddyNick = StringUtil.stringToByteArray( chatItem.buddyNick, true );
    byte[] itemDateTime = StringUtil.stringToByteArray( chatItem.itemDateTime, true );
    byte[] data = new byte[ 2 + text.length
            + 2 + buddyId.length
            + 2 + buddyNick.length
            + 2 + itemDateTime.length
            + 2
            + 1 ];
    int offset = 0;
    DataUtil.put16( data, offset, text.length );
    offset += 2;
    DataUtil.putArray( data, offset, text );
    offset += text.length;

    DataUtil.put16( data, offset, buddyId.length );
    offset += 2;
    DataUtil.putArray( data, offset, buddyId );
    offset += buddyId.length;

    DataUtil.put16( data, offset, buddyNick.length );
    offset += 2;
    DataUtil.putArray( data, offset, buddyNick );
    offset += buddyNick.length;

    DataUtil.put16( data, offset, itemDateTime.length );
    offset += 2;
    DataUtil.putArray( data, offset, itemDateTime );
    offset += itemDateTime.length;

    DataUtil.put16( data, offset, chatItem.itemType );
    offset += 2;

    DataUtil.put8( data, offset, chatItem.dlvStatus );
    offset++;
    return data;
  }
}
