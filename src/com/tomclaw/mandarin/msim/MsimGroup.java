package com.tomclaw.mandarin.msim;

import com.tomclaw.mandarin.main.BuddyGroup;

/**
 *
 * @author solkin
 */
public class MsimGroup extends BuddyGroup {

  public MsimGroup() {
    super();
  }

  public MsimGroup( String userId ) {
    super( userId );
  }

  public void updateUiData() {
    this.title = userId;
  }
}
