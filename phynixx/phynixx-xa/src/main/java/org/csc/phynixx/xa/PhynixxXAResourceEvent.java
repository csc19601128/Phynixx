package org.csc.phynixx.xa;

import java.util.EventObject;

import org.csc.phynixx.xa.IPhynixxXAResourceListener.IPhynixxXAResourceEvent;


public class PhynixxXAResourceEvent extends EventObject implements IPhynixxXAResourceEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 336547313996504512L;

	public PhynixxXAResourceEvent(PhynixxXAResource xaresource) {
		super(xaresource);
		
	}

	public PhynixxXAResource getXAResource() {
		return (PhynixxXAResource)this.getSource();
	}

}
