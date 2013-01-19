package com.tomclaw.mandarin.msim;

import com.tomclaw.mandarin.dc.DirectConnection;
import com.tomclaw.mandarin.main.AccountRoot;
import com.tomclaw.mandarin.main.BuddyGroup;
import com.tomclaw.mandarin.main.BuddyItem;
import com.tomclaw.mandarin.main.Cookie;
import java.io.IOException;
import java.util.Vector;

/**
 *
 * @author solkin
 */
public class MsimAccountRoot extends AccountRoot {

  public MsimAccountRoot( String userId ) {
    super( userId );
  }

  public void construct() {
    host = "home.m1kc.tk";
    port = "3215";
  }

  public void initSpecialData() {
  }

  public String getAccType() {
    return "msim";
  }

  public int getStatusIndex() {
    return 0;
  }

  public void sendTypingStatus( String userId, boolean b ) {
  }

  public void saveSpecialSettings() throws Throwable {
  }

  public byte[] sendMessage( BuddyItem buddyItem, String string, String resource ) throws IOException {
    return null;
  }

  public String getStatusImages() {
    return null;
  }

  public void setTreeItems( Vector buddyList ) {
    this.buddyItems = buddyList;
  }

  public void setPrivateItems( Vector privateList ) {
  }

  public void sortBuddyes() {
  }

  public Cookie addGroup( BuddyGroup buddyGroup ) throws IOException {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public Cookie addBuddy( BuddyItem buddyItem, BuddyGroup buddyGroup ) 
          throws IOException {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public Cookie renameBuddy( String itemName, BuddyItem buddyItem, 
          String phones ) throws IOException {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public Cookie renameGroup( String text, BuddyGroup buddyGroup ) 
          throws IOException {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public void requestAuth( String text, BuddyItem buddyItem ) 
          throws IOException {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public void acceptAuthorization( BuddyItem buddyItem ) throws IOException {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public void requestInfo( String userId, int reqSeqNum ) throws IOException {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public Cookie removeBuddy( BuddyItem buddyItem ) throws IOException {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public Cookie removeGroup( BuddyGroup buddyGroup ) throws IOException {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public BuddyGroup getGroupInstance() {
    return new MsimGroup();
  }

  public BuddyItem getBuddyInstance() {
    return new MsimItem( );
  }

  public DirectConnection getDirectConnectionInstance() {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  public int getNextBuddyId() {
    return 0;
  }

  public int getNextGroupId() {
    return 0;
  }

  public void connectAction( int statusId ) {
    throw new UnsupportedOperationException( "Not supported yet." );
  }
}
