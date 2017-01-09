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


/**
 * A LogRecord is an atomic piece of information.  This piece of Information ist qualified by an ordinal of an superior.

 * The current class manages the content and is not responsible for writing/persisting the content
 */
class PhynixxDataRecord implements IDataRecord, Comparable<PhynixxDataRecord> {

    private static final byte[][] EMPTY_DATA = new byte[][]{};

    private static final byte[] EMPTY_BYTES = new byte[]{};

    private Integer ordinal = null;

    /**
     * backward reference to the owner of the log record
     */
    private long messageSequenceId;

    /**
     * content
     */
    private byte[][] records = null;

    private XALogRecordType logRecordType = XALogRecordType.UNKNOWN;


    /**
     * If
     *
     * @param messageSequenceId
     * @param ordinal
     * @param logRecordType
     * @param data
     */
    PhynixxDataRecord(long messageSequenceId, int ordinal, XALogRecordType logRecordType, byte[][] data) {

        this.ordinal = ordinal;
        this.logRecordType = logRecordType;
        this.messageSequenceId = messageSequenceId;
        this.setRecords(data);
    }


    public XALogRecordType getLogRecordType() {
        return logRecordType;
    }

    public long getXADataRecorderId() {
        return this.messageSequenceId;
    }

    public byte[][] getData() {
        return (records != null) ? records : EMPTY_DATA;
    }


    /**
     * replaces null with EMPTY_DATA
     *
     * @param data
     */
    private void setRecords(byte[][] data) {
        if (data == null || data.length==0) {
            this.records = EMPTY_DATA;
            return;
        }

        this.records = new byte[data.length][];
        for (int i = 0; i < data.length; i++) {
            if (data[i] != null) {
                this.records[i] = data[i];
            } else {
                this.records[i] = EMPTY_BYTES;
            }
        }
    }

    /**
     * a log record is part of a record sequence and it knows its ordinal
     *
     * @return
     */
    public Integer getOrdinal() {
        return this.ordinal;
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
        return "LogMessage messageSequence=" + this.getXADataRecorderId() + " ordinal=" + this.getOrdinal() + " logRecordType=" + logRecordType + " records=" + recordLength.toString();
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Long.valueOf(messageSequenceId).intValue();
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

        int cmp = Long.valueOf(this.getXADataRecorderId() - otherMsg.getXADataRecorderId()).intValue();
        if (cmp != 0) {
            return cmp;
        } else {
            int diff = this.getOrdinal().intValue() - otherMsg.getOrdinal().intValue();
            return (diff == 0) ? 0 : (diff < 0) ? -1 : 1;
        }

    }


}
