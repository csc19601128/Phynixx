/**
 * 
 */
package org.csc.phynixx.connection;

class RecoveryListener extends PhynixxConnectionProxyListenerAdapter implements IPhynixxConnectionProxyDecorator
{
	
	int recoveredConnections= 0;
	private int commmittedConnections= 0;
	private int rollbackedConnections= 0; 

	public int getRecoveredConnections() {
		return recoveredConnections;
	}

	public int getCommmittedConnections() {
		return commmittedConnections;
	}

	public int getRollbackedConnections() {
		return rollbackedConnections;
	}

	public void connectionRecovered(IPhynixxConnectionProxyEvent event) 
	{
		this.recoveredConnections++;
	}
	

	public void connectionCommitted(IPhynixxConnectionProxyEvent event) {
		this.commmittedConnections++;
	}

	public void connectionRollbacked(IPhynixxConnectionProxyEvent event) 
	{
		this.rollbackedConnections++;
	}

	public IPhynixxConnectionProxy decorate(IPhynixxConnectionProxy connectionProxy) {
		connectionProxy.addConnectionListener(this);
		return connectionProxy;
		
	}
	
	
	
}