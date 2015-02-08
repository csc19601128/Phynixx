package org.csc.phynixx.loggersystem.logger;

import org.csc.phynixx.loggersystem.logrecord.IDataRecordReplay;
import org.csc.phynixx.loggersystem.logrecord.PhynixxXADataRecorder;
import org.csc.phynixx.loggersystem.logrecord.XALogRecordType;

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
 * callback to obtain the log records of a {@link PhynixxXADataRecorder}.
 * 
 *  @see PhynixxXADataRecorder#replayRecords(IDataRecordReplay)
 * @author christoph
 *
 */
public interface IDataLoggerReplay {

    /**
     * replays the records written.
     *
     * @param type
     * @param message
     */
    void onRecord(XALogRecordType type, byte[][] message);

}
