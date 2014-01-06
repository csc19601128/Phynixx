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


import org.csc.phynixx.loggersystem.XALogRecordType;


public class PhynixxLogRecord implements ILogRecord {

    private static final byte[][] EMPTY_DATA = new byte[][]{};

    private Integer ordinal = null;
    private PhynixxLogRecordSequence messageSequence;
    private byte[][] records = null;

    private boolean readOnly = false;

    private XALogRecordType logRecordType = XALogRecordType.UNKNOWN;


    /**
     * called to recover a message
     *
     * @param messageSequence
     * @param ordinal
     * @param logRecordType
     * @param data
     */
    public PhynixxLogRecord(PhynixxLogRecordSequence messageSequence, int ordinal, short logRecordType, byte[][] data) {
        super();
        this.ordinal = new Integer(ordinal);
        this.messageSequence = messageSequence;
        this.records = data;
        this.logRecordType = XALogRecordType.resolve(logRecordType);
        this.readOnly = true;
        this.messageSequence.addMessage(this);

        this.messageSequence.fireMessageCreated(this);
    }

    public PhynixxLogRecord(PhynixxLogRecordSequence messageSequence, int ordinal, XALogRecordType logRecordType) {
        super();
        this.ordinal = new Integer(ordinal);
        this.messageSequence = messageSequence;
        this.logRecordType = logRecordType;
        this.readOnly = false;
        this.messageSequence.addMessage(this);

        this.messageSequence.fireMessageCreated(this);
    }

    public XALogRecordType getLogRecordType() {
        return logRecordType;
    }

    public Long getRecordSequenceId() {
        return this.messageSequence.getLogRecordSequenceId();
    }

    public byte[][] getData() {
        return (records != null) ? records : EMPTY_DATA;
    }


    /**
     * sets the data. The message is written to the logger and can not be modified	 *
     * this methods is equivalent to setData(data,true)
     *
     * @param data
     * @throws IllegalStateException message is readonly
     * @see #isReadOnly()
     * @see #setData(byte[], boolean)
     */
    public void setData(byte[] data) {
        this.setData(new byte[][]{data});
    }


    /**
     *
     */
    public void setData(byte[][] data) {
        if (this.isReadOnly()) {
            throw new IllegalStateException("Message is read only an can't be modified");
        }
        if (data == null) {
            this.records = EMPTY_DATA;
        }

        // count the not empty ones
        int count = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] != null) count++;
        }

        if (count == 0) {
            this.records = EMPTY_DATA;
        }

        this.records = new byte[count][];
        count = -1;
        for (int i = 0; i < data.length; i++) {
            if (data[i] != null) {
                count++;
                this.records[count] = data[i];
            }
        }
        this.flush();
    }

    /**
     * even an empty message is written headed by the system data of an messsage
     */
    private void flush() {
        if (this.readOnly) {
            return;
        }

        // call back the sequence to inform that the current message is complete
        this.messageSequence.fireMessageCompleted(this);

        this.readOnly = true;
    }

    public Integer getOrdinal() {
        return this.ordinal;
    }


    public boolean isReadOnly() {
        return this.readOnly;
    }

    public String toString() {
        StringBuffer recordLength = new StringBuffer();

        recordLength.append("{");
        if (this.records != null && this.records.length > 0) {
            for (int i = 0; i < records.length; i++) {
                recordLength.append('[').append(records[i].length).append("] ");
            }
        }
        recordLength.append("}");
        return "LogMessage messageSequence=" + this.getRecordSequenceId() + " ordinal=" + this.getOrdinal() + " logRecordType=" + logRecordType + " records=" + recordLength.toString();
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((getRecordSequenceId() == null) ? 0 : getRecordSequenceId()
                .hashCode());
        result = prime * result + ((ordinal == null) ? 0 : ordinal.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        return this.compareTo(obj) == 0;
    }

    public int compareTo(Object obj) {
        if (obj == null) {
            return 1;
        }

        if (!(obj instanceof PhynixxLogRecord)) {
            return 1;
        }

        PhynixxLogRecord otherMsg = (PhynixxLogRecord) obj;

        if (otherMsg.getRecordSequenceId().equals(this.getRecordSequenceId())) {
            long diff = this.getRecordSequenceId().longValue() - otherMsg.getRecordSequenceId().longValue();
            return (diff < 0) ? -1 : 1;
        } else {
            int diff = this.getOrdinal().intValue() - otherMsg.getOrdinal().intValue();
            return (diff == 0) ? 0 : (diff < 0) ? -1 : 1;
        }

    }


}
