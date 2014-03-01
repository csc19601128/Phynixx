package org.csc.phynixx.connection;

import org.csc.phynixx.loggersystem.messages.IRecordLogger;

public interface IRecordLoggerAware {

	public abstract void setRecordLogger(IRecordLogger logger);

	public abstract IRecordLogger getRecordLogger();

}