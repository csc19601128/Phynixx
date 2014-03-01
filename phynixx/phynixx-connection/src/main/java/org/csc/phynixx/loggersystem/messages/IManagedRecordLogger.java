package org.csc.phynixx.loggersystem.messages;

public interface IManagedRecordLogger extends IRecordLogger{
	
	public void close(); 

	public void destroy(); 
}
