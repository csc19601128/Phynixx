package org.csc.phynixx.loggersystem;



import java.util.List;

import org.csc.phynixx.connection.IPhynixxConnectionProxyDecorator;
import org.csc.phynixx.connection.IPhynixxConnectionProxyListener;

/**
 * this IF represents a strategy assigning loggers to connections.
 * 
 * Different strategies could be :
 *       no logger, one logger per transaction, one logger per connection, one logger per System 
 * @author zf4iks2
 *
 */
public interface ILoggerSystemStrategy extends IPhynixxConnectionProxyListener, IPhynixxConnectionProxyDecorator {
	
	/**
	 * closes the strategy including all resources
	 */
	void close(); 
	
	/**
	 * recovers all Loggers of the system and returns a list of all open message sequences
	 * Each message sequence represents an incomplete transaction.
	 * To be able to recover the connection the message sequence is converted to a IMessageLogger
	 * 
	 * @return list of Objects of type IMessageLogger
	 */
	List readIncompleteTransactions() ;
	
	
}
