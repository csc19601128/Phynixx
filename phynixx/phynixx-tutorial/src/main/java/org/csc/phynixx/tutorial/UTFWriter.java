package org.csc.phynixx.tutorial;

/*
 * #%L
 * phynixx-tutorial
 * %%
 * Copyright (C) 2014 Christoph Schmidt-Casdorff
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


import org.csc.phynixx.connection.RequiresTransaction;

import java.io.IOException;
import java.util.List;

/**
 * Created by Christoph Schmidt-Casdorff on 04.02.14.
 */
public interface UTFWriter{

    /**
     *
     * @return lockToken you need to identify the unlock
     */
    String lock() throws InterruptedException;

    void unlock(String lockToken);

    boolean isClosed();

    /**
     * resets the content of the file associated with die current transaction
     * @throws IOException
     */
    @RequiresTransaction
    void resetContent() throws IOException;

    /**writes a string to file
     *
     * @param value
     * @return
     * @throws IOException
     */
    @RequiresTransaction
    long write(String value) throws IOException;

    void close();

    @RequiresTransaction
    /**
     * opens a file and associates it with the current transaction
     */
    String getFilename() throws IOException;


    /**
     * reads the content
     * @return
     */
    List<String> readContent() throws IOException;

    long size() throws IOException;

    void restoreSize(long filePosition) throws IOException;

    long position() throws IOException;
}
