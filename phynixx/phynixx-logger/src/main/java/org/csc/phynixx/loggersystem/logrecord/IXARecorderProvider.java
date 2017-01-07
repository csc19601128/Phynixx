package org.csc.phynixx.loggersystem.logrecord;

/**
 * 
 * provides fresh and ready for use {@link IXADataRecorder}
 * 
 * @author te_zf4iks2
 *
 */
public interface IXARecorderProvider {

   /**
    * provides a fresh DataLogger.
    * 
    * This logger is unleashed and the provided doesn't take care of the
    * lifecycle of the dataLogger.
    * 
    * @return
    */
   IXADataRecorder provideXADataRecorder();

   /**
    * Close this pool, and free any resources associated with it.
    * <p>
    * Calling {@link #addObject addObject} or {@link #borrowObject borrowObject}
    * after invoking this method on a pool will cause them to throw an
    * {@link IllegalStateException}.
    * <p>
    * Implementations should silently fail if not all resources can be freed.
    */
   void close();

   boolean isClosed();

   void destroy();

}
