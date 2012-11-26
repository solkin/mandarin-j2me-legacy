package com.tomclaw.mandarin.main;

import com.tomclaw.tcuilite.ChatItem;
import com.tomclaw.tcuilite.TabItem;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class AccountTab extends TabItem {

  public AccountRoot accountRoot;
  public String accountUserId;
  public int protocolFileHash = 0;

  public AccountTab( String accountUserId, String title, int imageFileHash, int imageFileIndex ) {
    super( title, imageFileHash, imageFileIndex );
    protocolFileHash = imageFileHash;
    this.accountUserId = accountUserId;
  }

  void updateAccountStatus() {
    if ( accountRoot != null ) {
      if ( accountRoot.getUnrMsgs() == 0 ) {
        this.imageFileHash = protocolFileHash;
        this.imageIndex = accountRoot.getStatusIndex();
      } else {
        this.imageFileHash = IconsType.HASH_CHAT;
        this.imageIndex = ChatItem.TYPE_PLAIN_MSG;
      }
    }
  }
}
