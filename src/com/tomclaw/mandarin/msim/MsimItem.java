/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tomclaw.mandarin.msim;

import com.tomclaw.mandarin.main.BuddyItem;

/**
 *
 * @author solkin
 */
public class MsimItem extends BuddyItem {
  
  public MsimItem( String userId ) {
    super( userId );
  }

  public void setUnreadCount( int unreadCount, String resource ) {
    
  }

  public int getUnreadCount() {
    return 0;
  }

  public int getUnreadCount( String resource ) {
    return 0;
  }

  public void updateUiData() {
    
  }
  
}
