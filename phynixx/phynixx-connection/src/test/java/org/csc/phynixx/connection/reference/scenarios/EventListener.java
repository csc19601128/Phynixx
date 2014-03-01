/**
 * 
 */
package org.csc.phynixx.connection.reference.scenarios;

import org.csc.phynixx.connection.IPhynixxConnectionProxy;
import org.csc.phynixx.connection.IPhynixxConnectionProxyDecorator;
import org.csc.phynixx.connection.IPhynixxConnectionProxyEvent;
import org.csc.phynixx.connection.PhynixxConnectionProxyListenerAdapter;

class EventListener extends PhynixxConnectionProxyListenerAdapter implements IPhynixxConnectionProxyDecorator
{
	
	private int recoveredConnections= 0;
	private int commmittedConnections= 0;
	private int rollbackedConnections= 0; 

	public int getRecoveredConnections() {
		return recoveredConnections;
	}

	public int getCommittedConnections() {
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

	public void connectionRolledback(IPhynixxConnectionProxyEvent event) 
	{
		this.rollbackedConnections++;
	}

	public IPhynixxConnectionProxy decorate(IPhynixxConnectionProxy connectionProxy) {
		connectionProxy.addConnectionListener(this);
		return connectionProxy;
		
	}

	public String toString() {
		return "Recovered Connections="+this.recoveredConnections+
		       " Rollbacked Connections="+rollbackedConnections+
		       " Committed Connections=" +commmittedConnections;
		
	}
	
	
	
}