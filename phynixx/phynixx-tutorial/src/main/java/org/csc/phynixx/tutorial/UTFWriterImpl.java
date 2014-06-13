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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Basisklasse zur Verwaltung von Filezugriffen.
 * <p/>
 * A RandomAccessFile provides random access to the file's content.
 */
public class UTFWriterImpl implements UTFWriter {


    Lock lock= new ReentrantLock();

    private Condition unlocked= lock.newCondition();

    private String lockToken = null;

    /**
     * Das RandomAccessFile, dass zum Schreiben u. Lesen geoeffnet wird.
     */
    private RandomAccessFile raf = null;

    private File file;

    public UTFWriterImpl(File file) {
        this.open(file);
    }

    /**
     *
     * @return lockToken you need to identify the unlock
     */
    public  String lock() throws InterruptedException {

        lock.lock() ;
        try {
            while(lockToken!=null) {
                unlocked.await();
            }
            lockToken=Long.toString(System.currentTimeMillis());

            return lockToken;

        } finally {
            lock.unlock();
        }

    }

    public void unlock(String lockToken) {

        lock.lock() ;
        try {
            if( this.lockToken==null ) {
                unlocked.signal();
                return;
            }

            if( this.lockToken.equals(lockToken)) {
               throw new IllegalStateException("The locktoken "+lockToken+" isn#T equals to the cuirrent locktoken "+this.lockToken);
            }
            lockToken=null;

        } finally {
            lock.unlock();
        }


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
                file=null;
            } catch (Exception e) {
            } finally {
                raf = null;
            }
        }
    }

    @Override
    public String getFilename() throws IOException {
        return this.file.getCanonicalPath();
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
    public void resetContent() throws IOException {

        if (this.isClosed()) {
            throw new IllegalStateException("Writer is closed");
        }
        this.getRandomAccessFile().getChannel().truncate(0);
    }

    @Override
    public UTFWriterImpl write(String value) throws IOException {
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
    public void reset() {
        try {
            this.close();
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }
    }


    private void open(File file) {
        try {
            this.raf = new RandomAccessFile(file, "rw");
            this.resetContent();
            this.file=file;
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }
    }


    private long position() throws IOException {
        return this.raf.getChannel().position();
    }


    @Override
    public String toString() {
        return "UTFWriterImpl{" +
                "file='" + file + '\'' +
                '}';
    }


}