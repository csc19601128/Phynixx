package org.csc.phynixx.loggersystem;

import java.io.IOException;

public interface ILogger {

	String getLoggerName(); 
	/**
	   * Sub-classes call this method to write log records with
	   * a specific record type.
	   * 
	   * @param type a record type defined in LogRecordType.
	 * @param data record data to be logged.
	   * @return a log key that can be used to reference
	   * the record.
	   */
	long write(short type, byte[][] data)
			throws InterruptedException, IOException;
	
	void replay(ILogRecordReplayListener replayListener) throws IOException ; 
	
	  /**
	   * close the Log files and perform necessary cleanup tasks.
	   */
	  void close() throws IOException, InterruptedException;

	  boolean isClosed();

	  
	  void open()   throws   IOException, InterruptedException;

}
