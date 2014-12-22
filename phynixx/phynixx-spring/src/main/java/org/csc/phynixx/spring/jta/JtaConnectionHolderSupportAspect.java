/**
 * Copyright (C) 2014 Deutsche Post AG
 * All rights reserved.
 */
package org.csc.phynixx.spring.jta;

import org.aspectj.lang.annotation.Before;
import org.springframework.transaction.support.ResourceHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public abstract class JtaConnectionHolderSupportAspect<H extends ResourceHolder, K> {



	public abstract H getConnectionHolder(); 
	
	public abstract K getConnectionFactory(); 
	
	@Before("@annotation(org.csc.phynixx.spring.jta.JtaConnectionHolderSupport)")
	public void before() throws Throwable {

		System.out
				.println("Actual Transaction Active for JtaConnectionHolderSynchronization"
						+ TransactionSynchronizationManager.isActualTransactionActive());

		if (!TransactionSynchronizationManager.isActualTransactionActive()) {
			return;
		}
		Object connectionFactory = this.getConnectionFactory();
		if (TransactionSynchronizationManager.hasResource(connectionFactory)) {
			return;
		}
		ResourceHolder connectionHolder= this.getConnectionHolder();
		TransactionSynchronizationManager.bindResource(connectionFactory, connectionHolder);
		
		
		/**
		 * support die LifeCycle of then binding
		 */
		JtaConnectionHolderSynchronization<H, K> synchronization = 
				     new JtaConnectionHolderSynchronization(connectionHolder, connectionFactory);
		TransactionSynchronizationManager.registerSynchronization(synchronization); 


		// Hole aktuelle Connection

		System.out.println("*************************");
	}
}