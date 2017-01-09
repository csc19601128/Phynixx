package org.csc.phynixx.loggersystem.logrecord;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.generator.IDGenerator;
import org.csc.phynixx.common.generator.IDGenerators;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.IDataLogger;
import org.csc.phynixx.loggersystem.logger.IDataLoggerFactory;

/**
 * This provider doesn't reuse released and reset IXADataLogger
 * 
 * @author te_zf4iks2
 *
 */
public class XADataRecorderPool implements IXARecorderProvider, IXADataRecorderLifecycleListener {

   private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(XADataRecorderPool.class);

   private class XADataLoggerPooledObjectFactory extends BasePooledObjectFactory<IXADataRecorder> {

      private IDataLoggerFactory dataLoggerFactory = null;
      private IDGenerator<Long> messageSeqGenerator = IDGenerators.synchronizeGenerator(IDGenerators
               .createLongGenerator(1l));

      public XADataLoggerPooledObjectFactory(IDataLoggerFactory dataLoggerFactory) {
         super();
         this.dataLoggerFactory = dataLoggerFactory;
         if (this.dataLoggerFactory == null) {
            throw new IllegalArgumentException("No dataLoggerFactory set");
         }
      }

      public String getLoggerSystemName() {
         return dataLoggerFactory.getLoggerSystemName();
      }

      void destroyResources() {
         this.dataLoggerFactory.cleanup();
         this.messageSeqGenerator = null;
      }

      /**
       * 
       * {@link PooledObjectFactory}
       */

      @Override
      public IXADataRecorder create() throws Exception {
         return this.createXADataRecorder();
      }

      @Override
      public PooledObject<IXADataRecorder> wrap(IXADataRecorder obj) {
         return new DefaultPooledObject<>(obj);

      }

      @Override
      public void destroyObject(PooledObject<IXADataRecorder> p) throws Exception {
         // TODO Auto-generated method stub
         super.destroyObject(p);
      }

      @Override
      public boolean validateObject(PooledObject<IXADataRecorder> p) {
         // TODO Auto-generated method stub
         return super.validateObject(p);
      }

/**
       * opens a new Recorder for writing. The recorder gets a new ID.
       * 
       * The lifecycle of the dataRecorder is managed by the current implementation of {@link 
       * 
       * @return created dataRecorder
       */
      private IXADataRecorder createXADataRecorder() {

         try {
            long xaDataRecorderId = this.messageSeqGenerator.generate();

            // create a new Logger
            IDataLogger dataLogger = this.dataLoggerFactory.instanciateLogger(Long.toString(xaDataRecorderId));

            // create a new XADataLogger
            XADataLogger xaDataLogger = new XADataLogger(dataLogger);

            PhynixxXADataRecorder xaDataRecorder = PhynixxXADataRecorder.openRecorderForWrite(xaDataRecorderId,
                     xaDataLogger, XADataRecorderPool.this);
            LOG.info("IXADataRecord " + xaDataRecorderId + " created in [" + Thread.currentThread() + "]");
            return xaDataRecorder;

         } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
         }

      }

   }

   private final XADataLoggerPooledObjectFactory dataLoggerPooledObjectFactory;

   private final GenericObjectPool<IXADataRecorder> objectPool;

   public XADataRecorderPool(IDataLoggerFactory dataLoggerFactory) {
      this.dataLoggerPooledObjectFactory = new XADataLoggerPooledObjectFactory(dataLoggerFactory);
      GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
      cfg.setTestOnBorrow(true);
      cfg.setTestOnReturn(true);
      cfg.setMaxTotal(Integer.MAX_VALUE);
      this.objectPool = new GenericObjectPool<>(this.dataLoggerPooledObjectFactory, cfg);
   }

   @Override
   public boolean isClosed() {
      return this.objectPool.isClosed();
   }

   @Override
   public IXADataRecorder provideXADataRecorder() {
      try {
         return this.objectPool.borrowObject();
      } catch (Exception e) {
         throw new DelegatedRuntimeException(e);
      }
   }

   @Override
   public void close() {
      if (this.isClosed()) {
         return;
      }
      this.objectPool.close();
   }

   @Override
   public void destroy() {

      try {
         this.close();
      } catch (Exception e) {
      }

      this.dataLoggerPooledObjectFactory.destroyResources();

   }

   /**
    * 
    * {@link IXADataRecorderLifecycleListener}
    */
   @Override
   public void recorderDataRecorderClosed(IXADataRecorder xaDataRecorder) {
      try {
         this.objectPool.invalidateObject(xaDataRecorder);
      } catch (Exception e) {
         throw new DelegatedRuntimeException(e);
      }

   }

   @Override
   public void recorderDataRecorderOpened(IXADataRecorder xaDataRecorder) {
      // dataRecorder is created by the pool; no further notification
      // neccessary
   }

   @Override
   public void recorderDataRecorderReleased(IXADataRecorder xaDataRecorder) {
      this.objectPool.returnObject(xaDataRecorder);

   }


}
