package org.csc.phynixx.xa;

import javax.transaction.xa.XAResource;

import org.csc.phynixx.connection.IPhynixxConnection;


/**
 * 
 * keeps the XAResource's relation to the connection. 
 * 
 * @author christoph
 *
 */
public interface IPhynixxXAConnection {

	public XAResource getXAResource() ;

	public IPhynixxConnection getConnection() ;
}
