package org.csc.phynixx.tutorial;

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


import org.csc.phynixx.common.exceptions.DelegatedRuntimeException;
import org.csc.phynixx.common.io.LogRecordReader;
import org.csc.phynixx.common.io.LogRecordWriter;
import org.csc.phynixx.connection.RequiresTransaction;
import org.csc.phynixx.loggersystem.logrecord.IDataRecord;
import org.csc.phynixx.loggersystem.logrecord.IDataRecordReplay;
import org.csc.phynixx.loggersystem.logrecord.IXADataRecorder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Basisklasse zur Verwaltung von Filezugriffen.
 * <p/>
 * A RandomAccessFile provides random access to the file's content.
 */
public class TAEnabledUTFWriterImpl implements TAEnabledUTFWriter {


    private transient String lockToken;
    /**
     * Das RandomAccessFile, dass zum Schreiben u. Lesen geoeffnet wird.
     */
    private UTFWriter utfWriter = null;

    private IXADataRecorder xaDataRecorder;

    private boolean autoCommit=false;

    private transient long rollbackPosition;

    private final String connectionId;

    public TAEnabledUTFWriterImpl(String connectionId, UTFWriter writer) throws Exception {

        this.connectionId = connectionId;
        this.utfWriter= writer;
        init();
    }

    /**
     * locks the sharedFile and inits the rollback  pointer
     */
    private void init() throws Exception{
        // try to lock the writer
        this.lockToken= this.getUTFWriter().lock();

        this.rollbackPosition= this.getUTFWriter().size();
    }

    public String getConnectionId() {
        return connectionId;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    /**
     * Schliesst die Datei und den FileChannel
     */
    public void close() {
        if(lockToken!=null) {
            this.getUTFWriter().unlock(this.lockToken);
            this.lockToken=null;
        }
    }

    /**
     * zeigt an, ob die Instanz geschlossen ist
     *
     * @return true wenn die Datei geschlossen ist
     */
    public boolean isClosed() {
        return (this.utfWriter == null);
    }


    @Override
    @RequiresTransaction
    public void resetContent() throws IOException {

        if (this.isClosed()) {
            throw new IllegalStateException("Writer is closed");
        }
        this.getUTFWriter().resetContent();

        this.rollbackPosition = 0;

        if (this.getXADataRecorder() != null) {
            LogRecordWriter logRecordWriter = new LogRecordWriter().writeLong(position());
            this.getXADataRecorder().writeRollbackData(logRecordWriter.toByteArray());
        }
    }

    @Override
    @RequiresTransaction
    public TAEnabledUTFWriter write(String value) throws IOException {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        this.getUTFWriter().write(value);

        return this;
    }

    @Override
    public List<String> readContent() throws IOException {

      return this.getUTFWriter().readContent();
    }


    UTFWriter getUTFWriter() {
        if (this.isClosed()) {
            throw new IllegalStateException("RandomAccessFile is close");
        }

        return utfWriter;
    }


    /**
     * bereitet die Writer zur Wiederverwendung vor
     */
    @Override
    public void reset() {
        try {
            this.close();
            this.init();
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }

    }


    @Override
    public void commit() {
        try {
            LogRecordWriter logRecordWriter = new LogRecordWriter().writeLong(position());
            this.getXADataRecorder().writeRollforwardData(logRecordWriter.toByteArray());
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }
    }

    private long position() throws IOException {
        return this.utfWriter.position();
    }

    @Override
    public void rollback() {
        try {
            this.getUTFWriter().restoreSize(this.rollbackPosition);
        } catch (IOException e) {
            throw new DelegatedRuntimeException(e);
        }
    }

    /**
     * definiert den gueltigen Zustand nach commit
     */
    @Override
    public void prepare() {

    }

    @Override
    public String toString() {
        return "TAEnabledUTFWriterImpl{" +
                "connectionId='" + connectionId + '\'' +
                '}';
    }

    /**
     * @param xaDataRecorder
     */
    @Override
    public void setXADataRecorder(IXADataRecorder xaDataRecorder) {
        this.xaDataRecorder = xaDataRecorder;
    }

    @Override
    public IXADataRecorder getXADataRecorder() {
        return xaDataRecorder;
    }


    private class DataRecordReplay implements IDataRecordReplay {

        @Override
        public void replayRollback(IDataRecord record) {
            LogRecordReader logRecordReader = new LogRecordReader(record.getData()[0]);
            try {
                String fileName= logRecordReader.readUTF();
                long filePosition=logRecordReader.readLong();
                TAEnabledUTFWriterImpl.this.getUTFWriter().restoreSize(filePosition);
                TAEnabledUTFWriterImpl.this.rollbackPosition= filePosition;
            } catch (IOException e) {
                throw new DelegatedRuntimeException(e);
            }
        }

        @Override
        public void replayRollforward(IDataRecord record) {

        }
    }

    @Override
    public IDataRecordReplay recoverReplayListener() {
        return new DataRecordReplay();
    }
}