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
import org.csc.phynixx.loggersystem.logrecord.ILogRecordReplayListener;

import java.io.IOException;

/**
 * A logger ist able to write data in an atomic manner. The data is entirely written or the data ist rejected.
 *
 * This class is not thread safe . Use facades to protect instances
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
    void replay(ILogRecordReplayListener replayListener) throws IOException;

    /**
     * close the Log files and perform necessary cleanup tasks.
     * The logger could be reopended
     */
    void close() throws IOException, InterruptedException;

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
     *    READ   - position to position 0
     *    WRITE  - position to position 0 and resets the committed size to 0
     *    APPEND - position to the committed size
     * </pre>
     *
     * @param accessMode
     * @throws IOException
     * @throws InterruptedException
     */
    void reopen(AccessMode accessMode) throws IOException, InterruptedException;


    /**
     * removes the logger an all its resources
     *
     * @throws IOException
     */
    void destroy() throws IOException;
}
