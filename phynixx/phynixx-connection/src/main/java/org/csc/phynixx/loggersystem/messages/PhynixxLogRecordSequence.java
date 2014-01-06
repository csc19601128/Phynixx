package org.csc.phynixx.loggersystem.messages;

/*
 * #%L
 * phynixx-connection
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


import org.csc.phynixx.loggersystem.XALogRecordType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class PhynixxLogRecordSequence implements ILogRecordSequence {

    private OrdinalGenerator ordinalGenerator = null;

    private List messageListeners = new ArrayList();

    private List messageSequenceListeners = new ArrayList();

    private Long messageSequenceId = null;

    private List messages = new ArrayList();


    private transient boolean committing = false;

    private transient boolean completed = false;

    private transient boolean prepared = false;

    public PhynixxLogRecordSequence(Long messageSequenceId) {
        this(messageSequenceId.longValue());
    }


    public PhynixxLogRecordSequence(long messageSequenceId) {
        this.messageSequenceId = new Long(messageSequenceId);
        this.ordinalGenerator = new OrdinalGenerator();
    }

    /* (non-Javadoc)
     * @see de.csc.xaresource.sample.loggersystem.ILogMessageSequence#getMessages()
     */
    public List getMessages() {
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
        ILogRecord msg = this.createNewLogRecord();
        msg.setData(data);
    }


    public void commitRollforwardData(byte[] data) {
        this.commitRollforwardData(new byte[][]{data});
    }

    public void commitRollforwardData(byte[][] data) {
        ILogRecord msg = this.createNewMessage(XALogRecordType.XA_COMMIT);
        msg.setData(data);
    }

    public synchronized void addMessage(ILogRecord message) {
        this.checkState(message);
        this.messages.add(message);
    }

    public synchronized void replayRecords(ILogRecordReplay replay) {

        if (this.messages == null || this.messages.size() == 0) {
            return;
        }
        for (int i = 0; i < messages.size(); i++) {
            ILogRecord msg = (ILogRecord) this.messages.get(i);
            if (msg.getLogRecordType().equals(XALogRecordType.USER)) {
                replay.replayRollback(msg);
            } else if (msg.getLogRecordType().equals(XALogRecordType.XA_COMMIT)) {
                replay.replayRollforward(msg);
            }
        }
    }

    public ILogRecord createNewLogRecord() {
        return this.createNewMessage(XALogRecordType.USER);
    }


    public synchronized ILogRecord createNewMessage(XALogRecordType logRecordType) {
        PhynixxLogRecord msg = new PhynixxLogRecord(this, this.ordinalGenerator.generate(), logRecordType);
        return msg;
    }

    public ILogRecord recover(int ordinal, short logRecordType, byte[][] data) {
        PhynixxLogRecord msg = new PhynixxLogRecord(this, ordinal, logRecordType, data);
        return msg;
    }


    private void checkState(ILogRecord msg) {
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

        final PhynixxLogRecordSequence otherMsg = (PhynixxLogRecordSequence) obj;

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

        final PhynixxLogRecordSequence other = (PhynixxLogRecordSequence) obj;
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

    public void addLogRecordListener(ILogRecordListener listener) {
        if (!messageListeners.contains(listener)) {
            this.messageListeners.add(listener);
        }
    }

    public void removeLogRecordListener(ILogRecordListener listener) {
        this.messageListeners.remove(listener);
    }

    /**
     * current sequence is closed an can be forgotten
     */
    public void close() {
        this.fireMessageSequenceCompleted();
    }


    interface IMessageEventDeliver {
        void fireEvent(ILogRecordListener listener, ILogRecord message);
    }

    private void fireEvents(IMessageEventDeliver deliver, ILogRecord message) {
        // copy all listeners as the callback may change the list of listeners ...
        List tmp = new ArrayList(this.messageListeners);
        for (int i = 0; i < tmp.size(); i++) {
            ILogRecordListener listener = (ILogRecordListener) tmp.get(i);
            deliver.fireEvent(listener, message);
        }
    }


    void fireMessageCreated(ILogRecord message) {
        IMessageEventDeliver deliver = new IMessageEventDeliver() {
            public void fireEvent(ILogRecordListener listener, ILogRecord message) {
                listener.recordCreated(message);
            }
        };
        fireEvents(deliver, message);
    }

    void fireMessageCompleted(ILogRecord message) {
        IMessageEventDeliver deliver = new IMessageEventDeliver() {
            public void fireEvent(ILogRecordListener listener, ILogRecord message) {
                listener.recordCompleted(message);
            }
        };
        fireEvents(deliver, message);
    }

    public void addLogRecordSequenceListener(ILogRecordSequenceListener listener) {
        if (!messageSequenceListeners.contains(listener)) {
            this.messageSequenceListeners.add(listener);
        }
        // initial notification ...
        listener.recordSequenceCreated(this);
    }

    public void removeLogRecordSequenceListener(ILogRecordSequenceListener listener) {

        this.messageSequenceListeners.remove(listener);
    }


    interface IMessageSequenceEventDeliver {
        void fireEvent(ILogRecordSequenceListener listener, ILogRecordSequence seq);
    }

    private void fireEvents(IMessageSequenceEventDeliver deliver) {
        // copy all listeners as the callback may change the list of listeners ...
        List tmp = new ArrayList(this.messageSequenceListeners);
        for (int i = 0; i < tmp.size(); i++) {
            ILogRecordSequenceListener listener = (ILogRecordSequenceListener) tmp.get(i);
            deliver.fireEvent(listener, this);
        }
    }


    void fireMessageSequenceCreated() {
        IMessageSequenceEventDeliver deliver = new IMessageSequenceEventDeliver() {
            public void fireEvent(ILogRecordSequenceListener listener, ILogRecordSequence seq) {
                listener.recordSequenceCreated(seq);
            }
        };
        fireEvents(deliver);
    }

    void fireMessageSequenceCompleted() {
        IMessageSequenceEventDeliver deliver = new IMessageSequenceEventDeliver() {
            public void fireEvent(ILogRecordSequenceListener listener, ILogRecordSequence seq) {
                listener.recordSequenceCompleted(seq);
            }
        };
        fireEvents(deliver);
    }


}
