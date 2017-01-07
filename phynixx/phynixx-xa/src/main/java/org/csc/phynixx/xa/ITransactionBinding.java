package org.csc.phynixx.xa;

/*
 * #%L
 * phynixx-xa
 * %%
 * Copyright (C) 2014 Christoph Schmidt-Casdorff
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

import org.csc.phynixx.connection.IPhynixxConnection;
import org.csc.phynixx.connection.IPhynixxManagedConnection;

/**
 * Created by christoph on 16.02.14.
 */
public interface ITransactionBinding<C extends IPhynixxConnection> {

    /**
     * 
     * @return <code>null</code> if no transaction is associated
     */
	IPhynixxManagedConnection<C> getManagedConnection(); 

    /**
	 * current state
	 * 
	 * @return
	 */
	TransactionBindingType getTransactionBindingType();

	/**
	 * 
	 * @return true if an if getTransactionBindingType()==LocalTransaction
	 */
	boolean isLocalTransaction();

	/**
	 * 
	 * @return true if an if getTransactionBindingType()==GlobalTransaction
	 */
	boolean isGlobalTransaction();

	/**
	 * @return my be null if no TX has been starte
	 * @throws IllegalStateException
	 *             isGlobalTransaction()==false
	 */
	GlobalTransactionProxy<C> getEnlistedGlobalTransaction();

	/**
	 * 
	 * Lid reference an Entry in the {@link LocalTransactionProxy}
	 * 
	 * @return
	 * @throws IllegalStateException
	 *             isLocalTransaction()==false
	 */
	LocalTransactionProxy<C> getEnlistedLocalTransaction();

	/**
	 * Xid is added to the list of accociated XA and becomes the active XA the
	 * former active XID is deactivated.
	 * 
	 * If the transaction
	 * 
	 */
	void activateGlobalTransaction(GlobalTransactionProxy<C> proxy);

	/**
	 * 
	 */
	void activateLocalTransaction(LocalTransactionProxy<C> proxy);

	/**
	 * gibt die Bindung an die Transaction frei. TransactionBindingType gibt uebewr in {@link TransactionBindingType#NoTransaction}
	 * 
	 **/
	void release();

   void close();

	

}
