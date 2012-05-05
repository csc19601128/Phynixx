package org.csc.phynixx.loggersystem.messages;

public interface IRecordLogger {

	/**
	 * logs the given data
	 * 
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
	 * 
	 * logs the given data to perfrom rollforward
	 * If commitRollforwardData is called once 
	 * this method can not be called any more 
	 * @param data
	 */
	void commitRollforwardData(byte[][] data);
	

	/**
	 * 
	 * logs the given data to perfrom rollforward
	 * If commitRollforwardData is called once 
	 * this method can not be called any more 
	 * @param data
	 */
	void commitRollforwardData(byte[] data);
	
	
	boolean isCommitting();
	
	boolean isPrepared(); 
	
	boolean isCompleted(); 
	
	/**
	 * 
	 * @param replay
	 */
	void replayRecords(ILogRecordReplay replay); 
}
