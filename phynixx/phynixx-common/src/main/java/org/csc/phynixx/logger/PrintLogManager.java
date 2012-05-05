package org.csc.phynixx.logger;


public class PrintLogManager implements IPhynixxLogManager {
	
	private PrintLogger printLogger= new PrintLogger(); 
	

	
	public PrintLogManager() {
		printLogger.setLogLevel(PrintLogger.ERROR);
	}

		
	public PrintLogManager(Integer logLevel) {
		printLogger.setLogLevel(logLevel);
	}

	public IPhynixxLogger getLogger(Class cls) {
		return printLogger;
	}

	public IPhynixxLogger getLogger(String logger) {
		return printLogger;
	}
}
