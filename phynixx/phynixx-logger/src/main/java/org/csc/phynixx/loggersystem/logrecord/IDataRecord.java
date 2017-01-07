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
 * a single record is consistent set logged data. this data is stored as
 * byte[][].
 * 
 * A data record gets an ordinal in the context of its owning message sequence
 * 
 * The write order is kept.
 */
public interface IDataRecord {


    /**
     * ordinal number of the message in the context of a message Sequence
     *
     * @return
     */
    Integer getOrdinal();


    /**
     * the id of the message sequence
     */
    long getXADataRecorderId();


    /**
     * @return the logRecord type of the current message
     */
    XALogRecordType getLogRecordType();


    /**
     * Data of the message ....
     *
     * @return data
     */
    byte[][] getData();


}
