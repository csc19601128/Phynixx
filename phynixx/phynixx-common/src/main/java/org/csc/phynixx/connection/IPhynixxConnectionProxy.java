package org.csc.phynixx.connection;


/**
 * this IF combines the role of a core connection and the role of a connection proxy.
 * 
 * Impl. of this IF represents the access to the core connections in this FW
 * 
 * @author christoph
 *
 */
public interface IPhynixxConnectionProxy extends IPhynixxConnection,IPhynixxConnectionHandle 
{
	void addConnectionListener(IPhynixxConnectionProxyListener listener) ;	
	
	void removeConnectionListener(IPhynixxConnectionProxyListener listener) ;	
}
