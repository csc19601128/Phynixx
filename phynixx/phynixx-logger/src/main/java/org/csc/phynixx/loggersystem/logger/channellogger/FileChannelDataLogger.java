package org.csc.phynixx.loggersystem.logger.channellogger;

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


import org.csc.phynixx.logger.IPhynixxLogger;
import org.csc.phynixx.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.IDataLogger;
import org.csc.phynixx.loggersystem.logrecord.ILogRecordReplayListener;
import org.csc.phynixx.loggersystem.logrecord.XALogRecordType;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * Logger uses a {@link org.csc.phynixx.loggersystem.logger.channellogger.TAEnabledRandomAccessFile} to persist the data.
 * <p/>
 * This class is not thread safe . Use facades to protect instances
 */
public class FileChannelDataLogger implements IDataLogger {

    private static class FileAccessor {

        private File cachedFile = null;
        private String absolutePathName = null;

        private FileAccessor(File cachedFile) {
            this.cachedFile = cachedFile;
            this.absolutePathName = this.cachedFile.getAbsolutePath();
        }

        private FileAccessor(String absolutePathName) {
            this.absolutePathName = absolutePathName;
            instanciateFile();
        }

        private void instanciateFile() {
            if (cachedFile != null) {
                return;
            }
            this.cachedFile = new File(this.absolutePathName);
            if (!cachedFile.exists()) {
                throw new IllegalStateException("File " + this.absolutePathName + " does not exist");
            }
        }

        String getAbsolutePathName() {
            return absolutePathName;
        }

        File getFile() {
            instanciateFile();
            return cachedFile;
        }

        void close() {
            this.cachedFile = null;
        }
    }

    /**
     * Open for reading and writing, as with "rw", and also require that every update to the file's content or metadata be written synchronously to the underlying storage device.
     */
    private static String FILE_MODE = "rw";

    private static final IPhynixxLogger LOGGER = PhynixxLogManager.getLogger(FileChannelDataLogger.class);

    private TAEnabledRandomAccessFile randomAccess = null;
    private FileAccessor logFileAccess = null;

    private AccessMode accessMode = AccessMode.NONE;

    /**
     * Oeffnet die Datei, legt sie an, falls erforderlich.
     * Schreibt 4 als Dateigroesse an den Anfang wenn die Datei neu angelegt wird.
     *
     * @param logFileAccess
     * @throws IOException
     */
    public FileChannelDataLogger(File logFileAccess) throws IOException {
        this.logFileAccess = new FileAccessor(logFileAccess);
    }

    public AccessMode getAccessMode() {
        return this.accessMode;
    }

    private void maybeWritten() {
        if (this.randomAccess == null) {
            throw new IllegalStateException("Channel is not open.");
        }
        if (this.accessMode != AccessMode.APPEND && this.accessMode != AccessMode.WRITE) {
            throw new IllegalStateException("Channel can not be written.");
        }
    }

    private void maybeRead() {
        if (this.randomAccess == null) {
            throw new IllegalStateException("Channel is not open.");
        }
        if (this.accessMode != AccessMode.READ) {
            throw new IllegalStateException("Channel can not be written.");
        }
    }


    public void open(AccessMode accessMode) throws IOException {
        this.close();
        RandomAccessFile raf = new RandomAccessFile(logFileAccess.getFile(), FILE_MODE);
        this.randomAccess = new TAEnabledRandomAccessFile(raf);

        // write start sequences ...
        switch (accessMode) {
            case READ:
                // wird auf erste Position gesetzt
                this.randomAccess.position(0L);
                break;
            case WRITE:
                //
                this.randomAccess.position(0L);
                this.randomAccess.commit();
                break;
            case APPEND:
                this.randomAccess.position(this.randomAccess.getCommittedSize());
                break;
            default:
                throw new IllegalArgumentException("Invalid AccessMode " + accessMode);
        }

        this.accessMode = accessMode;

    }


    private void reset() throws IOException {
        this.randomAccess.position(0);
        this.randomAccess.commit();
    }


    public void write(short type, byte[] record) throws IOException {
        maybeWritten();
        this.randomAccess.writeInt(record.length);
        this.randomAccess.write(record);
        this.randomAccess.commit();
    }

    /**
     * <pre>
     *    +- # records
     *    +- type
     *    +-- length of records[0]
     *    +-- data of records[0]
     *    +-- length of records[1]
     *    +-- data of records[1]
     *    . . .
     *
     * </pre>
     * <p/>
     * this format ensures that the record could be recovered
     *
     * @param type    a record type defined in LogRecordType.
     * @param records
     * @return the file position before writing the data
     * @throws IOException
     */
    public long write(short type, byte[][] records) throws IOException {
        maybeWritten();
        long referenceKey = this.randomAccess.position();
        // Number of records ....
        this.randomAccess.writeInt(records.length);

        // write the type of the recods ...
        this.randomAccess.writeShort(type);

        for (int i = 0; i < records.length; i++) {
            if (records[i] == null) {
                throw new IllegalArgumentException("Records[" + i + "]==null");
            }
            this.randomAccess.writeInt(records[i].length);
            this.randomAccess.write(records[i]);
        }
        this.randomAccess.commit();
        return referenceKey;
    }


    public void close() throws IOException {
        if (this.randomAccess == null) {
            return;
        }
        this.logFileAccess.close();
        this.randomAccess.close();
        this.randomAccess = null;
    }


    /**
     * the records a recovered from the format described in {@link #write(short, byte[][])}
     *
     * @param replay replayListener
     * @throws IOException
     */
    public void replay(ILogRecordReplayListener replay) throws IOException {
        maybeRead();
        this.randomAccess.position(0L);
        while (this.randomAccess.available() > TAEnabledRandomAccessFile.INT_BYTES) {

            int length = this.randomAccess.readInt();
            short type = this.randomAccess.readShort();
            XALogRecordType recordType = XALogRecordType.resolve(type);
            byte[][] data = new byte[length][];
            for (int i = 0; i < length; i++) {
                int recordSize = this.randomAccess.readInt();
                data[i] = this.randomAccess.read(recordSize);
            }
            replay.onRecord(recordType, data);
        }

    }

    public boolean isClosed() {
        if (this.randomAccess == null) {
            return true;
        }
        return randomAccess.isClose();
    }


    @Override
    public void destroy() throws IOException {
        try {
            this.close();
        } finally {
            if (this.logFileAccess != null) {
                try {
                    this.logFileAccess.getFile().delete();
                } catch (Exception e) {
                    // juts log the error -- if cleanup fials, no fialure ofv the system
                    LOGGER.fatal(this, e);
                }
            }
        }
    }


    public String toString() {
        return "FileChannelDataLogger (" + this.logFileAccess.getAbsolutePathName() + ")";
    }


}
