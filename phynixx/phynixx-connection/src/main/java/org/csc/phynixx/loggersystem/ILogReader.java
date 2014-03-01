package org.csc.phynixx.loggersystem;

import java.io.IOException;
import java.util.List;

public interface ILogReader {
	
	/**
	 * scans the logger and re-instanciates all LogMessage sequences
	 * @return
	 * @throws IOException 
	 */
	void readMessageSequences() throws IOException ;
	
	
	/**
	 * 
	 * 
	 */
	List getOpenMessageSequences(); 

}
