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
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *Implements an quit simple an cooperating lock. To have acces to write to the file, you have to aquire a lock. If the lock is successful, you gain an lockToken. This token identifies the lock and this token enables you to release the token.
 * If the file is lock all method to write/rad the file are unsynchronized to improve performance.
 */
public class UTFWriterImpl implements UTFWriter {


    Lock lock = new ReentrantLock();

    private Condition unlocked = lock.newCondition();

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
     * @return lockToken you need to identify the unlock
     */
    public String lock() throws InterruptedException {

        lock.lock();
        try {
            System.out.println("Locking Thread " + Thread.currentThread().getId());
            while (lockToken != null) {
                unlocked.await();
            }
            lockToken = Long.toString(System.currentTimeMillis());

            System.out.println("Lock acquired  Thread " + Thread.currentThread().getId());

            return lockToken;

        } finally {
            lock.unlock();
        }

    }

    /**
     *
     * @param lockToken identifing the lock
     */
    public void unlock(String lockToken) {

        lock.lock();
        try {
            System.out.println("Unlocking Thread " + Thread.currentThread().getId());
            if (this.lockToken != null) {
                if (!this.lockToken.equals(lockToken)) {
                    throw new IllegalStateException("The lock token " + lockToken + " isn#T equals to the cuirrent locktoken " + this.lockToken);
                }
                this.lockToken = null;
            }
            unlocked.signalAll();
            System.out.println("Unlocking successful Thread " + Thread.currentThread().getId());

        } finally {
            lock.unlock();
        }
    }

    /**
     * Schliesst die Datei und den FileChannel
     */
    @Override
    public void close() {

        if (raf != null) {
            // close Quietly
            try {
                // Schliessen der Daten-Datei
                raf.close();
                file = null;
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
    @Override
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
    public long write(String value) throws IOException {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        this.getRandomAccessFile().writeUTF(value);

        return  this.getRandomAccessFile().getChannel().position();
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


    @Override
    public long size() throws IOException {
        return this.getRandomAccessFile().getChannel().size();
    }

    @Override
    public void restoreSize(long filePosition) throws IOException {
        this.getRandomAccessFile().getChannel().truncate(filePosition);
        this.getRandomAccessFile().getChannel().position(filePosition);
    }


    RandomAccessFile getRandomAccessFile() {
        if (this.isClosed()) {
            throw new IllegalStateException("RandomAccessFile is close");
        }

        return raf;
    }



    private void open(File file) {
        try {
            this.raf = new RandomAccessFile(file, "rw");
            this.file = file;
        } catch (Exception e) {
            throw new DelegatedRuntimeException(e);
        }
    }


    @Override
    public long position() throws IOException {
        return this.raf.getChannel().position();
    }


    @Override
    public String toString() {
        return "UTFWriterImpl{" +
                "file='" + file + '\'' +
                '}';
    }


}