package org.csc.phynixx.loggersystem.logrecord;

/*
 * #%L
 * phynixx-logger
 * %%
 * Copyright (C) 2014 Christoph Schmidt-Casdorff
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.exceptions.ExceptionUtils;
import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.IDataLogger;
import org.csc.phynixx.loggersystem.logger.IDataLoggerReplay;
import org.csc.phynixx.loggersystem.logger.channellogger.AccessMode;

/**
 * brings IXADataRecorder and IDataLogger together.
 * 
 * An instance keeps an {@link IDataLogger} representing the physical logging
 * strategy.
 * 
 * Its permitted (but not recommended) to shared or reuse a dataLogger
 * 
 * Therefore the dataLogger isn't associated to the dataRecorder, but the
 * xaDataRecorder operates on the current dataLogger.
 * 
 * 
 * Created by christoph on 10.01.14.
 */
class XADataLogger {

   private static final IPhynixxLogger LOGGER = PhynixxLogManager.getLogger(XADataLogger.class);

   private static final int HEADER_SIZE = 8 + 4;

   private final IDataLogger dataLogger;

   XADataLogger(IDataLogger dataLogger) {
      this.dataLogger = dataLogger;
   }

   public boolean isClosed() {
      return this.dataLogger.isClosed();
   }

   public void destroy() throws IOException {
      this.dataLogger.destroy();
   }

   /**
    * callback to recover the content of the xadataRecorder
    * 
    * @author christoph
    *
    */
   private class RecoverReplayListener implements IDataLoggerReplay {

      private int count = 0;

      private PhynixxXADataRecorder dataRecorder;

      private RecoverReplayListener(PhynixxXADataRecorder dataRecorder) {
         this.dataRecorder = dataRecorder;
      }

      public int getCountLogRecords() {
         return count;
      }

      @Override
      public void onRecord(XALogRecordType recordType, byte[][] fieldData) {
         if (count == 0) {
            // recovers the message sequence id
            dataRecorder.setMessageSequenceId(XADataLogger.this.recoverMessageSequenceId(fieldData[0]));
         } else {
            short typeId = recordType.getType();
            switch (typeId) {
            case XALogRecordType.XA_START_TYPE:
            case XALogRecordType.XA_PREPARED_TYPE:
            case XALogRecordType.ROLLFORWARD_DATA_TYPE:
            case XALogRecordType.XA_DONE_TYPE:
            case XALogRecordType.USER_TYPE:
            case XALogRecordType.ROLLBACK_DATA_TYPE:
               XADataLogger.this.recoverData(dataRecorder, recordType, fieldData);
               break;
            default:
               LOGGER.error("Unknown LogRecordtype " + recordType);
               break;
            }
         }

         count++;
      }

   }

   /**
    * prepares the Logger for writing. The current content is removed.
    *
    * @param dataRecorder
    * @throws IOException
    * @throws InterruptedException
    */
   void prepareForWrite(long xaDataRecorderId) throws IOException, InterruptedException {
      this.dataLogger.reopen(AccessMode.WRITE);
      this.writeStartSequence(xaDataRecorderId);
   }

   /**
    * prepares the Logger for writing.
    * 
    * @throws IOException
    * @throws InterruptedException
    */
   void prepareForAppend() throws IOException, InterruptedException {
      this.dataLogger.reopen(AccessMode.APPEND);
   }

   /**
    * prepares the Logger for writing.
    *
    * @throws IOException
    * @throws InterruptedException
    */
   void prepareForRead() throws IOException, InterruptedException {
      this.dataLogger.reopen(AccessMode.READ);
   }

   /**
    *
    *
    * @param message
    *           message to be written
    * @throws IOException
    */
   void writeData(IDataRecord message) throws IOException {
      DataOutputStream io = null;
      try {

         ByteArrayOutputStream byteIO = new ByteArrayOutputStream(HEADER_SIZE);
         io = new DataOutputStream(byteIO);

         io.writeLong(message.getXADataRecorderId());
         io.writeInt(message.getOrdinal().intValue());
         byte[] header = byteIO.toByteArray();

         byte[][] data = message.getData();
         byte[][] content = null;
         if (data == null) {
            content = new byte[][] { header };
         } else {
            content = new byte[data.length + 1][];
            content[0] = header;
            for (int i = 0; i < data.length; i++) {
               content[i + 1] = data[i];
            }
         }

         try {
            this.dataLogger.write(message.getLogRecordType().getType(), content);
         } catch (Exception e) {
            throw new DelegatedRuntimeException("writing message " + message + "\n" + ExceptionUtils.getStackTrace(e),
                     e);
         }
      } finally {
         if (io != null) {
            io.close();
         }
      }
   }

   /**
    *
    *
    * @param dataRecorder
    *           DataRecorder that uses /operates on the current physical logger
    *
    * @throws IOException
    * @throws InterruptedException
    */
   void recover(PhynixxXADataRecorder dataRecorder) throws IOException, InterruptedException {
      RecoverReplayListener listener = new RecoverReplayListener(dataRecorder);
      dataRecorder.rewind();
      this.dataLogger.replay(listener);
      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("# Records=" + listener.getCountLogRecords());
      }
   }

   /**
    *
    * a new data record is created an added to dataRecorder. It's not checked if
    * the record is permissable
    *
    * @param dataRecorder
    *           DataRecorder that uses /operates on the current physical logger
    *
    * @param logRecordType
    * @param fieldData
    * 
    * 
    */
   private void recoverData(PhynixxXADataRecorder dataRecorder, XALogRecordType logRecordType, byte[][] fieldData) {
      if (LOGGER.isDebugEnabled()) {
         if (fieldData == null || fieldData.length == 0) {
            throw new IllegalArgumentException("Record fields are empty");
         }
      }
      // field 0 is header
      byte[] headerData = fieldData[0];
      DataInputStream io = null;
      try {
         io = new DataInputStream(new ByteArrayInputStream(headerData));
         // redundant , just read it an skip
         io.readLong();

         int ordinal = io.readInt();
         byte[][] content = null;

         if (fieldData.length > 1) {
            content = new byte[fieldData.length - 1][];
            for (int i = 0; i < fieldData.length - 1; i++) {
               content[i] = fieldData[i + 1];
            }
         } else {
            content = new byte[][] {};
         }

         PhynixxDataRecord msg = new PhynixxDataRecord(dataRecorder.getXADataRecorderId(), ordinal, logRecordType,
                  content);
         dataRecorder.recoverMessage(msg);

      } catch (Exception e) {
         throw new DelegatedRuntimeException(e);
      } finally {
         if (io != null) {
            IOUtils.closeQuietly(io);
         }
      }
   }

   void close() {
      try {
         this.dataLogger.close();
      } catch (Exception e) {
         throw new DelegatedRuntimeException(e);
      }

   }

   /**
    * start sequence writes the ID of the XADataLogger to identify the content
    * of the logger
    *
    * @param dataRecorder
    *           DataRecorder that uses /operates on the current physical logger
    *
    */
   private void writeStartSequence(long xaDataRecorderId) throws IOException, InterruptedException {
      ByteArrayOutputStream byteOut = null;
      try {
         byteOut = new ByteArrayOutputStream();
         DataOutputStream dos = new DataOutputStream(byteOut);
         dos.writeLong(xaDataRecorderId);
         dos.flush();
      } finally {
         if (byteOut != null) {
            IOUtils.closeQuietly(byteOut);
         }
      }
      byte[][] startSequence = new byte[1][];
      startSequence[0] = byteOut.toByteArray();

      this.dataLogger.write(XALogRecordType.USER.getType(), startSequence);
   }

   private long recoverMessageSequenceId(byte[] bytes) {
      byte[] headerData = bytes;
      DataInputStream io = null;
      try {
         io = new DataInputStream(new ByteArrayInputStream(headerData));
         return io.readLong();
      } catch (IOException e) {
         throw new DelegatedRuntimeException(e);
      } finally {
         if (io != null) {
            IOUtils.closeQuietly(io);
         }
      }
   }

}
