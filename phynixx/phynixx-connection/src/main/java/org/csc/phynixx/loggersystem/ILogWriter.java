package org.csc.phynixx.loggersystem;

import java.io.IOException;
import java.util.List;

import org.csc.phynixx.loggersystem.messages.ILogRecord;


public interface ILogWriter {
	
	void writeData(ILogRecord message) throws IOException;
	
	/**
	 * 
	 * 
	 */
	List getOpenMessageSequences(); 

}
