package org.csc.phynixx.loggersystem.messages;


/**
 * replays the messages in the correct order.
 * 
 * @author zf4iks2
 *
 */
public interface ILogRecordReplay {
	
	/**
	 * 
	 * @param record ILogMessage to be rollbacked
	 */
	void replayRollback(ILogRecord record);	
	
	/**
	 * 
	 * @param record to be rollforwared
	 */
	void replayRollforward(ILogRecord record);	


}
