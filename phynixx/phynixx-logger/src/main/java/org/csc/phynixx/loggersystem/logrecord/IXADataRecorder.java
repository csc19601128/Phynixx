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
 * logs binary data. The persistence media depends on the implementation
 */
public interface IXADataRecorder extends IDataRecordSequence {

	/**
	 *
	 * @return true if no records
	 */
	boolean isEmpty();

	/**
	 * logs the given data
	 *
	 * These data can be replyed to perform rollback. If writeRollforwardData is
	 * called once this method can not be called any more
	 *
	 * @param data
	 */
	void writeRollbackData(byte[] data);

	/**
	 * logs the given data to perform rollback If writeRollforwardData is called
	 * once this method can not be called any more
	 *
	 * @param data
	 */
	void writeRollbackData(byte[][] data);

	/**
	 * logs the given data to perfrom rollforward If writeRollforwardData is
	 * called once this method can not be called any more
	 *
	 * @param data
	 */
	void writeRollforwardData(byte[][] data);

	/**
	 * logs the given data to perfrom rollforward If writeRollforwardData is
	 * called once this method can not be called any more
	 *
	 * @param data
	 */
	void writeRollforwardData(byte[] data);

	/**
	 * tries to recover all persistent information
	 */
	void recover();

	/**
	 * @return indicates that current sequence has received a ROLLFORWARD_DATA
	 *         message no more logrecord are accepted except XA_DONE to complete
	 *         the sequence ....
	 */
	public boolean isCommitting();

	/**
	 * @param replay
	 */
	void replayRecords(IDataRecordReplay replay);


	IDataRecord createDataRecord(XALogRecordType logRecordType,	byte[][] recordData);

	IDataRecord createDataRecord(XALogRecordType logRecordType,byte[] recordData);

	/**
	 * true g.t.w. logger ist disqualified, destroyed or reset
	 * @return
	 */
	boolean isClosed();
	/**
	 * closes the dataLogger, but keeps all resources, so the dataLogger can be
	 * reopened. The logger can't during the current session of the logging
	 * system.
	 * 
	 * This method is called, if the logger is meant to be recover and its content has to be saved til the next recovery. 
	 */
	void disqualify();

	/**
	 * resets the dataLogger and prepares it for reuse. All data can be discarded.
	 * The logger isn't closed
	 */
	void release();

	/**
	 * closes the dataRecorder and destroys all resources/content of the logger
	 */
	void destroy();
}
