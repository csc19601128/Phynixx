package org.csc.phynixx.loggersystem;



public interface ILoggerListener {
	
	void loggerClosed(XAResourceLogger logger);
	
	void loggerOpened(XAResourceLogger logger);

}
