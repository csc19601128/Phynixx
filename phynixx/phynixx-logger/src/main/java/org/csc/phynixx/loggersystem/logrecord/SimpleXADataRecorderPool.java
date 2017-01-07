package org.csc.phynixx.loggersystem.logrecord;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.exceptions.ExceptionUtils;
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
public class SimpleXADataRecorderPool implements IXARecorderProvider,
         IXADataRecorderLifecycleListener {
   private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(SimpleXADataRecorderPool.class);

   private IDataLoggerFactory dataLoggerFactory = null;

   private IDGenerator<Long> messageSeqGenerator = IDGenerators.synchronizeGenerator(IDGenerators
            .createLongGenerator(1l));

   static class DataRecordMemetor {
      private final IXADataRecorder dataRecord;
      private final Exception creationLocation;

      public DataRecordMemetor(IXADataRecorder dataRecord) {
         super();
         this.dataRecord = dataRecord;
         this.creationLocation = new Exception("" + Thread.currentThread() + ":  DataRecord "
                  + dataRecord.getXADataRecorderId() + " created at " + new Date() + " :: ");
      }

      public final IXADataRecorder getDataRecord() {
         return dataRecord;
      }

      @Override
      public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((dataRecord == null) ? 0 : dataRecord.hashCode());
         return result;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         }
         if (obj == null) {
            return false;
         }
         if (!(obj instanceof DataRecordMemetor)) {
            return false;
         }
         DataRecordMemetor other = (DataRecordMemetor) obj;
         if (dataRecord == null) {
            if (other.dataRecord != null) {
               return false;
            }
         } else if (!dataRecord.equals(other.dataRecord)) {
            return false;
         }
         return true;
      }

      @Override
      public String toString() {
         return "DataRecord [dataRecord=" + dataRecord + ExceptionUtils.getStackTrace(this.creationLocation) + "]";
      }

   }

   /**
    * management of the registered XADataRecorder. This structure has to stand
    * heavy multithreaded read and write.
    * 
    * This management is important as all not closed/destroyed logger are
    * tracked
    */
   private Map<Long, DataRecordMemetor> xaDataRecorders = new HashMap<Long, DataRecordMemetor>();

   private boolean closed = false;

   public SimpleXADataRecorderPool(IDataLoggerFactory dataLoggerFactory) {
      this.dataLoggerFactory = dataLoggerFactory;
      if (this.dataLoggerFactory == null) {
         throw new IllegalArgumentException("No dataLoggerFactory set");
      }
   }

   public String getLoggerSystemName() {
      return dataLoggerFactory.getLoggerSystemName();
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
                  xaDataLogger, this);
         synchronized (this) {
            addXADataRecorder(xaDataRecorder);
         }
         LOG.info("IXADataRecord " + xaDataRecorderId + " created in [" + Thread.currentThread() + "]");
         return xaDataRecorder;

      } catch (Exception e) {
         throw new DelegatedRuntimeException(e);
      }

   }

   private void addXADataRecorder(IXADataRecorder xaDataRecorder) {
      if (!this.xaDataRecorders.containsKey(xaDataRecorder.getXADataRecorderId())) {
         this.xaDataRecorders.put(xaDataRecorder.getXADataRecorderId(), new DataRecordMemetor(xaDataRecorder));
      }
   }

   private void removeXADataRecoder(IXADataRecorder xaDataRecorder) {
      if (xaDataRecorder == null) {
         return;
      }
      this.xaDataRecorders.remove(xaDataRecorder.getXADataRecorderId());

   }

   @Override
   public boolean isClosed() {
      return closed;
   }

   @Override
   public IXADataRecorder provideXADataRecorder() {
      if (this.isClosed()) {
         throw new IllegalStateException("Pool already closed");
      }
      return this.createXADataRecorder();
   }


   @Override
   public void close() {
      if (this.isClosed()) {
         return;
      }

      HashSet<DataRecordMemetor> copy = new HashSet<DataRecordMemetor>(this.xaDataRecorders.values());
      for (DataRecordMemetor dataRecorderMemento : copy) {
         LOG.info("IXADataRecord destroyed during Shutdwon " + copy);
         dataRecorderMemento.getDataRecord().disqualify();
      }
      xaDataRecorders.clear();
   }

   @Override
   public void destroy() {

      try {
         this.close();
      } catch (Exception e) {
      }

      this.dataLoggerFactory.cleanup();
      this.messageSeqGenerator = null;

   }

   /**
    * a closed recorder is removed from the repository, but the content isn't
    * discard. the logger is destroyed and is removed form the internal
    * management
    */
   @Override
   public void recorderDataRecorderClosed(IXADataRecorder xaDataRecorder) {
      this.removeXADataRecoder(xaDataRecorder);
   }

   @Override
   public void recorderDataRecorderOpened(IXADataRecorder xaDataRecorder) {
      this.addXADataRecorder(xaDataRecorder);
   }

   /**
    * A logger is rewinded and ready for re-use. But the current Implementation
    * won't ruse logger but destroys them.
    * 
    */
   @Override
   public void recorderDataRecorderReleased(IXADataRecorder xaDataRecorder) {
      xaDataRecorder.destroy();
   }

   /**
    * the logger is destroyed and is removed form the internal management
    */
   @Override
   public void recorderDataRecorderDestroyed(IXADataRecorder xaDataRecorder) {
      this.removeXADataRecoder(xaDataRecorder);

   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((getLoggerSystemName() == null) ? 0 : getLoggerSystemName().hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      final SimpleXADataRecorderPool other = (SimpleXADataRecorderPool) obj;
      if (getLoggerSystemName() == null) {
         if (other.getLoggerSystemName() != null)
            return false;
      } else if (!this.getLoggerSystemName().equals(other.getLoggerSystemName()))
         return false;
      return true;
   }

   @Override
   public String toString() {
      return (this.dataLoggerFactory == null) ? "Closed Pool" : this.dataLoggerFactory.toString();
   }

}
