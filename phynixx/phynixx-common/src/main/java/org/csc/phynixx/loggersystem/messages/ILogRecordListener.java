package org.csc.phynixx.loggersystem.messages;

public interface ILogRecordListener 
{
	
	void recordCreated(ILogRecord record);
	
	void recordCompleted(ILogRecord record);

}
