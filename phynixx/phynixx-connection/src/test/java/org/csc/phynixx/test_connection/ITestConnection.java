package org.csc.phynixx.test_connection;

import org.csc.phynixx.connection.IRecordLoggerAware;
import org.csc.phynixx.connection.IPhynixxConnection;

/**
 * the current implementation manages a internal counter which can be incremented
 * 
 * @author christoph
 *
 */
public interface ITestConnection extends IPhynixxConnection, IRecordLoggerAware {


	public static final int RF_INCREMENT=17;
	
	/**
	 * 
	 * @return current ID of the connection
	 */
	public Object getId();

	/**
	 * incrememnts the current counter 
	 * @param inc
	 */
	public void act(int inc) ;	

	
	/**
	 * 
	 * @return current counter
	 */
	public int getCurrentCounter();
	
}