package org.csc.phynixx.loggersystem.channellogger;

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


import org.csc.phynixx.loggersystem.ILogRecordReplayListener;
import org.csc.phynixx.loggersystem.ILogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * Klasse zum Schreiben von byte Stoemen in RandomAccessFiles.
 * Die Schreibvorgaenge sind transaktionsgesichert, d.h. erst nach
 * Aufruf von commit werden die geschriebenen Daten in der Datei als gueltige
 * Inhalte bei einem Offnen der Datei akzeptiert.
 *
 * @version $Revision: 1.2 $
 * @project IS
 * @copyright Deutsche Post AG, CCE
 * @modified $Date: 2006/11/21 16:08:56 $, $Author: pt6bcc $
 * @author Ivens
 * @see            <{RandomAccessWriter}>
 */
public class FileChannelLogger implements ILogger {

    private TAEnabledChannel randomAccess = null;
    private File logFile = null;
    private String loggerName;

    /**
     * �ffnet die Datei, legt sie an, falls erforderlich.
     * Schreibt 4 als Dateigroesse an den Anfang wenn die Datei neu angelegt wird.
     *
     * @param loggerName ,  der Dateiname
     * @param directory  , Das Verzeichnis
     * @param reuseFile  : Flag, dass angibt, ob die Datei bereits existiert und
     *                   damit nur Daten angehaengt werden sollen. True, wenn die Datei bereits
     *                   existiert, sonst false.
     * @throws java.io.FileNotFoundException Wenn die Datei nicht gefunden wird
     * @throws java.io.IOException           , falls ein IO-Fehler auftritt
     * @author Ivens
     */
    public FileChannelLogger(String loggerName, File directory)
            throws IOException, FileNotFoundException {
        this.logFile = this.provideFile(loggerName, directory);

        this.loggerName = loggerName;

        // System.out.println("Logger create ::"+logFile.getAbsolutePath());
    }


    public String getLoggerName() {
        return loggerName;
    }


    public synchronized void open() throws IOException {
        this.close();
        RandomAccessFile raf = new RandomAccessFile(logFile, "rw");
        this.randomAccess = new TAEnabledChannel(raf);

    }


    public synchronized void write(short type, byte[] record) throws IOException {
        this.randomAccess.writeInt(record.length);
        this.randomAccess.write(record);
        this.randomAccess.commit();
    }

    public synchronized long write(short type, byte[][] records) throws IOException {
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


    public synchronized void close() throws IOException {
        if (this.randomAccess == null) {
            return;
        }
        this.randomAccess.close();
        this.logFile = null;
        this.randomAccess = null;
    }


    public synchronized void replay(ILogRecordReplayListener replay) throws IOException {
        this.randomAccess.position(0L);
        while (this.randomAccess.available() > TAEnabledChannel.INT_BYTES) {

            int length = this.randomAccess.readInt();
            short type = this.randomAccess.readShort();
            byte[][] data = new byte[length][];
            for (int i = 0; i < length; i++) {
                int recordSize = this.randomAccess.readInt();
                data[i] = this.randomAccess.read(recordSize);
            }
            replay.onRecord(type, data);
        }

    }


    public synchronized boolean isClosed() {
        if (this.randomAccess == null) {
            return true;
        }
        return randomAccess.isClose();
    }

    private File provideFile(String fileName, File directory) throws IOException {

        String fileCompleteName = directory + File.separator + fileName + "_1.log";

        File file = new File(directory, fileName + "_1.log");

        // Falls existent, so ist nichts zu tun.
        if (file.exists()) {
            return file;
        }

        // Falls existent, so ist nichts zu tun.
        if (file.exists()) {
            throw new IOException(" '" + file +
                    "' soll erzeugt werden, existiert aber bereits");
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
