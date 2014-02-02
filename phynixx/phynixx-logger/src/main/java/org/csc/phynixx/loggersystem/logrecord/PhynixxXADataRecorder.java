package org.csc.phynixx.loggersystem.logrecord;

/*
 * #%L
 * phynixx-common
 * %%
 * Copyright (C) 2014 csc
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


import org.csc.phynixx.exceptions.DelegatedRuntimeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * thisclas is backwards bound to the PhynixxXARecorderResource. The current class manages LogEntries but not know the persistence logger.
 */
public class PhynixxXADataRecorder implements IXADataRecorder {

    private OrdinalGenerator ordinalGenerator = new OrdinalGenerator();

    private long messageSequenceId = -1;

    private List<IDataRecord> messages = new ArrayList<IDataRecord>();

    private XADataLogger dataLogger;


    private transient boolean committing = false;

    private transient boolean completed = false;

    private transient boolean prepared = false;

    private IXADataRecorderLifecycleListener dataRecorderLifycycleListner;


    /**
     * opens an Recorder for read. If no recorder with the given ID exists recorder with no data is returned
     *
     * @param xaDataLogger Strategy to persist the records
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    static PhynixxXADataRecorder recoverDataRecorder(XADataLogger xaDataLogger, IXADataRecorderLifecycleListener dataRecorderLifycycleListner) {
        try {
            PhynixxXADataRecorder dataRecorder = new PhynixxXADataRecorder(-1, xaDataLogger, dataRecorderLifycycleListner);
            dataRecorder.recover();
            return dataRecorder;
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }

    }


    /**
     * recovers the dataRecorder
     * all messages are removed and all the messsages of the logger are recoverd
     */
    @Override
    public void recover() {
        try {
            this.messages.clear();
            this.dataLogger.prepareForRead(this);
            this.dataLogger.recover(this);
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }
    }

    /**
     * opens an Recorder for read. If no recorder with the given ID exists recorder with no data is returned
     *
     * @param messageSequenceId
     * @param xaDataLogger
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    static PhynixxXADataRecorder openRecorderForWrite(long messageSequenceId, XADataLogger xaDataLogger, IXADataRecorderLifecycleListener dataRecorderLifycycleListner) {
        PhynixxXADataRecorder dataRecorder = new PhynixxXADataRecorder(messageSequenceId, xaDataLogger, dataRecorderLifycycleListner);
        try {
            dataRecorder.dataLogger.prepareForWrite(dataRecorder);
            return dataRecorder;
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }
    }

    /**
     * entweder wird dem Recorder explizit ein Logger zugeordnet, poder via recyvcling aus Logger ...
     *
     * @param messageSequenceId
     * @param xaDataLogger
     */
    private PhynixxXADataRecorder(long messageSequenceId, XADataLogger xaDataLogger, IXADataRecorderLifecycleListener dataRecorderLifycycleListner) {
        this.messageSequenceId = messageSequenceId;
        this.dataLogger = xaDataLogger;
        this.dataRecorderLifycycleListner = dataRecorderLifycycleListner;
        if (dataRecorderLifycycleListner != null) {
            this.dataRecorderLifycycleListner.recorderDataRecorderOpened(this);
        }
    }


    void reset() {
        this.committing = false;
        this.completed = false;
        this.prepared = false;
        messages.clear();

    }

    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.loggersystem.ILogMessageSequence#getMessages()
     */
    public List<IDataRecord> getDataRecords() {
        return messages;
    }


    public boolean isCommitting() {
        return committing;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isPrepared() {
        return prepared;
    }

    /**
     * create a new Message with the given data
     */
    public void writeRollbackData(byte[] data) {
        this.writeRollbackData(toBytesBytes(data));
    }


    /**
     * create a new Message with the given data
     */
    public synchronized void writeRollbackData(byte[][] data) {
        IDataRecord msg = this.createDataRecord(XALogRecordType.USER, data);
    }


    public void commitRollforwardData(byte[] data) {
        this.commitRollforwardData(toBytesBytes(data));
    }

    public void commitRollforwardData(byte[][] data) {
            IDataRecord msg = this.createDataRecord(XALogRecordType.XA_COMMIT, data);
    }

    public synchronized void addMessage(IDataRecord message) {
        this.establishState(message);
        this.messages.add(message);
    }

    public synchronized void replayRecords(IDataRecordReplay replay) {

        if (this.messages == null || this.messages.size() == 0) {
            return;
        }

        for (int i = 0; i < messages.size(); i++) {
            IDataRecord msg = this.messages.get(i);
            if (msg.getLogRecordType().equals(XALogRecordType.USER)) {
                if (!this.isCompleted() && !this.isCommitting()) {
                    replay.replayRollback(msg);
                }
            } else if (msg.getLogRecordType().equals(XALogRecordType.XA_COMMIT)) {
                if (this.isCommitting()) {
                    replay.replayRollforward(msg);
                }
            }
        }
    }

    @Override
    public IDataRecord createDataRecord(XALogRecordType logRecordType, byte[][] recordData) {
        PhynixxDataRecord msg = new PhynixxDataRecord(this.getXADataRecorderId(), this.ordinalGenerator.generate(), logRecordType, recordData);
        try {
            this.dataLogger.writeData(this, msg);
            this.addMessage(msg);

            return msg;
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }
    }

    private byte[][] toBytesBytes(byte[] recordData) {
        byte[][] message = new byte[1][];
        message[0] = recordData;
        return message;
    }

    public long getMessageSequenceId() {
        return messageSequenceId;
    }

    void setMessageSequenceId(long messageSequenceId) {
        this.messageSequenceId = messageSequenceId;
    }

    private void establishState(IDataRecord msg) {
        XALogRecordType logRecordType = msg.getLogRecordType();

        if (this.isCommitting() && !logRecordType.equals(XALogRecordType.XA_DONE)) {
            if (logRecordType == XALogRecordType.USER) {
                throw new IllegalStateException("Sequence in State COMMITTING, only XA_DONE/XA_COMMIT are accepted");
            }
        }

        if (this.isCompleted()) {
            throw new IllegalStateException("Sequence in State COMPLETED, no more data is accepted");
        }

        if (logRecordType.equals(XALogRecordType.XA_PREPARED)) {
            this.committing = false;
            this.completed = false;
            this.prepared = true;
        }

        if (logRecordType.equals(XALogRecordType.XA_COMMIT)) {
            this.committing = true;
            this.completed = false;
            this.prepared = false;
        }
        if (logRecordType.equals(XALogRecordType.XA_DONE)) {
            this.committing = false;
            this.completed = true;
            this.prepared = false;
        }
    }

    public int compareTo(Object obj) {

        if (this == obj)
            return 1;
        if (obj == null)
            return 1;

        if (getClass() != obj.getClass())
            return 1;

        final PhynixxXADataRecorder otherMsg = (PhynixxXADataRecorder) obj;

        return Long.valueOf(this.getXADataRecorderId() - otherMsg.getXADataRecorderId()).intValue();
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        final PhynixxXADataRecorder other = (PhynixxXADataRecorder) obj;
        return messageSequenceId == other.messageSequenceId;
    }

    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.loggersystem.ILogMessageSequence#getMessageId()
     */
    public long getXADataRecorderId() {
        return this.messageSequenceId;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Long.valueOf(messageSequenceId).intValue();
        return result;
    }


    public String toString() {
        StringBuffer buffer = new StringBuffer(" { \n");
        for (Iterator iterator = messages.iterator(); iterator.hasNext(); ) {
            buffer.append('\t').append(iterator.next()).append('\n');
        }
        buffer.append(" }");
        return buffer.toString();

    }

    /**
     * current sequence is closed an can be forgotten
     */
    public void close() {
        if (dataRecorderLifycycleListner != null) {
            this.dataRecorderLifycycleListner.recorderDataRecorderClosed(this);
        }
        this.dataLogger.close();
    }

    @Override
    public boolean isClosed() {
        return this.dataLogger.isClosed();
    }

    @Override
    public void destroy() {
        try {
            this.dataLogger.destroy();
        } catch (IOException e) {
            throw new DelegatedRuntimeException(e);
        }
    }

    public void messageSequenceCreated() {
        this.dataRecorderLifycycleListner.recorderDataRecorderOpened(this);
    }


}
