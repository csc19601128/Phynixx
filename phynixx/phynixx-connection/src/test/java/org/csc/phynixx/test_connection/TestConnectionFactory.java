package org.csc.phynixx.test_connection;

import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxConnectionFactory;
import org.csc.phynixx.generator.IDGenerator;


public class TestConnectionFactory implements IPhynixxConnectionFactory {

	private static final IDGenerator idGenerator= new IDGenerator(0);
    public IPhynixxConnection getConnection() 
    {
    	Object connectionId=null;
    	synchronized(idGenerator) {
    		connectionId= idGenerator.generate();
    	}
		return new TestConnection( connectionId);
	}
	public Class connectionInterface() {
		return ITestConnection.class;
	}


	public void close() {
		
	}
    
}
