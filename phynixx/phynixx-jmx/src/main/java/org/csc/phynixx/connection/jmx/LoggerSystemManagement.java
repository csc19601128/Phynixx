package org.csc.phynixx.connection.jmx;

import org.csc.phynixx.loggersystem.ILoggerListener;
import org.csc.phynixx.loggersystem.XAResourceLogger;

public class LoggerSystemManagement implements LoggerSystemManagementMBean , ILoggerListener{
	
	private int openLoggerCounter= 0; 
	
	/* (non-Javadoc)
	 * @see org.csc.phynixx.connection.jmx.LoggerSystemManagementMBean#getOpenLoggers()
	 */
	public int getOpenLoggers() {
		return this.openLoggerCounter; 
	}

	public synchronized void loggerClosed(XAResourceLogger logger) {
		this.openLoggerCounter++;
		
	}

	public synchronized void loggerOpened(XAResourceLogger logger) {
		this.openLoggerCounter--;		
	}

	
}
