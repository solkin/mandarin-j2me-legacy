package com.tomclaw.mandarin.net;

import com.tomclaw.utils.LogUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

/**
 * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
 * http://www.tomclaw.com/
 * @author Solkin
 */
public class SporedStream extends OutputStream {

  private OutputStream outputStream;
  private Vector sporeQueue;
  public boolean isAlive;
  public Thread thread;

  /**
   * Constructs SporedStream from output stream
   * @param outputStream 
   */
  public SporedStream( OutputStream outputStream ) {
    this.outputStream = outputStream;
    sporeQueue = new Vector();
    isAlive = false;
    thread = null;
    start();
  }

  /**
   * Inserts XmlSpore into queue
   * @param xmlSpore 
   */
  public void releaseSpore( Spore spore ) {
    sporeQueue.addElement( spore );
  }

  /**
   * Starting listener thread
   */
  private void start() {
    /** Stopping thread if it is already running **/
    stop();
    /** Creating thread instance **/
    thread = new Thread() {
      public void run() {
        /** Setting up isAlive to true value **/
        isAlive = true;
        LogUtil.outMessage( "Spores stream now alive. " );
        try {
          /** Cycling while alive **/
          while ( isAlive ) {
            /** Checking for queue is not empty **/
            if ( sporeQueue.isEmpty() ) {
              LogUtil.outMessage( "Zzz... (Spored stream)" );
              sleep( 1000 );
            } else {
              LogUtil.outMessage( "Spore preparing... " );
              /** Obtain first XML spore item **/
              Spore spore = ( Spore ) sporeQueue.firstElement();
              /** XML build invocation **/
              spore.invoke();
              /** Sending data **/
              outputStream.write( spore.toByteArray() );
              /** Flushing **/
              outputStream.flush();
              /** Removing first spore item **/
              sporeQueue.removeElementAt( 0 );
              LogUtil.outMessage( "Spore sent. " );
            }
          }
          /** We are normally disconnected **/
          LogUtil.outMessage( "Cycle exit (spored). " );
        } catch ( Throwable ex ) {
          /** Something strange in stream **/
          LogUtil.outMessage( "Exception in spored stream thread: " + ex.getMessage(), true );
        }
        /** Destroying thread **/
        isAlive = false;
        stop();
        LogUtil.outMessage( "Connection destroyed (spored). " );
      }
    };
    /** Setting up new thread **/
    thread.setPriority( Thread.MIN_PRIORITY );
    /** Thread start **/
    thread.start();
  }

  /**
   * Stopping listener thread
   */
  private void stop() {
    /** Checking thread for non-null **/
    if ( thread != null ) {
      LogUtil.outMessage( "Thread stopping (spored)..." );
      if ( isAlive ) {
        /** Stopping parser cycle **/
        isAlive = false;
        try {
          /** Waiting for thread to stop **/
          thread.join();
        } catch ( InterruptedException ex ) {
          LogUtil.outMessage( "Exception while stopping spored stream thread: " 
                  + ex.getMessage(), true );
        }
      }
      thread = null;
      LogUtil.outMessage( "Thread stopped (spored)." );
    } else {
      /** Stopping parser cycle **/
      isAlive = false;
    }
  }

  /**
   * Flushing stream
   * @throws IOException 
   */
  public void flush() throws IOException {
    outputStream.flush();
  }

  /**
   * Direct writing char to output stream; may cause stream fall
   * @param b
   * @throws IOException 
   */
  public void write( int b ) throws IOException {
    outputStream.write( b );
  }

  /**
   * Direct writing byte array to output stream; may cause stream fall
   * @param b
   * @throws IOException 
   */
  public void write( byte[] b ) throws IOException {
    outputStream.write( b );
  }
  
  /**
   * Stopping thread cycle and closing output stream
   * @throws IOException 
   */
  public void close() throws IOException {
    stop();
    outputStream.close();
  }
}
