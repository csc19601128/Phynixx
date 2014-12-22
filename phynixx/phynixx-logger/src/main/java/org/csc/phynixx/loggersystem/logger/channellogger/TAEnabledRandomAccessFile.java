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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

/**
 *
 * A TAEnabledRandomAccessFile provides random access to the file's content and let you append data to the current.
 * It provides a simple but efficient atomic write.
 * The first long of the file contains the committed size. Any content beyond the file pointer is ignored.
 * To write bytes needs to operations. First these bytes are appended to the file.
 * What is the second operation is to update the new commit position.
 *
 * The committed size is the size of the written and committed data.
 *
 * The visible size of the file contains the written data. It starts from the end of the header data and has the size of {@link #getCommittedSize()}
 * Data beyond this range cannot be read and is not available.
 *
 * <pre>
 *   |--------|----................---------|------
 *   0    start of                  committed size
 *       visible data                + header size
 *
 * </pre>
 *
 *
 * @see java.nio.channels.FileChannel
 * @see java.io.RandomAccessFile
 * @see java.nio.channels.FileLock
 *
 */
class TAEnabledRandomAccessFile {


    /**
     * Header size. As the header contains the commited size (of type long) the header size is 8
     */
    public static final int HEADER_LENGTH = (Long.SIZE / Byte.SIZE);
    /**
     * Groesse eines Bytes
     */

    public static final int BYTE_BYTES = 1;

    /**
     * max. Inhalt eines Byte als int *
     */
    public static final int MAX_BYTE_VALUE = (int) Byte.MAX_VALUE;

    private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(TAEnabledRandomAccessFile.class);
    /**
     * Das RandomAccessFile, dass zum Schreiben u. Lesen geoeffnet wird.
     */
    private RandomAccessFile raf = null;


    private FileLock fileLock = null;


    /**
     * redundant committed size to achieve perform range checks
     */
    private long committedSize=0;


    /**
     * Initialisierungs-Methode
     *
     * @param raf - RandomAccessFile
     * @throws IOException
     */
    TAEnabledRandomAccessFile(RandomAccessFile raf) throws IOException {
        this.raf = raf;
        fileLock = acquireFileLock(raf);
        this.restoreCommittedSize();
        check();
    }

    private FileLock acquireFileLock(RandomAccessFile raf) throws IOException {
        return raf.getChannel().lock(0,HEADER_LENGTH, false);
    }

    /**
     * liefert die Groesse des Headerbereiches
     *
     * @return Groesse des Header
     */
    public static int getHeaderLength() {
        return HEADER_LENGTH;
    }

    /**
     * Gibt das RandomAccessFile zurueck
     *
     * @return RandomAccessFile
     */
    RandomAccessFile getRandomAccessFile() {
        check();
        return this.raf;
    }

    /**
     * ueberpueft die Gueltigkeit eine Dateiposition bzgl. des Nutzbereichs
     * wird in position gerufen.
     *
     * @param pos Position die auf Gueltigkeit bzgl. des Nutzbereichs
     *            ueberprueft werden soll
     * @throws IllegalArgumentException position ist nicht zuleassig
     */
    private void checkPosition(long pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("Uebergebene Position (=" + pos + ") darf nicht kleiner als 0 sein");
        }
    }


    /**
     * @return byte available starting from the current position to the visible end of the file (==committed size)
     * @throws IOException
     */
    public long available() throws IOException {
        check();
        return this.getCommittedSize() - this.position();
    }


    /**
     * @return current position starting from the end of the header data.
     * @throws java.io.IOException , IO Error
     * @see
     */
    long position() throws IOException {
        check();
        return this.raf.getFilePointer() - getHeaderLength();
    }

    private void check()  {
        if (isClose()) {
            throw new IllegalStateException("TAEnabledRandomAccessFile is closed");
        }

        if( this.fileLock==null || !fileLock.isValid()) {
            throw new IllegalStateException("Filelock is not valid");
        }
    }


    /**
     * Setzt Bereich der Nutzdaten auf die angegebenen Position.
     * Evtl. HeaderBrereiche werden ignoriert
     *
     * @param newPosition Position
     * @throws java.io.IOException IO Error
     * @author Phynixx
     */
    private void position(long newPosition) throws IOException {

        check();
        checkPosition(newPosition);
        this.getRandomAccessFile().seek(newPosition + getHeaderLength());
    }

    /**
     * Schliesst die Datei und den FileChannel
     *
     * @throws java.io.IOException , wenn das Schliessen schief geht.
     */
    public void close() throws IOException {
        if (raf != null) {
            // gibt Lock auf datei frei
            try {
                if (this.fileLock != null) {
                    if( fileLock.isValid()) {
                     this.fileLock.release();
                    } else {
                        LOG.error("Filelock not valid");
                    }
                } else {
                    LOG.error("Kein Filelock gesetzt");
                }
            } finally {
                // Schliessen der Daten-Datei
                this.fileLock = null;
                raf.close();
                raf = null;
            }
        }
    }

    /**
     * zeigt an, ob die Instanz geschlossen ist
     *
     * @return true wenn die Datei geschlossen ist
     */
    public boolean isClose() {
        return (this.raf == null);
    }

    /**
     *
     *
     * Gibt die Groesse des Comitteten Bereichs in Byte zurueck. Da am Anfang
     * der Datei die Dateigroesse geschrieben wird, ist die Mindest-Groesse 4.
     * Bei 4 Byte sind also noch keine Daten in die Datei geschrieben worden.
     *
     * @return commited size (bytes)
     * @throws java.io.IOException IO-Error
     */
    public long getCommittedSize() throws IOException {
        check();

        return this.committedSize;

    }


    /**
     *
     * reads the next INTEGER starting form the current file position.
     * If there are not enough bytes available (from the current position to the visible end of the file) to read the data an exception is thrown.
     *
     * @return read value
     * @throws java.io.IOException IO Error
     * @author Schmidt-Casdorff
     */
    public int readInt() throws IOException {
        check();
        checkRead(4);
        return raf.readInt();
    }

    /**
     *
     * reads the next SHORT starting form the current file position.
     * If there are not enough bytes available (from the current position to the visible end of the file) to read the data an exception is thrown.
     *
     * @return read value
     * @throws java.io.IOException IO Error
     * @author Schmidt-Casdorff
     */
    public short readShort() throws IOException {
        check();
        checkRead(2);
        return raf.readShort();
    }

    /**
     *
     * reads the next LONG starting form the current file position.
     * If there are not enough bytes available (from the current position to the visible end of the file) to read the data an exception is thrown.
     *
     * @return read value
     * @throws java.io.IOException IO Error
     * @author Schmidt-Casdorff
     */
    public long readLong() throws IOException {
        check();
        checkRead(8);
        return raf.readLong();
    }

    /**
     * reads the next length bytes starting form the current file position.
     * If there are not enough bytes available (from the current position to the visible end of the file) to read the data an exception is thrown.
     *
     * overflow (position is beyond committed data),  is
     * kopiert den Bereich zwischen startPosition und enedPosition der Datei in den ByteBuffer.
     * Es wird die Read-methode des RandomAccessFiles genommen, da sich
     * herausgestellt hat, dass die Methode im Batchbetrieb etwas schneller
     * ist als die Channel-methode.
     * Ee werden bytes gelesen fuer die gilt: <br>
     * startPosition <= b < endPosition
     *
     * @param length umber of bytes to be read
     * @return content
     * @throws java.io.IOException IO Error
     */
    public byte[] read(int length) throws IOException {
        check();

        checkRead(length);

        if (length >= Integer.MAX_VALUE) {
            throw new IOException("Length of read area may not exceed " + Integer.MAX_VALUE);
        }

        if (this.position()+length > this.getCommittedSize()) {
            throw new IOException("Length of read area may not exceed the committed size of " + this.getCommittedSize());
        }

        int intLength = Long.valueOf(length).intValue();

        byte[] buffer = new byte[intLength];

        if (isClose()) {
            throw new IOException("TAEnabledChanneList ist closed");
        }

        long retVal = this.getRandomAccessFile().read(buffer, 0, intLength);

        if (retVal < 0) {
            throw new IOException("Channel cannot read " + (position() + intLength) );
        }
        return buffer;
    }

    private void checkRead(int length) throws IOException {
        if( this.available() < length) {
            throw new IOException("Cannot read "+length +" byte. Starting from current position "+position()+" there are only "+this.available()+" bytes available");
        }
    }


    /**
     * appends a byte[] to the file, but doesn't commit.
     *
     * In order to re-read the content it is recommended to store the size of the byte[] before writing the byte[]. This value can be used as the value of parameter length.
     *
     * @param buffer value to be appended
     * @throws java.io.IOException IO Error
     *
     * @see #read(int)
     */
    public void write(byte[] buffer) throws IOException {
        check();

        //assert buffer != null : "Der Buffer ist null";
        if (isClose()) {
            throw new IOException("TAEnabledChanneList is closed");
        }
        long currentPosition = this.position();
        this.raf.write(buffer);
        // this.incPosition(buffer.length);

        assert this.position() - currentPosition == buffer.length : "Expected new position : " + currentPosition + buffer.length + " actual position " + this.position();


    }

    /**
     * appends a SHORT to the file, but doesn't commit
     *
     * @param value value to be appended
     * @throws java.io.IOException IO Error
     */
    public void writeShort(short value) throws IOException {
        check();
        // increments the current position
        getRandomAccessFile().writeShort(value);
    }

    /**
     * appends an INT to the file, but doesn't commit
     *
     * @param value value to be appended
     * @throws java.io.IOException IO Error
     */
    public void writeInt(int value) throws IOException {
        check();
        // increments the current position
        getRandomAccessFile().writeInt(value);

    }


    /**
     * appends a LONG to the file, but doesn't commit
     *
     * @param value value to be appended
     * @throws java.io.IOException IO Error
     */
    public void writeLong(long value) throws IOException {
        check();
        // increments the current position
        getRandomAccessFile().writeLong(value);

    }

    /**
     * sets the current position to the start of the data. (position()==0)
     * @throws IOException
     */
    public void rewind() throws IOException {
        check();
        this.position(0);

    }

    /**
     * sets the current position to end of the visible data. (position()==getCommittedSize())
     * @throws IOException
     */
    public void forwardWind() throws IOException {
        check();
        this.position(this.getCommittedSize());

    }



    /**
     * updates the committed size with 0. All data is truncated
     * @throws IOException
     */
    public void reset() throws IOException {
        check();

        this.position(0);

        this.commit();

        // forget about the rest
        this.getRandomAccessFile().getChannel().truncate(TAEnabledRandomAccessFile.HEADER_LENGTH);

    }


    /**
     *
     * updates the first 4 bytes of the file with the current position of the randomAccessFile
     *
     * @throws java.io.IOException Error updating the file
     */
    public void commit() throws IOException {
        check();
        long currentPosition = this.position();

        // go to the header in order to be prepared to update the committed size
        this.getRandomAccessFile().seek(0);

        // update the commiited data with the current position
        getRandomAccessFile().writeLong(currentPosition);


        // reset the file position to the original value
        this.position(currentPosition);

        // write through
        this.getRandomAccessFile().getChannel().force(false);

        this.committedSize= currentPosition;

    }

    /**
     * restores the committed size  recored in the first 4 bytes of the file.     *
     * the randomaccessFile is positioned to this position.
     *
     * The content beyond this new position is not destroyed but will be be overwritten,
     *
     * @throws java.io.IOException IO-Error
     * @see
     */
    private void restoreCommittedSize() throws IOException {

        check();
        long privCommittedSize=-1;
        this.getRandomAccessFile().seek(0);
        if (this.raf.length() < HEADER_LENGTH) {
            this.getRandomAccessFile().writeLong(0);
            privCommittedSize=0;
        } else {
            privCommittedSize = this.getRandomAccessFile().readLong();
        }
        this.position(privCommittedSize);
        this.committedSize=privCommittedSize;
    }




}
