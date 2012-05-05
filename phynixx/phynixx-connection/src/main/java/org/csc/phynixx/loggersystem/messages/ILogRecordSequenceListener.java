package org.csc.phynixx.loggersystem.messages;

public interface ILogRecordSequenceListener {

	void recordSequenceCreated(ILogRecordSequence sequence);
	
	void recordSequenceCompleted(ILogRecordSequence sequence);
	
}
