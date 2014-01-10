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


import org.csc.phynixx.loggersystem.messages.ILogRecordReplayListener;

import java.io.IOException;

public interface IDataLogger {

    String getLoggerName();

    /**
     * Sub-classes call this method to write log records with
     * a specific record type.
     *
     * @param type a record type defined in LogRecordType.
     * @param data record data to be logged.
     * @return a log key that can be used to de-reference the record.
     * <p/>
     * TODO was mache ich mit der reference -- besseres Konzept
     */
    long write(short type, byte[][] data)
            throws InterruptedException, IOException;


    void replay(ILogRecordReplayListener replayListener) throws IOException;

    /**
     * close the Log files and perform necessary cleanup tasks.
     * The logger could be reopended
     */
    void close() throws IOException, InterruptedException;

    boolean isClosed();

    void open() throws IOException, InterruptedException;


    /**
     * removes the logger an all its resources
     *
     * @throws IOException
     */
    void destroy() throws IOException;
}
