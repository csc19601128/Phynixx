package org.csc.phynixx.loggersystem.logger;

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


import org.csc.phynixx.loggersystem.logger.channellogger.AccessMode;

import java.io.IOException;

/**
 * A logger is enabled to write data in an atomic manner. The data is entirely written or the data is rejected.
 *
 * This class is not thread safe . Use facades to protect instances.
 * 
 * 
 */
public interface IDataLogger {

    /**
     * Sub-classes call this method to write log records with
     * a specific record type.
     *
     * @param type a record type defined in LogRecordType.
     * @param data record data to be logged.
     * @return a log key that can be used to de-reference the record.
     * <p/>
     */
    long write(short type, byte[][] data)
            throws InterruptedException, IOException;


    /**
     * callback method to replay the data of the logger.
     * @param replayListener
     * @throws IOException
     */
    void replay(IDataLoggerReplay replayListener) throws IOException;

    /**
     * close the Log files and perform necessary cleanup tasks. The content isn't discarded
     * The logger could be reopen
     */
    void close() throws IOException, InterruptedException;


    /**
     *
     * @return true if and if the logger is closed
     */
    boolean isClosed();

    /**
     * opens the logger with the specified ACCESS_MODE. If the logger isn't closed it is closed.
     *
     * @param accessMode
     * @throws IOException
     *
     * @see #reopen(org.csc.phynixx.loggersystem.logger.channellogger.AccessMode)
     */
    void open(AccessMode accessMode) throws IOException;

    /**
     * reopens the datalogger. It is assumed that the logger is open.
     * <pre>
     *    READ   - position to 0, content cannot be changed or added
     *    WRITE  - position to 0 and resets the committed size to 0. Content is deleted (kind of reset)
     *    APPEND - position to the committed size. Content is not effected.
     * </pre>
     *
     * @param accessMode
     * @throws IOException
     * @throws InterruptedException
     */
    void reopen(AccessMode accessMode) throws IOException, InterruptedException;


    /**
     * destroys the logger and removes its resources. The logger cannot be reopened
     *
     * @throws IOException
     */
    void destroy() throws IOException;
}
