package org.csc.phynixx.xa;


public interface IPhynixxXAResourceListener {

	interface IPhynixxXAResourceEvent {
	
		PhynixxXAResource getXAResource();
	}	
	
	void closed(IPhynixxXAResourceEvent event) ;

}
