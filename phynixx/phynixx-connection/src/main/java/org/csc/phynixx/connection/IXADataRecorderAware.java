package org.csc.phynixx.connection;

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


import org.csc.phynixx.loggersystem.logrecord.IDataRecordReplay;
import org.csc.phynixx.loggersystem.logrecord.IXADataRecorder;

/**
 * Connction is aware of a {@link IXADataRecorder}. The enviromne tof the connection provides a {@link IXADataRecorder} if necessary and
 * sets it via {@link #setXADataRecorder(org.csc.phynixx.loggersystem.logrecord.IXADataRecorder)}.
 */
public interface IXADataRecorderAware {


    /**
     * sets the current datzaRecorder. It is set bey the environment
     *
     * @param xaDataRecorder
     */
    void setXADataRecorder(IXADataRecorder xaDataRecorder);


    /**
     * @return the current xadataRecorder
     */
    IXADataRecorder getXADataRecorder();


    /**
     * This callback delivers the from the persistence store if the transaction/connection
     * has been interrupted an has ended abnormal. These data lets the connection recover its state at time time of interruption.
     *
     * @return callback for recovering data.
     */
    IDataRecordReplay recoverReplayListener();

}
