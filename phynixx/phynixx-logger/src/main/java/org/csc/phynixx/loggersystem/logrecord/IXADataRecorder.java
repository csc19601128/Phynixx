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


public interface IXADataRecorder extends IDataRecordSequence {

    /**
     * logs the given data
     * <p/>
     * These data can be replyed to perform rollback	 *
     * If commitRollforwardData is called once
     * this method can not be called any more
     *
     * @param data
     */
    void writeRollbackData(byte[] data);

    /**
     * logs the given data to perform rollback
     * If commitRollforwardData is called once
     * this method can not be called any more
     *
     * @param data
     */
    void writeRollbackData(byte[][] data);

    /**
     * logs the given data to perfrom rollforward
     * If commitRollforwardData is called once
     * this method can not be called any more
     *
     * @param data
     */
    void commitRollforwardData(byte[][] data);


    /**
     * logs the given data to perfrom rollforward
     * If commitRollforwardData is called once
     * this method can not be called any more
     *
     * @param data
     */
    void commitRollforwardData(byte[] data);

    /**
     * @return indicates that current sequence has received a XA_COMMIT message no more logrecord are
     * accepted except XA_DONE to complete the sequence ....
     */
    public boolean isCommitting();

    /**
     * @return indicates that current sequence is completed (received a XA_DONE) and no more logrecord are accepted
     */
    public boolean isCompleted();

    public boolean isPrepared();

    /**
     * @param replay
     */
    void replayRecords(IDataRecordReplay replay);
}
