package org.csc.phynixx.connection;


public interface IPhynixxConnectionProxyListener {

	/**
	 * called when the connection is closed
	 * Called after closing the connection
	 * @param con current connection
	 */
	 void connectionClosed(IPhynixxConnectionProxyEvent event );
	 
	/**
	  * connectionErrorOccurred — triggered when a fatal error, 
	  * such as the server crashing, causes the connection to be lost
	  * @param con current connection
	  */
	void connectionErrorOccurred(IPhynixxConnectionProxyEvent event);
	
	/**
	 * connectionRequiresTransaction - an action should be performed that 
	 * must be transactional
	 * @param con current connection
	 */
	void connectionRequiresTransaction(IPhynixxConnectionProxyEvent event) ;
	
	/**
	 * connectionRolledback
	 */
	void connectionRolledback(IPhynixxConnectionProxyEvent event) ;
	
	/**
	 * connection enters state 'committing'
	 * @param event
	 */
	void connectionCommitting(IPhynixxConnectionProxyEvent event);

	/**
	 * connectionCommitted
	 */
	void connectionCommitted(IPhynixxConnectionProxyEvent event) ;
	
	/**
	 * connection enters state 'preparing'
	 * @param event
	 */
	void connectionPreparing(IPhynixxConnectionProxyEvent event);

	
	/**
	 * connection is prepared 
	 */
	void connectionPrepared(IPhynixxConnectionProxyEvent event);

	
	/**
	 * connection dereferenced
	 */
	void connectionDereferenced(IPhynixxConnectionProxyEvent event);
	
	/**
	 * connection referenced
	 **/
	void connectionReferenced(IPhynixxConnectionProxyEvent event);

	
	/**
	 * connection enters state 'recovering'
	 * @param event
	 */
	void connectionRecovering(IPhynixxConnectionProxyEvent event);

	/**
	 * connection recovered
	 */
	void connectionRecovered(IPhynixxConnectionProxyEvent event) ;
	
	
}
