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
import org.csc.phynixx.loggersystem.logger.ILogger;
import org.csc.phynixx.loggersystem.messages.ILogRecordReplayListener;
import org.csc.phynixx.loggersystem.messages.XALogRecordType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * Logger uses a {@link org.csc.phynixx.loggersystem.logger.channellogger.TAEnabledRandomAccessFile} to persist the data.
 * <p/>
 * This class is not thread safe . Use facades to protect instances
 */
public class FileChannelLogger implements ILogger {

    /**
     * Open for reading and writing, as with "rw", and also require that every update to the file's content or metadata be written synchronously to the underlying storage device.
     */
    private static String FILE_MODE = "rws";

    private static final IPhynixxLogger LOGGER = PhynixxLogManager.getLogger(FileChannelLogger.class);

    private TAEnabledRandomAccessFile randomAccess = null;
    private File logFile = null;
    private String loggerName;

    /**
     * Oeffnet die Datei, legt sie an, falls erforderlich.
     * Schreibt 4 als Dateigroesse an den Anfang wenn die Datei neu angelegt wird.
     *
     * @param loggerName, der Dateiname
     * @param directory   , Das Verzeichnis
     * @throws java.io.FileNotFoundException Wenn die Datei nicht gefunden wird
     * @throws java.io.IOException           , falls ein IO-Fehler auftritt
     */
    public FileChannelLogger(String loggerName, File directory) throws IOException, FileNotFoundException {
        this.logFile = this.provideFile(loggerName, directory);

        this.loggerName = loggerName;

        // System.out.println("Logger create ::"+logFile.getAbsolutePath());
    }


    public String getLoggerName() {
        return loggerName;
    }


    public void open() throws IOException {
        this.close();
        RandomAccessFile raf = new RandomAccessFile(logFile, FILE_MODE);
        this.randomAccess = new TAEnabledRandomAccessFile(raf);

    }


    public void write(short type, byte[] record) throws IOException {
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
        this.randomAccess.close();
        this.logFile = null;
        this.randomAccess = null;
    }


    /**
     * the records a recovered from the format described in {@link #write(short, byte[][])}
     *
     * @param replay replayListener
     * @throws IOException
     */
    public void replay(ILogRecordReplayListener replay) throws IOException {
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
            if (this.logFile != null) {
                try {
                    this.logFile.delete();
                } catch (Exception e) {
                    // juts log the error -- if cleanup fials, no fialure ofv the system
                    LOGGER.fatal(this, e);
                }
            }
        }
    }

    private File provideFile(String fileName, File directory) throws IOException {

        String fileCompleteName = directory + File.separator + fileName + "_1.log";

        File file = new File(directory, fileName + ".log");

        // Falls existent, so ist nichts zu tun.
        if (file.exists()) {
            return file;
        }

        if (!directory.exists()) {
            directory.mkdirs();
        }

        file.createNewFile();

        return file;

    }

    public String toString() {
        return "FileChannelLogger (" + this.loggerName + "_1.log)";
    }


}
