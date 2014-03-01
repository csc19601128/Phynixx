package org.csc.phynixx.connection.reference;

import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxConnectionFactory;
import org.csc.phynixx.generator.IDGenerator;


public class ReferenceConnectionFactory implements IPhynixxConnectionFactory {

	private static final IDGenerator idGenerator= new IDGenerator(0);
    public IPhynixxConnection getConnection() 
    {
    	Object connectionId=null;
    	synchronized(idGenerator) {
    		connectionId= idGenerator.generate();
    	}
		return new ReferenceConnection(connectionId);
	}
	public Class connectionInterface() {
		return IReferenceConnection.class;
	}
	
	
	public void close() {
		
	}

    
}
