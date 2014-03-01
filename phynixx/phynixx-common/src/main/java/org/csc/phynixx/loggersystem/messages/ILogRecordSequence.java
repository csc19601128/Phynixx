package org.csc.phynixx.loggersystem.messages;

import java.util.List;

import org.csc.phynixx.loggersystem.XALogRecordType;


public interface ILogRecordSequence extends IRecordLogger,Comparable {

	/**
	 * 
	 * @return indicates that current sequence has received a XA_COMMIT message no more messages are 
	 * accepted except XA_DONE to complete the sequence ....
	 */
	public boolean isCommitting() ;
	
	/**
	 * @return indicates that current sequence is completed (received a XA_DONE) and no more messages are accepted
	 */
	public boolean isCompleted(); 

	public boolean isPrepared();
	
	
	public List getMessages();	
	
	/**
	 * 
	 * creates a new LogMessage containing user-data
	 * @return
	 */
	public ILogRecord createNewMessage(XALogRecordType logRecordType); 
	
	/**
	 * 
	 * creates a new LogMessage 
	 * @return
	 */
	public ILogRecord createNewLogRecord() ;

	
	/**
	 * 
	 * @return id of the sequence of messages 
	 */
	public Long getLogRecordSequenceId();
	
	public void addLogRecordListener(ILogRecordListener listener);
	
	public void removeLogRecordListener(ILogRecordListener listener) ;
	
	public void addLogRecordSequenceListener(ILogRecordSequenceListener listener) ;

	public void removeLogRecordSequenceListener(ILogRecordSequenceListener listener) ;

    /**
     * @supplierCardinality 0..* 
     */
    /*# ILogMessageListener lnkILogMessageListener; */

    /**
     * @supplierCardinality 0..* 
     */
    /*# ILogMessageSequenceListener lnkILogMessageSequenceListener; */

    /**
     * @label creates
     * @modifiedDate 21.01.02008 17:55 
     */
    /*# ILogMessage lnkILogMessage; */
}