package org.csc.phynixx.loggersystem.messages;

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


import org.csc.phynixx.loggersystem.XALogRecordType;

public interface ILogRecord extends Comparable {


    /**
     * ordinal number of the message in the space of a message Sequence
     *
     * @return
     */
    Integer getOrdinal();


    /**
     * the id of the message sequence
     */
    Long getRecordSequenceId();


    /**
     * @return the logRecord type of the current message
     */
    XALogRecordType getLogRecordType();


    /**
     * Data of the message ....
     *
     * @return
     */
    byte[][] getData();

    /**
     * sets the data. The message is written to the logger and can not be modified	 *
     *
     * @param data
     * @throws IllegalStateException message is readonly
     * @see #isReadOnly()
     */
    void setData(byte[] data);

    /**
     * sets the data. The message is written to the logger and can not be modified	 *
     *
     * @param data
     * @throws IllegalStateException message is readonly
     * @see #isReadOnly()
     */
    void setData(byte[][] data);


    /**
     * A message is modifiable, if isn't written to the log system. Once written it can not be modified
     * <p/>
     * A message recovered from a log system is read only.
     *
     * @returns if the current message can be modified
     */

    boolean isReadOnly();

}
