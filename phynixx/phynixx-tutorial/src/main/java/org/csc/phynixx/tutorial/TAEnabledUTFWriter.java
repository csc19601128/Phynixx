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
class TAEnabledUTFWriter {


    public static TAEnabledUTFWriter createWriter(File file) throws IOException {
        TAEnabledUTFWriter writer = new TAEnabledUTFWriter(file);
        writer.resetContent();
        return writer;
    }


    public static TAEnabledUTFWriter recoverWriter(File file) throws IOException {
        TAEnabledUTFWriter writer = new TAEnabledUTFWriter(file);
        writer.recover();
        return writer;
    }


    private List<String> content = new ArrayList<String>();

    /**
     * Das RandomAccessFile, dass zum Schreiben u. Lesen geoeffnet wird.
     */
    private RandomAccessFile raf = null;


    /**
     * Initialisierungs-Methode
     *
     * @param file -
     * @throws java.io.IOException
     */
    private TAEnabledUTFWriter(File file) throws IOException {
        this.raf = new RandomAccessFile(file, "rw");
    }


    /**
     * Schliesst die Datei und den FileChannel
     *
     * @throws java.io.IOException , wenn das Schliessen schief geht.
     */
    public void close() throws IOException {
        if (raf != null) {
            raf.getChannel().lock().release();

            // Schliessen der Daten-Datei
            raf.close();

            raf = null;
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


    public void resetContent() throws IOException {
        this.content.clear();
        this.getRandomAccessFile().getChannel().truncate(0);
    }

    public TAEnabledUTFWriter write(String value) throws IOException {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        this.getRandomAccessFile().writeUTF(value);
        this.content.add(value);

        return this;
    }

    public void recover() throws IOException {

        List<String> content = new ArrayList<String>();

        // start from beginning
        this.getRandomAccessFile().getChannel().position(0l);

        long size = this.getRandomAccessFile().getChannel().size();

        while (this.getRandomAccessFile().getChannel().position() < size) {
            String value = this.getRandomAccessFile().readUTF();
            content.add(value);
        }

        this.content = content;
    }

    void restoreSize(long size) throws IOException {

        this.getRandomAccessFile().getChannel().truncate(size);
        this.getRandomAccessFile().getChannel().position(size);
    }


    RandomAccessFile getRandomAccessFile() {
        if (this.isClose()) {
            throw new IllegalStateException("RandaomAccessFile is close");
        }

        return raf;
    }

    public List<String> getContent() {
        return content;
    }
}