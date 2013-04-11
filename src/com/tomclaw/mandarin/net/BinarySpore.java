package com.tomclaw.mandarin.net;

import com.tomclaw.mandarin.core.Handler;
import java.io.ByteArrayOutputStream;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2013
 * http://www.tomclaw.com/
 * @author Solkin
 */
public abstract class BinarySpore extends ByteArrayOutputStream implements Spore {

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
    Handler.showError( "IO_EXCEPTION" );
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
