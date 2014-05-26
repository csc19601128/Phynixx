package org.csc.phynixx.connection;

public interface IPhynixxConnectionProxyEvent {

	IPhynixxConnectionProxy getConnectionProxy();
	
	/**
	 * @return exception, that raises the event
	 */
	Exception getException();
	
	
}
