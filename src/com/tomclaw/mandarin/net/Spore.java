package com.tomclaw.mandarin.net;

import com.tomclaw.mandarin.main.ActionExec;
import java.io.ByteArrayOutputStream;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public abstract class Spore extends ByteArrayOutputStream {

  /**
   * Spore mechanism invocation
   */
  public void invoke() {
    try {
      onRun();
    } catch ( Throwable ex ) {
      onError( ex );
    }
    onResult();
  }

  /**
   * Runs first when spore invokes
   * @throws Throwable 
   */
  public abstract void onRun() throws Throwable;

  /**
   * Runs on error in onRun method
   * @param ex 
   */
  public void onError( Throwable ex ) {
    ActionExec.showError( "IO_EXCEPTION" );
  }

  /**
   * Runs in any case after all.
   */
  public void onResult() {
  }

  /**
   * Returns collected bytes
   * @return 
   */
  public byte[] toByteArray() {
    return super.toByteArray();
  }
}
