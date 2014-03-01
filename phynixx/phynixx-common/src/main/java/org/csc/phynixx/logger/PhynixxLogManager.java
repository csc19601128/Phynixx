package org.csc.phynixx.logger;

public class PhynixxLogManager  {

	private static IPhynixxLogManager logManager= null; 
	
	static {
		logManager= new Log4jLogManager(); 
	}	
	
	
	public static IPhynixxLogManager getLogManager() {
		return logManager;
	}

	public static void setLogManager(IPhynixxLogManager logManager) {
		if( logManager==null) {
			throw new IllegalArgumentException("LogManager may not be null");
		}
		PhynixxLogManager.logManager = logManager;
	}

	public static IPhynixxLogger getLogger(Class cls) {
		return PhynixxLogManager.getLogManager().getLogger(cls);
	}

	public static IPhynixxLogger getLogger(String logger) {
		return PhynixxLogManager.getLogManager().getLogger(logger);
	}

}
