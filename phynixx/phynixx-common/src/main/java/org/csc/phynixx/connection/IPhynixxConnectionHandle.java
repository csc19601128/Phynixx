package org.csc.phynixx.connection;

public interface IPhynixxConnectionHandle {
	
	void setConnection(IPhynixxConnection connection) ;

	/**
     * @associates ICoreConnection
     * @supplierCardinality 0..1
     * 
     * @return the core connection
     * */
	IPhynixxConnection getConnection(); 
	
	    
}
