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
 * Basisklasse zur Verwaltung von Filezugriffen.
 * <p/>
 * A RandomAccessFile provides random access to the file's content.
 */
class TAEnabledRandomAccessFile {

    /**
     * Groesse in Byte fuer Datentyp int
     */
    public static final int INT_BYTES = 4;
    /**
     * Groesse in Byte fuer Datentyp long
     */
    public static final int HEADER_LENGTH = 8;
    /**
     * Groesse eines Bytes
     */

    public static final int BYTE_BYTES = 1;
    /**
     * max. Inhalt eines Byte als int *
     */
    public static final int MAX_BYTE_VALUE = (int) Byte.MAX_VALUE;
    private static final IPhynixxLogger LOG = PhynixxLogManager.getLogger(TAEnabledRandomAccessFile.class);
    private static final long SHORT_BYTES = 2;
    /**
     * Das RandomAccessFile, dass zum Schreiben u. Lesen geoeffnet wird.
     */
    private RandomAccessFile raf = null;


    private FileLock fileLock = null;


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
     * @return byte availbale staing from the current position
     * @throws IOException
     */
    public long available() throws IOException {
        check();
        return this.getCommittedSize() - this.position();
    }


    /**
     * Liefert die aktuelle Position im Bereich der Nutzdaten.
     * Evtl. HeaderDaten werden ignoriert
     *
     * @return Die aktuelle Position im channel
     * @throws java.io.IOException , IO-Fehler
     * @see
     */
    long position() throws IOException {
        check();
        return this.raf.getFilePointer() - getHeaderLength();
    }

    private void check()  {
        if (isClose()) {
            throw new IllegalStateException("TAEnabledRandomAccessFile ist geschlossen");
        }

        if( this.fileLock==null || !fileLock.isValid()) {
            throw new IllegalStateException("Filelock ist nicht valide");
        }
    }


    /**
     * Setzt Bereich der Nutzdaten auf die angegebenen Position.
     * Evtl. HeaderBrereiche werden ignoriert
     *
     * @param newPosition Position
     * @throws java.io.IOException IO-Fehler
     * @author Phynixx
     */
    public void position(long newPosition) throws IOException {

        check();
        checkPosition(newPosition);
        this.getRandomAccessFile().seek(newPosition + getHeaderLength());
    }

    /**
     * Setzt Bereich der Nutzdaten auf die angegebenen Position.
     * Evtl. HeaderBrereiche werden ignoriert
     *
     * @throws java.io.IOException IO-Fehler
     * @author Phynixx
     */
    public void incPosition(long length) throws IOException {
        check();
        long newPosition = this.position() + length;
        this.position(newPosition);
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
     * Gibt die Groesse des Comitteten Bereichs in Byte zurueck. Da am Anfang
     * der Datei die Dateigroesse geschrieben wird, ist die Mindest-Groesse 4.
     * Bei 4 Byte sind also noch keine Daten in die Datei geschrieben worden.
     *
     * @return Die Committete Groesse in Bytes
     * @throws java.io.IOException IO-Fehler
     */
    public long getCommittedSize() throws IOException {
        check();

        long cp = this.position();

        this.getRandomAccessFile().seek(0);
        long committedSize = this.getRandomAccessFile().readLong();
        this.position(cp);

        return committedSize;
    }


    /**
     * Liest einen int-Wert aus der Datei ( wird fuer restart benoetigt )
     *
     * @return int, Die aus der Datei gelesene Zahl
     * @throws java.io.IOException IO-Fehler
     * @author Schmidt-Casdorff
     */
    public int readInt() throws IOException {
        check();
        return raf.readInt();
    }

    /**
     * Liest einen int-Wert aus der Datei ( wird fuer restart benoetigt )
     *
     * @return int, Die aus der Datei gelesene Zahl
     * @throws java.io.IOException IO-Fehler
     * @author Schmidt-Casdorff
     */
    public short readShort() throws IOException {
        check();
        return raf.readShort();
    }


    /**
     * kopiert den vollstaendigen Inhalt der Datei in einen ByteBuffer
     *
     * @return ByteBuffer mit dem Inhalt der Datei
     */
    public byte[] readContent() throws IOException {
        check();
        long comSize = getCommittedSize();
        //Wenn die Datei leer ist, einen ByteBuffer der Groesse 0 Bytes zurueckgeben
        if (comSize == 0) {
            return new byte[]{};
        } else {
            this.position(0);
            return read(comSize);
        }
    }


    /**
     * kopiert den Bereich zwischen startPosition und enedPosition der Datei in den ByteBuffer.
     * Es wird die Read-methode des RandomAccessFiles genommen, da sich
     * herausgestellt hat, dass die Methode im Batchbetrieb etwas schneller
     * ist als die Channel-methode.
     * Ee werden bytes gelesen fuer die gilt: <br>
     * startPosition <= b < endPosition
     *
     * @param length Anzahl der zu lesenden Zeichen
     *               werden soll
     * @return ByteBuffer mit dem Inhalt der Datei zwischen startPosition und enedPosition
     * @throws java.io.IOException IO-Fehler
     */
    public byte[] read(long length) throws IOException {
        check();

        if (length >= new Long(Integer.MAX_VALUE).longValue()) {
            throw new IllegalArgumentException("Length of read area may not exeed " + Integer.MAX_VALUE);
        }

        int intLength = new Long(length).intValue();

        byte[] buffer = new byte[intLength];

        if (isClose()) {
            throw new IOException("TAEnabledChanneList geschlossen");
        }
        // ueberpruefung der Eingabeargumente
        if (buffer.length < intLength) {
            throw new IllegalArgumentException("Uebergebener Buffer zu klein fuer angeforderten Lesezugriff : " + intLength);
        }

        long retVal = this.getRandomAccessFile().read(buffer, 0, intLength);

        if (retVal < 0) {
            throw new IOException("Channel kann nicht bis zur Position " + (position() + intLength) + " lesen");
        }
        return buffer;
    }

    /**
     * Schreibt den Inhalt des uebergebenen ByteBuffers in die Datei. Achtung:
     * ruft zuerst flip auf. Setzt nach dem Schreiben die Position auf 0 und
     * das Limit auf die Kapazitaet des ByteBuffers (zur weiteren Verwendung)
     *
     * @param buffer , der in die Datei zu schreiben ist.
     * @throws java.io.IOException , falls irgendetwas beim Schreiben schief geht
     */
    public void write(byte[] buffer) throws IOException {
        check();

        //assert buffer != null : "Der Buffer ist null";
        if (isClose()) {
            throw new IOException("TAEnabledChanneList geschlossen");
        }
        long currentPosition = this.position();
        this.raf.write(buffer);
        // this.incPosition(buffer.length);

        assert this.position() - currentPosition == buffer.length : "Expected new position : " + currentPosition + buffer.length + " actual position " + this.position();


    }

    /**
     * Schreibt einen int-Wert in den Channel
     *
     * @param value ,Der zu schreibende int-Wert, an der aktuellen Position.
     * @throws java.io.IOException IO-Fehler
     */
    public void writeShort(short value) throws IOException {
        check();
        long currentPosition = this.position();


        // increments the current position
        getRandomAccessFile().writeShort(value);
        // this.incPosition(INT_BYTES);

        assert this.position() - currentPosition == SHORT_BYTES : "Expected new position : " + currentPosition + INT_BYTES + " actual position " + this.position();

    }

    /**
     * Schreibt einen int-Wert in den Channel
     *
     * @param value ,Der zu schreibende int-Wert, an der aktuellen Position.
     * @throws java.io.IOException IO-Fehler
     */
    public void writeInt(int value) throws IOException {
        check();
        long currentPosition = this.position();


        // increments the current position
        getRandomAccessFile().writeInt(value);
        // this.incPosition(INT_BYTES);

        assert this.position() - currentPosition == INT_BYTES : "Expected new position : " + currentPosition + INT_BYTES + " actual position " + this.position();

    }


    /**
     * Schreibt einen int-Wert in den Channel
     *
     * @param value ,Der zu schreibende int-Wert, an der aktuellen Position.
     * @throws java.io.IOException IO-Fehler
     */
    public void writeLong(long value) throws IOException {
        check();
        long currentPosition = this.position();

        // increments the current position
        getRandomAccessFile().writeLong(value);

        // this.incPosition(HEADER_LENGTH);

        assert this.position() - currentPosition == HEADER_LENGTH : "Expected new position : " + currentPosition + HEADER_LENGTH + " actual position " + this.position();

    }


    /**
     * Schreibt die Position an den Anfang der Datei
     * Die Position entspricht der gueltigen Dateigroesse.
     *
     * @throws java.io.IOException , Fehler beim Schreiben der Datei.
     */
    public void commit() throws IOException {
        check();
        //Hole die Position der Nutzdaten
        long currentPosition = this.position();

        // Positioniere an den Anfang ders headers
        this.getRandomAccessFile().seek(0);

        getRandomAccessFile().writeLong(currentPosition);
        this.position(currentPosition);

        // write through
        this.raf.getChannel().force(false);

        long comSize = getCommittedSize();
        if( comSize!=currentPosition) {
            throw new IllegalStateException("commited size has unexpected value");
        }
    }

    /**
     * positioniert die Schreibposition auf die Position des letzten committs
     * Wenn die aktuelle Position bereits innerhalb des committeten Bereichs
     * liegt, bleibt Position unveraendert
     *
     * @throws java.io.IOException IO-Fehler
     */
    public void rollback() throws IOException {
        check();
        this.getRandomAccessFile().seek(0);
        // Hole bestaetigte Groesse der Nutzdaten
        long newPosition = getRandomAccessFile().readLong();
        this.position(newPosition);
    }

    /**
     * Gibt zurueck, ob Daten Comittet wurden. Daten wurden dann commitet,
     * wenn die comittete-Groesse groesser 4 Byte ist, da in den 1. vier Byte
     * die Dateigroesse steht.
     *
     * @return true, wenn es Daten gibt, die comittet sind, sonst false.
     * @throws java.io.IOException IO-Fehler
     */
    public boolean hasCommittedData() throws IOException {
        return (getCommittedSize() > HEADER_LENGTH);
    }


    /**
     * Stellt die Comittet Dateigroesse wieder her. Die Comittete Dateigroesse
     * steht jeweils am Anfang der Datei. Positioniert auf die neu gesetzte
     * fileSize.
     *
     * @throws java.io.IOException IO-Fehler
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
    }


}
