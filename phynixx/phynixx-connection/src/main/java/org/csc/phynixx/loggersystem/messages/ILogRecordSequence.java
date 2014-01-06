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

import java.util.List;


public interface ILogRecordSequence extends IRecordLogger, Comparable {

    /**
     * @return indicates that current sequence has received a XA_COMMIT message no more messages are
     * accepted except XA_DONE to complete the sequence ....
     */
    public boolean isCommitting();

    /**
     * @return indicates that current sequence is completed (received a XA_DONE) and no more messages are accepted
     */
    public boolean isCompleted();

    public boolean isPrepared();


    public List getMessages();

    /**
     * creates a new LogMessage containing user-data
     *
     * @return
     */
    public ILogRecord createNewMessage(XALogRecordType logRecordType);

    /**
     * creates a new LogMessage
     *
     * @return
     */
    public ILogRecord createNewLogRecord();


    /**
     * @return id of the sequence of messages
     */
    public Long getLogRecordSequenceId();

    public void addLogRecordListener(ILogRecordListener listener);

    public void removeLogRecordListener(ILogRecordListener listener);

    public void addLogRecordSequenceListener(ILogRecordSequenceListener listener);

    public void removeLogRecordSequenceListener(ILogRecordSequenceListener listener);

    /**
     * @supplierCardinality 0..* 
     */
    /*# ILogMessageListener lnkILogMessageListener; */

    /**
     * @supplierCardinality 0..* 
     */
    /*# ILogMessageSequenceListener lnkILogMessageSequenceListener; */

    /**
     * @label creates
     * @modifiedDate 21.01.02008 17:55 
     */
    /*# ILogMessage lnkILogMessage; */
}
