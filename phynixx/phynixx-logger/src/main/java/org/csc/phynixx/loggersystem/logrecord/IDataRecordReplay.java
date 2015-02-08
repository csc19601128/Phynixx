package org.csc.phynixx.loggersystem.logrecord;

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


/**
 * replays the log record in the right order.
 * 
 * This class is a callback when reading the content of a {@link IDataRecordSequence}
 *
 * @author Christoph Schmidt-Casdorff
 */
public interface IDataRecordReplay {

    /**
     * rolls back
     *
     * @param record ILogMessage to be rollbacked
     */
    void replayRollback(IDataRecord record);

    /**
     * rollforward moves from prepared to committed
     *
     * @param record to be rollforwared
     */
    void replayRollforward(IDataRecord record);


    /**
     * indicates that all data is transfered
     */
    void notifyNoMoreData();


}
