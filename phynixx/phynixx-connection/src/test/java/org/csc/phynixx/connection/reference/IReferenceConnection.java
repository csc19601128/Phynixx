package org.csc.phynixx.connection.reference;

import org.csc.phynixx.connection.IRecordLoggerAware;
import org.csc.phynixx.connection.IPhynixxConnection;

/**
 * the current implementation manages a internal counter which can be incremented
 * 
 * @author christoph
 *
 */
public interface IReferenceConnection extends IPhynixxConnection, IRecordLoggerAware 
{
	
	/**
	 * 
	 * @return current ID of the connection
	 */
	public Object getId();
	
	/**
	 * sets the counter to the initial value
	 * this value has to be restored if the connection is rollbacked
	 */
	void setInitialCounter(int value); 

	/**
	 * incrememnts the current counter 
	 * @param inc
	 */
	public void incCounter(int inc) ;	
	
	
	/**
	 * 
	 * @return current counter
	 */
	public int getCounter();
	
}