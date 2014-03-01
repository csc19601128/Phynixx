package org.csc.phynixx.connection;


public class PhynixxConnectionProxyListenerAdapter implements IPhynixxConnectionProxyListener {

	/**
	 * NOOP
	 */
	public void connectionClosed(IPhynixxConnectionProxyEvent event) {
	}
	/**
	 * NOOP
	 */
	public void connectionErrorOccurred(IPhynixxConnectionProxyEvent event) {		
	}
	
	/**
	 * NOOP
	 */
	public void connectionRequiresTransaction(IPhynixxConnectionProxyEvent event) {
	}
	
	
	public void connectionCommitting(IPhynixxConnectionProxyEvent event) {
		
	}
	public void connectionPrepared(IPhynixxConnectionProxyEvent event) {
		
	}
	public void connectionPreparing(IPhynixxConnectionProxyEvent event) {
		
	}
	/**
	 * NOOP
	 */
	public void connectionCommitted(IPhynixxConnectionProxyEvent event) {
	}
	
	
	/**
	 * NOOP
	 */
	public void connectionRolledback(IPhynixxConnectionProxyEvent event) {		
	}
	

	/**
	 * NOOP
	 */
	public void connectionDereferenced(IPhynixxConnectionProxyEvent event) {		
	}

	/**
	 * NOOP
	 */
	public void connectionReferenced(IPhynixxConnectionProxyEvent event) {
		
	}
	public void connectionRecovered(IPhynixxConnectionProxyEvent event) {
		
	}
	public void connectionRecovering(IPhynixxConnectionProxyEvent event) {
		
	}
	
	
	
	
}
