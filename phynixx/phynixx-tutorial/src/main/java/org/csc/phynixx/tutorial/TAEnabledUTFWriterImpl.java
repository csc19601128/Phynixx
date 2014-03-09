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



    /**
     * Das RandomAccessFile, dass zum Schreiben u. Lesen geoeffnet wird.
     */
    private RandomAccessFile raf = null;
    private IXADataRecorder xaDataRecorder;

    private boolean autoCommit=false;

    private long rollbackPosition;

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

        if (raf != null) {
            // close Quietly
            try {
                // Schliessen der Daten-Datei
                raf.close();
            } catch (Exception e) {
            } finally {
                raf = null;
            }
        }
    }

    /**
     * zeigt an, ob die Instanz geschlossen ist
     *
     * @return true wenn die Datei geschlossen ist
     */
    public boolean isClosed() {
        return (this.raf == null);
    }


    @Override
    @RequiresTransaction
    public void resetContent() throws IOException {

        if (this.isClosed()) {
            throw new IllegalStateException("Writer is closed");
        }
        this.getRandomAccessFile().getChannel().truncate(0);

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
        this.getRandomAccessFile().writeUTF(value);

        return this;
    }

    @Override
    public List<String> readContent() throws IOException {

        List<String> content = new ArrayList<String>();

        // start from beginning
        this.getRandomAccessFile().getChannel().position(0l);

        long size = this.getRandomAccessFile().getChannel().size();

        while (position() < size) {
            String value = this.getRandomAccessFile().readUTF();
            content.add(value);
        }
        this.rollbackPosition = position();

        return content;
    }

    private void restoreSize(long size) throws IOException {
        this.getRandomAccessFile().getChannel().truncate(size);
        this.getRandomAccessFile().getChannel().position(size);
    }


    RandomAccessFile getRandomAccessFile() {
        if (this.isClosed()) {
            throw new IllegalStateException("RandomAccessFile is close");
        }

        return raf;
    }


    /**
     * bereitet die Writer zur Wiederverwendung vor
     */
    @Override
    public void reset() {
        try {
            this.close();
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }
    }


    @Override
    @RequiresTransaction
    public void open(File file) {
        try {
            this.raf = new RandomAccessFile(file, "rw");
            this.readContent();
            if (this.getXADataRecorder() != null) {
                LogRecordWriter logRecordWriter = new LogRecordWriter().writeUTF(file.getAbsolutePath()).writeLong(position());
                this.getXADataRecorder().writeRollbackData(logRecordWriter.toByteArray());
            }
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
        return this.raf.getChannel().position();
    }

    @Override
    public void rollback() {
        try {
            this.restoreSize(this.rollbackPosition);
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

                File file= new File(fileName);
                TAEnabledUTFWriterImpl.this.raf = new RandomAccessFile(file, "rw");
                TAEnabledUTFWriterImpl.this.restoreSize(filePosition);
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