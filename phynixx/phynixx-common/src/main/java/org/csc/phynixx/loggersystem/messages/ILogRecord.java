package org.csc.phynixx.loggersystem.messages;

import org.csc.phynixx.loggersystem.XALogRecordType;

public interface ILogRecord extends Comparable{
	
	
	/**
	 * ordinal number of the message in the space of a message Sequence
	 * @return
	 */
	Integer getOrdinal();
	
	
	/**
	 * the id of the message sequence
	 */
	Long getRecordSequenceId();
	
	
	/**
	 * @return the logRecord type of the current message
	 * 
	 *
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
	 * @throws IllegalStateException message is readonly 
	 * @param data
	 * 
	 * @see #isReadOnly()
	 */
	void setData(byte[] data) ;
	
	/**
	 * sets the data. The message is written to the logger and can not be modified	 * 
	 * 
	 * @throws IllegalStateException message is readonly 
	 * @param data
	 * 
	 * @see #isReadOnly()
	 */
	void setData(byte[][] data) ;
	
	
	/**
	 * A message is modifiable, if isn't written to the log system. Once written it can not be modified
	 * 
	 *  A message recovered from a log system is read only.
	 *  
	 * @returns if the current message can be modified
	 */
	
	boolean isReadOnly(); 

}
