package org.csc.phynixx.connection;

import org.csc.phynixx.exceptions.SampleTransactionalException;


public interface IPhynixxConnection // extends IMessageLoggerAware 
{

	/**
	 * 
	 * @throws SampleTransactionalException
	 */
	void commit();
	
	
	/**
	 * 
	 * 
	 * @throws SampleTransactionalException
	 * 
	 */
	void rollback(); 
	
	
	/**
	 * 
	 * 
	 * @throws SampleTransactionalException
	 */
	void close(); 
	
	/*
	 * 
	 */
	boolean isClosed();
	
	
	/**
	 * 
	 * 
	 * @throws SampleTransactionalException
	 */
	void prepare(); 	
	

	void recover(); 

		
}
