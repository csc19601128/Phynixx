package org.csc.phynixx.connection;

public interface IPhynixxConnectionFactory {

	/**
	 * gets an new instance of the connection
	 */
	IPhynixxConnection getConnection() ; 
	
	
	/**
	 * @return the class of the connection's interface  
	 */
	Class connectionInterface(); 
	
	
	/**
	 * 
	 * closes the factory and releases all resources
	 */
	void close();
	
}
