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


import org.csc.phynixx.common.logger.IPhynixxLogger;
import org.csc.phynixx.common.logger.PhynixxLogManager;
import org.csc.phynixx.loggersystem.logger.IDataLogger;
import org.csc.phynixx.loggersystem.logger.IDataLoggerReplay;
import org.csc.phynixx.loggersystem.logrecord.XALogRecordType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * Logger uses a {@link org.csc.phynixx.loggersystem.logger.channellogger.TAEnabledRandomAccessFile}
 * to persist the data.
 * If the Logger is open, it holds a lock on
 *
 * This class is not thread safe . Use facades to protect instances
 */
public class FileChannelDataLogger implements IDataLogger {

    private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(FileChannelDataLogger.class);

    /**
     *
     */
    static class FileAccessor {

        private File cachedFile = null;
        private String absolutePathName = null;

        FileAccessor(File cachedFile) {
            this.cachedFile = cachedFile;
            this.absolutePathName = this.cachedFile.getAbsolutePath();
        }

        FileAccessor(String absolutePathName) {
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

        @Override
        public String toString() {
            return "FileAccessor {" +
                    "cachedFile=" + cachedFile +
                    ", absolutePathName='" + absolutePathName + '\'' +
                    '}';
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
     * Opens a logger on base of die given logfile. A RandomAccessFile is created .
     *
     * The accessMode of the logger is {@link org.csc.phynixx.loggersystem.logger.channellogger.AccessMode#WRITE}, so you can start writting date
     *
     *
     * @param logFileAccess
     * @throws IOException
     */
    @Deprecated
    private FileChannelDataLogger(File logFileAccess) throws IOException {
        this.logFileAccess = new FileAccessor(logFileAccess);
        associatedRandomAccessFile();
        this.reopen(AccessMode.APPEND);
    }

    /**
     * Opens a logger on base of die given logfile. A RandomAccessFile is created .
     *
     * The accessMode of the logger is {@link org.csc.phynixx.loggersystem.logger.channellogger.AccessMode#WRITE}, so you can start writting date
     *
     *
     * @param logFileAccess
     * @throws IOException
     */
    public FileChannelDataLogger(File logFileAccess,AccessMode aceessMode) throws IOException {
        this.logFileAccess = new FileAccessor(logFileAccess);
        associatedRandomAccessFile();
        this.reopen(aceessMode);
    }

    public AccessMode getAccessMode() {
        return this.accessMode;
    }

    private void maybeWritten() {
        if (this.randomAccess == null) {
            throw new IllegalStateException("Channel is not reopen.");
        }
        if (this.accessMode != AccessMode.APPEND && this.accessMode != AccessMode.WRITE) {
            throw new IllegalStateException("Channel can not be written.");
        }
    }

    private void maybeRead() {
        if (this.randomAccess == null) {
            throw new IllegalStateException("Channel is not reopen.");
        }
        if (this.accessMode == AccessMode.NONE) {
            throw new IllegalStateException("Channel can not be read.");
        }
    }

    /**
     * opens the logger with the specified ACCESS_MODE. If the logger isn't closed it is closed
     * @param accessMode
     * @throws IOException
     */
    @Override
    public void open(AccessMode accessMode) throws IOException {

        associatedRandomAccessFile();

        this.reopen(accessMode);
    }

    private void associatedRandomAccessFile() throws IOException {
        if(!this.isClosed()) {
           this.close();
        }
        RandomAccessFile raf = openRandomAccessFile(this.logFileAccess.getFile(), FILE_MODE);
        try {
            this.randomAccess = new TAEnabledRandomAccessFile(raf);
            LOG.error(Thread.currentThread() +" lock on "+this.logFileAccess +" succeeded");
        } catch(IOException e) {
            LOG.error(Thread.currentThread() +".lock on "+this.logFileAccess,e);
            throw e;
        } catch(IllegalStateException e) {
            LOG.error(Thread.currentThread()+ ".lock on "+this.logFileAccess,e);
            throw e;
        }
    }


    /**
     * reopens the datalogger. It is assumed that the logger is open.
     *
     * @param accessMode
     * @throws IOException
     * @throws java.lang.IllegalStateException logger isn't open
     */
    public void reopen(AccessMode accessMode) throws IOException {

        if( this.randomAccess==null) {
            associatedRandomAccessFile();
        }

        this.accessMode = accessMode;

        // write start sequences ...
        switch (accessMode) {
            case READ:
                maybeRead();
                // start reading from the fiorts position
                this.randomAccess.rewind();
                break;
            case WRITE:
                maybeWritten();
                this.randomAccess.reset();
                break;
            case APPEND:
                maybeWritten();
                this.randomAccess.forwardWind();
                break;
            default:
                throw new IllegalArgumentException("Invalid AccessMode " + accessMode);
        }


    }

    private RandomAccessFile openRandomAccessFile(File logFile, String fileMode) throws IOException {
        try {
            RandomAccessFile raf= new RandomAccessFile(logFile, fileMode);
            if( LOG.isInfoEnabled()) {
                LOG.info(Thread.currentThread()+" lock on "+logFile +" succeeded");
            }
            return raf;
        } catch(IOException e) {
            LOG.error(Thread.currentThread() +".release lock on "+logFile,e);
            throw e;
        } catch(IllegalStateException e) {
            LOG.error(Thread.currentThread()+ " release lock on "+logFile,e);
            throw e;
        }
    }


    private void reset() throws IOException {
        this.randomAccess.rewind();
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
        try {
            this.randomAccess.close();
            LOG.error(Thread.currentThread() +".release lock on "+this.logFileAccess+" succeeded");
        } catch(IOException e) {
            LOG.error(Thread.currentThread() +".release lock on "+this.logFileAccess,e);
            throw e;
        } catch(IllegalStateException e) {
            LOG.error(Thread.currentThread()+ " release lock on "+this.logFileAccess,e);
            throw e;

        } finally {
            this.logFileAccess.close();
            this.randomAccess = null;
        }
    }


    /**
     * the records a recovered from the format described in {@link #write(short, byte[][])}
     *
     * @param replay replayListener
     * @throws IOException
     */
    public void replay(IDataLoggerReplay replay) throws IOException {
        maybeRead();
        this.randomAccess.rewind();
        while (this.randomAccess.available() > (Integer.SIZE / Byte.SIZE) ) {

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
                    if(!this.logFileAccess.getFile().delete()) {
                    	 LOGGER.fatal("Failed deleting "+this.logFileAccess.getFile());
                    }
                } catch (Exception e) {
                    // just log the error -- if cleanup fails, no failure of the system
                    LOGGER.fatal(this.toString(), e);
                }
            }
        }
    }


    public String toString() {
        return "FileChannelDataLogger (" + this.logFileAccess.getAbsolutePathName() + ")";
    }


}
