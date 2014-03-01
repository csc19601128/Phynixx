package org.csc.phynixx.loggersystem;

import java.io.File;
import java.io.IOException;

public interface ILoggerFactory {
	
	ILogger instanciateLogger(String loggerName) throws IOException; 
	
	/**
	 * Directory containing the logfiles 
	 */
	File getLoggingDirectory(); 

}
