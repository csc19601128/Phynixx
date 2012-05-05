package org.csc.phynixx.connection.reference;

import org.csc.phynixx.connection.IPhynixxConnectionProxy;
import org.csc.phynixx.connection.IPhynixxConnectionProxyFactory;

public class ReferenceConnectionProxyFactory implements IPhynixxConnectionProxyFactory {

	public IPhynixxConnectionProxy getConnectionProxy() {
		return new ReferenceConnectionProxy(); 
	}

}
