package org.csc.phynixx.logger;


public class Log4jLogManager implements IPhynixxLogManager {
	
	public IPhynixxLogger getLogger(Class cls) {
		return new Log4jLogger(cls);
	}

	public IPhynixxLogger getLogger(String logger) {
		return new Log4jLogger(logger);
	}
}
