package org.csc.phynixx.test_connection;

import org.csc.phynixx.connection.IPhynixxConnectionProxy;
import org.csc.phynixx.connection.IPhynixxConnectionProxyFactory;

public class TestConnectionProxyFactory implements IPhynixxConnectionProxyFactory {

	public IPhynixxConnectionProxy getConnectionProxy() {
		return new TestConnectionProxy(); 
	}

}
