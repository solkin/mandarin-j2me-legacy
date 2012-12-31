package com.tomclaw.mandarin.main;

import java.util.Hashtable;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public abstract class QueueAction {

  public BuddyItem buddyItem;
  public BuddyGroup buddyGroup;
  public AccountRoot accountRoot;
  public Cookie cookie;

  public QueueAction( AccountRoot accountRoot, BuddyItem buddyItem, Cookie cookie ) {
    this.accountRoot = accountRoot;
    this.buddyItem = buddyItem;
    this.cookie = cookie;
  }

  public QueueAction( AccountRoot accountRoot, BuddyGroup buddyGroup, Cookie cookie ) {
    this.accountRoot = accountRoot;
    this.buddyGroup = buddyGroup;
    this.cookie = cookie;
  }

  public void setBuddyItem( BuddyItem buddyItem ) {
    this.buddyItem = buddyItem;
  }

  public void setBuddyItem( BuddyGroup buddyGroup ) {
    this.buddyGroup = buddyGroup;
  }

  public BuddyItem getBuddyItem() {
    return buddyItem;
  }

  public BuddyGroup getBuddyGroup() {
    return buddyGroup;
  }

  public void setAccountRoot( AccountRoot accountRoot ) {
    this.accountRoot = accountRoot;
  }

  public AccountRoot getAccountRoot() {
    return accountRoot;
  }

  public void setCookie( Cookie cookie ) {
    this.cookie = cookie;
  }

  public Cookie getCookie() {
    return cookie;
  }

  public void actionPerformed( Hashtable params ) {
    // Empty method, will be overwritten 
  }
}
