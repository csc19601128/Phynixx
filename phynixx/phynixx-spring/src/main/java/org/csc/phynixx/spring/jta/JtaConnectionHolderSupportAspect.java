/**
 * Copyright (C) 2014 Deutsche Post AG
 * All rights reserved.
 */
package org.csc.phynixx.spring.jta;

/*
 * #%L
 * phynixx-spring
 * %%
 * Copyright (C) 2014 - 2015 Christoph Schmidt-Casdorff
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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