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


/**
 * A LogRecord is an atomic piece of information.
 * <p/>
 * A record can be written once. If data is delivered the record cannot be changed {@link #isReadOnly()}
 * <p/>
 * A record knows its owner as every change of state is recorded to this owner.
 */
class PhynixxDataRecord implements IDataRecord, Comparable<PhynixxDataRecord> {

    private static final byte[][] EMPTY_DATA = new byte[][]{};

    private Integer ordinal = null;

    /**
     * backward reference to the owner of the log record
     */
    private PhynixxXADataRecorder messageSequence;

    /**
     * content
     */
    private byte[][] records = null;

    private boolean readOnly = false;

    private XALogRecordType logRecordType = XALogRecordType.UNKNOWN;


    /**
     * If
     *
     * @param messageSequence owner of the record (bachward reference)
     * @param ordinal
     * @param logRecordType
     * @param data
     */
    public PhynixxDataRecord(PhynixxXADataRecorder messageSequence, int ordinal, XALogRecordType logRecordType, byte[][] data) {
        this(messageSequence, ordinal, logRecordType);
        this.setData(data);
    }

    /**
     * @param messageSequence owner of the record
     * @param ordinal         ordinal according to the messageSequence
     * @param logRecordType
     */
    PhynixxDataRecord(PhynixxXADataRecorder messageSequence, int ordinal, XALogRecordType logRecordType) {
        super();
        this.ordinal = new Integer(ordinal);
        this.messageSequence = messageSequence;
        this.logRecordType = logRecordType;
        this.readOnly = false;
        this.messageSequence.addMessage(this);

        this.messageSequence.recordCreated(this);
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
     * @see #setData(byte[][])
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
        // this message gonna be stored
        this.messageSequence.recordCompleted(this);

        this.readOnly = true;
    }

    /**
     * a log record is part of a record sequence and it knows its ordinal
     *
     * @return
     */
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

        if (obj instanceof PhynixxDataRecord) {
            return this.compareTo((PhynixxDataRecord) obj) == 0;
        } else {
            return false;
        }
    }

    public int compareTo(PhynixxDataRecord otherMsg) {
        if (otherMsg == null) {
            return 1;
        }

        if (otherMsg.getRecordSequenceId().equals(this.getRecordSequenceId())) {
            long diff = this.getRecordSequenceId().longValue() - otherMsg.getRecordSequenceId().longValue();
            return (diff < 0) ? -1 : 1;
        } else {
            int diff = this.getOrdinal().intValue() - otherMsg.getOrdinal().intValue();
            return (diff == 0) ? 0 : (diff < 0) ? -1 : 1;
        }

    }


}
