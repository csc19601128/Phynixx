package org.csc.phynixx.loggersystem.messages;

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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * thisclas is backwards bound to the PhynixxXARecorderResource. The current class manages LogEntries but not know the persistence logger.
 */
public class PhynixxXADataRecorder implements IXADataRecorder {

    private OrdinalGenerator ordinalGenerator = null;

    private Long messageSequenceId = null;

    private List<IDataRecord> messages = new ArrayList<IDataRecord>();

    private PhynixxXARecorderResource resourceLogger;


    private transient boolean committing = false;

    private transient boolean completed = false;

    private transient boolean prepared = false;


    PhynixxXADataRecorder(long messageSequenceId, PhynixxXARecorderResource resourceLogger) {
        this.resourceLogger = resourceLogger;
        this.messageSequenceId = new Long(messageSequenceId);
        this.ordinalGenerator = new OrdinalGenerator();
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
    public List<IDataRecord> getMessages() {
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
        this.writeRollbackData(new byte[][]{data});
    }


    /**
     * create a new Message with the given data
     */
    public synchronized void writeRollbackData(byte[][] data) {
        IDataRecord msg = this.createNewLogRecord();
        msg.setData(data);
    }


    public void commitRollforwardData(byte[] data) {
        this.commitRollforwardData(new byte[][]{data});
    }

    public void commitRollforwardData(byte[][] data) {
        IDataRecord msg = this.createNewMessage(XALogRecordType.XA_COMMIT);
        msg.setData(data);
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
                replay.replayRollback(msg);
            } else if (msg.getLogRecordType().equals(XALogRecordType.XA_COMMIT)) {
                replay.replayRollforward(msg);
            }
        }
    }

    public IDataRecord createNewLogRecord() {
        return this.createNewMessage(XALogRecordType.USER);
    }


    public synchronized IDataRecord createNewMessage(XALogRecordType logRecordType) {
        PhynixxDataRecord msg = new PhynixxDataRecord(this, this.ordinalGenerator.generate(), logRecordType);
        // the record adds itself via addMessage
        return msg;
    }

    public IDataRecord recover1(int ordinal, XALogRecordType logRecordType, byte[][] data) {
        PhynixxDataRecord msg = new PhynixxDataRecord(this, ordinal, logRecordType, data);
        return msg;
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

        long diff = this.getLogRecordSequenceId().longValue() - otherMsg.getLogRecordSequenceId().longValue();
        return (diff == 0) ? 0 : (diff < 0) ? -1 : 1;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        final PhynixxXADataRecorder other = (PhynixxXADataRecorder) obj;
        if (messageSequenceId == null) {
            if (other.messageSequenceId != null)
                return false;
        } else if (!messageSequenceId.equals(other.messageSequenceId))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.loggersystem.ILogMessageSequence#getMessageId()
     */
    public Long getLogRecordSequenceId() {
        return this.messageSequenceId;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((messageSequenceId == null) ? 0 : messageSequenceId.hashCode());
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
        this.resourceLogger.recordSequenceCompleted(this);
    }


    public void messageSequenceCreated() {
        this.resourceLogger.recordSequenceCreated(this);
    }

    public void messageSequenceCompleted() {
        this.resourceLogger.recordSequenceCompleted(this);
    }


    public void recordCreated(IDataRecord record) {
        this.resourceLogger.recordCreated(record);
    }

    public void recordCompleted(IDataRecord record) {
        this.resourceLogger.recordCompleted(record);
    }
}
