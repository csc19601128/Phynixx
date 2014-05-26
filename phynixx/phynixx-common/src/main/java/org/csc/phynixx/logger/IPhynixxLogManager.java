package org.csc.phynixx.logger;

public interface IPhynixxLogManager {
	
	public IPhynixxLogger getLogger(Class cls) ;

	public IPhynixxLogger getLogger(String logger) ;
	
	

}