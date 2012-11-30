/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tomclaw.mandarin.msim;

import com.tomclaw.mandarin.main.BuddyItem;
import com.tomclaw.tcuilite.GroupChild;

/**
 *
 * @author solkin
 */
public class MsimItem extends GroupChild implements BuddyItem {
  
  public MsimItem( String userId ) {
    super( userId );
  }

  public String getUserNick() {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public void setUserNick( String userNick ) {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public String getUserId() {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public void setUserId( String userId ) {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public int[] getLeftImages() {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public void setLeftImages( int[] leftImages ) {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public int getUnreadCount() {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public int getUnreadCount( String resource ) {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public void setUnreadCount( int unreadCount, String resource ) {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public void updateUiData() {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public String getUserPhone() {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public boolean isPhone() {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public void setTypingStatus( boolean isTyping ) {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public boolean getTypingStatus() {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public int getBuddyType() {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public void setUserPhone( String userPhone ) {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public void setBuddyType( int buddyType ) {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public void setIsPhone( boolean isPhone ) {
    throw new UnsupportedOperationException( "Not supported yet." );
  }
  
}
